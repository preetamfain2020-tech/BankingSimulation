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

![Registration](<img width="1135" height="662" alt="Image" src="https://github.com/user-attachments/assets/32db3610-1414-492b-b192-549d79d76fb4" />)

### Login

![Login](<img width="1314" height="663" alt="Image" src="https://github.com/user-attachments/assets/f2e058ad-4757-4d45-bf7f-610eaa413a04" />)

### Deposit

![Deposit](<img width="1314" height="663" alt="Image" src="https://github.com/user-attachments/assets/f2e058ad-4757-4d45-bf7f-610eaa413a04" />)

### Withdraw

![Withdraw](<img width="1375" height="668" alt="Screenshot 2025-11-09 180017" src="https://github.com/user-attachments/assets/793fcbac-e697-4635-aad4-8e3992823d98" />
)

### Transfer

![Transfer](<img width="1348" height="578" alt="Screenshot 2025-11-09 180358" src="https://github.com/user-attachments/assets/44baadcd-8005-46de-a9b9-8c420d727d60" />
)

### Account Details

![Account Details](<img width="1375" height="668" alt="Screenshot 2025-11-09 180017" src="https://github.com/user-attachments/assets/cfab97bb-eeb8-4882-93fa-bc3ea7179493" />
)

### Transaction History

![Transaction History](<img width="1015" height="725" alt="Screenshot 2025-11-09 180123" src="https://github.com/user-attachments/assets/502e220c-1961-40e5-b2b1-3041f2768003" />
)

### Report File (bank_reports/)

![Report File](<img width="1299" height="215" alt="Screenshot 2025-11-09 180823" src="https://github.com/user-attachments/assets/3ef2f3fc-9a7e-4d66-a307-6215996bbec2" />

)


### Withdraw (Insufficient Balance Case)
![Insufficient Balance](<img width="1508" height="799" alt="Screenshot 2025-11-09 180655" src="https://github.com/user-attachments/assets/ebb66562-7b85-4b90-afb3-43c273680b90" />
)

### Email Alert (Low Balance)

![Email Alert](<img width="1652" height="847" alt="Screenshot 2025-11-09 180922" src="https://github.com/user-attachments/assets/6aa3a201-7221-4f30-9482-44da717a8bdc" />
)

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
