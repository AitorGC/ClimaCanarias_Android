# 🌊 ClimaCanarias 🇮🇨

[![Version](https://img.shields.io/badge/version-2.4.1-brightgreen.svg?style=for-the-badge)](app/build.gradle.kts)
[![Kotlin](https://img.shields.io/badge/Kotlin-1.9.24-7F52FF.svg?style=for-the-badge&logo=kotlin&logoColor=white)](https://kotlinlang.org)
[![Android SDK](https://img.shields.io/badge/Android-SDK%2024%2B%20%7C%20Target%2036-3DDC84.svg?style=for-the-badge&logo=android&logoColor=white)](https://developer.android.com)
[![Jetpack Compose](https://img.shields.io/badge/Compose-Material%203-4285F4.svg?style=for-the-badge&logo=jetpackcompose&logoColor=white)](https://developer.android.com/jetpack/compose)
[![License: CC BY-NC 4.0](https://img.shields.io/badge/License-CC%20BY--NC%204.0-lightgrey.svg?style=for-the-badge)](https://creativecommons.org/licenses/by-nc/4.0/)

> **Aplicación Android nativa para la monitorización meteorológica de alta precisión, calidad del aire, detección temprana de calima y estado oceanográfico del mar en las Islas Canarias.**

Desarrollada con 💛 por **AItor Santana**.

---

## 📖 Descripción

Las Islas Canarias presentan una geografía volcánica y abrupta que genera decenas de **microclimas singulares** en distancias de apenas unos pocos kilómetros. Una previsión meteorológica insular generalizada suele ser insuficiente para predecir las condiciones exactas en cumbres, medianías, valles o costas.

**ClimaCanarias** fue concebida y desarrollada para resolver este reto:
- **Motivación:** Brindar a residentes, deportistas náuticos, surfistas, pescadores, bañistas y visitantes una herramienta fiable, precisa y centralizada con datos locales en tiempo real.
- **Solución:** Integra múltiples capas de datos oceanográficos, atmosféricos y satelitales, combinando predicciones horarias (24h) y semanales (7 días), tablas astronómicas de mareas, estado del oleaje, alertas de fenómenos meteorológicos adversos y calidad del aire con algoritmos específicos para la detección de calima (polvo sahariano).
- **Enfoque Técnico:** Construida siguiendo los más altos estándares de desarrollo Android moderno: arquitectura Clean/MVVM, interfaz declarativa con Jetpack Compose Material 3, persistencia local reactiva con Room SQLite, y gestión estricta de seguridad en comunicaciones y credenciales.

---

## 📑 Tabla de Contenidos

- [Descripción](#-descripción)
- [Instalación y Configuración](#-instalación-y-configuración)
- [Guía de Uso](#-guía-de-uso)
- [Características Principales](#-características-principales)
- [Tecnologías Utilizadas](#-tecnologías-utilizadas)
- [Integración de Fuentes de Datos](#-integración-de-fuentes-de-datos)
- [Seguridad y Privacidad](#-seguridad-y-privacidad)
- [Pruebas (Tests)](#-pruebas-tests)
- [Cómo Contribuir](#-cómo-contribuir)
- [Créditos y Atribuciones](#-créditos-y-atribuciones)
- [Licencia](#-licencia)

---

## 🚀 Instalación y Configuración

### Prerrequisitos
- **Android Studio**: Ladybug (2024.2+) o superior recomendado.
- **JDK**: Java Development Kit 17 o 21.
- **Android SDK**: `minSdk: 24` (Android 7.0 Nougat) | `targetSdk: 36` (Android 16).
- **Gradle**: 8.x con soporte para Kotlin DSL (`build.gradle.kts`).

### Pasos para Compilar y Ejecutar

1. **Clonar el repositorio:**
   ```bash
   git clone https://github.com/tu-usuario/ClimaCanarias.git
   cd ClimaCanarias
   ```

2. **Configuración de Variables de Entorno (Opcional para servicios que requieran clave):**
   - El proyecto utiliza Gradle Secrets y `BuildConfig` para inyectar credenciales seguras durante la compilación.
   - Si dispones de credenciales de desarrollador para servicios oficiales opcionales, crea un archivo `.env` en la raíz del proyecto basado en `.env.example`:
     ```bash
     cp .env.example .env
     ```
   - Añade tus claves en `.env` (este archivo está excluido del control de versiones mediante `.gitignore` para máxima seguridad).

3. **Compilar el proyecto con Gradle:**
   ```bash
   # En Linux / macOS
   ./gradlew assembleDebug

   # En Windows
   gradlew.bat assembleDebug
   ```

4. **Instalar en dispositivo o emulador:**
   Ejecuta el proyecto directamente desde Android Studio o mediante:
   ```bash
   ./gradlew installDebug
   ```

---

## 📱 Guía de Uso

1. **Selección de Localidad y Microclimas:**
   - Despliega el selector jerárquico por **Isla > Municipio** para consultar el pronóstico exacto de cualquier localidad canaria.
   - Utiliza el botón de **Geolocalización GPS** para obtener instantáneamente las condiciones meteorológicas del punto exacto donde te encuentras.
   - La última localidad consultada se guarda automáticamente en memoria local para abrirse al reiniciar la app.

2. **Exploración por Pestañas:**
   - **🌤️ General / Previsión:** Temperatura actual, sensación térmica, humedad, viento, índice UV y gráficas de pronóstico 24 horas y 7 días.
   - **💨 Calidad del Aire:** Monitorización de partículas PM2.5, PM10, Ozono, índice ICA europeo y semáforo visual de alerta por calima.
   - **🌊 Marítimo y Mareas:** Curvas y tablas de mareas (pleamar / bajamar), altura significativa de olas, periodo de oleaje y dirección del viento marino.
   - **🏖️ Playas y Socorrismo:** Catálogo insular de playas con banderas de baño, horarios de vigilancia y servicios disponibles.
   - **📡 Estaciones en Vivo:** Lecturas de telemetría en tiempo real procedentes de estaciones meteorológicas oficiales canarias.
   - **🛰️ Satélite y Radar:** Visualizador de capas satelitales y reflectividad radar para el seguimiento de frentes y tormentas.

3. **Gestión de Favoritos y Copia en la Nube:**
   - Marca playas y municipios como favoritos con el icono de estrella para acceder a ellos rápidamente.
   - Puedes sincronizar tus preferencias y favoritos de forma privada a través de tu cuenta de Google.

---

## ✨ Características Principales

* 🇮🇨 **Especialización Insular:** Diseñada específicamente para los patrones de relieve, vientos alisios y microclimas de las Islas Canarias.
* 🌬️ **Detección Temprana de Calima:** Algoritmo dedicado para identificar intrusiones de polvo en suspensión y avisar de riesgos respiratorios.
* 🚨 **Sistema de Avisos y Alertas:** Notificaciones prioritarias de fenómenos meteorológicos adversos clasificados por nivel de riesgo (amarillo, naranja y rojo).
* ⚓ **Tablas de Mareas y Dinámica Costera:** Gráficas de pleamar/bajamar y predicción oceanográfica de oleaje para deportes acuáticos y seguridad náutica.
* 📦 **Modo Offline y Caché Reactiva:** Arquitectura con persistencia en base de datos local SQLite para carga instantánea y funcionamiento sin cobertura.
* 🌓 **Diseño Adaptativo Material 3:** Paleta de colores optimizada, compatibilidad con modo oscuro y claro con conmutación dinámica y tipografía legible.

---

## 🛠️ Tecnologías Utilizadas

| Componente | Tecnología / Librería | Propósito |
| :--- | :--- | :--- |
| **Lenguaje** | [Kotlin](https://kotlinlang.org/) (Coroutines + Flow) | Desarrollo reactivo, asíncrono y tipado seguro |
| **UI Framework** | [Jetpack Compose](https://developer.android.com/jetpack/compose) + M3 | Interfaces declarativas fluidas y modernas |
| **Arquitectura** | Clean Architecture + MVVM + UDF | Mantenibilidad, modularidad y separación de capas |
| **Persistencia Local** | [Room Database](https://developer.android.com/training/data-storage/room) (SQLite) | Caché offline indexada de playas, municipios y favoritos |
| **Red y Networking** | [OkHttp 4](https://square.github.io/okhttp/) + [Retrofit 2](https://square.github.io/retrofit/) | Cliente HTTP con interceptores y gestión de resiliencia |
| **Serialización** | [Moshi](https://github.com/square/moshi) + Kotlin Reflection | Parseo eficiente y seguro de estructuras JSON complejas |
| **Tareas en Segundo Plano** | [AndroidX WorkManager](https://developer.android.com/topic/libraries/architecture/workmanager) | Sincronización periódica de avisos meteorológicos |
| **Seguridad de Red** | Android Network Security Config | Políticas de cifrado HTTPS estricto y control de excepciones |
| **Testing** | [Robolectric](https://robolectric.org/) + [Roborazzi](https://github.com/takahirom/roborazzi) | Pruebas unitarias de integración y captura de UI en JVM |

---

## 🌐 Integración de Fuentes de Datos

La aplicación combina de forma sinérgica múltiples proveedores y organismos oficiales meteorológicos y oceanográficos:

1. **Modelos de Previsión Meteorológica:** Datos numéricos de alta resolución para previsiones horarias, viento, precipitación, radiación solar y variables atmosféricas.
2. **Modelos de Dispersión y Calidad del Aire:** Mediciones de partículas en suspensión (PM2.5, PM10), ozono (O₃), dióxido de nitrógeno (NO₂) y cálculo del índice ICA.
3. **Modelos Oceanográficos y Marítimos:** Altura de ola combinada, oleaje de fondo, periodo de pico y dirección del mar de viento.
4. **Organismos Estatales y Autonómicos Oficiales:** Observaciones en vivo de estaciones meteorológicas canarias, avisos de emergencias (CAP) e información de seguridad en playas y socorrismo.
5. **Servicios Hidrográficos:** Tablas astronómicas de cálculo y predicción de mareas para puertos e islas del archipiélago.
6. **Sincronización en la Nube Privada:** Almacenamiento cifrado en el espacio seguro `appData` de Google Drive.

---

## 🔒 Seguridad y Privacidad

- **Privacidad del Usuario:** La aplicación no recopila datos personales, no incluye rastreadores de publicidad de terceros y almacena la información de favoritos exclusivamente en la base de datos local del dispositivo o en el almacenamiento privado del usuario.

---

## 🧪 Pruebas (Tests)

Para ejecutar la suite de pruebas unitarias e integración en entorno local JVM:

```bash
# Ejecutar todas las pruebas unitarias con Robolectric
./gradlew :app:testDebugUnitTest

# Ejecutar un test específico
./gradlew :app:testDebugUnitTest --tests "com.example.ApiStatsTrackerTest"
```

---

## 🤝 Cómo Contribuir

¡Las contribuciones de la comunidad son bienvenidas! Si deseas colaborar:

1. Realiza un **Fork** del proyecto.
2. Crea una rama descriptiva para tu funcionalidad o corrección (`git checkout -b feature/NuevaFuncionalidad` o `fix/CorreccionError`).
3. Realiza tus cambios asegurando que el código compila y pasa los tests (`./gradlew testDebugUnitTest`).
4. Haz **Commit** de tus cambios siguiendo la convención de commits convencionales (`git commit -m 'feat: añadir nueva estación meteorológica'`).
5. Sube tu rama (`git push origin feature/NuevaFuncionalidad`).
6. Abre un **Pull Request** detallando las mejoras aportadas.

---

## 👥 Créditos y Atribuciones

- **Autor Principal y Desarrollador:** **AItor Santana** ([Aitor Santana](https://github.com/AitorGC))
- **Fuentes de Datos e Información Pública:**
  - Modelos y predicciones abiertas de Open-Meteo.
  - Datos de observación y avisos oficiales de la Agencia Estatal de Meteorología (AEMET).
  - Información de baño y seguridad del servicio Infoplayas del Gobierno de Canarias.
  - Tablas de marea astronómica del Instituto Hidrográfico de la Marina (IHM).
- **Agradecimientos:** A la comunidad de desarrolladores de Android y a los usuarios canarios que aportan feedback constante para mejorar la precisión de las predicciones en las islas.

---

## 📄 Licencia

Este proyecto está bajo la **Licencia Creative Commons Atribución-NoComercial 4.0 Internacional (CC BY-NC 4.0)**. 

Se permite compartir, copiar y redistribuir el material en cualquier medio o formato, así como adaptar, remezclar y transformar el documento, siempre que se reconozca adecuadamente la autoría de **AItor Santana**, se proporcione un enlace a la licencia y se indique si se han realizado cambios. Queda estrictamente prohibido el uso comercial de esta obra y de sus derivados.

Para más información, consulta los detalles en [Creative Commons BY-NC 4.0](https://creativecommons.org/licenses/by-nc/4.0/legalcode.es).
