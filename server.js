const geocode = require("./geocode");      // address → lat/lon
const getDistanceKm = require("./distance"); // distance logic
//const getDistanceKm = require("./getDistanceKm");

// ===========================================================
//  GoCab Backend - index.js
// ===========================================================

const express = require("express");
const sql = require("mssql");
const cors = require("cors");
require("dotenv").config();

//live location part
const http = require("http");
const { Server } = require("socket.io");

const app = express();
const server = http.createServer(app);

const io = new Server(server, {
  cors: {
    origin: "*",
    methods: ["GET", "POST"]
  }
});

//****** live location part *********
io.on("connection", (socket) => {

  console.log("User connected:", socket.id);

  // Student joins ride room
  socket.on("join_ride", (rideId) => {
    socket.join("ride_" + rideId);
    console.log("Joined ride:", rideId);
  });

  // Driver sends live location
  socket.on("driver_location", (data) => {

    const { rideId, latitude, longitude } = data;

    console.log("Driver location received:", rideId, latitude, longitude);  // ADD THIS

    io.to("ride_" + rideId).emit("update_driver_location", {
      latitude,
      longitude
    });

  });

  socket.on("disconnect", () => {
    console.log("User disconnected:", socket.id);
  });

});


//email part
const cron = require("node-cron");
//const sendRideReminder = require("./emailService");  // when we add reminder logic
const { sendRideReminder, sendWarningEmail } = require("./emailService");

// ===========================================================
//  Middlewares
// ===========================================================
app.use(cors());

// Safer JSON body parser (prevents crash on empty body)
app.use(express.json({
  verify: (req, res, buf, encoding) => {
    if (buf && buf.length === 0) {
      console.warn("⚠️ Received empty JSON body for:", req.originalUrl);
    }
  }
}));

// ===========================================================
//  Azure SQL Configuration
// ===========================================================
const dbConfig = {
  user: process.env.DB_USER,
  password: process.env.DB_PASS,
  server: process.env.DB_SERVER,
  database: process.env.DB_NAME,
  port: parseInt(process.env.DB_PORT) || 1433,
  options: {
    encrypt: true, // Required for Azure
    trustServerCertificate: false,
  },
};

console.log(" DB Config:", {
  server: process.env.DB_SERVER,
  user: process.env.DB_USER,
  database: process.env.DB_NAME,
  port: process.env.DB_PORT,
});

// ===========================================================
//  Connect to Database Once
// ===========================================================
sql.connect(dbConfig)
  .then(pool => {
    if (pool.connected) {
      console.log(" Connected to Azure SQL Database");
      // Simple test query
      pool.request().query("SELECT 1 AS number")
        .then(result => console.log(" Test query succeeded:", result.recordset))
        .catch(err => console.error(" Test query failed:", err.message));
    }
  })
  .catch(err => {
    console.error(" Database connection failed:");
    console.error("Server:", process.env.DB_SERVER);
    console.error("User:", process.env.DB_USER);
    console.error("DB:", process.env.DB_NAME);
    console.error("Error Details:", err.message);
  });

// ===========================================================
//  Root Route
// ===========================================================
app.get("/", (req, res) => {
  res.send("GoCab Backend is running ");
});
//  Temporary route to check if /api/user/register exists
app.get("/api/user/register", (req, res) => {
  res.send(" POST /api/user/register route exists!");
});

// ===========================================================
//  Fetch All Students
// ===========================================================
app.get("/students", async (req, res) => {
  try {
    const pool = await sql.connect(dbConfig);
    const result = await pool.request().query("SELECT * FROM Student");
    res.json(result.recordset);
  } catch (err) {
    console.error(" Error fetching students:", err.message);
    res.status(500).send("Error fetching students: " + err.message);
  }
});

// ===========================================================
//  Register a New User (Step 1)
// ===========================================================
app.post("/api/user/register", async (req, res) => {
  const { firebase_uid, email_id, user_type } = req.body;

  if (!firebase_uid || !email_id || !user_type) {
    console.warn(" Missing required fields in /api/user/register:", req.body);
    return res.status(400).json({ error: "Missing required fields" });
  }
  console.log(" Received user registration:", req.body);

  try {
    const pool = await sql.connect(dbConfig);

    //  Insert only into User table
    await pool.request()
      .input("firebase_uid", sql.VarChar, firebase_uid)
      .input("email_id", sql.VarChar, email_id)
      .input("user_type", sql.VarChar, user_type)
      .query(`
        INSERT INTO [User] (firebase_uid, email_id, user_type)
        VALUES (@firebase_uid, @email_id, @user_type)
      `);

    console.log(` User inserted: ${email_id} (${user_type})`);

    //  Do NOT auto-create student record now.
    // The student record will be created later in /api/student/add
    // after the user fills the personal information form.

    res.status(201).json({
      success: true,
      message: "User registered successfully. Complete personal info next."
    });

  } catch (err) {
    console.error(" Error inserting user:", err.message);
    res.status(500).json({ success: false, error: err.message });
  }
});

// ===========================================================
//  Add Student + Guardian Information Together
// ===========================================================
app.post("/api/student/add", async (req, res) => {
  const {
    firebase_uid, S_email_id, S_name, Smartcard_id,
    College_name, dateofbirth, gender, aadhar_number,
    course, branch, year, Permanent_address, hostel,
    G_name, G_phone_no, G_eid
  } = req.body;

  //  Validation for student fields
  if (!firebase_uid || !S_email_id || !S_name) {
    console.warn(" Missing required student fields:", req.body);
    return res.status(400).json({ success: false, message: "Missing required student details." });
  }

  //  Validation for guardian fields
  if (!G_name || !G_phone_no || !G_eid) {
    console.warn(" Missing required guardian fields:", req.body);
    return res.status(400).json({ success: false, message: "Missing required guardian details." });
  }

  console.log(" Received full student + guardian info:", req.body);

  try {
    const pool = await sql.connect(dbConfig);

    // 🔹 Begin a transaction so both inserts happen together
    const transaction = new sql.Transaction(pool);
    await transaction.begin();

    try {
      //  Insert Student Info
      await transaction.request()
        .input("firebase_uid", sql.VarChar, firebase_uid)
        .input("S_email_id", sql.VarChar, S_email_id)
        .input("S_name", sql.VarChar, S_name)

        .input("Smartcard_id", sql.VarChar, Smartcard_id)
        .input("College_name", sql.VarChar, College_name)
        .input("dateofbirth", sql.Date, dateofbirth)
        .input("gender", sql.VarChar, gender)
        .input("aadhar_number", sql.VarChar, aadhar_number)
        .input("course", sql.VarChar, course)
        .input("branch", sql.VarChar, branch)
        .input("year", sql.VarChar, year)
        .input("Permanent_address", sql.VarChar, Permanent_address)
        .input("hostel", sql.VarChar, hostel)
        .query(`
          INSERT INTO Student (
            firebase_uid, S_email_id, S_name, Smartcard_id,
            College_name, dateofbirth, gender, aadhar_number,
            course, branch, year, Permanent_address, hostel
          )
          VALUES (
            @firebase_uid, @S_email_id, @S_name, @Smartcard_id,
            @College_name, @dateofbirth, @gender, @aadhar_number,
            @course, @branch, @year, @Permanent_address, @hostel
          )
        `);

      //  Insert Guardian Info
      await transaction.request()
        .input("S_email_id", sql.VarChar, S_email_id)
        .input("G_name", sql.VarChar, G_name)
        .input("G_phone_no", sql.VarChar, G_phone_no)
        .input("G_eid", sql.VarChar, G_eid)
        .query(`
          INSERT INTO Parents_Information (S_email_id, G_name, G_phone_no, G_eid)
          VALUES (@S_email_id, @G_name, @G_phone_no, @G_eid)
        `);

      //  Commit transaction
      await transaction.commit();

      console.log(` Student + Guardian info added for: ${S_email_id}`);
      res.status(200).json({
        success: true,
        message: "Student and guardian information saved successfully."
      });

    } catch (err) {
      //  Rollback if anything fails
      await transaction.rollback();
      console.error(" Transaction failed:", err.message);
      res.status(500).json({
        success: false,
        message: "Failed to save student and guardian info."
      });
    }

  } catch (err) {
    console.error(" DB connection error:", err.message);
    res.status(500).json({
      success: false,
      message: "Database connection failed."
    });
  }
});
// ===========================================================
// Get User Role by Firebase UID (For Login - Others)
// ===========================================================
app.post("/api/user/getrole", async (req, res) => {
  const { firebase_uid } = req.body;

  if (!firebase_uid) {
    console.warn(" Missing firebase_uid in /api/user/getrole:", req.body);
    return res.status(400).json({ success: false, error: "firebase_uid is required" });
  }

  try {
    const pool = await sql.connect(dbConfig);
    const result = await pool.request()
      .input("firebase_uid", sql.VarChar, firebase_uid)
      .query("SELECT user_type FROM [User] WHERE firebase_uid = @firebase_uid");

    if (result.recordset.length > 0) {
      const userType = result.recordset[0].user_type;
      console.log(` Found user role for ${firebase_uid}: ${userType}`);
      return res.status(200).json({ success: true, user_type: userType });
    } else {
      console.warn(` No user found for UID: ${firebase_uid}`);
      return res.status(404).json({ success: false, error: "User not found" });
    }
  } catch (err) {
    console.error(" Error fetching user role:", err.message);
    return res.status(500).json({ success: false, error: "Database query failed" });
  }
});

