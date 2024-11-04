package tech.pedroduarte.gourmand.shared.domain;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PriceTest {

    @Test
    void shouldCreateValidPrice() {
        Price price = new Price(25.0);
        assertThat(price.getAmount()).isEqualTo(25.0);
    }

    @ParameterizedTest
    @ValueSource(doubles = {-1.0, 0.0, 50.1, 100.0})
    void shouldRejectInvalidPrices(double invalidPrice) {
        assertThatThrownBy(() -> new Price(invalidPrice))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Price must be between");
    }

    @Test
    void shouldFormatPriceWithCurrency() {
        Price price = new Price(25.50);
        assertThat(price.toString()).isEqualTo("$25.50");
    }

    @Test
    void shouldComparePrices() {
        Price price1 = new Price(25.0);
        Price price2 = new Price(15.0);
        Price price3 = new Price(25.0);

        assertThat(price1.compareTo(price2)).isGreaterThan(0);
        assertThat(price2.compareTo(price1)).isLessThan(0);
        assertThat(price1.compareTo(price3)).isEqualTo(0);
    }

    @Test
    void shouldEqualPrices() {
        Price price1 = new Price(25.0);
        Price price2 = new Price(25.0);
        Price price3 = new Price(15.0);

        assertThat(price1).isEqualTo(price2);
        assertThat(price1).isNotEqualTo(price3);
    }
}
