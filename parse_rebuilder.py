import re

with open("screen.kt.txt", "r") as f:
    orig = f.read()

# 1. Update tabs definition
tabs_old = """    val tabs = listOf("CLIMA", "PLAYA")
    val pagerState = rememberPagerState(pageCount = { tabs.size })"""
tabs_new = """    val tabs = listOf(
        "Clima" to Icons.Default.WbSunny,
        "Playa" to Icons.Default.BeachAccess,
        "Alertas" to Icons.Default.Notifications,
        "Estaciones" to Icons.Default.Place,
        "Satélite" to Icons.Default.Public
    )
    val pagerState = rememberPagerState(pageCount = { tabs.size })
    
    // Load data when changing pages
    LaunchedEffect(pagerState.currentPage) {
        when (pagerState.currentPage) {
            2 -> viewModel.loadWarnings()
            3 -> viewModel.loadAemetStations()
        }
    }"""
orig = orig.replace(tabs_old, tabs_new)

# Remove modal state variables (we keep showFavoritesModal and showSyncModal)
orig = re.sub(r"\s*var showStationsModal by remember \{ mutableStateOf\(false\) \}", "", orig)
orig = re.sub(r"\s*var showNotificationsModal by remember \{ mutableStateOf\(false\) \}", "", orig)
orig = re.sub(r"\s*var showSatelliteModal by remember \{ mutableStateOf\(false\) \}", "", orig)


# Change Scaffold to include bottomBar and remove DropdownMenuItems
scaffold_old = """    Scaffold(
        topBar = {"""
scaffold_new = """    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = if (isDarkTheme) Color(0xFF141318) else Color.White,
                contentColor = if (isDarkTheme) primaryCanaryYellow else Color(0xFF004993)
            ) {
                tabs.forEachIndexed { index, (title, icon) ->
                    NavigationBarItem(
                        icon = { Icon(icon, contentDescription = title) },
                        label = { Text(title, fontSize = 10.sp, maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis) },
                        selected = pagerState.currentPage == index,
                        onClick = {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(index)
                            }
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = if (isDarkTheme) Color.Black else Color.White,
                            selectedTextColor = if (isDarkTheme) primaryCanaryYellow else Color(0xFF004993),
                            indicatorColor = if (isDarkTheme) primaryCanaryYellow else Color(0xFF004993),
                            unselectedIconColor = if (isDarkTheme) Color.LightGray else Color.DarkGray,
                            unselectedTextColor = if (isDarkTheme) Color.LightGray else Color.DarkGray
                        )
                    )
                }
            }
        },
        topBar = {"""
orig = orig.replace(scaffold_old, scaffold_new)

# Clean up dropdown menu
orig = re.sub(r"""                            DropdownMenuItem\(
                                text = \{ Text\("Estaciones"\) \},
                                onClick = \{
                                    showMainMenu = false
                                    showStationsModal = true
                                    viewModel\.loadAemetStations\(\)
                                \},
                                leadingIcon = \{
                                    Icon\(Icons\.Default\.Place, contentDescription = null, modifier = Modifier\.size\(20\.dp\)\)
                                \}
                            \)""", "", orig)

orig = re.sub(r"""                            DropdownMenuItem\(
                                text = \{ Text\("Satélite"\) \},
                                onClick = \{
                                    showMainMenu = false
                                    showSatelliteModal = true
                                \},
                                leadingIcon = \{
                                    Icon\(Icons\.Default\.Public, contentDescription = null, modifier = Modifier\.size\(20\.dp\)\)
                                \}
                            \)""", "", orig)

orig = re.sub(r"""                            DropdownMenuItem\(
                                text = \{ Text\("Alertas"\) \},
                                onClick = \{
                                    showMainMenu = false
                                    showNotificationsModal = true
                                    viewModel\.loadWarnings\(\)
                                \},
                                leadingIcon = \{
                                    Icon\(Icons\.Default\.Notifications, contentDescription = null, modifier = Modifier\.size\(20\.dp\)\)
                                \}
                            \)""", "", orig)


