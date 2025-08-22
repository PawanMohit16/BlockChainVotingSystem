@echo off
echo ========================================
echo   Secure Online Voting System
echo   MERN Stack Backend
echo ========================================
echo.

echo Checking if Node.js is installed...
node --version >nul 2>&1
if %errorlevel% neq 0 (
    echo ERROR: Node.js is not installed!
    echo Please install Node.js from: https://nodejs.org/
    pause
    exit /b 1
)

echo Checking if MongoDB is running...
net start MongoDB >nul 2>&1
if %errorlevel% neq 0 (
    echo WARNING: MongoDB service is not running!
    echo Please start MongoDB service manually:
    echo   net start MongoDB
    echo.
    echo Press any key to continue anyway...
    pause >nul
)

echo.
echo Installing dependencies...
npm install

echo.
echo Starting the application...
echo.
npm start

pause
