# 🌊 ClimaCanarias — Meteorología de Precisión y Estado del Mar

### Desarrollado por Aitor Santana

[![Kotlin](https://img.shields.io/badge/Kotlin-1.9.x-blue.svg?style=flat-square&logo=kotlin)](https://kotlinlang.org)
[![Jetpack Compose](https://img.shields.io/badge/Compose-M3-green.svg?style=flat-square&logo=android)](https://developer.android.com/jetpack/compose)
[![Room](https://img.shields.io/badge/Room-Database-orange.svg?style=flat-square)](https://developer.android.com/training/data-storage/room)
[![Platform](https://img.shields.io/badge/Platform-Android-brightgreen.svg?style=flat-square&logo=android)](https://developer.android.com)

**ClimaCanarias** es la aplicación móvil definitiva para consultar la meteorología precisa y el estado del mar detallado de todo el archipiélago canario, proporcionando mediciones críticas para residentes, surfistas, pescadores y turistas.

![Capturas de ClimaCanarias](https://raw.githubusercontent.com/username/project/main/screenshots/banner.png)

---

## 📌 El Proyecto

El relieve volcánico de las Islas Canarias genera decenas de microclimas únicos donde las condiciones climáticas varían radicalmente a pocos kilómetros de distancia. **ClimaCanarias** nace con la misión de optimizar la previsión climatológica local mediante la gestión avanzada de microclimas específicos, el acceso a catálogos completos de playas integradas mediante bases de datos indexadas y herramientas de alerta junto a gráficas fluidas de mareas en tiempo real. La app ayuda a predecir con exactitud si el tiempo en una costa o municipio específico será favorable, mitigando sorpresas meteorológicas y garantizando seguridad en zonas marítimas.

---

## 🔥 Características Destacadas

* 🏝️ **Explorador de Playas y Ciudades**: Sistema de selección jerárquica y localizaciones preferidas que actúa como un *fonil* de datos altamente optimizado.
* 🚨 **Alertas Oficiales de AEMET**: Sistema integrado de avisos meteorológicos en tiempo real de la Agencia Estatal de Meteorología (AEMET) para tu zona actual y mediante notificaciones push en segundo plano para tus islas favoritas.
* 📈 **Tendencias Climáticas Inmediatas**: Gráfica de tendencias de temperatura, humedad y probabilidad de precipitación para las próximas 12 horas.
* 📦 **Persistencia Local Integrada**: Base de datos Room completamente indexada y poblada en el primer arranque, garantizando acceso offline instantáneo y guardado fluido de favoritos.
* 🌊 **Gráficas Oceanográficas de Mareas**: Representación visual e interactiva de los ciclos de pleamar y bajamar, así como la altura del oleaje para una interpretación técnica inmediata.
* 🚩 **Estado de Banderas y Datos en Tiempo Real**: Conexión directa a las APIs oficiales del Gobierno de Canarias para la lectura de banderas, aviso de peligros, índice UV (`uvdb`) dinámico y condiciones marinas al instante.
* 🌡️ **Estaciones de Observación en Vivo**: Acceso a datos crudos de las estaciones de observación meteorológica de AEMET para revisar la temperatura y el estado medido real en diferentes puntos de la isla.
* ⛑️ **Servicio de Socorrismo Activo**: Integración exhaustiva con la API de playas para buscar la presencia de personal de vigilancia y socorrismo, mostrando la empresa proveedora y el periodo de servicios.
* 🌓 **Modo Oscuro Adaptativo**: Interfaz visual optimizada bajo Material Design 3 con esquemas cromáticos dinámicos amigables con ambientes de mucha o poca luz solar.
* 🔄 **Sincronización Eficiente**: Llamadas concurrentes no bloqueantes optimizadas para obtener las últimas lecturas meteorológicas minimizando el uso de datos móviles usando corrutinas de Kotlin.

---

## 🛠️ Stack Tecnológico

* **Arquitectura**: MVVM (Model-View-ViewModel) + Unidirectional Data Flow (UDF).
* **Lenguaje**: Kotlin Moderno (Coroutines + Flow).
* **UI Framework**: Jetpack Compose con Material Design 3.
* **Integración de Servicios**: REST APIs del Gobierno de Canarias (Infoplayas / Socorrismo), Open-Meteo, AEMET (OpenData y RSS Atom).
* **Base de Datos Local**: Space-efficient Room Database (SQLite Engine).
* **Networking & Parsing**: Retrofit2 + Moshi con tipado adaptativo.
* **Testing & Snapshot Validation**: Robolectric & Roborazzi.
* **Background Work**: WorkManager para consultas periódicas de alertas.
