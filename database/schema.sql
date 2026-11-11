-- RACE AUDITOR database setup (MySQL 8, port 3307, database: expense_manager)
-- Run once:
--   mysql -h 127.0.0.1 -P 3307 -u root -p expense_manager < database/schema.sql
-- The application also auto-creates these tables on startup if missing.

CREATE TABLE IF NOT EXISTS users (
  id INT AUTO_INCREMENT PRIMARY KEY,
  full_name VARCHAR(120) NOT NULL,
  email VARCHAR(160) NOT NULL UNIQUE,
  username VARCHAR(60) NOT NULL UNIQUE,
  password_hash VARCHAR(128) NOT NULL,
  salt VARCHAR(64) NOT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS expenses (
  id INT AUTO_INCREMENT PRIMARY KEY,
  user_id INT NOT NULL,
  expense_date DATE NOT NULL,
  title VARCHAR(160) NOT NULL,
  category VARCHAR(30) NOT NULL,
  amount DECIMAL(12,2) NOT NULL,
  payment_method VARCHAR(40) NOT NULL DEFAULT 'Cash',
  notes VARCHAR(500),
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_expenses_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
  CONSTRAINT chk_expenses_category CHECK (category IN ('Food','Travel','Shopping','Education','Bills','Entertainment','Health','Other')),
  CONSTRAINT chk_expenses_amount CHECK (amount > 0),
  KEY idx_expenses_user_date (user_id, expense_date),
  KEY idx_expenses_user_cat (user_id, category)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS products (
  id INT AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(160) NOT NULL,
  brand VARCHAR(100) NOT NULL DEFAULT '',
  keywords TEXT NOT NULL,
  KEY idx_products_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS price_listings (
  id INT AUTO_INCREMENT PRIMARY KEY,
  product_id INT NOT NULL,
  marketplace VARCHAR(40) NOT NULL,
  price DECIMAL(12,2) NOT NULL,
  availability VARCHAR(40) NOT NULL DEFAULT 'In Stock',
  product_url VARCHAR(1024) NOT NULL,
  is_demo TINYINT(1) NOT NULL DEFAULT 1,
  CONSTRAINT fk_listings_product FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE,
  CONSTRAINT chk_listings_price CHECK (price > 0),
  KEY idx_listings_product (product_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- ---- Seed: 10 products x 3 marketplaces (Amazon / Flipkart / Meesho) ----
-- Prices are SAMPLE comparison data, not live marketplace prices.

DELETE FROM price_listings;
DELETE FROM products;

INSERT INTO products (id, name, brand, keywords) VALUES
  (1, 'Sunscreen SPF 50 PA+++ 50ml', 'SunSafe', 'sunscreen sunblock spf lotion skincare'),
  (2, 'Wireless Bluetooth Headphones', 'SonicWave', 'headphones headset earphone bluetooth wireless music'),
  (3, 'Thin Laptop 15.6 inch 16GB 512GB', 'AeroBook', 'laptop notebook computer ultrabook'),
  (4, 'Herbal Shampoo 340ml', 'NaturePure', 'shampoo hair haircare'),
  (5, 'Running Shoes (Unisex)', 'StrideX', 'shoes sneakers running sports footwear'),
  (6, 'Travel Backpack 45L', 'TrailPack', 'backpack bag travel laptopbag rucksack school bag'),
  (7, 'Smartwatch with AMOLED Display', 'PulseFit', 'smartwatch smart watch fitness wearable'),
  (8, 'Smartphone 5G 128GB', 'NovaPhone', 'smartphone phone mobile 5g'),
  (9, 'Mixer Grinder 750W 3 Jar', 'KitchenPro', 'mixer grinder kitchen juicer'),
  (10, 'Electric Kettle 1.8L', 'BrewFast', 'kettle electric tea water heater');

INSERT INTO price_listings (product_id, marketplace, price, availability, product_url, is_demo) VALUES
  (1, 'Amazon', 499.00, 'In Stock', 'https://www.amazon.in/s?k=sunscreen+spf+50', 1),
  (1, 'Flipkart', 549.00, 'In Stock', 'https://www.flipkart.com/search?q=sunscreen+spf+50', 1),
  (1, 'Meesho', 529.00, 'In Stock', 'https://www.meesho.com/search?q=sunscreen+spf+50', 1),
  (2, 'Amazon', 1999.00, 'Out of Stock', 'https://www.amazon.in/s?k=bluetooth+headphones', 1),
  (2, 'Flipkart', 1899.00, 'In Stock', 'https://www.flipkart.com/search?q=bluetooth+headphones', 1),
  (2, 'Meesho', 1799.00, 'In Stock', 'https://www.meesho.com/search?q=bluetooth+headphones', 1),
  (3, 'Amazon', 58990.00, 'In Stock', 'https://www.amazon.in/s?k=laptop+16gb+512gb', 1),
  (3, 'Flipkart', 57490.00, 'In Stock', 'https://www.flipkart.com/search?q=laptop+16gb+512gb', 1),
  (3, 'Meesho', 58490.00, 'In Stock', 'https://www.meesho.com/search?q=laptop+16gb+512gb', 1),
  (4, 'Amazon', 299.00, 'In Stock', 'https://www.amazon.in/s?k=herbal+shampoo', 1),
  (4, 'Flipkart', 279.00, 'In Stock', 'https://www.flipkart.com/search?q=herbal+shampoo', 1),
  (4, 'Meesho', 259.00, 'In Stock', 'https://www.meesho.com/search?q=herbal+shampoo', 1),
  (5, 'Amazon', 2499.00, 'In Stock', 'https://www.amazon.in/s?k=running+shoes', 1),
  (5, 'Flipkart', 2299.00, 'In Stock', 'https://www.flipkart.com/search?q=running+shoes', 1),
  (5, 'Meesho', 2199.00, 'In Stock', 'https://www.meesho.com/search?q=running+shoes', 1),
  (6, 'Amazon', 1799.00, 'In Stock', 'https://www.amazon.in/s?k=travel+backpack+45l', 1),
  (6, 'Flipkart', 1699.00, 'In Stock', 'https://www.flipkart.com/search?q=travel+backpack+45l', 1),
  (6, 'Meesho', 1599.00, 'In Stock', 'https://www.meesho.com/search?q=travel+backpack+45l', 1),
  (7, 'Amazon', 4999.00, 'In Stock', 'https://www.amazon.in/s?k=smartwatch+amoled', 1),
  (7, 'Flipkart', 4799.00, 'In Stock', 'https://www.flipkart.com/search?q=smartwatch+amoled', 1),
  (7, 'Meesho', 4899.00, 'In Stock', 'https://www.meesho.com/search?q=smartwatch+amoled', 1),
  (8, 'Amazon', 21999.00, 'In Stock', 'https://www.amazon.in/s?k=smartphone+5g+128gb', 1),
  (8, 'Flipkart', 21499.00, 'In Stock', 'https://www.flipkart.com/search?q=smartphone+5g+128gb', 1),
  (8, 'Meesho', 20999.00, 'In Stock', 'https://www.meesho.com/search?q=smartphone+5g+128gb', 1),
  (9, 'Amazon', 4299.00, 'In Stock', 'https://www.amazon.in/s?k=mixer+grinder+750w', 1),
  (9, 'Flipkart', 3999.00, 'In Stock', 'https://www.flipkart.com/search?q=mixer+grinder+750w', 1),
  (9, 'Meesho', 4199.00, 'In Stock', 'https://www.meesho.com/search?q=mixer+grinder+750w', 1),
  (10, 'Amazon', 1299.00, 'In Stock', 'https://www.amazon.in/s?k=electric+kettle+1.8l', 1),
  (10, 'Flipkart', 1199.00, 'In Stock', 'https://www.flipkart.com/search?q=electric+kettle', 1),
  (10, 'Meesho', 1099.00, 'In Stock', 'https://www.meesho.com/search?q=electric+kettle', 1);
