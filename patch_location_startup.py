import re

with open("app/src/main/java/com/example/ui/screens/MainWeatherScreen.kt", "r") as f:
    content = f.read()

target1 = """    var permissionRequestedOnce by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        if (!locationPermissionState.allPermissionsGranted && !permissionRequestedOnce) {
            locationPermissionState.launchMultiplePermissionRequest()
            permissionRequestedOnce = true
        }
    }

    LaunchedEffect(locationPermissionState.allPermissionsGranted) {
        if (locationPermissionState.allPermissionsGranted) {
            viewModel.fetchCurrentLocation(context)
        }
    }"""

replacement1 = """    var wantsLocation by remember { mutableStateOf(false) }

    LaunchedEffect(locationPermissionState.allPermissionsGranted) {
        if (locationPermissionState.allPermissionsGranted && wantsLocation) {
            viewModel.fetchCurrentLocation(context)
            wantsLocation = false
        }
    }"""

if target1 in content:
    content = content.replace(target1, replacement1)
    print("Replaced chunk 1")
else:
    print("Chunk 1 not found")

target2 = """                            onDetectLocation = {
                                if (locationPermissionState.allPermissionsGranted) {
                                    viewModel.fetchCurrentLocation(context)
                                } else {
                                    locationPermissionState.launchMultiplePermissionRequest()
                                }
                            }"""

replacement2 = """                            onDetectLocation = {
                                if (locationPermissionState.allPermissionsGranted) {
                                    viewModel.fetchCurrentLocation(context)
                                } else {
                                    wantsLocation = true
                                    locationPermissionState.launchMultiplePermissionRequest()
                                }
                            }"""

if target2 in content:
    content = content.replace(target2, replacement2)
    print("Replaced chunk 2")
else:
    print("Chunk 2 not found")

with open("app/src/main/java/com/example/ui/screens/MainWeatherScreen.kt", "w") as f:
    f.write(content)

