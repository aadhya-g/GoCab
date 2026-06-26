GoCab - Banasthali Cab Sharing App

GoCab is an Android application designed for Banasthali students to share rides, find travel partners, and manage cab bookings efficiently.

--------------------------------------------------

Project Requirements

- Android Studio (latest version)
- Node.js (v16 or above)
- Microsoft Azure account (for database)
- Internet connection
- Firebase
--------------------------------------------------

Installation Steps

1. Clone the Repository
git clone https://github.com/aadhya-g/GoCab.git
cd gocab

2. Open Android App in Android Studio
- Open Android Studio
- Click on Open Project
- Select the gocab folder

3. Setup Firebase
- Add google-services.json in:
app/google-services.json

4. Backend Setup (Node.js)

cd backend
npm install

5. Configure Environment Variables

Create a .env file inside backend folder and add:

DB_SERVER=your_azure_server
DB_USER=your_username
DB_PASS=your_password
DB_NAME=your_database_name
DB_PORT=1433

EMAIL_USER=your_email@gmail.com
EMAIL_PASS=your_app_password

6. Start Backend Server

npm start

Backend runs on:
http://localhost:5000

--------------------------------------------------

Microsoft Azure Database Setup

- Create a database in Azure
- Copy connection string
- Paste it in .env file
- Ensure network/firewall allows access

--------------------------------------------------

Important Configuration

- If running on emulator:
  Use 10.0.2.2 instead of localhost

- If running on physical device:
  Use your system IP address (e.g., 192.168.x.x)

- Ensure backend PORT matches API base URL

--------------------------------------------------


To make the backend accessible from the Android app (especially when running on a VM),
ngrok is used to expose the local server to the internet.

Steps:

1. Install ngrok

2. Start backend server:
   npm start

3. Run ngrok on backend port:
   ngrok http 5000

4. Copy the generated public URL (e.g., https://abcd1234.ngrok.io)

5. Replace API base URL in Android app with this ngrok URL

--------------------------------------------------

Steps to Run the Project

Option 1: Run on Emulator
1. Open Android Studio
2. Start Emulator
3. Click Run

Option 2: Run on Physical Device
1. Enable Developer Options
2. Enable USB Debugging
3. Connect device
4. Click Run

--------------------------------------------------

Steps to Successfully Execute the Project

1. Setup Azure Database and copy connection string
2. Configure .env file in backend
3. Start backend server using:
   npm start
4. Open project in Android Studio
5. Run the app on emulator or physical device

--------------------------------------------------

Running on Virtual Machine

1. Copy project to VM
2. Install:
   - Android Studio
   - Node.js
3. Start backend:
   cd backend
   npm install
   npm start
4. Run Android app inside VM

Note: Ensure both backend and Android app are running inside the VM for proper evaluation.

--------------------------------------------------

Project Structure

GoCab/
│── app/
│── backend/
│── gradle/
│── build.gradle

--------------------------------------------------

Features

- User Authentication
- Ride Creation & Joining
- Cab Sharing
- Real-time updates
- Ride Cancellation

--------------------------------------------------

Notes

- Backend must be running before app
- Check API base URL in app code
- Azure DB must be accessible
- Internet connection is required

--------------------------------------------------


