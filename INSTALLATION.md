# Installation Guide - Secure Online Voting System

This guide will help you set up and run the Secure Online Voting System on your local machine.

## Prerequisites

Before you begin, ensure you have the following installed:

### 1. Node.js
- **Version**: 14.0.0 or higher
- **Download**: [https://nodejs.org/](https://nodejs.org/)
- **Verify**: Run `node --version` in terminal

### 2. MongoDB
- **Version**: 4.4 or higher
- **Download**: [https://www.mongodb.com/try/download/community](https://www.mongodb.com/try/download/community)
- **Installation Guide**: [MongoDB Installation](https://docs.mongodb.com/manual/installation/)

### 3. Git (Optional)
- **Download**: [https://git-scm.com/](https://git-scm.com/)

## Quick Start (Windows)

### Option 1: Using the Batch File
1. Double-click `start.bat`
2. The script will automatically:
   - Check prerequisites
   - Install dependencies
   - Start the application

### Option 2: Manual Installation
Follow the steps below for manual installation.

## Manual Installation Steps

### Step 1: Clone/Download the Project
```bash
# If using Git
git clone https://github.com/yourusername/Secure-Online-Voting-System-using-Face-Recognition-and-BlockChain.git

# Or download and extract the ZIP file
```

### Step 2: Navigate to Project Directory
```bash
cd Secure-Online-Voting-System-using-Face-Recognition-and-BlockChain
```

### Step 3: Install Dependencies
```bash
npm install
```

### Step 4: Environment Configuration
1. Copy the environment example file:
   ```bash
   copy env.example .env
   ```

2. Edit `.env` file with your configuration:
   ```env
   # Server Configuration
   PORT=4000
   NODE_ENV=development

   # MongoDB Configuration
   MONGODB_URI=mongodb://localhost:27017/voting_system

   # JWT Configuration
   JWT_SECRET=your-super-secret-jwt-key-change-this-in-production

   # Email Configuration (Gmail Example)
   SMTP_HOST=smtp.gmail.com
   SMTP_PORT=587
   SMTP_USER=your-email@gmail.com
   SMTP_PASS=your-app-password

   # Frontend URL
   FRONTEND_URL=http://localhost:3000
   ```

### Step 5: Start MongoDB
```bash
# Windows
net start MongoDB

# macOS/Linux
sudo systemctl start mongod

# Or start MongoDB Compass (GUI)
```

### Step 6: Create Database
```bash
# Connect to MongoDB
mongosh

# Create and use database
use voting_system

# Exit MongoDB shell
exit
```

### Step 7: Download Face Recognition Models
1. Create the models directory:
   ```bash
   mkdir models\face-api
   ```

2. Download models from: [face-api.js weights](https://github.com/justadudewhohacks/face-api.js/tree/master/weights)
   - `tiny_face_detector_model-weights_manifest.json`
   - `tiny_face_detector_model-shard1`
   - `face_landmark_68_model-weights_manifest.json`
   - `face_landmark_68_model-shard1`
   - `face_recognition_model-weights_manifest.json`
   - `face_recognition_model-shard1`
   - `face_expression_model-weights_manifest.json`
   - `face_expression_model-shard1`

3. Place all files in `models\face-api\` directory

### Step 8: Run the Application
```bash
# Development mode (with auto-restart)
npm run dev

# Production mode
npm start
```

### Step 9: Access the Application
- **Main Application**: http://localhost:4000
- **Health Check**: http://localhost:4000/api/health
- **Test Endpoints**: http://localhost:4000/api/test/

## Troubleshooting

### Common Issues

#### 1. MongoDB Connection Error
```
MongoDB connection error: connect ECONNREFUSED 127.0.0.1:27017
```
**Solution**: Ensure MongoDB service is running
```bash
# Windows
net start MongoDB

# Check MongoDB status
sc query MongoDB
```

#### 2. Port Already in Use
```
Error: listen EADDRINUSE: address already in use :::4000
```
**Solution**: Change port in `.env` file or kill the process using port 4000
```bash
# Find process using port 4000
netstat -ano | findstr :4000

# Kill process (replace PID with actual process ID)
taskkill /PID <PID> /F
```

#### 3. Module Not Found Errors
```
Cannot find module 'face-api.js'
```
**Solution**: Ensure all dependencies are installed
```bash
npm install
# or
npm ci
```

#### 4. Face Recognition Models Not Found
```
Failed to initialize face recognition service
```
**Solution**: Ensure face-api.js models are downloaded and placed in `models/face-api/` directory

#### 5. Email Service Errors
```
Failed to send OTP email
```
**Solution**: Check SMTP configuration in `.env` file
- Verify email credentials
- Enable "Less secure app access" for Gmail
- Use App Password for Gmail 2FA accounts

### Performance Issues

#### 1. Slow Face Recognition
- Ensure face-api.js models are properly loaded
- Check system resources (CPU/RAM)
- Consider using GPU acceleration if available

#### 2. Database Performance
- Ensure MongoDB indexes are created
- Monitor database connection pool
- Consider MongoDB Atlas for production

## Development Mode

### Auto-restart on File Changes
```bash
npm run dev
```

### Debug Mode
```bash
# Set debug environment variable
set DEBUG=* && npm run dev

# Or add to .env file
DEBUG=*
```

### Logging
- Application logs are displayed in the console
- Check MongoDB logs for database issues
- Monitor system resources during development

## Production Deployment

### Environment Variables
```env
NODE_ENV=production
PORT=4000
MONGODB_URI=mongodb://your-production-mongodb-uri
JWT_SECRET=your-production-jwt-secret
SMTP_HOST=your-production-smtp-host
SMTP_USER=your-production-email
SMTP_PASS=your-production-password
```

### Security Considerations
- Use strong JWT secrets
- Enable HTTPS in production
- Configure proper CORS settings
- Set up rate limiting
- Use environment-specific configurations

### Monitoring
- Set up application monitoring (PM2, New Relic, etc.)
- Monitor MongoDB performance
- Set up error tracking (Sentry, etc.)
- Configure logging aggregation

## Support

If you encounter issues not covered in this guide:

1. Check the [README.md](README.md) for additional information
2. Review the console logs for error messages
3. Verify all prerequisites are properly installed
4. Check MongoDB and Node.js versions compatibility
5. Contact support at: gunjanganguly12@gmail.com

## Next Steps

After successful installation:

1. **Test the System**: Use the test endpoints to verify all services
2. **Create Admin User**: Set up the first admin account
3. **Configure Elections**: Create test elections and candidates
4. **Test Voting Flow**: Register users and test the complete voting process
5. **Customize**: Modify the system according to your requirements

Happy Voting! 🗳️
