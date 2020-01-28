package dev.ops.tools.microj.cmd;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine.Command;

import static picocli.CommandLine.Option;

@Command(name = "hello", description = "Print Hello Microj CLI message")
public class HelloCommand implements Runnable {

    private static final Logger LOGGER = LoggerFactory.getLogger(HelloCommand.class);

    @Option(names = {"-m", "--message"}, description = "The message", defaultValue = "Hello Microj CLI")
    private String message;

    @Override
    public void run() {
        LOGGER.info("{}", message);
    }

}
