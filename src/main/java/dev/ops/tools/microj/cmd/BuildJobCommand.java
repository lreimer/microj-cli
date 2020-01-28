package dev.ops.tools.microj.cmd;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine.Command;

import static picocli.CommandLine.Option;

@Command(name = "buildjob", description = "Create and register a new build job")
public class BuildJobCommand implements Runnable {

    private static final Logger LOGGER = LoggerFactory.getLogger(BuildJobCommand.class);

    @Option(names = {"-n", "--name"}, description = "Build job name", required = true)
    private String name;

    @Override
    public void run() {
        LOGGER.info("Creating new Jenkins build job {}", name);
        throw new UnsupportedOperationException("// TODO: Implement me.");
    }
}
