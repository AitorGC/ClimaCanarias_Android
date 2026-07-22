with open("app/src/main/java/com/example/ui/screens/MainWeatherScreen.kt", "r") as f:
    s = f.read()

# 1. Add import for rememberSaveable
if "import androidx.compose.runtime.saveable.rememberSaveable" not in s:
    s = s.replace("import androidx.compose.runtime.*", "import androidx.compose.runtime.*\nimport androidx.compose.runtime.saveable.rememberSaveable")

# 2. Move coroutineScope definition up
old_coroutine = "val coroutineScope = rememberCoroutineScope()"
if old_coroutine in s:
    s = s.replace(old_coroutine, "")
    s = s.replace("val pagerState = rememberPagerState(pageCount = { tabs.size })", "val pagerState = rememberPagerState(pageCount = { tabs.size })\n    val coroutineScope = rememberCoroutineScope()")

with open("app/src/main/java/com/example/ui/screens/MainWeatherScreen.kt", "w") as f:
    f.write(s)
print("Fixed errors")
