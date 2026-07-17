import re

with open("app/src/main/java/com/example/ui/screens/MainWeatherScreen.kt", "r") as f:
    content = f.read()

old_js = """                                            view?.evaluateJavascript(\"\"\"
                                                (function() {
                                                    var target = document.querySelector('.js-static-satellite') || document.querySelector('[data-component="Snippetsatellite"]') || document.getElementById('block-10593');
                                                    if (target) {
                                                        var node = target;
                                                        while (node && node !== document.body) {
                                                            if (node.classList) {
                                                                node.classList.remove('hide-mobile');
                                                                node.classList.remove('hide-tablet');
                                                                node.classList.remove('hidden');
                                                            }
                                                            node.style.display = 'block';
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

new_js = """                                            view?.evaluateJavascript(\"\"\"
                                                (function() {
                                                    var target = document.getElementById('block-10593');
                                                    if (target) {
                                                        var node = target;
                                                        while (node && node !== document.body) {
                                                            if (node.classList) {
                                                                node.classList.remove('hide-mobile');
                                                                node.classList.remove('hide-tablet');
                                                                node.classList.remove('hidden');
                                                            }
                                                            node.style.display = 'block';
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
