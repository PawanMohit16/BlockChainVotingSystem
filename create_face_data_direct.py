#!/usr/bin/env python3
"""
Direct MongoDB Atlas connection to create face data
"""

import pymongo
import base64
import time
from datetime import datetime

# MongoDB Atlas connection string - you'll need to replace this with your actual connection string
# Format: mongodb+srv://username:password@cluster.mongodb.net/database_name
MONGODB_URI = "mongodb+srv://your_username:your_password@your_cluster.mongodb.net/your_database"

def create_face_data_collection():
    """Create face data collection directly in MongoDB Atlas"""
    
    try:
        # Connect to MongoDB Atlas
        client = pymongo.MongoClient(MONGODB_URI)
        db = client.get_default_database()
        
        # Create face_data collection
        face_data_collection = db["face_data"]
        
        # Sample base64 image (1x1 pixel PNG)
        sample_image_base64 = "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNkYPhfDwAChwGA60e6kgAAAABJRU5ErkJggg=="
        
        # Sample face encoding (128-dimensional vector)
        sample_encoding = [0.1] * 128
        
        # Create sample face data document
        face_data_doc = {
            "username": "test_user_123",
            "faceImageBase64": sample_image_base64,
            "faceEncoding": sample_encoding,
            "imageType": "png",
            "timestamp": int(time.time() * 1000),
            "createdAt": datetime.now()
        }
        
        # Insert the document
        result = face_data_collection.insert_one(face_data_doc)
        
        print(f"✅ Face data created successfully!")
        print(f"   Document ID: {result.inserted_id}")
        print(f"   Username: {face_data_doc['username']}")
        print(f"   Image Type: {face_data_doc['imageType']}")
        print(f"   Timestamp: {face_data_doc['timestamp']}")
        
        # Verify the document was created
        created_doc = face_data_collection.find_one({"_id": result.inserted_id})
        if created_doc:
            print(f"✅ Document verified in database")
            print(f"   Collection: {face_data_collection.name}")
            print(f"   Database: {db.name}")
        else:
            print("❌ Document not found in database")
        
        # Count documents in collection
        count = face_data_collection.count_documents({})
        print(f"📊 Total documents in face_data collection: {count}")
        
        client.close()
        return True
        
    except Exception as e:
        print(f"❌ Error: {str(e)}")
        print("\n📝 To use this script:")
        print("1. Replace MONGODB_URI with your actual MongoDB Atlas connection string")
        print("2. Format: mongodb+srv://username:password@cluster.mongodb.net/database_name")
        print("3. Make sure your IP is whitelisted in MongoDB Atlas")
        return False

def view_face_data():
    """View face data from MongoDB Atlas"""
    
    try:
        # Connect to MongoDB Atlas
        client = pymongo.MongoClient(MONGODB_URI)
        db = client.get_default_database()
        face_data_collection = db["face_data"]
        
        # Find all documents
        documents = list(face_data_collection.find())
        
        print(f"📊 Found {len(documents)} face data documents:")
        
        for i, doc in enumerate(documents, 1):
            print(f"\n📸 Document #{i}:")
            print(f"   ID: {doc.get('_id')}")
            print(f"   Username: {doc.get('username')}")
            print(f"   Image Type: {doc.get('imageType')}")
            print(f"   Timestamp: {doc.get('timestamp')}")
            print(f"   Has Image: {'Yes' if doc.get('faceImageBase64') else 'No'}")
            print(f"   Has Encoding: {'Yes' if doc.get('faceEncoding') else 'No'}")
            
            # Save image to file
            if doc.get('faceImageBase64'):
                save_base64_image(doc['faceImageBase64'], f"face_{i}_{doc.get('username', 'unknown')}.jpg")
        
        client.close()
        return True
        
    except Exception as e:
        print(f"❌ Error: {str(e)}")
        return False

def save_base64_image(base64_string, filename):
    """Save base64 image to file"""
    try:
        # Remove data URL prefix if present
        if base64_string.startswith('data:image'):
            base64_string = base64_string.split(',')[1]
        
        # Decode and save
        image_data = base64.b64decode(base64_string)
        
        # Create images directory if it doesn't exist
        import os
        os.makedirs('mongodb_images', exist_ok=True)
        
        filepath = os.path.join('mongodb_images', filename)
        with open(filepath, 'wb') as f:
            f.write(image_data)
        
        print(f"   💾 Image saved as: {filepath}")
        
    except Exception as e:
        print(f"   ❌ Error saving image: {str(e)}")

def main():
    """Main function"""
    print("🔍 Direct MongoDB Atlas Face Data Manager")
    print("=" * 50)
    
    print("\n⚠️  IMPORTANT: You need to update the MONGODB_URI in this script first!")
    print("   Replace the placeholder with your actual MongoDB Atlas connection string.")
    
    choice = input("\nDo you want to proceed? (y/n): ").strip().lower()
    
    if choice == 'y':
        print("\n📋 Available options:")
        print("1. Create sample face data")
        print("2. View existing face data")
        print("3. Both")
        
        option = input("\nEnter your choice (1-3): ").strip()
        
        if option == '1':
            create_face_data_collection()
        elif option == '2':
            view_face_data()
        elif option == '3':
            create_face_data_collection()
            print("\n" + "="*50)
            view_face_data()
        else:
            print("❌ Invalid choice")
    else:
        print("👋 Goodbye!")

if __name__ == "__main__":
    main()
