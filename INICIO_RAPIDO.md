# 🚀 Inicio Rápido - LogiFlow con Kong Gateway

## Pasos para Pruebas Finales con API Gateway

### 1️⃣ Levantar la Plataforma Completa

```powershell
# Navegar al directorio del proyecto
cd C:\AppServ\Distribuidas\delivery

# Levantar todos los servicios (4 microservicios + 5 PostgreSQL + Kong Gateway)
docker-compose up -d

# Ver el progreso de los contenedores
docker-compose ps
```

**Esperado**: 10 contenedores corriendo:
- ✅ `auth-service` (8082)
- ✅ `pedido-service` (8083)
- ✅ `fleet-service` (8084)
- ✅ `billing-service` (8085)
- ✅ `logiflow_auth_db` (PostgreSQL 5434)
- ✅ `logiflow_pedidos_db` (PostgreSQL 5435)
- ✅ `logiflow_fleet_db` (PostgreSQL 5436)
- ✅ `logiflow_billing_db` (PostgreSQL 5437)
- ✅ `kong-database` (PostgreSQL 5438)
- ✅ `kong-gateway` (8000, 8001)

### 2️⃣ Verificar que Kong esté Listo

```powershell
# Esperar ~2-3 minutos y verificar salud de Kong
curl http://localhost:8001/status

# Deberías ver: {"database":{"reachable":true},...}
```

### 3️⃣ Configurar Rutas en Kong (AUTOMÁTICO)

```powershell
# Ejecutar script de configuración
./kong-config.ps1
```

Este script configura:
- **Auth Service**: `http://localhost:8000/auth/*`
- **Pedido Service**: `http://localhost:8000/pedidos/*`
- **Fleet Service**: `http://localhost:8000/fleet/*`
- **Billing Service**: `http://localhost:8000/billing/*`

### 4️⃣ Cargar Datos de Prueba

```powershell
# Auth Service - 7 usuarios
docker exec -i logiflow_auth_db psql -U postgres -d db_logiflow_auth < backend/auth-service/test-data.sql

# Pedido Service - 8 pedidos
docker exec -i logiflow_pedidos_db psql -U postgres -d db_logiflow_pedidos < backend/pedido-service/test-data.sql

# Fleet Service - 6 vehículos, 7 repartidores
docker exec -i logiflow_fleet_db psql -U postgres -d db_logiflow_fleet < backend/fleet-service/test-data.sql

# Billing Service - 8 facturas
docker exec -i logiflow_billing_db psql -U postgres -d db_logiflow_billing < backend/billing-service/test-data.sql
```

---

## 🧪 Pruebas con Kong Gateway (Puerto 8000)

### ✅ Prueba 1: Verificar Servicios

```powershell
# Auth Service
curl http://localhost:8000/auth/

# Pedido Service
curl http://localhost:8000/pedidos/

# Fleet Service
curl http://localhost:8000/fleet/

# Billing Service
curl http://localhost:8000/billing/
```

### ✅ Prueba 2: Login y Obtener Token

```powershell
# Login con usuario admin
curl -X POST http://localhost:8000/auth/api/auth/login `
  -H "Content-Type: application/json" `
  -d '{
    "username": "admin",
    "password": "password123"
  }'

# Guardar el accessToken de la respuesta
```

