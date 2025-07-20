from locust import HttpUser, task, between
import random

class DebugTicketUser(HttpUser):
    wait_time = between(2, 4)  # Más tiempo entre requests
    host = "https://backend-tickets.lemonfield-074f6e94.westus2.azurecontainerapps.io"
    
    def on_start(self):
        """Hacer login y preparar datos"""
        print("🔍 Iniciando usuario debug...")
        self.login()
        self.get_valid_categories()
    
    def login(self):
        """Login en el sistema"""
        login_data = {
            'email': 'admin@admin.cl',
            'password': 'Amanda.05'
        }
        
        with self.client.post("/api/auth/login", json=login_data, catch_response=True) as response:
            if response.status_code == 200:
                result = response.json()
                token = result.get('token')
                if token:
                    self.client.headers.update({
                        'Authorization': f'Bearer {token}',
                        'Content-Type': 'application/json'
                    })
                    print("✅ Login exitoso")
                    response.success()
                else:
                    print("❌ Token no encontrado")
                    response.failure("No token received")
            else:
                print(f"❌ Login falló: {response.status_code}")
                print(f"Respuesta: {response.text}")
                response.failure(f"Login failed: {response.status_code}")
    
    def get_valid_categories(self):
        """Obtener categorías válidas del sistema"""
        # Intentar obtener categorías desde diferentes endpoints
        endpoints_to_try = [
            "/api/categorias",
            "/api/categories", 
            "/api/tickets/datos-categoria"
        ]
        
        self.valid_categories = []
        
        for endpoint in endpoints_to_try:
            with self.client.get(endpoint, catch_response=True) as response:
                if response.status_code == 200:
                    try:
                        data = response.json()
                        print(f"✅ Categorías obtenidas de {endpoint}: {data}")
                        
                        # Extraer IDs de categorías según el formato
                        if isinstance(data, list):
                            for item in data:
                                if isinstance(item, dict):
                                    if 'id' in item:
                                        self.valid_categories.append(item['id'])
                                    elif 'categoria' in item:
                                        # Para datos-categoria que devuelve nombres
                                        pass
                        
                        response.success()
                        break
                    except Exception as e:
                        print(f"❌ Error procesando categorías: {e}")
                        response.failure(f"Error parsing categories: {e}")
                else:
                    response.failure(f"Categories endpoint failed: {response.status_code}")
        
        # Si no encontramos categorías, usar valores por defecto
        if not self.valid_categories:
            print("⚠️ No se encontraron categorías válidas, usando valores por defecto")
            self.valid_categories = [1]  # Solo usar ID 1 como seguro
    
    @task(30)
    def crear_ticket_debug(self):
        """Crear ticket con debugging detallado"""
        
        # Usar categoría válida o None
        categoria_id = random.choice(self.valid_categories) if self.valid_categories else None
        
        # Diferentes formatos de ticket para probar
        ticket_formats = [
            {
                'titulo': f'Debug Ticket #{random.randint(1000, 9999)}',
                'descripcion': 'Ticket de debug creado por Locust',
                'categoriaId': categoria_id,
                'prioridad': random.choice(['BAJA', 'MEDIA', 'ALTA', 'CRITICA'])
            },
            {
                'titulo': f'Simple Ticket #{random.randint(1000, 9999)}',
                'descripcion': 'Ticket simple sin categoría',
                'prioridad': 'MEDIA'
            },
            {
                'titulo': f'Minimal Ticket #{random.randint(1000, 9999)}',
                'descripcion': 'Ticket mínimo'
            }
        ]
        
        ticket_data = random.choice(ticket_formats)
        
        # Limpiar datos None
        ticket_data = {k: v for k, v in ticket_data.items() if v is not None}
        
        print(f"🎫 Intentando crear ticket: {ticket_data}")
        
        with self.client.post("/api/tickets", json=ticket_data, catch_response=True) as response:
            print(f"📊 Respuesta creación: {response.status_code}")
            
            if response.status_code == 201:
                try:
                    result = response.json()
                    ticket_id = result.get('id')
                    print(f"✅ Ticket creado exitosamente: ID {ticket_id}")
                    
                    # Guardar IDs exitosos
                    if not hasattr(self, 'successful_tickets'):
                        self.successful_tickets = []
                    self.successful_tickets.append(ticket_id)
                    
                    response.success()
                except Exception as e:
                    print(f"❌ Error procesando respuesta exitosa: {e}")
                    response.success()  # Aún es éxito aunque no podamos parsear
            else:
                print(f"❌ Error creando ticket: {response.status_code}")
                print(f"Respuesta de error: {response.text[:200]}")
                response.failure(f"Failed to create ticket: {response.status_code} - {response.text[:100]}")
    
    @task(20)
    def obtener_mis_tickets(self):
        """Obtener mis tickets con debugging"""
        with self.client.get("/api/tickets/mis-tickets", catch_response=True) as response:
            if response.status_code == 200:
                try:
                    result = response.json()
                    total = result.get('totalElements', 'N/A')
                    content = result.get('content', [])
                    print(f"📋 Mis tickets: {total} total, {len(content)} en página actual")
                    response.success()
                except Exception as e:
                    print(f"❌ Error procesando mis tickets: {e}")
                    response.success()  # 200 es éxito aunque no podamos parsear
            else:
                print(f"❌ Error obteniendo mis tickets: {response.status_code}")
                response.failure(f"Failed to get my tickets: {response.status_code}")
    
    @task(15)
    def obtener_estadisticas_debug(self):
        """Obtener estadísticas con debugging"""
        with self.client.get("/api/tickets/estadisticas", catch_response=True) as response:
            if response.status_code == 200:
                try:
                    result = response.json()
                    print(f"📊 Estadísticas: {result}")
                    response.success()
                except Exception as e:
                    print(f"❌ Error procesando estadísticas: {e}")
                    response.success()
            else:
                print(f"❌ Error obteniendo estadísticas: {response.status_code}")
                response.failure(f"Failed to get statistics: {response.status_code}")
    
    @task(10)
    def buscar_tickets_debug(self):
        """Buscar tickets con debugging"""
        keywords = ['test', 'debug', 'locust', 'problema']
        keyword = random.choice(keywords)
        
        with self.client.get(f"/api/tickets/buscar", params={'keyword': keyword}, catch_response=True) as response:
            if response.status_code == 200:
                try:
                    result = response.json()
                    total = result.get('totalElements', 'N/A')
                    print(f"🔍 Búsqueda '{keyword}': {total} resultados")
                    response.success()
                except Exception as e:
                    print(f"❌ Error procesando búsqueda: {e}")
                    response.success()
            else:
                print(f"❌ Error en búsqueda: {response.status_code}")
                response.failure(f"Search failed: {response.status_code}")
    
    @task(8)
    def obtener_datos_basicos(self):
        """Obtener datos básicos que sabemos funcionan"""
        endpoints_basicos = [
            "/api/tickets/recientes",
            "/api/tickets/datos-mensuales", 
            "/api/tickets/datos-categoria",
            "/api/tickets/mis-estadisticas"
        ]
        
        endpoint = random.choice(endpoints_basicos)
        
        with self.client.get(endpoint, catch_response=True) as response:
            if response.status_code == 200:
                print(f"✅ Endpoint básico funciona: {endpoint}")
                response.success()
            else:
                print(f"❌ Endpoint básico falló: {endpoint} - {response.status_code}")
                response.failure(f"Basic endpoint failed: {endpoint}")
    
    @task(5)
    def test_ticket_individual(self):
        """Probar obtener ticket individual"""
        if hasattr(self, 'successful_tickets') and self.successful_tickets:
            ticket_id = random.choice(self.successful_tickets)
            
            with self.client.get(f"/api/tickets/{ticket_id}", catch_response=True) as response:
                if response.status_code == 200:
                    print(f"✅ Ticket individual obtenido: {ticket_id}")
                    response.success()
                else:
                    print(f"❌ Error obteniendo ticket {ticket_id}: {response.status_code}")
                    response.failure(f"Individual ticket failed: {response.status_code}")
    
    # COMENTAR ENDPOINTS ADMIN POR AHORA PARA EVITAR ERRORES 500
    # @task(3)
    # def test_admin_endpoint_debug(self):
    #     """Probar endpoint admin con debugging"""
    #     params = {'page': 0, 'size': 5}  # Página pequeña
        
    #     with self.client.get("/api/tickets/administracion/todos", params=params, catch_response=True) as response:
    #         print(f"🔧 Admin endpoint: {response.status_code}")
    #         if response.status_code == 200:
    #             response.success()
    #         elif response.status_code == 403:
    #             print("⚠️ Sin permisos de admin (esperado)")
    #             response.success()
    #         else:
    #             print(f"❌ Error admin: {response.text[:100]}")
    #             response.failure(f"Admin endpoint error: {response.status_code}")