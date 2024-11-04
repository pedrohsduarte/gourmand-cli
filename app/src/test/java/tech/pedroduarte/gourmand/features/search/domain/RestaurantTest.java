package tech.pedroduarte.gourmand.features.search.domain;

import org.junit.jupiter.api.Test;
import tech.pedroduarte.gourmand.shared.domain.Distance;
import tech.pedroduarte.gourmand.shared.domain.Price;
import tech.pedroduarte.gourmand.shared.domain.Rating;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RestaurantTest {

    private final Rating rating = new Rating(4);
    private final Distance distance = new Distance(2.0);
    private final Price price = new Price(25.0);
    private final Cuisine cuisine = new Cuisine("Italian");

    @Test
    void shouldCreateValidRestaurant() {
        Restaurant restaurant = new Restaurant(
                "Test Restaurant",
                rating,
                distance,
                price,
                cuisine
        );

        assertThat(restaurant.getName()).isEqualTo("Test Restaurant");
        assertThat(restaurant.getRating()).isEqualTo(rating);
        assertThat(restaurant.getDistance()).isEqualTo(distance);
        assertThat(restaurant.getPrice()).isEqualTo(price);
        assertThat(restaurant.getCuisine()).isEqualTo(cuisine);
    }

    @Test
    void shouldRejectNullName() {
        assertThatThrownBy(() ->
                new Restaurant(null, rating, distance, price, cuisine)
        ).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("name cannot be empty");
    }

    @Test
    void shouldRejectEmptyName() {
        assertThatThrownBy(() ->
                new Restaurant("", rating, distance, price, cuisine)
        ).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("name cannot be empty");
    }

    @Test
    void shouldRejectNullValues() {
        assertThatThrownBy(() ->
                new Restaurant("Test", null, distance, price, cuisine)
        ).isInstanceOf(IllegalArgumentException.class);

        assertThatThrownBy(() ->
                new Restaurant("Test", rating, null, price, cuisine)
        ).isInstanceOf(IllegalArgumentException.class);

        assertThatThrownBy(() ->
                new Restaurant("Test", rating, distance, null, cuisine)
        ).isInstanceOf(IllegalArgumentException.class);

        assertThatThrownBy(() ->
                new Restaurant("Test", rating, distance, price, null)
        ).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldEqualRestaurants() {
        Restaurant restaurant1 = new Restaurant("Test", rating, distance, price, cuisine);
        Restaurant restaurant2 = new Restaurant("Test", rating, distance, price, cuisine);
        Restaurant restaurant3 = new Restaurant("Test2", rating, distance, price, cuisine);

        assertThat(restaurant1).isEqualTo(restaurant2);
        assertThat(restaurant1).isNotEqualTo(restaurant3);
    }

    @Test
    void shouldNotEqualNull() {
        Restaurant restaurant = new Restaurant("Test", rating, distance, price, cuisine);
        assertThat(restaurant).isNotEqualTo(null);
    }

    @Test
    void shouldReturnSameHashCode() {
        Restaurant restaurant1 = new Restaurant("Test", rating, distance, price, cuisine);
        Restaurant restaurant2 = new Restaurant("Test", rating, distance, price, cuisine);
        assertThat(restaurant1.hashCode()).isEqualTo(restaurant2.hashCode());
    }

    @Test
    void shouldReturnStringRepresentation() {
        Restaurant restaurant = new Restaurant("Test", rating, distance, price, cuisine);
        assertThat(restaurant.toString()).isEqualTo("Restaurant{name='Test', rating=4, distance=2.0 mi, price=$25.00, cuisine=Italian}");
    }

}
