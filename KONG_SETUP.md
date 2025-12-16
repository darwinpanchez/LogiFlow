# Kong Gateway - Configuración para LogiFlow

## Descripción
Kong API Gateway actúa como punto de entrada único para todos los microservicios de LogiFlow, proporcionando:
- **Enrutamiento centralizado**: Todas las peticiones pasan por el puerto 8000
- **Load balancing**: Distribución de carga entre instancias
- **Rate limiting**: Control de peticiones por segundo
- **Autenticación**: Validación de JWT tokens
- **Logging**: Registro centralizado de todas las peticiones
- **CORS**: Configuración centralizada de CORS

## Arquitectura

```
Cliente/Frontend
       ↓
Kong Gateway (puerto 8000)
       ↓
    ┌──────────┬──────────┬──────────┬──────────┐
    ↓          ↓          ↓          ↓          ↓
auth-service pedido-s  fleet-s  billing-s
  (8082)      (8083)    (8084)    (8085)
```

## Ejecución

### 1. Levantar todos los servicios
```powershell
# Desde el directorio delivery/
docker-compose up -d
```

### 2. Verificar que todos los servicios estén corriendo
```powershell
docker-compose ps
```

Deberías ver 10 contenedores:
- 5 PostgreSQL (auth, pedidos, fleet, billing, kong)
- 4 Microservicios (auth, pedido, fleet, billing)
- 1 Kong Gateway

### 3. Esperar a que Kong esté listo
```powershell
# Verificar salud de Kong
curl http://localhost:8001/status
```

## Configuración de Kong

### Opción 1: Script PowerShell (Automático)

Crea el archivo `kong-config.ps1`:

```powershell
# ================================================
# Kong Configuration Script - LogiFlow
# ================================================

$KONG_ADMIN = "http://localhost:8001"

Write-Host "Configurando Kong Gateway para LogiFlow..." -ForegroundColor Cyan

# ================================================
# 1. AUTH SERVICE
# ================================================
Write-Host "`n[1/4] Configurando Auth Service..." -ForegroundColor Yellow

# Crear servicio
Invoke-RestMethod -Method POST -Uri "$KONG_ADMIN/services" `
  -ContentType "application/json" `
  -Body (@{
    name = "auth-service"
    url = "http://auth-service:8082"
  } | ConvertTo-Json)

# Crear ruta
Invoke-RestMethod -Method POST -Uri "$KONG_ADMIN/services/auth-service/routes" `
  -ContentType "application/json" `
  -Body (@{
    name = "auth-route"
    paths = @("/auth")
    strip_path = $true
  } | ConvertTo-Json)

Write-Host "✓ Auth Service configurado" -ForegroundColor Green

# ================================================
# 2. PEDIDO SERVICE
# ================================================
Write-Host "`n[2/4] Configurando Pedido Service..." -ForegroundColor Yellow

Invoke-RestMethod -Method POST -Uri "$KONG_ADMIN/services" `
  -ContentType "application/json" `
  -Body (@{
    name = "pedido-service"
    url = "http://pedido-service:8083"
  } | ConvertTo-Json)

Invoke-RestMethod -Method POST -Uri "$KONG_ADMIN/services/pedido-service/routes" `
  -ContentType "application/json" `
  -Body (@{
    name = "pedido-route"
    paths = @("/pedidos")
    strip_path = $true
  } | ConvertTo-Json)

Write-Host "✓ Pedido Service configurado" -ForegroundColor Green

# ================================================
# 3. FLEET SERVICE
# ================================================
Write-Host "`n[3/4] Configurando Fleet Service..." -ForegroundColor Yellow

Invoke-RestMethod -Method POST -Uri "$KONG_ADMIN/services" `
  -ContentType "application/json" `
  -Body (@{
    name = "fleet-service"
    url = "http://fleet-service:8084"
  } | ConvertTo-Json)

Invoke-RestMethod -Method POST -Uri "$KONG_ADMIN/services/fleet-service/routes" `
  -ContentType "application/json" `
  -Body (@{
    name = "fleet-route"
    paths = @("/fleet")
    strip_path = $true
  } | ConvertTo-Json)

Write-Host "✓ Fleet Service configurado" -ForegroundColor Green

