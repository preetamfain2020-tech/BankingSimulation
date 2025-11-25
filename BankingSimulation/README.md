
# BankingSimulation

A console-based banking system that simulates real-world banking operations including customer registration, secure login, deposits, withdrawals, fund transfers, transaction history logging, report generation, and email alerts when minimum balance rules are violated.

---

## Table of Contents
- [Demo / Screenshots](#demo--screenshots)
- [Features](#features)
- [Technology Stack](#technology-stack)
- [Project Structure](#project-structure)
- [Project Milestones](#project-milestones)
- [Requirements](#requirements)
- [Database Setup](#database-setup)
- [Configuration](#configuration)
- [Build & Run](#build--run)
- [Console Usage](#console-usage)
- [Future Improvements](#future-improvements)
- [Author](#author)
- [License](#license)
---

## Demo / Screenshots

<details>
<summary><strong>Click to View Screenshots</strong></summary>

### Registration  
![Registration](https://github.com/user-attachments/assets/32db3610-1414-492b-b192-549d79d76fb4)

### Login  
![Login](https://github.com/user-attachments/assets/f2e058ad-4757-4d45-bf7f-610eaa413a04)

### Deposit  
![Deposit](https://github.com/user-attachments/assets/f2e058ad-4757-4d45-bf7f-610eaa413a04)

### Withdraw  
![Withdraw](https://github.com/user-attachments/assets/793fcbac-e697-4635-aad4-8e3992823d98)

### Transfer  
![Transfer](https://github.com/user-attachments/assets/44baadcd-8005-46de-a9b9-8c420d727d60)

### Account Details  
![Account Details](https://github.com/user-attachments/assets/cfab97bb-eeb8-4882-93fa-bc3ea7179493)

### Transaction History  
![Transaction History](https://github.com/user-attachments/assets/502e220c-1961-40e5-b2b1-3041f2768003)

### Report File  
![Report File](https://github.com/user-attachments/assets/3ef2f3fc-9a7e-4d66-a307-6215996bbec2)

### Insufficient Balance  
![Insufficient Balance](https://github.com/user-attachments/assets/ebb66562-7b85-4b90-afb3-43c273680b90)

### Low Balance Email Alert  
![Email Alert](https://github.com/user-attachments/assets/6aa3a201-7221-4f30-9482-44da717a8bdc)

</details>

---

## Features

- Customer Registration & Secure Login  
- Deposit, Withdraw, Transfer Money  
- Minimum Balance Validation with Alerts  
- Transaction History Display  
- Auto-generated Account Summary & Transaction Reports (`bank_reports/`)  
- Email Alerts for Low Balance or Failed Transactions  
- Menu-driven Console Interface  

---

## Technology Stack

| Component | Technology |
|----------|------------|
| Language | Java 21 |
| Build Tool | Maven |
| Database | MySQL + JDBC |
| Email Service | SMTP (Mailtrap) |
| Reports | Text files (`bank_reports/`) |

---

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

---

## Project Milestones

### Milestone 1 (Weeks 1–2): Introduction & Setup
- Environment setup  
- Learned Collections, JDBC, Exception Handling  
- Designed system models  
- Created initial project structure  

### Milestone 2 (Weeks 3–4): Account Management Engine
- Registration & Login system  
- Minimum balance validations  
- JDBC-based account storage  

### Milestone 3 (Weeks 5–6): Transaction Processing & Reporting
- Deposit, Withdraw, Transfer  
- Validations & exception handling  
- Auto-generated reports  
- Email notification integration  

### Milestone 4 (Weeks 7–8): Balance Alert Tracker & Deployment
- Implemented balance alert tracker  
- End-to-end testing  
- Packaged JAR using Maven  

---

## Requirements

- Java 21  
- Maven 3.9+  
- MySQL 8+  
- Mailtrap (optional for SMTP)  

---

## Database Setup

```sql
CREATE DATABASE bankingsimulation;
````

---

## Configuration

`src/main/resources/config.properties`

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

Disable email alerts:

```
mail.enabled=false
```

---

## Build & Run

```bash
mvn clean package
java -jar target/BankingSimulation-*.jar
```

---

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

---

## Future Improvements

* Web UI (Spring Boot / Angular / React)
* Export statements (PDF/CSV)
* Admin Dashboard
* Two-Factor Authentication

---

## Author

**Preetam Kumar Giri**

GitHub: `preetamfain2020-tech`

---
## License

MIT License

Copyright (c) 2025 preetamfain2020-tech

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
---
