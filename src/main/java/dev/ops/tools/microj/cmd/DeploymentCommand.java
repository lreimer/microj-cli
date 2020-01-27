package dev.ops.tools.microj.cmd;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine.Command;

import static picocli.CommandLine.Option;

@Command(name = "deployment", description = "Make a deployment")
public class DeploymentCommand implements Runnable {

    private static final Logger LOGGER = LoggerFactory.getLogger(DeploymentCommand.class);

    @Option(names = {"-n", "--namespace"}, description = "The namespace", required = true)
    private String namespace;

    @Override
    public void run() {
        LOGGER.info("Making service deployment in namespace {}", namespace);
    }
}
