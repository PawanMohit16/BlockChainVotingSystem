const express = require('express');
const router = express.Router();
const {
  castVote,
  getResults,
  getUserVote,
} = require('../controllers/votes');

const { protect, authorize } = require('../middleware/auth');

// All routes are protected
router.use(protect);

router
  .route('/')
  .post(castVote)
  .get(authorize('admin'), getResults);

router.get('/my-vote', getUserVote);

module.exports = router;
