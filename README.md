# 🌊 ClimaCanarias 🇮🇨 — Meteorología de Precisión, Calidad del Aire y Estado del Mar (v2.4.1)

### Desarrollado con 💛 por AItor Santana

[![Version](https://img.shields.io/badge/Version-2.4.1-brightgreen.svg?style=flat-square)](app/build.gradle.kts)
[![Kotlin](https://img.shields.io/badge/Kotlin-1.9.x-blue.svg?style=flat-square&logo=kotlin)](https://kotlinlang.org)
[![Jetpack Compose](https://img.shields.io/badge/Compose-M3-green.svg?style=flat-square&logo=android)](https://developer.android.com/jetpack/compose)
[![Room](https://img.shields.io/badge/Room-Database-orange.svg?style=flat-square)](https://developer.android.com/training/data-storage/room)
[![Platform](https://img.shields.io/badge/Platform-Android-brightgreen.svg?style=flat-square&logo=android)](https://developer.android.com)

**ClimaCanarias** es la aplicación móvil de referencia para consultar la meteorología precisa, calidad del aire y el estado del mar detallado de todo el archipiélago canario, proporcionando mediciones críticas y en tiempo real para residentes, deportistas náuticos, surfistas, pescadores, bañistas y turistas.

---

## 📌 El Proyecto

El relieve volcánico y la orografía singular de las Islas Canarias generan decenas de microclimas únicos donde las condiciones meteorológicas varían radicalmente en apenas pocos kilómetros de distancia. **ClimaCanarias** nace con la misión de optimizar la previsión climatológica local mediante la gestión avanzada de microclimas específicos, la detección temprana de calima y polvo en suspensión en tiempo real, el acceso a catálogos completos de playas integradas con bases de datos indexadas y herramientas de alerta junto a gráficas fluidas de mareas y oleaje.

La app permite predecir con exactitud las condiciones en cualquier municipio o costa canaria, mitigando sorpresas meteorológicas y garantizando la máxima seguridad en zonas marítimas y de montaña.

---

## 🔥 Novedades de la Versión 2.4.1

* ⚓ **Resolución en Comunicación de Mareas (IHM)**: Corrección y vinculación de la política de seguridad de red en el manifiesto (`networkSecurityConfig`), permitiendo la recepción sin bloqueos de las tablas de mareas y cotas astronómicas del Instituto Hidrográfico de la Marina.
* 🇮🇨 **Identidad Canaria Reforzada**: Inclusión de la bandera oficial de Canarias (`🇮🇨`) en el encabezado principal de la aplicación.
* ⚡ **Optimización de Red y Conectividad**: Rendimiento mejorado en la sincronización y consultas concurrentes a los servicios de datos meteorológicos y marítimos.
* 🛡️ **Seguridad y Privacidad Estricta**: Gestión centralizada y aislada de credenciales en tiempo de compilación mediante `BuildConfig`, garantizando que ninguna clave de API se exponga públicamente.
* 📊 **Estabilidad en Gráficas y Métricas**: Mayor precisión en el renderizado de curvas de mareas, pronósticos horarios y análisis de concentraciones de calima (PM2.5 / PM10).
* 🔄 **Sincronización Silenciosa y Caché Inteligente**: Mejoras en el motor de persistencia local en Room DB con almacenamiento en caché optimizado para accesos instantáneos y reducción del consumo de datos móviles.

---

## 🌟 Características Destacadas

* 📍 **Persistencia de Selección y Geolocalización Bajo Demanda**: Guarda automáticamente la última ciudad o municipio consultado para restaurarlo al iniciar la aplicación. La geolocalización por GPS sólo se activa a petición explícita del usuario mediante el botón de ubicación.
* 🏝️ **Explorador Integral de Playas y Ciudades**: Sistema de selección jerárquica con catálogo exhaustivo de municipios canarios y playas integradas por islas y provincias.
* 🌬️ **Calidad del Aire y Detección de Calima**: Monitorización de partículas PM2.5, PM10, Ozono e índices AQI (Europeo y US AQI) con algoritmos de detección visual de calima y alertas por concentración.
* 🚨 **Alertas Oficiales de AEMET**: Sistema integrado de avisos meteorológicos (niveles amarillo, naranja y rojo) en tiempo real para todas las islas y comarcas con avisos prioritarios.
* 📈 **Tendencias Climáticas e Histórico**: Pronósticos detallados hora a hora (24 horas) y a 7 días vista con gráficas de temperatura, probabilidad de precipitación, humedad, índice UV y viento.
* 🌊 **Gráficas Oceanográficas de Mareas y Marítimo**: Integración de tablas astronómicas de mareas y modelos de oleaje en tiempo real (pleamar, bajamar, altura de ola significativa y periodo).
* 🌡️ **Estaciones de Observación en Vivo**: Lecturas en tiempo real de las estaciones meteorológicas oficiales de AEMET distribuidas por el archipiélago con buscador interactivo y mapa de situación.
* 🚩 **Estado de Banderas y Servicios de Socorrismo**: Información actualizada sobre el color de la bandera para el baño y disponibilidad de personal de salvamento en playas.
* 📦 **Persistencia Local y Sincronización en la Nube**: Base de datos Room (SQLite) precargada con caché de respuestas para un funcionamiento ultra fluido. Opción de sincronización en la nube privada mediante Google Drive AppData.
* 🌓 **Diseño Adaptativo con Material Design 3**: Soporte completo para modo oscuro y claro con conmutación automática día/noche, transiciones animadas y diseño responsivo para smartphones y tablets.

---

## 📡 Documentación de APIs e Integraciones

ClimaCanarias utiliza una arquitectura de datos meteorológicos y geográficos multicapa, combinando fuentes abiertas internacionales con organismos oficiales estatales y autonómicos:

### 1. ☀️ Open-Meteo Weather Forecast API
* **Dominio / Endpoint Base**: `https://api.open-meteo.com/v1/forecast`
* **Protocolo**: HTTPS / REST JSON
* **Parámetros Principales**:
  * `hourly`: `temperature_2m`, `relative_humidity_2m`, `precipitation_probability`, `precipitation`, `weather_code`, `wind_speed_10m`, `wind_direction_10m`, `uv_index`.
  * `daily`: `weather_code`, `temperature_2m_max`, `temperature_2m_min`, `sunrise`, `sunset`, `uv_index_max`, `precipitation_sum`, `precipitation_probability_max`, `wind_speed_10m_max`.
* **Propósito**: Cálculo y generación de pronósticos meteorológicos horarios (24h) y semanales (7 días) para cualquier municipio o coordenada geográfica del archipiélago.

### 2. 💨 Open-Meteo Air Quality API
* **Dominio / Endpoint Base**: `https://air-quality-api.open-meteo.com/v1/air-quality`
* **Protocolo**: HTTPS / REST JSON
* **Parámetros Principales**:
  * `hourly`: `pm10`, `pm2_5`, `carbon_monoxide`, `nitrogen_dioxide`, `sulphur_dioxide`, `ozone`, `dust`, `european_aqi`, `us_aqi`.
* **Propósito**: Suministro del Índice de Calidad del Aire (ICA) oficial europeo y alimentación del algoritmo de detección de intrusión de polvo sahariano (calima) y concentración de aerosoles.

### 3. 🌊 Open-Meteo Marine Weather API
* **Dominio / Endpoint Base**: `https://marine-api.open-meteo.com/v1/marine`
* **Protocolo**: HTTPS / REST JSON
* **Parámetros Principales**:
  * `hourly`: `wave_height`, `wave_direction`, `wave_period`, `wind_wave_height`, `wind_wave_direction`, `wind_wave_period`.
* **Propósito**: Proporcionar información en tiempo real de condiciones marítimas, altura de ola significativa, dirección y periodo de oleaje en las costas y playas canarias.

### 4. 🏛️ AEMET OpenData API (Agencia Estatal de Meteorología)
* **Dominio / Endpoint Base**: `https://opendata.aemet.es/opendata/api/`
* **Protocolo**: HTTPS / REST JSON (Autenticación mediante cabecera o parámetro seguro)
* **Servicios Integrados**:
  * **Observación en Tiempo Real**: `/observacion/convencional/todas` y estaciones específicas canarias (temperatura, racha máxima, velocidad media de viento, precipitación acumulada, humedad relativa y presión atmosférica).
  * **Avisos Meteorológicos Oficiales (CAP / JSON)**: `/avisos_cap/ultimo` para la monitorización de avisos vigentes por fenómenos meteorológicos adversos en las 8 islas canarias.
  * **Inventario de Estaciones**: `/valores/climatologicos/inventarioestaciones/todasestaciones` para el mapeo y geolocalización de estaciones oficiales.
* **Seguridad de Credenciales**: Las claves de acceso a AEMET OpenData se gestionan de forma aislada a través del entorno de compilación seguro (`BuildConfig`) y variables de entorno protegidas, sin quedar nunca expuestas en el código fuente ni en la interfaz pública.

### 5. ⚓ IHM — Instituto Hidrográfico de la Marina
* **Dominio / Endpoint Base**: `http://ideihm.covam.es/api-ihm/getmarea`
* **Protocolo**: HTTP / REST JSON
* **Propósito**: Tablas astronómicas oficiales de mareas para puertos y costas de Canarias, permitiendo calcular con precisión las horas y cotas de altura para pleamares y bajamares diarias.

### 6. 🏖️ Infoplayas (Gobierno de Canarias)
* **Dominio / Endpoint Base**: `https://www3.gobiernodecanarias.org/aplicaciones/infoplayas/`
* **Protocolo**: HTTPS / REST JSON
* **Propósito**: Catálogo autonómico de playas y zonas de baño de Canarias, incluyendo color de bandera de seguridad en tiempo real, horarios y dotación de socorristas, tipo de arena, accesibilidad PMR y servicios disponibles.

### 7. 🔍 Open-Meteo Geocoding API
* **Dominio / Endpoint Base**: `https://geocoding-api.open-meteo.com/v1/search`
* **Protocolo**: HTTPS / REST JSON
* **Propósito**: Búsqueda interactiva y resolución de coordenadas geográficas para municipios, pueblos y puntos de interés del archipiélago canario.

### 8. ☁️ Google Identity & Google Drive AppData API
* **Alcance OAuth**: `https://www.googleapis.com/auth/drive.appdata`
* **Protocolo**: HTTPS / REST JSON
* **Propósito**: Almacenamiento y sincronización bidireccional privada de ubicaciones favoritas y preferencias de usuario en la carpeta oculta `appDataFolder` de Google Drive personal.

---

## 🛠️ Stack Tecnológico

* **Lenguaje**: Kotlin Moderno con Coroutines y Kotlin Flow.
* **UI Framework**: Jetpack Compose con Material Design 3 (M3).
* **Arquitectura**: Clean Architecture + MVVM (Model-View-ViewModel) + Unidirectional Data Flow (UDF).
* **Base de Datos Local**: Room Database (SQLite Engine) con DAOs asíncronos y precarga indexada.
* **Persistencia Ligera**: `SharedPreferences` para ajustes del usuario, temas y última selección.
* **Networking**: OkHttp 4 + Retrofit 2 + Moshi con convertidor Kotlin JSON.
* **Procesamiento Asíncrono**: AndroidX `WorkManager` para sincronizaciones periódicas en background.
* **Testing**: Robolectric y Roborazzi para pruebas unitarias de integración en JVM local.

---

## 📄 Licencia

Este proyecto está bajo licencia MIT. Desarrollado con 💛 por **AItor Santana**.
