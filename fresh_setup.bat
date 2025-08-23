@echo off
echo Setting up fresh Django project...

REM Create and activate virtual environment
echo Creating virtual environment...
python -m venv venv
call venv\Scripts\activate

REM Install required packages
echo Installing required packages...
pip install django djangorestframework djangorestframework-simplejwt django-cors-headers

REM Create Django project
echo Creating Django project...
django-admin startproject avax_project .
cd avax_project

REM Create apps
echo Creating apps...
python ..\manage.py startapp accounts
python ..\manage.py startapp voting

REM Create requirements.txt
echo Creating requirements file...
pip freeze > ..\requirements.txt

cd ..

echo.
echo Project setup complete!
echo Next steps:
echo 1. Activate virtual environment: .\venv\Scripts\activate
echo 2. Run migrations: python manage.py migrate
echo 3. Create superuser: python manage.py createsuperuser
echo 4. Start the server: python manage.py runserver

pause
