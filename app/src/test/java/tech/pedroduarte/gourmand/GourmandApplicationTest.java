package tech.pedroduarte.gourmand;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import picocli.CommandLine;
import tech.pedroduarte.gourmand.features.search.cli.SearchCommand;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class GourmandApplicationTest {

    private StringWriter outWriter;
    private StringWriter errWriter;
    private CommandLine cmd;

    @BeforeEach
    void setUp() {
        outWriter = new StringWriter();
        errWriter = new StringWriter();

        GourmandApplication app = new GourmandApplication();
        SearchCommand searchCommand = new SearchCommand();

        cmd = new CommandLine(app)
                .addSubcommand(searchCommand)
                .setOut(new PrintWriter(outWriter, true))
                .setErr(new PrintWriter(errWriter, true))
                .setExecutionStrategy(new CommandLine.RunLast())
                .setParameterExceptionHandler(new GourmandApplication.ParameterExceptionHandler())
                .setExecutionExceptionHandler(new GourmandApplication.ExecutionExceptionHandler());
    }

    @Nested
    class BasicFunctionality {

        @Test
        void shouldShowHelpMessage() {
            // When
            cmd.execute("--help");

            // Then
            assertThat(outWriter.toString())
                    .contains("Usage:")
                    .contains("Commands:")
                    .contains("search");
        }

        @Test
        void shouldShowVersion() {
            // When
            cmd.execute("--version");

            // Then
            assertThat(outWriter.toString())
                    .contains("1.0");
        }
    }

    @Nested
    class SubcommandHandling {

        @Test
        void shouldExecuteSearchCommand() {
            // When
            int exitCode = cmd.execute("search", "--help");

            // Then
            assertThat(exitCode).isZero();
            assertThat(outWriter.toString())
                    .contains("search")
                    .contains("NAME")
                    .contains("RATING")
                    .contains("DISTANCE");
        }
    }

    @Nested
    class ErrorHandling {
        @Test
        void shouldHandleUnknownCommand() {
            // When
            int exitCode = cmd.execute("unknown");

            // Then
            assertThat(exitCode).isNotZero();
            assertThat(errWriter.toString())
                    .contains("Unmatched argument at index 0: 'unknown'");
        }

        @Test
        void shouldHandleInvalidOption() {
            // When
            int exitCode = cmd.execute("--invalid-option");

            // Then
            assertThat(exitCode).isNotZero();
            assertThat(errWriter.toString())
                    .contains("Unknown option");
        }

        @Test
        void shouldHandleParameterValidationError() {
            // When
            int exitCode = cmd.execute("search", "--rating", "invalid");

            // Then
            assertThat(exitCode).isNotZero();
            assertThat(errWriter.toString())
                    .contains("Error:");
        }
    }

    @Nested
    class ExceptionHandling {
        @Test
        void shouldHandleValidationException() {
            // Given
            CommandLine cmdWithException = new CommandLine(new TestCommand())
                    .setErr(new PrintWriter(errWriter, true))
                    .setExecutionExceptionHandler(new GourmandApplication.ExecutionExceptionHandler());

            // When
            int exitCode = cmdWithException.execute();

            // Then
            assertThat(exitCode).isNotZero();
            assertThat(errWriter.toString())
                    .contains("Error: Validation failed");
        }

        @Test
        void shouldHandleUnexpectedException() {
            // Given
            CommandLine cmdWithException = new CommandLine(new UnexpectedErrorCommand())
                    .setErr(new PrintWriter(errWriter, true))
                    .setExecutionExceptionHandler(new GourmandApplication.ExecutionExceptionHandler());

            // When
            int exitCode = cmdWithException.execute();

            // Then
            assertThat(exitCode).isNotZero();
            assertThat(errWriter.toString())
                    .contains("Unexpected error:");
        }

        @CommandLine.Command(name = "test")
        static class TestCommand implements Runnable {
            @Override
            public void run() {
                throw new IllegalArgumentException("Validation failed");
            }
        }

        @CommandLine.Command(name = "error")
        static class UnexpectedErrorCommand implements Runnable {
            @Override
            public void run() {
                throw new RuntimeException("Unexpected error occurred");
            }
        }
    }

    @Nested
    class IntegrationTest {
        @TempDir
        Path tempDir;

        @Test
        void shouldExecuteFullSearchFlow() throws Exception {
            // Given
            createTestDataFiles(tempDir);

            // When
            int exitCode = cmd.execute("search",
                    "--data-dir", tempDir.toString(),
                    "--name", "Test Restaurant",
                    "--rating", "4");

            // Then
            assertThat(exitCode).isZero();
            assertThat(outWriter.toString())
                    .contains("NAME")
                    .contains("RATING")
                    .contains("DISTANCE");
        }

        private void createTestDataFiles(Path directory) throws Exception {
            Files.writeString(directory.resolve("cuisines.csv"),
                    "id,name\n1,Italian\n2,American");

            Files.writeString(directory.resolve("restaurants.csv"),
                    "name,customer_rating,distance,price,cuisine_id\n" +
                            "Test Restaurant,4,1.0,20.0,1");
        }
    }

    @Nested
    class EnvironmentTest {
        @Test
        void shouldRespectVerbosityFlag() {
            // When
            System.setProperty("gourmand.verbose", "true");
            int exitCode = cmd.execute("search", "--help");
            System.clearProperty("gourmand.verbose");

            // Then
            assertThat(exitCode).isZero();
            // Add assertions for verbose output if implemented
        }

    }
}
