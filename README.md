# Smart Inventory Reservation System

A backend-focused inventory reservation system designed to handle **concurrent checkouts**, **flash-sale scenarios**, and **abandoned carts** without overselling products.

---

## 📌 Problem Statement

In e-commerce platforms, multiple users may attempt to purchase the same product at the same time.  
This can lead to **overselling**, **inconsistent stock**, and **poor user experience**, especially during flash sales.

Traditional database-based stock updates are slow and unsafe under high concurrency.

---

## 💡 Solution Overview

The Smart Inventory Reservation System solves this by:

- Using **Redis** for real-time stock tracking and temporary reservations
- Applying **time-bound reservations** to handle abandoned carts
- Ensuring **atomic stock updates** to prevent overselling
- Persisting only **confirmed orders** in **MySQL**
- Keeping MySQL stock immutable and using Redis as the real-time authority

---

## 🛠️ Technologies Used

- **Java 17**
- **Spring Boot**
- **Spring Data JPA**
- **Redis (Dockerized)**
- **MySQL**
- **Lettuce Redis Client**
- **Lua Scripts (Redis atomic operations)**

---

## 📂 Project Structure

```
smart-inventory-reservation/
├─ src/main/java/com/example/sirs/
│  ├─ controller/
│  │  ├─ InventoryController.java
│  │  └─ CheckoutController.java
│  ├─ service/
│  │  ├─ InventoryService.java
│  │  ├─ ReservationService.java
│  │  └─ OrderService.java
│  ├─ scheduler/
│  │  └─ ExpiryReclaimerScheduler.java
│  ├─ entity/
│  │  ├─ Product.java
│  │  ├─ AppUser.java
│  │  └─ OrderEntity.java
│  ├─ repository/
│  │  ├─ ProductRepo.java
│  │  ├─ UserRepo.java
│  │  └─ OrderRepo.java
│  ├─ utils/
│  │  └─ RedisLuaScripts.java
│  └─ config/
│     └─ RedisConfig.java
├─ src/main/resources/
│  └─ application.properties
└─ README.md
```

---

## 🔄 System Workflow

### 1. Inventory Initialization
- Product stock is stored permanently in MySQL (`initial_stock`)
- Redis stock is initialized lazily when accessed for the first time

### 2. Reserve Inventory
- User requests to reserve a product
- Redis Lua script atomically:
  - Checks stock availability
  - Decrements stock
  - Creates a reservation entry
  - Adds expiry metadata

### 3. Checkout Confirmation
- If user confirms within reservation time:
  - Order is saved in MySQL
  - Reservation is removed from Redis
  - Stock remains reduced

### 4. Reservation Expiry
- A scheduled job scans expired reservations
- Automatically restores stock in Redis
- Cleans up reservation data

---

## 🌐 API Endpoints

### 1️⃣ Reserve Inventory
**POST** `/inventory/reserve`

**Request**
```json
{
  "userId": 101,
  "sku": "SKU-IPHONE-15",
  "quantity": 1
}
```

**Response**
```json
{
  "reservationId": "uuid-value",
  "sku": "SKU-IPHONE-15",
  "quantity": 1,
  "expiresInSeconds": 60,
  "message": "Reserved"
}
```

---

### 2️⃣ Confirm Checkout
**POST** `/checkout/confirm`

**Request**
```json
{
  "reservationId": "uuid-value",
  "userId": 101
}
```

**Response**
```
Confirmed
```

---

### 3️⃣ Cancel Checkout
**POST** `/checkout/cancel`

**Request**
```json
{
  "reservationId": "uuid-value",
  "userId": 101
}
```

**Response**
```
Cancelled
```

---

### 4️⃣ Get Inventory
**GET** `/inventory/{sku}`

**Response**
```json
{
  "sku": "SKU-IPHONE-15",
  "available": 4
}
```

---

## ⏱ Reservation & Expiry Rules

- Reservation TTL: **60 seconds**
- Scheduler runs every **5 seconds**
- Expired reservations automatically restore stock
- Cancel/confirm is valid **only while reservation exists**

---

## 🧠 Key Design Decisions

- Redis handles **real-time availability**
- MySQL stores **immutable reference and order data**
- No direct stock mutation in MySQL during checkout
- Scheduler ensures abandoned carts don’t block inventory
- Idempotent APIs prevent duplicate operations

---

## 🐳 Redis Setup (Docker)

```bash
docker run --name my-redis -p 6379:6379 -d redis
```

If Redis requires authentication, configure it in `application.properties`.

---

## 🧪 Sample MySQL Seed Data

```sql
INSERT INTO products (sku, name, initial_stock, price)
VALUES ('SKU-IPHONE-15', 'iPhone 15', 5, 79999);

INSERT INTO app_users (username, email)
VALUES ('gopi', 'gopi@example.com');
```

---

## ⚠️ Important Notes

- Redis data is **volatile** and may reset on restart
- MySQL remains the **source of truth**
- Stock is rebuilt from MySQL if Redis restarts
- Reservation cancellation after expiry is intentionally not allowed

---

## ✅ Result

- Prevented overselling completely
- Automatically reclaimed abandoned stock
- Ensured smooth checkout under concurrent load
- Built a scalable, production-ready backend architecture

---

---

## 📄 License

This project is for learning and demonstration purposes.