//index.js
app.get("/api/maintenance/students", async (req, res) => {
  try {
    const pool = await sql.connect(dbConfig);

    const result = await pool.request().query(`
      SELECT
        s.S_name        AS name,
        s.S_email_id   AS email,
        s.College_name AS collegeName
      FROM [User] u
      JOIN Student s
        ON u.firebase_uid = s.firebase_uid
      WHERE u.user_type = 'student'
      ORDER BY s.S_name
    `);

    res.status(200).json({
      success: true,
      students: result.recordset
    });

  } catch (err) {
    console.error(" Error fetching students:", err.message);
    res.status(500).json({
      success: false,
      message: "Failed to fetch students"
    });
  }
});
// ===========================================================
//  Maintenance: Fetch All Drivers
// ===========================================================
app.get("/api/maintenance/drivers", async (req, res) => {
  try {
    const pool = await sql.connect(dbConfig);

    const result = await pool.request().query(`
      SELECT
        d.D_name        AS name,
        d.D_eid   AS email,
        d.D_licence_no AS licenceNumber
      FROM [User] u
      JOIN Driver d
        ON u.firebase_uid = d.firebase_uid
      WHERE u.user_type = 'driver'
    `);

    res.status(200).json({
      success: true,
      drivers: result.recordset
    });

  } catch (err) {
    console.error(" Error fetching drivers:", err.message);
    res.status(500).json({
      success: false,
      message: "Failed to fetch drivers"
    });
  }
});

// ===========================================================
//       GET STUDENT DETAILS BY FIREBASE UID
// ===========================================================

app.get("/getStudentDetails/:firebase_uid", async (req, res) => {
  const { firebase_uid } = req.params;

  try {
    //  FIX: pool yahin create karo
    const pool = await sql.connect(dbConfig);

    const result = await pool
      .request()
      .input("firebase_uid", sql.VarChar, firebase_uid)
      .query(`
        SELECT
          s.firebase_uid,
          s.S_email_id,
          s.S_name,
          s.Smartcard_id,
          s.College_name,
          s.dateofbirth,
          s.gender,
          s.aadhar_number,
          s.course,
          s.branch,
          s.year,
          s.Permanent_address,
          s.hostel,
          p.G_name      AS guardian_name,
          p.G_phone_no AS guardian_phone,
          p.G_eid      AS guardian_email
        FROM Student s
        LEFT JOIN Parents_Information p
          ON s.S_email_id = p.S_email_id
        WHERE s.firebase_uid = @firebase_uid
      `);

    if (result.recordset.length === 0) {
      return res.status(404).json({
        success: false,
        message: "Student not found"
      });
    }

    res.json({
      success: true,
      data: result.recordset[0]
    });

  } catch (err) {
    console.error("Error fetching student details:", err.message);
    res.status(500).json({
      success: false,
      message: err.message
    });
  }
});


// ===========================================================
//  Get Student + Guardian Details by Firebase UID
// ==============================================================
app.get("/getStudentDetails/:firebase_uid", async (req, res) => {
  const { firebase_uid } = req.params;

  if (!firebase_uid) {
    return res.status(400).json({ success: false, message: "firebase_uid is required" });
  }

  try {
    const pool = await sql.connect(dbConfig);
    const result = await pool.request()
      .input("firebase_uid", sql.VarChar, firebase_uid)
      .query(`
        SELECT
          s.firebase_uid, s.S_email_id, s.S_name, s.Smartcard_id,
          s.College_name, s.dateofbirth, s.gender, s.aadhar_number,
          s.course, s.branch, s.year, s.Permanent_address, s.hostel, s.created_at,
          p.G_name AS guardian_name, p.G_phone_no AS guardian_phone, p.G_eid AS guardian_email
        FROM Student s
        LEFT JOIN Parents_Information p ON s.S_email_id = p.S_email_id
        WHERE s.firebase_uid = @firebase_uid
      `);

    if (result.recordset.length === 0) {
      return res.status(404).json({ success: false, message: "Student not found" });
    }

    // If there are multiple parent rows, this returns the first one.
    const row = result.recordset[0];
    res.json({ success: true, data: row });

  } catch (err) {
    console.error(" Error in /getStudentDetails:", err.message);
    res.status(500).json({ success: false, message: "Server error" });
  }
});
///////////////////////////////////////////////////////
app.put("/updateStudentProfile", async (req, res) => {
  const {
    firebase_uid,
    course,
    branch,
    year,
    Permanent_address,
    hostel,
    G_name,
    G_phone_no,
    G_eid
  } = req.body;

  if (!firebase_uid) {
    return res.status(400).json({ success: false, message: "firebase_uid required" });
  }

  const pool = await sql.connect(dbConfig);
  const tx = new sql.Transaction(pool);

  try {
    await tx.begin();

    //  UPDATE STUDENT (sirf selected fields)
    await tx.request()
      .input("firebase_uid", sql.VarChar, firebase_uid)
      .input("course", sql.VarChar, course)
      .input("branch", sql.VarChar, branch)
      .input("year", sql.VarChar, year)
      .input("Permanent_address", sql.VarChar, Permanent_address)
      .input("hostel", sql.VarChar, hostel)
      .query(`
        UPDATE Student SET
          course = @course,
          branch = @branch,
          year = @year,
          Permanent_address = @Permanent_address,
          hostel = @hostel
        WHERE firebase_uid = @firebase_uid
      `);

    // 2 student email (guardian ke liye)
    const emailRes = await tx.request()
      .input("firebase_uid", sql.VarChar, firebase_uid)
      .query(`SELECT S_email_id FROM Student WHERE firebase_uid=@firebase_uid`);

    const S_email_id = emailRes.recordset[0]?.S_email_id;
    if (!S_email_id) throw new Error("Student not found");

    //  UPSERT GUARDIAN
    const guardianUpdate = await tx.request()
      .input("S_email_id", sql.VarChar, S_email_id)
      .input("G_name", sql.VarChar, G_name)
      .input("G_phone_no", sql.VarChar, G_phone_no)
      .input("G_eid", sql.VarChar, G_eid)
      .query(`
        UPDATE Parents_Information
        SET
          G_name=@G_name,
          G_phone_no=@G_phone_no,
          G_eid=@G_eid
        WHERE S_email_id=@S_email_id
      `);

    if (guardianUpdate.rowsAffected[0] === 0) {
      await tx.request()
        .input("S_email_id", sql.VarChar, S_email_id)
        .input("G_name", sql.VarChar, G_name)
        .input("G_phone_no", sql.VarChar, G_phone_no)
        .input("G_eid", sql.VarChar, G_eid)
        .query(`
          INSERT INTO Parents_Information
          (S_email_id, G_name, G_phone_no, G_eid)
          VALUES (@S_email_id, @G_name, @G_phone_no, @G_eid)
        `);
    }

    await tx.commit();
    res.json({ success: true, message: "Profile updated successfully" });

  } catch (e) {
    await tx.rollback();
    res.status(500).json({ success: false, message: e.message });
  }
});


// ===========================================================
//  Global Error Handler for Invalid JSON
// ===========================================================
app.use((err, req, res, next) => {
  if (err instanceof SyntaxError && "body" in err) {
    console.error(" Invalid JSON:", err.message);
    return res.status(400).json({ success: false, error: "Invalid JSON body" });
  }
  next();
});

// ===========================================================
//  Start Server (Fixed Route Logging)
// ===========================================================
const PORT = process.env.PORT || 5000;

server.listen(PORT, () => {
  console.log(`Server running on port ${PORT}`);
});

// Wait a short moment for Express to register all routes
setTimeout(() => {
  if (app._router && app._router.stack) {
    console.log(" Registered Routes:");
    app._router.stack
      .filter(r => r.route)
      .forEach(r => {
        const method = Object.keys(r.route.methods)[0].toUpperCase();
        console.log(`${method} ${r.route.path}`);
      });
  } else {
    console.warn(" No routes found (router not initialized yet).");
  }
}, 200); // 200ms delay ensures routes exist



