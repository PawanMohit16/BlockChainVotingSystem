const express = require('express');
const mongoose = require('mongoose');
const cors = require('cors');
const helmet = require('helmet');
const rateLimit = require('express-rate-limit');
const path = require('path');
require('dotenv').config();

// Import routes
const authRoutes = require('./routes/auth');
const userRoutes = require('./routes/users');
const candidateRoutes = require('./routes/candidates');
const votingRoutes = require('./routes/voting');
const adminRoutes = require('./routes/admin');

// Import services
const FaceRecognitionService = require('./services/faceRecognitionService');
const BlockchainService = require('./services/blockchainService');
const EmailService = require('./services/emailService');

const app = express();
const PORT = process.env.PORT || 4000;

// Security middleware
app.use(helmet());
app.use(cors());

// Rate limiting
const limiter = rateLimit({
  windowMs: 15 * 60 * 1000, // 15 minutes
  max: 100 // limit each IP to 100 requests per windowMs
});
app.use(limiter);

// Body parsing middleware
app.use(express.json({ limit: '50mb' }));
app.use(express.urlencoded({ extended: true, limit: '50mb' }));

// Static files
app.use(express.static(path.join(__dirname, 'src/main/resources/static')));

// Database connection
mongoose.connect(process.env.MONGODB_URI || 'mongodb://localhost:27017/voting_system', {
  useNewUrlParser: true,
  useUnifiedTopology: true,
})
.then(() => console.log('Connected to MongoDB'))
.catch(err => console.error('MongoDB connection error:', err));

// Routes
app.use('/api/auth', authRoutes);
app.use('/api/users', userRoutes);
app.use('/api/candidates', candidateRoutes);
app.use('/api/voting', votingRoutes);
app.use('/api/admin', adminRoutes);

// Test endpoints for services
app.get('/api/test/face-recognition', async (req, res) => {
  try {
    const result = await FaceRecognitionService.test();
    res.json(result);
  } catch (error) {
    res.status(500).json({ error: error.message });
  }
});

app.get('/api/test/blockchain', async (req, res) => {
  try {
    const result = await BlockchainService.test();
    res.json(result);
  } catch (error) {
    res.status(500).json({ error: error.message });
  }
});

app.get('/api/test/email', async (req, res) => {
  try {
    const result = await EmailService.testConnection();
    res.json({ success: result, message: 'Email service test completed' });
  } catch (error) {
    res.status(500).json({ error: error.message });
  }
});

// Health check endpoint
app.get('/api/health', (req, res) => {
  res.json({
    status: 'OK',
    timestamp: new Date().toISOString(),
    uptime: process.uptime(),
    environment: process.env.NODE_ENV || 'development'
  });
});

// Serve HTML templates
app.get('/', (req, res) => {
  res.sendFile(path.join(__dirname, 'src/main/resources/templates/index.html'));
});

app.get('/register', (req, res) => {
  res.sendFile(path.join(__dirname, 'src/main/resources/templates/register_new.html'));
});

app.get('/login', (req, res) => {
  res.sendFile(path.join(__dirname, 'src/main/resources/templates/account.html'));
});

app.get('/admin', (req, res) => {
  res.sendFile(path.join(__dirname, 'src/main/resources/templates/admin.html'));
});

app.get('/vote', (req, res) => {
  res.sendFile(path.join(__dirname, 'src/main/resources/templates/vote.html'));
});

// Error handling middleware
app.use((err, req, res, next) => {
  console.error(err.stack);
  res.status(500).json({ message: 'Something went wrong!' });
});

// 404 handler
app.use((req, res) => {
  res.status(404).json({ message: 'Route not found' });
});

app.listen(PORT, () => {
  console.log(`🚀 Server running on port ${PORT}`);
  console.log(`📍 Visit: http://localhost:${PORT}`);
  console.log(`🔍 Health check: http://localhost:${PORT}/api/health`);
  console.log(`🧪 Test endpoints: http://localhost:${PORT}/api/test/`);
});
