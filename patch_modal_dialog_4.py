import re

with open("app/src/main/java/com/example/ui/screens/MainWeatherScreen.kt", "r") as f:
    content = f.read()

old_js = """                                            view?.evaluateJavascript(\"\"\"
                                                (function() {
                                                    var target = document.getElementById('block-10593');
                                                    if (target) {
                                                        document.body.innerHTML = '';
                                                        document.body.appendChild(target);
                                                        document.body.style.margin = '0';
                                                        document.body.style.padding = '0';
                                                        target.style.width = '100%';
                                                        target.style.height = '100vh';
                                                    } else {
                                                        var style = document.createElement('style');
                                                        style.innerHTML = 'body > *:not(:has(#block-10593)) { display: none !important; }';
                                                        document.head.appendChild(style);
                                                    }
                                                })();
                                            \"\"\".trimIndent(), null)"""

new_js = """                                            view?.evaluateJavascript(\"\"\"
                                                (function() {
                                                    var target = document.getElementById('block-10593');
                                                    if (target) {
                                                        var node = target;
                                                        while (node && node !== document.body) {
                                                            var siblings = node.parentNode.children;
                                                            for (var i = 0; i < siblings.length; i++) {
                                                                if (siblings[i] !== node && siblings[i].tagName !== 'SCRIPT' && siblings[i].tagName !== 'STYLE') {
                                                                    siblings[i].style.display = 'none';
                                                                }
                                                            }
                                                            node = node.parentNode;
                                                        }
                                                        document.body.style.margin = '0';
                                                        document.body.style.padding = '0';
                                                    }
                                                })();
                                            \"\"\".trimIndent(), null)"""

content = content.replace(old_js, new_js)

with open("app/src/main/java/com/example/ui/screens/MainWeatherScreen.kt", "w") as f:
    f.write(content)