# ================================================
# 4. BILLING SERVICE
# ================================================
Write-Host "`n[4/4] Configurando Billing Service..." -ForegroundColor Yellow

Invoke-RestMethod -Method POST -Uri "$KONG_ADMIN/services" `
  -ContentType "application/json" `
  -Body (@{
    name = "billing-service"
    url = "http://billing-service:8085"
  } | ConvertTo-Json)

Invoke-RestMethod -Method POST -Uri "$KONG_ADMIN/services/billing-service/routes" `
  -ContentType "application/json" `
  -Body (@{
    name = "billing-route"
    paths = @("/billing")
    strip_path = $true
  } | ConvertTo-Json)

Write-Host "✓ Billing Service configurado" -ForegroundColor Green

# ================================================
# VERIFICACIÓN
# ================================================
Write-Host "`n================================================" -ForegroundColor Cyan
Write-Host "Configuración completada!" -ForegroundColor Green
Write-Host "================================================" -ForegroundColor Cyan

Write-Host "`nServicios configurados:" -ForegroundColor Yellow
$services = Invoke-RestMethod -Method GET -Uri "$KONG_ADMIN/services"
$services.data | ForEach-Object {
  Write-Host "  - $($_.name): $($_.host):$($_.port)" -ForegroundColor White
}

Write-Host "`nRutas configuradas:" -ForegroundColor Yellow
$routes = Invoke-RestMethod -Method GET -Uri "$KONG_ADMIN/routes"
$routes.data | ForEach-Object {
  Write-Host "  - $($_.name): $($_.paths -join ', ')" -ForegroundColor White
}

Write-Host "`n================================================" -ForegroundColor Cyan
Write-Host "Endpoints disponibles a través de Kong:" -ForegroundColor Green
Write-Host "================================================" -ForegroundColor Cyan
Write-Host "Auth:    http://localhost:8000/auth/api/auth/*" -ForegroundColor White
Write-Host "Pedidos: http://localhost:8000/pedidos/api/pedidos/*" -ForegroundColor White
Write-Host "Fleet:   http://localhost:8000/fleet/api/*" -ForegroundColor White
Write-Host "Billing: http://localhost:8000/billing/api/facturas/*" -ForegroundColor White
Write-Host "================================================`n" -ForegroundColor Cyan
```

**Ejecutar el script:**
```powershell
./kong-config.ps1
```

### Opción 2: Configuración Manual con cURL

```powershell
# 1. Auth Service
curl -i -X POST http://localhost:8001/services `
  -H "Content-Type: application/json" `
  -d '{
    "name": "auth-service",
    "url": "http://auth-service:8082"
  }'

curl -i -X POST http://localhost:8001/services/auth-service/routes `
  -H "Content-Type: application/json" `
  -d '{
    "name": "auth-route",
    "paths": ["/auth"],
    "strip_path": true
  }'

# 2. Pedido Service
curl -i -X POST http://localhost:8001/services `
  -H "Content-Type: application/json" `
  -d '{
    "name": "pedido-service",
    "url": "http://pedido-service:8083"
  }'

curl -i -X POST http://localhost:8001/services/pedido-service/routes `
  -H "Content-Type: application/json" `
  -d '{
    "name": "pedido-route",
    "paths": ["/pedidos"],
    "strip_path": true
  }'

# 3. Fleet Service
curl -i -X POST http://localhost:8001/services `
  -H "Content-Type: application/json" `
  -d '{
    "name": "fleet-service",
    "url": "http://fleet-service:8084"
  }'

curl -i -X POST http://localhost:8001/services/fleet-service/routes `
  -H "Content-Type: application/json" `
  -d '{
    "name": "fleet-route",
    "paths": ["/fleet"],
    "strip_path": true
  }'

# 4. Billing Service
curl -i -X POST http://localhost:8001/services `
  -H "Content-Type: application/json" `
  -d '{
    "name": "billing-service",
    "url": "http://billing-service:8085"
  }'

curl -i -X POST http://localhost:8001/services/billing-service/routes `
  -H "Content-Type: application/json" `
  -d '{
    "name": "billing-route",
    "paths": ["/billing"],
    "strip_path": true
  }'
```

## Pruebas de los Endpoints

