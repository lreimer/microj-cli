package dev.ops.tools.microj.cmd;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine.Command;

import java.util.List;

import static picocli.CommandLine.Option;
import static picocli.CommandLine.Parameters;

/**
 * Simple CLI subcommand to print an info and hello message.
 */
@Command(name = "info", description = "Print info message")
public class InfoCommand implements Runnable {

    private static final Logger LOGGER = LoggerFactory.getLogger(InfoCommand.class);

    @Option(names = {"-m", "--message"}, description = "The message", defaultValue = "Easily bootstrap Java projects with Microj CLI")
    private String message;

    @Parameters(description = "Arbitrary command line parameters")
    private List<String> parameters;

    @Override
    public void run() {
        LOGGER.info("{} - {}", message, parameters);
    }

}
