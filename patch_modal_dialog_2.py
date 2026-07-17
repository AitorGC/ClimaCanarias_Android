with open("app/src/main/java/com/example/ui/screens/MainWeatherScreen.kt", "r") as f:
    content = f.read()

content = content.replace("header, footer, nav, iframe, .cookie-consent,", "header, footer, nav, .cookie-consent,")

with open("app/src/main/java/com/example/ui/screens/MainWeatherScreen.kt", "w") as f:
    f.write(content)
