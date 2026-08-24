import re

with open("app/src/main/java/com/example/viewmodel/WeatherViewModel.kt", "r") as f:
    content = f.read()

target_init = """            favorites.filter { it.isNotEmpty() }.first().let { list ->
                if (_selectedCity.value == null) {
                    val defaultCity = list.find { it.name.contains("Las Palmas") } ?: list.first()
                    selectCity(defaultCity)
                }
            }"""

replacement_init = """            favorites.filter { it.isNotEmpty() }.first().let { list ->
                if (_selectedCity.value == null) {
                    val lastSelectedCityName = sharedPrefs.getString("last_selected_city", null)
                    val defaultCity = if (lastSelectedCityName != null) {
                        list.find { it.name == lastSelectedCityName } ?: list.first()
                    } else {
                        list.find { it.name.contains("Las Palmas") } ?: list.first()
                    }
                    selectCity(defaultCity)
                }
            }"""

if target_init in content:
    content = content.replace(target_init, replacement_init)
    print("Replaced init")
else:
    print("Init not found")

target_select = """    fun selectCity(city: FavoriteCity) {
        _selectedCity.value = city
        fetchWeatherForCity(city)
    }"""

replacement_select = """    fun selectCity(city: FavoriteCity) {
        _selectedCity.value = city
        if (city.name != "Ubicación Actual") {
            sharedPrefs.edit().putString("last_selected_city", city.name).apply()
        }
        fetchWeatherForCity(city)
    }"""

if target_select in content:
    content = content.replace(target_select, replacement_select)
    print("Replaced select")
else:
    print("Select not found")

with open("app/src/main/java/com/example/viewmodel/WeatherViewModel.kt", "w") as f:
    f.write(content)
