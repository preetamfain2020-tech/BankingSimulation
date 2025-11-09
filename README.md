```markdown
# BankingSimulation

A console-based banking system that simulates real-world banking operations including customer registration, secure login, deposits, withdrawals, fund transfers, transaction history logging, report generation, and email alerts when minimum balance rules are violated.

## Features

- Customer Registration & Secure Login
- Deposit, Withdraw, Transfer Money
- Minimum Balance Validation with Alerts
- Transaction History Display
- Auto-generated Account Summary & Transaction Reports (`bank_reports/`)
- Email Alerts for Low Balance or Failed Transactions (Mailtrap / SMTP)
- Clean, Menu-driven Console Interface

## Tech Stack

| Component | Technology |
|----------|------------|
| Language | Java 21 |
| Build Tool | Maven |
| Database | MySQL + JDBC |
| Email Service | SMTP (tested with Mailtrap) |
| Reports | Text files stored in `bank_reports/` |

## Project Structure

```

BankingSimulation/
├─ src/
│  └─ main/
│     ├─ java/org/banking/
│     │  ├─ dao/
│     │  ├─ model/
│     │  ├─ service/
│     │  ├─ util/
│     │  └─ Main.java
│     └─ resources/config.properties
├─ bank_reports/
├─ pom.xml
└─ README.md

````

## Requirements

- Java 21
- Maven 3.9+
- MySQL 8+
- (Optional) Mailtrap account for SMTP testing

## Database Setup

```sql
CREATE DATABASE bankingsimulation;
````

## Configuration (`src/main/resources/config.properties`)

```properties
db.url=jdbc:mysql://localhost:3306/bankingsimulation
db.user=YOUR_DB_USERNAME
db.password=YOUR_DB_PASSWORD

mail.enabled=true
mail.smtp.host=sandbox.smtp.mailtrap.io
mail.smtp.port=587
mail.username=YOUR_MAILTRAP_USERNAME
mail.password=YOUR_MAILTRAP_PASSWORD
mail.from=bank.alerts@testmail.com
mail.starttls=true

report.dir=bank_reports
min.balance=500
```

Set `mail.enabled=false` to disable email alerts.

## Build & Run

```bash
mvn clean package
java -jar target/BankingSimulation-*.jar
```

## Console Usage

```
1) Register
2) Login
3) Exit

After Login:
1) Deposit
2) Withdraw
3) Transfer
4) Account Details
5) Transactions
6) Logout
```

## Screenshots

### Registration

![Registration](LINK_HERE)

### Login

![Login](LINK_HERE)

### Deposit

![Deposit](LINK_HERE)

### Withdraw

![Withdraw](LINK_HERE)

### Transfer

![Transfer](LINK_HERE)

### Account Details

![Account Details](LINK_HERE)

### Transaction History

![Transaction History](LINK_HERE)

### Report File (bank_reports/)

![Report File](LINK_HERE)

### Email Alert (Low Balance)

![Email Alert](LINK_HERE)

## Future Improvements

* Web UI (Spring Boot / Angular / React)
* Export transaction statements (PDF/CSV)
* Role-based Admin Dashboard
* Two-Factor Authentication

## Author

**Preetam Kumar Giri**
GitHub: `preetamfain2020-tech`

```
```
