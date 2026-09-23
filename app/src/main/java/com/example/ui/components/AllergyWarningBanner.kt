package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AllergenAlertItem
import com.example.data.AllergenSeverity
import com.example.data.AllergenType

/**
 * Top dashboard prioritized dynamic banner for aerobiological allergens and dust warnings.
 * Uses expandVertically + fadeIn smooth entrance.
 */
@Composable
fun AllergyWarningBanner(
    alerts: List<AllergenAlertItem>,
    isDarkTheme: Boolean,
    isAmoledTheme: Boolean = false,
    onOpenAlertsTab: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = alerts.isNotEmpty(),
        enter = expandVertically(animationSpec = tween(400)) + fadeIn(animationSpec = tween(400)),
        exit = shrinkVertically(animationSpec = tween(300)) + fadeOut(animationSpec = tween(300)),
        modifier = modifier
    ) {
        if (alerts.isEmpty()) return@AnimatedVisibility

        var isExpanded by rememberSaveable { mutableStateOf(false) }

        val topAlert = alerts.first()
        val hasMultiple = alerts.size > 1

        val primaryColor = when (topAlert.severity) {
            AllergenSeverity.VERY_HIGH -> Color(0xFFD32F2F) // Red
            AllergenSeverity.HIGH -> Color(0xFFE65100)      // Orange
            AllergenSeverity.MODERATE -> if (isDarkTheme) Color(0xFFFFD600) else Color(0xFFB78103) // Gold / Dark Amber
        }

        val containerColor = if (isDarkTheme) {
            if (isAmoledTheme) Color(0xFF0F0B0A) else primaryColor.copy(alpha = 0.15f)
        } else {
            primaryColor.copy(alpha = 0.08f)
        }

        val infiniteTransition = rememberInfiniteTransition(label = "AllergyPulse")
        val pulseAlpha by infiniteTransition.animateFloat(
            initialValue = 0.4f,
            targetValue = 0.9f,
            animationSpec = infiniteRepeatable(
                animation = tween(1800, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "AllergyPulseBorder"
        )

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("allergy_warning_banner")
                .animateContentSize(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = containerColor),
            border = BorderStroke(1.5.dp, primaryColor.copy(alpha = pulseAlpha))
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Main Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = primaryColor.copy(alpha = 0.20f),
                        modifier = Modifier.size(40.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = if (topAlert.allergenType == AllergenType.DUST_CALIMA) Icons.Default.Air else Icons.Default.Eco,
                                contentDescription = null,
                                tint = primaryColor,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "AVISO DE ALÉRGENOS",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = primaryColor,
                                letterSpacing = 0.8.sp
                            )
                            if (hasMultiple) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = primaryColor.copy(alpha = 0.2f),
                                    modifier = Modifier.padding(horizontal = 2.dp)
                                ) {
                                    Text(
                                        text = "+${alerts.size - 1} más",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = primaryColor,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                    )
                                }
                            }
                        }

                        Text(
                            text = topAlert.title,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isDarkTheme) Color.White else Color(0xFF1E1C24)
                        )

                        Text(
                            text = topAlert.alertDescription,
                            fontSize = 12.sp,
                            color = if (isDarkTheme) Color(0xFFCCCCCC) else Color(0xFF424242),
                            lineHeight = 16.sp
                        )
                    }

                    // Expand / Collapse action icon
                    IconButton(
                        onClick = { isExpanded = !isExpanded },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = if (isExpanded) "Contraer" else "Ver recomendaciones",
                            tint = primaryColor
                        )
                    }
                }

                // Expanded Section: Recommendations & Breakdown of active allergens
                if (isExpanded) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(
                                if (isDarkTheme) Color.Black.copy(alpha = 0.35f) else Color.White.copy(alpha = 0.70f)
                            )
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Actionable Advice Header
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.HealthAndSafety,
                                contentDescription = null,
                                tint = primaryColor,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "Recomendaciones de salud preventiva:",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isDarkTheme) Color.White else Color(0xFF1A1A1A)
                            )
                        }

                        // Practical recommendations list
                        topAlert.recommendations.forEach { tip ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Text(
                                    text = "•",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Black,
                                    color = primaryColor
                                )
                                Text(
                                    text = tip,
                                    fontSize = 12.sp,
                                    color = if (isDarkTheme) Color(0xFFDDDDDD) else Color(0xFF333333),
                                    lineHeight = 16.sp
                                )
                            }
                        }

                        // If multiple alerts, list the secondary ones concisely
                        if (hasMultiple) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Otros alérgenos activos en tu zona:",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (isDarkTheme) Color(0xFFB0B0B0) else Color(0xFF666666)
                            )
                            alerts.drop(1).forEach { alert ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = alert.allergenType.displayName,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = if (isDarkTheme) Color.White else Color(0xFF222222)
                                    )
                                    Text(
                                        text = "${alert.currentConcentration.toInt()} ${alert.unit} (${alert.severity.label})",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = when (alert.severity) {
                                            AllergenSeverity.VERY_HIGH -> Color(0xFFD32F2F)
                                            AllergenSeverity.HIGH -> Color(0xFFE65100)
                                            AllergenSeverity.MODERATE -> if (isDarkTheme) Color(0xFFFFD600) else Color(0xFFB78103)
                                        }
                                    )
                                }
                            }
                        }

                        // Navigation link to the full ALERTAS tab if callback provided
                        if (onOpenAlertsTab != null) {
                            Spacer(modifier = Modifier.height(2.dp))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onOpenAlertsTab() }
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.End,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Ver panel completo de alertas",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = primaryColor
                                )
                                Icon(
                                    imageVector = Icons.Default.ChevronRight,
                                    contentDescription = null,
                                    tint = primaryColor,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
