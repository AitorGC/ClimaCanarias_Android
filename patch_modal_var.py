with open("app/src/main/java/com/example/ui/screens/MainWeatherScreen.kt", "r") as f:
    content = f.read()

content = content.replace(
    "    var showNotificationsModal by remember { mutableStateOf(false) }",
    "    var showNotificationsModal by remember { mutableStateOf(false) }\n    var showSatelliteModal by remember { mutableStateOf(false) }"
)

with open("app/src/main/java/com/example/ui/screens/MainWeatherScreen.kt", "w") as f:
    f.write(content)
