package tech.pedroduarte.gourmand.features.search.domain;

import tech.pedroduarte.gourmand.shared.domain.Distance;
import tech.pedroduarte.gourmand.shared.domain.Price;
import tech.pedroduarte.gourmand.shared.domain.Rating;

public record SearchCriteria(
        String name,
        Rating minRating,
        Distance maxDistance,
        Price maxPrice,
        Cuisine cuisine
) {

    public SearchCriteria {
        if (name != null) {
            name = name.trim();
            if (name.isEmpty()) {
                name = null;
            }
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String name;
        private Rating minRating;
        private Distance maxDistance;
        private Price maxPrice;
        private Cuisine cuisine;

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder minRating(Rating rating) {
            this.minRating = rating;
            return this;
        }

        public Builder maxDistance(Distance distance) {
            this.maxDistance = distance;
            return this;
        }

        public Builder maxPrice(Price price) {
            this.maxPrice = price;
            return this;
        }

        public Builder cuisine(Cuisine cuisine) {
            this.cuisine = cuisine;
            return this;
        }

        public SearchCriteria build() {
            return new SearchCriteria(name, minRating, maxDistance, maxPrice, cuisine);
        }
    }

    public String formattedCriteria() {
        StringBuilder formatted = new StringBuilder();
        if (name != null) {
            formatted.append("- Name: ").append(name).append("\n");
        }
        if (minRating != null) {
            formatted.append("- Minimum rating: ").append(minRating).append("\n");
        }
        if (maxDistance != null) {
            formatted.append("- Maximum distance: ").append(maxDistance).append("\n");
        }
        if (maxPrice != null) {
            formatted.append("- Maximum price: ").append(maxPrice).append("\n");
        }
        if (cuisine != null) {
            formatted.append("- Cuisine: ").append(cuisine).append("\n");
        }
        if(formatted.length() == 0) {
            formatted.append("No criteria specified\n");
        }
        return formatted.toString();
    }
}
