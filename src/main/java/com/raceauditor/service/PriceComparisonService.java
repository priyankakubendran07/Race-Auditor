package com.raceauditor.service;

import com.raceauditor.dao.PriceListingDAO;
import com.raceauditor.dao.ProductDAO;
import com.raceauditor.model.PriceListing;
import com.raceauditor.model.Product;
import com.raceauditor.util.MarketplaceLinkBuilder;
import java.util.*;

/**
 * Price comparison business logic.
 * MODE 1 (real API) -> implement a PriceProvider and plug it in here.
 * MODE 2 (current)  -> MySQL demo/sample data, always labelled as demo.
 */
public class PriceComparisonService {
    private final ProductDAO products = new ProductDAO();
    private final PriceListingDAO listings = new PriceListingDAO();

    public static class ComparedProduct {
        public final Product product;
        public final List<PriceListing> listings;
        public final PriceListing best;

        ComparedProduct(Product p, List<PriceListing> listings) {
            this.product = p;
            this.listings = listings;
            this.best = listings.stream()
                .filter(l -> !"Out of Stock".equalsIgnoreCase(l.getAvailability()))
                .min(Comparator.comparingDouble(PriceListing::getPrice))
                .orElse(listings.stream().min(Comparator.comparingDouble(PriceListing::getPrice)).orElse(null));
        }
    }

    public List<ComparedProduct> compare(String query) throws Exception {
        if (query == null || query.trim().length() < 2)
            throw new IllegalArgumentException("Enter at least 2 characters to search (e.g. sunscreen, headphones, laptop).");
        // 1) Built-in demo catalog first: guarantees the three required demo
        // products work even when MySQL is down or unseeded.
        ComparedProduct demo = findDemoForQuery(query);
        if (demo != null) {
            return List.of(demo);
        }
        // 2) MySQL path (existing behaviour preserved).
        List<ComparedProduct> out = new ArrayList<>();
        try {
            List<Product> found = products.search(query.trim());
            for (Product p : found) {
                List<PriceListing> rows = listings.listingsFor(p.getId());
                if (!rows.isEmpty()) out.add(new ComparedProduct(p, rows));
            }
        } catch (Exception dbEx) {
            // DB down/empty and no demo match -> no results; UI shows the
            // "No matching demo product found." card instead of crashing.
            return List.of();
        }
        // Exact name/keyword hits first so the top card matches the user's query.
        String q = query.trim().toLowerCase();
        out.sort((a, b) -> Boolean.compare(!isExactMatch(b.product, q), !isExactMatch(a.product, q)));
        return out;
    }

    // ------------------------------------------------------------------
    // Built-in demo catalog (in-memory, always available, no DB needed).
    // Three demo products with distinct prices/stock per marketplace.
    // ------------------------------------------------------------------

    /** Returns the demo ComparedProduct for the query, or null when unsupported. Case-insensitive. */
    public static ComparedProduct findDemoForQuery(String query) {
        if (query == null) return null;
        String q = query.trim().toLowerCase(Locale.ROOT).replaceAll("\\s+", " ");
        if (q.length() < 2) return null;
        // Required aliases (explicit first so each phrase maps to exactly one product):
        // "headphones" / "wireless headphones" -> Headphones
        // "mouse" / "wireless mouse" -> Mouse
        // "park avenue voyage" / "voyage" / "voyage body spray" -> Body Spray
        if (q.equals("headphones") || q.equals("wireless headphones")
                || q.contains("headphone") || q.contains("head phone") || q.contains("headset")
                || q.contains("earphone")) {
            return demoHeadphones();
        }
        if (q.equals("mouse") || q.equals("wireless mouse") || q.contains("mouse")) {
            return demoMouse();
        }
        if (q.equals("park avenue voyage") || q.equals("voyage") || q.equals("voyage body spray")
                || q.contains("park avenue") || q.contains("voyage") || q.contains("body spray")
                || q.contains("body-spray")) {
            return demoBodySpray();
        }
        return null;
    }

    private static PriceListing demoListing(int id, int productId, String market,
                                            double price, String availability, String url) {
        PriceListing l = new PriceListing();
        l.setId(id);
        l.setProductId(productId);
        l.setMarketplace(market);
        l.setPrice(price);
        l.setAvailability(availability);
        l.setProductUrl(url);
        l.setDemo(true);
        return l;
    }

    private static Product demoProduct(int id, String name, String brand, String keywords) {
        Product p = new Product();
        p.setId(id);
        p.setName(name);
        p.setBrand(brand);
        p.setKeywords(keywords);
        return p;
    }

    /** Wireless Bluetooth Headphones: clear in-stock cheapest -> Best Overall justified. */
    public static ComparedProduct demoHeadphones() {
        Product p = demoProduct(101, "Wireless Bluetooth Headphones", "SonicWave",
            "headphones headset earphone bluetooth wireless music wireless headphones");
        String q = p.getName();
        List<PriceListing> rows = List.of(
            demoListing(1001, 101, "Amazon", 2999.00, "In Stock",
                MarketplaceLinkBuilder.amazonSearch(q)),
            demoListing(1002, 101, "Flipkart", 2799.00, "In Stock",
                MarketplaceLinkBuilder.flipkartSearch(q)),
            demoListing(1003, 101, "Meesho", 2499.00, "Only few left in stock",
                MarketplaceLinkBuilder.meeshoSearch(q)));
        return new ComparedProduct(p, new ArrayList<>(rows));
    }