# Remove TabRow
orig = re.sub(r"            // TabRow at the visual top.*?            HorizontalPager\(", "            HorizontalPager(", orig, flags=re.DOTALL)

# Modify root column to remove verticalScroll and padding
column_old = """    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {"""
column_new = """    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {"""
orig = orig.replace(column_old, column_new)

# Modify page 0 inner Column to add scrolling
page0_old = """                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (page == 0) {"""
page0_new = """                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)
                ) {
                    if (page == 0) {"""
orig = orig.replace(page0_old, page0_new)

# Find `} else {` for the end of `if (page == 0)` and change it to `} else if (page == 1) {`
def get_block(s, start):
    idx = s.find("{", start)
    if idx == -1: return -1
    count = 1
    idx += 1
    while count > 0 and idx < len(s):
        if s[idx] == "{": count += 1
        elif s[idx] == "}": count -= 1
        idx += 1
    return idx

idx_if_page0 = orig.find("if (page == 0) {")
if idx_if_page0 != -1:
    idx_end = get_block(orig, idx_if_page0)
    # idx_end is the index of `}` closing the page == 0 block.
    # The characters after it should be ` else {`
    else_str = " else {"
    if orig[idx_end:idx_end+len(else_str)] == else_str:
        orig = orig[:idx_end] + " else if (page == 1) {" + orig[idx_end+len(else_str):]
        
        # Now find the end of the `page == 1` block
        idx_page1_end = get_block(orig, idx_end)
        
        # Insert pages 2, 3, 4
        pages_ext = """
                    } else if (page == 2) {
                        val warningsState by viewModel.warningsUiState.collectAsStateWithLifecycle()
                        val islasCanarias = listOf(
                            "El Hierro",
                            "Fuerteventura",
                            "Gran Canaria",
                            "La Gomera",
                            "La Palma",
                            "Lanzarote",
                            "Tenerife"
                        )
                        when (val state = warningsState) {
                            is WarningsUiState.Idle, is WarningsUiState.Loading -> {
                                Box(modifier = Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
                                    CircularProgressIndicator(color = primaryCanaryYellow)
                                }
                            }
                            is WarningsUiState.Error -> {
                                Box(modifier = Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
                                    Text(
                                        text = state.message,
                                        color = MaterialTheme.colorScheme.error,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                            is WarningsUiState.Success -> {
                                val warnings = state.warnings
                                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                                    islasCanarias.forEach { isla ->
                                        val alertasIsla = warnings.filter { it.ambitoGeografico?.contains(isla, ignoreCase = true) == true }
                                        Column(modifier = Modifier.fillMaxWidth()) {
                                            Text(
                                                text = isla,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 18.sp,
                                                color = if (isDarkTheme) primaryCanaryYellow else Color(0xFF004993),
                                                modifier = Modifier.padding(bottom = 8.dp)
                                            )
                                            if (alertasIsla.isEmpty()) {
                                                Text(
                                                    text = "No hay alertas",
                                                    color = if (isDarkTheme) Color.LightGray else Color.DarkGray,
                                                    fontSize = 14.sp,
                                                    modifier = Modifier.padding(start = 8.dp)
                                                )
                                            } else {
                                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                                    alertasIsla.forEach { warning ->
                                                        val warningColor = when (warning.nivel?.lowercase()) {
                                                            "rojo" -> Color(0xFFD32F2F)
                                                            "naranja" -> Color(0xFFF57C00)
                                                            "amarillo" -> Color(0xFFFBC02D)
                                                            else -> if (isDarkTheme) Color.White else Color.Black
                                                        }
                                                        Card(
                                                            colors = CardDefaults.cardColors(containerColor = cardBackgroundColor),
                                                            modifier = Modifier.fillMaxWidth()
                                                        ) {
                                                            Column(modifier = Modifier.padding(12.dp)) {
                                                                Text(
                                                                    text = warning.ambitoGeografico ?: "Zona no especificada",
                                                                    fontWeight = FontWeight.SemiBold,
                                                                    fontSize = 14.sp,
                                                                    color = onSurfaceColor
                                                                )
                                                                Spacer(modifier = Modifier.height(4.dp))
                                                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                                                    Icon(Icons.Default.Warning, contentDescription = null, tint = warningColor, modifier = Modifier.size(20.dp))
                                                                    Text(
                                                                        text = warning.fenomeno ?: "Aviso",
                                                                        fontWeight = FontWeight.Bold,
                                                                        fontSize = 14.sp,
                                                                        color = warningColor
                                                                    )
                                                                }
                                                                if (!warning.descripcion.isNullOrEmpty()) {
                                                                    Spacer(modifier = Modifier.height(4.dp))
                                                                    Text(
                                                                        text = warning.descripcion,
                                                                        fontSize = 13.sp,
                                                                        color = onSurfaceColor.copy(alpha = 0.8f)
                                                                    )
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    } else if (page == 3) {
                        val stationsState by viewModel.aemetStationsUiState.collectAsStateWithLifecycle()
                        var searchQuery by remember { mutableStateOf("") }
                        var expandedStationId by remember { mutableStateOf<String?>(null) }
                        
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                            placeholder = { Text("Buscar estación o provincia...") },
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp)
                        )
                        
                        when (val state = stationsState) {
                            is com.example.viewmodel.AemetStationsUiState.Idle -> {
                                Box(modifier = Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
                                    Text("Inicializando...", color = Color.Gray)
                                }
                            }
                            is com.example.viewmodel.AemetStationsUiState.Loading -> {
                                Box(modifier = Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                        CircularProgressIndicator(color = if (isDarkTheme) primaryCanaryYellow else Color(0xFF004993))
                                        Text("Extrayendo estaciones de AEMET OpenData...", fontSize = 14.sp, color = Color.Gray, textAlign = TextAlign.Center)
                                    }
                                }
                            }
                            is com.example.viewmodel.AemetStationsUiState.Error -> {
                                Box(modifier = Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                        Icon(Icons.Default.Warning, contentDescription = "Error", tint = Color.Red, modifier = Modifier.size(48.dp))
                                        Text(state.message, color = Color.Red, fontSize = 14.sp, textAlign = TextAlign.Center)
                                        Button(
                                            onClick = { viewModel.loadAemetStations() },
                                            colors = ButtonDefaults.buttonColors(containerColor = if (isDarkTheme) primaryCanaryYellow else Color(0xFF004993))
                                        ) {
                                            Text("Reintentar", color = if (isDarkTheme) Color.Black else Color.White)
                                        }
                                    }
                                }
                            }
                            is com.example.viewmodel.AemetStationsUiState.Success -> {
                                val filteredStations = state.stations.filter {
                                    it.nombre.contains(searchQuery, ignoreCase = true) ||
                                    it.provincia.contains(searchQuery, ignoreCase = true)
                                }
                                if (filteredStations.isEmpty()) {
                                    Text("No se encontraron estaciones", modifier = Modifier.padding(16.dp))
                                } else {
                                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        filteredStations.forEach { station ->
                                            val isExpanded = expandedStationId == station.indicativo
                                            Card(
                                                modifier = Modifier.fillMaxWidth().clickable {
                                                    expandedStationId = if (isExpanded) null else station.indicativo
                                                    if (!isExpanded) viewModel.loadAemetStationObservation(station.indicativo)
                                                },
                                                colors = CardDefaults.cardColors(containerColor = cardBackgroundColor)
                                            ) {
                                                Column(modifier = Modifier.padding(16.dp)) {
                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        Column(modifier = Modifier.weight(1f)) {
                                                            Text(station.nombre, fontWeight = FontWeight.Bold, color = onSurfaceColor)
                                                            Text("${station.provincia} • Altitud: ${station.altitud}m", fontSize = 12.sp, color = Color.Gray)
                                                        }
                                                        Icon(
                                                            imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                                            contentDescription = null,
                                                            tint = onSurfaceColor
                                                        )
                                                    }
                                                    if (isExpanded) {
                                                        Spacer(modifier = Modifier.height(16.dp))
                                                        val obsState by viewModel.aemetObservationUiState.collectAsStateWithLifecycle()
                                                        if (obsState.stationId != station.indicativo) {
                                                            CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally), color = primaryCanaryYellow)
                                                        } else {
                                                            when (val obs = obsState.state) {
                                                                is com.example.viewmodel.AemetObservationUiState.Loading -> {
                                                                    CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally), color = primaryCanaryYellow)
                                                                }
                                                                is com.example.viewmodel.AemetObservationUiState.Error -> {
                                                                    Text("Error: ${obs.message}", color = Color.Red, fontSize = 12.sp)
                                                                }
                                                                is com.example.viewmodel.AemetObservationUiState.Success -> {
                                                                    val data = obs.observation
                                                                    if (data == null) {
                                                                        Text("No hay datos recientes", fontSize = 12.sp)
                                                                    } else {
                                                                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                                                            Column {
                                                                                Text("Temperatura", fontSize = 12.sp, color = Color.Gray)
                                                                                Text("${data.ta ?: "--"}°C", fontWeight = FontWeight.Bold, color = onSurfaceColor)
                                                                            }
                                                                            Column {
                                                                                Text("Humedad", fontSize = 12.sp, color = Color.Gray)
                                                                                Text("${data.hr ?: "--"}%", fontWeight = FontWeight.Bold, color = onSurfaceColor)
                                                                            }
                                                                            Column {
                                                                                Text("Viento", fontSize = 12.sp, color = Color.Gray)
                                                                                Text("${data.vv ?: "--"} m/s", fontWeight = FontWeight.Bold, color = onSurfaceColor)
                                                                            }
                                                                        }
                                                                    }
                                                                }
                                                                else -> {}
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    } else if (page == 4) {
                        var isSatelliteLoading by remember { mutableStateOf(true) }
                        Box(modifier = Modifier.fillMaxWidth().height(500.dp)) {
                            androidx.compose.ui.viewinterop.AndroidView(
                                factory = { context ->
                                    android.webkit.WebView(context).apply {
                                        settings.javaScriptEnabled = true
                                        settings.domStorageEnabled = true
                                        webViewClient = object : android.webkit.WebViewClient() {
                                            override fun onPageFinished(view: android.webkit.WebView?, url: String?) {
                                                super.onPageFinished(view, url)
                                                isSatelliteLoading = false
                                                view?.evaluateJavascript(\"\"\"
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
                                                \"\"\".trimIndent(), null)
                                            }
                                        }
                                        loadUrl("https://www.sat24.com/es-es/region/8000076")
                                    }
                                },
                                modifier = Modifier.fillMaxSize()
                            )
                            if (isSatelliteLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.align(Alignment.Center),
                                    color = primaryCanaryYellow
                                )
                            }
                        }
                    }"""
        orig = orig[:idx_page1_end-1] + pages_ext + orig[idx_page1_end-1:]


# We need to correctly remove the modals from the bottom of the file.
# The modals are: showSatelliteModal, showNotificationsModal, showStationsModal

def remove_modal(s, marker):
    start = s.find(marker)
    if start == -1: return s
    end = get_block(s, start)
    if end != -1:
        # Also remove trailing newlines
        return s[:start].rstrip() + "\n" + s[end:]
    return s

orig = remove_modal(orig, "    if (showSatelliteModal) {")
orig = remove_modal(orig, "    if (showNotificationsModal) {")
orig = remove_modal(orig, "    if (showStationsModal) {")

with open("app/src/main/java/com/example/ui/screens/MainWeatherScreen.kt", "w") as f:
    f.write(orig)

