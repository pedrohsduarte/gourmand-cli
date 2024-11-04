package tech.pedroduarte.gourmand.shared.domain;

import java.util.Objects;

public class Distance implements Comparable<Distance> {

    private final double miles;
    private static final double MIN_DISTANCE = 1.0;
    private static final double MAX_DISTANCE = 10.0;

    public Distance(double miles) {
        validateDistance(miles);
        this.miles = miles;
    }

    private void validateDistance(double miles) {
        if (miles < MIN_DISTANCE || miles > MAX_DISTANCE) {
            throw new IllegalArgumentException(
                    String.format("Distance must be between %.1f and %.1f miles",
                            MIN_DISTANCE, MAX_DISTANCE)
            );
        }
    }

    public double getMiles() { return miles; }

    @Override
    public int compareTo(Distance other) {
        return Double.compare(this.miles, other.miles);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Distance)) return false;
        Distance distance = (Distance) o;
        return Double.compare(distance.miles, miles) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(miles);
    }

    @Override
    public String toString() {
        return String.format("%.1f mi", miles);
    }
}
