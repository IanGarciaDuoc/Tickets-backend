from locust import HttpUser, task, between
import random

class AzureTicketUser(HttpUser):
    wait_time = between(1, 3)
    
    # URL base de tu aplicación en Azure
    host = "https://frontend-tickets-app.lemonfield-074f6e94.westus2.azurecontainerapps.io"
    
    def on_start(self):
        """Login al iniciar"""
        print("Iniciando usuario...")
        self.login()
    
    def login(self):
        """Realizar login"""
        login_data = {
            'email': 'admin@admin.cl',
            'password': 'Amanda.05'
        }
        
        print("Intentando login...")
        
        # Probar diferentes endpoints posibles
        login_endpoints = [
            "/api/auth/login",
            "/login",
            "/api/login",
            "/auth/login"
        ]
        
        for endpoint in login_endpoints:
            with self.client.post(endpoint, json=login_data, catch_response=True) as response:
                print(f"Probando endpoint: {endpoint}")
                print(f"Status: {response.status_code}")
                print(f"Response: {response.text[:200]}")
                
                if response.status_code == 200:
                    try:
                        result = response.json()
                        token = result.get('token')
                        if token:
                            self.client.headers.update({
                                'Authorization': f'Bearer {token}',
                                'Content-Type': 'application/json'
                            })
                            print("Login exitoso!")
                            response.success()
                            return
                    except:
                        pass
                
                response.failure(f"Login failed on {endpoint}")
        
        print("Todos los endpoints de login fallaron")
    
    @task(10)
    def test_home_page(self):
        """Probar página principal"""
        with self.client.get("/", catch_response=True) as response:
            if response.status_code == 200:
                response.success()
            else:
                response.failure(f"Home page failed: {response.status_code}")
    
    @task(5)
    def test_login_page(self):
        """Probar página de login"""
        with self.client.get("/login", catch_response=True) as response:
            if response.status_code == 200:
                response.success()
            else:
                response.failure(f"Login page failed: {response.status_code}")
    
    @task(3)
    def test_api_health(self):
        """Probar endpoint de salud"""
        endpoints = ["/health", "/api/health", "/actuator/health"]
        
        for endpoint in endpoints:
            with self.client.get(endpoint, catch_response=True) as response:
                if response.status_code == 200:
                    response.success()
                    return
                else:
                    response.failure(f"Health check failed on {endpoint}")
    
    @task(2)
    def test_tickets_api(self):
        """Probar API de tickets"""
        endpoints = ["/api/tickets", "/tickets", "/api/v1/tickets"]
        
        for endpoint in endpoints:
            with self.client.get(endpoint, catch_response=True) as response:
                print(f"Testing tickets endpoint: {endpoint} - Status: {response.status_code}")
                if response.status_code in [200, 401]:  # 401 es esperado sin auth
                    response.success()
                    return
                else:
                    response.failure(f"Tickets API failed on {endpoint}")