//=========================================================
// Driver and car info is added to database
//==================================================
app.post("/api/driver/accept-join", async (req, res) => {

  const { requestId } = req.body;

  try {
    const pool = await sql.connect(dbConfig);

    await pool.request()
      .input("requestId", sql.Int, requestId)
      .query(`
        UPDATE RideStudent
        SET Ride_status = 'Accepted'
        WHERE id = @requestId
        AND Ride_status = 'Pending'
      `);

    res.json({
      success: true,
      message: "Join request accepted"
    });

  } catch (error) {
    res.status(500).json({ error: error.message });
  }
});
app.post("/api/driver/add-with-car", async (req, res) => {
  const {
    // DRIVER
    firebase_uid, D_eid, D_name, D_aadhar_no,
    D_phone_no, D_address, D_licence_no,
    D_status, D_avg_rating, D_gender, cost_per_km, current_city, D_dob,


    // CAR
    C_id, C_name, C_number, C_colour,
    C_model, C_ac_nac, C_seater, C_carrier
  } = req.body;

  //  DRIVER VALIDATION
  if (!firebase_uid || !D_eid || !D_name || !D_phone_no || !D_aadhar_no || !D_licence_no) {
    return res.status(400).json({ success: false, message: "Missing driver details" });
  }

  //  CAR VALIDATION
  if (!C_id || !C_name || !C_number || !C_model || !C_colour) {
    return res.status(400).json({ success: false, message: "Missing car details" });
  }

  try {
    const pool = await sql.connect(dbConfig);
    const transaction = new sql.Transaction(pool);
    await transaction.begin();

    try {
      // 🔹 INSERT DRIVER
      await transaction.request()
        .input("firebase_uid", sql.VarChar, firebase_uid)
        .input("D_eid", sql.VarChar, D_eid)
        .input("D_name", sql.VarChar, D_name)

        .input("D_aadhar_no", sql.VarChar, D_aadhar_no)
        .input("D_phone_no", sql.VarChar, D_phone_no)
        .input("D_address", sql.VarChar, D_address)
        .input("D_licence_no", sql.VarChar, D_licence_no)
        .input("D_status", sql.VarChar, D_status)
        .input("D_gender", sql.VarChar, D_gender)
        .input("cost_per_km", sql.Decimal(6, 2), cost_per_km)
        .input("current_city", sql.VarChar, current_city || "Unknown")
        .input("D_dob", sql.Date, D_dob)

        .query(`
          INSERT INTO Driver (
            firebase_uid, D_eid, D_name, D_aadhar_no,
            D_phone_no, D_address, D_licence_no,
            D_status, D_gender, cost_per_km, current_city,D_dob

          )
          VALUES (
            @firebase_uid, @D_eid, @D_name, @D_aadhar_no,
            @D_phone_no, @D_address, @D_licence_no,
            @D_status, @D_gender, @cost_per_km, @current_city,@D_dob
          )
        `);

      // 🔹 INSERT CAR
      await transaction.request()
        .input("C_id", sql.VarChar, C_id)
        .input("C_name", sql.VarChar, C_name)
        .input("C_number", sql.VarChar, C_number)
        .input("D_eid", sql.VarChar, D_eid)
        .input("C_colour", sql.VarChar, C_colour)
        .input("C_model", sql.VarChar, C_model)
        .input("C_ac_nac", sql.VarChar, C_ac_nac)
        .input("C_seater", sql.Int, C_seater)
        .input("C_carrier", sql.VarChar, C_carrier)
        .query(`
          INSERT INTO Car (
            C_id, C_name, C_number, D_eid,
            C_colour, C_model, C_ac_nac, C_seater, C_carrier
          )
          VALUES (
            @C_id, @C_name, @C_number, @D_eid,
            @C_colour, @C_model, @C_ac_nac, @C_seater, @C_carrier
          )
        `);

      await transaction.commit();

      return res.json({
        success: true,
        message: "Driver and Car saved successfully"
      });

    } catch (err) {
      await transaction.rollback();
      console.error(" Transaction failed:", err);
      return res.status(500).json({ success: false, message: err.message });
    }

  } catch (err) {
    console.error(" DB error:", err);
    return res.status(500).json({ success: false, message: "DB connection error" });
  }
});
// ===========================================================
//  DRIVER ROUTES
// ===========================================================

// Fetch Driver Profile
app.get("/driver/profile/:firebase_uid", async (req, res) => {
  try {
    const { firebase_uid } = req.params;
    const pool = await sql.connect(dbConfig);

    const result = await pool
      .request()
      .input("firebase_uid", sql.VarChar, firebase_uid)
      .query("SELECT * FROM Driver WHERE firebase_uid = @firebase_uid");

    if (result.recordset.length === 0) {
      return res.status(404).json({ success: false, message: "Driver not found" });
    }

    res.json({ success: true, data: result.recordset[0] });
  } catch (error) {
    console.error(" Error fetching driver:", error);
    res.status(500).json({ success: false, message: "Server error" });
  }
});

//====================================================================
// Update Driver + Car Profile in azure also
//====================================================================

app.put("/driver/profile/:uid", async (req, res) => {
  const { uid } = req.params;
  const {
    D_phone_no,
    D_address,
    D_status,
    cost_per_km,
    current_city,

    // car fields
    C_name,
    C_number,
    C_colour,
    C_model,
    C_ac_nac,
    C_seater,
    C_carrier
  } = req.body;

  try {
    //  Get D_eid from Driver
    const driverRes = await sql.query`
      SELECT D_eid FROM Driver WHERE firebase_uid = ${uid}
    `;

    if (driverRes.recordset.length === 0) {
      return res.json({ success: false, message: "Driver not found" });
    }

    const D_eid = driverRes.recordset[0].D_eid;

    //  Update Driver table
    await sql.query`
      UPDATE Driver SET
        D_phone_no = ${D_phone_no},
        D_address = ${D_address},
        D_status = ${D_status},
        cost_per_km = ${cost_per_km},
        current_city = ${current_city}
      WHERE firebase_uid = ${uid}
    `;

    //  Check if Car exists
    const carRes = await sql.query`
      SELECT COUNT(*) AS cnt FROM Car WHERE D_eid = ${D_eid}
    `;

    const carExists = carRes.recordset[0].cnt > 0;

    if (carExists) {
      //  UPDATE Car
      await sql.query`
        UPDATE Car SET
          C_name = ${C_name},
          C_number = ${C_number},
          C_colour = ${C_colour},
          C_model = ${C_model},
          C_ac_nac = ${C_ac_nac},
          C_seater = ${C_seater},
          C_carrier = ${C_carrier}
        WHERE D_eid = ${D_eid}
      `;
    } else {
      //  INSERT Car ( THIS WAS MISSING)
      await sql.query`
        INSERT INTO Car (
          C_name, C_number, C_colour, C_model,
          C_ac_nac, C_seater, C_carrier, D_eid
        )
        VALUES (
          ${C_name}, ${C_number}, ${C_colour}, ${C_model},
          ${C_ac_nac}, ${C_seater}, ${C_carrier}, ${D_eid}
        )
      `;
    }

    res.json({ success: true, message: "Profile updated successfully" });

  } catch (err) {
    res.status(500).json({ success: false, message: err.message });
  }
});
app.get("/driver/profile/full/:uid", async (req, res) => {
  const { uid } = req.params;

  try {
    const result = await sql.query`
      SELECT
        d.firebase_uid,
        d.D_eid,
        d.D_name,
        d.D_aadhar_no, 
        d.D_phone_no,
        d.D_address,
        d.D_licence_no,
        d.D_status,
        d.D_avg_rating,
        d.D_gender,
        d.cost_per_km,
        d.current_city,
        d.D_dob,
        c.C_name,
        c.C_number,
        c.C_colour,
        c.C_model,
        c.C_ac_nac,
        c.C_seater,
        c.C_carrier
      FROM Driver d
      LEFT JOIN Car c ON d.D_eid = c.D_eid
      WHERE d.firebase_uid = ${uid}
    `;

    if (result.recordset.length === 0) {
      return res.json({ success: false, message: "Driver not found" });
    }

    const row = result.recordset[0];

    res.json({
      success: true,
      data: {
        firebase_uid: row.firebase_uid,
        D_eid: row.D_eid,
        D_name: row.D_name,
        D_aadhar_no: row.D_aadhar_no,
        D_phone_no: row.D_phone_no,
        D_address: row.D_address,
        D_licence_no: row.D_licence_no,
        D_status: row.D_status,
        D_avg_rating: row.D_avg_rating,
        D_gender: row.D_gender,
        cost_per_km: row.cost_per_km,
        current_city: row.current_city,

        //  NESTED CAR OBJECT (THIS FIXES EVERYTHING)
        car: row.C_name ? {
          C_name: row.C_name,
          C_number: row.C_number,
          C_colour: row.C_colour,
          C_model: row.C_model,
          C_ac_nac: row.C_ac_nac,
          C_seater: row.C_seater,
          C_carrier: row.C_carrier
        } : null
      }
    });

  } catch (err) {
    res.status(500).json({ success: false, message: err.message });
  }
});


// ===========================================================
// GET MaintenanceTeam Profile
// ===========================================================
app.get("/maintenance/profile/:uid", async (req, res) => {
  const { uid } = req.params;

  try {
    const result = await sql.query`
      SELECT firebase_uid, MT_email, created_at
      FROM MaintenanceTeam
      WHERE firebase_uid = ${uid}
    `;

    if (result.recordset.length === 0) {
      return res.json({
        success: false,
        message: "Maintenance member not found"
      });
    }

    res.json({
      success: true,
      data: result.recordset[0]
    });
  } catch (err) {
    console.error(err);
    res.status(500).json({
      success: false,
      message: "Server error"
    });
  }
});

const poolPromise = sql.connect(dbConfig);

// app.post("/api/search-rides-v2", async (req, res) => {
//   console.log(" Incoming request body:", req.body);

//   try {
//     const { pickup, drop } = req.body;

//     if (!pickup || !drop) {
//       return res.status(400).json({
//         success: false,
//         message: "Pickup and drop required"
//       });
//     }

//     //  Geocode pickup & drop (FULL address)
//     const start = await geocode(pickup);
//     const end = await geocode(drop);

//     if (!start || !end) {
//       return res.status(400).json({
//         success: false,
//         message: "Location not found"
//       });
//     }

//     //  Calculate road distance (place-to-place)
//     const distanceKm = await getDistanceKm(start, end);
//     const pool = await poolPromise;
//     //  DATABASE CONNECTION
//     /*const pool = await sql.connect(dbConfig);*/

//     // Get all UNIQUE available driver cities
//     const cityResult = await pool.request().query(`
//       SELECT DISTINCT current_city
//       FROM Driver
//       WHERE D_status = 'Available'
//     `);

//     const cities = cityResult.recordset.map(
//       row => row.current_city.trim()
//     );

//     //  Match pickup text with driver city (CASE-INSENSITIVE)
//     let matchedCity = null;

//     for (const city of cities) {
//       if (pickup.toLowerCase().includes(city.toLowerCase())) {
//         matchedCity = city;
//         break;
//       }
//     }

//     //  If no city matches → return empty drivers
//     if (!matchedCity) {
//       console.log(" No matching city found for pickup:", pickup);
//       return res.json({
//         success: true,
//         pickup,
//         drop,
//         drivers: []
//       });
//     }

