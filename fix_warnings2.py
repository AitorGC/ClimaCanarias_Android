with open("app/src/main/java/com/example/ui/screens/MainWeatherScreen.kt", "r") as f:
    s = f.read()

old = """    var initialAlertPopupChecked by rememberSaveable { mutableStateOf(false) }"""
new = """    val warningsState by viewModel.warningsUiState.collectAsStateWithLifecycle()
    var initialAlertPopupChecked by rememberSaveable { mutableStateOf(false) }"""

if "warningsState by" not in s:
    s = s.replace(old, new)
    with open("app/src/main/java/com/example/ui/screens/MainWeatherScreen.kt", "w") as f:
        f.write(s)
    print("Re-added global warningsState")
else:
    print("It's already there")

