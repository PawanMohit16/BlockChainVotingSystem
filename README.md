
# Secure Online Voting System using Face Recognition and Blockchain

A modern, secure online voting system built with the MERN stack that provides features such as Accuracy, Convenience, and Privacy. Our system uses face recognition for voter verification, blockchain technology for vote integrity, and a robust backend API.

![Logo](https://raw.githubusercontent.com/Themysticlees/Secure-Online-Voting-System-using-Face-Recognition-and-BlockChain/master/src/main/resources/static/images/loader1.png)

## Table of Contents
- [Brief Description](#brief-description)
- [Objective of the Project](#objective-of-the-project)
- [Screenshots](#screenshots)
- [Technologies](#technologies)
- [Features](#features)
- [Installation & Setup](#installation--setup)
- [API Documentation](#api-documentation)
- [Authors](#authors)

## Brief Description

An online voting system is an online voting technique. In this system, people who are registered in the system can cast their vote online without going to any physical polling station. There are many voting procedures that are being used for voting purposes, such as ballot paper, EVM machines but all these procedures require more time and more manpower. People also have to wait in long queues which takes a lot of time in the process.

To eliminate all these drawbacks, we provide an online system that provides features such as accuracy, convenience, and privacy. Our website will not only save time but also make a hassle-free user experience. Our online voting system provides a platform with proper instructions where users can register themselves and cast vote remotely. During the entire process, multiple verification processes are carried out which makes this entire system secure. This project gives freedom to the voter to use their voting rights from anywhere.

The main aim of this project is to cast votes with proper anti-corruption techniques and to minimize errors and be hassle-free.

## Objective of the Project
The specific objectives of the project include:

- Improving the existing/current voting process or approach.
- Implementing an automated voting system.
- Validating the system to ensure that only eligible voters are allowed to vote.
- An increasing number of voters as individuals will find it easier and more convenient to vote, especially those abroad.

## Screenshots

#### Home Page
![](https://raw.githubusercontent.com/Themysticlees/Secure-Online-Voting-System-using-Face-Recognition-and-BlockChain/master/Screenshots/Image%201.png)

#### Registration Page
![](https://raw.githubusercontent.com/Themysticlees/Secure-Online-Voting-System-using-Face-Recognition-and-BlockChain/master/Screenshots/Image%202.png)

#### Instructions (In Video and Written)
![](https://raw.githubusercontent.com/Themysticlees/Secure-Online-Voting-System-using-Face-Recognition-and-BlockChain/master/Screenshots/Image%203.jpg)

#### Admin Portal
![](https://raw.githubusercontent.com/Themysticlees/Secure-Online-Voting-System-using-Face-Recognition-and-BlockChain/master/Screenshots/Image%204.png)

#### Details of the Registered Users
![](https://raw.githubusercontent.com/Themysticlees/Secure-Online-Voting-System-using-Face-Recognition-and-BlockChain/master/Screenshots/Image%205.png)

#### User Portal when Voting goes LIVE!
![](https://raw.githubusercontent.com/Themysticlees/Secure-Online-Voting-System-using-Face-Recognition-and-BlockChain/master/Screenshots/Image%206.png)

#### Confirmation Email after successfully Voting
![](https://raw.githubusercontent.com/Themysticlees/Secure-Online-Voting-System-using-Face-Recognition-and-BlockChain/master/Screenshots/Image%207.png)

## Technologies

### Backend (MERN Stack)
- **Node.js** - Runtime environment
- **Express.js** - Web framework
- **MongoDB** - NoSQL database
- **Mongoose** - MongoDB object modeling
- **JWT** - Authentication and authorization
- **bcryptjs** - Password hashing
- **face-api.js** - Face recognition and detection
- **crypto** - Blockchain implementation
- **nodemailer** - Email services

### Frontend
- **HTML5** - Markup language
- **CSS3** - Styling
- **JavaScript** - Client-side logic
- **jQuery** - DOM manipulation
- **Thymeleaf** - Template engine (for server-side rendering)

### Security & Features
- **Helmet** - Security headers
- **Rate Limiting** - API protection
- **CORS** - Cross-origin resource sharing
- **Input Validation** - Data sanitization
- **Blockchain** - Vote integrity verification

## Features

- **Face Recognition** - Biometric voter verification
- **OTP Validation** - Email-based verification
- **Video Instructions** - User guidance
- **Decentralized Data** - Blockchain-based vote storage
- **User Authentication** - JWT-based security
- **Admin Portal** - Election and user management
- **Real-time Results** - Live election statistics
- **Multi-constituency Support** - Flexible election setup
- **Audit Trail** - Complete voting history
- **Email Notifications** - Automated communications

## Installation & Setup

### Prerequisites
- Node.js (v14 or higher)
- MongoDB (v4.4 or higher)
- npm or yarn package manager

### Step 1: Clone the Repository
```bash
git clone https://github.com/yourusername/Secure-Online-Voting-System-using-Face-Recognition-and-BlockChain.git
cd Secure-Online-Voting-System-using-Face-Recognition-and-BlockChain
```

### Step 2: Install Dependencies
```bash
npm install
```

### Step 3: Environment Configuration
Copy the environment example file and configure your settings:
```bash
cp env.example .env
```

Edit `.env` file with your configuration:
```env
# Server Configuration
PORT=4000
NODE_ENV=development

# MongoDB Configuration
MONGODB_URI=mongodb://localhost:27017/voting_system

# JWT Configuration
JWT_SECRET=your-super-secret-jwt-key-change-this-in-production

# Email Configuration
SMTP_HOST=smtp.gmail.com
SMTP_PORT=587
SMTP_USER=your-email@gmail.com
SMTP_PASS=your-app-password

# Frontend URL
FRONTEND_URL=http://localhost:3000
```

### Step 4: Database Setup
Start MongoDB service and create the database:
```bash
# Start MongoDB (Windows)
net start MongoDB

# Start MongoDB (macOS/Linux)
sudo systemctl start mongod

# Create database
mongosh
use voting_system
```

### Step 5: Face Recognition Models
Download face-api.js models and place them in the `models/face-api` directory:
```bash
mkdir -p models/face-api
# Download models from: https://github.com/justadudewhohacks/face-api.js/tree/master/weights
```

### Step 6: Run the Application
```bash
# Development mode
npm run dev

# Production mode
npm start
```

The application will be available at: http://localhost:4000

## API Documentation

### Authentication Endpoints
- `POST /api/auth/register` - User registration
- `POST /api/auth/login` - User login
- `POST /api/auth/verify-otp` - OTP verification
- `POST /api/auth/forgot-password` - Password reset request
- `POST /api/auth/reset-password` - Password reset

### User Endpoints
- `GET /api/users/profile` - Get user profile
- `PUT /api/users/profile` - Update user profile
- `PUT /api/users/change-password` - Change password
- `POST /api/users/upload-photo` - Upload profile photo
- `GET /api/users/voting-status` - Get voting status

### Voting Endpoints
- `GET /api/voting/elections` - Get active elections
- `GET /api/voting/elections/:id/candidates` - Get election candidates
- `POST /api/voting/cast-vote` - Cast a vote
- `GET /api/voting/results/:id` - Get election results
- `GET /api/voting/history` - Get voting history
- `GET /api/voting/verify/:id` - Verify vote hash

### Admin Endpoints
- `GET /api/admin/pending-users` - Get pending user approvals
- `POST /api/admin/approve-user/:id` - Approve user
- `POST /api/admin/reject-user/:id` - Reject user
- `POST /api/admin/elections` - Create election
- `PUT /api/admin/elections/:id` - Update election
- `GET /api/admin/elections/:id/stats` - Get election statistics

### Candidate Endpoints
- `GET /api/candidates` - Get all candidates
- `GET /api/candidates/:id` - Get candidate details
- `GET /api/candidates/election/:id` - Get election candidates
- `PUT /api/candidates/profile/:id` - Update candidate profile

## Project Structure
```
├── models/                 # MongoDB schemas
│   ├── User.js
│   ├── Candidate.js
│   ├── Election.js
│   └── Vote.js
├── routes/                 # API routes
│   ├── auth.js
│   ├── users.js
│   ├── candidates.js
│   ├── voting.js
│   └── admin.js
├── services/               # Business logic
│   ├── emailService.js
│   ├── faceRecognitionService.js
│   └── blockchainService.js
├── src/                    # Frontend assets
│   └── main/
│       └── resources/
│           ├── static/     # CSS, JS, images
│           └── templates/  # HTML templates
├── server.js               # Main server file
├── package.json            # Dependencies
└── env.example            # Environment variables
```

## Testing

### Test Services
```bash
# Test face recognition service
curl http://localhost:4000/api/test/face-recognition

# Test blockchain service
curl http://localhost:4000/api/test/blockchain

# Test email service
curl http://localhost:4000/api/test/email
```

### Test API Endpoints
Use tools like Postman or curl to test the API endpoints with proper authentication headers.

## Deployment

### Production Environment
1. Set `NODE_ENV=production` in `.env`
2. Use a production MongoDB instance
3. Configure proper SMTP settings
4. Set strong JWT secrets
5. Use HTTPS in production
6. Configure proper CORS settings

### Docker Deployment
```bash
# Build Docker image
docker build -t voting-system .

# Run container
docker run -p 4000:4000 --env-file .env voting-system
```

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests if applicable
5. Submit a pull request

## License

This project is licensed under the MIT License.

## Authors

- [Gunjan Ganguly](https://github.com/Themysticlees)
- [Rajdeep Sarkar](https://github.com/Speak2Rajdeep)
- [Sourav Bhadra](https://github.com/Sourav-Bhadra)
- [Arpan Saha](https://github.com/ArpanSaha08)

## Support

If you have any questions or need support, please reach out to us at gunjanganguly12@gmail.com

## Demo

(Will be Uploaded Later)

## Feedback

If you have any feedback, please reach out to us at gunjanganguly12@gmail.com

