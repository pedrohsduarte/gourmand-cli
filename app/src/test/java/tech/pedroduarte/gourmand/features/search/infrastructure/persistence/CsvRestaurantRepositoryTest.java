package tech.pedroduarte.gourmand.features.search.infrastructure.persistence;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import tech.pedroduarte.gourmand.common.exception.DataLoadException;
import tech.pedroduarte.gourmand.features.search.domain.Cuisine;
import tech.pedroduarte.gourmand.features.search.domain.Restaurant;
import tech.pedroduarte.gourmand.shared.domain.Distance;
import tech.pedroduarte.gourmand.shared.domain.Price;
import tech.pedroduarte.gourmand.shared.domain.Rating;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class CsvRestaurantRepositoryTest {

    @Mock
    private CsvDataSource dataSource;

    private CsvRestaurantRepository repository;

    private static final String VALID_CUISINES_CSV = """
            id,name
            1,Italian
            2,American
            3,Chinese""";

    private static final String VALID_RESTAURANTS_CSV = """
            name,customer_rating,distance,price,cuisine_id
            Pizza Place,4,1.0,20.0,1
            Burger Joint,3,2.0,15.0,2
            Chinese Garden,5,3.0,30.0,3""";

    @BeforeEach
    void setUp() throws IOException {
        // Default valid data setup
        when(dataSource.getCuisinesStream())
                .thenReturn(new ByteArrayInputStream(VALID_CUISINES_CSV.getBytes(StandardCharsets.UTF_8)));
        when(dataSource.getRestaurantsStream())
                .thenReturn(new ByteArrayInputStream(VALID_RESTAURANTS_CSV.getBytes(StandardCharsets.UTF_8)));
    }

    @Nested
    class BasicFunctionality {
        @Test
        void shouldLoadValidData() throws IOException {
            // When
            repository = new CsvRestaurantRepository(dataSource);

            // Then
            List<Restaurant> restaurants = repository.findAll();
            assertThat(restaurants)
                    .hasSize(3)
                    .extracting(Restaurant::getName)
                    .containsExactly("Pizza Place", "Burger Joint", "Chinese Garden");

            // Verify cuisines are correctly mapped
            assertThat(restaurants)
                    .extracting(r -> r.getCuisine().getName())
                    .containsExactly("Italian", "American", "Chinese");
        }

        @Test
        void shouldProvideAvailableCuisines() throws IOException {
            // When
            repository = new CsvRestaurantRepository(dataSource);
            List<Cuisine> cuisines = repository.findAllCuisines();

            // Then
            assertThat(cuisines)
                    .hasSize(3)
                    .extracting(Cuisine::getName)
                    .containsExactlyInAnyOrder("Italian", "American", "Chinese");
        }
    }

    @Nested
    class EdgeCases {
        @Test
        void shouldHandleInvalidCuisineId() throws IOException {
            // Given
            String invalidRestaurants = """
                    name,customer_rating,distance,price,cuisine_id
                    Bad Restaurant,4,1.0,20.0,999""";

            when(dataSource.getRestaurantsStream())
                    .thenReturn(new ByteArrayInputStream(invalidRestaurants.getBytes()));

            // When/Then
            assertThatThrownBy(() -> new CsvRestaurantRepository(dataSource))
                    .isInstanceOf(DataLoadException.class)
                    .cause().hasMessageContaining("Invalid restaurant data");
        }

        @Test
        void shouldHandleInvalidRating() throws IOException {
            // Given
            String invalidRestaurants = """
                    name,customer_rating,distance,price,cuisine_id
                    Bad Restaurant,6,1.0,20.0,1""";

            when(dataSource.getRestaurantsStream())
                    .thenReturn(new ByteArrayInputStream(invalidRestaurants.getBytes()));

            // When/Then
            assertThatThrownBy(() -> new CsvRestaurantRepository(dataSource))
                    .isInstanceOf(DataLoadException.class)
                    .cause().hasMessageContaining("Invalid restaurant data");
        }

        @ParameterizedTest
        @ValueSource(strings = {
                "name,customer_rating,distance,price,cuisine_id\nTest Restaurant,4,11.0,20.0,1",  // Distance > 10
                "name,customer_rating,distance,price,cuisine_id\nTest Restaurant,4,1.0,51.0,1",   // Price > 50
                "name,customer_rating,distance,price,cuisine_id\nTest Restaurant,4,-1.0,20.0,1",  // Negative distance
                "name,customer_rating,distance,price,cuisine_id\nTest Restaurant,4,1.0,-20.0,1"   // Negative price
        })
        void shouldValidateNumericRanges(String csvContent) throws IOException {
            // Given
            when(dataSource.getRestaurantsStream())
                    .thenReturn(new ByteArrayInputStream(csvContent.getBytes()));

            // When/Then
            assertThatThrownBy(() -> new CsvRestaurantRepository(dataSource))
                    .cause().isInstanceOf(DataLoadException.class)
                    .cause().isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    class ErrorHandling {
        @Test
        void shouldHandleIOExceptionInCuisines() throws IOException {
            // Given
            when(dataSource.getCuisinesStream()).thenThrow(new RuntimeException("Simulated error"));

            // When/Then
            assertThatThrownBy(() -> new CsvRestaurantRepository(dataSource))
                    .isInstanceOf(DataLoadException.class)
                    .hasMessageContaining("Failed to load data files");
        }

        @Test
        void shouldHandleIOExceptionInRestaurants() throws IOException {
            // Given
            when(dataSource.getRestaurantsStream()).thenThrow(new RuntimeException("Simulated error"));

            // When/Then
            assertThatThrownBy(() -> new CsvRestaurantRepository(dataSource))
                    .isInstanceOf(DataLoadException.class)
                    .hasMessageContaining("Failed to load data files")
                    .cause()
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Simulated error");
        }

        @Test
        void shouldHandleMalformedCuisineData() throws IOException {
            // Given
            String invalidCuisines = "id,name\nNot a number,Italian";

            when(dataSource.getCuisinesStream())
                    .thenReturn(new ByteArrayInputStream(invalidCuisines.getBytes()));

            // When/Then
            assertThatThrownBy(() -> new CsvRestaurantRepository(dataSource))
                    .isInstanceOf(DataLoadException.class)
                    .cause()
                    .isInstanceOf(NumberFormatException.class);
        }

        @Test
        void shouldHandleIncompleteRestaurantData() throws IOException {
            // Given
            String incompleteData = """
                    name,customer_rating,distance,price,cuisine_id
                    Incomplete Restaurant,4,1.0""";  // Missing fields

            when(dataSource.getRestaurantsStream())
                    .thenReturn(new ByteArrayInputStream(incompleteData.getBytes()));

            // When/Then
            assertThatThrownBy(() -> new CsvRestaurantRepository(dataSource))
                    .isInstanceOf(DataLoadException.class)
                    .cause()
                    .isInstanceOf(ArrayIndexOutOfBoundsException.class);
        }
    }

    @Nested
    class DataIntegrity {
        @Test
        void shouldReturnUnmodifiableCollections() throws IOException {
            // Given
            repository = new CsvRestaurantRepository(dataSource);

            // When/Then
            assertThatThrownBy(() -> repository.findAll().add(
                    new Restaurant("Test", new Rating(4), new Distance(1.0),
                            new Price(20.0), new Cuisine("Italian"))
            )).isInstanceOf(UnsupportedOperationException.class);

            assertThatThrownBy(() -> repository.findAllCuisines().add(
                    new Cuisine("NewCuisine")
            )).isInstanceOf(UnsupportedOperationException.class);
        }

        @Test
        void shouldMaintainCuisineReferences() throws IOException {
            // Given
            repository = new CsvRestaurantRepository(dataSource);

            // When
            List<Restaurant> restaurants = repository.findAll();
            List<Cuisine> cuisines = repository.findAllCuisines();

            // Then
            Restaurant italianRestaurant = restaurants.stream()
                    .filter(r -> r.getCuisine().getName().equals("Italian"))
                    .findFirst()
                    .orElseThrow();

            Cuisine italianCuisine = cuisines.stream()
                    .filter(c -> c.getName().equals("Italian"))
                    .findFirst()
                    .orElseThrow();

            // Verify that the cuisine reference is the same object
            assertThat(italianRestaurant.getCuisine()).isSameAs(italianCuisine);
        }
    }
}