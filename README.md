# Desarrollo de cliente responsivo para dispositivos móviles para el servicio web RESTful

# Instalación y ejecución
Para generar el archivo `apk` en Android Studio, ir al menú de opciones y elegir
- Build -> Build App Bundle(s) / APK(s) -> Build APK(s)

El archivo apk se encontrará en el directorio del proyecto en la ruta: `app/build/outputs/apk/debug`

Para instalarla en el dispositivo, simplemente hay que descargar el archivo en el dispositivo android y ejecutar el apk (aceptar riesgos de seguridad y otras preguntas).
# Dependencias
- `Retrofit`
- `Okhttp`
- `GSON`
- `Osmdroid`
- `Androidx`
# Estructura y organización del código
Se ha estructurado el proyecto de la siguiente forma:
```
C:.
│   AndroidManifest.xml
│
├───java
│   └───com
│       └───example
│           └───practica3
│               │   AdminActivity.kt
│               │   CartActivity.kt
│               │   CheckoutActivity.kt
│               │   EditProductActivity.kt
│               │   LoginActivity.kt
│               │   MainActivity.kt
│               │   MapActivity.kt
│               │
│               ├───adapters
│               │       AdminProductAdapter.kt
│               │       CartAdapter.kt
│               │       CheckoutAdapter.kt
│               │       ProductAdapter.kt
│               │
│               ├───api
│               │       ApiClient.kt
│               │       ApiService.kt
│               │
│               ├───interfaces
│               │       CleareableCookieJar.kt
│               │
│               ├───models
│               │       CartDto.kt
│               │       CartItemDto.kt
│               │       ProductModel.kt
│               │
│               └───ui
│                   └───theme
│                           Color.kt
│                           Theme.kt
│                           Type.kt
│
└───res
    ├───drawable
    │       baseline_arrow_white_24.xml
    │       baseline_monigote_new_24.xml
    │       ic_launcher_background.xml
    │       ic_launcher_foreground.xml
    │       ic_store.xml
    │       monigote_64.xml
    │       stickman.xml
    │
    ├───layout
    │       activity_administration.xml
    │       activity_cart.xml
    │       activity_checkout.xml
    │       activity_edit_product.xml
    │       activity_login.xml
    │       activity_main.xml
    │       activity_map.xml
    │       admin_product_item.xml
    │       cart_item.xml
    │       checkout_item.xml
    │       product_item.xml
    │
    ├───mipmap-anydpi-v26
    │       ic_launcher.xml
    │       ic_launcher_round.xml
    │
    ├───mipmap-hdpi
    │       ic_launcher.webp
    │       ic_launcher_round.webp
    │
    ├───mipmap-mdpi
    │       ic_launcher.webp
    │       ic_launcher_round.webp
    │
    ├───mipmap-xhdpi
    │       ic_launcher.webp
    │       ic_launcher_round.webp
    │
    ├───mipmap-xxhdpi
    │       ic_launcher.webp
    │       ic_launcher_round.webp
    │
    ├───mipmap-xxxhdpi
    │       ic_launcher.webp
    │       ic_launcher_round.webp
    │
    ├───values
    │       colors.xml
    │       strings.xml
    │       themes.xml
    │
    └───xml
            backup_rules.xml
            data_extraction_rules.xml
            file_paths.xml
            network_security_config.xml
```
Los directorios y archivos de interés son
- El archivo `AndroidManifest.xml` que contiene cierta configuración de la app y, lo más importante, las distintas **activities** que se usarán.
- En el directorio principal se encuentran las `activities`:
	- AdminActivity.kt
	- CartActivity.kt
	- CheckoutActivity.kt
	- EditProductActivity.kt
	- LoginActivity.kt
	- MainActivity.kt
	- MapActivity.kt
- El directorio `adapters` contiene el código de los adapter utilizados:
	- AdminProductAdapter.kt
	- CartAdapter.kt
	- CheckoutAdapter.kt
	- ProductAdapter.kt
- El directorio `api` contiene el código para hacer llamadas a la API del backend:
	- ApiClient
	- ApiService
- En el directorio `models` se encuentran los modelos utilizados:
	- CartDto.kt
	- CartItemDto.kt
	- ProductModel.kt
- En el directorio `layouts` se encuentran los diseños de las vistas necesarias:
	- activity_administration.xml
	- activity_cart.xml
	- activity_checkout.xml
	- activity_edit_product.xml
	- activity_login.xml
	- activity_main.xml
	- activity_map.xml
	- admin_product_item.xml
	- cart_item.xml
	- checkout_item.xml
	- product_item.xml
- En directorio `drawable` se encuentran iconos de la app. Son utilizados en el mapa y en las toolbars:
	- baseline_arrow_white_24.xml
	- baseline_monigote_new_24.xml
	- ic_launcher_background.xml
	- ic_launcher_foreground.xml
	- ic_store.xml
	- monigote_64.xml
	- stickman.xml
- En `values` se encuentran definidos los colores que se utilizan en la app:
	- colors.xml
	- strings.xml
	- themes.xml
- Por último, destacar el archivo `file_paths.xml` en el directorio `xml` pues indica donde se descargará la factura.
# API
- **GET** `/api/products`: endpoint que permite obtener la lista de productos disponibles.
- **POST** `/api/add`: endpoint que permite añadir un producto al catálogo de productos.
- **POST** `/api/delete/{id}`: endpoint que permite eliminar un producto del catálogo de productos.
- **PUT** `/api/edit/{id}`: endpoint que permite editar los datos de un producto del catálogo de productos.
- **POST** `/api/cart/add/{id}`: endpoint que permite añadir un producto de la lista de productos al carrito.
-  **GET** `/api/cart`: endpoint para obtener los productos que existen actualmente en el carrito.
-  **POST** `/api/cart/remove/{id}`: endpoint para eliminar un producto del carrito.
-  **POST** `/api/cart/invoice`: endpoint para realizar el pago de los productos elegidos.
-  **POST** `/login`: endpoint para realizar la autenticación.