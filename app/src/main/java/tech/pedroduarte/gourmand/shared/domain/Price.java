package tech.pedroduarte.gourmand.shared.domain;

import java.util.Objects;

public class Price implements Comparable<Price> {

    private final double amount;
    private static final double MIN_PRICE = 10.0;
    private static final double MAX_PRICE = 50.0;

    public Price(double amount) {
        validatePrice(amount);
        this.amount = amount;
    }

    private void validatePrice(double amount) {
        if (amount < MIN_PRICE || amount > MAX_PRICE) {
            throw new IllegalArgumentException(
                    String.format("Price must be between $%.2f and $%.2f",
                            MIN_PRICE, MAX_PRICE)
            );
        }
    }

    public double getAmount() { return amount; }

    @Override
    public int compareTo(Price other) {
        return Double.compare(this.amount, other.amount);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Price)) return false;
        Price price = (Price) o;
        return Double.compare(price.amount, amount) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(amount);
    }

    @Override
    public String toString() {
        return String.format("$%.2f", amount);
    }
}
