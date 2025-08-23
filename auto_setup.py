import os
import sys
import subprocess
from pathlib import Path

def run_command(command, cwd=None):
    try:
        result = subprocess.run(
            command,
            shell=True,
            check=True,
            cwd=cwd,
            capture_output=True,
            text=True
        )
        return result.stdout
    except subprocess.CalledProcessError as e:
        print(f"Error running command: {command}")
        print(f"Error: {e.stderr}")
        sys.exit(1)

def main():
    # Set up paths
    base_dir = Path(__file__).parent
    project_dir = base_dir / "fresh_django"
    
    print("Creating project directory...")
    project_dir.mkdir(exist_ok=True)
    
    # Create and activate virtual environment
    print("Setting up virtual environment...")
    run_command("python -m venv venv", cwd=project_dir)
    
    # Install requirements
    print("Installing requirements...")
    if os.name == 'nt':  # Windows
        pip_path = project_dir / "venv" / "Scripts" / "pip"
    else:  # Unix/Linux/Mac
        pip_path = project_dir / "venv" / "bin" / "pip"
    
    run_command(f"{pip_path} install django djangorestframework djangorestframework-simplejwt django-cors-headers")
    
    # Create Django project
    print("Creating Django project...")
    run_command(f"{project_dir}/venv/Scripts/python -m django startproject avax_backend .", cwd=project_dir)
    
    # Create apps
    print("Creating apps...")
    run_command(f"{project_dir}/venv/Scripts/python manage.py startapp accounts", cwd=project_dir)
    run_command(f"{project_dir}/venv/Scripts/python manage.py startapp voting", cwd=project_dir)
    
    # Update settings.py
    settings_path = project_dir / "avax_backend" / "settings.py"
    with open(settings_path, 'r+') as f:
        content = f.read()
        
        # Add apps to INSTALLED_APPS
        content = content.replace(
            "'django.contrib.staticfiles',",
            "'django.contrib.staticfiles',\n    'rest_framework',\n    'corsheaders',\n    'rest_framework_simplejwt',\n    'accounts',\n    'voting',"
        )
        
        # Add CORS settings
        cors_settings = """
# CORS settings
CORS_ALLOW_ALL_ORIGINS = True
CORS_ALLOW_CREDENTIALS = True

# Custom user model
AUTH_USER_MODEL = 'accounts.User'

# REST Framework settings
REST_FRAMEWORK = {
    'DEFAULT_AUTHENTICATION_CLASSES': (
        'rest_framework_simplejwt.authentication.JWTAuthentication',
    ),
    'DEFAULT_PERMISSION_CLASSES': [
        'rest_framework.permissions.IsAuthenticated',
    ],
}

# JWT settings
SIMPLE_JWT = {
    'ACCESS_TOKEN_LIFETIME': timedelta(days=1),
    'REFRESH_TOKEN_LIFETIME': timedelta(days=7),
}
"""
        content = content.replace("from pathlib import Path", "from pathlib import Path\nfrom datetime import timedelta")
        content = content.replace("# Application definition", f"# Application definition\n{cors_settings}")
        
        f.seek(0)
        f.write(content)
        f.truncate()
    
    # Create custom user model
    accounts_models = project_dir / "accounts" / "models.py"
    with open(accounts_models, 'w') as f:
        f.write("""from django.contrib.auth.models import AbstractUser
from django.db import models

class User(AbstractUser):
    is_voter = models.BooleanField(default=False)
    is_admin = models.BooleanField(default=False)
    voted = models.BooleanField(default=False)
    
    def __str__(self):
        return self.username
""")
    
    # Create admin.py for accounts
    with open(project_dir / "accounts" / "admin.py", 'w') as f:
        f.write("""from django.contrib import admin
from django.contrib.auth.admin import UserAdmin
from .models import User

admin.site.register(User, UserAdmin)
""")
    
    # Run migrations
    print("Running migrations...")
    run_command(f"{project_dir}/venv/Scripts/python manage.py makemigrations", cwd=project_dir)
    run_command(f"{project_dir}/venv/Scripts/python manage.py migrate", cwd=project_dir)
    
    # Create superuser
    print("\nCreating superuser...")
    run_command(f"{project_dir}/venv/Scripts/python manage.py createsuperuser --noinput --username admin --email admin@example.com", 
               cwd=project_dir)
    
    # Set password for superuser (default: admin123)
    print("Setting up superuser password...")
    import django
    import os
    os.environ.setdefault('DJANGO_SETTINGS_MODULE', 'avax_backend.settings')
    django.setup()
    from django.contrib.auth import get_user_model
    User = get_user_model()
    user = User.objects.get(username='admin')
    user.set_password('admin123')
    user.save()
    
    print("\nSetup complete!")
    print(f"Project directory: {project_dir}")
    print("\nTo start the development server:")
    print(f"1. cd {project_dir}")
    print("2. .\\venv\\Scripts\\activate")
    print("3. python manage.py runserver")
    print("\nAdmin credentials:")
    print("Username: admin")
    print("Password: admin123")

if __name__ == "__main__":
    main()
