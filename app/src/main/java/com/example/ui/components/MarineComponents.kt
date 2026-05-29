package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Beach
import com.example.viewmodel.MarineUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BeachSelectionDropdown(
    beaches: List<Beach>,
    selectedBeach: Beach?,
    onBeachSelected: (Beach) -> Unit
) {
    var selectedProvince by remember { mutableStateOf(selectedBeach?.province) }
    var selectedIsland by remember { mutableStateOf(selectedBeach?.island) }
    var selectedMunicipality by remember { mutableStateOf(selectedBeach?.municipality) }

    LaunchedEffect(selectedBeach) {
        if (selectedBeach != null) {
            selectedProvince = selectedBeach.province
            selectedIsland = selectedBeach.island
            selectedMunicipality = selectedBeach.municipality
        }
    }

    val provinces = remember(beaches) { beaches.map { it.province }.distinct().sorted() }
    val islands = remember(beaches, selectedProvince) {
        if (selectedProvince == null) emptyList()
        else beaches.filter { it.province == selectedProvince }.map { it.island }.distinct().sorted()
    }
    val municipalities = remember(beaches, selectedIsland) {
        if (selectedIsland == null) emptyList()
        else beaches.filter { it.island == selectedIsland }.map { it.municipality }.distinct().sorted()
    }
    val filteredBeaches = remember(beaches, selectedIsland, selectedMunicipality) {
        if (selectedMunicipality == null) emptyList()
        else beaches.filter { it.island == selectedIsland && it.municipality == selectedMunicipality }.sortedBy { it.name }
    }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        // Province Selector
        GenericDropdownSelector(
            label = "1. Provincia",
            options = provinces,
            selectedOption = selectedProvince,
            onOptionSelected = {
                selectedProvince = it
                selectedIsland = null
                selectedMunicipality = null
            }
        )

        // Island Selector
        GenericDropdownSelector(
            label = "2. Isla",
            options = islands,
            selectedOption = selectedIsland,
            enabled = islands.isNotEmpty(),
            onOptionSelected = {
                selectedIsland = it
                selectedMunicipality = null
            }
        )

        // Municipality Selector
        GenericDropdownSelector(
            label = "3. Municipio",
            options = municipalities,
            selectedOption = selectedMunicipality,
            enabled = municipalities.isNotEmpty(),
            onOptionSelected = {
                selectedMunicipality = it
            }
        )

        // Beach Selector
        BeachDropdown(
            label = "4. Playa o ZBM",
            beaches = filteredBeaches,
            selectedBeach = selectedBeach,
            enabled = filteredBeaches.isNotEmpty(),
            onBeachSelected = onBeachSelected
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GenericDropdownSelector(
    label: String,
    options: List<String>,
    selectedOption: String?,
    enabled: Boolean = true,
    onOptionSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded && enabled,
        onExpandedChange = { if (enabled) expanded = !expanded },
        modifier = Modifier.fillMaxWidth()
    ) {
        val display = selectedOption ?: "Seleccionar..."
        OutlinedTextField(
            value = display,
            onValueChange = {},
            readOnly = true,
            enabled = enabled,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(
                unfocusedBorderColor = Color.LightGray,
                focusedBorderColor = MaterialTheme.colorScheme.primary
            ),
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onOptionSelected(option)
                        expanded = false
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BeachDropdown(
    label: String,
    beaches: List<Beach>,
    selectedBeach: Beach?,
    enabled: Boolean,
    onBeachSelected: (Beach) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded && enabled,
        onExpandedChange = { if (enabled) expanded = !expanded },
        modifier = Modifier.fillMaxWidth()
    ) {
        val display = selectedBeach?.name ?: "Seleccionar playa..."
        OutlinedTextField(
            value = display,
            onValueChange = {},
            readOnly = true,
            enabled = enabled,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(
                unfocusedBorderColor = Color.LightGray,
                focusedBorderColor = MaterialTheme.colorScheme.primary
            ),
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            beaches.forEach { beach ->
                DropdownMenuItem(
                    text = { Text(beach.name, maxLines = 1) },
                    onClick = {
                        onBeachSelected(beach)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun MarineWeatherScreenMode(
    marineUiState: MarineUiState,
    isDarkTheme: Boolean,
    primaryCanaryYellow: Color,
    cardBackgroundColor: Color,
    onSurfaceColor: Color
) {
    when (marineUiState) {
        is MarineUiState.Idle -> {
            Box(Modifier.fillMaxWidth().height(180.dp), contentAlignment = Alignment.Center) {
                 Text("Selecciona una playa para ver su estado marítimo.", color = Color.Gray)
            }
        }
        is MarineUiState.Loading -> {
            Box(Modifier.fillMaxWidth().height(180.dp), contentAlignment = Alignment.Center) {
                 CircularProgressIndicator(color = primaryCanaryYellow)
            }
        }
        is MarineUiState.Error -> {
            Box(Modifier.fillMaxWidth().height(180.dp), contentAlignment = Alignment.Center) {
                 Text(marineUiState.message, color = Color.Red)
            }
        }
        is MarineUiState.Success -> {
            val hourly = marineUiState.data.hourly
            val time = hourly?.time?.firstOrNull() ?: ""
            val waveHeight = hourly?.waveHeight?.firstOrNull() ?: 0.0
            val wavePeriod = hourly?.wavePeriod?.firstOrNull() ?: 0.0
            val waveDirection = hourly?.waveDirection?.firstOrNull() ?: 0.0
            val windWaveDir = hourly?.windWaveDirection?.firstOrNull() ?: 0.0
            val windWaveHeight = hourly?.windWaveHeight?.firstOrNull() ?: 0.0
            
            // Logic for Bandera (Flag)
            val flagState = getBeachFlag(waveHeight)

            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                // Flag Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = cardBackgroundColor),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.25f))
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(flagState.color),
                            contentAlignment = Alignment.Center
                        ) {
                            if (flagState.name == "ROJA") {
                                Icon(Icons.Default.Warning, contentDescription = null, tint = Color.White)
                            }
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text("Bandera de la Playa", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = onSurfaceColor)
                            Text(
                               text = flagState.description, 
                               color = if (isDarkTheme) Color.LightGray else Color.DarkGray, 
                               fontSize = 12.sp
                            )
                        }
                    }
                }

                // Grid Stats Section 1
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    MarineStatCard(
                        title = "Altura Ola",
                        value = "${waveHeight}m",
                        modifier = Modifier.weight(1f),
                        cardBackgroundColor = cardBackgroundColor,
                        onSurfaceColor = onSurfaceColor,
                        isDarkTheme = isDarkTheme
                    )
                    MarineStatCard(
                        title = "Periodo Ola",
                        value = "${wavePeriod}s",
                        modifier = Modifier.weight(1f),
                        cardBackgroundColor = cardBackgroundColor,
                        onSurfaceColor = onSurfaceColor,
                        isDarkTheme = isDarkTheme
                    )
                    MarineStatCard(
                        title = "Dir. Ola",
                        value = "${waveDirection}°",
                        modifier = Modifier.weight(1f),
                        cardBackgroundColor = cardBackgroundColor,
                        onSurfaceColor = onSurfaceColor,
                        isDarkTheme = isDarkTheme
                    )
                }

                // Grid Stats Section 2
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    MarineStatCard(
                        title = "Ola de Viento",
                        value = "${windWaveHeight}m",
                        modifier = Modifier.weight(1f),
                        cardBackgroundColor = cardBackgroundColor,
                        onSurfaceColor = onSurfaceColor,
                        isDarkTheme = isDarkTheme
                    )
                    MarineStatCard(
                        title = "Dir. Viento",
                        value = "${windWaveDir}°",
                        modifier = Modifier.weight(1f),
                        cardBackgroundColor = cardBackgroundColor,
                        onSurfaceColor = onSurfaceColor,
                        isDarkTheme = isDarkTheme
                    )
                }

                // Tides Graph / List Section
                if (marineUiState.tides.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Pleamar y Bajamar (IHM)", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = onSurfaceColor)
                    Row(
                        modifier = Modifier.fillMaxWidth().horizontalScroll(androidx.compose.foundation.rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        marineUiState.tides.forEach { tide ->
                            val isHigh = tide.type.equals("pleamar", ignoreCase = true)
                            Card(
                                modifier = Modifier.width(100.dp),
                                shape = RoundedCornerShape(18.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isHigh) Color(0xFFE3F2FD) else Color(0xFFFFF3E0).copy(alpha = if (isDarkTheme) 0.1f else 1f)
                                ),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.25f))
                            ) {
                                Column(
                                    modifier = Modifier.padding(12.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = if (isHigh) "Pleamar" else "Bajamar",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = if (isHigh) Color(0xFF1565C0) else Color(0xFFEF6C00)
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(text = tide.time, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = onSurfaceColor)
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(text = "${tide.height}m", fontSize = 14.sp, color = if (isDarkTheme) Color.LightGray else Color.Gray)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MarineStatCard(
    title: String,
    value: String,
    modifier: Modifier,
    cardBackgroundColor: Color,
    onSurfaceColor: Color,
    isDarkTheme: Boolean
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = cardBackgroundColor),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.25f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(title, fontSize = 11.sp, color = if (isDarkTheme) Color.LightGray else Color.Gray)
            Spacer(Modifier.height(4.dp))
            Text(value, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = onSurfaceColor)
        }
    }
}

data class FlagState(val name: String, val color: Color, val description: String)
fun getBeachFlag(waveHeight: Double): FlagState {
    return when {
        waveHeight >= 1.5 -> FlagState("ROJA", Color(0xFFD32F2F), "Prohibido el baño. Fuerte oleaje.")
        waveHeight >= 0.8 -> FlagState("AMARILLA", Color(0xFFFBC02D), "Precaución. Oleaje o corrientes.")
        else -> FlagState("VERDE", Color(0xFF388E3C), "Baño libre. Buenas condiciones.")
    }
}
