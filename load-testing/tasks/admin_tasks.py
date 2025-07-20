from locust import task, TaskSet
import random
from config.test_data import TestData
from utils.api_client import APIClient
from config.environments import Config

class AdminTasks(TaskSet):
    def on_start(self):
        """Inicialización para tareas administrativas"""
        self.client_api = APIClient(Config.BASE_URL)
        self.client_api.login(Config.ADMIN_USER, Config.ADMIN_PASSWORD)
    
    @task(5)
    def get_dashboard_stats(self):
        """Obtener estadísticas del dashboard"""
        with self.client.get("/api/dashboard/stats", 
                           headers={'Authorization': f'Bearer {self.client_api.token}'},
                           catch_response=True) as response:
            if response.status_code == 200:
                response.success()
            else:
                response.failure(f"Failed to get dashboard stats: {response.status_code}")
    
    @task(3)
    def get_users_list(self):
        """Obtener lista de usuarios"""
        with self.client.get("/api/users", 
                           headers={'Authorization': f'Bearer {self.client_api.token}'},
                           catch_response=True) as response:
            if response.status_code == 200:
                response.success()
            else:
                response.failure(f"Failed to get users: {response.status_code}")
    
    @task(2)
    def generate_report(self):
        """Generar reporte"""
        report_params = {
            'tipo': random.choice(['tickets_estado', 'tickets_categoria', 'rendimiento_tecnico']),
            'fecha_inicio': '2025-01-01',
            'fecha_fin': '2025-01-31'
        }
        
        with self.client.post("/api/reports/generate", 
                            json=report_params,
                            headers={'Authorization': f'Bearer {self.client_api.token}'},
                            catch_response=True) as response:
            if response.status_code in [200, 202]:
                response.success()
            else:
                response.failure(f"Failed to generate report: {response.status_code}")
    
    @task(1)
    def get_system_config(self):
        """Obtener configuración del sistema"""
        with self.client.get("/api/config/system", 
                           headers={'Authorization': f'Bearer {self.client_api.token}'},
                           catch_response=True) as response:
            if response.status_code == 200:
                response.success()
            else:
                response.failure(f"Failed to get system config: {response.status_code}")