//     //  Fetch drivers for matched city
//     const driversResult = await pool.request()
//       .input("pickupCity", sql.NVarChar(50), matchedCity)
//       .query(`
//         SELECT
//         D.D_eid,
//         D.D_name,
//         D.current_city,
//         D.cost_per_km,
//         C.C_id,
//         COUNT(V.college_id) AS verifiedCount
//     FROM Driver D
//     JOIN Car C ON D.D_eid = C.D_eid
//     LEFT JOIN DriverVerification V
//     ON D.D_eid = V.D_eid
//     AND V.status = 'Verified'
//     WHERE D.D_status = 'Available'
//       AND D.current_city COLLATE Latin1_General_CI_AS
//           = @pickupCity COLLATE Latin1_General_CI_AS
//           GROUP BY
//     D.D_eid,
//     D.D_name,
//     D.current_city,
//     D.cost_per_km,
//     C.C_id
//       `);

//     //  Fare calculation
//     const drivers = driversResult.recordset.map(driver => ({
//       id: driver.D_eid,
//       name: driver.D_name,
//       city: driver.current_city,
//       distanceKm: Number(distanceKm.toFixed(2)),
//       fare: Number((distanceKm * driver.cost_per_km).toFixed(2)),
//       carId: driver.C_id,
//       verifiedCount: driver.verifiedCount || 0   // 🔥 now this exists
//     }));
// console.log("FINAL DRIVERS:", drivers);
//     console.log(" Sending drivers:", drivers);

//     //  Response
//     res.json({
//       success: true,
//       pickup,
//       drop,
//       drivers
//     });

//   } catch (err) {
//     console.error(" Search ride error:", err);
//     res.status(500).json({
//       success: false,
//       error: err.message
//     });
//   }
// });

app.post("/api/search-rides-v2", async (req, res) => {
  console.log(" Incoming request body:", req.body);

  try {
    const {
      pickup,
      drop,
      rating = null,
      costOrder = null,
      acType = null,
      seats = null,
      carType = null
    } = req.body;

    if (!pickup || !drop) {
      return res.status(400).json({
        success: false,
        message: "Pickup and drop required"
      });
    }

    // 🔹 Geocode pickup & drop
    const start = await geocode(pickup);
    const end = await geocode(drop);

    if (!start || !end) {
      return res.status(400).json({
        success: false,
        message: "Location not found"
      });
    }

    // 🔹 Distance
    const distanceKm = await getDistanceKm(start, end);
    const pool = await poolPromise;

    // 🔹 Get all available cities
    const cityResult = await pool.request().query(`
      SELECT DISTINCT current_city
      FROM Driver
      WHERE D_status = 'Active'
    `);

    const cities = cityResult.recordset.map(
      row => row.current_city.trim()
    );

    // =========================
    //  IMPROVED CITY MATCHING
    // =========================

    const normalizeText = (str) =>
      str.toLowerCase().replace(/[^a-z0-9 ]/g, "").trim();

    const pickupNormalized = normalizeText(pickup);
    const pickupWords = pickupNormalized.split(/\s+/);

    console.log("Pickup words:", pickupWords);
    console.log("Cities from DB:", cities);

    let matchedCity = null;

    for (const city of cities) {
      const cityNormalized = normalizeText(city);

      if (pickupWords.includes(cityNormalized)) {
        matchedCity = city;
        break;
      }
    }

    //  No match
    if (!matchedCity) {
      console.log(" No matching city found for pickup:", pickup);
      return res.json({
        success: true,
        pickup,
        drop,
        drivers: []
      });
    }

    console.log(" Matched City:", matchedCity);

    // =========================
    //  FETCH DRIVERS
    // =========================

    const driversResult = await pool.request()
      .input("pickupCity", sql.NVarChar(50), matchedCity)
      .query(`
        SELECT 
          D.D_eid,
          D.D_name,
          D.current_city,
          D.cost_per_km,
          D.D_avg_rating,
          C.C_id,
          C.C_ac_nac,
          C.C_seater,
          C.C_carrier,     
          COUNT(V.college_id) AS verifiedCount
        FROM Driver D
        JOIN Car C ON D.D_eid = C.D_eid
        LEFT JOIN DriverVerification V
          ON D.D_eid = V.D_eid
          AND V.status = 'Verified'
        WHERE D.D_status = 'Active'
          AND D.current_city COLLATE Latin1_General_CI_AS
              = @pickupCity COLLATE Latin1_General_CI_AS
        GROUP BY 
          D.D_eid,
          D.D_name,
          D.current_city,
          D.cost_per_km,
          D.D_avg_rating,
          C.C_id,
          C.C_ac_nac,
          C.C_seater,
          C.C_carrier
      `);

    let drivers = driversResult.recordset.map(driver => ({
      id: driver.D_eid,
      name: driver.D_name,
      city: driver.current_city,
      distanceKm: Number(distanceKm.toFixed(2)),
      fare: Number((distanceKm * driver.cost_per_km).toFixed(2)),
      carId: driver.C_id,
      verifiedCount: driver.verifiedCount || 0,
      rating: driver.D_avg_rating !== null
        ? parseFloat(driver.D_avg_rating)
        : 0,
      acType: driver.C_ac_nac,
      seats: driver.C_seater,
      carType: driver.C_carrier
    }));

    console.log("Incoming filters:", { rating, costOrder, acType, seats, carType });

    // =========================
    //  FILTERS
    // =========================

    const normalize = (val) => val?.toString().trim().toLowerCase();

    //  AC FILTER
    if (acType) {
      drivers = drivers.filter(d =>
        normalize(d.acType) === normalize(acType)
      );
    }

    //  SEATS FILTER
    if (seats !== null && seats !== undefined) {
      drivers = drivers.filter(d =>
        Number(d.seats) === Number(seats)
      );
    }

    //  CAR TYPE FILTER
    if (carType) {
      drivers = drivers.filter(d => {
        const dbVal = normalize(d.carType);

        if (carType === "Carrier") return dbVal === "yes";
        if (carType === "Non-Carrier") return dbVal === "no";

        return true;
      });
    }

    //  RATING FILTER
    if (rating !== null && rating !== undefined && rating !== "") {
      drivers = drivers.filter(d => {
        if (rating === "Best") return d.rating >= 4.0;
        if (rating === "Average") return d.rating >= 2.5 && d.rating < 4.0;
        if (rating === "Low") return d.rating < 2.5;
        return true;
      });
    }

    //  SORT
    if (costOrder === "LowToHigh") {
      drivers.sort((a, b) => a.fare - b.fare);
    } else if (costOrder === "HighToLow") {
      drivers.sort((a, b) => b.fare - a.fare);
    }

    console.log(" FINAL DRIVERS:", drivers);

    // =========================
    // 🔹 RESPONSE
    // =========================

    res.json({
      success: true,
      pickup,
      drop,
      drivers
    });

  } catch (err) {
    console.error(" Search ride error:", err);
    res.status(500).json({
      success: false,
      error: err.message
    });
  }
});

//View Driver (Administration)
app.get("/admin/drivers", async (req, res) => {

  try {

    const pool = await sql.connect(dbConfig);
    const search = req.query.search || "";

    const result = await pool.request()
      .input("search", sql.VarChar, `%${search}%`)
      .query(`
        SELECT
          D.D_name AS driver_name,
          D.D_licence_no AS licence_no,
          D.D_eid AS email
        FROM Driver D
        LEFT JOIN DriverVerification DV
          ON D.D_eid = DV.D_eid
        WHERE
          DV.status IS NULL
          AND (
            D.D_name LIKE @search
            OR D.D_licence_no LIKE @search
          )
        ORDER BY D.D_name
      `);

    res.json({
      success: true,
      drivers: result.recordset
    });

  } catch (err) {

    console.error("Driver fetch error:", err);

    res.status(500).json({
      success: false,
      error: err.message
    });

  }

});

//CONFIRM REQUEST(DRIVER INFO)
app.get("/api/drivers", async (req, res) => {
  try {
    const pool = await sql.connect(dbConfig);

    const result = await pool.request().query(`
      SELECT
        d.D_eid AS driverEmail,
        d.D_name AS name,
        c.C_id AS carId
      FROM Driver d
      JOIN Car c ON d.D_eid = c.D_eid
    `);

    res.json(result.recordset);

  } catch (err) {
    console.error("Driver fetch error:", err);
    res.status(500).json({
      success: false,
      error: err.message
    });
  }
});

// ===========================================================
//  ADMIN: STUDENT LIST (DOMAIN + SEARCH)
// ===========================================================
app.get("/admin/students", async (req, res) => {
  let { domain, search } = req.query;

  if (!domain || domain.trim() === "") {
    return res.json({ success: true, students: [] });
  }

  domain = domain.trim().toLowerCase();

  // admin@banasthali.in → banasthali.in
  if (domain.includes("@")) {
    domain = domain.split("@").pop();
  }

  // Search can be name or smartcard id
  search = search ? search.trim() : "";

  try {
    const pool = await sql.connect(dbConfig);

    let query = `
      SELECT
        Smartcard_id AS student_id,
        S_name AS student_name,
        College_name AS college_name
      FROM Student
      WHERE LOWER(
        SUBSTRING(S_email_id, CHARINDEX('@', S_email_id) + 1, LEN(S_email_id))
      ) = @domain
    `;

    // If search is typed, filter by name OR ID
    if (search !== "") {
      query += `
        AND (
          S_name LIKE '%' + @search + '%'
          OR Smartcard_id LIKE '%' + @search + '%'
        )
      `;
    }

    query += ` ORDER BY S_name`;

    const result = await pool.request()
      .input("domain", sql.VarChar, domain)
      .input("search", sql.VarChar, search)
      .query(query);

    res.json({
      success: true,
      students: result.recordset
    });

  } catch (err) {
    console.error(" ERROR:", err);
    res.status(500).json({
      success: false,
      error: err.message
    });
  }
});


