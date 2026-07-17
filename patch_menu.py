with open("app/src/main/java/com/example/ui/screens/MainWeatherScreen.kt", "r") as f:
    content = f.read()

satellite_item = """                            DropdownMenuItem(
                                text = { Text("Satélite") },
                                onClick = {
                                    showMainMenu = false
                                    onNavigateToSatellite()
                                },
                                leadingIcon = {
                                    Icon(Icons.Default.Public, contentDescription = null, modifier = Modifier.size(20.dp))
                                }
                            )
"""

content = content.replace("                            DropdownMenuItem(\n                                text = { Text(\"Alertas\") },", satellite_item + "                            DropdownMenuItem(\n                                text = { Text(\"Alertas\") },")

with open("app/src/main/java/com/example/ui/screens/MainWeatherScreen.kt", "w") as f:
    f.write(content)
