const express = require("express");
const router = express.Router();

const { acceptRide } = require("../controllers/rideController");

const authMiddleware = require("../middleware/authMiddleware");
const { 
  createRide,
  getMyRides,
  updateRideStatus,
  getAvailableRides
} = require("../controllers/rideController");

// DRIVER
router.get("/available", authMiddleware, getAvailableRides);

// driver accepts a ride
router.patch("/accept/:id", authMiddleware, acceptRide);

// update ride status
router.patch("/:id/status", authMiddleware, updateRideStatus);

// Get rides for logged-in user
router.get("/my", authMiddleware, getMyRides);

// Protected route
router.post("/create", authMiddleware, createRide);

module.exports = router;
