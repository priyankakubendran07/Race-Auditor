package com.raceauditor.ui.comparison;

import com.raceauditor.model.PriceListing;
import com.raceauditor.model.User;
import com.raceauditor.service.PriceComparisonService;
import com.raceauditor.service.PriceComparisonService.ComparedProduct;
import com.raceauditor.service.PriceComparisonService.PriceSummary;
import com.raceauditor.util.CurrencyUtil;
import com.raceauditor.util.Dialogs;
import com.raceauditor.util.MarketplaceLinkBuilder;
import com.raceauditor.util.UiTheme;
import java.awt.*;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

/** Premium dark price comparison: same query across Amazon, Flipkart and Meesho. */
public class PriceComparisonPanel extends JPanel {
    private static final String[] MARKETS = {"Amazon", "Flipkart", "Meesho"};

    private final PriceComparisonService service = new PriceComparisonService();
    private final JTextField search = UiTheme.field(24);
    private final JPanel results = new JPanel();
    // Last search state (for automated verification that the UI updates).
    private ComparedProduct lastResult;
    private String lastMessage = "";

    public ComparedProduct getLastResult() { return lastResult; }
    public String getLastMessage() { return lastMessage; }
    public String getSearchText() { return search.getText(); }
    public void setSearchText(String text) { search.setText(text); }
    public void triggerSearch() { runSearch(); }