**Respuesta esperada**:
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiJ9...",
  "tokenType": "Bearer",
  "expiresIn": 86400000,
  "username": "admin",
  "email": "admin@logiflow.com",
  "roles": ["ADMINISTRADOR"]
}
```

### ✅ Prueba 3: Listar Pedidos (a través de Kong)

```powershell
curl http://localhost:8000/pedidos/api/pedidos
```

**Respuesta esperada**: Lista de 8 pedidos

### ✅ Prueba 4: Listar Repartidores

```powershell
curl http://localhost:8000/fleet/api/repartidores
```

**Respuesta esperada**: Lista de 7 repartidores

### ✅ Prueba 5: Listar Vehículos

```powershell
curl http://localhost:8000/fleet/api/vehiculos
```

**Respuesta esperada**: Lista de 6 vehículos

### ✅ Prueba 6: Listar Facturas

```powershell
curl http://localhost:8000/billing/api/facturas
```

**Respuesta esperada**: Lista de 8 facturas

### ✅ Prueba 7: Crear Pedido (POST)

```powershell
curl -X POST http://localhost:8000/pedidos/api/pedidos `
  -H "Content-Type: application/json" `
  -d '{
    "clienteId": "c1111111-1111-1111-1111-111111111111",
    "clienteNombre": "Juan Pérez",
    "clienteTelefono": "0991234567",
    "clienteEmail": "cliente1@logiflow.com",
    "direccionOrigen": "Av. Amazonas N24-03",
    "coordenadasOrigen": "-0.1807,-78.4678",
    "direccionDestino": "Av. 6 de Diciembre N36-15",
    "coordenadasDestino": "-0.1650,-78.4822",
    "tipoEntrega": "URBANA_RAPIDA",
    "pesoKg": 2.5,
    "descripcionPaquete": "Documentos urgentes",
    "prioridad": "URGENTE"
  }'
```

### ✅ Prueba 8: Crear Factura (POST)

```powershell
curl -X POST http://localhost:8000/billing/api/facturas `
  -H "Content-Type: application/json" `
  -d '{
    "pedidoId": "c1111111-1111-1111-1111-111111111111",
    "numeroPedido": "PED-20240115-100000-0001",
    "clienteId": "a1111111-1111-1111-1111-111111111111",
    "clienteNombre": "Juan Pérez",
    "tipoEntrega": "URBANA_RAPIDA",
    "distanciaKm": 5.0,
    "pesoKg": 2.5,
    "prioridad": "NORMAL",
    "descuento": 0.00,
    "fechaVencimiento": "2024-02-15",
    "observaciones": "Factura de prueba desde Kong"
  }'
```

---

## 🔍 Verificar Configuración de Kong

### Ver Servicios Registrados
```powershell
curl http://localhost:8001/services
```

### Ver Rutas Configuradas
```powershell
curl http://localhost:8001/routes
```

### Ver Plugins Activos
```powershell
curl http://localhost:8001/plugins
```

---

## 📊 Monitoreo en Tiempo Real

### Ver Logs de Todos los Servicios
```powershell
docker-compose logs -f
```

### Ver Logs de un Servicio Específico
```powershell
docker-compose logs -f auth-service
docker-compose logs -f pedido-service
docker-compose logs -f kong-gateway
```

### Ver Logs de Kong Gateway
```powershell
docker-compose logs -f kong
```

---

## 🛠️ Comandos Útiles

### Reiniciar un Servicio
```powershell
docker-compose restart auth-service
```

### Reconstruir un Servicio
```powershell
docker-compose up -d --build auth-service
```

### Detener Todo
```powershell
docker-compose down
```

### Detener y Eliminar Volúmenes (Limpieza Total)
```powershell
docker-compose down -v
```

### Verificar Estado de Contenedores
```powershell
docker-compose ps
```

---

## 🎯 Flujo de Trabajo Completo (Prueba End-to-End)

### 1. Login
```powershell
$response = curl -X POST http://localhost:8000/auth/api/auth/login `
  -H "Content-Type: application/json" `
  -d '{"username": "admin", "password": "password123"}' | ConvertFrom-Json

$token = $response.accessToken
```

### 2. Crear Cliente
```powershell
curl -X POST http://localhost:8000/auth/api/auth/register `
  -H "Content-Type: application/json" `
  -d '{
    "username": "clientePrueba",
    "email": "prueba@logiflow.com",
    "password": "password123",
    "nombreCompleto": "Cliente de Prueba"
  }'
```

### 3. Crear Pedido
```powershell
curl -X POST http://localhost:8000/pedidos/api/pedidos `
  -H "Content-Type: application/json" `
  -d '{
    "clienteId": "UUID_DEL_CLIENTE",
    "clienteNombre": "Cliente de Prueba",
    "direccionOrigen": "Calle A",
    "direccionDestino": "Calle B",
    "coordenadasOrigen": "-0.1807,-78.4678",
    "coordenadasDestino": "-0.1650,-78.4822",
    "tipoEntrega": "URBANA_RAPIDA",
    "pesoKg": 3.0,
    "prioridad": "NORMAL"
  }'
```

