package tech.pedroduarte.gourmand.features.search.application;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.pedroduarte.gourmand.features.search.application.dto.SearchResult;
import tech.pedroduarte.gourmand.features.search.domain.Restaurant;
import tech.pedroduarte.gourmand.features.search.domain.SearchCriteria;
import tech.pedroduarte.gourmand.features.search.domain.persistence.RestaurantRepository;
import tech.pedroduarte.gourmand.features.search.domain.service.RestaurantSearchDomainService;

import java.util.List;
import java.util.stream.Collectors;

public class SearchService {

    private static final Logger logger = LoggerFactory.getLogger(SearchService.class);
    private static final int MAX_RESULTS = 5;

    private final RestaurantRepository restaurantRepository;
    private final RestaurantSearchDomainService domainService;

    public SearchService(RestaurantRepository restaurantRepository,
                         RestaurantSearchDomainService domainService) {
        this.restaurantRepository = restaurantRepository;
        this.domainService = domainService;
    }

    public List<SearchResult> search(SearchCriteria criteria) {
        logger.info("Executing search with criteria: {}", criteria);

        List<Restaurant> allRestaurants = restaurantRepository.findAll();

        List<Restaurant> matches = domainService.findMatches(
                allRestaurants,
                criteria.name(),
                criteria.minRating(),
                criteria.maxDistance(),
                criteria.maxPrice(),
                criteria.cuisine()
        );

        List<Restaurant> sortedMatches = domainService.sortByRelevance(matches);

        return sortedMatches.stream()
                .map(SearchResult::fromDomain)
                .limit(MAX_RESULTS)
                .collect(Collectors.toList());
    }

}