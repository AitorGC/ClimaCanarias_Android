package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AllergenAlertItem
import com.example.data.AllergenSeverity
import com.example.data.AllergenType

/**
 * Dedicated Allergy & Aerobiology section integrated inside the ALERTAS tab.
 */
@Composable
fun AllergyAlertsSection(
    alerts: List<AllergenAlertItem>,
    isDarkTheme: Boolean,
    cardBackgroundColor: Color,
    onSurfaceColor: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Section Header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.HealthAndSafety,
                contentDescription = null,
                tint = if (alerts.isNotEmpty()) Color(0xFFE65100) else Color(0xFF4CAF50),
                modifier = Modifier.size(24.dp)
            )
            Text(
                text = "Vigilancia de Alérgenos y Salud Ambiental",
                fontWeight = FontWeight.Bold,
                fontSize = 17.sp,
                color = onSurfaceColor
            )
        }

        if (alerts.isEmpty()) {
            Card(
                colors = CardDefaults.cardColors(containerColor = cardBackgroundColor),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = Color(0xFF4CAF50).copy(alpha = 0.15f),
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Eco,
                                contentDescription = null,
                                tint = Color(0xFF4CAF50),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Column {
                        Text(
                            text = "Niveles biológicos favorables",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = onSurfaceColor
                        )
                        Text(
                            text = "Los alérgenos seleccionados en tus ajustes no presentan concentraciones de riesgo en tu zona en este momento.",
                            fontSize = 12.sp,
                            color = onSurfaceColor.copy(alpha = 0.7f),
                            lineHeight = 16.sp
                        )
                    }
                }
            }
        } else {
            alerts.forEach { alert ->
                val alertColor = when (alert.severity) {
                    AllergenSeverity.VERY_HIGH -> Color(0xFFD32F2F)
                    AllergenSeverity.HIGH -> Color(0xFFE65100)
                    AllergenSeverity.MODERATE -> if (isDarkTheme) Color(0xFFFFD600) else Color(0xFFB78103)
                }

                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = alertColor.copy(alpha = if (isDarkTheme) 0.14f else 0.08f)
                    ),
                    border = BorderStroke(1.dp, alertColor.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(18.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Title & severity badge
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = if (alert.allergenType == AllergenType.DUST_CALIMA) Icons.Default.Air else Icons.Default.Eco,
                                    contentDescription = null,
                                    tint = alertColor,
                                    modifier = Modifier.size(22.dp)
                                )
                                Text(
                                    text = alert.allergenType.displayName,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = onSurfaceColor
                                )
                            }

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = alertColor,
                                contentColor = Color.White
                            ) {
                                Text(
                                    text = alert.severity.label.uppercase(),
                                    fontWeight = FontWeight.Black,
                                    fontSize = 10.sp,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                    letterSpacing = 0.5.sp
                                )
                            }
                        }

                        // Concentration readout
                        Text(
                            text = "Concentración: ${alert.currentConcentration.toInt()} ${alert.unit} • ${alert.allergenType.scientificFamily}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = alertColor
                        )

                        Text(
                            text = alert.alertDescription,
                            fontSize = 13.sp,
                            color = onSurfaceColor,
                            lineHeight = 17.sp
                        )

                        HorizontalDivider(
                            color = alertColor.copy(alpha = 0.2f),
                            modifier = Modifier.padding(vertical = 4.dp)
                        )

                        // Practical recommendations
                        Text(
                            text = "Medidas y recomendaciones:",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = onSurfaceColor
                        )

                        alert.recommendations.forEach { tip ->
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Text(
                                    text = "•",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = alertColor
                                )
                                Text(
                                    text = tip,
                                    fontSize = 12.sp,
                                    color = onSurfaceColor.copy(alpha = 0.85f),
                                    lineHeight = 16.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
