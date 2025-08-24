#!/usr/bin/env python3
"""
Test script for MongoDB-based face storage system
This script demonstrates how to register and verify faces using the new system
"""

import requests
import base64
import json
import os

# Configuration
FACE_SERVICE_URL = "http://localhost:5001/api/face"
SPRING_BOOT_URL = "http://localhost:8080/api/face"

def test_face_registration():
    """Test face registration with MongoDB storage"""
    print("=== Testing Face Registration ===")
    
    # Test image path (you need to provide a test image)
    test_image_path = "test_face.jpg"
    
    if not os.path.exists(test_image_path):
        print(f"Test image not found: {test_image_path}")
        print("Please provide a test image named 'test_face.jpg'")
        return False
    
    # Register face for a test user
    username = "test_user_123"
    
    with open(test_image_path, 'rb') as image_file:
        files = {'file': (test_image_path, image_file, 'image/jpeg')}
        data = {'username': username}
        
        try:
            response = requests.post(f"{FACE_SERVICE_URL}/register", files=files, data=data)
            
            if response.status_code == 200:
                result = response.json()
                print(f"✅ Face registration successful!")
                print(f"   User: {username}")
                print(f"   Face Data ID: {result.get('faceDataId')}")
                return True
            else:
                print(f"❌ Face registration failed: {response.status_code}")
                print(f"   Error: {response.text}")
                return False
                
        except Exception as e:
            print(f"❌ Error during face registration: {str(e)}")
            return False

def test_face_verification():
    """Test face verification using MongoDB storage"""
    print("\n=== Testing Face Verification ===")
    
    # Test image path (you need to provide a test image)
    test_image_path = "test_face.jpg"
    
    if not os.path.exists(test_image_path):
        print(f"Test image not found: {test_image_path}")
        print("Please provide a test image named 'test_face.jpg'")
        return False
    
    username = "test_user_123"
    
    # Convert image to base64
    with open(test_image_path, 'rb') as image_file:
        image_base64 = base64.b64encode(image_file.read()).decode('utf-8')
    
    # Prepare verification request
    verification_data = {
        'image': f"data:image/jpeg;base64,{image_base64}",
        'username': username
    }
    
    try:
        response = requests.post(f"{FACE_SERVICE_URL}/verify", 
                               json=verification_data,
                               headers={'Content-Type': 'application/json'})
        
        if response.status_code == 200:
            result = response.json()
            print(f"✅ Face verification successful!")
            print(f"   User: {result.get('username')}")
            print(f"   Match: {result.get('match')}")
            print(f"   Face Location: {result.get('face_location')}")
            return True
        else:
            print(f"❌ Face verification failed: {response.status_code}")
            print(f"   Error: {response.text}")
            return False
            
    except Exception as e:
        print(f"❌ Error during face verification: {str(e)}")
        return False

def test_get_user_face_data():
    """Test retrieving user face data from MongoDB"""
    print("\n=== Testing Get User Face Data ===")
    
    username = "test_user_123"
    
    try:
        response = requests.get(f"{FACE_SERVICE_URL}/user/{username}")
        
        if response.status_code == 200:
            result = response.json()
            print(f"✅ User face data retrieved successfully!")
            print(f"   User: {result.get('username')}")
            print(f"   Image Type: {result.get('imageType')}")
            print(f"   Has Encoding: {result.get('hasEncoding')}")
            print(f"   Timestamp: {result.get('timestamp')}")
            return True
        else:
            print(f"❌ Failed to retrieve user face data: {response.status_code}")
            print(f"   Error: {response.text}")
            return False
            
    except Exception as e:
        print(f"❌ Error retrieving user face data: {str(e)}")
        return False

def test_mongodb_direct_api():
    """Test direct MongoDB API endpoints"""
    print("\n=== Testing Direct MongoDB API ===")
    
    try:
        # Test health check
        response = requests.get(f"{SPRING_BOOT_URL}/user/test_user_123/exists")
        
        if response.status_code == 200:
            exists = response.json()
            print(f"✅ User face data exists: {exists}")
            return True
        else:
            print(f"❌ Failed to check user existence: {response.status_code}")
            return False
            
    except Exception as e:
        print(f"❌ Error testing MongoDB API: {str(e)}")
        return False

def main():
    """Main test function"""
    print("🧪 MongoDB Face Storage System Test")
    print("=" * 50)
    
    # Check if services are running
    print("Checking if services are running...")
    
    try:
        # Check face service
        face_health = requests.get(f"{FACE_SERVICE_URL}/health", timeout=5)
        if face_health.status_code == 200:
            print("✅ Face service is running")
        else:
            print("❌ Face service is not responding properly")
            return
    except:
        print("❌ Face service is not running. Please start it first.")
        print("   Run: cd face-service && python app.py")
        return
    
    try:
        # Check Spring Boot service
        spring_health = requests.get(f"{SPRING_BOOT_URL.replace('/api/face', '/api/health')}", timeout=5)
        if spring_health.status_code == 200:
            print("✅ Spring Boot service is running")
        else:
            print("❌ Spring Boot service is not responding properly")
            return
    except:
        print("❌ Spring Boot service is not running. Please start it first.")
        print("   Run: mvn spring-boot:run")
        return
    
    print("\nStarting tests...")
    
    # Run tests
    test_results = []
    
    test_results.append(test_face_registration())
    test_results.append(test_face_verification())
    test_results.append(test_get_user_face_data())
    test_results.append(test_mongodb_direct_api())
    
    # Summary
    print("\n" + "=" * 50)
    print("📊 Test Summary")
    print("=" * 50)
    
    passed = sum(test_results)
    total = len(test_results)
    
    print(f"Tests passed: {passed}/{total}")
    
    if passed == total:
        print("🎉 All tests passed! MongoDB face storage is working correctly.")
    else:
        print("⚠️  Some tests failed. Please check the errors above.")
    
    print("\n📝 Usage Instructions:")
    print("1. Start Spring Boot service: mvn spring-boot:run")
    print("2. Start Face service: cd face-service && python app.py")
    print("3. Register face: POST /api/face/register with file and username")
    print("4. Verify face: POST /api/face/verify with base64 image and username")
    print("5. Get user data: GET /api/face/user/{username}")

if __name__ == "__main__":
    main()
