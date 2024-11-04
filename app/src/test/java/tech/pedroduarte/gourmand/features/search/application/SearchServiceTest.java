package tech.pedroduarte.gourmand.features.search.application;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tech.pedroduarte.gourmand.features.search.application.dto.SearchResult;
import tech.pedroduarte.gourmand.features.search.domain.Cuisine;
import tech.pedroduarte.gourmand.features.search.domain.Restaurant;
import tech.pedroduarte.gourmand.features.search.domain.SearchCriteria;
import tech.pedroduarte.gourmand.features.search.domain.service.RestaurantSearchDomainService;
import tech.pedroduarte.gourmand.features.search.infrastructure.persistence.CsvRestaurantRepository;
import tech.pedroduarte.gourmand.shared.domain.Distance;
import tech.pedroduarte.gourmand.shared.domain.Price;
import tech.pedroduarte.gourmand.shared.domain.Rating;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SearchServiceTest {

    @Mock
    private CsvRestaurantRepository repository;

    private SearchService searchService;



    @BeforeEach
    void setUp() {
        RestaurantSearchDomainService domainService = new RestaurantSearchDomainService();
        searchService = new SearchService(repository, domainService);
    }

    @Test
    void shouldSortByDistanceFirst() {
        // Given
        final Restaurant CLOSE_EXPENSIVE_HIGH_RATED = new Restaurant(
                "Fancy Place", new Rating(5), new Distance(1.0), new Price(45.0), new Cuisine("Italian")
        );
        final Restaurant CLOSE_CHEAP_LOW_RATED = new Restaurant(
                "Budget Place", new Rating(2), new Distance(1.5), new Price(15.0), new Cuisine("Italian")
        );
        final Restaurant FAR_CHEAP_HIGH_RATED = new Restaurant(
                "Far Good Place", new Rating(5), new Distance(8.0), new Price(20.0), new Cuisine("Italian")
        );
        List<Restaurant> testData = List.of(
                FAR_CHEAP_HIGH_RATED,
                CLOSE_EXPENSIVE_HIGH_RATED,
                CLOSE_CHEAP_LOW_RATED
        );
        when(repository.findAll()).thenReturn(testData);

        // When
        List<SearchResult> results = searchService.search(
                SearchCriteria.builder().cuisine(new Cuisine("Italian")).build()
        );

        // Then
        assertThat(results).extracting("name")
                .containsExactly("Fancy Place", "Budget Place", "Far Good Place");
    }

    @Test
    void shouldPreferHigherRatingWhenDistanceEqual() {
        // Given
        Restaurant highRated = new Restaurant(
                "High Stars", new Rating(5), new Distance(2.0), new Price(30.0), new Cuisine("Italian")
        );
        Restaurant mediumRated = new Restaurant(
                "Medium Stars", new Rating(4), new Distance(2.0), new Price(30.0), new Cuisine("Italian")
        );
        Restaurant lowRated = new Restaurant(
                "Low Stars", new Rating(3), new Distance(2.0), new Price(30.0), new Cuisine("Italian")
        );
        when(repository.findAll()).thenReturn(List.of(mediumRated, lowRated, highRated));

        // When
        List<SearchResult> results = searchService.search(
                SearchCriteria.builder().cuisine(new Cuisine("Italian")).build()
        );

        // Then
        assertThat(results).extracting("name")
                .containsExactly("High Stars", "Medium Stars", "Low Stars");
    }

    @Test
    void shouldPreferLowerPriceWhenRatingAndDistanceEqual() {
        // Given
        Restaurant expensive = new Restaurant(
                "Expensive", new Rating(4), new Distance(2.0), new Price(45.0), new Cuisine("Italian")
        );
        Restaurant fair = new Restaurant(
                "Fair", new Rating(4), new Distance(2.0), new Price(30.0), new Cuisine("Italian")
        );
        Restaurant cheap = new Restaurant(
                "Cheap", new Rating(4), new Distance(2.0), new Price(15.0), new Cuisine("Italian")
        );
        when(repository.findAll()).thenReturn(List.of(fair, expensive, cheap));

        // When
        List<SearchResult> results = searchService.search(
                SearchCriteria.builder().build()
        );

        // Then
        assertThat(results).extracting("name")
                .containsExactly("Cheap", "Fair", "Expensive");
    }

    @Test
    void shouldReturnMaxFiveResults() {
        // Given
        List<Restaurant> manyRestaurants = List.of(
                new Restaurant("R1", new Rating(4), new Distance(1.0), new Price(20.0), new Cuisine("Italian")),
                new Restaurant("R2", new Rating(4), new Distance(2.0), new Price(20.0), new Cuisine("Italian")),
                new Restaurant("R3", new Rating(4), new Distance(3.0), new Price(20.0), new Cuisine("Italian")),
                new Restaurant("R4", new Rating(4), new Distance(4.0), new Price(20.0), new Cuisine("Italian")),
                new Restaurant("R5", new Rating(4), new Distance(5.0), new Price(20.0), new Cuisine("Italian")),
                new Restaurant("R6", new Rating(4), new Distance(6.0), new Price(20.0), new Cuisine("Italian"))
        );
        when(repository.findAll()).thenReturn(manyRestaurants);

        // When
        List<SearchResult> results = searchService.search(SearchCriteria.builder().build());

        // Then
        assertThat(results).hasSize(5)
                .extracting("name")
                .containsExactly("R1", "R2", "R3", "R4", "R5");
    }

    @Test
    void shouldMatchPartialRestaurantName() {
        // Given
        List<Restaurant> restaurants = List.of(
                new Restaurant("Pizza Place", new Rating(4), new Distance(1.0), new Price(20.0), new Cuisine("Italian")),
                new Restaurant("Burger Joint", new Rating(4), new Distance(1.0), new Price(20.0), new Cuisine("American"))
        );
        when(repository.findAll()).thenReturn(restaurants);

        // When
        List<SearchResult> results = searchService.search(
                SearchCriteria.builder().name("zza").build()
        );

        // Then
        assertThat(results).extracting("name")
                .containsExactly("Pizza Place");
    }

    @Test
    void shouldApplyAllCriteriaTogether() {
        // Given
        List<Restaurant> restaurants = List.of(
                new Restaurant("Good Pizza", new Rating(4), new Distance(1.0), new Price(20.0), new Cuisine("Italian")),
                new Restaurant("Bad Pizza", new Rating(2), new Distance(1.0), new Price(20.0), new Cuisine("Italian")),
                new Restaurant("Expensive Pizza", new Rating(4), new Distance(1.0), new Price(45.0), new Cuisine("Italian")),
                new Restaurant("Far Pizza", new Rating(4), new Distance(8.0), new Price(20.0), new Cuisine("Italian")),
                new Restaurant("Good Burger", new Rating(4), new Distance(1.0), new Price(20.0), new Cuisine("American"))
        );
        when(repository.findAll()).thenReturn(restaurants);

        // When
        List<SearchResult> results = searchService.search(
                SearchCriteria.builder()
                        .name("Pizza")
                        .minRating(new Rating(4))
                        .maxDistance(new Distance(2.0))
                        .maxPrice(new Price(30.0))
                        .cuisine(new Cuisine("Italian"))
                        .build()
        );

        // Then
        assertThat(results).extracting("name")
                .containsExactly("Good Pizza");
    }
}
