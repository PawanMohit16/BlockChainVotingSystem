#!/usr/bin/env python3
"""
MongoDB Atlas Image Viewer
This script helps you view face images stored in MongoDB Atlas
"""

import requests
import base64
import json
import os
from datetime import datetime

# Configuration
SPRING_BOOT_URL = "http://localhost:4000/api/face"
FACE_SERVICE_URL = "http://localhost:5001/api/face"

def view_user_face_data(username):
    """View face data for a specific user"""
    print(f"🔍 Searching for face data for user: {username}")
    
    try:
        # Try Spring Boot API first
        response = requests.get(f"{SPRING_BOOT_URL}/user/{username}")
        
        if response.status_code == 200:
            data = response.json()
            if isinstance(data, list):
                face_data_list = data
            else:
                face_data_list = [data]
            
            print(f"✅ Found {len(face_data_list)} face record(s) for user: {username}")
            
            for i, face_data in enumerate(face_data_list, 1):
                print(f"\n📸 Face Record #{i}:")
                print(f"   ID: {face_data.get('id', 'N/A')}")
                print(f"   Username: {face_data.get('username', 'N/A')}")
                print(f"   Image Type: {face_data.get('imageType', 'N/A')}")
                print(f"   Timestamp: {datetime.fromtimestamp(face_data.get('timestamp', 0)/1000)}")
                print(f"   Has Encoding: {'Yes' if face_data.get('faceEncoding') else 'No'}")
                
                # Save image to file
                image_base64 = face_data.get('faceImageBase64', '')
                if image_base64:
                    save_base64_image(image_base64, f"{username}_face_{i}.jpg")
                else:
                    print("   ⚠️  No image data found")
                    
        else:
            print(f"❌ No face data found for user: {username}")
            
    except Exception as e:
        print(f"❌ Error: {str(e)}")

def view_all_face_data():
    """View all face data in the database"""
    print("🔍 Loading all face data...")
    
    try:
        response = requests.get(f"{SPRING_BOOT_URL}/all")
        
        if response.status_code == 200:
            face_data_list = response.json()
            print(f"✅ Found {len(face_data_list)} total face record(s)")
            
            for i, face_data in enumerate(face_data_list, 1):
                print(f"\n📸 Face Record #{i}:")
                print(f"   ID: {face_data.get('id', 'N/A')}")
                print(f"   Username: {face_data.get('username', 'N/A')}")
                print(f"   Image Type: {face_data.get('imageType', 'N/A')}")
                print(f"   Timestamp: {datetime.fromtimestamp(face_data.get('timestamp', 0)/1000)}")
                print(f"   Has Encoding: {'Yes' if face_data.get('faceEncoding') else 'No'}")
                
                # Save image to file
                image_base64 = face_data.get('faceImageBase64', '')
                if image_base64:
                    save_base64_image(image_base64, f"face_{i}_{face_data.get('username', 'unknown')}.jpg")
                else:
                    print("   ⚠️  No image data found")
                    
        else:
            print("❌ Failed to load face data")
            
    except Exception as e:
        print(f"❌ Error: {str(e)}")

def save_base64_image(base64_string, filename):
    """Save base64 image to file"""
    try:
        # Remove data URL prefix if present
        if base64_string.startswith('data:image'):
            base64_string = base64_string.split(',')[1]
        
        # Decode and save
        image_data = base64.b64decode(base64_string)
        
        # Create images directory if it doesn't exist
        os.makedirs('mongodb_images', exist_ok=True)
        
        filepath = os.path.join('mongodb_images', filename)
        with open(filepath, 'wb') as f:
            f.write(image_data)
        
        print(f"   💾 Image saved as: {filepath}")
        
    except Exception as e:
        print(f"   ❌ Error saving image: {str(e)}")

def check_services():
    """Check if services are running"""
    print("🔍 Checking if services are running...")
    
    try:
        # Check Spring Boot service
        response = requests.get(f"{SPRING_BOOT_URL}/user/test/exists", timeout=5)
        if response.status_code in [200, 404]:  # 404 is OK for test user
            print("✅ Spring Boot service is running")
        else:
            print("❌ Spring Boot service is not responding properly")
            return False
    except:
        print("❌ Spring Boot service is not running")
        return False
    
    try:
        # Check Face service
        response = requests.get(f"{FACE_SERVICE_URL}/health", timeout=5)
        if response.status_code == 200:
            print("✅ Face service is running")
        else:
            print("❌ Face service is not responding properly")
    except:
        print("⚠️  Face service is not running (optional)")
    
    return True

def main():
    """Main function"""
    print("🔍 MongoDB Atlas Image Viewer")
    print("=" * 50)
    
    # Check if services are running
    if not check_services():
        print("\n❌ Please start the Spring Boot service first:")
        print("   mvn spring-boot:run")
        return
    
    print("\n📋 Available options:")
    print("1. View face data for specific user")
    print("2. View all face data")
    print("3. Exit")
    
    while True:
        choice = input("\nEnter your choice (1-3): ").strip()
        
        if choice == '1':
            username = input("Enter username: ").strip()
            if username:
                view_user_face_data(username)
            else:
                print("❌ Please enter a valid username")
                
        elif choice == '2':
            view_all_face_data()
            
        elif choice == '3':
            print("👋 Goodbye!")
            break
            
        else:
            print("❌ Invalid choice. Please enter 1, 2, or 3.")

if __name__ == "__main__":
    main()
