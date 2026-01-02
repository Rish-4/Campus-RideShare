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
exports.updateRideStatus = (req, res) => {
  const userId = req.user.id;
  const rideId = req.params.id;
  const { status } = req.body;

  const allowedStatuses = ["PAID", "COMPLETED", "CANCELLED"];

  if (!allowedStatuses.includes(status)) {
    return res.status(400).json({
      message: "Invalid status"
    });
  }

  const sql = `
    UPDATE rides
    SET status = ?
    WHERE id = ? AND user_id = ?
  `;

  db.query(sql, [status, rideId, userId], (err, result) => {
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
      id, pickup_address,
      drop_address, distance_km,
      fare, created_at
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
