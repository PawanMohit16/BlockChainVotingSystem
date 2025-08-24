# Local MongoDB Setup Guide

## Option 1: Use MongoDB Atlas (Recommended)
1. Go to https://cloud.mongodb.com
2. Login to your account
3. Check Database Access for user credentials
4. Verify the database "collegeproject" exists

## Option 2: Install Local MongoDB
1. Download MongoDB Community Server from: https://www.mongodb.com/try/download/community
2. Install MongoDB
3. Start MongoDB service
4. Update application.properties to use local MongoDB:

```properties
# Local MongoDB Configuration
spring.data.mongodb.uri=mongodb://localhost:27017/collegeproject
spring.data.mongodb.database=collegeproject
```

## Option 3: Use MongoDB Docker
```bash
docker run -d -p 27017:27017 --name mongodb mongo:latest
```

## Current Issue:
The MongoDB Atlas authentication is failing. This could be due to:
- Incorrect username/password
- User doesn't have access to the database
- Database doesn't exist
- Network connectivity issues

## Quick Fix:
Try updating the connection string with proper authentication parameters.

