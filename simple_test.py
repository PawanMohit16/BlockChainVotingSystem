#!/usr/bin/env python3
"""
Simple test to debug API response
"""

import requests
import json

# Test the API endpoints
SPRING_BOOT_URL = "http://localhost:4000/api/face"

def test_api():
    print("🔍 Testing API endpoints...")
    
    # Test 1: Check if service is running
    try:
        response = requests.get(f"{SPRING_BOOT_URL}/user/test/exists", timeout=5)
        print(f"Status: {response.status_code}")
        print(f"Response: {response.text}")
    except Exception as e:
        print(f"Error: {e}")
    
    # Test 2: Try to get all face data
    try:
        response = requests.get(f"{SPRING_BOOT_URL}/all", timeout=5)
        print(f"\nAll data status: {response.status_code}")
        print(f"Response: {response.text}")
    except Exception as e:
        print(f"Error: {e}")
    
    # Test 3: Try to save face data
    try:
        test_data = {
            "username": "test_user_123",
            "faceImageBase64": "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNkYPhfDwAChwGA60e6kgAAAABJRU5ErkJggg==",
            "faceEncoding": [0.1] * 128,
            "imageType": "png",
            "timestamp": 1234567890
        }
        
        response = requests.post(
            f"{SPRING_BOOT_URL}/save",
            json=test_data,
            headers={'Content-Type': 'application/json'},
            timeout=10
        )
        print(f"\nSave status: {response.status_code}")
        print(f"Response: {response.text}")
    except Exception as e:
        print(f"Error: {e}")

if __name__ == "__main__":
    test_api()
