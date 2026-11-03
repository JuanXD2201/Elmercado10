# Elmercado10

Marketplace móvil Android para comprar y vender productos nuevos y usados. Los compradores exploran un catálogo, arman un carrito, confirman pedidos y conversan con el vendedor; los vendedores publican productos con fotografía y atienden sus pedidos. Los datos residen en Firebase (proyecto `el-mercado10`).

Proyecto de la asignatura Optativa 2, Tecnología en Desarrollo de Software, Universidad de San Buenaventura.

**Autores:** Juan Esteban Olaya Muñoz, Nicolas Quiroz Gomez y Juan Sebastian Garzon Suarez.  
**Docente:** Jairo Armando Salcedo.

## Funcionalidades

- Registro e inicio de sesión con dos roles: comprador y vendedor.
- Catálogo con categorías, filtros (estado, precio, calificación) y orden.
- Publicación, edición y baja de productos con cámara o galería.
- Carrito y confirmación de pedidos con control transaccional de inventario y precio.
- Ciclo de vida del pedido: pendiente, confirmado, enviado, entregado o cancelado.
- Chat comprador–vendedor en tiempo real y calificaciones de 1 a 5 estrellas.

## Tecnologías

Kotlin, Jetpack Compose (Material 3), Navigation Compose, ViewModel, Coil, CameraX, Firebase Authentication, Cloud Firestore y Cloud Storage. Gradle 9.3.1, Android Gradle Plugin 9.1.1, minSdk 24, compileSdk 36.

## Compilar y ejecutar

1. Abrir la carpeta en Android Studio y sincronizar Gradle.
2. Crear la clave de depuración que usa `app/build.gradle.kts` (no se versiona):

   ```
   keytool -genkeypair -v -keystore debug.keystore -alias androiddebugkey -storepass android -keypass android -keyalg RSA -keysize 2048 -validity 10000 -dname "CN=Android Debug,O=Android,C=US"
   ```

3. Compilar y probar:

   ```
   gradlew.bat assembleDebug
   gradlew.bat testDebugUnitTest
   ```

La aplicación no usa ninguna API de inteligencia artificial, por lo que no requiere `GEMINI_API_KEY`. En equipos con 4 GB de RAM, `gradle.properties` limita la memoria de Gradle a 1536 MB y dos procesos de trabajo.

## Arquitectura

Tres capas: presentación (pantallas Compose, ViewModels, navegación), dominio (modelos e interfaces de repositorio) y datos (repositorios sobre Firebase). No hay servidor propio, por lo que la seguridad recae en `firestore.rules` y `storage.rules`.

## Estado

- Compilación de depuración correcta y 10 de 10 pruebas unitarias superadas.
- Las reglas de Firestore del repositorio no se han desplegado ni probado con el emulador de Firebase.
- Las notificaciones funcionan solo con la aplicación activa (sin FCM).
- No hay capturas de ejecución: el emulador de Android no estaba disponible.

El detalle de requisitos, arquitectura, pruebas, hallazgos de auditoría y riesgos está en [`DOCUMENTACION_ELMERCADO10`](DOCUMENTACION_ELMERCADO10):

| Carpeta | Contenido |
|---|---|
| `01_REQUISITOS_SRS` | Especificación de requisitos (IEEE 29148) |
| `02_ARQUITECTURA_Y_DISENO` | Arquitectura de software |
| `04_PRUEBAS_Y_CALIDAD` | Plan e informe de pruebas |
| `05_MANUALES` | Manuales de usuario, instalación y técnico |
| `06_FIREBASE_Y_BASE_DE_DATOS` | Servicios Firebase, modelo de datos y reglas |
| `07_DIAGRAMAS` | Diagramas editables (.drawio) y exportados |
| `08_PRESENTACION` | Presentación, PDF y guion de exposición |
| `10_AUDITORIA_TECNICA` | Informe de auditoría |
| `11_MATRICES` | Matriz maestra (XLSX) |
| `12_INFORME_FINAL` | Informe final |

## Seguridad

`google-services.json` contiene identificadores públicos de Firebase, no credenciales privadas. La clave de API debe restringirse a la aplicación en Google Cloud antes de publicar el repositorio. No se versionan `local.properties`, `debug.keystore` ni archivos `.env`.
