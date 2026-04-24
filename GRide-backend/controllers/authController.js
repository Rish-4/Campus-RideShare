const db = require("../config/db");
const bcrypt = require("bcryptjs");
const jwt = require("jsonwebtoken");

/**
 * REGISTER USER
 * -------------
 * Creates a new user with hashed password
 */
exports.register = (req, res) => {
  try {
    console.log("BODY:", req.body);

    const { name, email, password, role } = req.body;

    // Validate input
    if (!name || !email || !password) {
      return res.status(400).json({
        message: "All fields are required"
      });
    }

    const userRole = role || "USER";

    console.log("EMAIL:", email);
    console.log("ROLE:", userRole);

    // Check if user already exists
    const checkSql = "SELECT id FROM users WHERE email = ?";

    db.query(checkSql, [email], (err, result) => {
      if (err) {
        console.log("DB CHECK ERROR:", err);
        return res.status(500).json({
          message: "Database error"
        });
      }

      console.log("CHECK RESULT:", result);

      // Only block if actual match found
      if (Array.isArray(result) && result.length > 0) {
        return res.status(409).json({
          message: "Email already registered"
        });
      }

      // Hash password safely
      const hashedPassword = bcrypt.hashSync(password, 10);

      // Insert new user
      const insertSql = `
        INSERT INTO users (name, email, password, role)
        VALUES (?, ?, ?, ?)
      `;

      db.query(insertSql, [name, email, hashedPassword, userRole], (err, result) => {
        if (err) {
          console.log("INSERT ERROR:", err);

          return res.status(500).json({
            message: "Failed to register user"
          });
        }

        console.log("USER INSERTED:", result);

        return res.status(201).json({
          message: "User registered successfully"
        });
      });
    });

  } catch (error) {
    console.log("SERVER ERROR:", error);

    return res.status(500).json({
      message: "Server error"
    });
  }
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
