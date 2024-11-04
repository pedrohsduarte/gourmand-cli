package tech.pedroduarte.gourmand.commons.utils;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import tech.pedroduarte.gourmand.common.exception.DataLoadException;
import tech.pedroduarte.gourmand.common.utils.CsvReader;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CsvReaderTest {

    private CsvReader csvReader;

    @BeforeEach
    void setUp() {
        csvReader = new CsvReader();
    }

    @Nested
    class BasicFunctionality {
        @Test
        void shouldReadSimpleCsvData() {
            // Given
            String csv = """
                name,age,city
                John,30,New York
                Jane,25,London""";

            // When
            List<TestPerson> result = csvReader.readCsv(
                    toInputStream(csv),
                    true,
                    columns -> new TestPerson(columns[0], Integer.parseInt(columns[1]), columns[2])
            );

            // Then
            assertThat(result)
                    .hasSize(2)
                    .extracting(TestPerson::name)
                    .containsExactly("John", "Jane");
        }

        @Test
        void shouldReadWithoutSkippingHeader() {
            // Given
            String csv = """
                John,30,New York
                Jane,25,London""";

            // When
            List<TestPerson> result = csvReader.readCsv(
                    toInputStream(csv),
                    false,
                    columns -> new TestPerson(columns[0], Integer.parseInt(columns[1]), columns[2])
            );

            // Then
            assertThat(result).hasSize(2);
        }
    }

    @Nested
    class ErrorHandling {
        @Test
        void shouldHandleEmptyCsv() {
            // Given
            String csv = "";

            // When/Then
            assertThatThrownBy(() ->
                    csvReader.readCsv(toInputStream(csv), true, columns -> columns)
            )
                    .isInstanceOf(DataLoadException.class)
                    .hasMessageContaining("CSV data is empty");
        }

        @Test
        void shouldHandleNullInputStream() {
            // When/Then
            assertThatThrownBy(() ->
                    csvReader.readCsv(null, true, columns -> columns)
            )
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("InputStream cannot be null");
        }

        @Test
        void shouldHandleIOException() {
            // Given
            InputStream failingStream = new InputStream() {
                @Override
                public int read() throws IOException {
                    throw new IOException("Simulated failure");
                }

                @Override
                public void close() throws IOException {
                    // Ensure clean close
                }
            };

            // When/Then
            assertThatThrownBy(() ->
                    csvReader.readCsv(failingStream, true, columns -> columns)
            )
                    .isInstanceOf(DataLoadException.class)
                    .hasMessageContaining("Failed to read CSV data")
                    .hasCauseInstanceOf(IOException.class)
                    .hasRootCauseMessage("Simulated failure");
        }

        // Alternative approach using a failing BufferedReader
        @Test
        void shouldHandleStreamReadException() {
            // Given
            String csvContent = "header\ndata";
            InputStream inputStream = new ByteArrayInputStream(csvContent.getBytes()) {
                @Override
                public void close() throws IOException {
                    throw new IOException("Simulated close failure");
                }
            };

            // When/Then
            assertThatThrownBy(() ->
                    csvReader.readCsv(inputStream, true, columns -> columns)
            )
                    .isInstanceOf(DataLoadException.class)
                    .hasMessageContaining("Failed to read CSV data");
        }
    }

    @Nested
    class BlankLineHandling {
        @Test
        void shouldSkipBlankLines() {
            // Given
            String csv = """
                name,age
                John,30

                Jane,25
                """;

            // When
            List<String[]> result = csvReader.readCsv(
                    toInputStream(csv),
                    true,
                    columns -> columns
            );

            // Then
            assertThat(result).hasSize(2);
        }

        @Test
        void shouldHandleWhitespaceOnlyLines() {
            // Given
            String csv = """
                name,age
                John,30
                   
                Jane,25""";

            // When
            List<String[]> result = csvReader.readCsv(
                    toInputStream(csv),
                    true,
                    columns -> columns
            );

            // Then
            assertThat(result).hasSize(2);
        }
    }

    @Nested
    class ValueCleaning {
        @Test
        void shouldTrimWhitespace() {
            // Given
            String csv = "name , age \n John , 30 ";

            // When
            List<String[]> result = csvReader.readCsv(
                    toInputStream(csv),
                    true,
                    columns -> columns
            );

            // Then
            assertThat(result.get(0)[0]).isEqualTo("John");
            assertThat(result.get(0)[1]).isEqualTo("30");
        }

        @Test
        void shouldHandleEmptyFields() {
            // Given
            String csv = "name,age,city\nJohn,,New York";

            // When
            List<String[]> result = csvReader.readCsv(
                    toInputStream(csv),
                    true,
                    columns -> columns
            );

            // Then
            assertThat(result.get(0)[1]).isEmpty();
        }
    }

    @Nested
    class MappingFunctionality {
        @Test
        void shouldHandleMappingExceptions() {
            // Given
            String csv = "name,age\nJohn,notANumber";

            // When/Then
            assertThatThrownBy(() ->
                    csvReader.readCsv(
                            toInputStream(csv),
                            true,
                            columns -> new TestPerson(columns[0], Integer.parseInt(columns[1]), "")
                    )
            )
                    .isInstanceOf(NumberFormatException.class);
        }

        @Test
        void shouldMapToCustomObject() {
            // Given
            String csv = "name,age,city\nJohn,30,New York";

            // When
            List<TestPerson> result = csvReader.readCsv(
                    toInputStream(csv),
                    true,
                    CsvReaderTest::mapToPerson
            );

            // Then
            assertThat(result)
                    .hasSize(1)
                    .first()
                    .satisfies(person -> {
                        assertThat(person.name()).isEqualTo("John");
                        assertThat(person.age()).isEqualTo(30);
                        assertThat(person.city()).isEqualTo("New York");
                    });
        }
    }

    // Helper methods and classes
    private InputStream toInputStream(String content) {
        return new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
    }

    static TestPerson mapToPerson(String[] columns) {
        return new TestPerson(
                columns[0],
                Integer.parseInt(columns[1]),
                columns[2]
        );
    }

    private record TestPerson(String name, int age, String city) {}
}
