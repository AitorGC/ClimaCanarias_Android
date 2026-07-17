with open("app/src/main/java/com/example/ui/screens/MainWeatherScreen.kt", "r") as f:
    content = f.read()

content = content.replace(
"""                                onClick = {
                                    showMainMenu = false
                                    onNavigateToSatellite()
                                }""",
"""                                onClick = {
                                    showMainMenu = false
                                    showSatelliteModal = true
                                }"""
)

with open("app/src/main/java/com/example/ui/screens/MainWeatherScreen.kt", "w") as f:
    f.write(content)
