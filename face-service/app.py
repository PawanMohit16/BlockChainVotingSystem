import os
import base64
import numpy as np
import face_recognition
from flask import Flask, request, jsonify
from flask_cors import CORS
from werkzeug.utils import secure_filename
from PIL import Image
import io
import cv2
from dotenv import load_dotenv
import requests
import json

# Load environment variables
load_dotenv()

app = Flask(__name__)
CORS(app)

# Configuration
UPLOAD_FOLDER = 'uploads'
ALLOWED_EXTENSIONS = {'png', 'jpg', 'jpeg'}

# MongoDB API endpoints (Spring Boot backend)
MONGODB_API_BASE = os.environ.get('MONGODB_API_BASE', 'http://localhost:8080/api/face')

# Ensure upload folder exists
os.makedirs(UPLOAD_FOLDER, exist_ok=True)

app.config['UPLOAD_FOLDER'] = UPLOAD_FOLDER
app.config['MAX_CONTENT_LENGTH'] = 16 * 1024 * 1024  # 16MB max file size

def allowed_file(filename):
    return '.' in filename and \
           filename.rsplit('.', 1)[1].lower() in ALLOWED_EXTENSIONS

def image_to_encoding(image_path):
    """Convert an image to face encoding"""
    try:
        # Load the image
        image = face_recognition.load_image_file(image_path)
        
        # Find face encodings
        encodings = face_recognition.face_encodings(image)
        
        if len(encodings) > 0:
            return encodings[0].tolist()
        return None
    except Exception as e:
        print(f"Error in image_to_encoding: {str(e)}")
        return None

def image_to_base64(image_path):
    """Convert image to base64 string"""
    try:
        with open(image_path, "rb") as image_file:
            return base64.b64encode(image_file.read()).decode('utf-8')
    except Exception as e:
        print(f"Error converting image to base64: {str(e)}")
        return None

def save_face_data_to_mongodb(username, face_image_base64, face_encoding, image_type):
    """Save face data to MongoDB via Spring Boot API"""
    try:
        face_data = {
            "username": username,
            "faceImageBase64": face_image_base64,
            "faceEncoding": face_encoding,
            "imageType": image_type
        }
        
        response = requests.post(f"{MONGODB_API_BASE}/save", 
                               json=face_data,
                               headers={'Content-Type': 'application/json'})
        
        if response.status_code == 200:
            return response.json()
        else:
            print(f"Error saving to MongoDB: {response.status_code} - {response.text}")
            return None
    except Exception as e:
        print(f"Error saving face data to MongoDB: {str(e)}")
        return None

def get_face_data_from_mongodb(username):
    """Get face data from MongoDB via Spring Boot API"""
    try:
        response = requests.get(f"{MONGODB_API_BASE}/user/{username}/latest")
        
        if response.status_code == 200:
            return response.json()
        elif response.status_code == 404:
            return None
        else:
            print(f"Error retrieving from MongoDB: {response.status_code} - {response.text}")
            return None
    except Exception as e:
        print(f"Error retrieving face data from MongoDB: {str(e)}")
        return None

def compare_faces(known_encoding, unknown_encoding, tolerance=0.6):
    """Compare two face encodings and return True if they match"""
    if known_encoding is None or unknown_encoding is None:
        return False
        
    # Convert lists back to numpy arrays if they're not already
    if isinstance(known_encoding, list):
        known_encoding = np.array(known_encoding)
    if isinstance(unknown_encoding, list):
        unknown_encoding = np.array(unknown_encoding)
    
    # Calculate face distance
    face_distance = face_recognition.face_distance([known_encoding], unknown_encoding)
    return face_distance[0] <= tolerance

@app.route('/api/face/register', methods=['POST'])
def register_face():
    """
    Register a face for a user and store in MongoDB
    Expects form data with file and username
    """
    try:
        if 'file' not in request.files:
            return jsonify({
                'success': False,
                'error': 'No file part'
            }), 400
            
        file = request.files['file']
        username = request.form.get('username')
        
        if not username:
            return jsonify({
                'success': False,
                'error': 'Username is required'
            }), 400
        
        if file.filename == '':
            return jsonify({
                'success': False,
                'error': 'No selected file'
            }), 400
            
        if file and allowed_file(file.filename):
            filename = secure_filename(file.filename)
            filepath = os.path.join(app.config['UPLOAD_FOLDER'], filename)
            file.save(filepath)
            
            # Get face encoding
            encoding = image_to_encoding(filepath)
            
            if encoding is None:
                # Clean up the uploaded file
                try:
                    os.remove(filepath)
                except:
                    pass
                return jsonify({
                    'success': False,
                    'error': 'Could not detect a face in the image'
                }), 400
            
            # Convert image to base64
            image_base64 = image_to_base64(filepath)
            
            if image_base64 is None:
                # Clean up the uploaded file
                try:
                    os.remove(filepath)
                except:
                    pass
                return jsonify({
                    'success': False,
                    'error': 'Could not process the image'
                }), 400
            
            # Get image type
            image_type = filename.rsplit('.', 1)[1].lower()
            
            # Save to MongoDB
            saved_face_data = save_face_data_to_mongodb(username, image_base64, encoding, image_type)
            
            # Clean up the uploaded file
            try:
                os.remove(filepath)
            except:
                pass
            
            if saved_face_data:
                return jsonify({
                    'success': True,
                    'message': f'Face registered successfully for user {username}',
                    'faceDataId': saved_face_data.get('id')
                })
            else:
                return jsonify({
                    'success': False,
                    'error': 'Failed to save face data to database'
                }), 500
        else:
            return jsonify({
                'success': False,
                'error': 'Invalid file type'
            }), 400
                
    except Exception as e:
        return jsonify({
            'success': False,
            'error': f'Error processing image: {str(e)}'
        }), 500

