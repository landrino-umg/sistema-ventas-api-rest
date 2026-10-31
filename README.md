# Sistema de Ventas - Servicio API REST
**Universidad Mariano Gálvez de Guatemala**  
**Facultad de Ingeniería en Sistemas - Programación II**

Este repositorio contiene el servicio backend **API REST** encargado de recibir, validar y persistir las Órdenes de Venta emitidas desde la aplicación móvil Android y la aplicación de escritorio, conectándose a la base de datos MySQL relacional del Sistema de Ventas.

---

## 📌 Características Principales
- **Arquitectura RESTful:** Endpoints JSON con códigos de respuesta HTTP estándar (`200 OK`, `201 Created`, `400 Bad Request`, `404 Not Found`, `500 Internal Error`).
- **Soporte Completo CORS:** Headers para permitir consumo desde Android, navegadores web, emuladores y túneles ngrok.
- **Transaccionalidad ACID:** Registro seguro de la orden en `pedido`, sus ítems en `detalle_pedido` y descuento automático de inventario en `producto`.
- **Compatibilidad Multiplataforma:** Ejecución nativa con Maven y Java 17+.

---

## 🚀 Requisitos Previos
- **Java Development Kit (JDK):** Versión 17 o superior.
- **Apache Maven:** Versión 3.8+.
- **Acceso a MySQL:** Base de datos `sistema_ventas`.
- *(Opcional)* **ngrok:** Para exponer el servicio a Internet temporalmente.

---

## 🛠️ Ejecución del Proyecto

### 1. Clonar el repositorio
```bash
git clone <URL_DEL_REPOSITORIO_API>
cd sistema-ventas-api-rest
```

### 2. Compilar y Ejecutar con Maven
```bash
mvn clean compile exec:java
```

Por defecto el servidor se levantará en:
```
http://localhost:8080
```

*(Opcional: cambiar el puerto ejecutando `mvn exec:java -Dexec.args="9090"` o definiendo la variable de entorno `PORT=9090`).*

---

## 🌐 Exposición a Internet con ngrok (Demostración de Examen)

Para permitir que un teléfono físico o emulador fuera de la red local consuma la API:

1. Inicie el servidor API REST (`mvn exec:java`).
2. En una nueva terminal, ejecute:
   ```bash
   ngrok http 8080
   ```
3. Copie la **URL pública generada** (por ejemplo: `https://xxxx-xx-xx.ngrok-free.app`).
4. Ingrese esa URL en la configuración de la aplicación Android.

---

## 📋 Catálogo de Endpoints REST

| Método | Endpoint | Descripción | Código Éxito |
| :--- | :--- | :--- | :---: |
| `GET` | `/api/status` | Comprobación de salud y conexión con MySQL | `200 OK` |
| `GET` | `/api/productos` | Catálogo de productos con precios y existencias | `200 OK` |
| `GET` | `/api/pedidos` | Listado general de órdenes de venta con detalles | `200 OK` |
| `GET` | `/api/pedidos/{id}` | Obtener una orden específica por su ID | `200 OK` |
| `POST` | `/api/pedidos` | **Crear nueva Orden de Venta** | `201 Created` |
| `PUT` | `/api/pedidos/{id}` | **Editar/actualizar una Orden de Venta existente** | `200 OK` |
| `DELETE`| `/api/pedidos/{id}` | Eliminar una orden y reintegrar existencias | `200 OK` |

---

## 📝 Ejemplo de Petición JSON (`POST /api/pedidos`)

```json
{
  "nombreCliente": "Juan Pérez",
  "direccionEnvio": "7ma Avenida 12-34 Zona 1, Ciudad de Guatemala",
  "telefono": "5544-3322",
  "estado": "PENDIENTE",
  "items": [
    {
      "productoId": 1,
      "cantidad": 2
    },
    {
      "productoId": 3,
      "cantidad": 1
    }
  ]
}
```

### Respuesta Exitosa (`HTTP 201 Created`):
```json
{
  "success": true,
  "message": "Orden de Venta creada exitosamente en la base de datos",
  "data": {
    "id": 45,
    "clienteId": 12,
    "clienteNombre": "Juan Pérez",
    "direccionTexto": "7ma Avenida 12-34 Zona 1, Ciudad de Guatemala",
    "telefonoContacto": "5544-3322",
    "estado": "PENDIENTE",
    "total": 45.50,
    "detalles": [
      {
        "id": 89,
        "pedidoId": 45,
        "productoId": 1,
        "productoNombre": "Coca Cola 2.5L",
        "cantidad": 2,
        "precio": 18.50,
        "subtotal": 37.00
      }
    ]
  }
}
```