app.post("/api/ride/request", async (req, res) => {
  try {

    console.log("Incoming Ride Data:", req.body);

    const {
      driverEmail,
      carId,
      studentEmail,
      pickup,
      drop,
      distanceKm,
      fare,
      time,
      date,
      pickupCity,
      dropCity
    } = req.body;

    // Validation
    if (!driverEmail || !carId || !studentEmail || !pickup || !drop || !time) {
      return res.status(400).json({
        success: false,
        message: "Missing required fields"
      });
    }

    const pool = await sql.connect(dbConfig);

    // ✅ Fix Timezone Issue (Send as string, not Date)
    const formattedTime = time.length === 5 ? time + ":00" : time;

    // =========================
    // STEP 1: Insert into Ride
    // =========================
    const rideResult = await pool.request()
      .input("R_timing", sql.VarChar(8), formattedTime) // ✅ FIXED
      .input("D_eid", sql.VarChar(100), driverEmail)
      .input("C_id", sql.UniqueIdentifier, carId)
      .input("initial_loc", sql.VarChar(100), pickup)
      .input("final_loc", sql.VarChar(100), drop)
      .input("R_date", sql.VarChar(10), date)
      .input("R_status", sql.VarChar(50), "Pending")
      .input("distance_km", sql.Float, distanceKm)
      .input("fare_amount", sql.Decimal(10, 2), fare)
      .input("S_id", sql.VarChar(100), studentEmail)
      .input("pickupCity", sql.VarChar(100), pickupCity)
      .input("dropCity", sql.VarChar(100), dropCity)
      .query(`
                INSERT INTO Ride (
                    R_timing,
                    D_eid,
                    C_id,
                    initial_loc,
                    final_loc,
                    R_date,
                    R_status,
                    distance_km,
                    fare_amount,
                    S_id,
                    pickup_city,
                    drop_city
                )
                OUTPUT INSERTED.R_id
                VALUES (
                    @R_timing,
                    @D_eid,
                    @C_id,
                    @initial_loc,
                    @final_loc,
                    @R_date,
                    @R_status,
                    @distance_km,
                    @fare_amount,
                    @S_id,
                    @pickupCity,
                    @dropCity
                )
            `);

    const newRideId = rideResult.recordset[0].R_id;

    // =================================
    // STEP 2: Insert into RideStudent
    // =================================
    await pool.request()
      .input("R_id", sql.Int, newRideId)
      .input("S_email_id", sql.VarChar(100), studentEmail)
      .input("Pickup_loc", sql.VarChar(100), pickup)
      .input("Drop_loc", sql.VarChar(100), drop)
      .input("R_pay_status", sql.VarChar(50), "Unpaid")
      .input("R_date", sql.VarChar, date)
      .input("pickupCity", sql.VarChar(100), pickupCity)
      .input("dropCity", sql.VarChar(100), dropCity)

      .query(`
                INSERT INTO RideStudent (
                    R_id,
                    S_email_id,
                    Pickup_loc,
                    Drop_loc,
                    pickup_city,
                    drop_city,
                    R_pay_status,
                    R_date
                )
                VALUES (
                    @R_id,
                    @S_email_id,
                    @Pickup_loc,
                    @Drop_loc,
                    @pickupCity,
                    @dropCity,
                    @R_pay_status,
                    @R_date
                )
            `);

    res.status(200).json({
      success: true,
      message: "Ride created successfully",
      rideId: newRideId
    });

  } catch (error) {
    console.error("Insert Error:", error);

    res.status(500).json({
      success: false,
      message: "Database insert failed",
      error: error.message
    });
  }
});

app.post("/api/ride/search-existing", async (req, res) => {

  try {

    console.log("REQUEST BODY:", req.body);

    // const { pickupCity, dropCity, date } = req.body;
    const { pickupCity, dropCity, date, collegeOnly = false, collegeName = "" } = req.body;

    const pool = await sql.connect(dbConfig);

    const result = await pool.request()
      .input("pickupCity", sql.VarChar, pickupCity)
      .input("dropCity", sql.VarChar, dropCity)
      .input("date", sql.Date, date)
      .input("collegeOnly", sql.Bit, collegeOnly ? 1 : 0)   // ✅ ADD
      .input("collegeName", sql.VarChar, collegeName)       // ✅ ADD

      .query(`
        SELECT 
    R.R_id,
    R.pickup_city,
    R.drop_city,
    R.initial_loc,
    R.final_loc,
    R.R_date,
    R.R_timing,
    R.distance_km,
    R.fare_amount,
    D.D_name,

    COUNT(V.college_id) AS verifiedCount,

    C.C_seater - COUNT(CASE 
    WHEN RS.Ride_status = 'Accepted' THEN RS.id 
END) AS seats_left,

    CASE 
    WHEN COUNT(CASE WHEN RS.Ride_status = 'Accepted' THEN RS.id END) = 0 
    THEN R.fare_amount
    ELSE R.fare_amount / 
        (COUNT(CASE WHEN RS.Ride_status = 'Accepted' THEN RS.id END) + 1)
END AS fare_per_student,

    STRING_AGG(
    CASE WHEN RS.Ride_status = 'Accepted' THEN S.S_name END, ', '
) AS students_joined,

STRING_AGG(
    CASE WHEN RS.Ride_status = 'Accepted' THEN S.College_name END, ', '
) AS colleges,

STRING_AGG(
    CASE WHEN RS.Ride_status = 'Accepted' THEN S.course END, ', '
) AS courses,

STRING_AGG(
    CASE WHEN RS.Ride_status = 'Accepted' THEN S.branch END, ', '
) AS branches,

STRING_AGG(
    CASE WHEN RS.Ride_status = 'Accepted' THEN S.year END, ', '
) AS years

    FROM Ride R

    JOIN Driver D 
    ON R.D_eid = D.D_eid

    JOIN Car C
    ON R.C_id = C.C_id

    LEFT JOIN RideStudent RS
    ON R.R_id = RS.R_id

    LEFT JOIN Student S
    ON RS.S_email_id = S.S_email_id

    LEFT JOIN DriverVerification V
    ON D.D_eid = V.D_eid
    AND V.status = 'Verified'

    WHERE
    R.R_status = 'Accepted'
    AND R.R_date = @date
    AND LOWER(R.pickup_city) = LOWER(@pickupCity)
    AND LOWER(R.drop_city) = LOWER(@dropCity)

    GROUP BY
    R.R_id,
    R.pickup_city,
    R.drop_city,
    R.initial_loc,
    R.final_loc,
    R.R_date,
    R.R_timing,
    R.distance_km,
    R.fare_amount,
    D.D_name,
    C.C_seater

    HAVING 
    C.C_seater - COUNT(CASE 
    WHEN RS.Ride_status = 'Accepted' THEN RS.id 
END) > 0
    AND (
        @collegeOnly = 0
        OR SUM(CASE 
            WHEN S.College_name = @collegeName THEN 1 
            ELSE 0 
        END) > 0
    )
      `);

    console.log("RIDES WITH SEATS:", result.recordset);

    res.json({
      success: true,
      rides: result.recordset
    });

  } catch (err) {

    console.error(err);

    res.status(500).json({
      success: false,
      message: err.message
    });

  }
});


// ===========================================================
// GET STUDENT UPCOMING RIDES
// ===========================================================

app.get("/api/student/upcoming-rides/:studentEmail", async (req, res) => {
  try {

    const { studentEmail } = req.params;
    const pool = await sql.connect(dbConfig);

    const result = await pool.request()
      .input("studentEmail", sql.VarChar, studentEmail)
      .query(`

        SELECT
            RS.R_id,
            RS.Pickup_loc,
            RS.Drop_loc,
            RS.pickup_city,
            RS.drop_city,
            RS.R_date,
            RS.Ride_status,

            D.D_name,

            COUNT(DISTINCT V.college_id) AS verifiedCount

        FROM RideStudent RS

        JOIN Ride R
            ON RS.R_id = R.R_id

        JOIN Driver D
            ON R.D_eid = D.D_eid

        LEFT JOIN DriverVerification V
            ON D.D_eid = V.D_eid
            AND LOWER(V.status) = 'verified'

        WHERE
            RS.S_email_id = @studentEmail
            AND RS.Ride_status IN ('Accepted', 'Started')
            AND RS.R_date >= CAST(GETDATE() AS DATE)

        GROUP BY
            RS.R_id,
            RS.Pickup_loc,
            RS.Drop_loc,
            RS.pickup_city,
            RS.drop_city,
            RS.R_date,
            RS.Ride_status,
            D.D_name

        ORDER BY RS.R_date ASC
      `);

    res.json({
      success: true,
      rides: result.recordset
    });

  } catch (err) {

    console.error("Upcoming rides error:", err);

    res.status(500).json({
      success: false,
      message: err.message
    });
  }
});


