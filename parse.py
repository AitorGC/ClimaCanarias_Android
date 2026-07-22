import re

with open("screen.kt.txt", "r") as f:
    lines = f.read()

# 1. Update tabs definition
old_tabs = """    val tabs = listOf("CLIMA", "PLAYA")
    val pagerState = rememberPagerState(pageCount = { tabs.size })"""
new_tabs = """    val tabs = listOf(
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
lines = lines.replace(old_tabs, new_tabs)

# 2. Add BottomNavigationBar to Scaffold
scaffold_start = """    Scaffold(
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
lines = lines.replace(scaffold_start, scaffold_new)

# 3. Modify DropdownMenu to remove Alertas, Estaciones, Satélite
dropdown_old = """                            DropdownMenuItem(
                                text = { Text("Estaciones") },
                                onClick = {
                                    showMainMenu = false
                                    showStationsModal = true
                                    viewModel.loadAemetStations()
                                },
                                leadingIcon = {
                                    Icon(Icons.Default.Place, contentDescription = null, modifier = Modifier.size(20.dp))
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Satélite") },
                                onClick = {
                                    showMainMenu = false
                                    showSatelliteModal = true
                                },
                                leadingIcon = {
                                    Icon(Icons.Default.Public, contentDescription = null, modifier = Modifier.size(20.dp))
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Alertas") },
                                onClick = {
                                    showMainMenu = false
                                    showNotificationsModal = true
                                    viewModel.loadWarnings()
                                },
                                leadingIcon = {
                                    Icon(Icons.Default.Notifications, contentDescription = null, modifier = Modifier.size(20.dp))
                                }
                            )"""
lines = lines.replace(dropdown_old, "")

# 4. Remove TabRow and adjust Column
tab_row_regex = re.compile(r"            // TabRow at the visual top.*?            HorizontalPager\(", re.DOTALL)
m = tab_row_regex.search(lines)
if m:
    lines = lines.replace(m.group(0), "            HorizontalPager(")

# Modify root Column
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
lines = lines.replace(column_old, column_new)

# 5. Extract modals body to pager
# showStationsModal:
# showNotificationsModal:
# showSatelliteModal:
# We need to find their contents and put them into the pager.

with open("screen.kt.patched.txt", "w") as f:
    f.write(lines)
