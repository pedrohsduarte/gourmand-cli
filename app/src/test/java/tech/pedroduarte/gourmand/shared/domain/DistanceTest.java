package tech.pedroduarte.gourmand.shared.domain;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DistanceTest {

    @Test
    void shouldCreateValidDistance() {
        Distance distance = new Distance(5.5);
        assertThat(distance.getMiles()).isEqualTo(5.5);
    }

    @ParameterizedTest
    @ValueSource(doubles = {-1.0, 0.0, 10.1, 15.0})
    void shouldRejectInvalidDistances(double invalidDistance) {
        assertThatThrownBy(() -> new Distance(invalidDistance))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Distance must be between");
    }

    @Test
    void shouldFormatDistanceCorrectly() {
        Distance distance = new Distance(5.5);
        assertThat(distance.toString()).isEqualTo("5.5 mi");
    }

    @Test
    void shouldCompareDistances() {
        Distance distance1 = new Distance(5.5);
        Distance distance2 = new Distance(3.0);
        Distance distance3 = new Distance(5.5);

        assertThat(distance1.compareTo(distance2)).isGreaterThan(0);
        assertThat(distance2.compareTo(distance1)).isLessThan(0);
        assertThat(distance1.compareTo(distance3)).isEqualTo(0);
    }

    @Test
    void shouldEqualDistances() {
        Distance distance1 = new Distance(5.5);
        Distance distance2 = new Distance(5.5);
        Distance distance3 = new Distance(3.0);

        assertThat(distance1).isEqualTo(distance2);
        assertThat(distance1).isNotEqualTo(distance3);
    }
}