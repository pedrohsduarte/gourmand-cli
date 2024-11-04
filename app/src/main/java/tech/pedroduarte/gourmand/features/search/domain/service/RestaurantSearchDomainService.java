package tech.pedroduarte.gourmand.features.search.domain.service;

import tech.pedroduarte.gourmand.features.search.domain.Cuisine;
import tech.pedroduarte.gourmand.features.search.domain.Restaurant;
import tech.pedroduarte.gourmand.shared.domain.Distance;
import tech.pedroduarte.gourmand.shared.domain.Price;
import tech.pedroduarte.gourmand.shared.domain.Rating;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class RestaurantSearchDomainService {

    public List<Restaurant> findMatches(
            List<Restaurant> restaurants,
            String name,
            Rating minRating,
            Distance maxDistance,
            Price maxPrice,
            Cuisine cuisine) {

        return restaurants.stream()
                .filter(restaurant -> matchesName(restaurant, name))
                .filter(restaurant -> matchesRating(restaurant, minRating))
                .filter(restaurant -> matchesDistance(restaurant, maxDistance))
                .filter(restaurant -> matchesPrice(restaurant, maxPrice))
                .filter(restaurant -> matchesCuisine(restaurant, cuisine))
                .collect(Collectors.toList());
    }

    public List<Restaurant> sortByRelevance(List<Restaurant> matches) {
        return matches.stream()
                .sorted(
                        Comparator.comparing(Restaurant::getDistance)
                                .thenComparing(Restaurant::getRating, Comparator.reverseOrder())
                                .thenComparing(Restaurant::getPrice)
                )
                .collect(Collectors.toList());
    }

    private boolean matchesName(Restaurant restaurant, String searchName) {
        if (searchName == null || searchName.isEmpty()) {
            return true;
        }
        return restaurant.getName()
                .toLowerCase()
                .contains(searchName.toLowerCase());
    }

    private boolean matchesRating(Restaurant restaurant, Rating minRating) {
        if (minRating == null) {
            return true;
        }
        return restaurant.getRating().compareTo(minRating) >= 0;
    }

    private boolean matchesDistance(Restaurant restaurant, Distance maxDistance) {
        if (maxDistance == null) {
            return true;
        }
        return restaurant.getDistance().compareTo(maxDistance) <= 0;
    }

    private boolean matchesPrice(Restaurant restaurant, Price maxPrice) {
        if (maxPrice == null) {
            return true;
        }
        return restaurant.getPrice().compareTo(maxPrice) <= 0;
    }

    private boolean matchesCuisine(Restaurant restaurant, Cuisine searchCuisine) {
        if (searchCuisine == null) {
            return true;
        }
        return restaurant.getCuisine()
                .getName()
                .toLowerCase()
                .contains(searchCuisine.getName().toLowerCase());
    }
}