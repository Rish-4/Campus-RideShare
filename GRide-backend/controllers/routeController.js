const axios = require("axios");

exports.getRoute = async (req, res) => {
  try {
    const { startLat, startLng, endLat, endLng } = req.query;

    if (!startLat || !startLng || !endLat || !endLng) {
      return res.status(400).json({
        message: "Missing coordinates"
      });
    }

    const response = await axios.post(
  "https://api.openrouteservice.org/v2/directions/driving-car",
  {
    coordinates: [
      [parseFloat(startLng), parseFloat(startLat)],
      [parseFloat(endLng), parseFloat(endLat)]
    ]
  },
  {
    headers: {
      Authorization: process.env.ORS_API_KEY,
      "Content-Type": "application/json"
    }
  }
);

    // Extract polyline geometry
    const polyline =
  response.data.routes[0].geometry;

res.json({
  polyline
});

  } catch (error) {
    console.error(error.response?.data || error.message);
    res.status(500).json({
      message: "Failed to fetch route"
    });
  }
};