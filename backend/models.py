from flask_sqlalchemy import SQLAlchemy
from datetime import datetime, timedelta
import hashlib
import secrets

db = SQLAlchemy()


class User(db.Model):
    """Modelo de Usuario"""
    __tablename__ = 'users'
    
    id = db.Column(db.Integer, primary_key=True, autoincrement=True)
    name = db.Column(db.String(100), nullable=False)
    email = db.Column(db.String(100), unique=True, nullable=False, index=True)
    password = db.Column(db.String(200), nullable=False)
    device_id = db.Column(db.String(200), unique=True, nullable=True)
    reset_token = db.Column(db.String(100), nullable=True)
    reset_token_expiry = db.Column(db.DateTime, nullable=True)
    created_at = db.Column(db.DateTime, default=datetime.utcnow)
    
    # Relación con notas
    notes = db.relationship('Note', backref='user', lazy='dynamic', cascade='all, delete-orphan')
    
    def set_password(self, password):
        """Hashea la contraseña usando SHA-256"""
        self.password = hashlib.sha256(password.encode()).hexdigest()
    
    def check_password(self, password):
        """Verifica si la contraseña es correcta"""
        hashed = hashlib.sha256(password.encode()).hexdigest()
        return self.password == hashed
    
    def generate_reset_token(self):
        """Genera un token de recuperación de contraseña (válido por 1 hora)"""
        self.reset_token = secrets.token_hex(16)  # 32 caracteres
        self.reset_token_expiry = datetime.utcnow() + timedelta(hours=1)
        return self.reset_token
    
    def verify_reset_token(self, token):
        """Verifica si el token de recuperación es válido"""
        if not self.reset_token or not self.reset_token_expiry:
            return False
        if self.reset_token != token:
            return False
        if datetime.utcnow() > self.reset_token_expiry:
            return False
        return True
    
    def clear_reset_token(self):
        """Limpia el token de recuperación después de usarlo"""
        self.reset_token = None
        self.reset_token_expiry = None
    
    def to_dict(self):
        """Convierte el usuario a diccionario (sin contraseña)"""
        return {
            'id': str(self.id),
            'name': self.name,
            'email': self.email
        }


class Note(db.Model):
    """Modelo de Nota"""
    __tablename__ = 'notes'
    
    id = db.Column(db.String(36), primary_key=True)  # UUID
    title = db.Column(db.String(200), nullable=False)
    content = db.Column(db.Text, nullable=False)
    imageUrl = db.Column(db.String(500), nullable=True)
    userId = db.Column(db.Integer, db.ForeignKey('users.id'), nullable=False, index=True)
    createdAt = db.Column(db.DateTime, default=datetime.utcnow)
    updatedAt = db.Column(db.DateTime, default=datetime.utcnow, onupdate=datetime.utcnow)
    
    def to_dict(self):
        """Convierte la nota a diccionario compatible con Android"""
        try:
            created_at = self.createdAt.isoformat() + 'Z' if self.createdAt else datetime.utcnow().isoformat() + 'Z'
            updated_at = self.updatedAt.isoformat() + 'Z' if self.updatedAt else datetime.utcnow().isoformat() + 'Z'
        except Exception as e:
            print(f"⚠️ Error al convertir fechas: {e}")
            now = datetime.utcnow().isoformat() + 'Z'
            created_at = now
            updated_at = now
        
        return {
            'id': self.id,
            'title': self.title,
            'content': self.content,
            'imageUrl': self.imageUrl,
            'userId': str(self.userId),
            'createdAt': created_at,
            'updatedAt': updated_at
        }

