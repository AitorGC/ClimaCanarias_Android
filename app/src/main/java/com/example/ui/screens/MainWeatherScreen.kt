package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.*
import com.example.ui.animations.WeatherAnimations
import com.example.ui.components.ClimaIcon
import com.example.ui.components.AirQualityIndicator
import com.example.ui.components.BeachSelectionDropdown
import com.example.ui.components.FavoriteCitiesManager
import com.example.ui.components.MarineWeatherScreenMode
import com.example.ui.components.TrendChart
import com.example.ui.components.SunAndUvBlock
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import com.example.viewmodel.WeatherUiState
import com.example.viewmodel.WeatherViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainWeatherScreen(
    viewModel: WeatherViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.weatherUiState.collectAsStateWithLifecycle()
    val favorites by viewModel.favorites.collectAsStateWithLifecycle()
    val selectedCity by viewModel.selectedCity.collectAsStateWithLifecycle()
    val isCelsius by viewModel.isCelsius.collectAsStateWithLifecycle()
    val isDarkTheme by viewModel.isDarkTheme.collectAsStateWithLifecycle()
    val aemetAlert by viewModel.aemetAlert.collectAsStateWithLifecycle()

    val userProfile by viewModel.cloudSync.userProfile.collectAsStateWithLifecycle()
    val isSyncing by viewModel.cloudSync.isSyncing.collectAsStateWithLifecycle()
    val lastSyncTime by viewModel.cloudSync.lastSyncTime.collectAsStateWithLifecycle()

    val beaches by viewModel.beaches.collectAsStateWithLifecycle()
    val selectedBeach by viewModel.selectedBeach.collectAsStateWithLifecycle()
    val marineUiState by viewModel.marineUiState.collectAsStateWithLifecycle()

    var selectedTabIndex by remember { mutableStateOf(0) }
    val tabs = listOf("CLIMA", "PLAYA")

    val scrollState = rememberScrollState()

    // Base layout with custom theme colours
    val primaryCanaryYellow = Color(0xFFFFD600)
    
    // Light Background is off-white (cream), dark is clean charcoal
    val appBackgroundColor = if (isDarkTheme) Color(0xFF141318) else Color.White
    val cardBackgroundColor = if (isDarkTheme) Color(0xFF1E1C24) else Color(0xFFF4F7FA)
    val onSurfaceColor = if (isDarkTheme) Color(0xFFE6E1E5) else Color(0xFF141318)

    // Pulse animation for alerting alerts
    val infiniteTransition = rememberInfiniteTransition(label = "AEMETPulsing")
    val alertPulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "AlertPulseAlpha"
    )

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            // Elegant header "ClimaCanarias por Aitor Santana"
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "ClimaCanarias",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                        Text(
                            text = "por Aitor Santana",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isDarkTheme) primaryCanaryYellow else Color(0xFFFFD600)
                        )
                    }
                },
                actions = {
                    // Celsius vs Fahrenheit scale toggle
                    Row(
                        modifier = Modifier
                            .testTag("unit_selector")
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (isDarkTheme) Color(0xFF1E1C24) else Color.White.copy(alpha = 0.15f)
                            )
                            .clickable { viewModel.toggleTemperatureUnit() }
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "°C",
                            fontSize = 13.sp,
                            fontWeight = if (isCelsius) FontWeight.Bold else FontWeight.Normal,
                            color = if (isCelsius) {
                                if (isDarkTheme) primaryCanaryYellow else Color(0xFFFFD600)
                            } else {
                                Color.White.copy(alpha = 0.65f)
                            }
                        )
                        Text(
                            text = " | ",
                            fontSize = 13.sp,
                            color = Color.White.copy(alpha = 0.4f)
                        )
                        Text(
                            text = "°F",
                            fontSize = 13.sp,
                            fontWeight = if (!isCelsius) FontWeight.Bold else FontWeight.Normal,
                            color = if (!isCelsius) {
                                if (isDarkTheme) primaryCanaryYellow else Color(0xFFFFD600)
                            } else {
                                Color.White.copy(alpha = 0.65f)
                            }
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Dark/Light Mode Switcher button
                    IconButton(
                        onClick = { viewModel.toggleTheme() },
                        modifier = Modifier.testTag("theme_switcher")
                    ) {
                        ClimaIcon(
                            name = if (isDarkTheme) "light_mode" else "dark_mode",
                            tint = if (isDarkTheme) primaryCanaryYellow else Color(0xFFFFD600)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = if (isDarkTheme) Color(0xFF141318) else Color(0xFF004993),
                    titleContentColor = Color.White
                )
            )
        },
        containerColor = appBackgroundColor
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Silent Google login and Cloud Sinking Indicator panel
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (isDarkTheme) Color(0xFF1E262C) else Color(0xFFEFF6FF)
                ),
                shape = RoundedCornerShape(24.dp),
                border = androidx.compose.foundation.BorderStroke(
                    width = 1.dp,
                    color = if (isDarkTheme) Color(0xFF2D3135) else Color(0xFFDBEAFE)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = if (userProfile != null) primaryCanaryYellow else Color.LightGray,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = null,
                                    tint = Color.Black,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        Column {
                            if (userProfile != null) {
                                Text(
                                    text = "Sincronizado: ${userProfile!!.displayName}",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Cuenta de Google: ${userProfile!!.email}",
                                    fontSize = 11.sp,
                                    color = Color.Gray
                                )
                            } else {
                                Text(
                                    text = "Offline local persistente",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Sincroniza en la nube activando Google",
                                    fontSize = 11.sp,
                                    color = Color.Gray
                                )
                            }
                        }
                    }

                    // Sinking Action Button
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (userProfile == null) {
                            Button(
                                onClick = { viewModel.cloudSync.signInSilently() },
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                modifier = Modifier.height(30.dp)
                            ) {
                                Text("Conectar", fontSize = 11.sp)
                            }
                        } else {
                            IconButton(
                                onClick = { viewModel.triggerSync() },
                                modifier = Modifier.size(32.dp)
                            ) {
                                if (isSyncing) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(18.dp),
                                        strokeWidth = 2.dp,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.Refresh,
                                        contentDescription = "Sincronizar",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                            
                            IconButton(
                                onClick = { viewModel.cloudSync.logout() },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Cerrar sesión",
                                    tint = Color.Red,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }

            // 2. TabRow under the location selection
            // First we put the Location Selection Box
            Box(modifier = Modifier.fillMaxWidth()) {
                if (selectedTabIndex == 0) {
                    FavoriteCitiesManager(
                        favorites = favorites,
                        selectedCity = selectedCity,
                        onCitySelected = { viewModel.selectCity(it) },
                        onAddFavorite = { name, lat, lng -> viewModel.addCustomFavorite(name, lat, lng) },
                        onDeleteFavorite = { viewModel.removeFavorite(it) }
                    )
                } else {
                    BeachSelectionDropdown(
                        beaches = beaches,
                        selectedBeach = selectedBeach,
                        onBeachSelected = { viewModel.selectBeachId(it) }
                    )
                }
            }

            // TabRow placed immediately under location selection
            TabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = Color.Transparent,
                divider = {},
                indicator = { tabPositions ->
                    TabRowDefaults.Indicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                        color = if (isDarkTheme) primaryCanaryYellow else Color(0xFF004993),
                        height = 3.dp
                    )
                }
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = { 
                            Text(
                                title, 
                                fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Medium,
                                color = if (selectedTabIndex == index) {
                                    if (isDarkTheme) primaryCanaryYellow else Color(0xFF004993)
                                } else {
                                    Color.Gray
                                }
                            ) 
                        }
                    )
                }
            }

            if (selectedTabIndex == 0) {
            // 3. Highlighted regional extreme AEMET Alert card
            AnimatedVisibility(
                visible = aemetAlert != null,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                if (aemetAlert != null) {
                    val isHeatRedWarning = aemetAlert!!.contains("ROJA")
                    val alertColor = if (isHeatRedWarning) Color(0xFFD32F2F) else Color(0xFFE65100)

                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = alertColor.copy(alpha = 0.12f)
                        ),
                        border = androidx.compose.foundation.BorderStroke(
                            width = 1.5.dp,
                            color = alertColor.copy(alpha = alertPulseAlpha)
                        ),
                        shape = RoundedCornerShape(18.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = "Avisos meteorológicos AEMET",
                                tint = alertColor,
                                modifier = Modifier.size(28.dp)
                            )
                            Column {
                                Text(
                                    text = if (isHeatRedWarning) "AVISO EXTREMO AEMET" else "AVISO REGIONAL AEMET",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Black,
                                    color = alertColor
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = aemetAlert!!,
                                    fontSize = 12.sp,
                                    color = onSurfaceColor,
                                    lineHeight = 16.sp
                                )
                            }
                        }
                    }
                }
            }

            // 4. Central Current Weather Bento Block with Animations background
            if (favorites.isEmpty() || selectedCity == null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = cardBackgroundColor,
                        contentColor = onSurfaceColor
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.25f))
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        ClimaIcon(
                            name = "cloudy",
                            tint = if (isDarkTheme) primaryCanaryYellow else Color(0xFF004993),
                            modifier = Modifier.size(64.dp)
                        )
                        Text(
                            text = "No hay ubicaciones",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = onSurfaceColor,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = if (userProfile != null) "Añade una ubicación utilizando las coordenadas de arriba o haz clic abajo para sincronizar y restaurar tus ciudades guardadas en Google." else "Añade una ubicación canaria utilizando las coordenadas de arriba o haz clic abajo para restaurar las ciudades por defecto.",
                            fontSize = 12.sp,
                            color = if (isDarkTheme) Color.LightGray else Color.Gray,
                            textAlign = TextAlign.Center,
                            lineHeight = 18.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Button(
                            onClick = { 
                                if (userProfile != null) {
                                    viewModel.triggerSync()
                                } else {
                                    viewModel.restorePredefinedCities()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isDarkTheme) primaryCanaryYellow else Color(0xFF004993),
                                contentColor = if (isDarkTheme) Color.Black else Color.White
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (userProfile != null) "Restaurar desde Google" else "Restaurar Ciudades Iniciales", 
                                fontSize = 12.sp, 
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            } else {
                when (val state = uiState) {
                    is WeatherUiState.Loading -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(180.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = primaryCanaryYellow)
                        }
                    }

                    is WeatherUiState.Success -> {
                        CurrentWeatherBentoBlock(
                            data = state.data,
                            isCelsius = isCelsius,
                            isDarkTheme = isDarkTheme,
                            cardBackgroundColor = cardBackgroundColor,
                            onSurfaceColor = onSurfaceColor
                        )
                    }

                    is WeatherUiState.Error -> {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            ClimaIcon(
                                name = "disconnected",
                                tint = Color.Gray,
                                modifier = Modifier.size(48.dp)
                            )
                            Text(
                                text = state.errorMessage,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color.Red,
                                textAlign = TextAlign.Center
                            )
                            Button(
                                onClick = { selectedCity?.let { viewModel.fetchWeatherForCity(it) } },
                                colors = ButtonDefaults.buttonColors(containerColor = primaryCanaryYellow)
                            ) {
                                Text("Reintentar Conexión", color = Color.Black)
                            }
                        }
                    }
                }

                // 5. Trend Chart Canvas Curve (Bento design)
                if (uiState is WeatherUiState.Success) {
                    val data = (uiState as WeatherUiState.Success).data
                    TrendChart(
                        hourlyItems = data.hourlyForecast,
                        isCelsius = isCelsius
                    )
                }

                // 6. Extended 7-Day Weather Forecast (Professional Polish design)
                if (uiState is WeatherUiState.Success) {
                    val data = (uiState as WeatherUiState.Success).data
                    DailyForecastBlock(
                        dailyForecast = data.dailyForecast,
                        isCelsius = isCelsius,
                        isDarkTheme = isDarkTheme
                    )
                }

                // 7. Air Quality Indicators Block (Bento design)
                if (uiState is WeatherUiState.Success) {
                    val data = (uiState as WeatherUiState.Success).data
                    AirQualityIndicator(airQuality = data.airQuality)
                    Spacer(modifier = Modifier.height(16.dp))
                    SunAndUvBlock(
                        uvIndex = data.uvIndex,
                        sunrise = data.sunrise,
                        sunset = data.sunset,
                        isDarkTheme = isDarkTheme
                    )
                }
            }
            } else {
                MarineWeatherScreenMode(
                    marineUiState = marineUiState,
                    selectedBeach = selectedBeach,
                    isDarkTheme = isDarkTheme,
                    primaryCanaryYellow = primaryCanaryYellow,
                    cardBackgroundColor = cardBackgroundColor,
                    onSurfaceColor = onSurfaceColor
                )
            }
        }
    }
}

