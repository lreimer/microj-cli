package dev.ops.tools.microj.cmd;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine.Command;

import java.util.List;

import static picocli.CommandLine.Option;
import static picocli.CommandLine.Parameters;

@Command(name = "hello", description = "Print message")
public class HelloCommand implements Runnable {

    private static final Logger LOGGER = LoggerFactory.getLogger(HelloCommand.class);

    @Option(names = {"-m", "--message"}, description = "The message", defaultValue = "Hello Microj CLI")
    private String message;

    @Parameters(description = "Arbitrary command line parameters")
    private List<String> parameters;

    @Override
    public void run() {
        LOGGER.info("{} - {}", message, parameters);
    }

}
