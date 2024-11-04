package tech.pedroduarte.gourmand.shared.domain;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RatingTest {

    @Test
    void shouldCreateValidRating() {
        Rating rating = new Rating(4);
        assertThat(rating.getValue()).isEqualTo(4);
    }

    @ParameterizedTest
    @ValueSource(ints = {0, -1, 6, 10})
    void shouldRejectInvalidRatings(int invalidRating) {
        assertThatThrownBy(() -> new Rating(invalidRating))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Rating must be between");
    }

    @Test
    void shouldCompareRatings() {
        Rating rating1 = new Rating(4);
        Rating rating2 = new Rating(3);
        Rating rating3 = new Rating(4);

        assertThat(rating1.compareTo(rating2)).isGreaterThan(0);
        assertThat(rating2.compareTo(rating1)).isLessThan(0);
        assertThat(rating1.compareTo(rating3)).isEqualTo(0);
    }

    @Test
    void shouldFormatRatingWithStar() {
        Rating rating = new Rating(4);
        assertThat(rating.toString()).isEqualTo("4");
    }

    @Test
    void shouldEqualRatings() {
        Rating rating1 = new Rating(4);
        Rating rating2 = new Rating(4);
        Rating rating3 = new Rating(3);

        assertThat(rating1).isEqualTo(rating2);
        assertThat(rating1).isNotEqualTo(rating3);
    }

}
