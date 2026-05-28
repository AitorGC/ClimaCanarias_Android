package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.db.FavoriteCity

@Composable
fun FavoriteCitiesManager(
    favorites: List<FavoriteCity>,
    selectedCity: FavoriteCity?,
    onCitySelected: (FavoriteCity) -> Unit,
    onAddFavorite: (String, Double, Double) -> Unit,
    onDeleteFavorite: (FavoriteCity) -> Unit,
    modifier: Modifier = Modifier
) {
    var isAddPanelOpen by remember { mutableStateOf(false) }
    var inputName by remember { mutableStateOf("") }
    var inputLat by remember { mutableStateOf("") }
    var inputLng by remember { mutableStateOf("") }
    var validationError by remember { mutableStateOf<String?>(null) }
    
    val focusManager = LocalFocusManager.current

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Section Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Tus Ubicaciones Canarias",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Button(
                onClick = { 
                    isAddPanelOpen = !isAddPanelOpen
                    validationError = null
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                    contentColor = MaterialTheme.colorScheme.primary
                ),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.height(34.dp)
            ) {
                Icon(
                    imageVector = if (isAddPanelOpen) Icons.Default.Close else Icons.Default.Add,
                    contentDescription = if (isAddPanelOpen) "Limpiar" else "Añadir Coordenadas",
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = if (isAddPanelOpen) "Cancelar" else "Añadir", fontSize = 12.sp)
            }
        }

        // Add New Custom Coordinate Panel (Sliding foldout)
        AnimatedVisibility(
            visible = isAddPanelOpen,
            enter = expandVertically(),
            exit = shrinkVertically()
        ) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Añadir Coordenadas Personalizadas",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    OutlinedTextField(
                        value = inputName,
                        onValueChange = { inputName = it },
                        label = { Text("Nombre de la Región (pe. Maspalomas)") },
                        textStyle = LocalTextStyle.current.copy(fontSize = 13.sp),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = inputLat,
                            onValueChange = { inputLat = it },
                            label = { Text("Latitud (p.e. 27.76)") },
                            textStyle = LocalTextStyle.current.copy(fontSize = 13.sp),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        )

                        OutlinedTextField(
                            value = inputLng,
                            onValueChange = { inputLng = it },
                            label = { Text("Longitud (p.e. -15.57)") },
                            textStyle = LocalTextStyle.current.copy(fontSize = 13.sp),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // Predefined regional Canary Island quick-fill chips
                    val presets = remember {
                        listOf(
                            Triple("Las Palmas de Gran Canaria", 28.1235, -15.4362),
                            Triple("Telde", 27.9942, -15.4184),
                            Triple("Santa Lucía de Tirajana", 27.9103, -15.5414),
                            Triple("San Bartolomé de Tirajana", 27.9258, -15.5732),
                            Triple("Arucas", 28.1171, -15.5225),
                            Triple("Agüimes", 27.9056, -15.4461),
                            Triple("Gáldar", 28.1438, -15.6541),
                            Triple("Ingenio", 27.9213, -15.4411),
                            Triple("Teror", 28.0594, -15.5475),
                            Triple("Santa María de Guía", 28.1384, -15.6322),
                            Triple("Mogán", 27.8838, -15.7231),
                            Triple("Valsequillo de Gran Canaria", 27.9792, -15.5003),
                            Triple("Firgas", 28.1062, -15.5638),
                            Triple("Santa Brígida", 28.0319, -15.4975),
                            Triple("Vega de San Mateo", 28.0108, -15.5342),
                            Triple("Agaete", 28.1006, -15.7008),
                            Triple("La Aldea de San Nicolás", 27.9822, -15.7806),
                            Triple("Moya", 28.1119, -15.5842),
                            Triple("Tejeda", 27.9950, -15.6152),
                            Triple("Artenara", 28.0194, -15.6444),
                            Triple("Valleseco", 28.0506, -15.5753)
                        )
                    }
                    
                    Column(
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Presets Canarios (Autocompletar):",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(presets) { preset ->
                                SuggestionChip(
                                    onClick = {
                                        inputName = preset.first
                                        inputLat = preset.second.toString()
                                        inputLng = preset.third.toString()
                                    },
                                    label = { Text(preset.first, fontSize = 11.sp) }
                                )
                            }
                        }
                    }

                    if (validationError != null) {
                        Text(
                            text = validationError!!,
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Button(
                        onClick = {
                            focusManager.clearFocus()
                            val name = inputName.trim()
                            val lat = inputLat.toDoubleOrNull()
                            val lng = inputLng.toDoubleOrNull()

                            if (name.isEmpty()) {
                                validationError = "Ingrese un nombre descriptivo válido."
                                return@Button
                            }
                            if (lat == null || lat < 26.5 || lat > 29.8) {
                                validationError = "Latitud fuera del rango de Canarias (26.5 a 29.8)."
                                return@Button
                            }
                            if (lng == null || lng < -18.5 || lng > -13.0) {
                                validationError = "Longitud fuera del rango de Canarias (-18.5 a -13.0)."
                                return@Button
                            }

                            onAddFavorite(name, lat, lng)
                            
                            // Reset inputs
                            inputName = ""
                            inputLat = ""
                            inputLng = ""
                            validationError = null
                            isAddPanelOpen = false
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text("Registrar Nueva Ubicación", fontSize = 13.sp)
                    }
                }
            }
        }

        // Horizontal visual list of locations
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(bottom = 4.dp)
        ) {
            items(favorites) { favorite ->
                val isSelected = selectedCity?.name == favorite.name
                
                val isDark = MaterialTheme.colorScheme.background.red < 0.2f
                
                val cardColor = if (isSelected) {
                    Color(0xFFFFD600) // Canary Yellow
                } else {
                    if (isDark) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f) else Color(0xFFEFF6FF) // Soft Blue
                }

                val borderColor = if (isSelected) {
                    Color(0xFFFFD600)
                } else {
                    if (isDark) MaterialTheme.colorScheme.outlineVariant else Color(0xFFDBEAFE) // Blue Border
                }

                val textColor = if (isSelected) {
                    Color.Black
                } else {
                    if (isDark) MaterialTheme.colorScheme.onSurfaceVariant else Color(0xFF004993) // Signal Blue
                }

                val iconTint = if (isSelected) {
                    Color.Black
                } else {
                    if (isDark) MaterialTheme.colorScheme.primary.copy(alpha = 0.7f) else Color(0xFF004993).copy(alpha = 0.7f)
                }

                Surface(
                    modifier = Modifier
                        .widthIn(min = 125.dp, max = 210.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .clickable { onCitySelected(favorite) },
                    color = cardColor,
                    shape = RoundedCornerShape(24.dp),
                    border = androidx.compose.foundation.BorderStroke(1.2.dp, borderColor)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f, fill = false)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Place,
                                contentDescription = null,
                                tint = iconTint,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Column {
                                Text(
                                    text = favorite.name,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = textColor,
                                    maxLines = 1
                                )
                                Text(
                                    text = "${String.format("%.2f", favorite.latitude)}, ${String.format("%.2f", favorite.longitude)}",
                                    fontSize = 9.sp,
                                    color = textColor.copy(alpha = 0.6f)
                                )
                            }
                        }

                        // Allow deletion of any location for thorough favorite management
                        Spacer(modifier = Modifier.width(6.dp))
                        IconButton(
                            onClick = { onDeleteFavorite(favorite) },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Eliminar",
                                tint = if (isSelected) Color.Black.copy(alpha = 0.6f) else Color.Red.copy(alpha = 0.7f),
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