### 1. Auth Service (a través de Kong)
```powershell
# Registro
curl -X POST http://localhost:8000/auth/api/auth/register `
  -H "Content-Type: application/json" `
  -d '{
    "username": "testuser",
    "email": "test@example.com",
    "password": "password123",
    "nombreCompleto": "Usuario Test"
  }'

# Login
curl -X POST http://localhost:8000/auth/api/auth/login `
  -H "Content-Type: application/json" `
  -d '{
    "username": "admin",
    "password": "password123"
  }'
```

### 2. Pedido Service (a través de Kong)
```powershell
# Listar pedidos
curl http://localhost:8000/pedidos/api/pedidos
```

### 3. Fleet Service (a través de Kong)
```powershell
# Listar repartidores
curl http://localhost:8000/fleet/api/repartidores

# Listar vehículos
curl http://localhost:8000/fleet/api/vehiculos
```

### 4. Billing Service (a través de Kong)
```powershell
# Listar facturas
curl http://localhost:8000/billing/api/facturas
```

## Configuración Avanzada

### Rate Limiting (Límite de peticiones)
```powershell
# Limitar a 100 peticiones por minuto en auth-service
curl -X POST http://localhost:8001/services/auth-service/plugins `
  -H "Content-Type: application/json" `
  -d '{
    "name": "rate-limiting",
    "config": {
      "minute": 100,
      "policy": "local"
    }
  }'
```

### CORS (Cross-Origin Resource Sharing)
```powershell
# Habilitar CORS en todos los servicios
curl -X POST http://localhost:8001/plugins `
  -H "Content-Type: application/json" `
  -d '{
    "name": "cors",
    "config": {
      "origins": ["*"],
      "methods": ["GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"],
      "headers": ["Accept", "Authorization", "Content-Type"],
      "exposed_headers": ["X-Auth-Token"],
      "credentials": true,
      "max_age": 3600
    }
  }'
```

### Request Logging
```powershell
# Habilitar logging HTTP
curl -X POST http://localhost:8001/plugins `
  -H "Content-Type: application/json" `
  -d '{
    "name": "http-log",
    "config": {
      "http_endpoint": "http://localhost:9000/logs"
    }
  }'
```

## Comandos Útiles

### Ver configuración actual
```powershell
# Listar servicios
curl http://localhost:8001/services

# Listar rutas
curl http://localhost:8001/routes

# Listar plugins
curl http://localhost:8001/plugins
```

### Eliminar configuración
```powershell
# Eliminar un servicio (reemplaza {id} con el ID del servicio)
curl -X DELETE http://localhost:8001/services/{id}

# Eliminar una ruta
curl -X DELETE http://localhost:8001/routes/{id}
```

### Logs
```powershell
# Ver logs de Kong
docker logs -f kong-gateway

# Ver logs de un servicio específico
docker logs -f auth-service
docker logs -f pedido-service
docker logs -f fleet-service
docker logs -f billing-service
```

## Detener y Limpiar

```powershell
# Detener todos los contenedores
docker-compose down

# Detener y eliminar volúmenes (limpieza completa)
docker-compose down -v
```

## Puertos Utilizados

| Servicio | Puerto Host | Puerto Container |
|----------|-------------|------------------|
| **Kong Proxy** | 8000 | 8000 |
| **Kong Admin API** | 8001 | 8001 |
| Auth Service | 8082 | 8082 |
| Pedido Service | 8083 | 8083 |
| Fleet Service | 8084 | 8084 |
| Billing Service | 8085 | 8085 |
| PostgreSQL Auth | 5434 | 5432 |
| PostgreSQL Pedidos | 5435 | 5432 |
| PostgreSQL Fleet | 5436 | 5432 |
| PostgreSQL Billing | 5437 | 5432 |
| PostgreSQL Kong | 5438 | 5432 |

## Diagrama de Flujo

```
1. Cliente hace petición → http://localhost:8000/auth/api/auth/login
2. Kong recibe en puerto 8000
3. Kong busca ruta que coincida con /auth
4. Kong encuentra auth-service
5. Kong reenvía a http://auth-service:8082/api/auth/login (strip_path=true)
6. Auth Service procesa y responde
7. Kong retorna respuesta al cliente
```

---
**Nota**: Asegúrate de que todos los servicios estén en estado "healthy" antes de configurar Kong.
