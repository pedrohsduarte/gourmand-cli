package tech.pedroduarte.gourmand.features.search.infrastructure.persistence;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import tech.pedroduarte.gourmand.common.exception.DataLoadException;
import tech.pedroduarte.gourmand.features.search.domain.Cuisine;
import tech.pedroduarte.gourmand.features.search.domain.Restaurant;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CsvRestaurantRepositoryIntegrationTest {

    @TempDir
    Path tempDir;

    private CsvDataSource dataSource;
    private CsvRestaurantRepository repository;

    @Nested
    class ResourceBasedTests {

        @BeforeEach
        void setUp() {
            dataSource = CsvDataSource.fromResources();
            repository = new CsvRestaurantRepository(dataSource);
        }

        @Test
        void shouldLoadFromResources() {
            // When
            List<Restaurant> restaurants = repository.findAll();
            List<Cuisine> cuisines = repository.findAllCuisines();

            // Then
            assertThat(cuisines).isNotEmpty();
            assertThat(restaurants)
                    .isNotEmpty()
                    .allSatisfy(restaurant ->
                            assertThat(cuisines)
                                    .extracting(Cuisine::getName)
                                    .contains(restaurant.getCuisine().getName())
                    );
        }

        @Test
        void shouldMaintainReferentialIntegrity() {
            // When
            List<Restaurant> restaurants = repository.findAll();
            List<Cuisine> cuisines = repository.findAllCuisines();

            // Then
            restaurants.forEach(restaurant -> {
                Cuisine restaurantCuisine = restaurant.getCuisine();
                assertThat(cuisines)
                        .anySatisfy(cuisine ->
                                assertThat(cuisine).isSameAs(restaurantCuisine)
                        );
            });
        }
    }

    @Nested
    class FileSystemTests {

        @BeforeEach
        void setUp() throws IOException {
            createValidTestFiles();
            dataSource = CsvDataSource.fromDirectory(tempDir);
            repository = new CsvRestaurantRepository(dataSource);
        }

        @Test
        void shouldLoadFromFileSystem() {
            // When
            List<Restaurant> restaurants = repository.findAll();
            List<Cuisine> cuisines = repository.findAllCuisines();

            // Then
            assertThat(cuisines)
                    .hasSize(3)
                    .extracting(Cuisine::getName)
                    .containsExactlyInAnyOrder("Italian", "American", "Chinese");

            assertThat(restaurants)
                    .hasSize(2)
                    .extracting(Restaurant::getName)
                    .containsExactly("Pizza Place", "Burger Joint");
        }

        @Test
        void shouldHandleMissingDirectory() {
            // Given
            Path nonExistentDir = tempDir.resolve("non-existent");

            // When/Then
            assertThatThrownBy(() -> CsvDataSource.fromDirectory(nonExistentDir))
                    .isInstanceOf(DataLoadException.class)
                    .hasMessageContaining("not found");
        }

        @Test
        void shouldHandleMissingFiles() throws IOException {
            // Given
            Files.delete(tempDir.resolve("restaurants.csv"));

            // When/Then
            assertThatThrownBy(() -> CsvDataSource.fromDirectory(tempDir))
                    .isInstanceOf(DataLoadException.class)
                    .hasMessageContaining("not found");
        }

        private void createValidTestFiles() throws IOException {
            String cuisinesData = """
                id,name
                1,Italian
                2,American
                3,Chinese""";

            String restaurantsData = """
                name,customer_rating,distance,price,cuisine_id
                Pizza Place,4,1.0,20.0,1
                Burger Joint,3,2.0,15.0,2""";

            Files.writeString(tempDir.resolve("cuisines.csv"), cuisinesData);
            Files.writeString(tempDir.resolve("restaurants.csv"), restaurantsData);
        }
    }

}