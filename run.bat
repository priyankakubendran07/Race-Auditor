@echo off
REM Race Auditor quick run (Windows). Requires JDK 21 + Maven + MySQL 8 on port 3307.
REM 1) mysql -h 127.0.0.1 -P 3307 -u root -p -e "CREATE DATABASE IF NOT EXISTS expense_manager"
REM 2) mysql -h 127.0.0.1 -P 3307 -u root -p expense_manager < database\schema.sql
REM 3) edit src\main\resources\config\db.properties if needed
mvn clean package
if errorlevel 1 exit /b 1
java -jar target\race-auditor-1.0.0.jar
