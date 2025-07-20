from locust import HttpUser, task, between
import random
import json

class SysTicketUser(HttpUser):
    wait_time = between(1, 3)
    
    # URL correcta del backend
    host = "https://backend-tickets.lemonfield-074f6e94.westus2.azurecontainerapps.io"
    
    def on_start(self):
        """Login al iniciar usuario"""
        print("🚀 Iniciando usuario en SysTicket...")
        self.login()
    
    def login(self):
        """Login usando el endpoint real /api/auth/login"""
        login_data = {
            'email': 'admin@admin.cl',
            'password': 'Amanda.05'
        }
        
        print("🔐 Realizando login...")
        
        with self.client.post("/api/auth/login", json=login_data, catch_response=True) as response:
            if response.status_code == 200:
                try:
                    result = response.json()
                    token = result.get('token')
                    if token:
                        self.client.headers.update({
                            'Authorization': f'Bearer {token}',
                            'Content-Type': 'application/json'
                        })
                        print("✅ Login exitoso!")
                        response.success()
                        return True
                except Exception as e:
                    print(f"❌ Error procesando token: {e}")
            
            response.failure(f"Login failed: {response.status_code}")
            return False

    # ==================== TICKETS CRUD ====================
    
    @task(20)
    def crear_ticket(self):
        """POST /api/tickets - Crear nuevo ticket"""
        ticket_data = {
            'titulo': f'Ticket Locust #{random.randint(1000, 9999)}',
            'descripcion': 'Ticket creado por Locust para pruebas de carga del sistema',
            'categoriaId': random.choice([1, 2, 3]),  # Asumiendo que hay categorías con estos IDs
            'prioridad': random.choice(['BAJA', 'MEDIA', 'ALTA', 'CRITICA'])
        }
        
        with self.client.post("/api/tickets", json=ticket_data, catch_response=True) as response:
            if response.status_code == 201:
                response.success()
                try:
                    result = response.json()
                    ticket_id = result.get('id')
                    if ticket_id:
                        # Guardar ID para operaciones posteriores
                        if not hasattr(self, 'ticket_ids'):
                            self.ticket_ids = []
                        self.ticket_ids.append(ticket_id)
                        print(f"✅ Ticket creado: ID {ticket_id}")
                except:
                    pass
            else:
                response.failure(f"Failed to create ticket: {response.status_code}")
    
    @task(15)
    def obtener_mis_tickets(self):
        """GET /api/tickets/mis-tickets - Obtener tickets del usuario"""
        with self.client.get("/api/tickets/mis-tickets", catch_response=True) as response:
            if response.status_code == 200:
                response.success()
                try:
                    result = response.json()
                    total = result.get('totalElements', 0)
                    print(f"📋 Mis tickets: {total}")
                except:
                    pass
            else:
                response.failure(f"Failed to get my tickets: {response.status_code}")
    
    @task(10)
    def obtener_mis_asignaciones(self):
        """GET /api/tickets/mis-asignaciones - Tickets asignados al técnico"""
        with self.client.get("/api/tickets/mis-asignaciones", catch_response=True) as response:
            if response.status_code == 200:
                response.success()
            else:
                response.failure(f"Failed to get assignments: {response.status_code}")
    
    @task(8)
    def obtener_tickets_sin_asignar(self):
        """GET /api/tickets/sin-asignar - Tickets disponibles"""
        with self.client.get("/api/tickets/sin-asignar", catch_response=True) as response:
            if response.status_code == 200:
                response.success()
            else:
                response.failure(f"Failed to get unassigned tickets: {response.status_code}")
    
    @task(12)
    def buscar_tickets(self):
        """GET /api/tickets/buscar - Buscar tickets por keyword"""
        keywords = ['problema', 'error', 'solicitud', 'acceso', 'sistema']
        keyword = random.choice(keywords)
        
        params = {'keyword': keyword}
        
        with self.client.get("/api/tickets/buscar", params=params, catch_response=True) as response:
            if response.status_code == 200:
                response.success()
            else:
                response.failure(f"Failed to search tickets: {response.status_code}")
    
    @task(7)
    def obtener_tickets_por_estado(self):
        """GET /api/tickets/estado/{estado} - Filtrar por estado"""
        estados = ['NUEVO', 'ASIGNADO', 'EN_PROGRESO', 'EN_ESPERA', 'RESUELTO', 'CERRADO']
        estado = random.choice(estados)
        
        with self.client.get(f"/api/tickets/estado/{estado}", catch_response=True) as response:
            if response.status_code == 200:
                response.success()
            else:
                response.failure(f"Failed to get tickets by status {estado}: {response.status_code}")
    
    @task(5)
    def obtener_ticket_individual(self):
        """GET /api/tickets/{id} - Obtener ticket específico"""
        if hasattr(self, 'ticket_ids') and self.ticket_ids:
            ticket_id = random.choice(self.ticket_ids)
            
            with self.client.get(f"/api/tickets/{ticket_id}", catch_response=True) as response:
                if response.status_code == 200:
                    response.success()
                else:
                    response.failure(f"Failed to get ticket {ticket_id}: {response.status_code}")

    # ==================== ESTADÍSTICAS ====================
    
    @task(15)
    def obtener_estadisticas(self):
        """GET /api/tickets/estadisticas - Stats generales"""
        with self.client.get("/api/tickets/estadisticas", catch_response=True) as response:
            if response.status_code == 200:
                response.success()
                try:
                    result = response.json()
                    print(f"📊 Stats: {result.get('nuevo', 0)} nuevos, {result.get('asignado', 0)} asignados")
                except:
                    pass
            else:
                response.failure(f"Failed to get statistics: {response.status_code}")
    
    @task(10)
    def obtener_mis_estadisticas(self):
        """GET /api/tickets/mis-estadisticas - Stats del usuario"""
        with self.client.get("/api/tickets/mis-estadisticas", catch_response=True) as response:
            if response.status_code == 200:
                response.success()
            else:
                response.failure(f"Failed to get my statistics: {response.status_code}")
    
    @task(8)
    def obtener_tickets_recientes(self):
        """GET /api/tickets/recientes - Últimos 5 tickets"""
        with self.client.get("/api/tickets/recientes", catch_response=True) as response:
            if response.status_code == 200:
                response.success()
            else:
                response.failure(f"Failed to get recent tickets: {response.status_code}")
    
    @task(6)
    def obtener_datos_mensuales(self):
        """GET /api/tickets/datos-mensuales - Datos para gráficos"""
        with self.client.get("/api/tickets/datos-mensuales", catch_response=True) as response:
            if response.status_code == 200:
                response.success()
            else:
                response.failure(f"Failed to get monthly data: {response.status_code}")
    
    @task(6)
    def obtener_datos_categoria(self):
        """GET /api/tickets/datos-categoria - Distribución por categoría"""
        with self.client.get("/api/tickets/datos-categoria", catch_response=True) as response:
            if response.status_code == 200:
                response.success()
            else:
                response.failure(f"Failed to get category data: {response.status_code}")
    
    @task(5)
    def obtener_mis_datos_mensuales(self):
        """GET /api/tickets/mis-datos-mensuales - Datos mensuales del usuario"""
        with self.client.get("/api/tickets/mis-datos-mensuales", catch_response=True) as response:
            if response.status_code == 200:
                response.success()
            else:
                response.failure(f"Failed to get my monthly data: {response.status_code}")
    
    @task(5)
    def obtener_mis_datos_categoria(self):
        """GET /api/tickets/mis-datos-categoria - Datos por categoría del usuario"""
        with self.client.get("/api/tickets/mis-datos-categoria", catch_response=True) as response:
            if response.status_code == 200:
                response.success()
            else:
                response.failure(f"Failed to get my category data: {response.status_code}")

    # ==================== OPERACIONES CON TICKETS ====================
    
    @task(3)
    def cambiar_estado_ticket(self):
        """PUT /api/tickets/{id}/estado - Cambiar estado de ticket"""
        if hasattr(self, 'ticket_ids') and self.ticket_ids:
            ticket_id = random.choice(self.ticket_ids)
            nuevo_estado = random.choice(['EN_PROGRESO', 'EN_ESPERA', 'RESUELTO'])
            
            data = {'estado': nuevo_estado}
            
            with self.client.put(f"/api/tickets/{ticket_id}/estado", json=data, catch_response=True) as response:
                if response.status_code == 200:
                    response.success()
                    print(f"🔄 Estado cambiado: Ticket {ticket_id} → {nuevo_estado}")
                else:
                    response.failure(f"Failed to change status: {response.status_code}")
    
    @task(2)
    def tomar_ticket(self):
        """PUT /api/tickets/{id}/tomar - Tomar ticket para sí mismo"""
        if hasattr(self, 'ticket_ids') and self.ticket_ids:
            ticket_id = random.choice(self.ticket_ids)
            
            with self.client.put(f"/api/tickets/{ticket_id}/tomar", catch_response=True) as response:
                if response.status_code == 200:
                    response.success()
                    print(f"👤 Ticket tomado: {ticket_id}")
                else:
                    response.failure(f"Failed to take ticket: {response.status_code}")
    
    @task(3)
    def agregar_comentario(self):
        """POST /api/tickets/{id}/comentarios - Agregar comentario"""
        if hasattr(self, 'ticket_ids') and self.ticket_ids:
            ticket_id = random.choice(self.ticket_ids)
            
            comentario_data = {
                'texto': f'Comentario de prueba desde Locust - {random.randint(100, 999)}',
                'esPrivado': random.choice([True, False])
            }
            
            with self.client.post(f"/api/tickets/{ticket_id}/comentarios", json=comentario_data, catch_response=True) as response:
                if response.status_code == 201:
                    response.success()
                    print(f"💬 Comentario agregado al ticket {ticket_id}")
                else:
                    response.failure(f"Failed to add comment: {response.status_code}")
    
    @task(2)
    def obtener_comentarios(self):
        """GET /api/tickets/{id}/comentarios - Obtener comentarios de ticket"""
        if hasattr(self, 'ticket_ids') and self.ticket_ids:
            ticket_id = random.choice(self.ticket_ids)
            
            with self.client.get(f"/api/tickets/{ticket_id}/comentarios", catch_response=True) as response:
                if response.status_code == 200:
                    response.success()
                else:
                    response.failure(f"Failed to get comments: {response.status_code}")
    
    @task(2)
    def obtener_historial(self):
        """GET /api/tickets/{id}/historial - Obtener historial de cambios"""
        if hasattr(self, 'ticket_ids') and self.ticket_ids:
            ticket_id = random.choice(self.ticket_ids)
            
            with self.client.get(f"/api/tickets/{ticket_id}/historial", catch_response=True) as response:
                if response.status_code == 200:
                    response.success()
                else:
                    response.failure(f"Failed to get history: {response.status_code}")

    # ==================== ENDPOINTS ADMINISTRATIVOS ====================
    
    @task(4)
    def obtener_todos_tickets_admin(self):
        """GET /api/tickets/administracion/todos - Vista administrativa (requiere ADMIN)"""
        params = {
            'page': 0,
            'size': 10,
            'sort': 'fechaCreacion,desc'
        }
        
        with self.client.get("/api/tickets/administracion/todos", params=params, catch_response=True) as response:
            if response.status_code == 200:
                response.success()
            elif response.status_code == 403:
                response.success()  # Esperado si no es admin
                print("⚠️  Usuario no es admin - 403 esperado")
            else:
                response.failure(f"Failed to get admin tickets: {response.status_code}")
    
    @task(3)
    def obtener_estadisticas_admin(self):
        """GET /api/tickets/administracion/estadisticas - Stats admin"""
        with self.client.get("/api/tickets/administracion/estadisticas", catch_response=True) as response:
            if response.status_code == 200:
                response.success()
            elif response.status_code == 403:
                response.success()  # Esperado si no es admin
            else:
                response.failure(f"Failed to get admin stats: {response.status_code}")

    # ==================== UTILIDADES ====================
    
    @task(1)
    def obtener_ultimo_correlativo(self):
        """GET /api/tickets/ultimo-correlativo - Obtener último número"""
        with self.client.get("/api/tickets/ultimo-correlativo", catch_response=True) as response:
            if response.status_code == 200:
                response.success()
            else:
                response.failure(f"Failed to get correlativo: {response.status_code}")

