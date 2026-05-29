package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.foundation.clickable
import androidx.compose.animation.animateContentSize
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Accessible
import androidx.compose.material.icons.filled.Shower
import androidx.compose.material.icons.filled.LocalParking
import androidx.compose.material.icons.filled.Tour
import androidx.compose.material.icons.filled.Landscape
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material.icons.filled.Wc
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.BeachAccess
import androidx.compose.material.icons.filled.Chair
import androidx.compose.material.icons.filled.Sailing
import androidx.compose.material.icons.filled.ChildCare
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Terrain
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.Waves
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.drawText
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
    selectedBeach: Beach?,
    isDarkTheme: Boolean,
    primaryCanaryYellow: Color,
    cardBackgroundColor: Color,
    onSurfaceColor: Color
) {
    if (selectedBeach != null) {
        BeachDetailsCard(
            beach = selectedBeach,
            isDarkTheme = isDarkTheme,
            cardBackgroundColor = cardBackgroundColor,
            onSurfaceColor = onSurfaceColor
        )
        Spacer(modifier = Modifier.height(16.dp))
    }

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
                    Spacer(modifier = Modifier.height(16.dp))
                    TideGraphCard(
                        tides = marineUiState.tides,
                        isDarkTheme = isDarkTheme,
                        onSurfaceColor = onSurfaceColor,
                        sunrise = marineUiState.sunrise,
                        sunset = marineUiState.sunset
                    )
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

@Composable
fun TideGraphCard(
    tides: List<com.example.data.TideInfo>,
    isDarkTheme: Boolean,
    onSurfaceColor: Color,
    sunrise: String?,
    sunset: String?
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isDarkTheme) Color(0xFF1E1E1E) else Color(0xFFFFFFFF)
        ),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Sunrise / Sunset Header
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Sunrise
                Row(verticalAlignment = Alignment.CenterVertically) {
                    androidx.compose.material3.Icon(
                        imageVector = Icons.Filled.WbSunny,
                        contentDescription = "Amanecer",
                        modifier = Modifier.size(32.dp),
                        tint = Color(0xFFFBC02D)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = sunrise ?: "--:--",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = onSurfaceColor
                        )
                        Text(
                            text = "Amanecer",
                            fontSize = 12.sp,
                            color = if (isDarkTheme) Color.LightGray else Color.Gray
                        )
                    }
                }

                // Sunset
                Row(verticalAlignment = Alignment.CenterVertically) {
                    androidx.compose.material3.Icon(
                        imageVector = Icons.Filled.NightsStay,
                        contentDescription = "Atardecer",
                        modifier = Modifier.size(32.dp),
                        tint = Color(0xFFFFA000)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = sunset ?: "--:--",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = onSurfaceColor
                        )
                        Text(
                            text = "Atardecer",
                            fontSize = 12.sp,
                            color = if (isDarkTheme) Color.LightGray else Color.Gray
                        )
                    }
                }
            }

            Text(
                "Mareas", 
                fontWeight = FontWeight.Bold, 
                fontSize = 14.sp, 
                color = onSurfaceColor
            )
            Spacer(modifier = Modifier.height(16.dp))
            TideGraphCanvas(
                tides = tides,
                isDarkTheme = isDarkTheme
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start
            ) {
                Text(
                    text = "Fuente: IHM",
                    fontSize = 12.sp,
                    color = if (isDarkTheme) Color.LightGray else Color.Gray
                )
            }
        }
    }
}

