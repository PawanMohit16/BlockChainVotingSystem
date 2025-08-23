@echo off
echo Setting up Django backend...

REM Create and activate virtual environment
echo Creating virtual environment...
python -m venv venv

REM Install required packages
echo Installing required packages...
call venv\Scripts\activate
pip install django djangorestframework djangorestframework-simplejwt django-cors-headers pymongo djongo

REM Create Django project and apps
echo Creating Django project and apps...
django-admin startproject avax_backend .
cd avax_backend
django-admin startapp accounts
django-admin startapp voting

REM Create requirements.txt
echo Creating requirements file...
pip freeze > ..\requirements.txt

echo.
echo Setup complete! To continue:
echo 1. cd into the project directory
echo 2. Run 'python manage.py migrate'
echo 3. Run 'python manage.py createsuperuser'
echo 4. Run 'python manage.py runserver'
pause
