const faceapi = require('face-api.js');
const canvas = require('canvas');
const path = require('path');

class FaceRecognitionService {
  constructor() {
    this.isInitialized = false;
    this.faceDetectionNet = null;
    this.faceApiOptions = null;
  }

  // Initialize face-api.js with models
  async initialize() {
    try {
      if (this.isInitialized) {
        return true;
      }

      // Set up canvas for face-api.js
      const { Canvas, Image, ImageData } = canvas;
      faceapi.env.monkeyPatch({ Canvas, Image, ImageData });

      // Load face detection models
      const modelPath = path.join(__dirname, '../models/face-api');
      
      await faceapi.nets.tinyFaceDetector.loadFromUri(modelPath);
      await faceapi.nets.faceLandmark68Net.loadFromUri(modelPath);
      await faceapi.nets.faceRecognitionNet.loadFromUri(modelPath);
      await faceapi.nets.faceExpressionNet.loadFromUri(modelPath);

      // Configure face detection options
      this.faceApiOptions = new faceapi.TinyFaceDetectorOptions({
        inputSize: 224,
        scoreThreshold: 0.5
      });

      this.isInitialized = true;
      console.log('Face recognition service initialized successfully');
      return true;
    } catch (error) {
      console.error('Failed to initialize face recognition service:', error);
      return false;
    }
  }

  // Convert base64 image to canvas
  async base64ToCanvas(base64Data) {
    try {
      // Remove data URL prefix if present
      const base64 = base64Data.replace(/^data:image\/[a-z]+;base64,/, '');
      
      // Create image from base64
      const img = new canvas.Image();
      img.src = Buffer.from(base64, 'base64');
      
      // Create canvas and draw image
      const canvas = new canvas.Canvas(img.width, img.height);
      const ctx = canvas.getContext('2d');
      ctx.drawImage(img, 0, 0);
      
      return canvas;
    } catch (error) {
      console.error('Failed to convert base64 to canvas:', error);
      throw new Error('Invalid image data');
    }
  }

  // Detect faces in an image
  async detectFaces(imageData) {
    try {
      if (!this.isInitialized) {
        await this.initialize();
      }

      const canvas = await this.base64ToCanvas(imageData);
      const detections = await faceapi.detectAllFaces(canvas, this.faceApiOptions);

      return {
        success: true,
        faceCount: detections.length,
        detections: detections
      };
    } catch (error) {
      console.error('Face detection failed:', error);
      return {
        success: false,
        error: error.message
      };
    }
  }

  // Extract face descriptors (features) from an image
  async extractFaceDescriptors(imageData) {
    try {
      if (!this.isInitialized) {
        await this.initialize();
      }

      const canvas = await this.base64ToCanvas(imageData);
      
      // Detect faces and extract descriptors
      const detections = await faceapi.detectAllFaces(canvas, this.faceApiOptions)
        .withFaceLandmarks()
        .withFaceDescriptors();

      if (detections.length === 0) {
        return {
          success: false,
          error: 'No faces detected in the image'
        };
      }

      if (detections.length > 1) {
        return {
          success: false,
          error: 'Multiple faces detected. Please provide an image with only one face.'
        };
      }

      const faceDescriptor = detections[0].descriptor;
      
      return {
        success: true,
        descriptor: Array.from(faceDescriptor),
        landmarks: detections[0].landmarks
      };
    } catch (error) {
      console.error('Face descriptor extraction failed:', error);
      return {
        success: false,
        error: error.message
      };
    }
  }

  // Verify face during registration
  async verifyFace(faceData) {
    try {
      // Check if face data is provided
      if (!faceData) {
        return {
          isValid: false,
          error: 'Face data is required'
        };
      }

      // Detect faces in the image
      const faceDetection = await this.detectFaces(faceData);
      if (!faceDetection.success) {
        return {
          isValid: false,
          error: faceDetection.error
        };
      }

      // Check if exactly one face is detected
      if (faceDetection.faceCount === 0) {
        return {
          isValid: false,
          error: 'No face detected in the image. Please provide a clear photo of your face.'
        };
      }

      if (faceDetection.faceCount > 1) {
        return {
          isValid: false,
          error: 'Multiple faces detected. Please provide an image with only your face.'
        };
      }

      // Extract face descriptors for quality check
      const descriptorResult = await this.extractFaceDescriptors(faceData);
      if (!descriptorResult.success) {
        return {
          isValid: false,
          error: descriptorResult.error
        };
      }

      // Basic quality checks
      const qualityCheck = this.checkImageQuality(faceData);
      if (!qualityCheck.isGood) {
        return {
          isValid: false,
          error: qualityCheck.error
        };
      }

      return {
        isValid: true,
        faceCount: faceDetection.faceCount,
        quality: qualityCheck.quality
      };
    } catch (error) {
      console.error('Face verification failed:', error);
      return {
        isValid: false,
        error: 'Face verification failed. Please try again with a different image.'
      };
    }
  }