@Composable
fun TideGraphCanvas(
    tides: List<com.example.data.TideInfo>,
    isDarkTheme: Boolean,
    modifier: Modifier = Modifier
) {
    if (tides.isEmpty()) return

    val maxTide = (tides.maxOfOrNull { it.height } ?: 3.0) + 0.5
    val minTide = 0.0

    val textColor = if (isDarkTheme) Color.LightGray else Color.Gray
    val gridColor = if (isDarkTheme) Color.DarkGray else Color.LightGray.copy(alpha = 0.5f)
    val lineColor = Color(0xFF1976D2)

    val textMeasurer = androidx.compose.ui.text.rememberTextMeasurer()
    val textStyle = androidx.compose.ui.text.TextStyle(color = textColor, fontSize = 12.sp)

    androidx.compose.foundation.Canvas(modifier = modifier.fillMaxWidth().height(200.dp)) {
        val width = size.width
        val height = size.height

        val paddingLeft = 40.dp.toPx()
        val paddingBottom = 40.dp.toPx()
        val paddingTop = 10.dp.toPx()
        val paddingRight = 10.dp.toPx()

        val graphWidth = width - paddingLeft - paddingRight
        val graphHeight = height - paddingTop - paddingBottom
        
        // Draw grid and Y labels
        val ySteps = 5
        for (i in 0..ySteps) {
            val yVal = maxTide - (maxTide - minTide) * (i.toFloat() / ySteps)
            val yPos = paddingTop + (i.toFloat() / ySteps) * graphHeight
            
            drawLine(
                color = gridColor,
                start = androidx.compose.ui.geometry.Offset(paddingLeft, yPos),
                end = androidx.compose.ui.geometry.Offset(width - paddingRight, yPos),
                strokeWidth = 1.dp.toPx()
            )
            
            val formattedVal = String.format(java.util.Locale.US, "%.1f", yVal).replace(".", ",")
            drawText(
                textMeasurer = textMeasurer,
                text = formattedVal,
                style = textStyle,
                topLeft = androidx.compose.ui.geometry.Offset(10.dp.toPx(), yPos - 8.dp.toPx())
            )
        }
        
        // Draw Y axis label
        drawContext.canvas.save()
        drawContext.canvas.translate(0f, height / 2)
        drawContext.canvas.rotate(-90f)
        drawText(
            textMeasurer = textMeasurer,
            text = "Altura (m)",
            style = textStyle,
            topLeft = androidx.compose.ui.geometry.Offset(-25.dp.toPx(), -20.dp.toPx())
        )
        drawContext.canvas.restore()

        // X labels and points
        val pointDistance = if (tides.size > 1) graphWidth / (tides.size - 1) else graphWidth / 2
        val pointsToDraw = mutableListOf<androidx.compose.ui.geometry.Offset>()
        
        tides.forEachIndexed { index, tide ->
            val xPos = paddingLeft + (if (tides.size > 1) index * pointDistance else pointDistance)
            val yNormalized = ((tide.height - minTide) / (maxTide - minTide)).toFloat()
            val yPos = height - paddingBottom - yNormalized * graphHeight
            
            pointsToDraw.add(androidx.compose.ui.geometry.Offset(xPos, yPos))
            
            // X Label (Time)
            val timeTextSize = textMeasurer.measure(tide.time, textStyle).size
            drawText(
                textMeasurer = textMeasurer,
                text = tide.time,
                style = textStyle,
                topLeft = androidx.compose.ui.geometry.Offset(xPos - timeTextSize.width / 2, height - paddingBottom + 10.dp.toPx())
            )
        }
        
        // X axis label
        val xLabel = "Hora UTC"
        val labelSize = textMeasurer.measure(xLabel, textStyle).size
        drawText(
            textMeasurer = textMeasurer,
            text = xLabel,
            style = textStyle,
            topLeft = androidx.compose.ui.geometry.Offset(paddingLeft + graphWidth / 2 - labelSize.width / 2, height - 15.dp.toPx())
        )

        // Draw Line
        if (pointsToDraw.size > 1) {
            val path = androidx.compose.ui.graphics.Path()
            path.moveTo(pointsToDraw.first().x, pointsToDraw.first().y)

            for (i in 0 until pointsToDraw.size - 1) {
                // Bezier curve for smoothness
                val p1 = pointsToDraw[i]
                val p2 = pointsToDraw[i + 1]
                val controlX = (p1.x + p2.x) / 2
                path.cubicTo(controlX, p1.y, controlX, p2.y, p2.x, p2.y)
            }
            
            drawPath(
                path = path,
                color = lineColor,
                style = androidx.compose.ui.graphics.drawscope.Stroke(
                    width = 3.dp.toPx(),
                    cap = androidx.compose.ui.graphics.StrokeCap.Round,
                    join = androidx.compose.ui.graphics.StrokeJoin.Round
                )
            )
        }
        
        // Draw Points
        pointsToDraw.forEach { point ->
            drawCircle(
                color = Color.White,
                radius = 4.dp.toPx(),
                center = point
            )
            drawCircle(
                color = lineColor,
                radius = 4.dp.toPx(),
                center = point,
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2.dp.toPx())
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun BeachDetailsCard(
    beach: com.example.data.Beach,
    isDarkTheme: Boolean,
    cardBackgroundColor: Color,
    onSurfaceColor: Color
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded }
            .animateContentSize(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = cardBackgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isDarkTheme) 0.dp else 2.dp),
        border = if (isDarkTheme) androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.1f)) else null
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = beach.name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = onSurfaceColor
                    )
                    Text(
                        text = beach.municipality,
                        fontSize = 14.sp,
                        color = if (isDarkTheme) Color.LightGray else Color.DarkGray
                    )
                }
                Icon(
                    imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = if (expanded) "Colapsar" else "Expandir",
                    tint = onSurfaceColor
                )
            }

            if (expanded) {
                Spacer(modifier = Modifier.height(16.dp))
                
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (beach.banderaAzul) {
                        AssistChip(onClick = {}, label = { Text("Bandera Azul") }, leadingIcon = { Icon(Icons.Default.Tour, contentDescription = null, tint = Color(0xFF1E88E5)) }, colors = AssistChipDefaults.assistChipColors(labelColor = onSurfaceColor))
                    }
                    if (beach.riesgo.isNotEmpty() && beach.riesgo != "No evaluado") {
                        AssistChip(onClick = {}, label = { Text("Riesgo: ${beach.riesgo}") }, leadingIcon = { Icon(Icons.Default.WarningAmber, contentDescription = null) }, colors = AssistChipDefaults.assistChipColors(labelColor = onSurfaceColor))
                    }
                    if (beach.duchas) {
                        AssistChip(onClick = {}, label = { Text("Duchas") }, leadingIcon = { Icon(Icons.Default.Shower, contentDescription = null, tint = Color(0xFF039BE5)) }, colors = AssistChipDefaults.assistChipColors(labelColor = onSurfaceColor))
                    }
                    if (beach.duchaAdaptada) {
                        AssistChip(onClick = {}, label = { Text("Ducha adaptada") }, leadingIcon = { Icon(Icons.Default.Accessible, contentDescription = null, tint = Color(0xFF039BE5)) }, colors = AssistChipDefaults.assistChipColors(labelColor = onSurfaceColor))
                    }
                    if (beach.aseos) {
                        AssistChip(onClick = {}, label = { Text("Aseos") }, leadingIcon = { Icon(Icons.Default.Wc, contentDescription = null) }, colors = AssistChipDefaults.assistChipColors(labelColor = onSurfaceColor))
                    }
                    if (beach.aseoAdaptado) {
                        AssistChip(onClick = {}, label = { Text("Aseo adaptado") }, leadingIcon = { Icon(Icons.Default.Accessible, contentDescription = null) }, colors = AssistChipDefaults.assistChipColors(labelColor = onSurfaceColor))
                    }
                    if (beach.banoAsistido) {
                        AssistChip(onClick = {}, label = { Text("Baño asistido") }, leadingIcon = { Icon(Icons.Default.Accessible, contentDescription = null, tint = Color(0xFF43A047)) }, colors = AssistChipDefaults.assistChipColors(labelColor = onSurfaceColor))
                    }
                    if (beach.accesoPmr) {
                        AssistChip(onClick = {}, label = { Text("Acceso PMR") }, leadingIcon = { Icon(Icons.Default.Accessible, contentDescription = null, tint = Color(0xFF43A047)) }, colors = AssistChipDefaults.assistChipColors(labelColor = onSurfaceColor))
                    }
                    if (beach.sombraPmr) {
                        AssistChip(onClick = {}, label = { Text("Sombra PMR") }, leadingIcon = { Icon(Icons.Default.Accessible, contentDescription = null) }, colors = AssistChipDefaults.assistChipColors(labelColor = onSurfaceColor))
                    }
                    if (beach.lavapies) {
                        AssistChip(onClick = {}, label = { Text("Lavapiés") }, leadingIcon = { Icon(Icons.Default.WaterDrop, contentDescription = null, tint = Color(0xFF039BE5)) }, colors = AssistChipDefaults.assistChipColors(labelColor = onSurfaceColor))
                    }
                    if (beach.aparcar) {
                        AssistChip(onClick = {}, label = { Text("Aparcamiento") }, leadingIcon = { Icon(Icons.Default.LocalParking, contentDescription = null, tint = Color(0xFF8E24AA)) }, colors = AssistChipDefaults.assistChipColors(labelColor = onSurfaceColor))
                    }
                    if (beach.alquilerSombrillas) {
                        AssistChip(onClick = {}, label = { Text("Sombrillas") }, leadingIcon = { Icon(Icons.Default.BeachAccess, contentDescription = null, tint = Color(0xFFE53935)) }, colors = AssistChipDefaults.assistChipColors(labelColor = onSurfaceColor))
                    }
                    if (beach.alquilerHamacas) {
                        AssistChip(onClick = {}, label = { Text("Hamacas") }, leadingIcon = { Icon(Icons.Default.Chair, contentDescription = null, tint = Color(0xFF43A047)) }, colors = AssistChipDefaults.assistChipColors(labelColor = onSurfaceColor))
                    }
                    if (beach.alquilerNautico) {
                        AssistChip(onClick = {}, label = { Text("Alquiler náutico") }, leadingIcon = { Icon(Icons.Default.Sailing, contentDescription = null, tint = Color(0xFF1E88E5)) }, colors = AssistChipDefaults.assistChipColors(labelColor = onSurfaceColor))
                    }
                    if (beach.areaInfantil) {
                        AssistChip(onClick = {}, label = { Text("Área Infantil") }, leadingIcon = { Icon(Icons.Default.ChildCare, contentDescription = null, tint = Color(0xFFFDD835)) }, colors = AssistChipDefaults.assistChipColors(labelColor = onSurfaceColor))
                    }
                    if (beach.areaDeportiva) {
                        AssistChip(onClick = {}, label = { Text("Área Deportiva") }, leadingIcon = { Icon(Icons.Default.FitnessCenter, contentDescription = null) }, colors = AssistChipDefaults.assistChipColors(labelColor = onSurfaceColor))
                    }

                    if (beach.tipoArena.isNotEmpty()) {
                        AssistChip(onClick = {}, label = { Text(beach.tipoArena.take(20) + if (beach.tipoArena.length > 20) "..." else "") }, leadingIcon = { Icon(Icons.Default.Landscape, contentDescription = null, tint = Color(0xFFFDD835)) }, colors = AssistChipDefaults.assistChipColors(labelColor = onSurfaceColor))
                    }
                    if (beach.color.isNotEmpty()) {
                        AssistChip(onClick = {}, label = { Text(beach.color.take(20) + if (beach.color.length > 20) "..." else "") }, leadingIcon = { Icon(Icons.Default.Palette, contentDescription = null) }, colors = AssistChipDefaults.assistChipColors(labelColor = onSurfaceColor))
                    }
                    if (beach.condicionesBano.isNotEmpty()) {
                        AssistChip(onClick = {}, label = { Text(beach.condicionesBano.take(20) + if (beach.condicionesBano.length > 20) "..." else "") }, leadingIcon = { Icon(Icons.Default.Waves, contentDescription = null, tint = Color(0xFF1E88E5)) }, colors = AssistChipDefaults.assistChipColors(labelColor = onSurfaceColor))
                    }
                    if (beach.condicionesEntorno.isNotEmpty()) {
                        AssistChip(onClick = {}, label = { Text(beach.condicionesEntorno.take(20) + if (beach.condicionesEntorno.length > 20) "..." else "") }, leadingIcon = { Icon(Icons.Default.Terrain, contentDescription = null, tint = Color(0xFF43A047)) }, colors = AssistChipDefaults.assistChipColors(labelColor = onSurfaceColor))
                    }
                    if (beach.condicionesAcceso.isNotEmpty()) {
                        AssistChip(onClick = {}, label = { Text(beach.condicionesAcceso.take(20) + if (beach.condicionesAcceso.length > 20) "..." else "") }, leadingIcon = { Icon(Icons.Default.DirectionsWalk, contentDescription = null) }, colors = AssistChipDefaults.assistChipColors(labelColor = onSurfaceColor))
                    }
                }
            }
        }
    }
}
