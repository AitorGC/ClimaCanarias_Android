with open("app/src/main/java/com/example/ui/screens/MainWeatherScreen.kt", "r") as f:
    content = f.read()

content = content.replace(
"""fun MainWeatherScreen(
    viewModel: WeatherViewModel,
    onNavigateToSatellite: () -> Unit = {},
    modifier: Modifier = Modifier
) {""",
"""fun MainWeatherScreen(
    viewModel: WeatherViewModel,
    modifier: Modifier = Modifier
) {"""
)

with open("app/src/main/java/com/example/ui/screens/MainWeatherScreen.kt", "w") as f:
    f.write(content)
