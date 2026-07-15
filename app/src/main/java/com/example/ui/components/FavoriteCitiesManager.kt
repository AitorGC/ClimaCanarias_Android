package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.db.FavoriteCity

@Composable
fun FavoriteCitiesManager(
    favorites: List<FavoriteCity>,
    selectedCity: FavoriteCity?,
    onCitySelected: (FavoriteCity) -> Unit,
    onSearchRegion: (String, (String?) -> Unit) -> Unit,
    onDeleteFavorite: (FavoriteCity) -> Unit,
    modifier: Modifier = Modifier
) {
    var isAddPanelOpen by remember { mutableStateOf(false) }
    var inputQuery by remember { mutableStateOf("") }
    var searchError by remember { mutableStateOf<String?>(null) }
    var isSearching by remember { mutableStateOf(false) }
    
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
                    searchError = null
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
                    contentDescription = if (isAddPanelOpen) "Limpiar" else "Buscar Ubicación",
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
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
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
                        text = "Buscar Ubicación",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    OutlinedTextField(
                        value = inputQuery,
                        onValueChange = { inputQuery = it },
                        label = { Text("Escribe una zona (pe. Maspalomas)") },
                        textStyle = LocalTextStyle.current.copy(fontSize = 13.sp),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (searchError != null) {
                        Text(
                            text = searchError!!,
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Button(
                        onClick = {
                            focusManager.clearFocus()
                            val query = inputQuery.trim()
                            if (query.isEmpty()) {
                                searchError = "Ingrese un nombre descriptivo válido."
                                return@Button
                            }
                            isSearching = true
                            searchError = null
                            onSearchRegion(query) { error ->
                                isSearching = false
                                if (error == null) {
                                    inputQuery = ""
                                    isAddPanelOpen = false
                                } else {
                                    searchError = error
                                }
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        ),
                        enabled = !isSearching
                    ) {
                        if (isSearching) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Buscando...", fontSize = 13.sp)
                        } else {
                            Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Buscar y Añadir", fontSize = 13.sp)
                        }
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
