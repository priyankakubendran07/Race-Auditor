package com.raceauditor.service;

import com.raceauditor.model.PriceListing;
import com.raceauditor.model.Product;
import java.util.List;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class PriceComparisonServiceTest {

    private PriceListing listing(int id, String market, double price, String avail) {
        PriceListing l = new PriceListing();
        l.setId(id);
        l.setProductId(1);
        l.setMarketplace(market);
        l.setPrice(price);
        l.setAvailability(avail);
        l.setProductUrl("https://example.com/" + market);
        l.setDemo(true);
        return l;
    }

    @Test
    void lowestPriceWinsBestBadge() {
        Product p = new Product();
        p.setId(1);
        p.setName("Test Sunscreen");
        p.setBrand("TestBrand");
        PriceComparisonService.ComparedProduct cp = new PriceComparisonService.ComparedProduct(p, List.of(
            listing(1, "Amazon", 499.0, "In Stock"),
            listing(2, "Flipkart", 549.0, "In Stock"),
            listing(3, "Meesho", 599.0, "In Stock")));
        assertNotNull(cp.best);
        assertEquals(499.0, cp.best.getPrice());
        assertEquals("Amazon", cp.best.getMarketplace());
    }

    @Test
    void outOfStockCheapestIsSkipped() {
        Product p = new Product();
        p.setId(1);
        p.setName("Test Headphones");
        p.setBrand("TestBrand");
        PriceComparisonService.ComparedProduct cp = new PriceComparisonService.ComparedProduct(p, List.of(
            listing(1, "Amazon", 399.0, "Out of Stock"),
            listing(2, "Flipkart", 549.0, "In Stock")));
        assertNotNull(cp.best);
        assertEquals("Flipkart", cp.best.getMarketplace());
    }

    @Test
    void shortQueryRejected() {
        PriceComparisonService svc = new PriceComparisonService();
        assertThrows(IllegalArgumentException.class, () -> svc.compare("x"));
    }

    @Test
    void summaryReportsBestHighestAndDifference() {
        Product p = new Product();
        p.setId(1);
        p.setName("Test Shoes");
        p.setBrand("TestBrand");
        PriceComparisonService.ComparedProduct cp = new PriceComparisonService.ComparedProduct(p, List.of(
            listing(1, "Amazon", 2499.0, "In Stock"),
            listing(2, "Flipkart", 2299.0, "In Stock"),
            listing(3, "Meesho", 2199.0, "In Stock")));
        PriceComparisonService.PriceSummary sum = PriceComparisonService.summarize(cp);
        assertEquals("Meesho", sum.best.getMarketplace());
        assertEquals("Amazon", sum.highest.getMarketplace());
        assertEquals(300.0, sum.difference, 0.001);
    }

    @Test
    void exactMatchDetection() {
        Product p = new Product();
        p.setName("Sunscreen SPF 50 PA+++ 50ml");
        p.setKeywords("sunscreen sunblock spf lotion skincare");
        assertTrue(PriceComparisonService.isExactMatch(p, "sunscreen"));
        assertFalse(PriceComparisonService.isExactMatch(p, "headphones"));
    }
}
