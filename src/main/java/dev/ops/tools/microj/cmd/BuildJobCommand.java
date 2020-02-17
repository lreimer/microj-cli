package dev.ops.tools.microj.cmd;

import com.google.common.base.Optional;
import com.hubspot.jinjava.Jinjava;
import com.hubspot.jinjava.JinjavaConfig;
import com.offbytwo.jenkins.JenkinsServer;
import com.offbytwo.jenkins.model.FolderJob;
import com.offbytwo.jenkins.model.JobWithDetails;
import dev.ops.tools.microj.MicrojCli;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.RemoteListCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.transport.RemoteConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine.*;
import picocli.CommandLine.Model.CommandSpec;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Create and register a new build job for the current project.
 */
@Command(name = "buildjob", description = "Create and register a new build job")
public class BuildJobCommand implements Runnable {

    private static final Logger LOGGER = LoggerFactory.getLogger(BuildJobCommand.class);

    @Spec
    private CommandSpec spec;

    @ParentCommand
    private MicrojCli cli;

    @Option(names = {"-n", "--name"}, description = "Build job name", required = true)
    private String name;

    @Option(names = {"-c", "--config"}, description = "Jenkins config XML template", required = true)
    private String config;

    @Option(names = {"-f", "--folder"}, description = "Folder name and hierarchy")
    private String folder;

    @Option(names = {"-d", "--directory"}, description = "Project directory", defaultValue = ".")
    private String directory;

    @Option(names = {"-s", "--server"}, description = "Server URI", required = true)
    private URI serverUri;

    @Override
    public void run() {
        String gitRemoteUrl = getGitRemoteURL();
        LOGGER.info("Creating new Jenkins build job {} for remote Git repository {}", name, gitRemoteUrl);

        String jobXml = getJobXml(gitRemoteUrl);
        createJob(jobXml);
    }

    private void createJob(String jobXml) {
        try (JenkinsServer jenkinsServer = new JenkinsServer(serverUri, cli.getUserName(), new String(cli.getPassword()))) {

            Optional<FolderJob> folderJob = Optional.absent();

            if (StringUtils.isNotEmpty(folder)) {
                JobWithDetails job = jenkinsServer.getJob(folder);
                if (job != null) {
                    folderJob = jenkinsServer.getFolderJob(job);
                }
            }

            jenkinsServer.createJob(folderJob.orNull(), name, jobXml, true);
        } catch (IOException e) {
            throw new IllegalStateException("Unable to create Jenkins job.", e);
        }
    }

    private String getJobXml(String gitRemoteUrl) {
        Map<String, Object> context = new HashMap<>();
        context.put("uuid", UUID.randomUUID().toString());
        context.put("name", name);
        context.put("remote", gitRemoteUrl);

        Jinjava jinja = jinja();

        String template;
        try {
            template = FileUtils.readFileToString(new File(config), Charset.defaultCharset());
        } catch (IOException e) {
            throw new ParameterException(spec.commandLine(), "Unable to read Jenkins job config XML.", e);
        }
        return jinja.render(template, context);
    }

    private String getGitRemoteURL() {
        try (Git git = Git.open(new File(directory))) {
            RemoteListCommand remoteList = git.remoteList();
            List<RemoteConfig> remoteConfigs = remoteList.call();
            return remoteConfigs.get(0).getURIs().get(0).toASCIIString();
        } catch (IOException | GitAPIException | IndexOutOfBoundsException e) {
            throw new ParameterException(spec.commandLine(), "Directory needs to be a remote Git repository.", e);
        }
    }

    private Jinjava jinja() {
        return new Jinjava(new JinjavaConfig());
    }
}
