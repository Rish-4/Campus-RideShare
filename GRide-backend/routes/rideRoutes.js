const express = require("express");
const router = express.Router();

const { acceptRide } = require("../controllers/rideController");

const authMiddleware = require("../middleware/authMiddleware");
const { 
  createRide,
  getMyRides,
  updateRideStatus,
  getAvailableRides,
  getRideStatus,
  createDriverProfile,
  checkDriverProfile,
  submitReview
} = require("../controllers/rideController");

// DRIVER
router.get("/available", authMiddleware, getAvailableRides);

// driver accepts a ride
router.patch("/accept/:id", authMiddleware, acceptRide);

// update ride status
router.patch("/:id/status", authMiddleware, updateRideStatus);

// Get rides for logged-in user
router.get("/my", authMiddleware, getMyRides);

// Get ride status
router.get("/status/:rideId", authMiddleware, getRideStatus);

// Create driver profile
router.post("/driver/create", authMiddleware, createDriverProfile);

// Check if user has driver profile
router.get("/driver/check", authMiddleware, checkDriverProfile);

// Protected route
router.post("/create", authMiddleware, createRide);

router.patch("/review/:id", authMiddleware, submitReview);

module.exports = router;
