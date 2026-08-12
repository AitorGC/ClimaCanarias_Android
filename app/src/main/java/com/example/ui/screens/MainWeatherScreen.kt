package com.example.ui.screens


import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.BackHandler
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import android.Manifest
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.Scope
import android.app.Activity

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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontStyle
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
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import com.example.viewmodel.WeatherUiState
import com.example.viewmodel.WarningsUiState
import com.example.viewmodel.WeatherViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun MainWeatherScreen(
    viewModel: WeatherViewModel,
    initialPage: Int = 0,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.weatherUiState.collectAsStateWithLifecycle()
    val isRefreshing by viewModel.isRefreshing.collectAsStateWithLifecycle()
    val favorites by viewModel.favorites.collectAsStateWithLifecycle()
    val favoriteBeaches by viewModel.favoriteBeaches.collectAsStateWithLifecycle()
    val selectedCity by viewModel.selectedCity.collectAsStateWithLifecycle()
    val actualLocation by viewModel.actualLocation.collectAsStateWithLifecycle()
    val isCelsius by viewModel.isCelsius.collectAsStateWithLifecycle()
    val isDarkTheme by viewModel.isDarkTheme.collectAsStateWithLifecycle()
    val aemetAlert by viewModel.aemetAlert.collectAsStateWithLifecycle()

    val userProfile by viewModel.cloudSync.userProfile.collectAsStateWithLifecycle()
    val isSyncing by viewModel.cloudSync.isSyncing.collectAsStateWithLifecycle()
    val lastSyncTime by viewModel.cloudSync.lastSyncTime.collectAsStateWithLifecycle()

    val beaches by viewModel.beaches.collectAsStateWithLifecycle()
    val selectedBeach by viewModel.selectedBeach.collectAsStateWithLifecycle()
    val marineUiState by viewModel.marineUiState.collectAsStateWithLifecycle()
    val selectedIslands by viewModel.selectedIslands.collectAsStateWithLifecycle()

    val tabs = listOf(
        "Clima" to Icons.Default.WbSunny,
        "Playa" to Icons.Default.BeachAccess,
        "Alertas" to Icons.Default.Notifications,
        "Estaciones" to Icons.Default.Place,
        "Satélite" to Icons.Default.Public
    )
    val pagerState = rememberPagerState(
        initialPage = initialPage.coerceIn(0, tabs.size - 1),
        pageCount = { tabs.size }
    )
    val coroutineScope = rememberCoroutineScope()
    
    
    var initialAlertPopupChecked by rememberSaveable { mutableStateOf(false) }
    var showInitialAlertPopup by rememberSaveable { mutableStateOf(false) }
    val warningsState by viewModel.warningsUiState.collectAsStateWithLifecycle()

    LaunchedEffect(pagerState.currentPage) {
        when (pagerState.currentPage) {
            2 -> viewModel.loadWarnings()
            3 -> viewModel.loadAemetStations()
        }
    }

    LaunchedEffect(Unit) {
        if (selectedIslands.isNotEmpty()) {
            viewModel.loadWarnings()
        }
    }

    LaunchedEffect(warningsState, selectedIslands) {
        if (!initialAlertPopupChecked && selectedIslands.isNotEmpty() && warningsState is com.example.viewmodel.WarningsUiState.Success) {
            val warnings = (warningsState as com.example.viewmodel.WarningsUiState.Success).warnings
            val hasAlerts = warnings.any { warning ->
                selectedIslands.any { island ->
                    warning.ambitoGeografico?.contains(island, ignoreCase = true) == true && isWarningActive(warning)
                }
            }
            if (hasAlerts) {
                showInitialAlertPopup = true
            }
            initialAlertPopupChecked = true
        }
    }

    if (showInitialAlertPopup) {
        AlertDialog(
            onDismissRequest = { showInitialAlertPopup = false },
            containerColor = if (isDarkTheme) Color(0xFF1E1C24) else Color.White,
            titleContentColor = if (isDarkTheme) Color.White else Color.Black,
            textContentColor = if (isDarkTheme) Color.LightGray else Color.DarkGray,
            title = { Text("Alerta Meteorológica") },
            text = { Text("Existe una alerta meteorológica vigente en las islas que has seleccionado.") },
            confirmButton = {
                Button(
                    onClick = {
                        showInitialAlertPopup = false
                        coroutineScope.launch { pagerState.animateScrollToPage(2) }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isDarkTheme) Color(0xFFFFD600) else Color(0xFF004993),
                        contentColor = if (isDarkTheme) Color.Black else Color.White
                    )
                ) {
                    Text("Aceptar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showInitialAlertPopup = false }) {
                    Text("Cerrar", color = if (isDarkTheme) Color.White else Color.Black)
                }
            }
        )
    }
    
    var showSyncModal by remember { mutableStateOf(false) }
    var showMainMenu by remember { mutableStateOf(false) }
    var showFavoritesModal by remember { mutableStateOf(false) }
    
    
    

    val scrollState = rememberScrollState()

    // Base layout with custom theme colours
    val primaryCanaryYellow = Color(0xFFFFD600)
    
    // Light Background is off-white (cream), dark is clean charcoal
    val appBackgroundColor = if (isDarkTheme) Color(0xFF141318) else Color.White
    val cardBackgroundColor = if (isDarkTheme) Color(0xFF1E1C24) else Color(0xFFF4F7FA)
    val onSurfaceColor = if (isDarkTheme) Color(0xFFE6E1E5) else Color(0xFF141318)

    // Pulse animation for alerting alerts
    
    val context = LocalContext.current

    val locationPermissionState = rememberMultiplePermissionsState(
        permissions = listOf(
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
    )

    var wantsLocation by remember { mutableStateOf(false) }

    LaunchedEffect(locationPermissionState.allPermissionsGranted) {
        if (locationPermissionState.allPermissionsGranted && wantsLocation) {
            viewModel.fetchCurrentLocation(context)
            wantsLocation = false
        }
    }

    BackHandler(enabled = pagerState.currentPage != 0) {
        coroutineScope.launch {
            pagerState.animateScrollToPage(0)
        }
    }

    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)
                if (account != null) {
                    viewModel.cloudSync.handleSignInResult(context, account)
                }
            } catch (e: Exception) {
                // ignore
            }
        }
    }

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
        bottomBar = {
            NavigationBar(
                containerColor = if (isDarkTheme) Color(0xFF141318) else Color.White,
                contentColor = if (isDarkTheme) primaryCanaryYellow else Color(0xFF004993)
            ) {
                tabs.forEachIndexed { index, (title, icon) ->
                    NavigationBarItem(
                        icon = { Icon(icon, contentDescription = title) },
                        label = { Text(title, fontSize = 10.sp, maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis) },
                        selected = pagerState.currentPage == index,
                        onClick = {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(index)
                            }
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = primaryCanaryYellow,
                            selectedTextColor = if (isDarkTheme) primaryCanaryYellow else Color(0xFF004993),
                            indicatorColor = Color(0xFF004993),
                            unselectedIconColor = if (isDarkTheme) Color.LightGray else Color.DarkGray,
                            unselectedTextColor = if (isDarkTheme) Color.LightGray else Color.DarkGray
                        )
                    )
                }
            }
        },
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
                            text = "F",
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

                    // Hamburger Menu
                    Box {
                        IconButton(
                            onClick = { showMainMenu = true },
                            modifier = Modifier.testTag("main_menu_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Menu,
                                contentDescription = "Menú Principal",
                                tint = Color.White
                            )
                        }
                        
                        DropdownMenu(
                            expanded = showMainMenu,
                            onDismissRequest = { showMainMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Favoritos") },
                                onClick = {
                                    showMainMenu = false
                                    showFavoritesModal = true
                                },
                                leadingIcon = {
                                    Icon(Icons.Default.Favorite, contentDescription = null, modifier = Modifier.size(20.dp))
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Ajustes") },
                                onClick = {
                                    showMainMenu = false
                                    showSyncModal = true
                                },
                                leadingIcon = {
                                    Icon(Icons.Default.Settings, contentDescription = null, modifier = Modifier.size(20.dp))
                                }
                            )
                        }
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
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.weight(1f).fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) { page ->
                Box(
                    modifier = Modifier.fillMaxSize().padding(16.dp)
                ) {
                    when (page) {
                        0 -> {
                            PullToRefreshBox(
                                isRefreshing = isRefreshing,
                                onRefresh = { viewModel.refreshData() },
                                modifier = Modifier
                                    .fillMaxSize()
                                    .testTag("swipe_refresh_dashboard")
                            ) {
                                Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                                    
                        FavoriteCitiesManager(
                            favorites = favorites,
                            selectedCity = selectedCity,
                            actualLocation = actualLocation,
                            onCitySelected = { viewModel.selectCity(it) },
                            onSearchRegion = { query, callback -> viewModel.searchAndAddLocation(query, callback) },
                            onDeleteFavorite = { viewModel.removeFavorite(it) },
                            onRemoveActualLocation = { viewModel.removeActualLocation() },
                            onDetectLocation = {
                                if (locationPermissionState.allPermissionsGranted) {
                                    viewModel.fetchCurrentLocation(context)
                                } else {
                                    wantsLocation = true
                                    locationPermissionState.launchMultiplePermissionRequest()
                                }
                            }
                        )

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
                            text = if (userProfile != null) "Busca y añade una ubicación utilizando el buscador de arriba o haz clic abajo para sincronizar y restaurar tus ciudades guardadas en Google." else "Busca y añade una ubicación utilizando el buscador de arriba o haz clic abajo para restaurar las ciudades por defecto.",
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
                        Spacer(modifier = Modifier.height(16.dp))

                        // AEMET ALERTS for Current City
                        val currentCity = selectedCity
                        if (currentCity != null && warningsState is WarningsUiState.Success) {
                            val islandName = getIslandForCity(currentCity.name, currentCity.latitude, currentCity.longitude)
                            val cityAlerts = (warningsState as WarningsUiState.Success).warnings.filter { warning ->
                                val ambito = warning.ambitoGeografico?.lowercase() ?: ""
                                val cityMatch = ambito.contains(currentCity.name.lowercase())
                                val islandMatch = islandName.isNotEmpty() && ambito.contains(islandName.lowercase())
                                (cityMatch || islandMatch) && isWarningActive(warning)
                            }
                            if (cityAlerts.isNotEmpty()) {
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    cityAlerts.forEach { warning ->
                                        val warningColor = when (warning.nivel?.lowercase()) {
                                            "rojo" -> Color(0xFFD32F2F)
                                            "naranja" -> Color(0xFFF57C00)
                                            "amarillo" -> Color(0xFFFBC02D)
                                            else -> if (isDarkTheme) Color.White else Color.Black
                                        }
                                        Card(
                                            colors = CardDefaults.cardColors(containerColor = cardBackgroundColor),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Column(modifier = Modifier.padding(12.dp)) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Icon(Icons.Default.Warning, contentDescription = null, tint = warningColor, modifier = Modifier.size(20.dp))
                                                    Spacer(modifier = Modifier.width(8.dp))
                                                    Text(
                                                        text = warning.fenomeno ?: "Aviso",
                                                        fontWeight = FontWeight.Bold,
                                                        color = warningColor
                                                    )
                                                }
                                                if (!warning.ambitoGeografico.isNullOrEmpty()) {
                                                    Spacer(modifier = Modifier.height(4.dp))
                                                    Text(
                                                        text = warning.ambitoGeografico,
                                                        fontSize = 14.sp,
                                                        fontWeight = FontWeight.SemiBold,
                                                        color = onSurfaceColor
                                                    )
                                                }
                                                if (!warning.descripcion.isNullOrEmpty()) {
                                                    Spacer(modifier = Modifier.height(4.dp))
                                                    Text(
                                                        text = warning.descripcion,
                                                        fontSize = 13.sp,
                                                        color = onSurfaceColor.copy(alpha = 0.8f)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(16.dp))
                            }
                        }

                        SunAndUvBlock(
                            uvIndex = state.data.uvIndex,
                            sunrise = state.data.sunrise,
                            sunset = state.data.sunset,
                            isDarkTheme = isDarkTheme
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
                }
            }
        }
    }
}
                        1 -> {
                            PullToRefreshBox(
                                isRefreshing = isRefreshing,
                                onRefresh = { viewModel.refreshData() },
                                modifier = Modifier
                                    .fillMaxSize()
                                    .testTag("swipe_refresh_beach")
                            ) {
                                Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                                
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Box(modifier = Modifier.weight(1f)) {
                                BeachSelectionDropdown(
                                    beaches = beaches,
                                    selectedBeach = selectedBeach,
                                    onBeachSelected = { viewModel.selectBeachId(it) }
                                )
                            }
                            if (selectedBeach != null) {
                                val isFavoriteMode = favoriteBeaches.any { it.id == selectedBeach!!.id }
                                IconButton(
                                    onClick = {
                                        if (isFavoriteMode) {
                                            favoriteBeaches.find { it.id == selectedBeach!!.id }?.let { viewModel.removeFavoriteBeach(it) }
                                        } else {
                                            viewModel.addFavoriteBeach(selectedBeach!!.id, selectedBeach!!.nombre)
                                        }
                                    }
                                ) {
                                    Icon(
                                        imageVector = if (isFavoriteMode) Icons.Default.Star else Icons.Default.StarBorder,
                                        contentDescription = "Favorito",
                                        tint = if (isFavoriteMode) Color(0xFFFFD600) else Color.Gray,
                                        modifier = Modifier.size(32.dp)
                                    )
                                }
                            }
                        }
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
                        2 -> {
                            Box(modifier = Modifier.fillMaxSize()) {
                                
                            
                            val islasCanarias = listOf("El Hierro", "Fuerteventura", "Gran Canaria", "La Gomera", "La Palma", "Lanzarote", "Tenerife")
                            when (val state = warningsState) {
                    is WarningsUiState.Idle, is WarningsUiState.Loading -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = primaryCanaryYellow)
                        }
                    }
                    is WarningsUiState.Error -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(
                                text = state.message,
                                color = MaterialTheme.colorScheme.error,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    }
                    is WarningsUiState.Success -> {
                        val warnings = state.warnings
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            items(islasCanarias) { isla ->
                                val alertasIsla = warnings.filter { 
                                    it.ambitoGeografico?.contains(isla, ignoreCase = true) == true && isWarningActive(it)
                                }
                                
                                Column(modifier = Modifier.fillMaxWidth()) {
                                    Text(
                                        text = isla,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 18.sp,
                                        color = if (isDarkTheme) primaryCanaryYellow else Color(0xFF004993),
                                        modifier = Modifier.padding(bottom = 8.dp)
                                    )
                                    
                                    if (alertasIsla.isEmpty()) {
                                        Text(
                                            text = "No hay alertas",
                                            color = if (isDarkTheme) Color.LightGray else Color.DarkGray,
                                            fontSize = 14.sp,
                                            modifier = Modifier.padding(start = 8.dp)
                                        )
                                    } else {
                                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                            alertasIsla.forEach { warning ->
                                                val warningColor = when (warning.nivel?.lowercase()) {
                                                    "rojo" -> Color(0xFFD32F2F)
                                                    "naranja" -> Color(0xFFF57C00)
                                                    "amarillo" -> Color(0xFFFBC02D)
                                                    else -> if (isDarkTheme) Color.White else Color.Black
                                                }
                                                
                                                Card(
                                                    colors = CardDefaults.cardColors(containerColor = cardBackgroundColor),
                                                    modifier = Modifier.fillMaxWidth()
                                                ) {
                                                    Column(modifier = Modifier.padding(12.dp)) {
                                                        Text(
                                                            text = warning.ambitoGeografico ?: "Zona no especificada",
                                                            fontWeight = FontWeight.SemiBold,
                                                            fontSize = 14.sp,
                                                            color = onSurfaceColor
                                                        )
                                                        Spacer(modifier = Modifier.height(4.dp))
                                                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                                            Icon(Icons.Default.Warning, contentDescription = null, tint = warningColor, modifier = Modifier.size(20.dp))
                                                            Text(
                                                                text = warning.fenomeno ?: "Aviso",
                                                                fontWeight = FontWeight.Bold,
                                                                fontSize = 14.sp,
                                                                color = warningColor
                                                            )
                                                        }
                                                        if (!warning.descripcion.isNullOrEmpty()) {
                                                            Spacer(modifier = Modifier.height(4.dp))
                                                            Text(
                                                                text = warning.descripcion,
                                                                fontSize = 13.sp,
                                                                color = onSurfaceColor.copy(alpha = 0.8f)
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
                        3 -> {
                            
                            val stationsState by viewModel.aemetStationsUiState.collectAsStateWithLifecycle()
                            var searchQuery by remember { mutableStateOf("") }
                            var expandedStationId by remember { mutableStateOf<String?>(null) }
                                            Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Buscar estación o provincia...") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )

                    Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                        when (val state = stationsState) {
                        is com.example.viewmodel.AemetStationsUiState.Idle -> {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("Inicializando...", color = Color.Gray)
                            }
                        }
                        is com.example.viewmodel.AemetStationsUiState.Loading -> {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    CircularProgressIndicator(
                                        color = if (isDarkTheme) primaryCanaryYellow else Color(0xFF004993)
                                    )
                                    Text(
                                        text = "Extrayendo estaciones de AEMET OpenData...",
                                        fontSize = 14.sp,
                                        color = Color.Gray,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                        is com.example.viewmodel.AemetStationsUiState.Error -> {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(12.dp),
                                    modifier = Modifier.padding(16.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Warning,
                                        contentDescription = "Error",
                                        tint = Color.Red,
                                        modifier = Modifier.size(48.dp)
                                    )
                                    Text(
                                        text = state.message,
                                        color = Color.Red,
                                        fontSize = 14.sp,
                                        textAlign = TextAlign.Center
                                    )
                                    Button(
                                        onClick = { viewModel.loadAemetStations() },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (isDarkTheme) primaryCanaryYellow else Color(0xFF004993)
                                        )
                                    ) {
                                        Text("Reintentar")
                                    }
                                }
                            }
                        }
                        is com.example.viewmodel.AemetStationsUiState.Success -> {
                            val filteredStations = state.stations.filter {
                                it.nombre.lowercase().contains(searchQuery.lowercase()) ||
                                it.provincia.lowercase().contains(searchQuery.lowercase()) ||
                                it.indicativo.lowercase().contains(searchQuery.lowercase())
                            }

                            if (filteredStations.isEmpty()) {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "No se encontraron estaciones para \"$searchQuery\"",
                                        color = Color.Gray,
                                        fontSize = 14.sp,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            } else {
                                LazyColumn(
                                    verticalArrangement = Arrangement.spacedBy(10.dp),
                                    modifier = Modifier.fillMaxSize()
                                ) {
                                    items(items = filteredStations, key = { it.indicativo }) { station ->
                                        val isExpanded = expandedStationId == station.indicativo
                                        
                                        LaunchedEffect(isExpanded) {
                                            if (isExpanded && station.temperatura == null && !station.isLoadingObservation && station.observationError == null) {
                                                viewModel.loadAemetStationObservation(station.indicativo)
                                            }
                                        }

                                        Card(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .animateContentSize(),
                                            colors = CardDefaults.cardColors(
                                                containerColor = if (isDarkTheme) Color(0xFF23212A) else Color(0xFFF0F3F6)
                                            ),
                                            shape = RoundedCornerShape(12.dp)
                                        ) {
                                            Column(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clickable {
                                                        expandedStationId = if (isExpanded) null else station.indicativo
                                                    }
                                                    .padding(12.dp),
                                                verticalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Column(modifier = Modifier.weight(1f)) {
                                                        Text(
                                                            text = station.nombre,
                                                            fontWeight = FontWeight.Bold,
                                                            fontSize = 15.sp,
                                                            color = if (isDarkTheme) Color.White else Color(0xFF141318)
                                                        )
                                                        Spacer(modifier = Modifier.height(2.dp))
                                                        Row(
                                                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                                                            verticalAlignment = Alignment.CenterVertically
                                                        ) {
                                                            Text(
                                                                text = "Cod: ${station.indicativo}",
                                                                fontSize = 11.sp,
                                                                color = Color.Gray
                                                            )
                                                            Text(
                                                                text = "•",
                                                                fontSize = 11.sp,
                                                                color = Color.Gray
                                                            )
                                                            Text(
                                                                text = "${station.altitud.toInt()} msnm",
                                                                fontSize = 11.sp,
                                                                color = Color.Gray
                                                            )
                                                        }
                                                    }
                                                    
                                                    val isTenerife = station.provincia.lowercase().contains("tenerife") || station.provincia.lowercase().contains("cruz")
                                                    Box(
                                                        modifier = Modifier
                                                            .background(
                                                                color = Color(0xFF004993),
                                                                shape = RoundedCornerShape(6.dp)
                                                            )
                                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                                    ) {
                                                        Text(
                                                            text = if (isTenerife) "S.C. de Tenerife" else "Las Palmas",
                                                            fontSize = 10.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            color = if (isTenerife) Color.White else primaryCanaryYellow
                                                        )
                                                    }
                                                }

                                                if (isExpanded) {
                                                    HorizontalDivider(
                                                        color = if (isDarkTheme) Color.White.copy(alpha = 0.1f) else Color.Black.copy(alpha = 0.05f),
                                                        modifier = Modifier.padding(vertical = 4.dp)
                                                    )

                                                    if (station.isLoadingObservation) {
                                                        Column(
                                                            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                                                            horizontalAlignment = Alignment.CenterHorizontally,
                                                            verticalArrangement = Arrangement.spacedBy(6.dp)
                                                        ) {
                                                            LinearProgressIndicator(
                                                                modifier = Modifier.fillMaxWidth(0.5f),
                                                                color = if (isDarkTheme) primaryCanaryYellow else Color(0xFF004993)
                                                            )
                                                            Text(
                                                                text = "Consultando sensores en tiempo real...",
                                                                fontSize = 11.sp,
                                                                color = Color.Gray
                                                            )
                                                        }
                                                    } else if (station.observationError != null) {
                                                        Row(
                                                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                                            verticalAlignment = Alignment.CenterVertically,
                                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                                        ) {
                                                            Icon(
                                                                imageVector = Icons.Default.Info,
                                                                contentDescription = null,
                                                                tint = Color.Gray,
                                                                modifier = Modifier.size(16.dp)
                                                            )
                                                            Text(
                                                                text = station.observationError,
                                                                fontSize = 12.sp,
                                                                color = Color.Gray
                                                            )
                                                        }
                                                    } else if (station.fechaObservacion != null) {
                                                        Column(
                                                            modifier = Modifier.fillMaxWidth(),
                                                            verticalArrangement = Arrangement.spacedBy(8.dp)
                                                        ) {
                                                            val cleanTime = try {
                                                                station.fechaObservacion.substringAfter("T").substringBeforeLast(":")
                                                            } catch (e: Exception) {
                                                                station.fechaObservacion
                                                            }
                                                            Text(
                                                                text = "Datos reportados a las $cleanTime",
                                                                fontSize = 11.sp,
                                                                color = Color.Gray,
                                                                fontStyle = FontStyle.Italic
                                                            )

                                                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                                                Row(
                                                                    modifier = Modifier.fillMaxWidth(),
                                                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                                                ) {
                                                                    Column(
                                                                        modifier = Modifier
                                                                            .weight(1f)
                                                                            .background(
                                                                                color = if (isDarkTheme) Color(0xFF2C2935) else Color(0xFFE2E7EC),
                                                                                shape = RoundedCornerShape(8.dp)
                                                                            )
                                                                            .padding(8.dp),
                                                                        horizontalAlignment = Alignment.CenterHorizontally
                                                                    ) {
                                                                        Text("Temperatura", fontSize = 10.sp, color = Color.Gray)
                                                                        Text(
                                                                            text = if (station.temperatura != null) "${station.temperatura} °C" else "-",
                                                                            fontWeight = FontWeight.Bold,
                                                                            fontSize = 14.sp,
                                                                            color = if (isDarkTheme) primaryCanaryYellow else Color(0xFF004993)
                                                                        )
                                                                    }
                                                                    Column(
                                                                        modifier = Modifier
                                                                            .weight(1.5f)
                                                                            .background(
                                                                                color = if (isDarkTheme) Color(0xFF2C2935) else Color(0xFFE2E7EC),
                                                                                shape = RoundedCornerShape(8.dp)
                                                                            )
                                                                            .padding(8.dp),
                                                                        horizontalAlignment = Alignment.CenterHorizontally
                                                                    ) {
                                                                        Text("Viento", fontSize = 10.sp, color = Color.Gray)
                                                                        val velStr = if (station.vientoVelocidad != null) "${(station.vientoVelocidad * 3.6).toInt()} km/h" else "-"
                                                                        val dirStr = if (station.vientoDireccion != null) getWindDirectionCode(station.vientoDireccion) else ""
                                                                        val rachaStr = if (station.racha != null) " (Racha: ${(station.racha * 3.6).toInt()} km/h)" else ""
                                                                        Text(
                                                                            text = "$velStr $dirStr$rachaStr".trim(),
                                                                            fontWeight = FontWeight.Bold,
                                                                            fontSize = 14.sp,
                                                                            color = if (isDarkTheme) Color(0xFFB9F6CA) else Color(0xFF2E7D32),
                                                                            textAlign = TextAlign.Center
                                                                        )
                                                                    }
                                                                }
                                                                Row(
                                                                    modifier = Modifier.fillMaxWidth(),
                                                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                                                ) {
                                                                    Column(
                                                                        modifier = Modifier
                                                                            .weight(1f)
                                                                            .background(
                                                                                color = if (isDarkTheme) Color(0xFF2C2935) else Color(0xFFE2E7EC),
                                                                                shape = RoundedCornerShape(8.dp)
                                                                            )
                                                                            .padding(8.dp),
                                                                        horizontalAlignment = Alignment.CenterHorizontally
                                                                    ) {
                                                                        Text("Precipitación", fontSize = 10.sp, color = Color.Gray)
                                                                        Text(
                                                                            text = if (station.precipitacion != null) "${station.precipitacion} mm" else "-",
                                                                            fontWeight = FontWeight.Bold,
                                                                            fontSize = 14.sp,
                                                                            color = if (isDarkTheme) Color(0xFF80D8FF) else Color(0xFF00838F)
                                                                        )
                                                                    }
                                                                    Column(
                                                                        modifier = Modifier
                                                                            .weight(1f)
                                                                            .background(
                                                                                color = if (isDarkTheme) Color(0xFF2C2935) else Color(0xFFE2E7EC),
                                                                                shape = RoundedCornerShape(8.dp)
                                                                            )
                                                                            .padding(8.dp),
                                                                        horizontalAlignment = Alignment.CenterHorizontally
                                                                    ) {
                                                                        Text("Presión", fontSize = 10.sp, color = Color.Gray)
                                                                        Text(
                                                                            text = if (station.presion != null) "${station.presion} hPa" else "-",
                                                                            fontWeight = FontWeight.Bold,
                                                                            fontSize = 14.sp,
                                                                            color = onSurfaceColor
                                                                        )
                                                                    }
                                                                    Column(
                                                                        modifier = Modifier
                                                                            .weight(1f)
                                                                            .background(
                                                                                color = if (isDarkTheme) Color(0xFF2C2935) else Color(0xFFE2E7EC),
                                                                                shape = RoundedCornerShape(8.dp)
                                                                            )
                                                                            .padding(8.dp),
                                                                        horizontalAlignment = Alignment.CenterHorizontally
                                                                    ) {
                                                                        Text("Humedad", fontSize = 10.sp, color = Color.Gray)
                                                                        Text(
                                                                            text = if (station.humedad != null) "${station.humedad.toInt()} %" else "-",
                                                                            fontWeight = FontWeight.Bold,
                                                                            fontSize = 14.sp,
                                                                            color = if (isDarkTheme) Color(0xFF80D8FF) else Color(0xFF00838F)
                                                                        )
                                                                    }
                                                                }
                                                            }
                                                            Button(
                                                                onClick = {
                                                                    viewModel.addCustomFavorite(
                                                                        name = station.nombre,
                                                                        latitude = station.latitud,
                                                                        longitude = station.longitud
                                                                    )
                                                                    
                                                                },
                                                                modifier = Modifier.fillMaxWidth(),
                                                                colors = ButtonDefaults.buttonColors(
                                                                    containerColor = if (isDarkTheme) primaryCanaryYellow else Color(0xFF004993),
                                                                    contentColor = if (isDarkTheme) Color.Black else Color.White
                                                                ),
                                                                shape = RoundedCornerShape(8.dp)
                                                            ) {
                                                                Row(
                                                                    verticalAlignment = Alignment.CenterVertically,
                                                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                                                ) {
                                                                    Text("Añadir a mis ubicaciones", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                        }
                    }
                        4 -> {
                            Box(modifier = Modifier.fillMaxWidth().height(500.dp)) {
                        var isSatelliteLoading by remember { mutableStateOf(true) }
                        androidx.compose.ui.viewinterop.AndroidView(
                            factory = { context ->
                                android.webkit.WebView(context).apply {
                                    settings.javaScriptEnabled = true
                                    settings.domStorageEnabled = true
                                    webViewClient = object : android.webkit.WebViewClient() {
                                        override fun onRenderProcessGone(view: android.webkit.WebView?, detail: android.webkit.RenderProcessGoneDetail?): Boolean {
                                            return true
                                        }

                                        override fun onPageFinished(view: android.webkit.WebView?, url: String?) {
                                            super.onPageFinished(view, url)
                                            isSatelliteLoading = false
                                            view?.evaluateJavascript("""
                                                (function() {
                                                    var target = document.getElementById('block-10593');
                                                    if (target) {
                                                        var node = target;
                                                        while (node && node !== document.body) {
                                                            if (node.classList) {
                                                                node.classList.remove('hide-mobile');
                                                                node.classList.remove('hide-tablet');
                                                                node.classList.remove('hidden');
                                                            }
                                                            node.style.display = 'block';
                                                            var siblings = node.parentNode.children;
                                                            for (var i = 0; i < siblings.length; i++) {
                                                                if (siblings[i] !== node && siblings[i].tagName !== 'SCRIPT' && siblings[i].tagName !== 'STYLE') {
                                                                    siblings[i].style.display = 'none';
                                                                }
                                                            }
                                                            node = node.parentNode;
                                                        }
                                                        document.body.style.margin = '0';
                                                        document.body.style.padding = '0';
                                                    }
                                                })();
                                            """.trimIndent(), null)
                                        }
                                    }
                                    loadUrl("https://www.sat24.com/es-es/region/8000076")
                                }
                            },
                            modifier = Modifier.fillMaxSize()
                        )
                        if (isSatelliteLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.align(Alignment.Center),
                                color = primaryCanaryYellow
                            )
                        }
                    }
                        }
                    }
                }
            }
        }
    }
    
    if (showSyncModal) {
        val islasCanarias = listOf(
            "El Hierro",
            "Fuerteventura",
            "Gran Canaria",
            "La Gomera",
            "La Palma",
            "Lanzarote",
            "Tenerife"
        )
        AlertDialog(
            titleContentColor = onSurfaceColor,
            textContentColor = onSurfaceColor,
            containerColor = if (isDarkTheme) Color.Black else Color.White,
            onDismissRequest = { showSyncModal = false },
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = null,
                        tint = if (isDarkTheme) primaryCanaryYellow else Color(0xFF004993),
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = "Ajustes de la Aplicación", color = onSurfaceColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                }
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.verticalScroll(rememberScrollState())
                ) {
                    // SECCIÓN 1: ISLAS PREFERIDAS PARA ALERTAS AEMET
                    Text(
                        text = "Avisos de Alertas AEMET",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = if (isDarkTheme) primaryCanaryYellow else Color(0xFF004993)
                    )
                    Text(
                        text = "Selecciona tus islas preferidas. Recibirás un aviso al abrir la aplicación cuando existan alertas vigentes en estas islas:",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )

                    Column(
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        islasCanarias.forEach { island ->
                            val isChecked = selectedIslands.contains(island)
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { viewModel.toggleIslandSelection(island) }
                                    .padding(vertical = 4.dp)
                            ) {
                                Checkbox(
                                    checked = isChecked,
                                    onCheckedChange = { viewModel.toggleIslandSelection(island) },
                                    colors = CheckboxDefaults.colors(
                                        checkedColor = if (isDarkTheme) primaryCanaryYellow else Color(0xFF004993)
                                    )
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = island,
                                    fontSize = 14.sp,
                                    color = onSurfaceColor
                                )
                            }
                        }
                    }

                    HorizontalDivider(
                        color = if (isDarkTheme) Color.White.copy(alpha = 0.1f) else Color.Black.copy(alpha = 0.05f),
                        modifier = Modifier.padding(vertical = 4.dp)
                    )

                    // SECCIÓN 2: CUENTA Y SINCRONIZACIÓN
                    Text(
                        text = "Cuenta de Google y Sincronización",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = if (isDarkTheme) primaryCanaryYellow else Color(0xFF004993)
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = if (userProfile != null) primaryCanaryYellow else Color.LightGray,
                            modifier = Modifier.size(48.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = null,
                                    tint = Color.Black,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }

                        Column {
                            if (userProfile != null) {
                                Text(
                                    text = "Sincronizado: ${userProfile!!.displayName}",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = onSurfaceColor
                                )
                                Text(
                                    text = userProfile!!.email ?: "",
                                    fontSize = 12.sp,
                                    color = Color.Gray
                                )
                            } else {
                                Text(
                                    text = "Modo Offline",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = onSurfaceColor
                                )
                                Text(
                                    text = "Sincroniza tus ubicaciones en la nube activando Google",
                                    fontSize = 12.sp,
                                    color = Color.Gray
                                )
                            }
                        }
                    }

                    if (userProfile == null) {
                        Button(
                            onClick = { 
                                val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                                    .requestEmail()
                                    .requestScopes(Scope("https://www.googleapis.com/auth/drive.appdata"))
                                    .build()
                                val googleSignInClient = GoogleSignIn.getClient(context, gso)
                                googleSignInLauncher.launch(googleSignInClient.signInIntent)
                                showSyncModal = false
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Conectar con Google")
                        }
                    } else {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Ajustes de Sincronización", fontWeight = FontWeight.Bold)
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Button(
                                    onClick = { 
                                        viewModel.triggerSaveToCloud()
                                        showSyncModal = false
                                    },
                                    modifier = Modifier.weight(1f),
                                    enabled = !isSyncing
                                ) {
                                    Text("Guardar")
                                }
                                
                                Button(
                                    onClick = { 
                                        viewModel.triggerRestoreFromCloud()
                                        showSyncModal = false
                                    },
                                    modifier = Modifier.weight(1f),
                                    enabled = !isSyncing
                                ) {
                                    Text("Restaurar")
                                }
                            }
                            OutlinedButton(
                                onClick = { 
                                    viewModel.cloudSync.logout()
                                    showSyncModal = false
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Cerrar Sesión", color = Color.Red)
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "ClimaCanarias v2.2.6",
                                fontSize = 12.sp,
                                color = onSurfaceColor.copy(alpha = 0.6f),
                                modifier = Modifier.align(Alignment.CenterHorizontally)
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showSyncModal = false }) {
                    Text("Cerrar")
                }
            }
        )
    }

    if (showFavoritesModal) {
        AlertDialog(
            titleContentColor = onSurfaceColor,
            textContentColor = onSurfaceColor,
            containerColor = if (isDarkTheme) Color.Black else Color.White,
            onDismissRequest = { showFavoritesModal = false },
            title = {
                Text(
                    text = "Tus Favoritos", color = onSurfaceColor,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            },
            text = {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    item {
                        Text("Zonas (Clima)", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }
                    if (favorites.isEmpty()) {
                        item { Text("No hay zonas favoritas", color = Color.Gray) }
                    } else {
                        items(favorites) { city ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        coroutineScope.launch { pagerState.animateScrollToPage(0) }
                                        viewModel.selectCity(city)
                                        showFavoritesModal = false
                                    },
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                            ) {
                                Text(
                                    text = city.name,
                                    modifier = Modifier.padding(16.dp)
                                )
                            }
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Playas", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }
                    if (favoriteBeaches.isEmpty()) {
                        item { Text("No hay playas favoritas", color = Color.Gray) }
                    } else {
                        items(favoriteBeaches) { beach ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        coroutineScope.launch { pagerState.animateScrollToPage(1) }
                                        viewModel.selectBeachId(beach.id)
                                        showFavoritesModal = false
                                    },
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                            ) {
                                Text(
                                    text = beach.name,
                                    modifier = Modifier.padding(16.dp)
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showFavoritesModal = false }) {
                    Text("Cerrar")
                }
            }
        )
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
        "${(data.temperatureCelsius * 9/5 + 32).toInt()}F"
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
            .heightIn(min = 230.dp)
    ) {
        // High fidelity canvas overlay representing moving environments (Calima/Storm/Sun/Rain)
        WeatherAnimations(
            condition = data.condition,
            windSpeedKmh = data.windSpeedKmh,
            modifier = Modifier.matchParentSize()
        )

        // Text & metrics content overlaid safely with contrast shadow boxes
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(22.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
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
                WeatherMetricItem(iconName = "wind", label = "Dir. Viento", valStr = "${getWindDirectionLabel(data.windDirectionDegrees)} (${data.windDirectionDegrees.toInt()}°)", textColor = contentColor)
                val elevationStr = data.elevation?.let { "${it.toInt()}m" } ?: "N/D"
                WeatherMetricItem(iconName = "gps", label = "Altitud", valStr = elevationStr, textColor = contentColor)
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
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            ClimaIcon(
                name = iconName,
                tint = textColor.copy(alpha = 0.6f),
                modifier = Modifier.size(16.dp)
            )
            Text(text = valStr, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = textColor)
        }
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = textColor.copy(alpha = 0.75f)
        )
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
                        "${(item.maxTemp * 9/5 + 32).toInt()}F"
                    }
                    val tempMinLabel = if (isCelsius) {
                        "${item.minTemp.toInt()}°"
                    } else {
                        "${(item.minTemp * 9/5 + 32).toInt()}F"
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

private fun getIslandForCity(cityName: String, lat: Double, lon: Double): String {
    // Basic coordinate boxing for Canary Islands
    if (lon in -15.82..-15.35 && lat in 27.72..28.18) return "Gran Canaria"
    if (lon in -16.93..-16.11 && lat in 27.98..28.59) return "Tenerife"
    if (lon in -14.52..-13.82 && lat in 28.02..28.75) return "Fuerteventura"
    if (lon in -13.88..-13.33 && lat in 28.82..29.28) return "Lanzarote"
    if (lon in -18.00..-17.72 && lat in 28.43..28.85) return "La Palma"
    if (lon in -17.35..-17.09 && lat in 28.01..28.23) return "La Gomera"
    if (lon in -18.17..-17.88 && lat in 27.62..27.86) return "El Hierro"
    
    // Fallback name matching
    val nameLower = cityName.lowercase()
    val islands = listOf("Gran Canaria", "Tenerife", "Fuerteventura", "Lanzarote", "La Palma", "La Gomera", "El Hierro")
    for (island in islands) {
        if (nameLower.contains(island.lowercase())) return island
    }
    // A few well-known cities
    if (nameLower.contains("palmas")) return "Gran Canaria"
    if (nameLower.contains("cruz") || nameLower.contains("laguna")) return "Tenerife"
    return ""
}

private fun isWarningActive(warning: com.example.data.AemetWarningDomainData): Boolean {
    val inicio = warning.fechaInicio
    val fin = warning.fechaFin
    if (inicio == null || fin == null) return true // Cannot determine exact time, keep it
    return try {
        val sdf = SimpleDateFormat("HH:mm dd-MM-yyyy", Locale.getDefault())
        val startDate = sdf.parse(inicio)
        val endDate = sdf.parse(fin)
        val now = Date()
        (startDate != null && endDate != null && !now.before(startDate) && !now.after(endDate))
    } catch (e: Exception) {
        true
    }
}
fun getWindDirectionCode(degrees: Double?): String {
    if (degrees == null) return "-"
    val normalized = ((degrees % 360) + 360) % 360
    return when (normalized) {
        in 337.5..360.0, in 0.0..22.5 -> "N"
        in 22.5..67.5 -> "NE"
        in 67.5..112.5 -> "E"
        in 112.5..157.5 -> "SE"
        in 157.5..202.5 -> "S"
        in 202.5..247.5 -> "SO"
        in 247.5..292.5 -> "O"
        in 292.5..337.5 -> "NO"
        else -> "-"
    }
}