app.get("/api/ride/full-details/:rideId", async (req, res) => {
  try {
    const { rideId } = req.params;
    const pool = await sql.connect(dbConfig);

    // 🔹 1. Ride + Driver + Car
    const rideResult = await pool.request()
      .input("rideId", sql.Int, rideId)
      .query(`
        SELECT 
          r.R_id,
          r.initial_loc,
          r.final_loc,
          r.R_date,
          r.distance_km,
          r.fare_amount,
          r.R_status,   -- ✅ IMPORTANT

          d.D_name,
          d.D_phone_no,
          d.D_gender,
          d.current_city,

          c.C_name,
          c.C_number,
          c.C_model,
          c.C_ac_nac,
          c.C_seater,

          COUNT(DISTINCT V.college_id) AS verifiedCount

        FROM Ride r
        JOIN Driver d ON r.D_eid = d.D_eid
        JOIN Car c ON r.C_id = c.C_id

        LEFT JOIN DriverVerification V
        ON d.D_eid = V.D_eid
        AND LOWER(V.status) = 'verified'

        WHERE r.R_id = @rideId

        GROUP BY
          r.R_id, r.initial_loc, r.final_loc, r.R_date,
          r.distance_km, r.fare_amount, r.R_status,
          d.D_name, d.D_phone_no, d.D_gender, d.current_city,
          c.C_name, c.C_number, c.C_model, c.C_ac_nac,c.C_seater
      `);

    // 🔹 2. Students
    const studentsResult = await pool.request()
      .input("rideId", sql.Int, rideId)
      .query(`
        SELECT 
          S.S_name,
          S.S_email_id,
          S.College_name,
          S.course,
          S.branch,
          S.year,

          RS.pickup_loc AS student_pickup,
          RS.drop_loc AS student_drop,
          RS.Ride_status,

          R.fare_amount / 
          NULLIF(
            (SELECT COUNT(*) 
             FROM RideStudent 
             WHERE R_id = R.R_id 
             AND Ride_status='Accepted'),
            0
          ) AS fare_per_student

        FROM RideStudent RS
        JOIN Student S ON RS.S_email_id = S.S_email_id
        JOIN Ride R ON RS.R_id = R.R_id

        WHERE RS.R_id = @rideId
      `);

    // 🔹 3. Response
    res.json({
      success: true,
      ride: rideResult.recordset[0] || null,
      students: studentsResult.recordset || []
    });

  } catch (err) {
    console.error("❌ ERROR:", err);

    res.status(500).json({
      success: false,
      message: err.message
    });
  }
});
// app.get("/api/ride/full-details/:rideId", async (req, res) => {
//   try {
//     const { rideId } = req.params;
//     const pool = await sql.connect(dbConfig);

//     // 1️⃣ Ride + Driver + Car
//     const rideResult = await pool.request()
//       .input("rideId", sql.Int, rideId)
//       .query(`
//         SELECT
//           r.R_id,
//           r.initial_loc,
//           r.final_loc,
//           r.R_date,
//           r.distance_km,
//           r.fare_amount,
//           d.D_eid,
//           d.D_name,
//           d.D_phone_no,
//           d.D_gender,
//           d.D_avg_rating,
//           d.cost_per_km,
//           d.current_city,
//           c.C_id,
//           c.C_name,
//           c.C_number,
//           c.C_model,
//           c.C_ac_nac,
//           c.C_seater,
//             COUNT(DISTINCT V.college_id) AS verifiedCount
//         FROM Ride r
//         JOIN Driver d ON r.D_eid = d.D_eid
//         JOIN Car c ON r.C_id = c.C_id

//     LEFT JOIN DriverVerification V
//       ON d.D_eid = V.D_eid
//       AND LOWER(V.status) = 'verified'

//         WHERE r.R_id = @rideId

//  GROUP BY
//       r.R_id,
//       r.initial_loc,
//       r.final_loc,
//       r.R_date,
//       r.distance_km,
//       r.fare_amount,
//       d.D_eid,
//       d.D_name,
//       d.D_phone_no,
//       d.D_gender,
//       d.D_avg_rating,
//       d.cost_per_km,
//       d.current_city,
//       c.C_id,
//       c.C_name,
//       c.C_number,
//       c.C_model,
//       c.C_ac_nac,
//       c.C_seater


//       `);

//     // 2️⃣ Students already joined
//     const studentsResult = await pool.request()
//       .input("rideId", sql.Int, rideId)
//       .query(`
//         SELECT
//         S.S_name,
//         S.College_name,
//         S.course,
//         S.branch,
//         S.year,

//         RS.pickup_loc AS student_pickup,
//         RS.drop_loc AS student_drop,

//         R.fare_amount /
//         NULLIF(
//             (SELECT COUNT(*)
//             FROM RideStudent
//             WHERE R_id = R.R_id
//             AND Ride_status IN ('Accepted', 'Started')),
//             0
//         ) AS fare_per_student
//         FROM RideStudent RS

//         JOIN Student S
//         ON RS.S_email_id = S.S_email_id

//         JOIN Ride R
//         ON RS.R_id = R.R_id

//         WHERE RS.R_id = @rideId
//         AND RS.Ride_status IN ('Accepted', 'Started')

//       `);

//     res.json({
//       success: true,
//       ride: rideResult.recordset[0],
//       students: studentsResult.recordset
//     });

//   } catch (err) {
//     res.status(500).json({
//       success: false,
//       message: err.message
//     });
//   }
// });

app.post("/api/ride/join", async (req, res) => {
  try {

    const {
      rideId,
      studentEmail,
      pickup,
      drop,
      pickupCity,
      dropCity,
      date
    } = req.body;

    const pool = await sql.connect(dbConfig);

    await pool.request()
      .input("R_id", sql.Int, rideId)
      .input("S_email_id", sql.VarChar(100), studentEmail)
      .input("Pickup_loc", sql.VarChar(200), pickup)
      .input("Drop_loc", sql.VarChar(200), drop)
      .input("pickup_city", sql.VarChar(100), pickupCity)
      .input("drop_city", sql.VarChar(100), dropCity)
      .input("R_pay_status", sql.VarChar(50), "Unpaid")
      .input("R_date", sql.VarChar, date)
      .query(`
        INSERT INTO RideStudent
        (R_id, S_email_id, Pickup_loc, Drop_loc,
         pickup_city, drop_city,
         R_pay_status, R_date)
        VALUES
        (@R_id, @S_email_id, @Pickup_loc, @Drop_loc,
         @pickup_city, @drop_city,
         @R_pay_status, @R_date)
      `);

    res.json({ success: true });

  } catch (err) {
    res.status(500).json({ success: false, message: err.message });
  }
});

app.get("/api/admin/drivers", async (req, res) => {

  const collegeId = req.query.collegeId;
  const pool = await sql.connect(dbConfig);
  const result = await pool.request()
    .input("collegeId", sql.VarChar, collegeId)
    .query(`
      SELECT
        D.D_name,
        D.D_licence_no,
        D.D_eid,
        DV.status
      FROM Driver D
      LEFT JOIN DriverVerification DV
        ON D.D_eid = DV.D_eid
        AND DV.college_id = @collegeId
         WHERE DV.status IS NULL
         ORDER BY D.D_name
    `);

  res.json(result.recordset);
});

//View Driver (Administration)

app.post("/api/admin/verify-driver", async (req, res) => {


  const { driverEmail, collegeId } = req.body;
  const pool = await sql.connect(dbConfig);
  await pool.request()
    .input("driverEmail", sql.VarChar, driverEmail)
    .input("collegeId", sql.VarChar, collegeId)
    .query(`
      MERGE DriverVerification AS target
      USING (SELECT @driverEmail AS D_eid, @collegeId AS college_id) AS source
      ON target.D_eid = source.D_eid
      AND target.college_id = source.college_id

      WHEN MATCHED THEN
        UPDATE SET status = 'Verified', verified_at = GETDATE()

      WHEN NOT MATCHED THEN
        INSERT (D_eid, college_id, status, verified_at)
        VALUES (@driverEmail, @collegeId, 'Verified', GETDATE());
    `);

  res.json({ success: true });
});
app.post("/api/admin/reject-driver", async (req, res) => {

  const { driverEmail, collegeId } = req.body;
  const pool = await sql.connect(dbConfig);

  await pool.request()
    .input("driverEmail", sql.VarChar, driverEmail)
    .input("collegeId", sql.VarChar, collegeId)
    .query(`
      MERGE DriverVerification AS target
      USING (SELECT @driverEmail AS D_eid, @collegeId AS college_id) AS source
      ON target.D_eid = source.D_eid
      AND target.college_id = source.college_id

      WHEN MATCHED THEN
        UPDATE SET status = 'Rejected'

      WHEN NOT MATCHED THEN
        INSERT (D_eid, college_id, status)
        VALUES (@driverEmail, @collegeId, 'Rejected');
    `);

  res.json({ success: true });
});



//Driver+Car(Initiate Ride)
app.get("/api/driver/details/:email", async (req, res) => {
  try {
    const { email } = req.params;
    const pool = await sql.connect(dbConfig);

    const result = await pool.request()
      .input("email", sql.VarChar(100), email)
      .query(`
        SELECT
          d.D_name,
          d.D_phone_no,
          d.D_avg_rating,
          d.cost_per_km,
          c.C_id,
          c.C_name,
          c.C_number,
          c.C_seater
        FROM Driver d
        LEFT JOIN Car c ON d.D_eid = c.D_eid
        WHERE d.D_eid = @email
      `);

    if (result.recordset.length === 0) {
      return res.status(404).json({ message: "Driver not found" });
    }

    res.json(result.recordset[0]);

  } catch (error) {
    res.status(500).json({ message: "Server error" });
  }
});