class AdminUser(HttpUser):
    """Usuario con privilegios de administrador"""
    wait_time = between(2, 5)
    weight = 1  # Menos usuarios admin
    
    host = "https://backend-tickets.lemonfield-074f6e94.westus2.azurecontainerapps.io"
    
    def on_start(self):
        """Login como admin"""
        self.login()
    
    def login(self):
        """Login con credenciales de admin"""
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
                    response.success()
    
    @task(30)
    def admin_obtener_todos_tickets(self):
        """Operaciones administrativas con filtros"""
        params = {
            'page': random.randint(0, 2),
            'size': random.choice([10, 20, 50]),
            'estado': random.choice(['NUEVO', 'ASIGNADO', 'EN_PROGRESO', '']),
            'prioridad': random.choice(['ALTA', 'CRITICA', 'MEDIA', '']),
            'sort': random.choice(['fechaCreacion,desc', 'prioridad,asc', 'estado,desc'])
        }
        
        # Remover parámetros vacíos
        params = {k: v for k, v in params.items() if v != ''}
        
        with self.client.get("/api/tickets/administracion/todos", params=params, catch_response=True) as response:
            if response.status_code == 200:
                response.success()
            else:
                response.failure(f"Admin tickets failed: {response.status_code}")
    
    @task(15)
    def admin_estadisticas(self):
        """Estadísticas administrativas"""
        with self.client.get("/api/tickets/administracion/estadisticas", catch_response=True) as response:
            if response.status_code == 200:
                response.success()
            else:
                response.failure(f"Admin stats failed: {response.status_code}")
    
    @task(5)
    def admin_asignacion_masiva(self):
        """Asignación masiva de tickets"""
        # Simular asignación masiva
        data = {
            'ticketIds': [1, 2, 3],  # IDs de ejemplo
            'tecnicoId': 1
        }
        
        with self.client.post("/api/tickets/administracion/asignar-masivo", json=data, catch_response=True) as response:
            if response.status_code == 200:
                response.success()
            else:
                response.failure(f"Mass assignment failed: {response.status_code}")
    
    @task(5)
    def admin_cambio_estado_masivo(self):
        """Cambio de estado masivo"""
        data = {
            'ticketIds': [1, 2, 3],
            'nuevoEstado': 'EN_PROGRESO'
        }
        
        with self.client.post("/api/tickets/administracion/cambiar-estado-masivo", json=data, catch_response=True) as response:
            if response.status_code == 200:
                response.success()
            else:
                response.failure(f"Mass status change failed: {response.status_code}")

class RegularUser(HttpUser):
    """Usuario regular del sistema"""
    wait_time = between(1, 4)
    weight = 8  # Más usuarios regulares
    
    host = "https://backend-tickets.lemonfield-074f6e94.westus2.azurecontainerapps.io"
    
    tasks = [SysTicketUser]  # Usar las tareas del usuario regular