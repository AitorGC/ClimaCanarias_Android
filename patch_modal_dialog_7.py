import re

with open("app/src/main/java/com/example/ui/screens/MainWeatherScreen.kt", "r") as f:
    content = f.read()

old_box = """                    Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                        androidx.compose.ui.viewinterop.AndroidView(
                            factory = { context ->
                                android.webkit.WebView(context).apply {
                                    settings.javaScriptEnabled = true
                                    settings.domStorageEnabled = true
                                    webViewClient = object : android.webkit.WebViewClient() {
                                        override fun onPageFinished(view: android.webkit.WebView?, url: String?) {
                                            super.onPageFinished(view, url)
                                            view?.evaluateJavascript(\"\"\""""

new_box = """                    Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                        var isSatelliteLoading by remember { mutableStateOf(true) }
                        androidx.compose.ui.viewinterop.AndroidView(
                            factory = { context ->
                                android.webkit.WebView(context).apply {
                                    settings.javaScriptEnabled = true
                                    settings.domStorageEnabled = true
                                    webViewClient = object : android.webkit.WebViewClient() {
                                        override fun onPageFinished(view: android.webkit.WebView?, url: String?) {
                                            super.onPageFinished(view, url)
                                            isSatelliteLoading = false
                                            view?.evaluateJavascript(\"\"\""""

content = content.replace(old_box, new_box)

old_end_box = """                                    loadUrl("https://www.sat24.com/es-es/region/8000076")
                                }
                            },
                            modifier = Modifier.fillMaxSize()
                        )
                    }"""

new_end_box = """                                    loadUrl("https://www.sat24.com/es-es/region/8000076")
                                }
                            },
                            modifier = Modifier.fillMaxSize()
                        )
                        if (isSatelliteLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.align(Alignment.Center),
                                color = primaryCanaryYellow
                            )
                        }
                    }"""

content = content.replace(old_end_box, new_end_box)

with open("app/src/main/java/com/example/ui/screens/MainWeatherScreen.kt", "w") as f:
    f.write(content)
