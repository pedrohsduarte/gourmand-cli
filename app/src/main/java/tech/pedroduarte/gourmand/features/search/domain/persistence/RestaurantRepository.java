package tech.pedroduarte.gourmand.features.search.domain.persistence;

import tech.pedroduarte.gourmand.features.search.domain.Cuisine;
import tech.pedroduarte.gourmand.features.search.domain.Restaurant;

import java.util.List;

public interface RestaurantRepository {

    List<Restaurant> findAll();

    List<Cuisine> findAllCuisines();

}
