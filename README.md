# SON Clinic & Pharmacy — Android Java starter

## Features
- Native Android UI built in Java
- Dashboard totals
- Patient registration and search
- Appointment scheduling and status updates
- Medicine inventory and low-stock indicators
- Stock adjustments
- Sales recording with stock deduction
- Local SQLite database
- KES price display

## Run
1. Extract the ZIP.
2. Open the `son-clinic-pharmacy-android` folder in Android Studio.
3. Install Android SDK Platform 35 and use JDK 17.
4. Allow Gradle to sync, then run on an emulator or Android phone.

Demo login: `admin` / `ChangeMe123!`

## Important security notice
This is a learning prototype. The login is hard-coded and is not secure authentication. Do not use it with real patient data. Before production, implement server-backed authentication, role permissions, encrypted data and backups, audit logs, session timeouts, and a compliance review.

Data is stored in the app's private SQLite database on the device. There is no cloud sync or multi-device support yet. Prescriptions, diagnosis, receipt printing, M-Pesa integration, and regulatory workflows are not included.