@Composable
fun CurrentWeatherBentoBlock(
    data: WeatherDomainData,
    isCelsius: Boolean,
    isDarkTheme: Boolean,
    cardBackgroundColor: Color,
    onSurfaceColor: Color
) {
    val tempLabel = if (isCelsius) {
        "${data.temperatureCelsius.toInt()}°C"
    } else {
        "${(data.temperatureCelsius * 9/5 + 32).toInt()}°F"
    }

    val conditionName = when (data.condition) {
        WeatherCondition.SUNNY -> "Soleado / Despejado"
        WeatherCondition.CLOUDY -> "Nubosidad Variable"
        WeatherCondition.CALIMA -> "Presencia de Calima"
        WeatherCondition.RAINY -> "Lluvia de Vertiente"
        WeatherCondition.SNOWY -> "Nieve en Altas Cumbres"
        WeatherCondition.STORM -> "Tormentas Activas"
    }

    val iconName = when (data.condition) {
        WeatherCondition.SUNNY -> "sunny"
        WeatherCondition.CLOUDY -> "cloudy"
        WeatherCondition.CALIMA -> "calima"
        WeatherCondition.RAINY -> "rainy"
        WeatherCondition.SNOWY -> "snowy"
        WeatherCondition.STORM -> "storm"
    }

    val gradientBrush = if (isDarkTheme) {
        Brush.linearGradient(
            colors = listOf(Color(0xFF2C3135), Color(0xFF15181B))
        )
    } else {
        Brush.linearGradient(
            colors = listOf(Color(0xFF004993), Color(0xFF1E64B2))
        )
    }

    val contentColor = Color.White
    val borderTint = if (isDarkTheme) Color(0xFF383C42) else Color(0xFF004993).copy(alpha = 0.3f)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(32.dp))
            .background(gradientBrush)
            .border(1.2.dp, borderTint, RoundedCornerShape(32.dp))
            .height(220.dp)
    ) {
        // High fidelity canvas overlay representing moving environments (Calima/Storm/Sun/Rain)
        WeatherAnimations(
            condition = data.condition,
            modifier = Modifier.fillMaxSize()
        )

        // Text & metrics content overlaid safely with contrast shadow boxes
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(22.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Region metadata
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column {
                    Text(
                        text = data.cityName,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black,
                        color = contentColor
                    )
                    Text(
                        text = "Islas Canarias / España",
                        fontSize = 12.sp,
                        color = contentColor.copy(alpha = 0.8f)
                    )
                }

                // If fallback data is served (rate-limited / offline)
                if (data.isSynthetic) {
                    Box(
                        modifier = Modifier
                            .background(Color(0xFFE65100), RoundedCornerShape(10.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "Modo Fallback",
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Central thermal representation
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = tempLabel,
                    fontSize = 54.sp,
                    fontWeight = FontWeight.Black,
                    color = contentColor
                )
                
                Spacer(modifier = Modifier.width(16.dp))

                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        ClimaIcon(
                            name = iconName,
                            tint = if (data.condition == WeatherCondition.SUNNY) Color(0xFFFFD600) else contentColor,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = conditionName,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = contentColor
                        )
                    }
                    Text(
                        text = "Viento: ${data.windSpeedKmh.toInt()} km/h (${getWindDirectionLabel(data.windDirectionDegrees)})",
                        fontSize = 12.sp,
                        color = contentColor.copy(alpha = 0.8f)
                    )
                }
            }

            // Sub-bento secondary parameters bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Black.copy(alpha = 0.15f), RoundedCornerShape(14.dp))
                    .padding(10.dp),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                WeatherMetricItem(iconName = "humidity", label = "Humedad", valStr = "${data.humidity.toInt()}%", textColor = contentColor)
                WeatherMetricItem(iconName = "wind", label = "Dirección Viento", valStr = "${data.windDirectionDegrees.toInt()}°", textColor = contentColor)
                WeatherMetricItem(iconName = "gps", label = "Altitud Ref", valStr = "GPS", textColor = contentColor)
            }
        }
    }
}

