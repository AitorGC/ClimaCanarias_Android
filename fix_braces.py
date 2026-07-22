with open("app/src/main/java/com/example/ui/screens/MainWeatherScreen.kt", "r") as f:
    lines = f.readlines()

for i, line in enumerate(lines[1045:1065]):
    print(f"{1045+i+1}: {line.rstrip()}")

