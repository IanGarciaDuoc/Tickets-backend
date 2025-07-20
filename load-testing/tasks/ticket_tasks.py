from locust import task, TaskSet
import random
import json
from config.test_data import TestData
from utils.api_client import APIClient
from config.environments import Config

class TicketTasks(TaskSet):
    def on_start(self):
        """Inicialización del usuario"""
        self.client_api = APIClient(Config.BASE_URL)
        self.client_api.login(Config.ADMIN_USER, Config.ADMIN_PASSWORD)
        self.created_tickets = []
    
    @task(10)
    def list_tickets(self):
        """Obtener lista de tickets"""
        with self.client.get("/api/tickets", 
                           headers={'Authorization': f'Bearer {self.client_api.token}'},
                           catch_response=True) as response:
            if response.status_code == 200:
                response.success()
                tickets = response.json()
                if tickets and len(tickets) > 0:
                    # Guardar algunos IDs para operaciones posteriores
                    self.created_tickets = [t.get('id') for t in tickets[:5]]
            else:
                response.failure(f"Failed to get tickets: {response.status_code}")
    
    @task(5)
    def create_ticket(self):
        """Crear nuevo ticket"""
        ticket_data = TestData.generate_ticket_data()
        
        with self.client.post("/api/tickets", 
                            json=ticket_data,
                            headers={'Authorization': f'Bearer {self.client_api.token}'},
                            catch_response=True) as response:
            if response.status_code in [200, 201]:
                response.success()
                result = response.json()
                ticket_id = result.get('id')
                if ticket_id:
                    self.created_tickets.append(ticket_id)
            else:
                response.failure(f"Failed to create ticket: {response.status_code}")
    
    @task(7)
    def get_ticket_details(self):
        """Obtener detalles de un ticket específico"""
        if not self.created_tickets:
            return
        
        ticket_id = random.choice(self.created_tickets)
        with self.client.get(f"/api/tickets/{ticket_id}", 
                           headers={'Authorization': f'Bearer {self.client_api.token}'},
                           catch_response=True) as response:
            if response.status_code == 200:
                response.success()
            else:
                response.failure(f"Failed to get ticket {ticket_id}: {response.status_code}")
    
    @task(3)
    def update_ticket_status(self):
        """Actualizar estado de ticket"""
        if not self.created_tickets:
            return
        
        ticket_id = random.choice(self.created_tickets)
        statuses = ['EN_PROGRESO', 'ASIGNADO', 'RESUELTO']
        update_data = {
            'estado': random.choice(statuses)
        }
        
        with self.client.put(f"/api/tickets/{ticket_id}/status", 
                           json=update_data,
                           headers={'Authorization': f'Bearer {self.client_api.token}'},
                           catch_response=True) as response:
            if response.status_code == 200:
                response.success()
            else:
                response.failure(f"Failed to update ticket {ticket_id}: {response.status_code}")
    
    @task(2)
    def search_tickets(self):
        """Buscar tickets con filtros"""
        search_params = {
            'estado': random.choice(['NUEVO', 'ASIGNADO', 'EN_PROGRESO']),
            'categoria': random.choice(['SISTEMAS', 'SOPORTE']),
            'limit': 20
        }
        
        with self.client.get("/api/tickets/search", 
                           params=search_params,
                           headers={'Authorization': f'Bearer {self.client_api.token}'},
                           catch_response=True) as response:
            if response.status_code == 200:
                response.success()
            else:
                response.failure(f"Failed to search tickets: {response.status_code}")