with open("app/src/main/java/com/example/ui/screens/MainWeatherScreen.kt", "r") as f:
    s = f.read()

bad = """                        }
                    }
                        }
                        4 -> {"""
good = """                        }
                    }
                        4 -> {"""

if bad in s:
    s = s.replace(bad, good, 1)

with open("app/src/main/java/com/example/ui/screens/MainWeatherScreen.kt", "w") as f:
    f.write(s)
print("Done")
