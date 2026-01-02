const db = require("../config/db");
const bcrypt = require("bcryptjs");
const jwt = require("jsonwebtoken");

/**
 * REGISTER USER
 * -------------
 * Creates a new user with hashed password
 */
exports.register = (req, res) => {
  const { name, email, password } = req.body;

  // Validate input
  if (!name || !email || !password) {
    return res.status(400).json({
      message: "All fields are required"
    });
  }

  // Check if user already exists
  const checkSql = "SELECT id FROM users WHERE email = ?";
  db.query(checkSql, [email], (err, result) => {
    if (err) {
      return res.status(500).json(err);
    }

    if (result.length > 0) {
      return res.status(409).json({
        message: "User already exists"
      });
    }

    // Hash password
    const hashedPassword = bcrypt.hashSync(password, 10);

    // Insert new user
    const insertSql = "INSERT INTO users (name, email, password) VALUES (?, ?, ?)";

    db.query(insertSql, [name, email, hashedPassword], (err) => {
      if (err) {

        // Handle duplicate email
        if (err.code === "ER_DUP_ENTRY") {
          return res.status(409).json({
            message: "Email already registered"
          });
        }

        // Other DB errors
        return res.status(500).json({
          message: "Database error",
          error: err
        });
      }

      res.status(201).json({
        message: "User registered successfully"
      });
    });
  });
};

/**
 * LOGIN USER
 * ----------
 * Verifies email & password and returns JWT token
 */
exports.login = (req, res) => {
  const { email, password } = req.body;

  // Validate input
  if (!email || !password) {
    return res.status(400).json({
      message: "Email and password are required"
    });
  }

  // Find user by email
  const sql = "SELECT * FROM users WHERE email = ?";
  db.query(sql, [email], async (err, result) => {
    if (err) {
      return res.status(500).json(err);
    }

    if (result.length === 0) {
      return res.status(401).json({
        message: "Invalid email or password"
      });
    }

    const user = result[0];

    // Compare password using bcrypt
    const isMatch = await bcrypt.compare(password, user.password);

    if (!isMatch) {
      return res.status(401).json({
        message: "Invalid email or password"
      });
    }

    // Generate JWT token
    const token = jwt.sign(
      { id: user.id, email: user.email },
      process.env.JWT_SECRET,
      { expiresIn: "1d" }
    );

    // Login success
    res.json({
      message: "Login successful",
      token,
      user: {
        id: user.id,
        name: user.name,
        email: user.email,
        role: user.role
      }
    });
  });
};
