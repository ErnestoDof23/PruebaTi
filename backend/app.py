from flask import Flask, request, jsonify
from flask_cors import CORS
from flask_jwt_extended import (
    JWTManager, create_access_token, jwt_required, get_jwt_identity
)
from config import Config
from models import db, User, Note
import re
from datetime import datetime

# Crear aplicación Flask
app = Flask(__name__)
app.config.from_object(Config)

# Inicializar extensiones
db.init_app(app)
jwt = JWTManager(app)
CORS(app, origins=app.config['CORS_ORIGINS'])


# ============================================
# MANEJADORES DE ERRORES
# ============================================

@app.errorhandler(422)
def handle_unprocessable_entity(e):
    """Manejar errores 422"""
    print(f"❌ Error 422: {str(e)}")
    import traceback
    traceback.print_exc()
    return error_response(f'Error 422: {str(e)}', 422)

@app.errorhandler(Exception)
def handle_exception(e):
    """Manejar todas las excepciones"""
    print(f"❌ Excepción no manejada: {str(e)}")
    import traceback
    traceback.print_exc()
    return error_response(f'Error del servidor: {str(e)}', 500)

# Manejar errores de JWT
@jwt.invalid_token_loader
def invalid_token_callback(error):
    print(f"❌ Token inválido: {error}")
    return error_response('Token inválido', 401)

@jwt.expired_token_loader
def expired_token_callback(jwt_header, jwt_payload):
    print(f"❌ Token expirado")
    return error_response('Token expirado', 401)

@jwt.unauthorized_loader
def unauthorized_callback(error):
    print(f"❌ No autorizado: {error}")
    return error_response('Falta el token de autorización', 401)


# ============================================
# UTILIDADES
# ============================================

def validate_email(email):
    """Valida formato de email"""
    pattern = r'^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$'
    return re.match(pattern, email) is not None


def error_response(message, status_code=400):
    """Respuesta de error estándar"""
    return jsonify({'error': message}), status_code


def success_response(data, status_code=200):
    """Respuesta exitosa estándar"""
    return jsonify(data), status_code


# ============================================
# ENDPOINTS DE AUTENTICACIÓN
# ============================================

@app.route('/api/auth/register', methods=['POST'])
def register():
    """Registro de nuevos usuarios"""
    try:
        data = request.get_json()
        
        # Validar datos requeridos
        name = data.get('name', '').strip()
        email = data.get('email', '').strip().lower()
        password = data.get('password', '')
        device_id = data.get('deviceId')
        
        # Validaciones
        if not name or len(name) < 3:
            return error_response('El nombre debe tener al menos 3 caracteres')
        
        if not email or not validate_email(email):
            return error_response('Email inválido')
        
        if not password or len(password) < 6:
            return error_response('La contraseña debe tener al menos 6 caracteres')
        
        # Verificar si el email ya existe
        if User.query.filter_by(email=email).first():
            return error_response('El email ya está registrado', 409)
        
        # Crear nuevo usuario
        user = User(name=name, email=email, device_id=device_id)
        user.set_password(password)
        
        db.session.add(user)
        db.session.commit()
        
        # Generar token JWT (identity debe ser string)
        token = create_access_token(identity=str(user.id))
        
        return success_response({
            'success': True,
            'message': 'Usuario registrado exitosamente',
            'token': token,
            'user': user.to_dict()
        }, 201)
        
    except Exception as e:
        db.session.rollback()
        return error_response(f'Error al registrar usuario: {str(e)}', 500)