/*BRAND NEW*/
//Driver Reject Join Request
app.post("/api/driver/reject-join", async (req, res) => {

  const { requestId } = req.body;

  try {
    const pool = await sql.connect(dbConfig);

    await pool.request()
      .input("requestId", sql.Int, requestId)
      .query(`
        UPDATE RideStudent
        SET Ride_status = 'Rejected'
        WHERE id = @requestId
        AND Ride_status = 'Pending'
      `);

    res.json({ success: true, message: "Join request rejected" });

  } catch (error) {
    res.status(500).json({ error: error.message });
  }
});
app.put("/api/join/update-status", async (req, res) => {

  const { requestId, status } = req.body;

  try {
    const pool = await sql.connect(dbConfig);

    const result = await pool.request()
      .input("requestId", sql.Int, requestId)
      .input("status", sql.VarChar, status)
      .query(`
        UPDATE RideStudent
        SET Ride_status = @status
        WHERE id = @requestId
        AND Ride_status = 'Pending'
      `);

    if (result.rowsAffected[0] === 0) {
      return res.status(400).json({
        message: "Join request already updated or invalid"
      });
    }

    return res.json({
      message: "Join request updated successfully"
    });

  } catch (err) {
    console.error(err);
    res.status(500).json({ error: "Server error" });
  }
});
app.get("/api/driver/join-requests/:driverEmail", async (req, res) => {

  const { driverEmail } = req.params;

  try {
    const pool = await sql.connect(dbConfig);

    const result = await pool.request()
      .input("driverEmail", sql.VarChar(100), driverEmail)
      .query(`
        SELECT
            rs.id,
            rs.R_id,
            rs.S_email_id,
            rs.Pickup_loc,
            rs.Drop_loc,
            rs.R_date,
            rs.Ride_status,
            r.R_timing,
            r.pickup_city,
            r.drop_city,
            r.distance_km,
            r.fare_amount,
            r.initial_loc,
            r.final_loc
        FROM RideStudent rs
        JOIN Ride r ON rs.R_id = r.R_id
        WHERE r.D_eid = @driverEmail
        AND rs.Ride_status = 'Pending'
      `);

    res.json(result.recordset);

  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});



// ======================
// CRON JOB (ADD HERE)
// ======================
cron.schedule("0 * * * *", async () => {

  try {
    const pool = await sql.connect(dbConfig);

    await pool.request().query(`
      UPDATE Ride
      SET R_status = 'Expired'
      WHERE R_status = 'Pending'
      AND DATEDIFF(HOUR, requested_at, GETDATE()) >= 24
    `);

    console.log("Expired rides checked");

  } catch (err) {
    console.error("Cron Error:", err);
  }

});

app.get("/api/driver/pending/:driverEmail", async (req, res) => {

  try {
    const pool = await sql.connect(dbConfig);

    const result = await pool.request()
      .input("driverEmail", sql.VarChar(100), req.params.driverEmail)
      .query(`
        SELECT *
        FROM Ride
        WHERE D_eid = @driverEmail
        AND R_status = 'Pending'
      `);

    res.json(result.recordset);

  } catch (error) {
    res.status(500).json({ error: error.message });
  }
});
app.post("/api/ride/accept", async (req, res) => {

  const { rideId } = req.body;

  try {
    const pool = await sql.connect(dbConfig);

    await pool.request()
      .input("rideId", sql.Int, rideId)
      .query(`
        UPDATE Ride
        SET R_status = 'Accepted',
            response_at = GETDATE()
        WHERE R_id = @rideId
        AND R_status = 'Pending'
      `);

    res.json({ success: true, message: "Ride Accepted" });

  } catch (error) {
    res.status(500).json({ error: error.message });
  }
});
app.post("/api/ride/reject", async (req, res) => {

  const { rideId } = req.body;

  try {
    const pool = await sql.connect(dbConfig);

    await pool.request()
      .input("rideId", sql.Int, rideId)
      .query(`
        UPDATE Ride
        SET R_status = 'Rejected',
            response_at = GETDATE()
        WHERE R_id = @rideId
        AND R_status = 'Pending'
      `);

    res.json({ success: true, message: "Ride Rejected" });

  } catch (error) {
    res.status(500).json({ error: error.message });
  }
});

app.put("/api/ride/update-status", async (req, res) => {
  const { rideId, driverId, status } = req.body;

  try {
    const pool = await sql.connect(dbConfig);

    if (status === "Rejected") {

      // 🔴 Use transaction for safety
      const transaction = new sql.Transaction(pool);
      await transaction.begin();

      try {
        const request = new sql.Request(transaction);

        // Delete from RideStudent first (FK constraint safety)
        await request
          .input("rideId", sql.Int, rideId)
          .query(`
            DELETE FROM RideStudent
            WHERE R_id = @rideId
          `);

        // Then delete from Ride
        await request
          .input("driverId", sql.VarChar, driverId)
          .query(`
            DELETE FROM Ride
            WHERE R_id = @rideId
            AND D_eid = @driverId
          `);

        await transaction.commit();

        return res.json({ message: "Ride rejected and deleted successfully" });

      } catch (err) {
        await transaction.rollback();
        throw err;
      }

    } else {

      // 🟢 ACCEPT CASE
      const transaction = new sql.Transaction(pool);
      await transaction.begin();

      try {
        const request = new sql.Request(transaction);

        // Update Ride
        const rideResult = await request
          .input("rideId", sql.Int, rideId)
          .input("driverId", sql.VarChar, driverId)
          .input("status", sql.VarChar, status)
          .query(`
            UPDATE Ride
            SET R_status = @status
            WHERE R_id = @rideId
            AND D_eid = @driverId
            AND R_status = 'Pending'
          `);

        if (rideResult.rowsAffected[0] === 0) {
          await transaction.rollback();
          return res.status(400).json({
            message: "Ride already updated or invalid"
          });
        }

        // Update RideStudent
        await request.query(`
          UPDATE RideStudent
          SET Ride_status = @status
          WHERE R_id = @rideId
          AND Ride_status = 'Pending'
        `);

        await transaction.commit();

        return res.json({ message: "Ride accepted successfully" });

      } catch (err) {
        await transaction.rollback();
        throw err;
      }
    }

  } catch (err) {
    console.error(err);
    res.status(500).json({ error: "Server error" });
  }
});

app.get("/api/driver/:driverId/rides", async (req, res) => {

  const { driverId } = req.params;

  try {
    const pool = await sql.connect(dbConfig);

    const result = await pool.request()
      .input("driverId", sql.VarChar, driverId)
      .query(`
        SELECT * FROM Ride
        WHERE D_eid = @driverId
        AND R_status = 'Pending'
      `);

    res.json(result.recordset);

  } catch (err) {
    res.status(500).json({ error: err.message });
  }

});

//CONFIRMED RIDES SCREEN OF DRIVER
app.get("/api/driver/confirmed-rides/:driverId", async (req, res) => {

  const { driverId } = req.params;

  try {
    const pool = await sql.connect(dbConfig);

    const result = await pool.request()
      .input("driverId", sql.VarChar, driverId)
      .query(`
        SELECT *
        FROM Ride
        WHERE D_eid = @driverId
        AND R_status IN ('Accepted', 'Started')
        ORDER BY R_date DESC
      `);

    res.json(result.recordset);

  } catch (err) {
    console.error(err);
    res.status(500).json({ error: "Server error" });
  }
});


//======================================================
//3-HOUR RIDE REMINDER CRON (FINAL CLEAN)
//======================================================
cron.schedule("*/10 * * * *", async () => {

  console.log("Cron running...");
  console.log("IST:", new Date(Date.now() + 330 * 60000));

  try {

    const pool = await sql.connect(dbConfig);

    // ✅ SQL handles everything
    const result = await pool.request().query(`
      SELECT  
          r.R_id,
          r.D_eid,
          rs.Pickup_loc,
          rs.Drop_loc,
          r.R_date,
          r.R_timing,
          rs.S_email_id,
          p.G_eid,
    p.G_name,
    d.D_name,
    d.D_phone_no,
    c.C_name,
    c.C_number,
    c.C_model,
    c.C_colour
      FROM Ride r
      JOIN RideStudent rs ON r.R_id = rs.R_id
      LEFT JOIN Parents_Information p
     
    ON rs.S_email_id = p.S_email_id
    LEFT JOIN Driver d
    ON r.D_eid = d.D_eid

LEFT JOIN Car c
    ON r.D_eid = c.D_eid
      WHERE r.R_status = 'Accepted'
      AND ISNULL(rs.reminder_sent, 0) = 0
      -- ✅ Ride within next 3 hours
      AND DATEADD(SECOND,
          DATEDIFF(SECOND,0,r.R_timing),
          CAST(r.R_date AS DATETIME)
      ) <= DATEADD(HOUR,3, DATEADD(MINUTE,330,GETUTCDATE()))
    `);

    console.log("Rows fetched:", result.recordset.length);

    for (const ride of result.recordset) {

      const update = await pool.request()
        .input("R_id", sql.Int, ride.R_id)
        .input("S_email_id", sql.VarChar, ride.S_email_id)
        .query(`
          UPDATE RideStudent
          SET reminder_sent = 1
          WHERE R_id = @R_id
          AND S_email_id = @S_email_id
          AND (reminder_sent = 0 OR reminder_sent IS NULL)
        `);

      console.log("Rows updated:", update.rowsAffected[0]);

      if (update.rowsAffected[0] > 0) {

        await sendRideReminder(
          ride.S_email_id,
          ride.Pickup_loc,
          ride.Drop_loc,
          ride.R_date,
          ride.R_timing,
          ride.D_name,
          ride.D_phone_no,

          ride.C_name,
          ride.C_number,
          ride.C_model,
          ride.C_colour
        );

        console.log("✅ Mail sent:", ride.S_email_id);
        if (ride.G_eid) {

          await sendRideReminder(
            ride.G_eid,
            ride.Pickup_loc,
            ride.Drop_loc,
            ride.R_date,
            ride.R_timing,

            ride.D_name,
            ride.D_phone_no,

            ride.C_name,
            ride.C_number,
            ride.C_model,
            ride.C_colour
          );

          console.log("✅ Parent mail sent:", ride.G_eid);
        }
      }
      // =========================
      // ✅ DRIVER MAIL
      // =========================
      if (ride.D_eid && ride.D_eid.trim() !== "") {

        const updateDriver = await pool.request()
          .input("R_id", sql.Int, ride.R_id)
          .query(`
              UPDATE Ride
              SET reminder_sent = 1
              WHERE R_id = @R_id
              AND (reminder_sent = 0 OR reminder_sent IS NULL)
            `);

        if (updateDriver.rowsAffected[0] > 0) {

          await sendRideReminder(
            ride.D_eid,
            ride.Pickup_loc,
            ride.Drop_loc,
            ride.R_date,
            ride.R_timing,
            ride.D_name,
            ride.D_phone_no,
            ride.C_name,
            ride.C_number,
            ride.C_model,
            ride.C_colour
          );

          console.log(`✅ Driver mail sent for Ride ${ride.R_id} to ${ride.D_eid}`);
        }
      }

    }


  } catch (err) {
    console.error("❌ Cron Error:", err);
  }

});

//***gps***
app.put("/startRide/:rideId", async (req, res) => {
  const rideId = req.params.rideId;

  try {

    // 1️⃣ Update Ride table
    const rideResult = await sql.query(`
      UPDATE Ride
SET R_status = 'Started'
WHERE R_id = ${rideId}
AND R_status = 'Accepted'
    `);

    if (rideResult.rowsAffected[0] === 0) {
      return res.status(404).json({ error: "Ride not found" });
    }

    // 2️⃣ Update RideStudent table
    await sql.query(`
      UPDATE RideStudent
      SET Ride_status = 'Started'
      WHERE R_id = ${rideId}
      AND Ride_status = 'Accepted'
    `);

    res.json({ message: "Ride and students updated successfully" });

  } catch (err) {
    console.error("START RIDE ERROR:", err);
    res.status(500).json({ error: "Failed to start ride" });
  }
});

app.put("/endRide/:rideId", async (req, res) => {
  const rideId = req.params.rideId;

  try {

    // Update Ride table
    const rideResult = await sql.query(`
      UPDATE Ride
      SET R_status = 'Completed'
      WHERE R_id = ${rideId}
    `);

    if (rideResult.rowsAffected[0] === 0) {
      return res.status(404).json({ error: "Ride not found" });
    }

    // Update RideStudent table
    await sql.query(`
      UPDATE RideStudent
      SET Ride_status = 'Completed'
      WHERE R_id = ${rideId}
      AND Ride_status = 'Started'
    `);

    res.json({ message: "Ride completed successfully" });

  } catch (err) {
    console.error("END RIDE ERROR:", err);
    res.status(500).json({ error: "Failed to complete ride" });
  }
});

app.get('/ride-history/:email', async (req, res) => {
  try {
    const email = req.params.email;

    const pool = await poolPromise;   // ✅ FIX HERE

    const result = await pool.request()
      .input('email', sql.VarChar, email)
      .query(`
                SELECT
                    r.R_id,
                    r.initial_loc,
                    r.final_loc,
                    r.fare_amount,
                    r.distance_km,
                    r.R_date,
                    r.R_status AS Ride_status,
                        r.D_eid AS D_eid,
                    d.D_name AS driver_name

                FROM dbo.RideStudent rs
                JOIN dbo.Ride r ON rs.R_id = r.R_id
                LEFT JOIN dbo.Driver d ON r.D_eid = d.D_eid

                WHERE rs.S_email_id = @email
                AND r.R_status = 'Completed'

                ORDER BY r.R_date DESC
            `);

    res.json({
      success: true,
      rides: result.recordset
    });

  } catch (err) {
    console.error(err);
    res.status(500).json({ error: "Error fetching ride history" });
  }
});

app.post('/rate-driver', async (req, res) => {
  try {
    const { R_id, email, driverId, rating } = req.body;

    // ✅ MUST be inside async
    const pool = await poolPromise;

    // 🔹 1. Check if already rated
    const checkResult = await pool.request()
      .input('R_id', sql.Int, R_id)
      .input('email', sql.VarChar, email)
      .query(`
                SELECT has_rated 
                FROM dbo.RideStudent
                WHERE R_id = @R_id AND S_email_id = @email
            `);

    if (checkResult.recordset.length > 0 && checkResult.recordset[0].has_rated === true) {
      return res.status(400).json({
        success: false,
        message: "You have already rated this ride"
      });
    }

    // 🔹 2. Insert rating
    await pool.request()
      .input('R_id', sql.Int, R_id)
      .input('email', sql.VarChar, email)
      .input('driverId', sql.VarChar, driverId)
      .input('rating', sql.Float, rating)
      .query(`
                INSERT INTO dbo.RideRatings (R_id, S_email_id, D_eid, rating)
                VALUES (@R_id, @email, @driverId, @rating)
            `);

    const debug = await pool.request()
      .input('R_id', sql.Int, R_id)
      .query(`
    SELECT R_id, S_email_id, has_rated 
    FROM RideStudent
    WHERE R_id = @R_id
  `);

    console.log("DB rows:", debug.recordset);
    console.log("Incoming email:", email);
    // 🔹 3. Mark as rated
    /*
    await pool.request()
        .input('R_id', sql.Int, R_id)
        .input('email', sql.VarChar, email)
        .query(`
            UPDATE dbo.RideStudent
            SET has_rated = 1
            WHERE R_id = @R_id AND S_email_id = @email
        `);*/

    await pool.request()
      .input('R_id', sql.Int, R_id)
      .input('email', sql.VarChar, email.trim())
      .query(`
    UPDATE dbo.RideStudent
    SET has_rated = 1
    WHERE R_id = @R_id 
    AND LOWER(LTRIM(RTRIM(S_email_id))) = LOWER(LTRIM(RTRIM(@email)))
  `);

    // 🔹 4. Update driver rating
    await pool.request()
      .input('driverId', sql.VarChar, driverId)
      .query(`
                UPDATE dbo.Driver
                SET 
                    D_avg_rating = (
                        SELECT AVG(rating * 1.0)
                        FROM dbo.RideRatings
                        WHERE D_eid = @driverId
                    ),
                    total_ratings = (
                        SELECT COUNT(*)
                        FROM dbo.RideRatings
                        WHERE D_eid = @driverId
                    )
                WHERE D_eid = @driverId
            `);

    // 🔹 5. Fetch data for email check
    const result = await pool.request()
      .input('driverId', sql.VarChar, driverId)
      .input('R_id', sql.Int, R_id)
      .query(`
                SELECT d.D_avg_rating, d.D_eid, r.mail_sent
                FROM dbo.Driver d
                JOIN dbo.Ride r ON d.D_eid = r.D_eid
                WHERE d.D_eid = @driverId AND r.R_id = @R_id
            `);

    const data = result.recordset[0];

    // 🔥 6. Email logic
    if (data) {
      const avgRating = parseFloat(data.D_avg_rating);

      if (avgRating <= 2 && data.mail_sent === false) {
        await sendWarningEmail(data.D_eid, avgRating);

        await pool.request()
          .input('R_id', sql.Int, R_id)
          .query(`
                        UPDATE dbo.Ride
                        SET mail_sent = 1
                        WHERE R_id = @R_id
                    `);
      }
    }

    // ✅ Final response
    res.json({
      success: true,
      message: "Rating submitted successfully"
    });

  } catch (err) {
    console.error("ERROR:", err);
    res.status(500).json({
      success: false,
      message: "Rating failed"
    });
  }
});


//===========================================================
//ADMIN: TODAY'S RIDES (BASED ON ADMIN EMAIL)
//===========================================================
app.get("/admin/todays-rides", async (req, res) => {

  try {

    // 🔥 STEP 1: Get admin email from query
    const adminEmail = req.query.email;

    if (!adminEmail) {
      return res.status(400).json({
        success: false,
        message: "Admin email is required"
      });
    }

    const pool = await sql.connect(dbConfig);

    // 🔐 STEP 2: Verify this is actually an admin
    const userCheck = await pool.request()
      .input("email", sql.VarChar, adminEmail)
      .query(`
        SELECT user_type 
        FROM [User]
        WHERE email_id = @email
      `);

    if (
      userCheck.recordset.length === 0 ||
      userCheck.recordset[0].user_type !== 'Administration'
    ) {
      return res.status(403).json({
        success: false,
        message: "Unauthorized access"
      });
    }

    // 🔥 STEP 3: Extract domain
    const domain = adminEmail.split("@")[1].toLowerCase();

    // 🔥 STEP 4: Fetch today's rides
    const result = await pool.request()
      .input("domain", sql.VarChar, domain)
      .query(`
        SELECT 
    r.R_id,
    r.R_timing,
    r.R_date,
    r.R_status,
    r.initial_loc,
    r.final_loc,
    s.S_name,
    rs.S_email_id,
    rs.Ride_status
FROM Ride r

-- 🔥 IMPORTANT: Filter at JOIN level
JOIN RideStudent rs 
    ON r.R_id = rs.R_id

JOIN Student s 
    ON rs.S_email_id = s.S_email_id
    AND LOWER(SUBSTRING(s.S_email_id, CHARINDEX('@', s.S_email_id) + 1, LEN(s.S_email_id))) = @domain

WHERE 
    -- ✅ DATE (IST)
    CAST(r.R_date AS DATE) = CAST(DATEADD(MINUTE, 330, GETDATE()) AS DATE)

    -- ✅ STATUS
    AND rs.Ride_status IN ('Accepted', 'Completed')

ORDER BY r.R_timing;`);

    res.json({
      success: true,
      rides: result.recordset
    });

  } catch (err) {
    console.error("Admin rides error:", err);
    res.status(500).json({
      success: false,
      error: err.message
    });
  }
});

app.post("/api/cancel-seat", async (req, res) => {
  try {
    const { rideId, email } = req.body;

    const pool = await sql.connect(dbConfig);

    // 🔥 Update RideStudent table
    const result = await pool.request()
      .input("rideId", sql.Int, rideId)
      .input("email", sql.VarChar, email)
      .query(`
        UPDATE RideStudent
        SET Ride_status = 'Cancelled'
        WHERE R_id = @rideId
        AND S_email_id = @email
      `);

    res.json({
      success: true,
      message: "Seat cancelled successfully"
    });

  } catch (err) {
    console.error("Cancel Error:", err);

    res.status(500).json({
      success: false,
      message: err.message
    });
  }
});
