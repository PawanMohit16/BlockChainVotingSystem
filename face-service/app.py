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

# Load environment variables
load_dotenv()

app = Flask(__name__)
CORS(app)

# Configuration
UPLOAD_FOLDER = 'uploads'
ALLOWED_EXTENSIONS = {'png', 'jpg', 'jpeg'}

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

@app.route('/api/face/verify', methods=['POST'])
def verify_face():
    """
    Verify if the uploaded face matches the known face
    Expects a JSON with base64 encoded image and known encoding
    """
    try:
        data = request.get_json()
        
        if not data or 'image' not in data or 'known_encoding' not in data:
            return jsonify({
                'success': False,
                'error': 'Missing required fields: image and known_encoding are required'
            }), 400
        
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
        
        # Get known encoding from request
        known_encoding = data['known_encoding']
        
        # Compare faces
        match = compare_faces(known_encoding, unknown_encoding)
        
        return jsonify({
            'success': True,
            'match': match,
            'face_location': face_locations[0]  # Return the location of the face
        })
        
    except Exception as e:
        return jsonify({
            'success': False,
            'error': f'Error processing image: {str(e)}'
        }), 500

@app.route('/api/face/encode', methods=['POST'])
def encode_face():
    """
    Encode a face from an uploaded image
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

@app.route('/api/health', methods=['GET'])
def health_check():
    """Health check endpoint"""
    return jsonify({
        'status': 'ok',
        'service': 'face-recognition',
        'version': '1.0.0'
    })

if __name__ == '__main__':
    port = int(os.environ.get('PORT', 5001))
    app.run(host='0.0.0.0', port=port, debug=os.environ.get('FLASK_DEBUG', 'false').lower() == 'true')