@app.route('/api/auth/login', methods=['POST'])
def login():
    """Inicio de sesión"""
    try:
        data = request.get_json()
        
        email = data.get('email', '').strip().lower()
        password = data.get('password', '')
        device_id = data.get('deviceId')
        
        if not email or not password:
            return error_response('Email y contraseña son requeridos')
        
        # Buscar usuario
        user = User.query.filter_by(email=email).first()
        
        if not user or not user.check_password(password):
            return error_response('Email o contraseña incorrectos', 401)
        
        # Validar device_id si está configurado
        if user.device_id and device_id:
            if user.device_id != device_id:
                return error_response(
                    'Esta cuenta está vinculada a otro dispositivo. '
                    'Usa la opción "Desvincular dispositivo" para cambiar.',
                    403
                )
        elif device_id and not user.device_id:
            # Vincular dispositivo si no está vinculado
            user.device_id = device_id
            db.session.commit()
        
        # Generar token JWT (identity debe ser string)
        token = create_access_token(identity=str(user.id))
        
        return success_response({
            'token': token,
            'user': user.to_dict()
        })
        
    except Exception as e:
        return error_response(f'Error al iniciar sesión: {str(e)}', 500)


@app.route('/api/auth/forgot-password', methods=['POST'])
def forgot_password():
    """Solicitar recuperación de contraseña"""
    try:
        data = request.get_json()
        email = data.get('email', '').strip().lower()
        
        if not email:
            return error_response('Email es requerido')
        
        user = User.query.filter_by(email=email).first()
        
        if not user:
            # Por seguridad, no revelar si el email existe
            return success_response({
                'success': True,
                'message': 'Si el email existe, recibirás un código de recuperación'
            })
        
        # Generar token de recuperación
        reset_token = user.generate_reset_token()
        db.session.commit()
        
        # TODO: En producción, enviar el token por email
        # Para desarrollo, lo incluimos en la respuesta
        return success_response({
            'success': True,
            'message': 'Código de recuperación generado',
            'resetToken': reset_token  # Solo para desarrollo
        })
        
    except Exception as e:
        db.session.rollback()
        return error_response(f'Error al solicitar recuperación: {str(e)}', 500)


@app.route('/api/auth/verify-reset-token', methods=['POST'])
def verify_reset_token():
    """Verificar código de recuperación"""
    try:
        data = request.get_json()
        email = data.get('email', '').strip().lower()
        token = data.get('token', '')
        
        if not email or not token:
            return error_response('Email y token son requeridos')
        
        user = User.query.filter_by(email=email).first()
        
        if not user or not user.verify_reset_token(token):
            return error_response('Token inválido o expirado', 401)
        
        return success_response({
            'valid': True,
            'message': 'Token válido'
        })
        
    except Exception as e:
        return error_response(f'Error al verificar token: {str(e)}', 500)


@app.route('/api/auth/reset-password', methods=['POST'])
def reset_password():
    """Cambiar contraseña con token de recuperación"""
    try:
        data = request.get_json()
        email = data.get('email', '').strip().lower()
        token = data.get('token', '')
        new_password = data.get('newPassword', '')
        
        if not email or not token or not new_password:
            return error_response('Email, token y nueva contraseña son requeridos')
        
        if len(new_password) < 6:
            return error_response('La contraseña debe tener al menos 6 caracteres')
        
        user = User.query.filter_by(email=email).first()
        
        if not user or not user.verify_reset_token(token):
            return error_response('Token inválido o expirado', 401)
        
        # Cambiar contraseña
        user.set_password(new_password)
        user.clear_reset_token()
        db.session.commit()
        
        return success_response({
            'success': True,
            'message': 'Contraseña actualizada exitosamente'
        })
        
    except Exception as e:
        db.session.rollback()
        return error_response(f'Error al cambiar contraseña: {str(e)}', 500)


@app.route('/api/auth/unlink-device', methods=['POST'])
def unlink_device():
    """Desvincular dispositivo"""
    try:
        data = request.get_json()
        email = data.get('email', '').strip().lower()
        password = data.get('password', '')
        
        if not email or not password:
            return error_response('Email y contraseña son requeridos')
        
        user = User.query.filter_by(email=email).first()
        
        if not user or not user.check_password(password):
            return error_response('Credenciales incorrectas', 401)
        
        user.device_id = None
        db.session.commit()
        
        return success_response({
            'success': True,
            'message': 'Dispositivo desvinculado exitosamente'
        })
        
    except Exception as e:
        db.session.rollback()
        return error_response(f'Error al desvincular dispositivo: {str(e)}', 500)


