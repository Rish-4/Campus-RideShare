const db = require("../config/db");

/**
 * CREATE RIDE
 * -----------
 * Saves pickup, drop, fare for logged-in user
 */
exports.createRide = (req, res) => {
  const { pickup_lat, pickup_lng, pickup_address, drop_lat, drop_lng, drop_address, fare, distance_km } = req.body;

  // Logged-in user id comes from JWT middleware
  const userId = req.user.id;

  // Basic validation
  if (
    pickup_lat == null ||
    pickup_lng == null ||
    drop_lat == null ||
    drop_lng == null ||
    distance_km == null ||
    fare == null
  ) {
    return res.status(400).json({
      message: "All ride details are required"
    });
  }

  const sql = `
    INSERT INTO rides
    (user_id, pickup_lat, pickup_lng, pickup_address, drop_lat, drop_lng, drop_address, fare, distance_km, status)
    VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, 'REQUESTED')
  `;

  db.query(
    sql,
    [userId, pickup_lat, pickup_lng, pickup_address, drop_lat, drop_lng, drop_address, fare, distance_km],
    (err, result) => {
      if (err) {
        return res.status(500).json(err);
      }

      res.status(201).json({
        message: "Ride created successfully",
        rideId: result.insertId
      });
    }
  );
};
exports.getMyRides = (req, res) => {
  const userId = req.user.id; // from JWT

  const sql = `
    SELECT id, pickup_lat, pickup_lng, drop_lat, drop_lng, fare, status, created_at
    FROM rides
    WHERE user_id = ?
    ORDER BY created_at DESC
  `;

  db.query(sql, [userId], (err, rows) => {
    if (err) return res.status(500).json(err);
    res.json(rows);
  });
};
exports.getRideStatus = (req, res) => {

  const rideId = req.params.rideId;

  const sql = `
    SELECT r.status,
           u.name AS driver_name,
           u.phone AS driver_phone,
           d.vehicle_name,
           d.vehicle_number,
           d.avg_rating
    FROM rides r
    LEFT JOIN users u ON r.driver_id = u.id
    LEFT JOIN drivers d ON d.user_id = u.id
    WHERE r.id = ?
  `;

  db.query(sql, [rideId], (err, result) => {

    if (err) {
      console.log("STATUS ERROR:", err);
      return res.status(500).json({ message: "Server error" });
    }

    if (!result || result.length === 0) {
      return res.status(404).json({ message: "Ride not found" });
    }

    const ride = result[0];

    // ✅ SAFE RESPONSE (no crash even if driver null)
    res.json({
      status: ride.status,
      driver_name: ride.driver_name || "",
      driver_phone: ride.driver_phone || "",
      vehicle_name: ride.vehicle_name || "",
      vehicle_number: ride.vehicle_number || ""
    });
  });
};


exports.updateRideStatus = (req, res) => {
  const userId = req.user.id;
  const rideId = req.params.id;
  const { status } = req.body;

  const allowedStatuses = ["REQUESTED", "ACCEPTED", "ARRIVED", "PAID", "COMPLETED", "CANCELLED"];

  if (!allowedStatuses.includes(status)) {
    return res.status(400).json({
      message: "Invalid status"
    });
  }

  const sql = `
    UPDATE rides
    SET status = ?
    WHERE id = ?
  `;

  db.query(sql, [status, rideId], (err, result) => {
    if (err) return res.status(500).json(err);

    if (result.affectedRows === 0) {
      return res.status(404).json({
        message: "Ride not found or unauthorized"
      });
    }

    res.json({
      message: "Ride status updated",
      status
    });
  });
};

exports.getAvailableRides = (req, res) => {

  const sql = `
  SELECT 
    id,
    pickup_address,
    drop_address,
    distance_km,
    fare,
    created_at,

    pickup_lat,
    pickup_lng,

    drop_lat,
    drop_lng

  FROM rides

  WHERE status = 'REQUESTED'
    AND driver_id IS NULL

  ORDER BY created_at ASC
`;

  db.query(sql, (err, rows) => {
    if (err) {
      return res.status(500).json(err);
    }
    res.json(rows);
  });
};

exports.acceptRide = (req, res) => {
  const driverId = req.user.id;     // logged-in driver
  const rideId = req.params.id;

  const sql = `
    UPDATE rides
    SET driver_id = ?, status = 'ACCEPTED'
    WHERE id = ?
      AND status = 'REQUESTED'
      AND driver_id IS NULL
  `;

  db.query(sql, [driverId, rideId], (err, result) => {
    if (err) {
      return res.status(500).json(err);
    }

    if (result.affectedRows === 0) {
      return res.status(400).json({
        message: "Ride already taken or invalid"
      });
    }

    res.json({
      message: "Ride accepted successfully"
    });
  });
};

exports.createDriverProfile = (req, res) => {

  const userId = req.user.id; // comes from auth middleware
  const { vehicle_name, vehicle_number } = req.body;

  // validation
  if (!vehicle_name || !vehicle_number) {
    return res.status(400).json({
      message: "All fields are required"
    });
  }

  const sql = `
    INSERT INTO drivers (user_id, vehicle_name, vehicle_number)
    VALUES (?, ?, ?)
  `;

  db.query(sql, [userId, vehicle_name, vehicle_number], (err) => {

    if (err) {
      console.log("DRIVER INSERT ERROR:", err);
      return res.status(500).json({
        message: "Database error"
      });
    }

    res.json({
      message: "Driver profile created"
    });
  });
};

exports.checkDriverProfile = (req, res) => {

  const userId = req.user.id;

  const sql = "SELECT * FROM drivers WHERE user_id = ?";

  db.query(sql, [userId], (err, result) => {

    if (err) {
      return res.status(500).json({ message: "Database error" });
    }

    if (result.length > 0) {
      return res.json({ exists: true });
    } else {
      return res.json({ exists: false });
    }
  });
};

exports.submitReview = (req, res) => {

  const rideId = req.params.id;
  const { rating, review } = req.body;

  // 1️⃣ Save review in rides
  const updateRideSql = `
    UPDATE rides 
    SET rating = ?, review = ?, status = 'COMPLETED'
    WHERE id = ?
  `;

  db.query(updateRideSql, [rating, review, rideId], (err) => {

    if (err) {
      console.log(err);
      return res.status(500).json({ message: "Error saving review" });
    }

    // 2️⃣ Get driver_id from ride
    const getDriverSql = `SELECT driver_id FROM rides WHERE id = ?`;

    db.query(getDriverSql, [rideId], (err, result) => {

      if (err || result.length === 0) {
        return res.status(500).json({ message: "Driver not found" });
      }

      const driverId = result[0].driver_id;

      // 3️⃣ Update driver rating
      const updateDriverSql = `
        UPDATE drivers
        SET total_rating = total_rating + ?,
            rating_count = rating_count + 1,
            avg_rating = (total_rating + ?) / (rating_count + 1)
        WHERE user_id = ?
      `;

      db.query(updateDriverSql, [rating, rating, driverId], (err) => {

        if (err) {
          console.log(err);
          return res.status(500).json({ message: "Error updating driver rating" });
        }

        res.json({ message: "Review submitted successfully" });
      });
    });
  });
};