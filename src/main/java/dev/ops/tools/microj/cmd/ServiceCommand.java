package dev.ops.tools.microj.cmd;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(name = "service", description = "Create a new microservice")
public class ServiceCommand implements Runnable {

    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceCommand.class);

    @Option(names = {"-n", "--name"}, description = "Service name", required = true)
    private String name;

    @Override
    public void run() {
        LOGGER.info("Creating new service {}", name);

    }
}
