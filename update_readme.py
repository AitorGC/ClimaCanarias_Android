content = """# 🌊 ClimaCanarias

### Desarrollado por Aitor Santana

[![Kotlin](https://img.shields.io/badge/Kotlin-1.9.x-blue.svg?style=flat-square&logo=kotlin)](https://kotlinlang.org)
[![Jetpack Compose](https://img.shields.io/badge/Compose-M3-green.svg?style=flat-square&logo=android)](https://developer.android.com/jetpack/compose)
[![Room](https://img.shields.io/badge/Room-Database-orange.svg?style=flat-square)](https://developer.android.com/training/data-storage/room)
[![Platform](https://img.shields.io/badge/Platform-Android-brightgreen.svg?style=flat-square&logo=android)](https://developer.android.com)

**ClimaCanarias** es una aplicación móvil para consultar la meteorología y el estado del mar en las Islas Canarias.

---

## 📌 El Proyecto

Las Islas Canarias cuentan con una gran variedad de microclimas. ClimaCanarias ofrece previsiones meteorológicas locales, acceso a información de playas, alertas en tiempo real y gráficas de mareas. La aplicación permite consultar el tiempo en diferentes municipios y costas para facilitar la planificación de actividades.

---

## 🔥 Características

* 🏝️ **Explorador de Playas**: Selección por Provincia, Isla, Municipio y Playa.
* 📦 **Datos Locales**: Base de datos para almacenar información de playas y lugares favoritos.
* 📈 **Mareas y Oleaje**: Visualización de los ciclos de pleamar, bajamar y altura de las olas.
* 🚩 **Estado en Tiempo Real**: Información sobre banderas, índice UV y condiciones marinas.
* 🌬️ **Viento**: Información sobre la orientación y velocidad del viento.
* 🌡️ **Temperatura del Agua**: Datos de temperatura procedentes de sensores costeros.
* ⛑️ **Socorrismo**: Horarios y fechas del servicio de vigilancia en playas.
* ⚠️ **Avisos de Seguridad**: Indicación clara cuando una playa no cuenta con servicio de socorrista.
* 🌤️ **Datos AEMET**: Consulta de estaciones meteorológicas y avisos de alerta vigentes en Canarias.
* ☁️ **Sincronización**: Posibilidad de guardar favoritos en la nube vinculando una cuenta de Google.
* 🌓 **Modo Oscuro**: Soporte para temas claros y oscuros según la configuración del sistema.

---

## 🛠️ Stack Tecnológico

* **Arquitectura**: MVVM (Model-View-ViewModel).
* **Lenguaje**: Kotlin (Coroutines + Flow).
* **UI**: Jetpack Compose (Material Design 3).
* **APIs**: APIs del Gobierno de Canarias y AEMET OpenData.
* **Base de Datos**: Room Database.
* **Red**: Retrofit2 + Moshi.
"""

with open("/README.md", "w") as f:
    f.write(content)

print("README updated")
