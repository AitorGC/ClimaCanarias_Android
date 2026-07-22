with open("app/src/main/java/com/example/ui/screens/MainWeatherScreen.kt", "r") as f:
    s = f.read()

s = s.replace("var showInitialAlertPopup by rememberSaveable { mutableStateOf(false) }", 
"var showInitialAlertPopup by rememberSaveable { mutableStateOf(false) }\n    val warningsState by viewModel.warningsUiState.collectAsStateWithLifecycle()")

# Also remove the one at line ~666
import re
occurrences = [m.start() for m in re.finditer(r'val warningsState by viewModel\.warningsUiState\.collectAsStateWithLifecycle\(\)', s)]
if len(occurrences) > 1:
    s = s[:occurrences[1]] + s[occurrences[1] + len("val warningsState by viewModel.warningsUiState.collectAsStateWithLifecycle()"): ]

with open("app/src/main/java/com/example/ui/screens/MainWeatherScreen.kt", "w") as f:
    f.write(s)
print("Done")
