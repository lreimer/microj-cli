package dev.ops.tools.microj;

import dev.ops.tools.microj.cmd.BuildJobCommand;
import dev.ops.tools.microj.cmd.DeploymentCommand;
import dev.ops.tools.microj.cmd.HelloCommand;
import dev.ops.tools.microj.cmd.ServiceCommand;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Model.CommandSpec;
import picocli.jansi.graalvm.AnsiConsole;

import static picocli.CommandLine.ParameterException;
import static picocli.CommandLine.Spec;

/**
 * Main application for the Microj CLI.
 */
@Command(name = "microj",
        subcommands = {ServiceCommand.class, BuildJobCommand.class, DeploymentCommand.class, HelloCommand.class},
        version = "Microj CLI 1.0",
        mixinStandardHelpOptions = true,
        synopsisSubcommandLabel = "command",
        commandListHeading = "%nThese are common Microj CLI commands used in various situations:%n")
class MicrojCli implements Runnable {

    @Spec
    private CommandSpec spec;

    public static void main(String[] args) {
        AnsiConsole.systemInstall();
        System.exit(new CommandLine(new MicrojCli()).execute(args));
    }

    @Override
    public void run() {
        throw new ParameterException(spec.commandLine(), "Missing required command");
    }
}
