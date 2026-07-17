with open("app/src/main/java/com/example/ui/screens/MainWeatherScreen.kt", "r") as f:
    content = f.read()

satellite_modal = """
    if (showSatelliteModal) {
        androidx.compose.ui.window.Dialog(
            onDismissRequest = { showSatelliteModal = false },
            properties = androidx.compose.ui.window.DialogProperties(
                usePlatformDefaultWidth = false
            )
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth(0.95f)
                    .fillMaxHeight(0.8f)
                    .clip(RoundedCornerShape(16.dp)),
                color = appBackgroundColor
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Satélite", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = onSurfaceColor)
                        IconButton(onClick = { showSatelliteModal = false }) {
                            Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = onSurfaceColor)
                        }
                    }
                    Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                        androidx.compose.ui.viewinterop.AndroidView(
                            factory = { context ->
                                android.webkit.WebView(context).apply {
                                    settings.javaScriptEnabled = true
                                    settings.domStorageEnabled = true
                                    webViewClient = object : android.webkit.WebViewClient() {
                                        override fun onPageFinished(view: android.webkit.WebView?, url: String?) {
                                            super.onPageFinished(view, url)
                                            view?.evaluateJavascript(\"\"\"
                                                (function() {
                                                    var style = document.createElement('style');
                                                    style.innerHTML = 'header, footer, nav, iframe, .cookie-consent, [id*=\"header\"], [id*=\"footer\"], [class*=\"header\"], [class*=\"footer\"], [class*=\"ad-\"], [class*=\"banner\"], [class*=\"cookie\"], div[data-testid=\"header\"], div[role=\"banner\"], div[role=\"contentinfo\"] { display: none !important; }';
                                                    document.head.appendChild(style);
                                                })();
                                            \"\"\".trimIndent(), null)
                                        }
                                    }
                                    loadUrl("https://www.sat24.com/en-gb/region/8000076")
                                }
                            },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }
        }
    }
"""

content = content.replace("    if (showNotificationsModal) {", satellite_modal + "\n    if (showNotificationsModal) {")

with open("app/src/main/java/com/example/ui/screens/MainWeatherScreen.kt", "w") as f:
    f.write(content)
