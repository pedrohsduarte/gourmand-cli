package tech.pedroduarte.gourmand;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;
import tech.pedroduarte.gourmand.common.config.LoggingConfig;
import tech.pedroduarte.gourmand.features.search.cli.SearchCommand;

import java.io.PrintWriter;

@CommandLine.Command(
        name = "gourmand",
        version = "1.0",
        description = "Find the perfect restaurant for your next meal",
        mixinStandardHelpOptions = true
)
public class GourmandApplication implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(GourmandApplication.class);

    public static void main(String[] args) {
        LoggingConfig.configureLogging(args);
        GourmandApplication app = new GourmandApplication();
        SearchCommand searchCommand = new SearchCommand();

        CommandLine cmd = new CommandLine(app);
        printBanner(cmd);
        int exitCode = cmd
                .addSubcommand(searchCommand)
                .setExecutionStrategy(new CommandLine.RunLast()) // Run only the last specified command
                .setParameterExceptionHandler(new ParameterExceptionHandler())
                .setExecutionExceptionHandler(new ExecutionExceptionHandler())
                .execute(args);

        System.exit(exitCode);
    }

    @Override
    public void run() {
        // This method is called when no subcommand is specified
        CommandLine.usage(SearchCommand.class, System.out);

    }

    static class ParameterExceptionHandler implements CommandLine.IParameterExceptionHandler {

        @Override
        public int handleParseException(CommandLine.ParameterException ex, String[] args) {
            CommandLine cmd = ex.getCommandLine();

            logger.error("Failed to parse command", ex);

            PrintWriter err = cmd.getErr();
            err.printf("Error: %s%n", ex.getMessage());
            err.flush();

            if (!CommandLine.UnmatchedArgumentException.printSuggestions(ex, System.err)) {
                ex.getCommandLine().usage(System.err);
            }

            return cmd.getExitCodeExceptionMapper() != null
                    ? cmd.getExitCodeExceptionMapper().getExitCode(ex)
                    : cmd.getCommandSpec().exitCodeOnInvalidInput();
        }
    }

    static class ExecutionExceptionHandler implements CommandLine.IExecutionExceptionHandler {

        @Override
        public int handleExecutionException(Exception e, CommandLine cmd,
                                            CommandLine.ParseResult parseResult) {

            logger.error("Command execution failed", e);
            PrintWriter err = cmd.getErr();

            if (e instanceof IllegalArgumentException ||
                    e instanceof IllegalStateException) {
                // Known validation errors - show just the message
                err.printf("Error: %s%n", e.getMessage());
                err.flush();
                logger.error("Validation error", e);
            } else {
                // Unexpected errors - show more details in development
                err.printf("Unexpected error: %s%n", e.getMessage());
                err.flush();
                logger.error("Unexpected error", e);
            }

            return cmd.getCommandSpec().exitCodeOnExecutionException();
        }
    }

    private static void printBanner(CommandLine cmd) {
        cmd.getOut().println("""
                ##########################################
                #                                        #
                #             GOURMAND CLI               #
                #                                        #
                #   Find your next favorite restaurant   #
                #                                        #
                ##########################################
                """);
    }
}