    public PriceComparisonPanel(User user) {
        setBackground(UiTheme.BG);
        setLayout(new BorderLayout(0, 12));
        setBorder(new EmptyBorder(4, 4, 4, 4));

        JLabel h = new JLabel("PRICE COMPARISON");
        h.setFont(UiTheme.TITLE);
        h.setForeground(Color.WHITE);
        JLabel s = new JLabel("Search for a product and compare prices across marketplaces.");
        s.setFont(UiTheme.SUBTITLE);
        s.setForeground(UiTheme.MUTED);

        JPanel bar = UiTheme.card();
        bar.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 6));
        search.setPreferredSize(new Dimension(320, 36));
        JButton go = UiTheme.primaryButton("COMPARE PRICES");
        go.addActionListener(e -> runSearch());
        search.addActionListener(e -> runSearch());
        search.setToolTipText("Try: headphones, wireless mouse, park avenue voyage");
        bar.add(search);
        bar.add(go);

        JPanel top = new JPanel(new BorderLayout(0, 6));
        top.setBackground(UiTheme.BG);
        JPanel titles = new JPanel();
        titles.setBackground(UiTheme.BG);
        titles.setLayout(new BoxLayout(titles, BoxLayout.Y_AXIS));
        titles.add(h);
        titles.add(s);
        top.add(titles, BorderLayout.NORTH);
        top.add(bar, BorderLayout.SOUTH);
        add(top, BorderLayout.NORTH);

        results.setLayout(new BoxLayout(results, BoxLayout.Y_AXIS));
        results.setBackground(UiTheme.BG);
        JScrollPane sp = new JScrollPane(results);
        sp.setBackground(UiTheme.BG);
        sp.getViewport().setBackground(UiTheme.BG);
        sp.setBorder(BorderFactory.createEmptyBorder());
        add(sp, BorderLayout.CENTER);
        UiTheme.fixHierarchy(this);
    }

    private void runSearch() {
        String q = search.getText().trim();
        results.removeAll();
        lastResult = null;
        lastMessage = "";
        try {
            List<ComparedProduct> out = service.compare(q);
            if (out.isEmpty()) {
                lastMessage = "No matching demo product found.";
                results.add(noDataCard(q));
            } else {
                ComparedProduct top = out.get(0);
                lastResult = top;
                lastMessage = top.product.getName();
                boolean exact = PriceComparisonService.isExactMatch(top.product, q);
                results.add(summaryBanner(q, top, exact));
                results.add(Box.createVerticalStrut(12));
                results.add(marketRow(q, top));
                if (out.size() > 1) {
                    results.add(Box.createVerticalStrut(12));
                    results.add(relatedCard(out.subList(1, Math.min(out.size(), 4))));
                }
            }
        } catch (IllegalArgumentException ex) {
            lastMessage = ex.getMessage();
            Dialogs.error(this, ex.getMessage());
            return;
        } catch (Exception ex) {
            lastMessage = "No matching demo product found.";
            Dialogs.error(this, "Search failed: " + ex.getMessage());
            results.add(noDataCard(q));
        }
        results.revalidate();
        results.repaint();
        UiTheme.fixHierarchy(this);
    }

    /** Banner with best / highest / difference, computed from the listings. */
    private JPanel summaryBanner(String query, ComparedProduct cp, boolean exact) {
        JPanel card = UiTheme.card();
        card.setLayout(new BorderLayout(0, 6));
        JPanel line = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        line.setBackground(UiTheme.CARD);
        JLabel icon = new JLabel(productEmoji(cp.product));
        icon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 30));
        icon.setForeground(Color.WHITE);
        icon.setName("productIconLabel");
        line.add(icon);
        JLabel tag = new JLabel(exact ? "  EXACT MATCH  " : "  RELATED MATCH  ");
        tag.setOpaque(true);
        tag.setBackground(exact ? new Color(0x134E4A) : new Color(0x3B2F0B));
        tag.setForeground(exact ? UiTheme.GREEN : UiTheme.GOLD);
        tag.setFont(UiTheme.LABEL);
        JLabel pq = new JLabel("“" + query + "”  →  " + displayName(cp.product));
        pq.setFont(UiTheme.HEADING);
        pq.setForeground(Color.WHITE);
        pq.setName("productNameLabel");
        line.add(tag);
        line.add(pq);
        card.add(line, BorderLayout.NORTH);

        PriceSummary sum = PriceComparisonService.summarize(cp);
        JPanel stats = new JPanel(new GridLayout(1, 3, 12, 0));
        stats.setBackground(UiTheme.CARD);
        stats.add(sumStat("BEST PRICE",
            sum.best == null ? "—" : CurrencyUtil.inr(sum.best.getPrice()) + "  •  " + sum.best.getMarketplace(),
            UiTheme.GREEN));
        stats.add(sumStat("HIGHEST PRICE",
            sum.highest == null ? "—" : CurrencyUtil.inr(sum.highest.getPrice()) + "  •  " + sum.highest.getMarketplace(),
            UiTheme.GOLD));
        stats.add(sumStat("PRICE DIFFERENCE", CurrencyUtil.inr(sum.difference), UiTheme.BLUE));
        card.add(stats, BorderLayout.CENTER);

        JPanel bottom = new JPanel();
        bottom.setBackground(UiTheme.CARD);
        bottom.setLayout(new BoxLayout(bottom, BoxLayout.Y_AXIS));
        // Best Overall appears ONLY when the demo data justifies it.
        if (PriceComparisonService.isBestOverallJustified(cp) && sum.best != null) {
            JLabel bestOverall = new JLabel("★ BEST OVERALL: " + sum.best.getMarketplace().toUpperCase()
                + " at " + CurrencyUtil.inr(sum.best.getPrice()) + " — cheapest available option");
            bestOverall.setFont(UiTheme.LABEL);
            bestOverall.setForeground(UiTheme.GREEN);
            bestOverall.setOpaque(true);
            bestOverall.setBackground(new Color(0x134E4A));
            bestOverall.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
            bestOverall.setName("bestOverallLabel");
            JPanel wrap = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 4));
            wrap.setBackground(UiTheme.CARD);
            wrap.add(bestOverall);
            bottom.add(wrap);
        }
        JLabel cheapest = new JLabel("Cheapest: "
            + (sum.best == null ? "—" : sum.best.getMarketplace() + " at " + CurrencyUtil.inr(sum.best.getPrice())));
        cheapest.setFont(UiTheme.SMALL);
        cheapest.setForeground(Color.WHITE);
        cheapest.setName("cheapestLabel");
        bottom.add(cheapest);

        JLabel bestShop = new JLabel("Best Price Shop: "
            + (sum.best == null ? "—" : sum.best.getMarketplace() + " — " + CurrencyUtil.inr(sum.best.getPrice())
                + "  (lowest of the 3 shops above)"));
        bestShop.setFont(UiTheme.SMALL);
        bestShop.setForeground(UiTheme.GREEN);
        bestShop.setName("bestShopLabel");
        bottom.add(bestShop);

        // When the overall lowest listing is out of stock, the cheapest
        // *available* option differs from the overall lowest — spell that
        // out so Best / Highest / Difference stay consistent and verifiable.
        if (sum.best != null && sum.highest != null) {
            PriceListing lowestOverall = cp.listings.stream()
                .min(java.util.Comparator.comparingDouble(PriceListing::getPrice)).orElse(null);
            if (lowestOverall != null
                    && !lowestOverall.getMarketplace().equalsIgnoreCase(sum.best.getMarketplace())) {
                JLabel note = new JLabel("Lowest overall: " + lowestOverall.getMarketplace()
                    + " at " + CurrencyUtil.inr(lowestOverall.getPrice())
                    + " (" + lowestOverall.getAvailability() + ")");
                note.setFont(UiTheme.SMALL);
                note.setForeground(UiTheme.MUTED);
                note.setName("lowestOverallLabel");
                bottom.add(note);
            }
        }

        JLabel banner = new JLabel("DEMO DATA — NOT LIVE MARKETPLACE PRICES");
        banner.setFont(UiTheme.LABEL);
        banner.setForeground(UiTheme.GOLD);
        banner.setOpaque(true);
        banner.setBackground(new Color(0x3B2F0B));
        banner.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
        banner.setName("demoDataBanner");
        JPanel bannerWrap = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 4));
        bannerWrap.setBackground(UiTheme.CARD);
        bannerWrap.add(banner);
        bottom.add(bannerWrap);

        JLabel demo = new JLabel("Demo Price — sample comparison data, not live marketplace prices.");
        demo.setFont(UiTheme.SMALL);
        demo.setForeground(UiTheme.MUTED);
        bottom.add(demo);
        card.add(bottom, BorderLayout.SOUTH);
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 340));
        return card;
    }

    private JPanel sumStat(String title, String value, Color accent) {
        JPanel p = new JPanel();
        p.setBackground(UiTheme.CARD_2);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBorder(BorderFactory.createCompoundBorder(
            new UiTheme.RoundedBorder(UiTheme.BORDER, 12), new EmptyBorder(10, 12, 10, 12)));
        JLabel t = new JLabel(title);
        t.setFont(UiTheme.LABEL);
        t.setForeground(UiTheme.MUTED);
        JLabel v = new JLabel(value);
        v.setFont(new Font("Segoe UI", Font.BOLD, 16));
        v.setForeground(accent);
        p.add(t);
        p.add(v);
        return p;
    }

    /** Three marketplace cards for the SAME product. */
    private JPanel marketRow(String query, ComparedProduct cp) {
        JPanel row = new JPanel(new GridLayout(1, 3, 12, 12));
        row.setBackground(UiTheme.BG);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 420));
        row.setPreferredSize(new Dimension(900, 330));
        for (String m : MARKETS) row.add(marketCard(query, cp, findListing(cp, m)));
        return row;
    }

    private PriceListing findListing(ComparedProduct cp, String market) {
        for (PriceListing l : cp.listings) if (l.getMarketplace().equalsIgnoreCase(market)) return l;
        return null;
    }

    private JPanel marketCard(String query, ComparedProduct cp, PriceListing l) {
        String market = l == null ? "?" : l.getMarketplace();
        Color brand = brandColor(market);
        JPanel card = UiTheme.card();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));

        JPanel head = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        head.setBackground(UiTheme.CARD);
        head.add(UiTheme.badge(market.isEmpty() ? "?" : market.substring(0, 1).toUpperCase(), brand));
        JLabel name = new JLabel(market.toUpperCase());
        name.setFont(UiTheme.HEADING);
        name.setForeground(Color.WHITE);
        name.setName("shopName-" + market);
        head.add(name);
        card.add(head);
        card.add(Box.createVerticalStrut(8));

        if (l == null) {
            JLabel none = new JLabel("No demo listing for this query.");
            none.setFont(UiTheme.SMALL);
            none.setForeground(UiTheme.MUTED);
            card.add(none);
        } else {
            JLabel visual = new JLabel(productEmoji(cp.product) + "  " + productTagline(cp.product));
            visual.setFont(UiTheme.SMALL);
            visual.setForeground(productAccent(cp.product));
            visual.setName("productVisual-" + market);
            card.add(visual);
            JLabel prod = new JLabel("<html><p style='width:220px'>" + displayName(cp.product) + "</p></html>");
            prod.setFont(UiTheme.SMALL);
            prod.setForeground(Color.WHITE);
            prod.setName("productLabel-" + market);
            card.add(prod);
            card.add(Box.createVerticalStrut(6));
            JLabel priceTag = new JLabel("Demo price:");
            priceTag.setFont(UiTheme.LABEL);
            priceTag.setForeground(UiTheme.MUTED);
            card.add(priceTag);
            JLabel price = new JLabel(CurrencyUtil.inr(l.getPrice()));
            price.setFont(UiTheme.CARD_VALUE);
            price.setForeground(Color.WHITE);
            price.setName("priceLabel-" + market);
            card.add(price);
            JLabel avail = new JLabel("Stock: " + l.getAvailability() + "  •  Demo Price");
            avail.setFont(UiTheme.SMALL);
            avail.setForeground("Out of Stock".equalsIgnoreCase(l.getAvailability()) ? UiTheme.RED : UiTheme.GREEN);
            avail.setName("stockLabel-" + market);
            card.add(avail);
            card.add(Box.createVerticalStrut(6));
            boolean best = cp.best != null && l.getId() == cp.best.getId();
            if (best) {
                JLabel badge = new JLabel("  ★ BEST PRICE  ");
                badge.setOpaque(true);
                badge.setBackground(new Color(0x134E4A));
                badge.setForeground(UiTheme.GREEN);
                badge.setFont(UiTheme.LABEL);
                card.add(badge);
            } else if (cp.best != null) {
                double delta = l.getPrice() - cp.best.getPrice();
                String diffText;
                if (delta > 0.001) {
                    diffText = "+" + CurrencyUtil.inr(delta) + " vs best";
                } else if (delta < -0.001) {
                    diffText = CurrencyUtil.inr(delta) + " vs best (Out of Stock — not buyable)";
                } else {
                    diffText = "Same as best price";
                }
                JLabel diff = new JLabel(diffText);
                diff.setFont(UiTheme.SMALL);
                diff.setForeground(UiTheme.GOLD);
                card.add(diff);
            }
        }
        card.add(Box.createVerticalStrut(10));
        String url = l == null ? searchUrl(marketFallback(market), query) : l.getProductUrl();
        JButton open = UiTheme.brandButton("OPEN " + market.toUpperCase(), brand);
        open.setName("openShop-" + market);
        open.setToolTipText(url);
        open.addActionListener(e -> {
            try { MarketplaceLinkBuilder.openInBrowser(url); }
            catch (Exception ex) { Dialogs.error(this, "Could not open browser:\n" + ex.getMessage()); }
        });
        card.add(open);
        return card;
    }

    private String marketFallback(String market) {
        for (String m : MARKETS) if (m.equalsIgnoreCase(market)) return m;
        return "Amazon";
    }

    /** Additional matches, honestly labelled. */
    private JPanel relatedCard(List<ComparedProduct> rest) {
        JPanel card = UiTheme.card();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        JLabel t = new JLabel("MORE RELATED MATCHES");
        t.setFont(UiTheme.LABEL);
        t.setForeground(UiTheme.MUTED);
        card.add(t);
        card.add(Box.createVerticalStrut(6));
        for (ComparedProduct cp : rest) {
            PriceSummary sum = PriceComparisonService.summarize(cp);
            JLabel r = new JLabel("• " + cp.product.getName()
                + "   —   from " + (sum.best == null ? "—" : CurrencyUtil.inr(sum.best.getPrice()) + " (" + sum.best.getMarketplace() + ")"));
            r.setFont(UiTheme.SMALL);
            r.setForeground(UiTheme.TEXT);
            card.add(r);
        }
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 140));
        return card;
    }

    /**
     * No match: show the required "No matching demo product found." message
     * directly in Swing, while keeping OPEN SHOP buttons (live search links)
     * so existing browser functionality is preserved.
     */
    private JPanel noDataCard(String q) {
        JPanel card = UiTheme.card();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        JLabel t = new JLabel("No matching demo product found.");
        t.setFont(UiTheme.HEADING);
        t.setForeground(Color.WHITE);
        t.setName("noMatchLabel");
        card.add(t);
        card.add(Box.createVerticalStrut(4));
        JLabel sub = new JLabel("<html>Try: Wireless Bluetooth Headphones, Park Avenue Voyage Body Spray, Wireless Mouse."
            + "<br/>Or open each shop below to compare live prices:</html>");
        sub.setFont(UiTheme.SMALL);
        sub.setForeground(UiTheme.MUTED);
        card.add(sub);
        card.add(Box.createVerticalStrut(10));
        JPanel row = new JPanel(new GridLayout(1, 3, 12, 12));
        row.setBackground(UiTheme.CARD);
        row.add(liveShopCard("Amazon", q));
        row.add(liveShopCard("Flipkart", q));
        row.add(liveShopCard("Meesho", q));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 230));
        card.add(row);
        return card;
    }

    /** One shop card: badge + shop name + product + real clickable shop link. */
    private JPanel liveShopCard(String market, String query) {
        Color brand = brandColor(market);
        JPanel p = new JPanel();
        p.setBackground(UiTheme.CARD_2);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBorder(BorderFactory.createCompoundBorder(
            new UiTheme.RoundedBorder(UiTheme.BORDER, 12), new EmptyBorder(10, 12, 10, 12)));
        JPanel head = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        head.setBackground(UiTheme.CARD_2);
        head.add(UiTheme.badge(market.substring(0, 1).toUpperCase(), brand));
        JLabel name = new JLabel(market.toUpperCase());
        name.setFont(UiTheme.HEADING);
        name.setForeground(Color.WHITE);
        head.add(name);
        p.add(head);
        p.add(Box.createVerticalStrut(6));
        JLabel prod = new JLabel("<html><p style='width:220px'>" + esc(query) + "</p></html>");
        prod.setFont(UiTheme.SMALL);
        prod.setForeground(UiTheme.MUTED);
        p.add(prod);
        p.add(Box.createVerticalStrut(4));
        JLabel hint = new JLabel("Live price on site — compare before buying");
        hint.setFont(UiTheme.SMALL);
        hint.setForeground(UiTheme.MUTED);
        p.add(hint);
        p.add(Box.createVerticalStrut(8));
        p.add(openSearchButton("OPEN " + market.toUpperCase(), brand, searchUrl(market, query)));
        return p;
    }

    private String esc(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    private JButton openSearchButton(String text, Color brand, String url) {
        JButton b = UiTheme.brandButton(text, brand);
        b.addActionListener(e -> {
            try { MarketplaceLinkBuilder.openInBrowser(url); }
            catch (Exception ex) { Dialogs.error(this, "Could not open browser:\n" + ex.getMessage()); }
        });
        return b;
    }

    private String searchUrl(String market, String query) {
        return switch (market) {
            case "Flipkart" -> MarketplaceLinkBuilder.flipkartSearch(query);
            case "Meesho" -> MarketplaceLinkBuilder.meeshoSearch(query);
            default -> MarketplaceLinkBuilder.amazonSearch(query);
        };
    }

    /**
     * Distinct visual identity per demo product (never the same icon twice):
     * headphones -> headphone icon, body spray -> perfume icon, mouse -> mouse icon.
     */
    public static String productEmoji(com.raceauditor.model.Product p) {
        String n = p == null || p.getName() == null ? "" : p.getName().toLowerCase(java.util.Locale.ROOT);
        if (n.contains("spray") || n.contains("voyage") || n.contains("park avenue") || n.contains("perfume")) return "\uD83E\uDDF4";
        if (n.contains("mouse")) return "\uD83D\uDDB1\uFE0F";
        return "\uD83C\uDFA7";
    }

    public static Color productAccent(com.raceauditor.model.Product p) {
        String n = p == null || p.getName() == null ? "" : p.getName().toLowerCase(java.util.Locale.ROOT);
        if (n.contains("spray") || n.contains("voyage") || n.contains("park avenue") || n.contains("perfume")) return UiTheme.CYAN;
        if (n.contains("mouse")) return UiTheme.GREEN;
        return UiTheme.PURPLE;
    }

    public static String productTagline(com.raceauditor.model.Product p) {
        String n = p == null || p.getName() == null ? "" : p.getName().toLowerCase(java.util.Locale.ROOT);
        if (n.contains("spray") || n.contains("voyage") || n.contains("park avenue") || n.contains("perfume")) return "Fragrance • Body spray";
        if (n.contains("mouse")) return "Wireless • Optical";
        return "Over-ear • Bluetooth";
    }

    /** Display name with its product icon prefix (model name itself stays plain for search/URLs). */
    public static String displayName(com.raceauditor.model.Product p) {
        if (p == null) return "";
        return productEmoji(p) + " " + p.getName();
    }

    private Color brandColor(String market) {
        if ("Flipkart".equalsIgnoreCase(market)) return UiTheme.FLIPKART;
        if ("Meesho".equalsIgnoreCase(market)) return UiTheme.MEESHO;
        return UiTheme.AMAZON;
    }
}
