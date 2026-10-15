package com.example.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.entity.ProductEntity
import com.example.ui.screens.contacts.ContactsAndReportsScreen
import com.example.ui.screens.dashboard.DashboardScreen
import com.example.ui.screens.inventory.InventoryScreen
import com.example.ui.screens.pos.PosScreen
import com.example.ui.screens.profit.ProfitLossScreen
import com.example.ui.screens.purchase.PurchaseScreen
import com.example.ui.screens.transfer.StockTransferScreen
import com.example.ui.screens.watchlist.PriceWatchlistScreen
import com.example.ui.theme.BrandEmerald
import com.example.ui.theme.StockVisionTheme

enum class NavDestination(
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val testTag: String
) {
    DASHBOARD("Dashboard", Icons.Filled.Dashboard, Icons.Outlined.Dashboard, "nav_dashboard"),
    INVENTORY("Inventory", Icons.Filled.Inventory2, Icons.Outlined.Inventory2, "nav_inventory"),
    WATCHLIST("Demat Price", Icons.Filled.ShowChart, Icons.Outlined.ShowChart, "nav_watchlist"),
    POS("POS Billing", Icons.Filled.PointOfSale, Icons.Outlined.PointOfSale, "nav_pos"),
    FINANCE("P&L Ledger", Icons.Filled.AccountBalance, Icons.Outlined.AccountBalance, "nav_finance"),
    PURCHASES("Purchases", Icons.Filled.AddBusiness, Icons.Outlined.AddBusiness, "nav_purchases"),
    TRANSFERS("Transfers", Icons.Filled.SwapHoriz, Icons.Outlined.SwapHoriz, "nav_transfers"),
    CONTACTS("Directory", Icons.Filled.Groups, Icons.Outlined.Groups, "nav_contacts")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StockVisionApp(
    viewModel: StockVisionViewModel = viewModel()
) {
    var isDarkTheme by remember { mutableStateOf(true) }
    var currentScreen by remember { mutableStateOf(NavDestination.DASHBOARD) }
    var showMoreMenuSheet by remember { mutableStateOf(false) }

    val snackMessage by viewModel.snackMessage.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val currentRole by viewModel.currentUserRole.collectAsStateWithLifecycle()

    LaunchedEffect(snackMessage) {
        snackMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearSnackMessage()
        }
    }

    StockVisionTheme(darkTheme = isDarkTheme) {
        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .statusBarsPadding()
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .background(BrandEmerald, RoundedCornerShape(8.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CandlestickChart,
                                    contentDescription = "StockVision",
                                    tint = MaterialTheme.colorScheme.surface,
                                    modifier = Modifier.size(22.dp)
                                )
                            }

                            Column {
                                Text(
                                    text = "StockVision",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Black,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Business & Demat Terminal",
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            // Dark/Light Theme Switcher
                            IconButton(onClick = { isDarkTheme = !isDarkTheme }) {
                                Icon(
                                    imageVector = if (isDarkTheme) Icons.Default.LightMode else Icons.Default.DarkMode,
                                    contentDescription = "Toggle Theme"
                                )
                            }

                            // Quick More Menu (Purchases, Transfers, Contacts)
                            IconButton(onClick = { showMoreMenuSheet = true }) {
                                Icon(
                                    imageVector = Icons.Default.Apps,
                                    contentDescription = "Modules"
                                )
                            }
                        }
                    }
                }
            },
            bottomBar = {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 6.dp
                ) {
                    val mainTabs = listOf(
                        NavDestination.DASHBOARD,
                        NavDestination.INVENTORY,
                        NavDestination.WATCHLIST,
                        NavDestination.POS,
                        NavDestination.FINANCE
                    )

                    mainTabs.forEach { tab ->
                        val isSelected = currentScreen == tab
                        NavigationBarItem(
                            selected = isSelected,
                            onClick = { currentScreen = tab },
                            icon = {
                                Icon(
                                    imageVector = if (isSelected) tab.selectedIcon else tab.unselectedIcon,
                                    contentDescription = tab.title
                                )
                            },
                            label = { Text(tab.title, fontSize = 10.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                            modifier = Modifier.testTag(tab.testTag)
                        )
                    }
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                AnimatedContent(
                    targetState = currentScreen,
                    transitionSpec = { fadeIn() togetherWith fadeOut() },
                    label = "ScreenTransition"
                ) { screen ->
                    when (screen) {
                        NavDestination.DASHBOARD -> DashboardScreen(
                            viewModel = viewModel,
                            onNavigateToPos = { currentScreen = NavDestination.POS },
                            onNavigateToPurchases = { currentScreen = NavDestination.PURCHASES },
                            onNavigateToTransfers = { currentScreen = NavDestination.TRANSFERS },
                            onNavigateToInventory = { currentScreen = NavDestination.INVENTORY },
                            onNavigateToProfitLoss = { currentScreen = NavDestination.FINANCE },
                            onSelectProduct = { currentScreen = NavDestination.WATCHLIST }
                        )
                        NavDestination.INVENTORY -> InventoryScreen(
                            viewModel = viewModel,
                            onProductClick = { currentScreen = NavDestination.WATCHLIST }
                        )
                        NavDestination.WATCHLIST -> PriceWatchlistScreen(
                            viewModel = viewModel
                        )
                        NavDestination.POS -> PosScreen(
                            viewModel = viewModel
                        )
                        NavDestination.FINANCE -> ProfitLossScreen(
                            viewModel = viewModel
                        )
                        NavDestination.PURCHASES -> PurchaseScreen(
                            viewModel = viewModel
                        )
                        NavDestination.TRANSFERS -> StockTransferScreen(
                            viewModel = viewModel
                        )
                        NavDestination.CONTACTS -> ContactsAndReportsScreen(
                            viewModel = viewModel
                        )
                    }
                }
            }
        }

        // More Modules Bottom Sheet
        if (showMoreMenuSheet) {
            ModalBottomSheet(
                onDismissRequest = { showMoreMenuSheet = false }
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Business Management Modules",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    val modules = listOf(
                        NavDestination.PURCHASES to "Inward Purchases & Supplier Bills",
                        NavDestination.TRANSFERS to "Inter-Location Stock Transfers",
                        NavDestination.CONTACTS to "Customers, Suppliers & Audit Reports",
                        NavDestination.FINANCE to "Profit & Loss Ledger & Valuation",
                        NavDestination.INVENTORY to "Master Inventory & Godown Levels"
                    )

                    modules.forEach { (dest, desc) ->
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            onClick = {
                                currentScreen = dest
                                showMoreMenuSheet = false
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(8.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(dest.selectedIcon, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimaryContainer)
                                }
                                Column {
                                    Text(dest.title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Text(desc, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}
