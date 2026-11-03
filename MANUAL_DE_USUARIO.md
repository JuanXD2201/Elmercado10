# Manual de Usuario y Arquitectura de Datos - El Mercado 10

Este documento es la guía oficial de uso y administración técnica para la plataforma **El Mercado 10**. Está dividido en roles de usuario y una sección técnica detallada sobre la gestión de las bases de datos.

---

## 📚 ÍNDICE

1. [Manual del Comprador](#1-manual-del-comprador)
2. [Manual del Vendedor](#2-manual-del-vendedor)
3. [Manual del Administrador](#3-manual-del-administrador)
4. [Arquitectura y Edición de Bases de Datos (Técnico)](#4-arquitectura-y-edición-de-bases-de-datos-técnico)

---

## 1. MANUAL DEL COMPRADOR

El rol de comprador está diseñado para buscar, guardar y adquirir productos de forma rápida y segura.

### 1.1 Registro e Inicio de Sesión
* **Registro:** Al abrir la aplicación, selecciona "Crear Cuenta". Ingresa tus datos personales (Nombre, Correo, Teléfono, Contraseña) y asegúrate de seleccionar el rol **"Comprador"**.
* **Inicio de Sesión:** Si ya tienes cuenta, ingresa con tu correo electrónico y contraseña.

### 1.2 Exploración y Búsqueda
* **Inicio (Home):** Al entrar, verás los productos destacados y las últimas publicaciones ordenadas cronológicamente.
* **Catálogo y Filtros:** Usa la barra de búsqueda para encontrar productos por nombre o descripción. Puedes utilizar los filtros por categoría para refinar tu búsqueda.
* **Detalles del Producto:** Toca cualquier producto para ver su imagen ampliada, precio, descripción detallada, stock disponible, vendedor y las calificaciones de otros usuarios.

### 1.3 Carrito de Compras
* **Agregar:** En la vista de detalle de un producto, selecciona la cantidad que deseas y presiona "Agregar al carrito". La app verificará en tiempo real si hay stock suficiente.
* **Gestión del Carrito:** Accede al ícono del carrito en la barra de navegación. Aquí puedes modificar las cantidades (usando los botones `+` y `-`) o eliminar productos deslizando o reduciendo la cantidad a cero. Verás un subtotal calculado automáticamente.

### 1.4 Proceso de Compra (Checkout)
* En la pantalla del carrito, presiona "Realizar Pedido".
* **Importante:** La aplicación realizará una comprobación de inventario en milisegundos. Si otro usuario compró el último artículo antes que tú, la app te avisará. Si hay stock, la compra se confirmará y tu carrito se vaciará.

### 1.5 Mis Pedidos y Calificaciones
* **Historial:** Ve a la pestaña "Mis Pedidos" para revisar el estado de tus compras (Pendiente, Enviado, Entregado, Cancelado).
* **Calificar:** Una vez recibas el producto, puedes calificarlo (de 1 a 5 estrellas). Tu calificación actualizará el promedio global del producto de forma automática.

### 1.6 Chat
* Si tienes dudas sobre un producto, en su pantalla de detalle presiona "Contactar Vendedor". Esto abrirá un chat privado en tiempo real donde podrás enviar mensajes directamente al vendedor.

---

## 2. MANUAL DEL VENDEDOR

El rol de vendedor incluye herramientas integradas de gestión de inventario, ventas y atención al cliente.

### 2.1 Registro y Perfil
* Durante el registro, debes seleccionar el rol **"Vendedor"**. 
* En la pestaña "Perfil", puedes subir una foto que represente a tu tienda y actualizar tu información de contacto. Esta foto será visible para los compradores.

### 2.2 Panel de Gestión de Productos
* Accede a tu Panel de Vendedor. Aquí verás una lista exclusiva de *solo los productos que tú has publicado*.
* **Crear Producto:** Presiona el botón flotante (+). Deberás:
  1. Tomar una foto o subirla desde tu galería.
  2. Asignar un Nombre, Categoría y Descripción.
  3. Establecer el Precio y el Stock (Cantidad disponible).
  4. Guardar. El producto será visible inmediatamente en el catálogo global.

### 2.3 Edición y Eliminación
* **Editar:** Toca un producto de tu lista para modificar su precio, actualizar el stock (si recibiste nueva mercancía) o cambiar la foto.
* **Eliminar:** Puedes eliminar un producto. Desaparecerá del catálogo, pero los pedidos históricos asociados a él se mantendrán intactos para propósitos contables.

### 2.4 Gestión de Pedidos
* En la pestaña de Pedidos, verás una lista de todas las compras que incluyen tus productos.
* **Cambio de Estado:** Es tu responsabilidad cambiar el estado del pedido conforme avanza el proceso logístico:
  * *Pendiente* -> *Enviado* -> *Entregado* o *Cancelado*.
  * El comprador verá estos cambios en tiempo real.

### 2.5 Atención al Cliente (Chats)
* En la pestaña de "Mensajes", recibirás las consultas de los compradores. Debes responder a través del chat en vivo para facilitar tus ventas.

---

## 3. MANUAL DEL ADMINISTRADOR

*Nota: La administración de nivel superior se realiza principalmente a través de la **Consola de Firebase (Firebase Console)**.*

### 3.1 Firebase Console (Panel Web)
El Administrador debe tener acceso al proyecto `el-mercado10` en [console.firebase.google.com](https://console.firebase.google.com/).

### 3.2 Gestión de Usuarios (Authentication)
* Accede a la sección **Authentication** > **Users**.
* Desde aquí puedes:
  * Restablecer contraseñas de usuarios que hayan perdido el acceso.
  * Inhabilitar (Suspender) cuentas por comportamiento indebido.
  * Eliminar cuentas permanentemente.

### 3.3 Moderación de Contenido (Firestore)
* Accede a **Firestore Database**.
* Si un producto viola los términos de servicio, búscalo en la colección `productos` y bórralo manualmente o modifica su contenido.
* Puedes alterar manualmente el stock de un vendedor, corregir precios mal formateados o cancelar pedidos (cambiando el campo `status` en la colección `pedidos`).

### 3.4 Gestión de Almacenamiento (Storage)
* En **Storage**, se guardan todas las imágenes. Puedes borrar imágenes inapropiadas o liberar espacio eliminando imágenes de productos que ya no existen.

---

## 4. ARQUITECTURA Y EDICIÓN DE BASES DE DATOS (TÉCNICO)

Esta sección explica cómo interactúa la aplicación con la base de datos y cómo fluye la información ("cómo funciona la edición").

La aplicación no utiliza SQL tradicional, sino **Cloud Firestore** (una base de datos NoSQL basada en documentos) y **Firebase Storage** (para archivos).

### 4.1 Estructura de Colecciones en Firestore

Toda la base de datos está estructurada en jerarquías de Colecciones y Documentos:

1. **`usuarios / {uid}`**: Contiene nombre, correo, rol (comprador/vendedor), fecha de registro y URL de foto de perfil.
2. **`productos / {productoId}`**: Contiene detalles del producto, precio, stock, URL de imagen, ID del vendedor, y métricas (promedio de calificación).
   * **Subcolección `calificaciones / {userId}`**: Evita que un usuario califique dos veces.
3. **`carritos / {userId} / items / {productoId}`**: Funciona como un espacio aislado por usuario. Nadie puede ver el carrito de otro.
4. **`pedidos / {pedidoId}`**: Registros inmutables de compras finalizadas.
5. **`chats / {chatId}`**: Documentos de conversaciones.
   * **Subcolección `mensajes / {mensajeId}`**: Historial de mensajes individuales ordenados por Timestamp.

### 4.2 Flujo de Datos (Cómo funciona la "Edición")

Cuando un usuario edita algo en la aplicación (ej. actualiza el stock de un producto o edita su perfil), ocurre el siguiente flujo técnico:

1. **Capa de Interfaz (UI - Jetpack Compose):** El usuario presiona el botón "Guardar". La interfaz captura el nuevo valor y dispara un evento.
2. **Capa Lógica (ViewModel):** El ViewModel recibe el evento, muestra una animación de "Carga" (Loading) y envía los datos al Repository.
3. **Capa de Datos (Repository):** 
   * Si hay una imagen nueva, el `FirebaseStorageRepository` primero sube la imagen en segundo plano y obtiene una URL segura.
   * Luego, el `FirebaseProductRepository` crea un mapa de datos (ej. `mapOf("precio" to 500, "stock" to 10)`).
   * Se ejecuta una llamada `documentRef.update(datos)` hacia Firestore.
4. **Respuesta en Tiempo Real:** 
   * Firestore actualiza el documento en la nube.
   * Todos los dispositivos que estén viendo ese producto tienen un **SnapshotListener** (un observador en vivo).
   * Firestore notifica automáticamente a la app que los datos cambiaron. La UI se redibuja en milisegundos mostrando el nuevo precio, sin necesidad de que el usuario recargue la página.

### 4.3 Transacciones Atómicas (Seguridad en la Edición)

La base de datos utiliza "Transacciones Atómicas" para procesos críticos, asegurando que los datos no se corrompan si dos personas editan al mismo tiempo.

* **Ejemplo del Checkout (Crear Pedido):**
  Si dos compradores intentan comprar el último "Reloj" al mismo tiempo, Firebase abre una transacción. 
  1. Bloquea el documento del producto.
  2. Lee el stock (Stock = 1).
  3. El Comprador A procesa su carrito, el stock baja a 0 y se crea el Pedido A.
  4. La transacción finaliza.
  5. Cuando la operación del Comprador B se intenta ejecutar, Firebase lee que el Stock ahora es 0, y arroja un error ("Stock insuficiente"), evitando inventarios negativos.

* **Ejemplo de Calificaciones:**
  Al calificar, una transacción lee el "promedio actual" y la "cantidad de votos", calcula matemáticamente el nuevo promedio, y actualiza ambos campos de una sola vez. Esto evita desfases.

### 4.4 Reglas de Seguridad de Firestore (Security Rules)

Las ediciones de la base de datos no son libres; están protegidas en el servidor mediante `firestore.rules`:
* Nadie puede editar un producto salvo que su `auth.uid` sea igual al `vendedorId` del producto.
* Nadie puede ver ni editar un carrito ajeno.
* Los pedidos solo son visibles para el comprador que lo creó y los vendedores dueños de los productos dentro de ese pedido.

### 4.5 Persistencia Offline
Si un usuario pierde la conexión a internet y edita un producto o envía un mensaje, Firestore guarda la edición en una **caché local cifrada**. La interfaz mostrará el cambio instantáneamente. En cuanto el celular recupere la conexión de red, Firestore sincronizará automáticamente los cambios con la nube.
