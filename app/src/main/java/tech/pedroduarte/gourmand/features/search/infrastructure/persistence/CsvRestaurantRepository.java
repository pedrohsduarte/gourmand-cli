package tech.pedroduarte.gourmand.features.search.infrastructure.persistence;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.pedroduarte.gourmand.common.exception.DataLoadException;
import tech.pedroduarte.gourmand.common.utils.CsvReader;
import tech.pedroduarte.gourmand.features.search.domain.Cuisine;
import tech.pedroduarte.gourmand.features.search.domain.Restaurant;
import tech.pedroduarte.gourmand.features.search.domain.persistence.RestaurantRepository;
import tech.pedroduarte.gourmand.shared.domain.Distance;
import tech.pedroduarte.gourmand.shared.domain.Price;
import tech.pedroduarte.gourmand.shared.domain.Rating;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

public class CsvRestaurantRepository implements RestaurantRepository {

    private static final Logger logger = LoggerFactory.getLogger(CsvRestaurantRepository.class);

    private final CsvReader csvReader;
    private final CsvDataSource dataSource;
    private List<Restaurant> restaurants;
    private Map<Long, Cuisine> cuisinesMap;

    public CsvRestaurantRepository(CsvDataSource dataSource) {
        this.csvReader = new CsvReader();
        this.dataSource = dataSource;
        loadData();
    }

    @Override
    public List<Restaurant> findAll() {
        return restaurants;
    }

    private void loadData() {
        try {
            this.cuisinesMap = loadCuisines();
            this.restaurants = loadRestaurants();
        } catch (Exception e) {
            throw new DataLoadException("Failed to load data files", e);
        }
    }

    @Override
    public List<Cuisine> findAllCuisines() {
        return List.copyOf(cuisinesMap.values());
    }

    private Map<Long, Cuisine> loadCuisines() throws IOException {
        logger.debug("Loading cuisines from data source");

        try (InputStream is = dataSource.getCuisinesStream()) {
            List<Map.Entry<Long, Cuisine>> entries = csvReader.readCsv(
                    is,
                    true, // skip header
                    columns -> {
                        Long cuisineId = Long.parseLong(columns[0].trim());
                        return Map.entry(cuisineId, new Cuisine(columns[1].trim()));
                    }
            );

            Map<Long, Cuisine> cuisinesMap = entries.stream()
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

            logger.info("Loaded {} cuisines", cuisinesMap.size());
            return Collections.unmodifiableMap(cuisinesMap);
        }
    }

    private List<Restaurant> loadRestaurants() throws IOException {
        logger.debug("Loading restaurants from data source");

        try (InputStream is = dataSource.getRestaurantsStream()) {
            List<Restaurant> loadedRestaurants = csvReader.readCsv(
                    is,
                    true, // skip header
                    this::mapToRestaurant
            );

            logger.info("Loaded {} restaurants", loadedRestaurants.size());
            return Collections.unmodifiableList(loadedRestaurants);
        }
    }

    private Restaurant mapToRestaurant(String[] columns) {
        try {
            String name = columns[0].trim();
            Rating rating = new Rating(Integer.parseInt(columns[1]));
            Distance distance = new Distance(Double.parseDouble(columns[2]));
            Price price = new Price(Double.parseDouble(columns[3]));
            Long cuisineId = Long.parseLong(columns[4].trim());

            return new Restaurant(
                    name,
                    rating,
                    distance,
                    price,
                    cuisinesMap.get(cuisineId)
            );
        } catch (IllegalArgumentException e) {
            throw new DataLoadException(
                    String.format("Invalid restaurant data: %s", String.join(", ", columns)),
                    e
            );
        }
    }

}