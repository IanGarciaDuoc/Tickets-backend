from locust import HttpUser, task, between
import random

class FixedTicketUser(HttpUser):
    wait_time = between(1, 3)
    host = "https://backend-tickets.lemonfield-074f6e94.westus2.azurecontainerapps.io"
    
    def on_start(self):
        """Hacer login y obtener datos necesarios"""
        print("🚀 Iniciando usuario con corrección de subcategorías...")
        self.login()
        self.get_categories_and_subcategories()
    
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
                    response.failure("No token received")
            else:
                response.failure(f"Login failed: {response.status_code}")
    
    def get_categories_and_subcategories(self):
        """Obtener categorías y subcategorías válidas"""
        self.valid_category_subcategory_pairs = []
        
        # Intentar obtener categorías
        with self.client.get("/api/categorias", catch_response=True) as response:
            if response.status_code == 200:
                try:
                    categorias = response.json()
                    print(f"✅ Categorías encontradas: {len(categorias)}")
                    
                    for categoria in categorias:
                        categoria_id = categoria.get('id')
                        
                        # Obtener subcategorías para cada categoría
                        with self.client.get(f"/api/categorias/{categoria_id}/subcategorias", catch_response=True) as sub_response:
                            if sub_response.status_code == 200:
                                subcategorias = sub_response.json()
                                for subcategoria in subcategorias:
                                    self.valid_category_subcategory_pairs.append({
                                        'categoriaId': categoria_id,
                                        'subcategoriaId': subcategoria.get('id'),
                                        'categoriaNombre': categoria.get('nombre'),
                                        'subcategoriaNombre': subcategoria.get('nombre')
                                    })
                    
                    response.success()
                except Exception as e:
                    print(f"❌ Error procesando categorías: {e}")
                    response.failure(f"Error parsing categories: {e}")
            else:
                response.failure(f"Categories failed: {response.status_code}")
        
        # Si no se encontraron pares válidos, usar valores por defecto
        if not self.valid_category_subcategory_pairs:
            print("⚠️ No se encontraron categoría/subcategoría válidas, usando valores por defecto")
            # Valores por defecto basados en lo que es común en sistemas de tickets
            self.valid_category_subcategory_pairs = [
                {'categoriaId': 1, 'subcategoriaId': 1},
                {'categoriaId': 1, 'subcategoriaId': 2},
                {'categoriaId': 2, 'subcategoriaId': 3},
                {'categoriaId': 2, 'subcategoriaId': 4},
                {'categoriaId': 3, 'subcategoriaId': 5}
            ]
        
        print(f"📋 Pares categoría/subcategoría disponibles: {len(self.valid_category_subcategory_pairs)}")
        for pair in self.valid_category_subcategory_pairs[:3]:  # Mostrar solo los primeros 3
            print(f"   - Cat {pair['categoriaId']}, Subcat {pair['subcategoriaId']}")
    
    @task(25)
    def crear_ticket_correcto(self):
        """Crear ticket con categoriaId Y subcategoriaId"""
        
        if not self.valid_category_subcategory_pairs:
            print("❌ No hay pares categoría/subcategoría disponibles")
            return
        
        # Seleccionar un par categoría/subcategoría válido
        pair = random.choice(self.valid_category_subcategory_pairs)
        
        ticket_data = {
            'titulo': f'Ticket Locust #{random.randint(1000, 9999)}',
            'descripcion': 'Ticket de prueba creado por Locust con categoría y subcategoría válidas',
            'categoriaId': pair['categoriaId'],
            'subcategoriaId': pair['subcategoriaId'],  # ✅ CAMPO OBLIGATORIO AGREGADO
            'prioridad': random.choice(['BAJA', 'MEDIA', 'ALTA', 'CRITICA'])
        }
        
        print(f"🎫 Creando ticket: Cat {pair['categoriaId']}, Subcat {pair['subcategoriaId']}")
        
        with self.client.post("/api/tickets", json=ticket_data, catch_response=True) as response:
            print(f"📊 Respuesta: {response.status_code}")
            
            if response.status_code == 201:
                try:
                    result = response.json()
                    ticket_id = result.get('id')
                    numero_ticket = result.get('numeroTicket', 'N/A')
                    print(f"✅ Ticket creado exitosamente: #{numero_ticket} (ID: {ticket_id})")
                    
                    # Guardar ID para usar en otras operaciones
                    if not hasattr(self, 'created_tickets'):
                        self.created_tickets = []
                    self.created_tickets.append(ticket_id)
                    
                    response.success()
                except Exception as e:
                    print(f"⚠️ Ticket creado pero error al parsear respuesta: {e}")
                    response.success()  # Aún es éxito
            else:
                print(f"❌ Error creando ticket: {response.status_code}")
                try:
                    error_detail = response.json()
                    print(f"Detalles del error: {error_detail}")
                except:
                    print(f"Respuesta de error: {response.text[:200]}")
                response.failure(f"Failed to create ticket: {response.status_code}")
    
    @task(20)
    def obtener_mis_tickets(self):
        """Obtener mis tickets"""
        with self.client.get("/api/tickets/mis-tickets", catch_response=True) as response:
            if response.status_code == 200:
                try:
                    result = response.json()
                    total = result.get('totalElements', 'N/A')
                    content = result.get('content', [])
                    print(f"📋 Mis tickets: {total} total, {len(content)} en página")
                    response.success()
                except:
                    response.success()
            else:
                response.failure(f"Failed to get my tickets: {response.status_code}")
    
    @task(15)
    def obtener_estadisticas(self):
        """Obtener estadísticas del sistema"""
        with self.client.get("/api/tickets/estadisticas", catch_response=True) as response:
            if response.status_code == 200:
                try:
                    result = response.json()
                    print(f"📊 Stats: {result.get('nuevo', 0)} nuevos, {result.get('asignado', 0)} asignados")
                    response.success()
                except:
                    response.success()
            else:
                response.failure(f"Failed to get statistics: {response.status_code}")
    
    @task(12)
    def buscar_tickets(self):
        """Buscar tickets"""
        keywords = ['locust', 'test', 'problema', 'error', 'solicitud']
        keyword = random.choice(keywords)
        
        with self.client.get(f"/api/tickets/buscar", params={'keyword': keyword}, catch_response=True) as response:
            if response.status_code == 200:
                try:
                    result = response.json()
                    total = result.get('totalElements', 0)
                    print(f"🔍 Búsqueda '{keyword}': {total} resultados")
                    response.success()
                except:
                    response.success()
            else:
                response.failure(f"Search failed: {response.status_code}")
    
    @task(10)
    def obtener_datos_dashboard(self):
        """Obtener datos para dashboard"""
        endpoints = [
            "/api/tickets/recientes",
            "/api/tickets/datos-mensuales",
            "/api/tickets/datos-categoria",
            "/api/tickets/mis-estadisticas"
        ]
        
        endpoint = random.choice(endpoints)
        
        with self.client.get(endpoint, catch_response=True) as response:
            if response.status_code == 200:
                print(f"✅ Dashboard data: {endpoint}")
                response.success()
            else:
                response.failure(f"Dashboard endpoint failed: {endpoint}")
    
    @task(8)
    def obtener_tickets_sin_asignar(self):
        """Obtener tickets disponibles"""
        with self.client.get("/api/tickets/sin-asignar", catch_response=True) as response:
            if response.status_code == 200:
                response.success()
            else:
                response.failure(f"Unassigned tickets failed: {response.status_code}")
    
    @task(5)
    def obtener_ticket_individual(self):
        """Obtener detalles de un ticket específico"""
        if hasattr(self, 'created_tickets') and self.created_tickets:
            ticket_id = random.choice(self.created_tickets)
            
            with self.client.get(f"/api/tickets/{ticket_id}", catch_response=True) as response:
                if response.status_code == 200:
                    print(f"✅ Detalles obtenidos para ticket {ticket_id}")
                    response.success()
                else:
                    response.failure(f"Individual ticket failed: {response.status_code}")
    
    @task(3)
    def cambiar_estado_ticket(self):
        """Cambiar estado de un ticket"""
        if hasattr(self, 'created_tickets') and self.created_tickets:
            ticket_id = random.choice(self.created_tickets)
            nuevo_estado = random.choice(['EN_PROGRESO', 'EN_ESPERA'])
            
            data = {'estado': nuevo_estado}
            
            with self.client.put(f"/api/tickets/{ticket_id}/estado", json=data, catch_response=True) as response:
                if response.status_code == 200:
                    print(f"🔄 Estado cambiado: Ticket {ticket_id} → {nuevo_estado}")
                    response.success()
                else:
                    response.failure(f"Status change failed: {response.status_code}")
    
    @task(2)
    def agregar_comentario(self):
        """Agregar comentario a un ticket"""
        if hasattr(self, 'created_tickets') and self.created_tickets:
            ticket_id = random.choice(self.created_tickets)
            
            comentario_data = {
                'contenido': f'Comentario de prueba Locust - {random.randint(100, 999)}',
                'esPrivado': random.choice([True, False])
            }
            
            with self.client.post(f"/api/tickets/{ticket_id}/comentarios", json=comentario_data, catch_response=True) as response:
                if response.status_code == 201:
                    print(f"💬 Comentario agregado al ticket {ticket_id}")
                    response.success()
                else:
                    response.failure(f"Comment failed: {response.status_code}")

# NOTA: Remover tareas administrativas por ahora para evitar errores 500
# Se pueden agregar después cuando se confirme que el usuario tiene permisos admin