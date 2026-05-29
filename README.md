# Clima Canarias 🌤️🏝️

Una aplicación meteorológica moderna para dispositivos Android, desarrollada íntegramente en **Kotlin** utilizando **Jetpack Compose**. Diseñada para ofrecer pronósticos precisos, calidad del aire e información climática relevante para las Islas Canarias, con un enfoque en la experiencia de usuario y el diseño adaptativo.

---

## ✨ Funcionalidades Principales

*   **Pronóstico en Tiempo Real:** Datos precisos del tiempo actual y previsiones (diarias y horarias) mediante la API de *Open-Meteo*.
*   **Calidad del Aire (AQI):** Información detallada sobre elementos como el Polvo Sahariano y PM10, particularmente útiles en las Islas Canarias (calima).
*   **Gestión de Ciudades Favoritas:** Guarda tus ubicaciones preferidas para acceder a su información rápidamente. Los datos se mantienen persistidos localmente incluso sin conexión.
*   **Sincronización en la Nube:** (Modo Simulado con Google) Sincronización y restauración segura de preferencias y ciudades favoritas utilizando los datos vinculados a la cuenta.
*   **Interfaz Moderna y Adaptativa:** Construida con `Jetpack Compose` siguiendo las guías de *Material Design 3*. Soporte integrado para **Modo Claro** y **Modo Oscuro** (Night Mode).
*   **Alertas Meteorológicas:** Sistema de avisos emergentes (estilo AEMET) para eventos climáticos relevantes.
*   **Información Marítima y Playas:** Consulta del estado meteorológico de las playas de Canarias mediante un potente sistema de filtrado jerárquico: **Provincia > Isla > Municipio > Playa & Zona de Baño Marítima (ZBM)**, con parseo automático de coordenadas sexagesimales (DMS) y carga dinámica de playas desde su origen CSV.

---

## 🛠️ Tecnologías y Arquitectura

El proyecto sigue una arquitectura **MVVM (Model-View-ViewModel)** robusta, promoviendo la separación de conceptos, testabilidad y un flujo de datos unidireccional (UDF).

*   **Lenguaje:** [Kotlin](https://kotlinlang.org/)
*   **Interfaz de Usuario:** [Jetpack Compose](https://developer.android.com/jetpack/compose) - Toolkit moderno y declarativo para UI nativa.
*   **Programación Asíncrona:** Corrutinas (`Coroutines`) y `StateFlow` para gestionar la reactividad y los flujos de datos asíncronos.
*   **Base de Datos Local:** [Room Database](https://developer.android.com/training/data-storage/room) - Capa de abstracción sobre SQLite para persistencia local.
*   **Llamadas de Red:** [Retrofit2](https://square.github.io/retrofit/) & [OkHttp3](https://square.github.io/okhttp/) (Network y Logging Interceptor).
*   **Serialización JSON:** [Moshi](https://github.com/square/moshi/) con soporte de generación KSP.
*   **Pruebas (Testing):** Robolectric y Roborazzi para pruebas unitarias de JVM y capturas de pantallas (Screenshot testing).

---

## 🚀 Instalación y Despliegue

1.  Asegúrate de contar con la versión más reciente de **Android Studio** u otra herramienta compatible que soporte la ejecución y compilación de proyectos con Gradle.
2.  El archivo de configuración principal se encuentra en `app/build.gradle.kts`.
3.  Usa los comandos habituales de Gradle o la interfaz gráfica del IDE para sincronizar dependencias y ejecutar:
    ```bash
    ./gradlew :app:assembleDebug
    ```

---

> **Desarrollada por Aitor Santana**
