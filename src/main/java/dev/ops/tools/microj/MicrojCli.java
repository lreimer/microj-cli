package dev.ops.tools.microj;

import dev.ops.tools.microj.cmd.BuildJobCommand;
import dev.ops.tools.microj.cmd.DeploymentCommand;
import dev.ops.tools.microj.cmd.InfoCommand;
import dev.ops.tools.microj.cmd.ServiceCommand;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Option;
import picocli.jansi.graalvm.AnsiConsole;

import static picocli.CommandLine.ParameterException;
import static picocli.CommandLine.Spec;

/**
 * Main application for the Microj CLI.
 */
@Command(name = "microj",
        subcommands = {ServiceCommand.class, BuildJobCommand.class, DeploymentCommand.class, InfoCommand.class},
        version = "Microj CLI 1.0",
        mixinStandardHelpOptions = true,
        synopsisSubcommandLabel = "command",
        commandListHeading = "%nThese are common Microj CLI commands used in various situations:%n")
public class MicrojCli implements Runnable {

    @Spec
    private CommandSpec spec;

    @Option(names = {"-u", "--username"}, description = "The user name", arity = "0..1")
    private String userName;

    @Option(names = {"-p", "--password"}, description = "The password", arity = "0..1")
    private char[] password;

    public static void main(String[] args) {
        AnsiConsole.systemInstall();
        System.exit(new CommandLine(new MicrojCli()).execute(args));
    }

    @Override
    public void run() {
        throw new ParameterException(spec.commandLine(), "Missing required command");
    }

    public String getUserName() {
        return userName;
    }

    public char[] getPassword() {
        return password;
    }
}
