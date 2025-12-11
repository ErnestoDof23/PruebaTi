import os
from datetime import timedelta

class Config:
    """Configuración del servidor Flask"""
    
    # Seguridad
    SECRET_KEY = os.environ.get('SECRET_KEY') or 'dev-secret-key-change-in-production-2024'
    JWT_SECRET_KEY = os.environ.get('JWT_SECRET_KEY') or 'jwt-secret-key-change-in-production-2024'
    JWT_ACCESS_TOKEN_EXPIRES = timedelta(hours=24)
    
    # Desactivar CSRF para tokens Bearer (APIs REST)
    JWT_COOKIE_CSRF_PROTECT = False
    JWT_CSRF_CHECK_FORM = False
    
    # Base de datos
    SQLALCHEMY_DATABASE_URI = os.environ.get('DATABASE_URL') or 'sqlite:///database.db'
    SQLALCHEMY_TRACK_MODIFICATIONS = False
    
    # CORS - Permitir peticiones desde cualquier origen (para desarrollo)
    # En producción, especificar dominios permitidos
    CORS_ORIGINS = ['*']
    
    # Configuración de la aplicación
    DEBUG = os.environ.get('FLASK_DEBUG', 'True').lower() == 'true'
    HOST = os.environ.get('FLASK_HOST', '192.168.109.8')
    PORT = int(os.environ.get('FLASK_PORT', 5000))

