class Config:
    # URLs base para diferentes ambientes
    ENVIRONMENTS = {
        'local': 'http://localhost:8080',
        'azure': 'https://backend-tickets.lemonfield-074f6e94.westus2.azurecontainerapps.io',  # ✅ URL CORRECTA
        'dev': 'https://dev-api.systicket.com',
        'staging': 'https://staging-api.systicket.com'
    }
    
    # Cambiar a ambiente Azure
    CURRENT_ENV = 'azure'
    BASE_URL = ENVIRONMENTS.get(CURRENT_ENV)
    
    # Credenciales de prueba
    ADMIN_USER = 'admin@admin.cl'
    ADMIN_PASSWORD = 'Amanda.05'
    
    # Configuraciones de carga
    DEFAULT_USERS = 10
    DEFAULT_SPAWN_RATE = 2
    DEFAULT_RUN_TIME = '5m'