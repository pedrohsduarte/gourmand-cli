package tech.pedroduarte.gourmand.features.search.domain;

import tech.pedroduarte.gourmand.shared.domain.Distance;
import tech.pedroduarte.gourmand.shared.domain.Price;
import tech.pedroduarte.gourmand.shared.domain.Rating;

import java.util.Objects;

public class Restaurant {
    private final String name;
    private final Rating rating;
    private final Distance distance;
    private final Price price;
    private final Cuisine cuisine;

    public Restaurant(String name, Rating rating, Distance distance, Price price, Cuisine cuisine) {
        this.name = name;
        this.rating = rating;
        this.distance = distance;
        this.price = price;
        this.cuisine = cuisine;
        validate();
    }

    private void validate() {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Restaurant name cannot be empty");
        }
        if (rating == null) {
            throw new IllegalArgumentException("Rating cannot be null");
        }
        if (distance == null) {
            throw new IllegalArgumentException("Distance cannot be null");
        }
        if (price == null) {
            throw new IllegalArgumentException("Price cannot be null");
        }
        if (cuisine == null) {
            throw new IllegalArgumentException("Cuisine cannot be null");
        }
    }

    // Getters
    public String getName() { return name; }
    public Rating getRating() { return rating; }
    public Distance getDistance() { return distance; }
    public Price getPrice() { return price; }
    public Cuisine getCuisine() { return cuisine; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Restaurant)) return false;
        Restaurant that = (Restaurant) o;
        return name.equals(that.name) &&
                rating.equals(that.rating) &&
                distance.equals(that.distance) &&
                price.equals(that.price) &&
                cuisine.equals(that.cuisine);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, rating, distance, price, cuisine);
    }

    @Override
    public String toString() {
        return String.format(
                "Restaurant{name='%s', rating=%s, distance=%s, price=%s, cuisine=%s}",
                name, rating, distance, price, cuisine
        );
    }
}