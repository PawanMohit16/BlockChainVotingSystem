# MongoDB-Based Face Storage System

This system stores face images and encodings directly in MongoDB instead of just storing file paths as strings.

## Overview

The new system consists of:

1. **FaceData Model** - MongoDB document to store face data
2. **FaceData Repository** - Database operations for face data
3. **FaceData Service** - Business logic for face data operations
4. **FaceData Controller** - REST API endpoints for face data
5. **Updated Face Service** - Python service that integrates with MongoDB

## Architecture

```
┌─────────────────┐    ┌──────────────────┐    ┌─────────────────┐
│   Frontend      │    │   Face Service   │    │  Spring Boot    │
│   (React/HTML)  │◄──►│   (Python/Flask) │◄──►│   (Java)        │
└─────────────────┘    └──────────────────┘    └─────────────────┘
                                                         │
                                                         ▼
                                              ┌─────────────────┐
                                              │   MongoDB       │
                                              │   (face_data    │
                                              │   collection)   │
                                              └─────────────────┘
```

## Data Structure

### FaceData Document
```json
{
  "_id": "507f1f77bcf86cd799439011",
  "username": "user123",
  "faceImageBase64": "data:image/jpeg;base64,/9j/4AAQSkZJRgABAQAAAQ...",
  "faceEncoding": [0.123, -0.456, 0.789, ...],
  "imageType": "jpg",
  "timestamp": 1640995200000
}
```

## API Endpoints

### Spring Boot API (MongoDB Operations)

#### Save Face Data
```
POST /api/face/save
Content-Type: application/json

{
  "username": "user123",
  "faceImageBase64": "data:image/jpeg;base64,...",
  "faceEncoding": [0.123, -0.456, ...],
  "imageType": "jpg"
}
```

#### Get Face Data by Username
```
GET /api/face/user/{username}
```

#### Get Latest Face Data by Username
```
GET /api/face/user/{username}/latest
```

#### Check if User Has Face Data
```
GET /api/face/user/{username}/exists
```

#### Delete Face Data by Username
```
DELETE /api/face/user/{username}
```

### Face Service API (Face Recognition Operations)

#### Register Face
```
POST /api/face/register
Content-Type: multipart/form-data

file: [image file]
username: user123
```

#### Verify Face
```
POST /api/face/verify
Content-Type: application/json

{
  "image": "data:image/jpeg;base64,...",
  "username": "user123"
}
```

#### Get User Face Data
```
GET /api/face/user/{username}
```

## Setup Instructions

### 1. Start Spring Boot Application
```bash
# From project root
mvn spring-boot:run
```

### 2. Start Face Service
```bash
# From face-service directory
cd face-service
pip install -r requirements.txt
python app.py
```

### 3. Test the System
```bash
# From face-service directory
python test_mongodb_face.py
```

## Usage Examples

### Register a Face
```python
import requests

# Register face for a user
files = {'file': open('user_photo.jpg', 'rb')}
data = {'username': 'john_doe'}

response = requests.post('http://localhost:5001/api/face/register', 
                        files=files, data=data)

if response.status_code == 200:
    print("Face registered successfully!")
    print(f"Face Data ID: {response.json()['faceDataId']}")
```

### Verify a Face
```python
import requests
import base64

# Convert image to base64
with open('test_photo.jpg', 'rb') as image_file:
    image_base64 = base64.b64encode(image_file.read()).decode('utf-8')

# Verify face
verification_data = {
    'image': f"data:image/jpeg;base64,{image_base64}",
    'username': 'john_doe'
}

response = requests.post('http://localhost:5001/api/face/verify', 
                        json=verification_data)

if response.status_code == 200:
    result = response.json()
    if result['match']:
        print("Face verification successful!")
    else:
        print("Face verification failed!")
```

### Get User Face Data
```python
import requests

# Get face data for a user
response = requests.get('http://localhost:5001/api/face/user/john_doe')

if response.status_code == 200:
    face_data = response.json()
    print(f"User: {face_data['username']}")
    print(f"Image Type: {face_data['imageType']}")
    print(f"Has Encoding: {face_data['hasEncoding']}")
```

## Benefits of MongoDB Storage

1. **Complete Data Storage**: Face images and encodings are stored in the database
2. **No File System Dependency**: No need to manage file paths or file system storage
3. **Scalability**: MongoDB can handle large amounts of face data efficiently
4. **Backup and Recovery**: Face data is included in database backups
5. **Consistency**: All user data (including face data) is in one place
6. **Security**: Face data is stored securely in the database with proper access controls

## Security Considerations

1. **Base64 Encoding**: Face images are stored as base64 strings in MongoDB
2. **Access Control**: API endpoints should be protected with authentication
3. **Data Privacy**: Face data should be encrypted at rest
4. **Retention Policy**: Implement data retention policies for face data
5. **Audit Logging**: Log access to face data for security auditing

## Performance Considerations

1. **Image Size**: Limit image size to reasonable dimensions (e.g., 640x480)
2. **Compression**: Consider compressing images before base64 encoding
3. **Indexing**: Create indexes on username and timestamp fields
4. **Caching**: Consider caching frequently accessed face encodings
5. **Cleanup**: Implement cleanup procedures for old face data

## Troubleshooting

### Common Issues

1. **MongoDB Connection**: Ensure MongoDB is running and accessible
2. **Face Detection**: Ensure uploaded images contain clear, front-facing faces
3. **Image Format**: Supported formats are JPG, PNG, JPEG
4. **Service Communication**: Ensure both Spring Boot and Face services are running

### Debug Commands

```bash
# Check MongoDB connection
mongo --eval "db.face_data.find().limit(1)"

# Check Spring Boot logs
tail -f logs/spring-boot.log

# Check Face service logs
tail -f logs/face-service.log
```

## Migration from File-Based Storage

If migrating from the old file-based system:

1. **Export Existing Data**: Extract face images from file system
2. **Convert to Base64**: Convert images to base64 format
3. **Generate Encodings**: Generate face encodings for existing images
4. **Import to MongoDB**: Use the new API to import face data
5. **Update Frontend**: Update frontend to use new API endpoints
6. **Cleanup**: Remove old file-based storage code

## Future Enhancements

1. **Multiple Face Support**: Store multiple face encodings per user
2. **Face Quality Assessment**: Implement face quality scoring
3. **Automatic Updates**: Periodically update face encodings
4. **Face Aging**: Handle face changes over time
5. **Liveness Detection**: Add liveness detection to prevent spoofing
