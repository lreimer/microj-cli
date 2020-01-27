package dev.ops.tools.microj;

import java.io.File;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

/**
 * Main application for the Microj CLI.
 */
@Command(version = "Microj CLI 1.0", mixinStandardHelpOptions = true)
class MicrojCli implements Runnable {

    private static final Logger LOGGER = LoggerFactory.getLogger(MicrojCli.class);

    @Option(names = {"-f", "--file"}, paramLabel = "JSON_CONFIG", description = "the configuration file", required = false)
    private File configFile;

    public static void main(String[] args) {
        CommandLine.run(new MicrojCli(), args);
    }

    @Override
    public void run() {
        LOGGER.info("Running Microj CLI ...");
    }
}
