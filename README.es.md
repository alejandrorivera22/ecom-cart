# ecom-cart

Este READMEse encuentra disponible [Inges EN](./README.md)

ecom-cart es una API backend diseñada para un sistema de e-commerce, 
creada con Spring Boot. Incluye todas las funcionalidades básicas que 
esperas de una tienda en línea. Ofrece un flujo de compra sencillo que 
abarca la gestión de productos, autenticación mediante JWT, un carrito de 
compras, así como la gestion de usuarios y órdenes. Además, cuenta
con integración de caché en Redis, pruebas unitarias y documentación a
través de Swagger.

---

## Tecnologías utilizadas

| Herramienta           | Uso principal                              |
|-----------------------|---------------------------------------------|
| Java 17               | Lenguaje principal                          |
| Spring Boot 3.4.5     | Framework backend                           |
| Spring Web            | Exposición de API REST                     |
| Spring Data JPA       | Persistencia y consultas con Hibernate      |
| Spring Security + JWT | Autenticación y autorización                |
| Redis + Redisson      | Cache de productos                         |
| MySQL 8               | Base de datos relacional                    |
| H2                    | Base de datos en memoria para testing       |
| Swagger (OpenAPI)     | Documentación interactiva de la API         |
| Docker Compose        | Contenedores para MySQL y Redis             |
| JUnit, Mockito        | Pruebas unitarias y de integración          |

---
## Funcionalidades

- `Autenticación con JWT` (registro e inicio de sesión)
- `Gestión de productos` (crear, paginar, buscar, actualizar, deshabilitar)
- `Ordenes` (crear, paginar, cancelar, cambiar estado)
- `Gestión de usuarios` —  (crear, paginar, actualizar, roles)
- `Detalles de ordenes` — (ver detalle de ordenes por una orden o usuario)
- `Cache con Redis` para mejorar rendimiento
-  `Seguimiento de Stock`: Mantén un control preciso en tiempo real.
- `Carrito`:
   - Agregar/eiminar products
   - Evitar añadir productos deshabilitados
---
## Validacónes implementadas
#### Ejemplos
- `Contro de Stock` Al crear o actuaizar una orden a API
valida si hay suficiente stock disponibe para e producto en específico
- `Actuaización automatica de inventario` Cuando se reaiza un pedido, e stick del
producto se actualiza automáticamente
- `Vaidación de negocios` Se impide cancelar ordenes que ya fueron completados, 
cambiar el estado de ordenes canceladas. Se manejan cuatro estados diferentes
para cada etapa de la orden o pedido 
(Pendiente, Enviado, Competado, Cancelado).
- `Productos deshabilitados` Solo usuarios con rol adecuado(ADMIN o SELLER)
pueden modificar productos, y solo CUSTOMER puede comprar o agregar al 
carrito.
-  `Gestión por roles` los productos deshabiitados no pueden añadirce al carrito
   ni a las ordenes.

---
## Estructura del proyecto

- `api/` — Controladores y rutas de la API
- `config/` — Configuraciones generales (Swagger, seguridad, etc)
- `domain/` — Entidades y repositorios para la base de datos
- `infrastructure/` — interfaces y lógica de negocio
- `resources/` — Archivos de configuración
- `util/` — CLase de utileria roles, excepciones personalizadas, etc.
- `test/` — Pruebas unitarias

### Arquitectura
El proyecto está diseñado por una arquitectura por capas, 
inspirada en los principios de Clean Architecture. Aquí, 
las responsabilidades se dividen de manera clara entre los controladores
(API), servicios (lógica de negocio)
, dominio (entidades y repositorios) y configuración.
Esta estructura no solo mejora la mantenibilidad y escalabilidad,
sino que también hace que las pruebas unitarias sean mucho más sencillas de
implementar.

---
##  Instalación local

### 1. Requisitos previos

- Java 17 instalado
- Docker y Docker Compose
- Maven

### 2. Clonar el repositorio
git clone https://github.com/alejandrorivera22/ecom-cart.git
cd ecom-cart

### 3. Levantar MySQL y REDIS
docker-compose up -d

### 4. Compilar y correr la aplicación
- ./mvnw clean install
- ./mvnw spring-boot:run

### 5. Accede a la API en:
- http://localhost:8080/ecom-cart

### 6. Accede a documentación Swagger UI:
- http://localhost:8080/ecom-cart/swagger-ui/index.html

---
## Usuarios predefinidos para pruebas

Estos usuarios están precargados en la base de datos (`data.sql`)
y permiten simular autenticación y autorización 
según los distintos roles disponibles en el sistema.

| Rol      | Username    | Contraseña       |
|----------|-------------|------------------|
| Admin    | `admin`     | `adminpassword`  |
| Seller   | `seller `   | `sellerpassword` |
| Customer | `john_doe ` | `password123`    |

> Las contraseñas están encriptadas con BCrypt. 
> Se indican aquí solo para prueba en entorno local.

---

### Cómo probar autenticación JWT en Swagger

1. Accede a Swagger UI (`http://localhost:8080/ecom-cart/swagger-ui/index.html`) en tu navegador.
2. Ve a POST /auth/login y autentícate con alguno de los usuarios mencionados.
3. Copia el token JWT que se encuentra en la propiedad token de la respuesta.
4. Haz clic en el botón **"Authorize"** (el ícono de candado).
5. Pega el token.

##  Autor

**Alejandro Rivera**  
- [![LinkedIn](https://img.shields.io/badge/LinkedIn-Connect-blue?logo=linkedin)](https://www.linkedin.com/in/alejandro-rivera-verdayes-443895375/)
- [![GitHub](https://img.shields.io/badge/GitHub-000?style=for-the-badge&logo=github&logoColor=white)](https://github.com/alejandrorivera22)