@app.route('/api/face/verify', methods=['POST'])
def verify_face():
    """
    Verify if the uploaded face matches the stored face for a user
    Expects JSON with base64 encoded image and username
    """
    try:
        data = request.get_json()
        
        if not data or 'image' not in data or 'username' not in data:
            return jsonify({
                'success': False,
                'error': 'Missing required fields: image and username are required'
            }), 400
        
        username = data['username']
        
        # Get stored face data from MongoDB
        stored_face_data = get_face_data_from_mongodb(username)
        
        if not stored_face_data:
            return jsonify({
                'success': False,
                'error': f'No face data found for user {username}'
            }), 404
        
        # Get base64 image data
        image_data = data['image']
        if 'base64,' in image_data:
            # Handle data URL
            image_data = image_data.split('base64,')[1]
        
        # Decode base64 image
        image_bytes = base64.b64decode(image_data)
        
        # Convert to numpy array
        nparr = np.frombuffer(image_bytes, np.uint8)
        image = cv2.imdecode(nparr, cv2.IMREAD_COLOR)
        
        # Convert to RGB (face_recognition uses RGB)
        rgb_image = cv2.cvtColor(image, cv2.COLOR_BGR2RGB)
        
        # Find face locations
        face_locations = face_recognition.face_locations(rgb_image)
        
        if not face_locations:
            return jsonify({
                'success': False,
                'error': 'No face detected in the uploaded image'
            }), 400
        
        # Get face encodings
        face_encodings = face_recognition.face_encodings(rgb_image, face_locations)
        
        if not face_encodings:
            return jsonify({
                'success': False,
                'error': 'Could not extract face features'
            }), 400
        
        # Get the first face
        unknown_encoding = face_encodings[0]
        
        # Get stored encoding
        known_encoding = stored_face_data.get('faceEncoding')
        
        if not known_encoding:
            return jsonify({
                'success': False,
                'error': 'No face encoding found in stored data'
            }), 400
        
        # Compare faces
        match = compare_faces(known_encoding, unknown_encoding)
        
        return jsonify({
            'success': True,
            'match': match,
            'face_location': face_locations[0],  # Return the location of the face
            'username': username
        })
        
    except Exception as e:
        return jsonify({
            'success': False,
            'error': f'Error processing image: {str(e)}'
        }), 500

@app.route('/api/face/encode', methods=['POST'])
def encode_face():
    """
    Encode a face from an uploaded image (without storing)
    Returns the face encoding
    """
    try:
        if 'file' not in request.files:
            return jsonify({
                'success': False,
                'error': 'No file part'
            }), 400
            
        file = request.files['file']
        
        if file.filename == '':
            return jsonify({
                'success': False,
                'error': 'No selected file'
            }), 400
            
        if file and allowed_file(file.filename):
            filename = secure_filename(file.filename)
            filepath = os.path.join(app.config['UPLOAD_FOLDER'], filename)
            file.save(filepath)
            
            # Get face encoding
            encoding = image_to_encoding(filepath)
            
            # Clean up the uploaded file
            try:
                os.remove(filepath)
            except:
                pass
                
            if encoding is not None:
                return jsonify({
                    'success': True,
                    'encoding': encoding
                })
            else:
                return jsonify({
                    'success': False,
                    'error': 'Could not detect a face in the image'
                }), 400
                
    except Exception as e:
        return jsonify({
            'success': False,
            'error': f'Error processing image: {str(e)}'
        }), 500

@app.route('/api/face/user/<username>', methods=['GET'])
def get_user_face_data(username):
    """
    Get face data for a specific user
    """
    try:
        face_data = get_face_data_from_mongodb(username)
        
        if face_data:
            # Don't return the full base64 image for security, just metadata
            return jsonify({
                'success': True,
                'username': face_data.get('username'),
                'imageType': face_data.get('imageType'),
                'timestamp': face_data.get('timestamp'),
                'hasEncoding': face_data.get('faceEncoding') is not None
            })
        else:
            return jsonify({
                'success': False,
                'error': f'No face data found for user {username}'
            }), 404
            
    except Exception as e:
        return jsonify({
            'success': False,
            'error': f'Error retrieving face data: {str(e)}'
        }), 500

@app.route('/api/health', methods=['GET'])
def health_check():
    """Health check endpoint"""
    return jsonify({
        'status': 'ok',
        'service': 'face-recognition-mongodb',
        'version': '2.0.0',
        'mongodb_api_base': MONGODB_API_BASE
    })

if __name__ == '__main__':
    port = int(os.environ.get('PORT', 5001))
    app.run(host='0.0.0.0', port=port, debug=os.environ.get('FLASK_DEBUG', 'false').lower() == 'true')
