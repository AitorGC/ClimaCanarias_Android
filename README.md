# 🌊 ClimaCanarias — Meteorología de Precisión y Estado del Mar

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

* 🏝️ **Explorador de Playas**: Sistema de selección jerárquica (Provincia > Isla > Municipio > Playa) que actúa como un *fonil* de datos altamente optimizado para mitigar sobrecargas en memoria.
* 📦 **Persistencia Local Integrada**: Base de datos Room completamente indexada y poblada en el primer arranque desde un dataset CSV de playas de Canarias, garantizando acceso offline instantáneo y guardado fluido de favoritos.
* 📈 **Gráficas Oceanográficas de Mareas**: Representación visual e interactiva de los ciclos de pleamar y bajamar, así como la altura del oleaje para una interpretación técnica inmediata.
* 🚩 **Estado de Banderas y Datos en Tiempo Real**: Conexión directa a las APIs oficiales del Gobierno de Canarias para la lectura de banderas, aviso de peligros, índice UV (`uvdb`) dinámico y condiciones marinas al instante.
* 🌬️ **Orientación del Viento Intuitiva (Nuevo)**: Mapeo directo del parámetro `wind_orientation` (como NE, N, SW) de la API oficial para ofrecer una lectura de orientación de viento amigable y fácilmente interpretable.
* 🌡️ **Temperatura del Agua (Nuevo)**: Lectura directa y visualización clara de la temperatura del agua (`water_temp`) obtenida en tiempo real desde los sensores de boyas costeras.
* ⛑️ **Servicio de Socorrismo Activo (Nuevo)**: Integración exhaustiva con la API de playas (`/api/beach`) para buscar la presencia de personal de vigilancia y socorrismo (`lifeguard`). Muestra la empresa proveedora, fechas de vigencia formateadas de manera elegante, periodo de servicios y horario de presencia activa.
* ⚠️ **Alertas de Seguridad Gráciles**: Si la playa seleccionada no posee cobertura de salvamento en ese instante en la base de datos oficial, se avisa de forma muy visual y destacada con el estado **"SIN SOCORRISTA"**, fomentando la seguridad ciudadana.
* 🌓 **Modo Oscuro Adaptativo**: Interfaz visual optimizada bajo Material Design 3 con esquemas cromáticos dinámicos amigables con ambientes de mucha o poca luz solar.
* 🔄 **Sincronización Eficiente**: Llamadas concurrentes no bloqueantes optimizadas para obtener las últimas lecturas meteorológicas minimizando el uso de datos móviles usando corrutinas de Kotlin.

---

## 🛠️ Stack Tecnológico

* **Arquitectura**: MVVM (Model-View-ViewModel) + Unidirectional Data Flow (UDF).
* **Lenguaje**: Kotlin Moderno (Coroutines + Flow).
* **UI Framework**: Jetpack Compose con Material Design 3.
* **Integración de Servicios**: REST APIs del Gobierno de Canarias (Infoplayas / Socorrismo).
* **Base de Datos Local**: Space-efficient Room Database (SQLite Engine).
* **Networking & Parsing**: Retrofit2 + Moshi con tipado adaptativo.
* **Testing & Snapshot Validation**: Robolectric & Roborazzi.


