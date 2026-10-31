# Sistema de Ventas - API REST

Proyecto del curso de Programación II - Universidad Mariano Gálvez de Guatemala.

Servicio backend desarrollado en Java para conectar la base de datos MySQL con la aplicación móvil Android y permitir la gestión de órdenes de compra.

## Tecnologías utilizadas
* Java 17
* Java HttpServer (`com.sun.net.httpserver`)
* MySQL Connector/J
* Gson (Google)
* Maven

## Endpoints disponibles

* `GET /api/status` - Verifica el estado del servidor y la conexión a la base de datos.
* `GET /api/productos` - Lista los productos disponibles con precio y stock.
* `GET /api/pedidos` - Lista las órdenes de compra registradas.
* `GET /api/pedidos/{id}` - Consulta el detalle de una orden específica.
* `POST /api/pedidos` - Registra una nueva orden de compra.
* `PUT /api/pedidos/{id}` - Modifica el estado de una orden.
* `DELETE /api/pedidos/{id}` - Cancela o elimina una orden de compra.

## Cómo ejecutar el servidor

### Opción 1: Con el archivo ejecutable
Hacer doble clic en `iniciar_servidor.bat` en la raíz del proyecto.

### Opción 2: Con Maven por consola
```bash
mvn compile exec:java
```

El servidor iniciará en el puerto 8080 (`http://localhost:8080/api`).

Para exponerlo hacia un dispositivo móvil fuera de la red local, se puede usar ngrok:
```bash
ngrok http 8080
```
