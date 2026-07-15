import re

with open("app/src/main/java/com/example/ui/screens/MainWeatherScreen.kt", "r") as f:
    content = f.read()

content = content.replace("onAddFavorite = { name, lat, lng -> viewModel.addCustomFavorite(name, lat, lng) },", "onSearchRegion = { query, callback -> viewModel.searchAndAddLocation(query, callback) },")
content = content.replace("viewModel.addCustomFavorite(\n                                                                        preset.first,\n                                                                        preset.second,\n                                                                        preset.third\n                                                                    )", "viewModel.addCustomFavorite(preset.first, preset.second, preset.third)")

with open("app/src/main/java/com/example/ui/screens/MainWeatherScreen.kt", "w") as f:
    f.write(content)

