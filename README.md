---

# 🚕 GRide – Campus Ride Sharing App

GRide is a **campus-based ride sharing application** inspired by services like Uber/Ola, designed specifically for university campuses.
It supports **User (Passenger)** and **Driver** roles, ride booking, payments, ride history, and driver ride acceptance.

---

## 📌 Features

### 👤 User (Passenger)

* Register & Login (JWT based authentication)
* Select **pickup & drop locations** on map
* Campus rule enforced (either pickup or drop must be inside campus)
* Fare & distance calculation
* Ride confirmation screen
* Payment options:

  * UPI (intent based)
  * Wallet (mock)
  * Cash
* Ride status updates (REQUESTED → PAID → COMPLETED)
* Ride history

### 🚗 Driver

* Login as DRIVER role
* View **Available Rides**
* See:

  * Pickup address
  * Drop address
  * Distance
  * Fare
* Accept rides
* Complete rides
* Logout

---

## 🧱 Tech Stack

### 📱 Android Frontend

* Language: **Java**
* UI: XML (Activities)
* Maps: Google Maps SDK
* Networking: **Retrofit**
* Auth storage: **SharedPreferences**
* Location: FusedLocationProviderClient

### 🌐 Backend

* Runtime: **Node.js**
* Framework: **Express**
* Database: **MySQL**
* Auth: **JWT**
* Password hashing: **bcrypt**
* ORM: Raw SQL (mysql2)

---

## 📂 Project Structure

### Android

```
app/
 ├── activities/
 │    ├── LoginActivity
 │    ├── RegisterActivity
 │    ├── HomeActivity
 │    ├── RideConfirmActivity
 │    ├── PaymentActivity
 │    ├── RideStatusActivity
 │    ├── AvailableRidesActivity
 │    └── DriverHomeActivity
 ├── network/
 │    ├── ApiClient
 │    └── ApiService
 └── res/
      ├── layout/
      ├── menu/
      └── values/
```

### Backend

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
 └── .env
```

---

## 🗄️ Database Setup

### 1️⃣ Create Database

```sql
CREATE DATABASE gride_db;
USE gride_db;
```

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

### 2️⃣ Environment Variables (`.env`)

```env
PORT=5000
DB_HOST=localhost
DB_USER=root
DB_PASSWORD=your_mysql_password
DB_NAME=gride_db
JWT_SECRET=super_secret_key
```

### 3️⃣ Start Server

```bash
npx nodemon server.js
```

Server runs on:

```
http://localhost:5000
```

---

## 🔗 API Endpoints

### 🔑 Auth

| Method | Endpoint           | Description   |
| ------ | ------------------ | ------------- |
| POST   | /api/auth/register | Register user |
| POST   | /api/auth/login    | Login user    |

### 🚕 Rides

| Method | Endpoint              | Description            |
| ------ | --------------------- | ---------------------- |
| POST   | /api/rides/create     | Create ride            |
| GET    | /api/rides/my         | User ride history      |
| GET    | /api/rides/available  | Driver available rides |
| PATCH  | /api/rides/:id/status | Update ride status     |

> All protected routes require:

```
Authorization: Bearer <JWT_TOKEN>
```

---

## 📱 Android Setup

### 1️⃣ Clone Project

```bash
git clone https://github.com/your-username/GRide.git
```

### 2️⃣ Open in Android Studio

* **Build system**: Groovy DSL
* **Language**: Java
* **Minimum SDK**: 24
* **Target SDK**: 36

### 3️⃣ API Base URL

In `ApiClient.java`:

```java
.baseUrl("http://10.0.2.2:5000/")
```

> For physical device, replace with your local IP:

```java
http://192.168.x.x:5000/
```

---

## 🗺️ Google Maps Setup

### Required APIs

* Maps SDK for Android

> ❗ Directions API (Polyline) is skipped due to billing limitations.

### Add API Key

`res/values/google_maps_api.xml`

```xml
<string name="google_maps_key">YOUR_API_KEY</string>
```

---

## 🧠 Authentication Flow

1. User logs in
2. Backend validates credentials
3. JWT token returned
4. Token stored in SharedPreferences
5. App decides screen:

   * USER → HomeActivity
   * DRIVER → DriverHomeActivity

---

## ✅ Current Status

✔ Login / Register
✔ JWT authentication
✔ User & Driver roles
✔ Ride booking
✔ Payment handling
✔ Ride history
✔ Driver available rides
✔ Ride acceptance
✔ Ride completion

---

## 🚀 Future Enhancements

* Google Directions polyline (billing enabled)
* Live driver tracking
* Push notifications
* Driver earnings dashboard
* Admin panel
* Wallet system

---

## 🧑‍💻 Author

**Rishabh**
MCA | Full-Stack Developer
Android • Node.js • MySQL

---
