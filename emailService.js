const nodemailer = require("nodemailer");
require("dotenv").config();

const transporter = nodemailer.createTransport({
  service: "gmail",
  auth: {
    user: process.env.EMAIL_USER,
    pass: process.env.EMAIL_PASS,
  },
});

async function sendRideReminder(
  email,
  from,
  to,
  date,
  time,
  driverName,
  driverPhone,
  carName,
  carNumber,
  carModel,
  carColour
) {
  try {
    console.log("Driver:", driverName);
    console.log("Car:", carName);

    // ✅ FORMAT DATE (remove GMT)
    let formattedDate = "Not Available";
    if (date) {
      const rideDate = new Date(date);
      formattedDate = rideDate.toLocaleDateString("en-IN", {
        weekday: "short",
        day: "2-digit",
        month: "short",
        year: "numeric",
      });
    }

    // ✅ FORMAT TIME (fix Invalid Date)
    let formattedTime = "Not Available";

    if (time && typeof time === "string") {
      const parts = time.split(":"); // ["05","30","00"]

      if (parts.length >= 2) {
        const tempDate = new Date();
        tempDate.setHours(parts[0], parts[1], parts[2] || 0);

        formattedTime = tempDate.toLocaleTimeString("en-IN", {
          hour: "2-digit",
          minute: "2-digit",
          hour12: true,
        });
      }
    }

    await transporter.sendMail({
      from: `"GoCab" <${process.env.EMAIL_USER}>`,
      to: email,
      subject: "Ride Reminder - GoCab",
      html: `
        <h2>🚗 Ride Reminder</h2>
        <p>Your ride is scheduled:</p>

        <p><b>From:</b> ${from}</p>
        <p><b>To:</b> ${to}</p>

        <p><b>Date:</b> ${formattedDate}</p>
        <p><b>Time:</b> ${time
          ? new Date(time).toISOString().substring(11, 16)
          : "Not Available"
        }</p>

        <hr/>

        <p><b>Driver:</b> ${driverName || "Not Assigned"}</p>
        <p><b>Phone:</b> ${driverPhone || "Not Available"}</p>

        <p><b>Car:</b> ${carName || "Not Assigned"}</p>
        <p><b>Number:</b> ${carNumber || "Not Available"}</p>
        <p><b>Model:</b> ${carModel || "Not Available"}</p>
        <p><b>Colour:</b> ${carColour || "Not Available"}</p>
      `,
    });

    console.log("Reminder email sent to:", email);
  } catch (err) {
    console.error("Email error:", err);
  }
}

async function sendWarningEmail(driverEmail, avgRating) {
  try {
    await transporter.sendMail({
      from: `"GoCab" <${process.env.EMAIL_USER}>`,
      to: driverEmail,
      subject: "⚠️ Low Rating Warning",
      html: `
        <h2>⚠️ Warning from GoCab Team</h2>
        <p>Your average rating has dropped to <b>${avgRating}</b>.</p>
        <p>Please improve your service quality.</p>
      `,
    });

    console.log("Warning email sent to:", driverEmail);
  } catch (err) {
    console.error("Warning email error:", err);
  }
}

module.exports = {
  sendRideReminder,
  sendWarningEmail,
};






// const nodemailer = require("nodemailer");
// require("dotenv").config();

// const transporter = nodemailer.createTransport({
//   service: "gmail",
//   auth: {
//     user: process.env.EMAIL_USER,
//     pass: process.env.EMAIL_PASS,
//   },
// });

// async function sendRideReminder(email, from, to, date, time,driverName, driverPhone,
//   carName, carNumber, carModel, carColour) {
//   try {
//     console.log("Driver:", driverName);
//     console.log("Car:", carName);
//     await transporter.sendMail({
//       from: `"GoCab" <${process.env.EMAIL_USER}>`,
//       to: email,
//       subject: "Ride Reminder - GoCab",
//       html: `
//         <h2>🚗 Ride Reminder</h2>
//         <p>Your ride is scheduled:</p>
//         <p><b>From:</b> ${from}</p>
//         <p><b>To:</b> ${to}</p>
//         <p><b>Date:</b> ${date}</p>
//         <p><b>Time:</b> ${new Date(`1970-01-01T${time}`).toLocaleTimeString()}</p>
//         <p><b>Driver:</b> ${driverName || "Not Assigned"}</p>
// <p><b>Phone:</b> ${driverPhone || "Not Available"}</p>

// <p><b>Car:</b> ${carName || "Not Assigned"}</p>
// <p><b>Number:</b> ${carNumber || "Not Available"}</p>
// <p><b>Model:</b> ${carModel || "Not Available"}</p>
// <p><b>Colour:</b> ${carColour || "Not Available"}</p>
//       `,
//     });

//     console.log("Reminder email sent to:", email);
//   } catch (err) {
//     console.error("Email error:", err);
//   }
// }
// async function sendWarningEmail(driverEmail, avgRating) {
//   try {
//     await transporter.sendMail({
//       from: `"GoCab" <${process.env.EMAIL_USER}>`,
//       to: driverEmail,
//       subject: "⚠️ Low Rating Warning",
//       html: `
//         <h2>⚠️ Warning from GoCab Team</h2>
//         <p>Your average rating has dropped to <b>${avgRating}</b>.</p>
//         <p>Please improve your service quality.</p>
//       `,
//     });

//     console.log("Warning email sent to:", driverEmail);
//   } catch (err) {
//     console.error("Warning email error:", err);
//   }
// }

// module.exports = {
//   sendRideReminder,
//   sendWarningEmail,
// };