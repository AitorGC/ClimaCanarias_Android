import re

with open("app/src/main/java/com/example/ui/screens/SatelliteScreen.kt", "r") as f:
    content = f.read()

# Add systemBarsPadding to Scaffold
if "modifier = modifier.fillMaxSize().systemBarsPadding()" not in content:
    content = content.replace(
        "    Scaffold(\n        topBar = {",
        "    Scaffold(\n        modifier = modifier.fillMaxSize().systemBarsPadding(),\n        topBar = {"
    )

# Inject JS into onPageFinished
js_injection = """                            override fun onPageFinished(view: WebView?, url: String?) {
                                super.onPageFinished(view, url)
                                isRefreshing = false
                                view?.evaluateJavascript(\"\"\"
                                    (function() {
                                        var style = document.createElement('style');
                                        style.innerHTML = 'header, footer, nav, iframe, .cookie-consent, [id*=\"header\"], [id*=\"footer\"], [class*=\"header\"], [class*=\"footer\"], [class*=\"ad-\"], [class*=\"banner\"], [class*=\"cookie\"], div[data-testid=\"header\"], div[role=\"banner\"], div[role=\"contentinfo\"] { display: none !important; }';
                                        document.head.appendChild(style);
                                    })();
                                \"\"\".trimIndent(), null)
                            }"""

content = re.sub(
    r"override fun onPageFinished\(view: WebView\?, url: String\?\) \{[\s\S]*?\}",
    js_injection,
    content
)

with open("app/src/main/java/com/example/ui/screens/SatelliteScreen.kt", "w") as f:
    f.write(content)
