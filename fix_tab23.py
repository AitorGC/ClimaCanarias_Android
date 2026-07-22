with open("app/src/main/java/com/example/ui/screens/MainWeatherScreen.kt", "r") as f:
    s = f.read()

# Fix Tab 2
s = s.replace(
"""                        2 -> {
                            Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(16.dp)) {""",
"""                        2 -> {
                            Box(modifier = Modifier.fillMaxSize()) {""")

# Fix Tab 3
s = s.replace(
"""                    when (val state = stationsState) {
                        is com.example.viewmodel.AemetStationsUiState.Idle -> {""",
"""                    Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                        when (val state = stationsState) {
                        is com.example.viewmodel.AemetStationsUiState.Idle -> {""")

# We need to add the closing brace for the Box in Tab 3.
# Where does Tab 3's when end?
# Let's locate the 4 -> branch.
s = s.replace(
"""                        }
                        4 -> {""",
"""                        }
                    }
                        }
                        4 -> {""")

with open("app/src/main/java/com/example/ui/screens/MainWeatherScreen.kt", "w") as f:
    f.write(s)
print("Done")
