with open("app/src/main/java/com/example/ui/screens/MainWeatherScreen.kt", "r") as f:
    content = f.read()

target = """                                        override fun onPageFinished(view: android.webkit.WebView?, url: String?) {"""
replacement = """                                        override fun onRenderProcessGone(view: android.webkit.WebView?, detail: android.webkit.RenderProcessGoneDetail?): Boolean {
                                            return true
                                        }

                                        override fun onPageFinished(view: android.webkit.WebView?, url: String?) {"""

if target in content:
    content = content.replace(target, replacement)
    with open("app/src/main/java/com/example/ui/screens/MainWeatherScreen.kt", "w") as f:
        f.write(content)
    print("WebView patched!")
else:
    print("Not found!")
