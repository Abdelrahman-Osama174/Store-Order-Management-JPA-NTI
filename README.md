# 🛒 Online Store Order Management

> **NTI Capstone Project** — Spring Data Access & Spring ORM/JPA Integration  
> Individual project | Suggested duration: 2 weeks | Grade: 100 pts (+ 10 bonus)

---

## 📌 Overview

A fully functional **back-end system** for a small online store built with **plain Spring Framework + JPA/Hibernate** — no Spring Boot, no Spring Data JPA.

The system manages **customers, products, orders, and payments** with full transaction consistency (e.g. two customers buying the last item in stock at the same time).

There is no web layer. The application is driven by a `Main` class and automated tests.

---

## ⚙️ Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 21 |
| Framework | Spring Framework 6.1 (plain, no Boot) |
| ORM | Hibernate 6.4 via Spring ORM |
| JPA Provider | `LocalContainerEntityManagerFactoryBean` + `HibernateJpaVendorAdapter` |
| Database | PostgreSQL |
| Build | Maven |
| Testing | JUnit 5 |

---

## 🚫 Constraints

This project intentionally avoids:

| Forbidden | Reason |
|---|---|
| `Spring Boot` | Manual Spring wiring is the learning goal |
| `Spring Data JPA` (`JpaRepository`, derived queries) | Hand-written repositories only |
| `JdbcTemplate` / Spring JDBC | JPA/JPQL/Criteria API only |
| Native Hibernate API (`Session`, `SessionFactory`) | Jakarta Persistence API only |

---

## 📁 Project Structure

```
store/
├── pom.xml
├── README.md
└── src
    ├── main/java/com/store
    │   ├── config/
    │   │   └── AppConfig.java          # DataSource, EMF, TxManager, PETP
    │   ├── model/                      # JPA Entities & Embeddables
    │   │   ├── BaseEntity.java
    │   │   ├── Customer.java
    │   │   ├── Category.java
    │   │   ├── Product.java
    │   │   ├── Order.java
    │   │   ├── OrderItem.java
    │   │   ├── Payment.java
    │   │   └── AuditLog.java
    │   ├── embeddables/
    │   │   └── Address.java
    │   ├── enums/
    │   │   ├── OrderStatus.java
    │   │   └── PaymentMethod.java
    │   ├── dto/                        # Query result DTOs (Java records)
    │   │   ├── OrderSummaryDTO.java
    │   │   ├── OrderItemDTO.java
    │   │   ├── CategoryRevenue.java
    │   │   ├── CustomerSpend.java
    │   │   ├── OrderStatusCount.java
    │   │   └── MonthlySales.java
    │   ├── repository/                 # Hand-written @Repository classes
    │   │   ├── CustomerRepo.java
    │   │   ├── ProductRepo.java
    │   │   ├── OrderRepo.java
    │   │   ├── CategoryRepo.java
    │   │   ├── AuditLogRepo.java
    │   │   └── ReportRepo.java
    │   ├── service/                    # Business logic + @Transactional
    │   │   ├── CustomerService.java
    │   │   ├── ProductService.java
    │   │   ├── OrderService.java
    │   │   ├── ReportService.java
    │   │   └── AuditLogService.java
    │   ├── exception/                  # Custom unchecked exceptions
    │   │   ├── InsufficientStockException.java
    │   │   ├── InvalidOrderStateException.java
    │   │   ├── DuplicateCustomerException.java
    │   │   └── ResourceNotFoundException.java
    │   └── App.java                    # Demo scenario entry point
    └── test/java/com/store             # JUnit 5 integration tests
```

---

## 🗂️ Domain Model

```
Customer ──< Order >── OrderItem >── Product >── Category
                │
                └── Payment
```

| Entity | Key Fields | Notes |
|---|---|---|
| `BaseEntity` | `id`, `createdAt`, `@Version` | All entities extend it |
| `Customer` | `name`, `email` (unique), `Address`, `orders` | `Address` is `@Embeddable` |
| `Category` | `name` (unique) | — |
| `Product` | `sku` (unique), `name`, `price`, `stock`, `categories` | `@ManyToMany` with Category |
| `Order` | `customer`, `status`, `orderedAt`, `items`, `payment` | Table name: `orders` |
| `OrderItem` | `order`, `product`, `quantity`, `unitPrice` | Price copied at order time |
| `Payment` | `order`, `amount`, `method`, `paidAt` | `@OneToOne` with Order |
| `AuditLog` | `action`, `details`, `happenedAt` | Saved even if main tx fails |

---

## 🔁 Order Lifecycle

