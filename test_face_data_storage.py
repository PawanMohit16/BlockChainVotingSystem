#!/usr/bin/env python3
"""
Test script to add sample face data to MongoDB Atlas
"""

import requests
import base64
import json
import time

# Configuration
SPRING_BOOT_URL = "http://localhost:4000/api/face"

def create_sample_face_data():
    """Create sample face data for testing"""
    
    # Sample base64 image (1x1 pixel PNG)
    sample_image_base64 = "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNkYPhfDwAChwGA60e6kgAAAABJRU5ErkJggg=="
    
    # Sample face encoding (128-dimensional vector)
    sample_encoding = [0.1] * 128  # 128 zeros as placeholder
    
    sample_data = {
        "username": "test_user_123",
        "faceImageBase64": sample_image_base64,
        "faceEncoding": sample_encoding,
        "imageType": "png",
        "timestamp": int(time.time() * 1000)
    }
    
    return sample_data

def test_save_face_data():
    """Test saving face data to MongoDB"""
    print("🧪 Testing face data storage...")
    
    try:
        # Create sample data
        face_data = create_sample_face_data()
        
        # Save to MongoDB via Spring Boot API
        response = requests.post(
            f"{SPRING_BOOT_URL}/save",
            json=face_data,
            headers={'Content-Type': 'application/json'}
        )
        
        if response.status_code == 200:
            saved_data = response.json()
            print("✅ Face data saved successfully!")
            print(f"   ID: {saved_data.get('id')}")
            print(f"   Username: {saved_data.get('username')}")
            print(f"   Timestamp: {saved_data.get('timestamp')}")
            return True
        else:
            print(f"❌ Failed to save face data: {response.status_code}")
            print(f"   Response: {response.text}")
            return False
            
    except Exception as e:
        print(f"❌ Error: {str(e)}")
        return False

def test_retrieve_face_data():
    """Test retrieving face data from MongoDB"""
    print("\n🔍 Testing face data retrieval...")
    
    try:
        # Retrieve face data for test user
        response = requests.get(f"{SPRING_BOOT_URL}/user/test_user_123")
        
        if response.status_code == 200:
            data = response.json()
            if isinstance(data, list):
                face_data_list = data
            else:
                face_data_list = [data]
            
            print(f"✅ Found {len(face_data_list)} face record(s)")
            
            for i, face_data in enumerate(face_data_list, 1):
                print(f"\n📸 Face Record #{i}:")
                print(f"   ID: {face_data.get('id')}")
                print(f"   Username: {face_data.get('username')}")
                print(f"   Image Type: {face_data.get('imageType')}")
                print(f"   Has Encoding: {'Yes' if face_data.get('faceEncoding') else 'No'}")
                print(f"   Image Size: {len(face_data.get('faceImageBase64', ''))} characters")
            
            return True
        else:
            print(f"❌ Failed to retrieve face data: {response.status_code}")
            print(f"   Response: {response.text}")
            return False
            
    except Exception as e:
        print(f"❌ Error: {str(e)}")
        return False

def test_get_all_face_data():
    """Test getting all face data"""
    print("\n📋 Testing get all face data...")
    
    try:
        response = requests.get(f"{SPRING_BOOT_URL}/all")
        
        if response.status_code == 200:
            data = response.json()
            print(f"✅ Found {len(data)} total face record(s) in database")
            
            for i, face_data in enumerate(data, 1):
                print(f"   {i}. {face_data.get('username')} - {face_data.get('imageType')}")
            
            return True
        else:
            print(f"❌ Failed to get all face data: {response.status_code}")
            print(f"   Response: {response.text}")
            return False
            
    except Exception as e:
        print(f"❌ Error: {str(e)}")
        return False

def check_service_status():
    """Check if Spring Boot service is running"""
    print("🔍 Checking Spring Boot service status...")
    
    try:
        response = requests.get(f"{SPRING_BOOT_URL}/user/test/exists", timeout=5)
        if response.status_code in [200, 404]:  # 404 is OK for test user
            print("✅ Spring Boot service is running")
            return True
        else:
            print("❌ Spring Boot service is not responding properly")
            return False
    except:
        print("❌ Spring Boot service is not running")
        print("   Please start it with: .\\mvnw.cmd spring-boot:run")
        return False

def main():
    """Main function"""
    print("🧪 MongoDB Face Data Storage Test")
    print("=" * 50)
    
    # Check if service is running
    if not check_service_status():
        return
    
    # Test saving face data
    if test_save_face_data():
        # Test retrieving face data
        test_retrieve_face_data()
        
        # Test getting all face data
        test_get_all_face_data()
        
        print("\n🎉 Test completed! Check MongoDB Atlas for the 'face_data' collection.")
        print("   You should now see the collection in your MongoDB Atlas dashboard.")
    else:
        print("\n❌ Test failed. Please check the Spring Boot application logs.")

if __name__ == "__main__":
    main()