# ============================================
# ENDPOINTS DE NOTAS
# ============================================

@app.route('/api/notes', methods=['GET'])
@jwt_required()
def get_notes():
    """Obtener todas las notas del usuario autenticado"""
    try:
        user_id_str = get_jwt_identity()
        user_id = int(user_id_str)
        print(f"📋 Obtener notas - Usuario ID: {user_id}")
        notes = Note.query.filter_by(userId=user_id).order_by(Note.createdAt.desc()).all()
        print(f"📋 Notas encontradas: {len(notes)}")
        
        notes_dict = [note.to_dict() for note in notes]
        print(f"📋 Notas serializadas: {notes_dict}")
        
        return success_response(notes_dict)
        
    except Exception as e:
        print(f"❌ Error al obtener notas: {str(e)}")
        import traceback
        traceback.print_exc()
        return error_response(f'Error al obtener notas: {str(e)}', 500)


@app.route('/api/notes/<note_id>', methods=['GET'])
@jwt_required()
def get_note(note_id):
    """Obtener una nota específica"""
    try:
        user_id_str = get_jwt_identity()
        user_id = int(user_id_str)
        note = Note.query.filter_by(id=note_id, userId=user_id).first()
        
        if not note:
            return error_response('Nota no encontrada', 404)
        
        return success_response(note.to_dict())
        
    except Exception as e:
        return error_response(f'Error al obtener nota: {str(e)}', 500)


@app.route('/api/notes', methods=['POST'])
@jwt_required()
def create_note():
    """Crear una nueva nota"""
    print("=" * 50)
    print("📝 INICIO - Crear nota")
    print("=" * 50)
    
    try:
        # Paso 1: Obtener usuario (convertir a int)
        user_id_str = get_jwt_identity()
        user_id = int(user_id_str)
        print(f"✅ Usuario autenticado - ID: {user_id}")
        
        # Paso 2: Obtener datos
        data = request.get_json()
        print(f"✅ Datos recibidos: {data}")
        
        # Paso 3: Validar datos
        if not data:
            print("❌ No se recibieron datos")
            return error_response('No se recibieron datos', 400)
        
        title = data.get('title', '').strip()
        content = data.get('content', '').strip()
        image_url = data.get('imageUrl')
        
        print(f"✅ Título: '{title}'")
        print(f"✅ Contenido: '{content}'")
        print(f"✅ ImageUrl: '{image_url}'")
        
        # Validaciones
        if not title:
            print("❌ Error: Título vacío")
            return error_response('El título es requerido', 400)
        
        if not content:
            print("❌ Error: Contenido vacío")
            return error_response('El contenido es requerido', 400)
        
        # Paso 4: Crear nota
        import uuid
        note_id = str(uuid.uuid4())
        now = datetime.utcnow()
        
        print(f"✅ Generando nota con ID: {note_id}")
        
        note = Note(
            id=note_id,
            title=title,
            content=content,
            imageUrl=image_url,
            userId=user_id,
            createdAt=now,
            updatedAt=now
        )
        
        print(f"✅ Objeto Note creado")
        
        # Paso 5: Guardar en BD
        db.session.add(note)
        print(f"✅ Nota agregada a sesión")
        
        db.session.commit()
        print(f"✅ Nota guardada en BD")
        
        # Paso 6: Serializar y responder
        note_dict = note.to_dict()
        print(f"✅ Nota serializada: {note_dict}")
        
        print("=" * 50)
        print("✅ FIN - Nota creada exitosamente")
        print("=" * 50)
        
        return success_response(note_dict, 201)
        
    except Exception as e:
        db.session.rollback()
        print("=" * 50)
        print(f"❌ EXCEPCIÓN al crear nota: {str(e)}")
        print("=" * 50)
        import traceback
        traceback.print_exc()
        return error_response(f'Error al crear nota: {str(e)}', 500)


