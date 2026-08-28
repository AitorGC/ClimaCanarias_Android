package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.ApiCategory
import com.example.data.GlobalApiSummary
import com.example.data.SingleApiStats
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ApiStatsModal(
    summary: GlobalApiSummary,
    onResetStats: () -> Unit,
    onDismiss: () -> Unit,
    isDarkTheme: Boolean
) {
    var showResetConfirmDialog by remember { mutableStateOf(false) }

    val primaryColor = if (isDarkTheme) Color(0xFFFFD600) else Color(0xFF004993)
    val cardBackground = if (isDarkTheme) Color(0xFF1E1E28) else Color.White
    val surfaceColor = if (isDarkTheme) Color(0xFF121218) else Color(0xFFF4F6F9)
    val onSurfaceColor = if (isDarkTheme) Color.White else Color(0xFF1C1B1F)
    val subtextColor = if (isDarkTheme) Color.LightGray else Color.Gray

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.88f)
                .padding(horizontal = 20.dp, vertical = 32.dp)
                .clip(RoundedCornerShape(24.dp))
                .testTag("api_stats_dialog"),
            color = surfaceColor,
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                // Modal Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(primaryColor.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("📊", fontSize = 20.sp)
                        }
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Telemetría de APIs",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Black,
                                    color = onSurfaceColor
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "v2.4.1",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = primaryColor,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(primaryColor.copy(alpha = 0.15f))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                            Text(
                                text = "Panel oculto • con 💛 por AItor Santana",
                                fontSize = 12.sp,
                                color = subtextColor,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .testTag("close_api_stats_button")
                            .clip(CircleShape)
                            .background(cardBackground)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Cerrar",
                            tint = onSurfaceColor
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Global Summary Metrics Cards
                    item {
                        Text(
                            text = "MÉTRICAS GLOBALES",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = primaryColor,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            GlobalMetricCard(
                                title = "Total Peticiones",
                                value = "${summary.totalCalls}",
                                subtitle = "Llamadas registradas",
                                icon = Icons.Default.CloudSync,
                                accentColor = Color(0xFF2196F3),
                                modifier = Modifier.weight(1f),
                                cardBackground = cardBackground,
                                onSurfaceColor = onSurfaceColor
                            )
                            GlobalMetricCard(
                                title = "Tasa de Éxito",
                                value = "${summary.successRatePercentage}%",
                                subtitle = "${summary.totalSuccess} exitosas",
                                icon = Icons.Default.CheckCircle,
                                accentColor = Color(0xFF4CAF50),
                                modifier = Modifier.weight(1f),
                                cardBackground = cardBackground,
                                onSurfaceColor = onSurfaceColor
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            GlobalMetricCard(
                                title = "Errores / Fallos",
                                value = "${summary.totalFailed}",
                                subtitle = if (summary.totalFailed == 0L) "Sin incidencias" else "Errores HTTP/Red",
                                icon = Icons.Default.WarningAmber,
                                accentColor = if (summary.totalFailed > 0) Color(0xFFE91E63) else Color(0xFF9E9E9E),
                                modifier = Modifier.weight(1f),
                                cardBackground = cardBackground,
                                onSurfaceColor = onSurfaceColor
                            )
                            GlobalMetricCard(
                                title = "Latencia Media",
                                value = "${summary.averageResponseTimeMs} ms",
                                subtitle = "Tiempo de respuesta",
                                icon = Icons.Default.Timer,
                                accentColor = Color(0xFFFF9800),
                                modifier = Modifier.weight(1f),
                                cardBackground = cardBackground,
                                onSurfaceColor = onSurfaceColor
                            )
                        }
                    }

                    // Traffic Distribution Bar
                    if (summary.totalCalls > 0) {
                        item {
                            Spacer(modifier = Modifier.height(8.dp))
                            Card(
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = cardBackground),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Text(
                                        text = "Distribución de Consultas",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = onSurfaceColor
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))

                                    // Multi-segment progress bar
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(10.dp)
                                            .clip(RoundedCornerShape(5.dp))
                                            .background(Color.Gray.copy(alpha = 0.2f))
                                    ) {
                                        val palette = listOf(
                                            Color(0xFFFFC107),
                                            Color(0xFF03A9F4),
                                            Color(0xFF009688),
                                            Color(0xFF3F51B5),
                                            Color(0xFFE91E63),
                                            Color(0xFF9C27B0),
                                            Color(0xFF4CAF50),
                                            Color(0xFFFF5722),
                                            Color(0xFF795548),
                                            Color(0xFF607D8B)
                                        )

                                        summary.statsList.filter { it.totalCalls > 0 }.forEachIndexed { index, stat ->
                                            val weight = (stat.totalCalls.toFloat() / summary.totalCalls.toFloat()).coerceAtLeast(0.01f)
                                            val color = palette[index % palette.size]
                                            Box(
                                                modifier = Modifier
                                                    .weight(weight)
                                                    .fillMaxHeight()
                                                    .background(color)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))

                                    // Legend
                                    Column(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        val palette = listOf(
                                            Color(0xFFFFC107),
                                            Color(0xFF03A9F4),
                                            Color(0xFF009688),
                                            Color(0xFF3F51B5),
                                            Color(0xFFE91E63),
                                            Color(0xFF9C27B0),
                                            Color(0xFF4CAF50),
                                            Color(0xFFFF5722),
                                            Color(0xFF795548),
                                            Color(0xFF607D8B),
                                            Color(0xFF00BCD4),
                                            Color(0xFF8BC34A)
                                        )
                                        summary.statsList.filter { it.totalCalls > 0 }.forEachIndexed { index, stat ->
                                            val color = palette[index % palette.size]
                                            val category = ApiCategory.values().find { it.id == stat.categoryId }
                                            val categoryName = category?.displayName ?: stat.categoryId
                                            val emoji = category?.emoji ?: "🌐"
                                            val percentage = if (summary.totalCalls > 0) (stat.totalCalls * 100) / summary.totalCalls else 0
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(10.dp)
                                                        .clip(CircleShape)
                                                        .background(color)
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text = "$emoji $categoryName",
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Medium,
                                                    color = onSurfaceColor,
                                                    modifier = Modifier.weight(1f)
                                                )
                                                Text(
                                                    text = "${stat.totalCalls} reqs ($percentage%)",
                                                    fontSize = 11.sp,
                                                    color = subtextColor
                                                )
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "Primer registro: ${formatTimestamp(summary.firstRecordedDate)}",
                                        fontSize = 11.sp,
                                        color = subtextColor
                                    )
                                }
                            }
                        }
                    }

                    // Detailed Per-API Breakdown
                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "DETALLE POR SERVICIO Y API",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = primaryColor,
                            letterSpacing = 1.sp
                        )
                    }

                    items(ApiCategory.values()) { category ->
                        val stat = summary.statsList.find { it.categoryId == category.id }
                            ?: SingleApiStats(categoryId = category.id)
                        
                        ApiDetailCard(
                            category = category,
                            stat = stat,
                            cardBackground = cardBackground,
                            onSurfaceColor = onSurfaceColor,
                            subtextColor = subtextColor,
                            isDarkTheme = isDarkTheme
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Bottom Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = { showResetConfirmDialog = true },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("reset_api_stats_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Icon(Icons.Default.DeleteOutline, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Restablecer", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("dismiss_api_stats_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = primaryColor,
                            contentColor = if (isDarkTheme) Color.Black else Color.White
                        )
                    ) {
                        Text("Cerrar", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    if (showResetConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showResetConfirmDialog = false },
            title = { Text("¿Restablecer estadísticas?", fontWeight = FontWeight.Bold) },
            text = { Text("Se borrarán los contadores y métricas acumuladas de llamadas a las APIs.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onResetStats()
                        showResetConfirmDialog = false
                    }
                ) {
                    Text("Sí, restablecer", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetConfirmDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
private fun GlobalMetricCard(
    title: String,
    value: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    accentColor: Color,
    modifier: Modifier = Modifier,
    cardBackground: Color,
    onSurfaceColor: Color
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = cardBackground),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = onSurfaceColor.copy(alpha = 0.7f)
                )
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(accentColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                fontSize = 20.sp,
                fontWeight = FontWeight.Black,
                color = onSurfaceColor
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                fontSize = 10.sp,
                color = onSurfaceColor.copy(alpha = 0.55f)
            )
        }
    }
}

@Composable
private fun ApiDetailCard(
    category: ApiCategory,
    stat: SingleApiStats,
    cardBackground: Color,
    onSurfaceColor: Color,
    subtextColor: Color,
    isDarkTheme: Boolean
) {
    val isNeverCalled = stat.totalCalls == 0L
    val statusColor = when {
        isNeverCalled -> Color.Gray
        stat.failedCalls == 0L -> Color(0xFF4CAF50)
        stat.successCalls > stat.failedCalls -> Color(0xFFFF9800)
        else -> Color(0xFFE91E63)
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = cardBackground),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(category.emoji, fontSize = 18.sp)
                    Column {
                        Text(
                            text = category.displayName,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = onSurfaceColor
                        )
                        Text(
                            text = category.endpointHost,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            color = subtextColor
                        )
                    }
                }

                // Call count chip
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(statusColor.copy(alpha = 0.15f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = if (isNeverCalled) "Inactiva" else "${stat.totalCalls} reqs",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = statusColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = category.description,
                fontSize = 11.sp,
                color = onSurfaceColor.copy(alpha = 0.75f)
            )

            if (!isNeverCalled) {
                Spacer(modifier = Modifier.height(10.dp))
                HorizontalDivider(color = onSurfaceColor.copy(alpha = 0.08f), thickness = 1.dp)
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Éxito / Fallos", fontSize = 10.sp, color = subtextColor)
                        Text(
                            text = "${stat.successCalls} ✓ / ${stat.failedCalls} ✗",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = onSurfaceColor
                        )
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Última Latencia", fontSize = 10.sp, color = subtextColor)
                        Text(
                            text = "${stat.lastResponseTimeMs} ms",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = onSurfaceColor
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("Último Estado", fontSize = 10.sp, color = subtextColor)
                        Text(
                            text = if (stat.lastStatusCode > 0) "HTTP ${stat.lastStatusCode}" else (stat.lastErrorMessage ?: "OK"),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (stat.lastStatusCode in 200..299) Color(0xFF4CAF50) else Color(0xFFE91E63)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Media: ${stat.averageResponseTimeMs} ms • Tasa: ${stat.successRate}%",
                        fontSize = 10.sp,
                        color = subtextColor
                    )
                    Text(
                        text = "Última: ${formatTimestamp(stat.lastCallTimestamp)}",
                        fontSize = 10.sp,
                        color = subtextColor
                    )
                }
            }
        }
    }
}

private fun formatTimestamp(timestamp: Long): String {
    if (timestamp <= 0L) return "Nunca"
    val diff = System.currentTimeMillis() - timestamp
    if (diff < 60_000) return "Hace un momento"
    if (diff < 3600_000) return "Hace ${diff / 60_000} min"
    val sdf = SimpleDateFormat("dd/MM HH:mm:ss", Locale.getDefault())
    return sdf.format(Date(timestamp))
}
