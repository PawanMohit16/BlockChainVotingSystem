const Candidate = require('../models/Candidate');
const ErrorResponse = require('../utils/errorResponse');
const asyncHandler = require('../middleware/async');
const path = require('path');

// @desc    Get all candidates
// @route   GET /api/v1/candidates
// @access  Public
exports.getCandidates = asyncHandler(async (req, res, next) => {
  res.status(200).json(res.advancedResults);
});

// @desc    Get single candidate
// @route   GET /api/v1/candidates/:id
// @access  Public
exports.getCandidate = asyncHandler(async (req, res, next) => {
  const candidate = await Candidate.findById(req.params.id);

  if (!candidate) {
    return next(
      new ErrorResponse(`Candidate not found with id of ${req.params.id}`, 404)
    );
  }

  res.status(200).json({
    success: true,
    data: candidate,
  });
});

// @desc    Create candidate
// @route   POST /api/v1/candidates
// @access  Private/Admin
exports.createCandidate = asyncHandler(async (req, res, next) => {
  const candidate = await Candidate.create(req.body);

  res.status(201).json({
    success: true,
    data: candidate,
  });
});

// @desc    Update candidate
// @route   PUT /api/v1/candidates/:id
// @access  Private/Admin
exports.updateCandidate = asyncHandler(async (req, res, next) => {
  const candidate = await Candidate.findByIdAndUpdate(req.params.id, req.body, {
    new: true,
    runValidators: true,
  });

  if (!candidate) {
    return next(
      new ErrorResponse(`Candidate not found with id of ${req.params.id}`, 404)
    );
  }

  res.status(200).json({
    success: true,
    data: candidate,
  });
});

// @desc    Delete candidate
// @route   DELETE /api/v1/candidates/:id
// @access  Private/Admin
exports.deleteCandidate = asyncHandler(async (req, res, next) => {
  const candidate = await Candidate.findById(req.params.id);

  if (!candidate) {
    return next(
      new ErrorResponse(`Candidate not found with id of ${req.params.id}`, 404)
    );
  }

  // Remove photo file if exists
  if (candidate.photo) {
    const filePath = path.join(
      __dirname,
      '..',
      '..',
      'uploads',
      'candidates',
      candidate.photo
    );
    if (fs.existsSync(filePath)) {
      fs.unlinkSync(filePath);
    }
  }

  await candidate.remove();

  res.status(200).json({
    success: true,
    data: {},
  });
});

// @desc    Upload photo for candidate
// @route   PUT /api/v1/candidates/:id/photo
// @access  Private/Admin
exports.candidatePhotoUpload = asyncHandler(async (req, res, next) => {
  const candidate = await Candidate.findById(req.params.id);

  if (!candidate) {
    return next(
      new ErrorResponse(`Candidate not found with id of ${req.params.id}`, 404)
    );
  }

  if (!req.files) {
    return next(new ErrorResponse(`Please upload a file`, 400));
  }

  const file = req.files.file;

  // Make sure the image is a photo
  if (!file.mimetype.startsWith('image')) {
    return next(new ErrorResponse(`Please upload an image file`, 400));
  }

  // Check filesize
  if (file.size > process.env.MAX_FILE_UPLOAD) {
    return next(
      new ErrorResponse(
        `Please upload an image less than ${process.env.MAX_FILE_UPLOAD}`,
        400
      )
    );
  }

  // Create custom filename
  file.name = `candidate_${candidate._id}${path.parse(file.name).ext}`;

  file.mv(
    `${process.env.FILE_UPLOAD_PATH}/candidates/${file.name}`,
    async (err) => {
      if (err) {
        console.error(err);
        return next(new ErrorResponse(`Problem with file upload`, 500));
      }

      await Candidate.findByIdAndUpdate(req.params.id, { photo: file.name });

      res.status(200).json({
        success: true,
        data: file.name,
      });
    }
  );
});
