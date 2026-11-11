# RACE AUDITOR — Premium Expense Manager + Price Comparison (Java Swing Desktop App, Dark Neon Edition)

Dark-neon desktop application built with **Java 21 + Swing + JDBC + MySQL 8**.
No web stack. No JavaScript/TypeScript/React/Node/Python/HTML-CSS UI.

## Features
- Splash screen → Login / Register → Sidebar dashboard
- Dashboard: monthly total, Food/Travel/Shopping cards, recent expenses, quick actions
- Expenses: add / edit / delete / search / filter / sortable table
- Categories: per-category transaction counts + totals
- **Price Comparison**: type any product (`sunscreen`, `headphones`, `shoes`, `school bag`, `smart watch`…),
  same-query results across **Amazon, Flipkart and Meesho**, Java-computed **BEST PRICE / HIGHEST / DIFFERENCE**,
  **OPEN AMAZON / FLIPKART / MEESHO** buttons opening your default browser via `Desktop.browse()`.
  All seed prices labelled **Demo Price / Sample Comparison Data** (not live).
- Reports: monthly total, transaction count, top category, pure-Swing bar chart
- Profile: view/edit name + email

## Prerequisite
- JDK 21, Maven 3.8+, MySQL 8 running locally on **port 3307**.

## 1) Create database (once)
```powershell
mysql -h 127.0.0.1 -P 3307 -u root -p -e "CREATE DATABASE IF NOT EXISTS expense_manager"
mysql -h 127.0.0.1 -P 3307 -u root -p expense_manager < database/schema.sql
```

## 2) Configure credentials (only file you may need to touch)
Edit `src/main/resources/config/db.properties`:
```properties
db.url=jdbc:mysql://127.0.0.1:3307/expense_manager?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC&characterEncoding=UTF-8
db.user=root
db.password=your_password
```
(Or set env vars `RACEAUDITOR_DB_URL`, `RACEAUDITOR_DB_USER`, `RACEAUDITOR_DB_PASSWORD`.)

## 3) Run
```powershell
mvn clean package
java -jar target/race-auditor-1.0.0.jar
```
Alternative without packaging:
```powershell
mvn exec:java -Dexec.mainClass="com.raceauditor.Main"
```
Entry point: `com.raceauditor.Main` (`src/main/java/com/raceauditor/Main.java`).

## If MySQL is down
The app shows a clear connection-error dialog (it does NOT silently switch databases, per requirements).
Start MySQL on port 3307, verify `db.properties`, re-login.

## Swapping in a real price API later
Implement a `PriceProvider` and call it from `service/PriceComparisonService.java`.
Current demo path (MySQL `products` + `price_listings`, `is_demo=1`) stays as fallback.

## Structure
```
race-auditor/
  pom.xml  README.md  run.bat  database/schema.sql
  src/main/java/com/raceauditor/
    Main.java
    config/ database/ model/ dao/ service/ util/
    ui/MainFrame.java
    ui/login/ ui/dashboard/ ui/expenses/ ui/categories/
    ui/comparison/ ui/reports/ ui/profile/
  src/main/resources/config/ images/ icons/
  src/test/java/com/raceauditor/service/ util/
```
