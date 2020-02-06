package dev.ops.tools.microj.cmd;

import com.hubspot.jinjava.Jinjava;
import com.hubspot.jinjava.JinjavaConfig;
import com.hubspot.jinjava.loader.FileLocator;
import dev.ops.tools.microj.MicrojCli;
import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine.Command;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Option;
import picocli.CommandLine.ParameterException;
import picocli.CommandLine.Spec;

import java.io.*;
import java.net.Authenticator;
import java.net.MalformedURLException;
import java.net.PasswordAuthentication;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static picocli.CommandLine.ParentCommand;

/**
 * The subcommand to create and bootstrap a new microservice using a template.
 */
@Command(name = "service", description = "Create a new microservice")
public class ServiceCommand implements Runnable {

    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceCommand.class);

    @Spec
    private CommandSpec spec;

    @ParentCommand
    private MicrojCli cli;

    @Option(names = {"-n", "--name"}, description = "Service name", required = true)
    private String name;

    @Option(names = {"-r", "--repository"}, description = "Repository URL", defaultValue = "https://dl.bintray.com/qaware-oss/maven/")
    private String repository;

    @Option(names = {"-t", "--template"}, description = "Template coordinate")
    private String template;

    @Option(names = {"-o", "--overwrite"}, description = "Overwrite service")
    private boolean overwrite;

    @Override
    public void run() {
        LOGGER.info("Creating new service {} from template {}", name, template);

        Path directory = createDirectory();
        Path file = downloadTemplate();

        extract(directory, file);
        renderTemplates(directory);
    }

    private void extract(Path directory, Path file) {
        LOGGER.debug("Extracting template file {} into {}", file, directory);

        try (ArchiveInputStream i = new ArchiveStreamFactory().createArchiveInputStream(getExtension(), new FileInputStream(file.toFile()))) {
            ArchiveEntry entry;
            while ((entry = i.getNextEntry()) != null) {
                if (!i.canReadEntryData(entry)) {
                    continue;
                }

                File f = new File(directory.toFile(), entry.getName());
                if (entry.isDirectory()) {
                    if (!f.isDirectory() && !f.mkdirs()) {
                        throw new IOException("Failed to create directory " + f);
                    }
                } else {
                    File parent = f.getParentFile();
                    if (!parent.isDirectory() && !parent.mkdirs()) {
                        throw new IOException("Failed to create parent directory " + parent);
                    }
                    try (OutputStream o = Files.newOutputStream(f.toPath())) {
                        IOUtils.copy(i, o);
                    }
                }
            }
        } catch (IOException | ArchiveException | IllegalArgumentException e) {
            throw new IllegalStateException("Unable to extract template " + file.toString(), e);
        }
    }

    private Path createDirectory() {
        Path target = Paths.get(name).toAbsolutePath();

        try {
            if (target.toFile().exists() && overwrite) {
                return target;
            }
            return Files.createDirectory(target);
        } catch (IOException e) {
            throw new IllegalStateException("Unable to create project directory.", e);
        }
    }

    private Path downloadTemplate() {
        URL templateUrl = getTemplateURL();
        LOGGER.info("Downloading template file from {}", templateUrl);

        setDefaultAuthenticator();

        try (InputStream in = templateUrl.openStream()) {
            Path tempFile = Files.createTempFile("microj-", "." + getExtension());
            Files.copy(in, tempFile, REPLACE_EXISTING);
            return tempFile;
        } catch (IOException e) {
            throw new IllegalStateException("Unable to download template.", e);
        }
    }

    private String getTemplatePath() {
        String[] coordinates = template.split(":");

        StringBuilder builder = new StringBuilder();
        builder.append(coordinates[0].replace('.', '/')).append('/');
        builder.append(coordinates[1]).append('/');
        builder.append(getVersion()).append('/');
        builder.append(coordinates[1]).append('-');
        builder.append(getVersion());
        builder.append(".").append(getExtension());

        return builder.toString();
    }

    public URL getTemplateURL() {
        try {
            return new URL(repository.concat(getTemplatePath()));
        } catch (MalformedURLException e) {
            throw new ParameterException(spec.commandLine(), "Invalid template URL", e);
        }
    }

    private String getVersion() {
        String[] coordinates = template.split(":");
        String[] version = coordinates[2].split("@");
        return version[0];
    }

    private String getExtension() {
        String[] coordinates = template.split(":");
        String[] version = coordinates[2].split("@");
        return version.length == 2 ? version[1].toLowerCase() : "jar";
    }

    private Jinjava jinja(Path directory) {
        Jinjava jinjava = new Jinjava(new JinjavaConfig());
        try {
            jinjava.setResourceLocator(new FileLocator(directory.toFile()));
        } catch (FileNotFoundException e) {
            throw new IllegalStateException("Unable to initialize Jinja.", e);
        }
        return jinjava;
    }

    private void renderTemplates(Path directory) {
        Jinjava jinja = jinja(directory);
        try (Stream<Path> pathStream = Files.walk(directory)) {
            pathStream.filter(p -> p.toString().endsWith(".jinja")).forEach(f -> renderTemplate(jinja, f));
        } catch (IOException e) {
            LOGGER.warn("Error processing templates.", e);
        }
    }

    private void renderTemplate(Jinjava jinja, Path file) {
        Map<String, Object> model = new HashMap<>();
        model.put("serviceName", name);

        File templateFile = file.toFile();
        File parsedFile = new File(templateFile.getParent(), templateFile.getName().replace(".jinja", ""));

        try (Writer out = new FileWriter(parsedFile)) {
            String jinjaTemplate = FileUtils.readFileToString(templateFile, Charset.defaultCharset());
            out.write(jinja.render(jinjaTemplate, model));
            Files.delete(templateFile.toPath());
        } catch (IOException e) {
            LOGGER.warn("Unable to render template.", e);
        }
    }

    private void setDefaultAuthenticator() {
        if (StringUtils.isNotEmpty(cli.getUserName())) {
            Authenticator.setDefault(new RepositoryAuthenticator());
        }
    }

    public class RepositoryAuthenticator extends Authenticator {
        @Override
        protected PasswordAuthentication getPasswordAuthentication() {
            return new PasswordAuthentication(cli.getUserName(), cli.getPassword());
        }
    }
}
