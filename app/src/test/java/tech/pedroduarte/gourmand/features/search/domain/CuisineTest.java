package tech.pedroduarte.gourmand.features.search.domain;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CuisineTest {

    @Test
    void shouldCreateValidCuisine() {
        Cuisine cuisine = new Cuisine("Italian");
        assertThat(cuisine.getName()).isEqualTo("Italian");
    }

    @Test
    void shouldRejectNullName() {
        assertThatThrownBy(() -> new Cuisine(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("cannot be empty");
    }

    @ParameterizedTest
    @ValueSource(strings = {"", " ", "   "})
    void shouldRejectEmptyOrBlankNames(String invalidName) {
        assertThatThrownBy(() -> new Cuisine(invalidName))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("cannot be empty");
    }

    @Test
    void shouldHandleCaseInsensitiveComparison() {
        Cuisine italian1 = new Cuisine("Italian");
        Cuisine italian2 = new Cuisine("italian");
        Cuisine italian3 = new Cuisine("ITALIAN");

        assertThat(italian1)
                .isEqualTo(italian2)
                .isEqualTo(italian3);
    }

    @Test
    void shouldConsiderDifferentNames() {
        Cuisine italian = new Cuisine("Italian");
        Cuisine japanese = new Cuisine("Japanese");

        assertThat(italian)
                .isNotEqualTo(japanese);
    }

    @Test
    void shouldValidateNameFormat() {
        assertThatThrownBy(() -> new Cuisine("Italian123"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("must contain only letters");

        assertThatThrownBy(() -> new Cuisine("Italian!"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("must contain only letters");
    }

    @Test
    void shouldCompareCuisines() {
        Cuisine italian = new Cuisine("Italian");
        Cuisine italian2 = new Cuisine("Italian");
        Cuisine japanese = new Cuisine("Japanese");

        assertThat(italian).isEqualTo(italian2);
        assertThat(italian).isNotEqualTo(japanese);
        assertThat(italian.hashCode()).isEqualTo(italian2.hashCode());
    }
}
