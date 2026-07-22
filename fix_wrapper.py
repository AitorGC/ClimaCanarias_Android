with open("app/src/main/java/com/example/ui/screens/MainWeatherScreen.kt", "r") as f:
    s = f.read()

old_wrapper = """                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxSize().padding(16.dp)
                ) {"""

new_wrapper = """                Box(
                    modifier = Modifier.fillMaxSize().padding(16.dp)
                ) {"""

s = s.replace(old_wrapper, new_wrapper)

with open("app/src/main/java/com/example/ui/screens/MainWeatherScreen.kt", "w") as f:
    f.write(s)
print("Done")
