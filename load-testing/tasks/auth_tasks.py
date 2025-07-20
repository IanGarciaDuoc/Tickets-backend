from locust import task, TaskSet
from config.environments import Config
from utils.api_client import APIClient

class AuthTasks(TaskSet):
    def on_start(self):
        """Se ejecuta cuando inicia el usuario"""
        self.client_api = APIClient(Config.BASE_URL)
        self.login()
    
    def login(self):
        """Realizar login"""
        success = self.client_api.login(Config.ADMIN_USER, Config.ADMIN_PASSWORD)
        if not success:
            self.user.environment.runner.quit()
    
    @task(1)
    def verify_session(self):
        """Verificar que la sesión está activa"""
        with self.client.get("/api/auth/verify-token", 
                           headers={'Authorization': f'Bearer {self.client_api.token}'},
                           catch_response=True) as response:
            if response.status_code == 200:
                response.success()
            else:
                response.failure(f"Session verification failed: {response.status_code}")