@app.route('/api/notes/<note_id>', methods=['PUT'])
@jwt_required()
def update_note(note_id):
    """Actualizar una nota existente"""
    try:
        user_id_str = get_jwt_identity()
        user_id = int(user_id_str)
        print(f"✏️ Actualizar nota - ID: {note_id}, Usuario: {user_id}")
        
        note = Note.query.filter_by(id=note_id, userId=user_id).first()
        
        if not note:
            print(f"❌ Nota no encontrada: {note_id}")
            return error_response('Nota no encontrada', 404)
        
        data = request.get_json()
        print(f"✏️ Datos recibidos: {data}")
        
        title = data.get('title', '').strip()
        content = data.get('content', '').strip()
        image_url = data.get('imageUrl')
        
        # Validaciones
        if not title:
            print("❌ Error: Título vacío")
            return error_response('El título es requerido')
        
        if not content:
            print("❌ Error: Contenido vacío")
            return error_response('El contenido es requerido')
        
        # Actualizar nota
        note.title = title
        note.content = content
        note.imageUrl = image_url
        note.updatedAt = datetime.utcnow()
        
        db.session.commit()
        
        print(f"✅ Nota actualizada exitosamente: {note_id}")
        return success_response(note.to_dict())
        
    except Exception as e:
        db.session.rollback()
        print(f"❌ Error al actualizar nota: {str(e)}")
        return error_response(f'Error al actualizar nota: {str(e)}', 500)


@app.route('/api/notes/<note_id>', methods=['DELETE'])
@jwt_required()
def delete_note(note_id):
    """Eliminar una nota"""
    try:
        user_id_str = get_jwt_identity()
        user_id = int(user_id_str)
        note = Note.query.filter_by(id=note_id, userId=user_id).first()
        
        if not note:
            return error_response('Nota no encontrada', 404)
        
        db.session.delete(note)
        db.session.commit()
        
        return success_response({
            'success': True,
            'message': 'Nota eliminada exitosamente'
        })
        
    except Exception as e:
        db.session.rollback()
        return error_response(f'Error al eliminar nota: {str(e)}', 500)


# ============================================
# ENDPOINTS DE PRUEBA
# ============================================

@app.route('/api/health', methods=['GET'])
def health_check():
    """Verificar que el servidor está funcionando"""
    return success_response({
        'status': 'ok',
        'message': 'Servidor funcionando correctamente',
        'timestamp': datetime.utcnow().isoformat()
    })


@app.route('/api/', methods=['GET'])
def api_info():
    """Información de la API"""
    return success_response({
        'name': 'Notas App API',
        'version': '1.0.0',
        'endpoints': {
            'auth': [
                'POST /api/auth/register',
                'POST /api/auth/login',
                'POST /api/auth/forgot-password',
                'POST /api/auth/verify-reset-token',
                'POST /api/auth/reset-password',
                'POST /api/auth/unlink-device'
            ],
            'notes': [
                'GET /api/notes',
                'GET /api/notes/<id>',
                'POST /api/notes',
                'PUT /api/notes/<id>',
                'DELETE /api/notes/<id>'
            ]
        }
    })


# ============================================
# INICIALIZACIÓN
# ============================================

def init_db():
    """Inicializar base de datos"""
    with app.app_context():
        db.create_all()
        print('✅ Base de datos inicializada')


if __name__ == '__main__':
    # Crear tablas si no existen
    init_db()
    
    # Iniciar servidor
    print(f'🚀 Servidor iniciando en http://{app.config["HOST"]}:{app.config["PORT"]}')
    print(f'📱 Para Android Emulator usa: http://10.0.2.2:{app.config["PORT"]}/api/')
    print(f'📱 Para dispositivo físico usa: http://[TU_IP_LOCAL]:{app.config["PORT"]}/api/')
    
    app.run(
        host=app.config['HOST'],
        port=app.config['PORT'],
        debug=app.config['DEBUG']
    )

