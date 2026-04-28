const jwt = require("jsonwebtoken");

module.exports = (req, res, next) => {

  const authHeader = req.headers["authorization"];

  if (!authHeader || !authHeader.startsWith("Bearer ")) {
    return res.status(401).json({ message: "No/Invalid auth header" });
  }

  const token = authHeader.split(" ")[1]; // 🔥 extract real token

  try {
    const decoded = jwt.verify(token, process.env.JWT_SECRET);
    req.user = decoded; // { id, roll_number, role }
    next();
  } catch (err) {
    return res.status(401).json({ message: "Invalid token" });
  }
};