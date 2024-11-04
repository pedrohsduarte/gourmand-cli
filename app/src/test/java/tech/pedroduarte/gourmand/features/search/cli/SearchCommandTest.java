package tech.pedroduarte.gourmand.features.search.cli;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import picocli.CommandLine;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import tech.pedroduarte.gourmand.features.search.application.dto.SearchResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SearchCommandTest {

    private StringWriter outputWriter;
    private StringWriter errorWriter;
    private CommandLine cmd;

    @BeforeEach
    void setUp() {
        outputWriter = new StringWriter();
        errorWriter = new StringWriter();

        SearchCommand searchCommand = new SearchCommand();
        cmd = new CommandLine(searchCommand)
                .setOut(new PrintWriter(outputWriter, true))  // Note the autoflush=true
                .setErr(new PrintWriter(errorWriter, true));
    }

    @Nested
    class TypeConverterTests {
        @Nested
        class RatingConverterTest {
            private final SearchCommand.RatingConverter converter = new SearchCommand.RatingConverter();

            @Test
            void shouldConvertValidRating() {
                assertThat(converter.convert("4").getValue()).isEqualTo(4);
            }

            @ParameterizedTest
            @ValueSource(strings = {"0", "6", "-1", "abc"})
            void shouldRejectInvalidRatings(String invalidRating) {
                assertThatThrownBy(() -> converter.convert(invalidRating))
                        .isInstanceOf(CommandLine.TypeConversionException.class);
            }
        }

        @Nested
        class DistanceConverterTest {
            private final SearchCommand.DistanceConverter converter = new SearchCommand.DistanceConverter();

            @Test
            void shouldConvertValidDistance() {
                assertThat(converter.convert("5.5").getMiles()).isEqualTo(5.5);
            }

            @ParameterizedTest
            @ValueSource(strings = {"0", "11", "-1", "abc"})
            void shouldRejectInvalidDistances(String invalidDistance) {
                assertThatThrownBy(() -> converter.convert(invalidDistance))
                        .isInstanceOf(CommandLine.TypeConversionException.class);
            }
        }

        @Nested
        class PriceConverterTest {
            private final SearchCommand.PriceConverter converter = new SearchCommand.PriceConverter();

            @Test
            void shouldConvertValidPrice() {
                assertThat(converter.convert("25.50").getAmount()).isEqualTo(25.50);
            }

            @ParameterizedTest
            @ValueSource(strings = {"9", "51", "-1", "abc"})
            void shouldRejectInvalidPrices(String invalidPrice) {
                assertThatThrownBy(() -> converter.convert(invalidPrice))
                        .isInstanceOf(CommandLine.TypeConversionException.class);
            }
        }

        @Nested
        class CuisineConverterTest {
            private final SearchCommand.CuisineTypeConverter converter = new SearchCommand.CuisineTypeConverter();

            @Test
            void shouldConvertValidCuisine() {
                assertThat(converter.convert("Italian").getName()).isEqualTo("Italian");
            }

            @Test
            void shouldRejectInvalidCuisine() {
                assertThatThrownBy(() -> converter.convert(""))
                        .isInstanceOf(CommandLine.TypeConversionException.class);
            }
        }
    }

    @Nested
    class CommandExecutionTests {
        @Test
        void shouldExecuteWithValidArguments() {
            // When
            int exitCode = cmd.execute(
                    "--name", "Pizza",
                    "--rating", "4",
                    "--distance", "2.5",
                    "--price", "25",
                    "--cuisine", "Italian"
            );

            // Then
            assertThat(exitCode).isZero();
        }

        @Test
        void shouldHandleNoArguments() {
            // When
            int exitCode = cmd.execute();

            // Then
            assertThat(exitCode).isZero();
        }
    }

    @Nested
    class DataDirectoryTests {
        @TempDir
        Path tempDir;

        @Test
        void shouldUseCustomDataDirectory() throws Exception {
            // Given
            createValidTestFiles(tempDir);

            // When
            int exitCode = cmd.execute("--data-dir", tempDir.toString());

            // Then
            assertThat(exitCode).isZero();
        }

        private void createValidTestFiles(Path directory) throws Exception {
            Files.writeString(directory.resolve("cuisines.csv"),
                    "id,name\n1,Italian\n2,American");
            Files.writeString(directory.resolve("restaurants.csv"),
                    "name,customer_rating,distance,price,cuisine_id\n" +
                            "Test Restaurant,4,1.0,20.0,1");
        }
    }

    @Nested
    class OutputFormattingTests {

        @BeforeEach
        void setUpTestData(@TempDir Path tempDir) throws IOException {
            // Create test data files
            Files.writeString(tempDir.resolve("cuisines.csv"), """
                id,name
                1,Italian
                2,Chinese
                3,Korean
                4,Russian
                5,Spanish
                """);

            Files.writeString(tempDir.resolve("restaurants.csv"), """
                name,customer_rating,distance,price,cuisine_id
                Test Italian,4,1.0,20.0,1
                Test Chinese,3,2.0,15.0,2
                """);

            // Execute command with test data directory
            cmd.execute("--data-dir", tempDir.toString());
        }

        @Test
        void shouldDisplayFormattedResults() {
            // When executing a search that will return results
            cmd.execute("--distance", "5");

            // Then capture and verify the output
            String output = outputWriter.toString();

            // Print the actual output for debugging
            System.out.println("Actual output:");
            System.out.println(output);

            assertThat(output)
                    .contains("NAME")
                    .contains("RATING")
                    .contains("DISTANCE")
                    .contains("PRICE")
                    .contains("CUISINE")
                    .contains("Test Italian")
                    .contains("Test Chinese");
        }

        @Test
        void shouldHandleEmptyResults() {
            // When
            cmd.execute("--name", "NonExistent");

            // Then
            assertThat(outputWriter.toString().trim())
                    .contains("No restaurants found");
        }

        @Test
        void shouldAlignColumnsCorrectly() {
            // When
            cmd.execute("--distance", "5");

            // Then
            String[] lines = outputWriter.toString().split("\n");

            // Find the header line
            String headerLine = Arrays.stream(lines)
                    .filter(line -> line.contains("NAME"))
                    .findFirst()
                    .orElseThrow();

            // Find first data line
            String dataLine = Arrays.stream(lines)
                    .filter(line -> line.contains("Test"))
                    .findFirst()
                    .orElseThrow();

            // Verify column alignment
            assertThat(headerLine.indexOf("NAME"))
                    .isEqualTo(dataLine.indexOf("Test"));
            assertThat(headerLine.indexOf("RATING"))
                    .isEqualTo(dataLine.indexOf("4"));
        }
    }

    @Nested
    class HelpAndVersionTests {
        @Test
        void shouldShowHelp() {
            // When
            int exitCode = cmd.execute("--help");

            // Then
            assertThat(exitCode).isZero();
            assertThat(outputWriter.toString())
                    .contains("Usage:")
                    .contains("-n, --name")
                    .contains("-r, --rating")
                    .contains("-d, --distance")
                    .contains("-p, --price")
                    .contains("-c, --cuisine");
        }

        @Test
        void shouldShowVersion() {
            // When
            int exitCode = cmd.execute("--version");

            // Then
            assertThat(exitCode).isZero();
            assertThat(outputWriter.toString()).contains("1.0");
        }
    }
}