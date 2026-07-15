import re

with open("app/src/main/java/com/example/viewmodel/WeatherViewModel.kt", "r") as f:
    content = f.read()

new_method = """    fun searchAndAddLocation(query: String, onResult: (String?) -> Unit) {
        viewModelScope.launch {
            try {
                val url = "https://geocoding-api.open-meteo.com/v1/search?name=${query}&count=5&language=es&format=json"
                val response = com.example.data.WeatherApiClient.api.searchLocation(url)
                val results = response.results
                if (results != null && results.isNotEmpty()) {
                    val first = results[0]
                    addCustomFavorite(first.name, first.latitude, first.longitude)
                    onResult(null) // Success
                } else {
                    onResult("No se encontraron resultados para '$query'")
                }
            } catch (e: Exception) {
                onResult("Error al buscar: ${e.message}")
            }
        }
    }

"""

content = content.replace("fun addCustomFavorite", new_method + "fun addCustomFavorite")

with open("app/src/main/java/com/example/viewmodel/WeatherViewModel.kt", "w") as f:
    f.write(content)
