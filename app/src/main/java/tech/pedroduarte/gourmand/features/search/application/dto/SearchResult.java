package tech.pedroduarte.gourmand.features.search.application.dto;

import lombok.Builder;
import lombok.Value;
import tech.pedroduarte.gourmand.features.search.domain.Restaurant;

@Value
@Builder
public class SearchResult {

    String name;
    int rating;
    double distance;
    double price;
    String cuisine;

    public static SearchResult fromDomain(Restaurant restaurant) {
        return SearchResult.builder()
                .name(restaurant.getName())
                .rating(restaurant.getRating().getValue())
                .distance(restaurant.getDistance().getMiles())
                .price(restaurant.getPrice().getAmount())
                .cuisine(restaurant.getCuisine().getName())
                .build();
    }

}