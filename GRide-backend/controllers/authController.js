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

    const { name, roll_number, email, password, role } = req.body;

    // ✅ Validate input (UPDATED)
    if (!name || !roll_number || !email || !password) {
      return res.status(400).json({
        message: "All fields are required"
      });
    }

    const userRole = role || "USER";

    // ✅ Check if roll number OR email already exists
    const checkSql = `
      SELECT id FROM users 
      WHERE email = ? OR roll_number = ?
    `;

    db.query(checkSql, [email, roll_number], (err, result) => {

      if (err) {
        console.log("DB CHECK ERROR:", err);
        return res.status(500).json({
          message: "Database error"
        });
      }

      if (result.length > 0) {
        return res.status(409).json({
          message: "Email or Roll Number already registered"
        });
      }

      // 🔐 Hash password
      const hashedPassword = bcrypt.hashSync(password, 10);

      // ✅ Insert user
      const insertSql = `
        INSERT INTO users (name, roll_number, email, password, role)
        VALUES (?, ?, ?, ?, ?)
      `;

      db.query(
        insertSql,
        [name, roll_number, email, hashedPassword, userRole],
        (err, result) => {

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
        }
      );
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

  const { roll_number, password } = req.body;

  // Validate input
  if (!roll_number || !password) {
    return res.status(400).json({
      message: "Roll number and password are required"
    });
  }

  // Find user by roll number
  const sql = "SELECT * FROM users WHERE roll_number = ?";

  db.query(sql, [roll_number], async (err, result) => {

    if (err) {
      return res.status(500).json(err);
    }

    if (result.length === 0) {
      return res.status(401).json({
        message: "Invalid roll number or password"
      });
    }

    const user = result[0];

    // Compare password
    const isMatch = await bcrypt.compare(password, user.password);

    if (!isMatch) {
      return res.status(401).json({
        message: "Invalid roll number or password"
      });
    }

    // Generate token
const token = jwt.sign(
  {
    id: user.id,
    roll_number: user.roll_number,
    role: user.role
  },
  process.env.JWT_SECRET,
  { expiresIn: "1d" }
);

    // Success response
    res.json({
      message: "Login successful",
      token,
      user: {
        id: user.id,
        name: user.name,
        email: user.email,        // still returning email
        roll_number: user.roll_number,
        role: user.role
      }
    });
  });
};