@Composable
fun WeatherMetricItem(
    iconName: String,
    label: String,
    valStr: String,
    textColor: Color
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        ClimaIcon(
            name = iconName,
            tint = textColor.copy(alpha = 0.6f),
            modifier = Modifier.size(14.dp)
        )
        Column {
            Text(text = label, fontSize = 9.sp, color = Color.Gray)
            Text(text = valStr, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = textColor)
        }
    }
}

fun getWindDirectionLabel(degrees: Double): String {
    val directions = listOf("N", "NE", "E", "SE", "S", "SO", "O", "NO")
    val index = (((degrees + 22.5) % 360) / 45).toInt()
    return directions[index.coerceIn(0, 7)]
}

@Composable
fun DailyForecastBlock(
    dailyForecast: List<DailyForecastItem>,
    isCelsius: Boolean,
    isDarkTheme: Boolean,
    modifier: Modifier = Modifier
) {
    val primaryCanaryYellow = Color(0xFFFFD600)
    val cardBackgroundColor = if (isDarkTheme) Color(0xFF1E1C24) else Color.White
    val onSurfaceColor = if (isDarkTheme) Color(0xFFE6E1E5) else Color(0xFF1C1B1F)

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = cardBackgroundColor,
            contentColor = onSurfaceColor
        ),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.25f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(18.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Place,
                        contentDescription = null,
                        tint = if (isDarkTheme) primaryCanaryYellow else Color(0xFF004993),
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "PREDICCIÓN (7 DÍAS)",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        color = if (isDarkTheme) Color.LightGray else Color(0xFF004993)
                    )
                }
                
                // Badge decoration
                Box(
                    modifier = Modifier
                        .background(
                            color = (if (isDarkTheme) primaryCanaryYellow else Color(0xFF004993)).copy(alpha = 0.08f),
                            shape = RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "AEMET Semanal",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isDarkTheme) primaryCanaryYellow else Color(0xFF004993)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Forecast days
            if (dailyForecast.isEmpty()) {
                Text(
                    text = "No hay datos de predicción disponibles",
                    fontSize = 12.sp,
                    color = Color.LightGray,
                    modifier = Modifier.padding(vertical = 12.dp)
                )
            } else {
                dailyForecast.forEachIndexed { index, item ->
                    val tempMaxLabel = if (isCelsius) {
                        "${item.maxTemp.toInt()}°"
                    } else {
                        "${(item.maxTemp * 9/5 + 32).toInt()}°"
                    }
                    val tempMinLabel = if (isCelsius) {
                        "${item.minTemp.toInt()}°"
                    } else {
                        "${(item.minTemp * 9/5 + 32).toInt()}°"
                    }

                    val conditionIconName = when (item.condition) {
                        WeatherCondition.SUNNY -> "sunny"
                        WeatherCondition.CLOUDY -> "cloudy"
                        WeatherCondition.CALIMA -> "calima"
                        WeatherCondition.RAINY -> "rainy"
                        WeatherCondition.SNOWY -> "snowy"
                        WeatherCondition.STORM -> "storm"
                    }

                    val conditionText = when (item.condition) {
                        WeatherCondition.SUNNY -> "Soleado"
                        WeatherCondition.CLOUDY -> "Nublado"
                        WeatherCondition.CALIMA -> "Calima"
                        WeatherCondition.RAINY -> "Lluvia"
                        WeatherCondition.SNOWY -> "Nieve"
                        WeatherCondition.STORM -> "Tormenta"
                    }

                    val iconTint = when (item.condition) {
                        WeatherCondition.SUNNY -> Color(0xFFFFD600)
                        WeatherCondition.CALIMA -> if (isDarkTheme) Color(0xFFE9D8A6) else Color(0xFFEE9B00)
                        WeatherCondition.RAINY -> Color(0xFF42A5F5)
                        WeatherCondition.STORM -> Color(0xFF7E57C2)
                        else -> if (isDarkTheme) Color.LightGray else Color.Gray
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // 1. Day Name
                        Text(
                            text = item.dateString,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = onSurfaceColor,
                            modifier = Modifier.weight(1.2f)
                        )

                        // 2. Weather Icon & Name
                        Row(
                            modifier = Modifier.weight(1.8f),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Start
                        ) {
                            ClimaIcon(
                                name = conditionIconName,
                                tint = iconTint,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = conditionText,
                                fontSize = 11.sp,
                                color = onSurfaceColor.copy(alpha = 0.8f)
                            )
                        }

                        // 3. Precipitation Probability
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            if (item.precipitationProbability > 0) {
                                Text(
                                    text = "💧 ${item.precipitationProbability}%",
                                    fontSize = 11.sp,
                                    color = Color(0xFF42A5F5),
                                    fontWeight = FontWeight.SemiBold
                                )
                            } else {
                                Text(
                                    text = "0%",
                                    fontSize = 11.sp,
                                    color = Color.Gray.copy(alpha = 0.5f)
                                )
                            }
                        }

                        // 4. Low/High Temp graph-like representation
                        Row(
                            modifier = Modifier.weight(1.4f),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = tempMinLabel,
                                fontSize = 12.sp,
                                color = onSurfaceColor.copy(alpha = 0.5f)
                            )
                            // A tiny bar matching current range
                            Box(
                                modifier = Modifier
                                    .padding(horizontal = 6.dp)
                                    .width(20.dp)
                                    .height(4.dp)
                                    .background(
                                        color = if (isDarkTheme) Color(0xFF2D3135) else Color(0xFFECEFF1),
                                        shape = RoundedCornerShape(2.dp)
                                    )
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxHeight()
                                        .fillMaxWidth(0.6f)
                                        .align(Alignment.Center)
                                        .background(
                                            color = if (item.condition == WeatherCondition.CALIMA) Color(0xFFEE9B00) else primaryCanaryYellow,
                                            shape = RoundedCornerShape(2.dp)
                                        )
                                )
                            }
                            Text(
                                text = tempMaxLabel,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = onSurfaceColor
                            )
                        }
                    }

                    if (index < dailyForecast.size - 1) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(0.8.dp)
                                .background(if (isDarkTheme) Color(0xFF25282B) else Color(0xFFF1F5F9))
                        )
                    }
                }
            }
        }
    }
}
