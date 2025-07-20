import requests
import json
from requests.adapters import HTTPAdapter
from urllib3.util.retry import Retry

class APIClient:
    def __init__(self, base_url, timeout=30):
        self.base_url = base_url.rstrip('/')
        self.session = requests.Session()
        self.timeout = timeout
        self.token = None
        
        # Configurar reintentos
        retry_strategy = Retry(
            total=3,
            backoff_factor=1,
            status_forcelist=[429, 500, 502, 503, 504],
        )
        adapter = HTTPAdapter(max_retries=retry_strategy)
        self.session.mount("http://", adapter)
        self.session.mount("https://", adapter)
    
    def login(self, username, password):
        """Realizar login y obtener token de autenticación"""
        url = f"{self.base_url}/api/auth/login"
        data = {
            'email': username,
            'password': password
        }
        
        response = self.session.post(url, json=data, timeout=self.timeout)
        if response.status_code == 200:
            result = response.json()
            self.token = result.get('token')
            # Agregar token a headers por defecto
            self.session.headers.update({
                'Authorization': f'Bearer {self.token}',
                'Content-Type': 'application/json'
            })
            return True
        return False
    
    def get(self, endpoint, params=None):
        """Realizar petición GET"""
        url = f"{self.base_url}{endpoint}"
        return self.session.get(url, params=params, timeout=self.timeout)
    
    def post(self, endpoint, data=None):
        """Realizar petición POST"""
        url = f"{self.base_url}{endpoint}"
        return self.session.post(url, json=data, timeout=self.timeout)
    
    def put(self, endpoint, data=None):
        """Realizar petición PUT"""
        url = f"{self.base_url}{endpoint}"
        return self.session.put(url, json=data, timeout=self.timeout)
    
    def delete(self, endpoint):
        """Realizar petición DELETE"""
        url = f"{self.base_url}{endpoint}"
        return self.session.delete(url, timeout=self.timeout)