  // Verify face during voting (compare with registered face)
  async verifyVotingFace(faceData, registeredFaceData) {
    try {
      // Extract descriptors from both images
      const currentDescriptor = await this.extractFaceDescriptors(faceData);
      const registeredDescriptor = await this.extractFaceDescriptors(registeredFaceData);

      if (!currentDescriptor.success || !registeredDescriptor.success) {
        return {
          isValid: false,
          error: 'Failed to process face images'
        };
      }

      // Calculate similarity between descriptors
      const similarity = this.calculateSimilarity(
        currentDescriptor.descriptor,
        registeredDescriptor.descriptor
      );

      // Threshold for face matching (adjust as needed)
      const threshold = 0.6;
      const isMatch = similarity > threshold;

      return {
        isValid: isMatch,
        similarity: similarity,
        threshold: threshold,
        isMatch: isMatch
      };
    } catch (error) {
      console.error('Voting face verification failed:', error);
      return {
        isValid: false,
        error: 'Face verification failed during voting'
      };
    }
  }

  // Calculate similarity between two face descriptors
  calculateSimilarity(descriptor1, descriptor2) {
    try {
      if (descriptor1.length !== descriptor2.length) {
        return 0;
      }

      // Calculate Euclidean distance
      let sumSquaredDiff = 0;
      for (let i = 0; i < descriptor1.length; i++) {
        const diff = descriptor1[i] - descriptor2[i];
        sumSquaredDiff += diff * diff;
      }
      
      const distance = Math.sqrt(sumSquaredDiff);
      
      // Convert distance to similarity (0 = no similarity, 1 = identical)
      // Normalize based on typical face descriptor distances
      const similarity = Math.max(0, 1 - (distance / 1.0));
      
      return similarity;
    } catch (error) {
      console.error('Similarity calculation failed:', error);
      return 0;
    }
  }

  // Check image quality
  checkImageQuality(imageData) {
    try {
      // Basic quality checks
      const base64 = imageData.replace(/^data:image\/[a-z]+;base64,/, '');
      const imageSize = Buffer.byteLength(base64, 'base64');
      
      // Check file size (minimum 10KB, maximum 5MB)
      if (imageSize < 10 * 1024) {
        return {
          isGood: false,
          error: 'Image file is too small. Please provide a higher quality image.',
          quality: 'poor'
        };
      }

      if (imageSize > 5 * 1024 * 1024) {
        return {
          isGood: false,
          error: 'Image file is too large. Please provide a smaller image.',
          quality: 'poor'
        };
      }

      // Additional quality checks can be added here
      // For example, checking image dimensions, brightness, etc.

      return {
        isGood: true,
        quality: 'good',
        size: imageSize
      };
    } catch (error) {
      console.error('Image quality check failed:', error);
      return {
        isGood: false,
        error: 'Failed to check image quality',
        quality: 'unknown'
      };
    }
  }

  // Get face analysis results
  async analyzeFace(imageData) {
    try {
      if (!this.isInitialized) {
        await this.initialize();
      }

      const canvas = await this.base64ToCanvas(imageData);
      
      // Detect faces with landmarks and expressions
      const detections = await faceapi.detectAllFaces(canvas, this.faceApiOptions)
        .withFaceLandmarks()
        .withFaceExpressions();

      if (detections.length === 0) {
        return {
          success: false,
          error: 'No faces detected'
        };
      }

      const analysis = detections.map(detection => ({
        boundingBox: detection.detection.box,
        landmarks: detection.landmarks,
        expressions: detection.expressions,
        dominantExpression: this.getDominantExpression(detection.expressions)
      }));

      return {
        success: true,
        faceCount: detections.length,
        analysis: analysis
      };
    } catch (error) {
      console.error('Face analysis failed:', error);
      return {
        success: false,
        error: error.message
      };
    }
  }

  // Get dominant expression from expression scores
  getDominantExpression(expressions) {
    try {
      let maxScore = 0;
      let dominantExpression = 'neutral';

      for (const [expression, score] of Object.entries(expressions)) {
        if (score > maxScore) {
          maxScore = score;
          dominantExpression = expression;
        }
      }

      return {
        expression: dominantExpression,
        confidence: maxScore
      };
    } catch (error) {
      console.error('Failed to get dominant expression:', error);
      return {
        expression: 'neutral',
        confidence: 0
      };
    }
  }

  // Test the service
  async test() {
    try {
      const initialized = await this.initialize();
      if (!initialized) {
        return {
          success: false,
          error: 'Failed to initialize face recognition service'
        };
      }

      return {
        success: true,
        message: 'Face recognition service is working properly',
        models: 'Loaded',
        options: 'Configured'
      };
    } catch (error) {
      console.error('Face recognition service test failed:', error);
      return {
        success: false,
        error: error.message
      };
    }
  }
}

module.exports = new FaceRecognitionService();