    /**
     * Park Avenue Voyage Body Spray: cheapest listing is Out of Stock, so the
     * cheapest available option is NOT the overall lowest -> Best Overall
     * is NOT justified for this product (demonstrates the conditional rule).
     */
    public static ComparedProduct demoBodySpray() {
        Product p = demoProduct(102, "Park Avenue Voyage Body Spray", "Park Avenue",
            "park avenue voyage body spray perfume deodorant fragrance voyage body spray");
        String q = p.getName();
        List<PriceListing> rows = List.of(
            demoListing(1011, 102, "Amazon", 350.00, "In Stock",
                MarketplaceLinkBuilder.amazonSearch(q)),
            demoListing(1012, 102, "Flipkart", 315.00, "Out of Stock",
                MarketplaceLinkBuilder.flipkartSearch(q)),
            demoListing(1013, 102, "Meesho", 340.00, "In Stock",
                MarketplaceLinkBuilder.meeshoSearch(q)));
        return new ComparedProduct(p, new ArrayList<>(rows));
    }

    /** Wireless Mouse: clear in-stock cheapest -> Best Overall justified. */
    public static ComparedProduct demoMouse() {
        Product p = demoProduct(103, "Wireless Mouse", "LogiTech",
            "mouse wireless optical usb computer accessory wireless mouse");
        String q = p.getName();
        List<PriceListing> rows = List.of(
            demoListing(1021, 103, "Amazon", 899.00, "In Stock",
                MarketplaceLinkBuilder.amazonSearch(q)),
            demoListing(1022, 103, "Flipkart", 749.00, "Only 2 left in stock",
                MarketplaceLinkBuilder.flipkartSearch(q)),
            demoListing(1023, 103, "Meesho", 649.00, "In Stock",
                MarketplaceLinkBuilder.meeshoSearch(q)));
        return new ComparedProduct(p, new ArrayList<>(rows));
    }

    /** True when the product genuinely matches the query; otherwise the UI labels it "Related Match". */
    public static boolean isExactMatch(Product p, String queryLower) {
        if (p == null || queryLower == null) return false;
        String q = queryLower.toLowerCase().trim();
        if (q.isEmpty()) return false;
        String name = p.getName() == null ? "" : p.getName().toLowerCase();
        String kw = p.getKeywords() == null ? "" : p.getKeywords().toLowerCase();
        // Demo alias matching: short queries map to the canonical demo products.
        if (findDemoForQuery(q) != null) {
            ComparedProduct demo = findDemoForQuery(q);
            if (demo != null && demo.product.getName().equalsIgnoreCase(p.getName())) return true;
        }
        return name.contains(q) || kw.contains(q);
    }

    /** Best / highest / difference, always computed from the displayed listings. */
    public static class PriceSummary {
        public final PriceListing best;
        public final PriceListing highest;
        public final double difference;

        public PriceSummary(PriceListing best, PriceListing highest, double difference) {
            this.best = best;
            this.highest = highest;
            this.difference = difference;
        }
    }

    public static PriceSummary summarize(ComparedProduct cp) {
        PriceListing highest = cp.listings.stream()
            .max(Comparator.comparingDouble(PriceListing::getPrice)).orElse(null);
        PriceListing lowest = cp.listings.stream()
            .min(Comparator.comparingDouble(PriceListing::getPrice)).orElse(null);
        // Price Difference = highest minus lowest (true range across shops).
        double diff = (highest != null && lowest != null) ? highest.getPrice() - lowest.getPrice() : 0;
        return new PriceSummary(cp.best, highest, diff);
    }

    /**
     * Best Overall is shown ONLY when the demo data justifies it:
     * the cheapest available (best) listing must also be the overall
     * lowest-priced listing, must be purchasable (not Out of Stock),
     * and there must be a real price spread across shops.
     */
    public static boolean isBestOverallJustified(ComparedProduct cp) {
        if (cp == null || cp.best == null || cp.listings == null || cp.listings.size() < 2) return false;
        if ("Out of Stock".equalsIgnoreCase(cp.best.getAvailability())) return false;
        PriceListing lowest = cp.listings.stream()
            .min(Comparator.comparingDouble(PriceListing::getPrice)).orElse(null);
        PriceListing highest = cp.listings.stream()
            .max(Comparator.comparingDouble(PriceListing::getPrice)).orElse(null);
        if (lowest == null || highest == null) return false;
        boolean cheapestIsBest = Double.compare(lowest.getPrice(), cp.best.getPrice()) == 0
            && lowest.getMarketplace().equalsIgnoreCase(cp.best.getMarketplace());
        if (!cheapestIsBest) return false;
        return (highest.getPrice() - lowest.getPrice()) > 0.001;
    }
}
