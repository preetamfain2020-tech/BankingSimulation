
# Banking Transaction Simulator (Java)

A console-based banking system built using Core Java, Collections, JDBC, File Reporting, and Email Alerts.

## Features

- Create and manage customer accounts
- Secure login (SHA-256 password hashing)
- Deposit, Withdraw, and Transfer funds
- Minimum balance validation
- Logs transactions to MySQL and text files
- Sends low balance email alerts

## Technology Stack

| Component            | Technology Used          |
|---------------------|--------------------------|
| Language            | Java 21                  |
| Database            | MySQL + JDBC             |
| Build Tool          | Maven                    |
| Logging             | SLF4J Simple Logger      |
| Email Testing       | Mailtrap SMTP            |
| Reporting           | Text file logs           |

## Project Structure

```

src/
├── main/java/org/banking/
│   ├── dao/
│   ├── model/
│   ├── service/
│   ├── util/
│   └── Main.java
└── main/resources/
└── config.properties
bank_reports/

````

## Setup

### 1. Database
```sql
CREATE DATABASE bankingsimulation;
````

### 2. Configure `config.properties`

```
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
```

### 3. Run

```
mvn clean package
mvn exec:java
```

## Usage

```
Register → Login → Menu:
1. Deposit
2. Withdraw
3. Transfer
4. Account Details
5. Transaction History
```

Reports stored in:

```
bank_reports/
```

Emails visible in Mailtrap inbox.

`
![img.png](C:\Users\KIIT0001\OneDrive\Desktop\CI\WhatsApp Image 2025-11-07 at 14.02.42_4a6748bd.jpg)``
```
