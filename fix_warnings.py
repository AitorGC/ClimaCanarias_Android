with open("app/src/main/java/com/example/ui/screens/MainWeatherScreen.kt", "r") as f:
    s = f.read()

# I want to remove the *second* occurrence, which is inside `2 -> {` block.
import re
# First, let's just make sure there is a global one.
# If not, let's re-add it.
if "val warningsState by" not in s:
    print("No warningsState found at all!")
else:
    occurrences = [m.start() for m in re.finditer(r'val warningsState by viewModel\.warningsUiState\.collectAsStateWithLifecycle\(\)', s)]
    print(f"Found {len(occurrences)} occurrences")
    if len(occurrences) > 1:
        s = s[:occurrences[1]] + s[occurrences[1] + len("val warningsState by viewModel.warningsUiState.collectAsStateWithLifecycle()"): ]
        with open("app/src/main/java/com/example/ui/screens/MainWeatherScreen.kt", "w") as f:
            f.write(s)
        print("Removed second occurrence")

