with open("app/src/main/java/com/example/ui/screens/MainWeatherScreen.kt", "r") as f:
    s = f.read()

print(s[s.find("LaunchedEffect(pagerState.currentPage) {"):s.find("val coroutineScope = rememberCoroutineScope()")])

