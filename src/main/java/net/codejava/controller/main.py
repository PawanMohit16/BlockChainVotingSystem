#!/usr/bin/env python3
"""
Face Recognition Script for Online Voting System
This script performs face verification using OpenCV and face_recognition library
"""

import cv2
import face_recognition
import os
import sys
import numpy as np

def load_known_face(username):
    """Load the known face image for a given username"""
    # Path to user photos directory
    photos_dir = "user-photos"
    user_dir = os.path.join(photos_dir, username)
    
    if not os.path.exists(user_dir):
        return None
    
    # Look for image files in user directory
    image_files = [f for f in os.listdir(user_dir) if f.lower().endswith(('.jpg', '.jpeg', '.png'))]
    
    if not image_files:
        return None
    
    # Load the first image found
    image_path = os.path.join(user_dir, image_files[0])
    known_image = face_recognition.load_image_file(image_path)
    known_encoding = face_recognition.face_encodings(known_image)
    
    if not known_encoding:
        return None
    
    return known_encoding[0]

def capture_and_verify_face(username):
    """Capture live face and verify against stored image"""
    # Load known face
    known_encoding = load_known_face(username)
    
    if known_encoding is None:
        print("No known face found for user:", username)
        return False
    
    # Initialize camera
    cap = cv2.VideoCapture(0)
    
    if not cap.isOpened():
        print("Error: Could not open camera")
        return False
    
    print("Camera opened. Please look at the camera...")
    
    # Capture frame
    ret, frame = cap.read()
    cap.release()
    
    if not ret:
        print("Error: Could not capture frame")
        return False
    
    # Find faces in the captured frame
    face_locations = face_recognition.face_locations(frame)
    face_encodings = face_recognition.face_encodings(frame, face_locations)
    
    if not face_encodings:
        print("No face detected in captured image")
        return False
    
    # Compare faces
    for face_encoding in face_encodings:
        matches = face_recognition.compare_faces([known_encoding], face_encoding, tolerance=0.6)
        
        if matches[0]:
            print("Face verification successful!")
            return True
    
    print("Face verification failed!")
    return False

def main():
    """Main function"""
    if len(sys.argv) != 2:
        print("Usage: python main.py <username>")
        sys.exit(1)
    
    username = sys.argv[1]
    print(f"Starting face verification for user: {username}")
    
    # Perform face verification
    result = capture_and_verify_face(username)
    
    # Write result to output file
    output_file = "src/main/resources/templates/out.txt"
    os.makedirs(os.path.dirname(output_file), exist_ok=True)
    
    with open(output_file, 'w') as f:
        if result:
            f.write("Verification successful: 1")
        else:
            f.write("Verification failed: 0")
    
    print(f"Face verification result: {'SUCCESS' if result else 'FAILED'}")
    return 0 if result else 1

if __name__ == "__main__":
    sys.exit(main())
