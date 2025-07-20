from faker import Faker
import random

fake = Faker('es_ES')  # Español para datos más realistas

class TestData:
    @staticmethod
    def generate_ticket_data():
        """Genera datos realistas para crear tickets"""
        categories = ['SISTEMAS', 'SOPORTE', 'TELECOMUNICACIONES']
        priorities = ['BAJA', 'MEDIA', 'ALTA', 'CRITICA']
        
        problems = [
            "No puedo acceder al sistema",
            "Error en la aplicación web",
            "Problema con la impresora",
            "Solicitud de nuevo usuario",
            "Cambio de contraseña",
            "Error en base de datos",
            "Lentitud en el sistema",
            "Problema de conectividad"
        ]
        
        return {
            'titulo': f"{random.choice(problems)} - {fake.company()}",
            'descripcion': fake.text(max_nb_chars=200),
            'categoria': random.choice(categories),
            'prioridad': random.choice(priorities),
            'usuario_creador': fake.email()
        }
    
    @staticmethod
    def generate_user_data():
        """Genera datos para crear usuarios"""
        roles = ['ADMIN', 'TECNICO', 'USER']
        companies = ['ASESORIAS Y GESTION EMPRESARIALES SA', 'TECH CORP', 'SISTEMAS LTDA']
        
        return {
            'nombre': fake.name(),
            'email': fake.email(),
            'empresa': random.choice(companies),
            'rol': random.choice(roles),
            'activo': True
        }