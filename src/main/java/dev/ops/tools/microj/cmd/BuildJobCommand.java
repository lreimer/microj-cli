package dev.ops.tools.microj.cmd;

import dev.ops.tools.microj.MicrojCli;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine.Command;
import picocli.CommandLine.ParentCommand;

import static picocli.CommandLine.Option;

/**
 * Create and register a new build job for the current project.
 */
@Command(name = "buildjob", description = "Create and register a new build job")
public class BuildJobCommand implements Runnable {

    private static final Logger LOGGER = LoggerFactory.getLogger(BuildJobCommand.class);

    @ParentCommand
    private MicrojCli cli;

    @Option(names = {"-n", "--name"}, description = "Build job name", required = true)
    private String name;

    @Override
    public void run() {
        LOGGER.info("Creating new Jenkins build job {}", name);
        throw new UnsupportedOperationException("// TODO: Implement me.");
    }
}
