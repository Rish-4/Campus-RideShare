

# 🚕 GRide – Campus Ride Sharing App

**GRide** is a **campus-focused ride-sharing application** inspired by platforms like Uber and Ola, designed specifically for **university campuses**.
It enables safe, controlled ride sharing within campus boundaries and supports **Passenger (User)** and **Driver** roles with real-time ride workflows.

The project is built as a **full-stack system** with an **Android frontend** and a **Node.js + MySQL backend**.

---

## 📌 Key Highlights

* Campus rule enforcement (pickup or drop must lie within campus)
* Role-based authentication (User / Driver)
* JWT-secured APIs
* Map-based location selection
* Payment workflow simulation
* Clean separation of frontend and backend

---

## ✨ Features

### 👤 User (Passenger)

* Register & Login (**JWT-based authentication**)
* Select **pickup & drop locations** using Google Maps
* Campus boundary validation
* Automatic **distance & fare calculation**
* Ride confirmation screen
* Multiple payment options:

  * UPI (Intent-based)
  * Wallet (mock implementation)
  * Cash
* Ride lifecycle tracking:

  ```
  REQUESTED → PAID → COMPLETED
  ```
* View complete ride history

---

### 🚗 Driver

* Login using **DRIVER role**
* View **available ride requests**
* See detailed ride info:

  * Pickup address
  * Drop address
  * Distance
  * Fare
* Accept rides
* Mark rides as completed
* Logout functionality

---

## 🧱 Tech Stack

### 📱 Android Frontend

* **Language:** Java
* **UI:** XML (Activity-based architecture)
* **Maps:** Google Maps SDK for Android
* **Networking:** Retrofit
* **Authentication Storage:** SharedPreferences
* **Location Services:** FusedLocationProviderClient
* **Minimum SDK:** 24
* **Target SDK:** 36

---

### 🌐 Backend

* **Runtime:** Node.js
* **Framework:** Express.js
* **Database:** MySQL
* **Authentication:** JWT
* **Password Hashing:** bcrypt
* **Database Access:** Raw SQL using `mysql2`
* **Environment Config:** dotenv

---

## 📂 Project Structure

### 📱 Android (`GRide-Android`)

```
app/
 ├── activities/
 │    ├── LoginActivity.java
 │    ├── RegisterActivity.java
 │    ├── HomeActivity.java
 │    ├── RideConfirmActivity.java
 │    ├── PaymentActivity.java
 │    ├── RideStatusActivity.java
 │    ├── AvailableRidesActivity.java
 │    └── DriverHomeActivity.java
 ├── network/
 │    ├── ApiClient.java
 │    └── ApiService.java
 └── res/
      ├── layout/
      ├── menu/
      └── values/
```

---

### 🌐 Backend (`GRide-backend`)

```
gride-backend/
 ├── controllers/
 │    ├── authController.js
 │    └── rideController.js
 ├── middleware/
 │    └── authMiddleware.js
 ├── routes/
 │    ├── authRoutes.js
 │    └── rideRoutes.js
 ├── config/
 │    └── db.js
 ├── server.js
 └── .env   (ignored in Git)
```

---

## 🗄️ Database Setup

### 1️⃣ Create Database

```sql
CREATE DATABASE gride_db;
USE gride_db;
```

---

### 2️⃣ Users Table

```sql
CREATE TABLE users (
  id INT AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(100) NOT NULL,
  email VARCHAR(100) NOT NULL UNIQUE,
  password VARCHAR(255) NOT NULL,
  role ENUM('USER','DRIVER') DEFAULT 'USER',
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

---

### 3️⃣ Rides Table

```sql
CREATE TABLE rides (
  id INT AUTO_INCREMENT PRIMARY KEY,
  user_id INT NOT NULL,
  driver_id INT DEFAULT NULL,
  pickup_lat DOUBLE,
  pickup_lng DOUBLE,
  pickup_address VARCHAR(255),
  drop_lat DOUBLE,
  drop_lng DOUBLE,
  drop_address VARCHAR(255),
  distance_km FLOAT,
  fare INT,
  status ENUM('REQUESTED','PAID','COMPLETED','CANCELLED') DEFAULT 'REQUESTED',
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

---

## 🔐 Backend Setup

### 1️⃣ Install Dependencies

```bash
npm install
```

---

### 2️⃣ Environment Variables (`.env`)

```env
PORT=5000
DB_HOST=localhost
DB_USER=root
DB_PASSWORD=your_mysql_password
DB_NAME=gride_db
JWT_SECRET=super_secret_key
```

> ⚠️ `.env` is intentionally **excluded from GitHub** for security.

---

### 3️⃣ Start Backend Server

```bash
npx nodemon server.js
```

Server runs at:

```
http://localhost:5000
```

---

## 🔗 API Endpoints

### 🔑 Authentication

| Method | Endpoint           | Description   |
| ------ | ------------------ | ------------- |
| POST   | /api/auth/register | Register user |
| POST   | /api/auth/login    | Login user    |

---

### 🚕 Ride Management

| Method | Endpoint              | Description                |
| ------ | --------------------- | -------------------------- |
| POST   | /api/rides/create     | Create new ride            |
| GET    | /api/rides/my         | User ride history          |
| GET    | /api/rides/available  | Available rides for driver |
| PATCH  | /api/rides/:id/status | Update ride status         |

🔒 **Protected Routes Require Header:**

```
Authorization: Bearer <JWT_TOKEN>
```

---

## 📱 Android Setup

### 1️⃣ Clone Repository

```bash
git clone https://github.com/your-username/GRIDE.git
```

---

### 2️⃣ Open in Android Studio

* **Build System:** Gradle (Groovy DSL)
* **Language:** Java
* **Minimum SDK:** 24
* **Target SDK:** 36

---

### 3️⃣ Backend Base URL

In `ApiClient.java`:

```java
.baseUrl("http://10.0.2.2:5000/")
```

> For a physical device, use your system’s local IP:

```java
http://192.168.x.x:5000/
```

---

## 🗺️ Google Maps Setup (Secure)

### Required API

* **Maps SDK for Android**

> ❗ Directions API / Polylines are excluded due to billing limitations.

### API Key Handling

* API key is **not hardcoded**
* Loaded securely via `local.properties`
* Injected at build time using Gradle
* Excluded from GitHub using `.gitignore`

✔ Safe for public repositories

---

## 🧠 Authentication Flow

1. User logs in
2. Backend validates credentials
3. JWT token is returned
4. Token stored in SharedPreferences
5. Role-based navigation:

   * **USER → HomeActivity**
   * **DRIVER → DriverHomeActivity**

---

## 📌 Current Status

✔ User & Driver authentication
✔ JWT-secured APIs
✔ Ride creation & lifecycle
✔ Payment flow (mock + UPI intent)
✔ Ride history
✔ Driver ride acceptance
✔ Ride completion logic

---

## 🚀 Future Enhancements

* Google Directions API (Polyline support)
* Live driver tracking
* Push notifications (FCM)
* Driver earnings dashboard
* Admin panel
* Digital wallet system

---

## 🧑‍💻 Author

**Rishabh**
MCA | Full-Stack Developer
Android • Node.js • MySQL
