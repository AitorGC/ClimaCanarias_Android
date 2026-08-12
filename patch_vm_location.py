with open("app/src/main/java/com/example/viewmodel/WeatherViewModel.kt", "r") as f:
    content = f.read()

target = """                            } else {
                                Log.e("WeatherViewModel", "Current location is null")
                            }
                        }.addOnFailureListener { e ->
                            Log.e("WeatherViewModel", "Error getting current location", e)
                        }
                    }
                }.addOnFailureListener { e ->
                    Log.e("WeatherViewModel", "Error getting last location", e)
                }
            } catch (e: Exception) {
                Log.e("WeatherViewModel", "Exception while fetching location", e)
            }"""

replacement = """                            } else {
                                Log.e("WeatherViewModel", "Current location is null")
                                android.os.Handler(android.os.Looper.getMainLooper()).post { android.widget.Toast.makeText(context, "No se pudo obtener la ubicación actual. Comprueba el GPS.", android.widget.Toast.LENGTH_LONG).show() }
                            }
                        }.addOnFailureListener { e ->
                            Log.e("WeatherViewModel", "Error getting current location", e)
                            android.os.Handler(android.os.Looper.getMainLooper()).post { android.widget.Toast.makeText(context, "Error al obtener la ubicación.", android.widget.Toast.LENGTH_LONG).show() }
                        }
                    }
                }.addOnFailureListener { e ->
                    Log.e("WeatherViewModel", "Error getting last location", e)
                }
            } catch (e: Exception) {
                Log.e("WeatherViewModel", "Exception while fetching location", e)
            }"""

if target in content:
    content = content.replace(target, replacement)
    with open("app/src/main/java/com/example/viewmodel/WeatherViewModel.kt", "w") as f:
        f.write(content)
    print("Location patched!")
else:
    print("Not found!")
