# 🌊 ClimaCanarias — Meteorología de Precisión, Calidad del Aire y Estado del Mar (v2.2.8)

### Desarrollado por Aitor Santana

[![Version](https://img.shields.io/badge/Version-2.2.8-brightgreen.svg?style=flat-square)](app/build.gradle.kts)
[![Kotlin](https://img.shields.io/badge/Kotlin-1.9.x-blue.svg?style=flat-square&logo=kotlin)](https://kotlinlang.org)
[![Jetpack Compose](https://img.shields.io/badge/Compose-M3-green.svg?style=flat-square&logo=android)](https://developer.android.com/jetpack/compose)
[![Room](https://img.shields.io/badge/Room-Database-orange.svg?style=flat-square)](https://developer.android.com/training/data-storage/room)
[![Platform](https://img.shields.io/badge/Platform-Android-brightgreen.svg?style=flat-square&logo=android)](https://developer.android.com)

**ClimaCanarias** es la aplicación móvil definitiva para consultar la meteorología precisa, calidad del aire y el estado del mar detallado de todo el archipiélago canario, proporcionando mediciones críticas y en tiempo real para residentes, surfistas, pescadores y turistas.

![Capturas de ClimaCanarias](https://raw.githubusercontent.com/username/project/main/screenshots/banner.png)

---

## 📌 El Proyecto

El relieve volcánico de las Islas Canarias genera decenas de microclimas únicos donde las condiciones climáticas varían radicalmente a pocos kilómetros de distancia. **ClimaCanarias** nace con la misión de optimizar la previsión climatológica local mediante la gestión avanzada de microclimas específicos, la detección de calima en tiempo real, el acceso a catálogos completos de playas integradas mediante bases de datos indexadas y herramientas de alerta junto a gráficas fluidas de mareas en tiempo real. La app ayuda a predecir con exactitud las condiciones en cualquier municipio o costa canaria, mitigando sorpresas meteorológicas y garantizando seguridad en zonas marítimas.

---

## 🔥 Características Destacadas

* 📍 **Persistencia de Selección y Ubicación Bajo Demanda**: Guarda automáticamente la última ciudad o municipio consultado para restaurarlo al iniciar la aplicación. La geolocalización por GPS sólo se activa a petición del usuario mediante el botón dedicado.
* 🏝️ **Explorador de Playas y Ciudades**: Sistema de selección jerárquica con catálogo de municipios canarios y playas integradas.
* 🌬️ **Calidad del Aire y Detección de Calima**: Monitorización de partículas PM2.5, PM10, Ozono e índices AQI (Europeo y US AQI) con algoritmos de detección visual de calima y avisos por concentración.
* 🚨 **Alertas Oficiales de AEMET**: Sistema integrado de avisos meteorológicos (amarillo, naranja, rojo) en tiempo real para todas las islas Canarias y notificaciones mediante `WorkManager`.
* 📈 **Tendencias Climáticas e Histórico**: Pronósticos detallados hora a hora (24h) y a 7 días con gráficas de temperatura, probabilidad de precipitación, humedad y viento.
* 🌊 **Gráficas Oceanográficas de Mareas y Marítimo**: Integración con las predicciones del Instituto Hidrográfico de la Marina (IHM) y Open-Meteo Marine para mostrar pleamar/bajamar, altura del oleaje y temperatura del agua.
* 🌡️ **Estaciones de Observación en Vivo**: Lecturas en tiempo real de las estaciones meteorológicas de AEMET distribuidas por el archipiélago.
* 🚩 **Estado de Banderas y Servicios de Socorrismo**: Datos en tiempo real de banderas en playas y disponibilidad de personal de salvamento y socorrismo.
* 📦 **Persistencia Local y Sincronización**: Base de datos Room (SQLite) precargada con caché de respuestas para un funcionamiento fluido sin consumo excesivo de red. Opción de sincronización en la nube con Google Sign-In.
* 🌓 **Diseño Adaptativo con Material Design 3**: Soporte completo para modo oscuro y claro, transiciones animadas entre vistas y compatibilidad con diferentes tamaños de pantalla.

---

## 🛠️ Stack Tecnológico

* **Arquitectura**: MVVM (Model-View-ViewModel) + Unidirectional Data Flow (UDF).
* **Lenguaje**: Kotlin Moderno (Coroutines + Flow).
* **UI Framework**: Jetpack Compose con Material Design 3.
* **Integración de Servicios**: REST APIs de Open-Meteo (Weather, Air Quality, Marine), AEMET (OpenData y RSS), Instituto Hidrográfico de la Marina (IHM) e Infoplayas (Gobierno de Canarias).
* **Base de Datos Local**: Room Database (SQLite Engine) con consultas optimizadas.
* **Persistencia Ligera**: `SharedPreferences` para preferencias del usuario y última ciudad seleccionada.
* **Networking**: Retrofit2 + Ktor / Moshi con serialización orientada a Kotlin.
* **Background Work**: `WorkManager` para comprobaciones periódicas de avisos en segundo plano.
* **Testing**: Robolectric & Roborazzi para pruebas locales y de regresión visual.
