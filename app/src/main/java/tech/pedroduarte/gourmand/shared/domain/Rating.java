package tech.pedroduarte.gourmand.shared.domain;

import java.util.Objects;

public class Rating implements Comparable<Rating> {

    private final int value;
    private static final int MIN_RATING = 1;
    private static final int MAX_RATING = 5;

    public Rating(int value) {
        validateRating(value);
        this.value = value;
    }

    private void validateRating(int value) {
        if (value < MIN_RATING || value > MAX_RATING) {
            throw new IllegalArgumentException(
                    String.format("Rating must be between %d and %d stars", MIN_RATING, MAX_RATING)
            );
        }
    }

    public int getValue() { return value; }

    @Override
    public int compareTo(Rating other) {
        return Integer.compare(this.value, other.value);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Rating)) return false;
        Rating rating = (Rating) o;
        return value == rating.value;
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }
}
