# Campus ERP - Backend Server

Full-stack educational Enterprise Resource Planning (ERP) backend built with **Node.js**, **Express**, and **MongoDB (Mongoose)**, featuring JWT authentication, role-based access control (Admin, Teacher, Student), and a Student Management CRUD module.

## 📁 Directory Structure
```
server/
├── config/
│   └── db.js                 # Mongoose connection logic
├── controllers/
│   ├── authController.js     # User registration, login, and auth profile
│   └── studentController.js  # Student CRUD operations
├── middleware/
│   └── auth.js               # JWT verification & role authorization (RBAC)
├── models/
│   ├── User.js               # User schema with bcrypt password hashing
│   └── Student.js            # Student schema with validation & rollNumber
├── routes/
│   ├── testRoutes.js         # /api/test endpoint
│   ├── authRoutes.js         # /api/auth endpoints
│   └── studentRoutes.js      # /api/students endpoints
├── .env                      # Environment variables
├── .env.example              # Template environment variables
├── package.json              # Dependencies and scripts
├── server.js                 # Express app initialization & server entry point
└── API_DOCUMENTATION.md      # Full REST API specifications and CRUD docs
```

## 🛠️ Quick Start

### 1. Install dependencies
```bash
cd server
npm install
```

### 2. Configure Environment
Copy `.env.example` to `.env` (already created):
```env
PORT=5000
MONGO_URI=mongodb://127.0.0.1:27017/campus_erp
JWT_SECRET=campus_erp_jwt_super_secret_key_2026_xyz
JWT_EXPIRE=30d
NODE_ENV=development
```

### 3. Run Server
Development mode with automatic reload:
```bash
npm run dev
```

Production mode:
```bash
npm start
```

### 4. Verify API
Open your browser or run:
```bash
curl http://localhost:5000/api/test
```
Response:
```json
{
  "success": true,
  "message": "ERP API Server is online and functioning properly!",
  "service": "Campus ERP REST API",
  "version": "1.0.0",
  "timestamp": "2026-10-09T23:30:00.000Z"
}
```
