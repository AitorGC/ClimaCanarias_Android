with open("app/src/main/java/com/example/MainActivity.kt", "r") as f:
    content = f.read()

new_set_content = """    setContent {
      MyApplicationTheme {
        var backPressedOnce by remember { mutableStateOf(false) }
        val context = LocalContext.current
        
        LaunchedEffect(backPressedOnce) {
            if (backPressedOnce) {
                delay(2000)
                backPressedOnce = false
            }
        }
        
        BackHandler(enabled = true) {
            if (backPressedOnce) {
                finish()
            } else {
                backPressedOnce = true
                Toast.makeText(context, "Presiona una vez más para salir", Toast.LENGTH_SHORT).show()
            }
        }

        Surface(
          modifier = Modifier.fillMaxSize(),
          color = MaterialTheme.colorScheme.background
        ) {
          MainWeatherScreen(viewModel = viewModel)
        }
      }
    }"""

import re
content = re.sub(r"    setContent \{[\s\S]*\}\n  \}", new_set_content + "\n  }", content)

content = content.replace("import androidx.navigation.compose.NavHost\n", "")
content = content.replace("import androidx.navigation.compose.composable\n", "")
content = content.replace("import androidx.navigation.compose.rememberNavController\n", "")
content = content.replace("import com.example.ui.screens.SatelliteScreen\n", "")

with open("app/src/main/java/com/example/MainActivity.kt", "w") as f:
    f.write(content)
