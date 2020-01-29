package dev.ops.tools.microj.cmd;

import com.hubspot.jinjava.Jinjava;
import com.hubspot.jinjava.JinjavaConfig;
import com.hubspot.jinjava.loader.FileLocator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine.Command;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Option;
import picocli.CommandLine.ParameterException;
import picocli.CommandLine.Spec;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Stream;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

@Command(name = "service", description = "Create a new microservice")
public class ServiceCommand implements Runnable {

    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceCommand.class);

    @Spec
    private CommandSpec spec;

    @Option(names = {"-n", "--name"}, description = "Service name", required = true)
    private String name;

    @Option(names = {"-r", "--repository"}, description = "Repository URL", defaultValue = "https://repo.maven.apache.org/maven2/")
    private String repository;

    @Option(names = {"-t", "--template"}, description = "Template coordinate", required = true)
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

        try (JarFile jar = new JarFile(file.toFile())) {
            // create directories first
            jar.stream().forEach(entry -> {
                File f = new File(directory.toFile(), entry.getName());
                if (entry.getName().endsWith("/")) {
                    f.mkdirs();
                }
            });

            // then extract the files
            jar.stream().forEach(entry -> {
                File f = new File(directory.toFile(), entry.getName());
                if (!entry.getName().endsWith("/")) {
                    extractFile(jar, entry, f);
                }
            });
        } catch (IOException e) {
            throw new IllegalStateException("Invalid template JAR.", e);
        }
    }

    private void extractFile(JarFile jar, JarEntry entry, File f) {
        try {
            Files.copy(jar.getInputStream(entry), f.toPath(), REPLACE_EXISTING);
        } catch (IOException e) {
            throw new IllegalStateException("Unable to extract template JAR entry.", e);
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

        try (InputStream in = templateUrl.openStream()) {
            Path tempFile = Files.createTempFile("microj-", getExtension());
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
        builder.append(coordinates[2]).append('/');
        builder.append(coordinates[1]).append('-').append(coordinates[2]).append(getExtension());

        return builder.toString();
    }

    public URL getTemplateURL() {
        try {
            return new URL(repository.concat(getTemplatePath()));
        } catch (MalformedURLException e) {
            throw new ParameterException(spec.commandLine(), "Invalid template URL", e);
        }
    }

    private String getExtension() {
        String[] coordinates = template.split(":");
        String[] version = coordinates[2].split("@");
        return version.length == 2 ? "." + version[1] : ".jar";
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
            // filter all Freemarker templates
            pathStream.filter(p -> p.endsWith(".jinja")).forEach(f -> {
                renderTemplate(jinja, f);
            });
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
            out.write(jinja.render(template, model));
            Files.delete(templateFile.toPath());
        } catch (IOException e) {
            LOGGER.warn("Unable to render template.", e);
        }
    }
}
