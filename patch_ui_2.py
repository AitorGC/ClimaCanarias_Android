import re

with open("app/src/main/java/com/example/ui/screens/MainWeatherScreen.kt", "r") as f:
    content = f.read()

target = """                                                            Row(
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
                                                                        text = if (station.temperatura != null) "${station.temperatura} °C" else "--",
                                                                        fontWeight = FontWeight.Bold,
                                                                        fontSize = 14.sp,
                                                                        color = if (isDarkTheme) primaryCanaryYellow else Color(0xFF004993)
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
                                                                        text = if (station.humedad != null) "${station.humedad.toInt()} %" else "--",
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
                                                                    Text("Viento", fontSize = 10.sp, color = Color.Gray)
                                                                    Text(
                                                                        text = if (station.vientoVelocidad != null) "${(station.vientoVelocidad * 3.6).toInt()} km/h" else "--",
                                                                        fontWeight = FontWeight.Bold,
                                                                        fontSize = 14.sp,
                                                                        color = if (isDarkTheme) Color(0xFFB9F6CA) else Color(0xFF2E7D32)
                                                                    )
                                                                }
                                                            }"""

replacement = """                                                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
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
                                                            }"""

if target in content:
    content = content.replace(target, replacement)
    with open("app/src/main/java/com/example/ui/screens/MainWeatherScreen.kt", "w") as f:
        f.write(content)
    print("Replaced!")
else:
    print("Not found!")