### 4. Asignar Repartidor
```powershell
curl -X PATCH http://localhost:8000/pedidos/api/pedidos/{PEDIDO_ID}/asignar-repartidor `
  -H "Content-Type: application/json" `
  -d '{
    "repartidorId": "UUID_DEL_REPARTIDOR"
  }'
```

### 5. Generar Factura
```powershell
curl -X POST http://localhost:8000/billing/api/facturas `
  -H "Content-Type: application/json" `
  -d '{
    "pedidoId": "UUID_DEL_PEDIDO",
    "clienteId": "UUID_DEL_CLIENTE",
    "tipoEntrega": "URBANA_RAPIDA",
    "distanciaKm": 5.0,
    "pesoKg": 3.0,
    "prioridad": "NORMAL"
  }'
```

### 6. Registrar Pago
```powershell
curl -X PATCH http://localhost:8000/billing/api/facturas/{FACTURA_ID}/pagar `
  -H "Content-Type: application/json" `
  -d '{
    "metodoPago": "Tarjeta de Crédito"
  }'
```

---

## 🌐 Acceso Directo vs Kong Gateway

### Acceso Directo (Sin Gateway)
- Auth: `http://localhost:8082/api/auth/login`
- Pedidos: `http://localhost:8083/api/pedidos`
- Fleet: `http://localhost:8084/api/repartidores`
- Billing: `http://localhost:8085/api/facturas`

### A través de Kong Gateway (Recomendado)
- Auth: `http://localhost:8000/auth/api/auth/login`
- Pedidos: `http://localhost:8000/pedidos/api/pedidos`
- Fleet: `http://localhost:8000/fleet/api/repartidores`
- Billing: `http://localhost:8000/billing/api/facturas`

**Ventajas de usar Kong**:
- ✅ Punto de entrada único (puerto 8000)
- ✅ Rate limiting
- ✅ CORS configurado
- ✅ Logging centralizado
- ✅ Load balancing
- ✅ Autenticación centralizada (futuro)

---

## 📝 Notas Importantes

1. **Primera vez**: Los contenedores tardan ~2-3 minutos en estar completamente operativos
2. **Kong Gateway**: Debe estar en estado "healthy" antes de configurar rutas
3. **Bases de datos**: Se crean automáticamente con `ddl-auto: update`
4. **Datos de prueba**: Cárgalos después de que los servicios estén corriendo
5. **Puertos**: Asegúrate de que los puertos 8000, 8001, 8082-8085, 5434-5438 estén libres

---

## 🆘 Solución de Problemas

### Kong no inicia
```powershell
# Ver logs de Kong
docker-compose logs kong

# Verificar que kong-database esté healthy
docker-compose ps kong-database

# Reiniciar Kong
docker-compose restart kong
```

### Servicio no responde
```powershell
# Ver logs del servicio
docker-compose logs auth-service

# Verificar que la BD esté healthy
docker-compose ps logiflow_auth_db

# Reiniciar el servicio
docker-compose restart auth-service
```

### Error de conexión a BD
```powershell
# Verificar que las BDs estén corriendo
docker-compose ps | findstr postgres

# Verificar logs de la BD
docker-compose logs logiflow_auth_db
```

---

## ✅ Checklist de Verificación

- [ ] Docker Desktop está corriendo
- [ ] Puertos 8000, 8001, 8082-8085, 5434-5438 están libres
- [ ] `docker-compose up -d` ejecutado exitosamente
- [ ] 10 contenedores en estado "running"
- [ ] Kong está "healthy": `curl http://localhost:8001/status`
- [ ] Script `kong-config.ps1` ejecutado exitosamente
- [ ] Datos de prueba cargados en las 4 BDs
- [ ] Endpoints responden a través de Kong (puerto 8000)

---

**¡Listo para las pruebas finales! 🎉**

Para cualquier consulta, revisa:
- [README.md](README.md) - Documentación general
- [KONG_SETUP.md](KONG_SETUP.md) - Guía detallada de Kong
- [backend/*/POSTMAN_EXAMPLES.txt](backend/) - Ejemplos de API
