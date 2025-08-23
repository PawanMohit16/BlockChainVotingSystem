const express = require('express');
const router = express.Router();
const {
  getCandidates,
  getCandidate,
  createCandidate,
  updateCandidate,
  deleteCandidate,
  candidatePhotoUpload,
} = require('../controllers/candidates');

const { protect, authorize } = require('../middleware/auth');
const advancedResults = require('../middleware/advancedResults');
const Candidate = require('../models/Candidate');

// All routes are protected
router.use(protect);

// Only admins can create, update, or delete candidates
router
  .route('/')
  .get(
    advancedResults(Candidate, [
      { path: 'votes.user', select: 'name email' },
    ]),
    getCandidates
  )
  .post(authorize('admin'), createCandidate);

router
  .route('/:id')
  .get(getCandidate)
  .put(authorize('admin'), updateCandidate)
  .delete(authorize('admin'), deleteCandidate);

router
  .route('/:id/photo')
  .put(authorize('admin'), candidatePhotoUpload);

module.exports = router;
