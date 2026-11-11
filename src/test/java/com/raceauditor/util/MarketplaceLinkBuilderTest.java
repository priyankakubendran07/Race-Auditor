package com.raceauditor.util;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class MarketplaceLinkBuilderTest {

    @Test
    void marketplaceSearchUrlsContainQuery() {
        assertTrue(MarketplaceLinkBuilder.amazonSearch("sunscreen").contains("sunscreen"));
        assertTrue(MarketplaceLinkBuilder.flipkartSearch("headphones").contains("headphones"));
        assertTrue(MarketplaceLinkBuilder.meeshoSearch("shoes").contains("shoes"));
    }

    @Test
    void marketplaceSearchUrlsPointAtMarketplaces() {
        assertTrue(MarketplaceLinkBuilder.amazonSearch("shoes").startsWith("https://www.amazon.in/"));
        assertTrue(MarketplaceLinkBuilder.flipkartSearch("shoes").startsWith("https://www.flipkart.com/"));
        assertTrue(MarketplaceLinkBuilder.meeshoSearch("shoes").startsWith("https://www.meesho.com/"));
    }
}
