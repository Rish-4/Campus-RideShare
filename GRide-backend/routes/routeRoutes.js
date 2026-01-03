const express = require("express");
const router = express.Router();
const authMiddleware = require("../middleware/authMiddleware");
const { getRoute } = require("../controllers/routeController");

router.get("/route", authMiddleware, getRoute);

module.exports = router;