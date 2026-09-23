package com.example.ui.screens

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.draw.clip
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.service.AllergyNotificationWorker
import com.example.viewmodel.WeatherViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.Scope

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: WeatherViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler {
        onNavigateBack()
    }

    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current

    val isDarkTheme by viewModel.isDarkTheme.collectAsStateWithLifecycle()
    val isAmoledTheme by viewModel.isAmoledTheme.collectAsStateWithLifecycle()
    val isAutoDarkMode by viewModel.isAutoDarkMode.collectAsStateWithLifecycle()
    val isCelsius by viewModel.isCelsius.collectAsStateWithLifecycle()
    val selectedIslands by viewModel.selectedIslands.collectAsStateWithLifecycle()
    val allergySettings by viewModel.settingsManager.settings.collectAsStateWithLifecycle()

    val userProfile by viewModel.cloudSync.userProfile.collectAsStateWithLifecycle()
    val isSyncing by viewModel.cloudSync.isSyncing.collectAsStateWithLifecycle()

    val primaryCanaryYellow = Color(0xFFFFD600)
    val primaryCanaryBlue = Color(0xFF004993)
    val accentColor = if (isDarkTheme) primaryCanaryYellow else primaryCanaryBlue

    val cardBackground = if (isDarkTheme) {
        if (isAmoledTheme) Color(0xFF0D0D0D) else Color(0xFF1E1C24)
    } else {
        Color.White
    }

    val screenBackground = if (isDarkTheme) {
        if (isAmoledTheme) Color.Black else Color(0xFF141318)
    } else {
        Color(0xFFF4F6F9)
    }

    val onSurfaceColor = if (isDarkTheme) Color.White else Color(0xFF1C1B1F)

    val islasCanarias = remember {
        listOf(
            "El Hierro",
            "Fuerteventura",
            "Gran Canaria",
            "La Gomera",
            "La Palma",
            "Lanzarote",
            "Tenerife"
        )
    }

    // Google Sign-In launcher (reserved for future cloud sync activation)
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

    val toggleAllergy = { key: String, isChecked: Boolean ->
        val current = allergySettings
        val newSettings = when (key) {
            "grass" -> current.copy(allergyGrass = isChecked)
            "olive" -> current.copy(allergyOlive = isChecked)
            "mugwort" -> current.copy(allergyMugwort = isChecked)
            "alder" -> current.copy(allergyAlder = isChecked)
            "birch" -> current.copy(allergyBirch = isChecked)
            "ragweed" -> current.copy(allergyRagweed = isChecked)
            "dust" -> current.copy(sensitiveToDust = isChecked)
            else -> current
        }
        viewModel.settingsManager.saveSettings(newSettings)
        val hasAnyActive = newSettings.allergyGrass || newSettings.allergyOlive ||
                newSettings.allergyMugwort || newSettings.allergyAlder ||
                newSettings.allergyBirch || newSettings.allergyRagweed ||
                newSettings.sensitiveToDust
        if (hasAnyActive) {
            AllergyNotificationWorker.schedule(context)
        } else {
            AllergyNotificationWorker.cancel(context)
        }
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .testTag("settings_screen"),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Ajustes",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = Color.White
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("settings_back_btn")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = if (isDarkTheme) {
                        if (isAmoledTheme) Color.Black else Color(0xFF141318)
                    } else {
                        primaryCanaryBlue
                    },
                    titleContentColor = Color.White
                )
            )
        },
        containerColor = screenBackground
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // SECCIÓN 1: AVISOS OFICIALES AEMET
            SettingsCard(
                cardBackground = cardBackground,
                onSurfaceColor = onSurfaceColor
            ) {
                SettingsSectionHeader(
                    icon = Icons.Default.Notifications,
                    title = "Avisos oficiales (AEMET)",
                    accentColor = accentColor
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Selecciona tus islas preferidas para recibir alertas meteorológicas al abrir la aplicación:",
                    fontSize = 13.sp,
                    color = Color.Gray,
                    lineHeight = 18.sp
                )
                Spacer(modifier = Modifier.height(10.dp))
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    islasCanarias.forEach { island ->
                        val isChecked = selectedIslands.contains(island)
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { viewModel.toggleIslandSelection(island) }
                                .padding(vertical = 4.dp, horizontal = 4.dp)
                        ) {
                            Checkbox(
                                checked = isChecked,
                                onCheckedChange = { viewModel.toggleIslandSelection(island) },
                                colors = CheckboxDefaults.colors(
                                    checkedColor = accentColor
                                )
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = island,
                                fontSize = 15.sp,
                                fontWeight = if (isChecked) FontWeight.SemiBold else FontWeight.Normal,
                                color = onSurfaceColor
                            )
                        }
                    }
                }
            }

            // SECCIÓN 2: ALERGIAS Y POLEN
            SettingsCard(
                cardBackground = cardBackground,
                onSurfaceColor = onSurfaceColor
            ) {
                SettingsSectionHeader(
                    icon = Icons.Default.Air,
                    title = "Alergias y Calidad del Aire",
                    accentColor = accentColor
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Activa los alérgenos a los que eres sensible para mostrar avisos prioritarios en la tarjeta de Calidad del Aire:",
                    fontSize = 13.sp,
                    color = Color.Gray,
                    lineHeight = 18.sp
                )
                Spacer(modifier = Modifier.height(12.dp))

                SettingsSwitchRow(
                    title = "Gramíneas",
                    subtitle = "Aviso si la concentración de polen de gramíneas es alta.",
                    checked = allergySettings.allergyGrass,
                    onCheckedChange = { toggleAllergy("grass", it) },
                    accentColor = accentColor,
                    isDarkTheme = isDarkTheme,
                    onSurfaceColor = onSurfaceColor
                )

                HorizontalDivider(
                    color = if (isDarkTheme) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.06f),
                    modifier = Modifier.padding(vertical = 8.dp)
                )

                SettingsSwitchRow(
                    title = "Olivo",
                    subtitle = "Vigilancia de polen de olivo.",
                    checked = allergySettings.allergyOlive,
                    onCheckedChange = { toggleAllergy("olive", it) },
                    accentColor = accentColor,
                    isDarkTheme = isDarkTheme,
                    onSurfaceColor = onSurfaceColor
                )

                HorizontalDivider(
                    color = if (isDarkTheme) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.06f),
                    modifier = Modifier.padding(vertical = 8.dp)
                )

                SettingsSwitchRow(
                    title = "Artemisa / Maleza",
                    subtitle = "Detección de pólenes de malezas y artemisa.",
                    checked = allergySettings.allergyMugwort,
                    onCheckedChange = { toggleAllergy("mugwort", it) },
                    accentColor = accentColor,
                    isDarkTheme = isDarkTheme,
                    onSurfaceColor = onSurfaceColor
                )

                HorizontalDivider(
                    color = if (isDarkTheme) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.06f),
                    modifier = Modifier.padding(vertical = 8.dp)
                )

                SettingsSwitchRow(
                    title = "Aliso",
                    subtitle = "Vigilancia de polen de aliso (Betulaceae / Alnus).",
                    checked = allergySettings.allergyAlder,
                    onCheckedChange = { toggleAllergy("alder", it) },
                    accentColor = accentColor,
                    isDarkTheme = isDarkTheme,
                    onSurfaceColor = onSurfaceColor
                )

                HorizontalDivider(
                    color = if (isDarkTheme) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.06f),
                    modifier = Modifier.padding(vertical = 8.dp)
                )

                SettingsSwitchRow(
                    title = "Abedul",
                    subtitle = "Detección de polen de abedul (Betula).",
                    checked = allergySettings.allergyBirch,
                    onCheckedChange = { toggleAllergy("birch", it) },
                    accentColor = accentColor,
                    isDarkTheme = isDarkTheme,
                    onSurfaceColor = onSurfaceColor
                )

                HorizontalDivider(
                    color = if (isDarkTheme) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.06f),
                    modifier = Modifier.padding(vertical = 8.dp)
                )

                SettingsSwitchRow(
                    title = "Ambrosía",
                    subtitle = "Vigilancia de polen de ambrosía (Asteraceae).",
                    checked = allergySettings.allergyRagweed,
                    onCheckedChange = { toggleAllergy("ragweed", it) },
                    accentColor = accentColor,
                    isDarkTheme = isDarkTheme,
                    onSurfaceColor = onSurfaceColor
                )

                HorizontalDivider(
                    color = if (isDarkTheme) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.06f),
                    modifier = Modifier.padding(vertical = 8.dp)
                )

                SettingsSwitchRow(
                    title = "Polvo / Calima",
                    subtitle = "Sensibilidad adicional a partículas en suspensión PM10 y PM2.5.",
                    checked = allergySettings.sensitiveToDust,
                    onCheckedChange = { toggleAllergy("dust", it) },
                    accentColor = accentColor,
                    isDarkTheme = isDarkTheme,
                    onSurfaceColor = onSurfaceColor
                )

                HorizontalDivider(
                    color = if (isDarkTheme) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.06f),
                    modifier = Modifier.padding(vertical = 8.dp)
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isDarkTheme) Color.White.copy(alpha = 0.05f) else Color.Black.copy(alpha = 0.03f))
                        .padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.HealthAndSafety,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "Umbrales según la Red Española de Aerobiología (REA) y vigilancia canaria. Con alérgenos activos, se programan comprobaciones periódicas automáticas.",
                        fontSize = 11.sp,
                        color = onSurfaceColor.copy(alpha = 0.7f),
                        lineHeight = 15.sp
                    )
                }
            }

            // SECCIÓN 3: APARIENCIA
            SettingsCard(
                cardBackground = cardBackground,
                onSurfaceColor = onSurfaceColor
            ) {
                SettingsSectionHeader(
                    icon = Icons.Default.Palette,
                    title = "Apariencia",
                    accentColor = accentColor
                )
                Spacer(modifier = Modifier.height(12.dp))

                SettingsSwitchRow(
                    title = "Modo noche",
                    subtitle = "Alternar manualmente entre tema claro y tema oscuro.",
                    checked = isDarkTheme,
                    onCheckedChange = { viewModel.toggleTheme() },
                    accentColor = accentColor,
                    isDarkTheme = isDarkTheme,
                    onSurfaceColor = onSurfaceColor
                )

                HorizontalDivider(
                    color = if (isDarkTheme) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.06f),
                    modifier = Modifier.padding(vertical = 8.dp)
                )

                SettingsSwitchRow(
                    title = "Modo noche automático",
                    subtitle = "Activar modo oscuro al anochecer y desactivar al amanecer.",
                    checked = isAutoDarkMode,
                    onCheckedChange = { viewModel.toggleAutoDarkMode() },
                    accentColor = accentColor,
                    isDarkTheme = isDarkTheme,
                    onSurfaceColor = onSurfaceColor
                )

                HorizontalDivider(
                    color = if (isDarkTheme) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.06f),
                    modifier = Modifier.padding(vertical = 8.dp)
                )

                SettingsSwitchRow(
                    title = "Tema AMOLED (Negro puro)",
                    subtitle = "Fondos 100% negros para optimizar batería en pantallas OLED.",
                    checked = isAmoledTheme,
                    onCheckedChange = { viewModel.toggleAmoledTheme() },
                    accentColor = accentColor,
                    isDarkTheme = isDarkTheme,
                    onSurfaceColor = onSurfaceColor
                )

                HorizontalDivider(
                    color = if (isDarkTheme) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.06f),
                    modifier = Modifier.padding(vertical = 8.dp)
                )

                SettingsSwitchRow(
                    title = "Unidad de temperatura (°C / F)",
                    subtitle = if (isCelsius) "Mostrando temperaturas en grados Celsius (°C)." else "Mostrando temperaturas en Fahrenheit (F).",
                    checked = !isCelsius,
                    onCheckedChange = { viewModel.toggleTemperatureUnit() },
                    accentColor = accentColor,
                    isDarkTheme = isDarkTheme,
                    onSurfaceColor = onSurfaceColor
                )
            }

            // SECCIÓN 4: CUENTA Y SINCRONIZACIÓN (Oculta para habilitar en versión posterior)
            val isSyncFeatureEnabled = false
            if (isSyncFeatureEnabled) {
                SettingsCard(
                    cardBackground = cardBackground,
                    onSurfaceColor = onSurfaceColor
                ) {
                    SettingsSectionHeader(
                        icon = Icons.Default.CloudSync,
                        title = "Cuenta de Google y Sincronización",
                        accentColor = accentColor
                    )
                    Spacer(modifier = Modifier.height(12.dp))

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

                    Spacer(modifier = Modifier.height(12.dp))

                    if (userProfile == null) {
                        Button(
                            onClick = {
                                val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                                    .requestEmail()
                                    .requestScopes(Scope("https://www.googleapis.com/auth/drive.appdata"))
                                    .build()
                                val googleSignInClient = GoogleSignIn.getClient(context, gso)
                                googleSignInLauncher.launch(googleSignInClient.signInIntent)
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Conectar con Google")
                        }
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Button(
                                    onClick = { viewModel.triggerSaveToCloud() },
                                    modifier = Modifier.weight(1f),
                                    enabled = !isSyncing
                                ) {
                                    Text("Guardar")
                                }

                                Button(
                                    onClick = { viewModel.triggerRestoreFromCloud() },
                                    modifier = Modifier.weight(1f),
                                    enabled = !isSyncing
                                ) {
                                    Text("Restaurar")
                                }
                            }
                            OutlinedButton(
                                onClick = { viewModel.cloudSync.logout() },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Cerrar Sesión", color = Color.Red)
                            }
                        }
                    }
                }
            }

            // SECCIÓN 5: INFORMACIÓN Y LEGAL
            SettingsCard(
                cardBackground = cardBackground,
                onSurfaceColor = onSurfaceColor
            ) {
                SettingsSectionHeader(
                    icon = Icons.Default.Info,
                    title = "Información y Legal",
                    accentColor = accentColor
                )
                Spacer(modifier = Modifier.height(12.dp))

                val privacyUrl = stringResource(id = R.string.privacy_policy_url)
                val disclaimerText = stringResource(id = R.string.disclaimer_text)
                val appVersion = stringResource(id = R.string.app_version)

                Text(
                    text = "Política de Privacidad",
                    color = Color(0xFF29B6F6),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    textDecoration = TextDecoration.Underline,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .clickable { uriHandler.openUri(privacyUrl) }
                        .padding(vertical = 6.dp)
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = disclaimerText,
                    fontSize = 12.sp,
                    color = Color.Gray,
                    lineHeight = 16.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "ClimaCanarias v$appVersion",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = onSurfaceColor.copy(alpha = 0.7f)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Desarrollado por ",
                            fontSize = 12.sp,
                            color = onSurfaceColor.copy(alpha = 0.5f)
                        )
                        Text(
                            text = "Aitor Santana",
                            fontSize = 12.sp,
                            color = Color(0xFF29B6F6),
                            textDecoration = TextDecoration.Underline,
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .clickable {
                                    uriHandler.openUri("https://github.com/AitorGC")
                                }
                                .padding(horizontal = 2.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun SettingsCard(
    cardBackground: Color,
    onSurfaceColor: Color,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = cardBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            content = content
        )
    }
}

@Composable
private fun SettingsSectionHeader(
    icon: ImageVector,
    title: String,
    accentColor: Color
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = accentColor,
            modifier = Modifier.size(22.dp)
        )
        Text(
            text = title,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            color = accentColor
        )
    }
}

@Composable
private fun SettingsSwitchRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    accentColor: Color,
    isDarkTheme: Boolean,
    onSurfaceColor: Color
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
            Text(
                text = title,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = onSurfaceColor
            )
            if (subtitle.isNotEmpty()) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    fontSize = 12.sp,
                    color = Color.Gray,
                    lineHeight = 16.sp
                )
            }
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = accentColor,
                uncheckedThumbColor = Color.Gray,
                uncheckedTrackColor = if (isDarkTheme) Color(0xFF333333) else Color(0xFFE0E0E0)
            )
        )
    }
}
