with open("app/src/main/java/com/example/ui/screens/MainWeatherScreen.kt", "r") as f:
    s = f.read()

import re
old_code = """    val pagerState = rememberPagerState(pageCount = { tabs.size })
    
    LaunchedEffect(pagerState.currentPage) {
        when (pagerState.currentPage) {
            2 -> viewModel.loadWarnings()
            3 -> viewModel.loadAemetStations()
        }
    }"""

new_code = """    val pagerState = rememberPagerState(pageCount = { tabs.size })
    
    val warningsState by viewModel.warningsUiState.collectAsStateWithLifecycle()
    var initialAlertPopupChecked by rememberSaveable { mutableStateOf(false) }
    var showInitialAlertPopup by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(pagerState.currentPage) {
        when (pagerState.currentPage) {
            2 -> viewModel.loadWarnings()
            3 -> viewModel.loadAemetStations()
        }
    }

    LaunchedEffect(Unit) {
        if (selectedIslands.isNotEmpty()) {
            viewModel.loadWarnings()
        }
    }

    LaunchedEffect(warningsState, selectedIslands) {
        if (!initialAlertPopupChecked && selectedIslands.isNotEmpty() && warningsState is com.example.viewmodel.WarningsUiState.Success) {
            val warnings = (warningsState as com.example.viewmodel.WarningsUiState.Success).warnings
            val hasAlerts = warnings.any { warning ->
                selectedIslands.any { island ->
                    warning.ambitoGeografico?.contains(island, ignoreCase = true) == true
                }
            }
            if (hasAlerts) {
                showInitialAlertPopup = true
            }
            initialAlertPopupChecked = true
        }
    }

    if (showInitialAlertPopup) {
        AlertDialog(
            onDismissRequest = { showInitialAlertPopup = false },
            containerColor = if (isDarkTheme) Color(0xFF1E1C24) else Color.White,
            titleContentColor = if (isDarkTheme) Color.White else Color.Black,
            textContentColor = if (isDarkTheme) Color.LightGray else Color.DarkGray,
            title = { Text("Alerta Meteorológica") },
            text = { Text("Existe una alerta meteorológica vigente en las islas que has seleccionado.") },
            confirmButton = {
                Button(
                    onClick = {
                        showInitialAlertPopup = false
                        coroutineScope.launch { pagerState.animateScrollToPage(2) }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isDarkTheme) Color(0xFFFFD600) else Color(0xFF004993),
                        contentColor = if (isDarkTheme) Color.Black else Color.White
                    )
                ) {
                    Text("Aceptar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showInitialAlertPopup = false }) {
                    Text("Cerrar", color = if (isDarkTheme) Color.White else Color.Black)
                }
            }
        )
    }"""

if old_code in s:
    s = s.replace(old_code, new_code)
    with open("app/src/main/java/com/example/ui/screens/MainWeatherScreen.kt", "w") as f:
        f.write(s)
    print("Replaced!")
else:
    print("Not found")