```
NEW ──► PAID ──► SHIPPED
 │        │
 └────────┴──► CANCELLED
```

| Transition | Method | Rule |
|---|---|---|
| → PAID | `pay()` | Only from `NEW` |
| → SHIPPED | `ship()` | Only from `PAID` |
| → CANCELLED | `cancel()` | From `NEW` or `PAID` — restores stock |

---

## 💡 Key JPA Concepts Demonstrated

### ✅ Configuration
- `LocalContainerEntityManagerFactoryBean` with `HibernateJpaVendorAdapter`
- `JpaTransactionManager`
- `@EnableTransactionManagement`
- `static PersistenceExceptionTranslationPostProcessor`

### ✅ Repositories
- `@PersistenceContext EntityManager` — hand-written, no Spring Data
- **JPQL** queries with named parameters
- **Criteria API** — dynamic multi-filter product search
- **JOIN FETCH** — avoids N+1 problem in `findByIdWithItems()`
- **Pagination** — `setFirstResult` / `setMaxResults`

### ✅ Transactions
- `@Transactional` on **service methods only**, never on repositories
- `readOnly = true` on all read-only methods
- `Propagation.REQUIRES_NEW` on `AuditLogService.log()` — audit entry is saved even if the main transaction rolls back
- **Dirty checking** — `pay()` updates order status with no explicit `save()` call

### ✅ Queries & Reports (`ReportRepository`)

| Method | Technique |
|---|---|
| `revenueByCategory()` | JPQL + `SUM` + `GROUP BY` |
| `topCustomers(limit)` | JPQL + `ORDER BY SUM DESC` |
| `ordersPerStatus()` | JPQL → `Map` via stream |
| `productsNeverOrdered()` | JPQL `NOT EXISTS` |
| `monthlySales(year)` | JPQL `MONTH()` + `YEAR()` |
| `applyDiscount(category, %)` | JPQL bulk `UPDATE` + `em.clear()` |

---

## 🧹 Why `em.clear()` After Bulk Update?

JPQL bulk `UPDATE` bypasses the JPA first-level cache (persistence context) and writes directly to the database.  
Any `Product` entity already loaded in memory still holds the **old price**.

Calling `em.clear()` evicts all cached entities, so the next `em.find(Product)` fetches fresh data from the database.

```
Without clear():  DB = new price ✅  |  In-memory entity = old price ❌
With clear():     DB = new price ✅  |  Next read = new price ✅
```

---

## 🚀 How to Run

### Prerequisites
- Java 21
- Maven 3.8+
- PostgreSQL running locally

### Setup

```bash
# Clone the repo
git clone https://github.com/Abdelrahman-Osama174/Store-Order-Management-JPA-NTI.git
cd Store-Order-Management-JPA-NTI
```

Create the database in PostgreSQL:
```sql
CREATE DATABASE storedb;
```

Update `AppConfig.java` with your credentials:
```java
ds.setUrl("jdbc:postgresql://localhost:5432/storedb");
ds.setUsername("your_username");
ds.setPassword("your_password");
```

### Run

```bash
mvn compile exec:java -Dexec.mainClass="com.store.App"
```

Or run `App.java` directly from IntelliJ IDEA.

---

## 📦 Maven Dependencies

```xml
<dependencies>
    <!-- Spring -->
    <dependency>
        <groupId>org.springframework</groupId>
        <artifactId>spring-context</artifactId>
        <version>6.1.14</version>
    </dependency>
    <dependency>
        <groupId>org.springframework</groupId>
        <artifactId>spring-orm</artifactId>
        <version>6.1.14</version>
    </dependency>

    <!-- Hibernate -->
    <dependency>
        <groupId>org.hibernate.orm</groupId>
        <artifactId>hibernate-core</artifactId>
        <version>6.4.10.Final</version>
    </dependency>

    <!-- PostgreSQL -->
    <dependency>
        <groupId>org.postgresql</groupId>
        <artifactId>postgresql</artifactId>
        <version>42.7.3</version>
    </dependency>

    <!-- JUnit 5 -->
    <dependency>
        <groupId>org.junit.jupiter</groupId>
        <artifactId>junit-jupiter</artifactId>
        <version>5.10.0</version>
        <scope>test</scope>
    </dependency>
</dependencies>
```

---

## 👨‍💻 Author

**Abdelrahman Osama**  
Computer Science & Engineering Graduate — Menoufia University (2025)  
NTI Spring Data Access & ORM/JPA Course

---

> *Built as part of the NTI (National Telecom Institute) backend development training program.*
