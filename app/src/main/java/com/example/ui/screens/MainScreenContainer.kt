package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Notes
import androidx.compose.material.icons.automirrored.outlined.Notes
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.List
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import com.example.ui.viewmodel.LeadViewModel

enum class NavigationTab(
    val route: String,
    val title: String,
    val filledIcon: ImageVector,
    val outlinedIcon: ImageVector
) {
    NOTES("notes", "Ghi chú", Icons.AutoMirrored.Filled.Notes, Icons.AutoMirrored.Outlined.Notes),
    LIST("list", "Danh sách", Icons.Default.List, Icons.Outlined.List),
    SETTINGS("settings", "Cài đặt", Icons.Default.Settings, Icons.Outlined.Settings),
    PROFILE("profile", "Cá nhân", Icons.Default.Person, Icons.Outlined.Person)
}

enum class ScreenRoute {
    MAIN_TABS,
    ADD_CUSTOMER
}

@Composable
fun MainScreenContainer(
    viewModel: LeadViewModel
) {
    var currentScreen by remember { mutableStateOf(ScreenRoute.MAIN_TABS) }
    var activeTab by remember { mutableStateOf(NavigationTab.NOTES) }

    AnimatedContent(
        targetState = currentScreen,
        transitionSpec = {
            if (targetState == ScreenRoute.ADD_CUSTOMER) {
                slideInHorizontally { width -> width } + fadeIn() togetherWith
                        slideOutHorizontally { width -> -width } + fadeOut()
            } else {
                slideInHorizontally { width -> -width } + fadeIn() togetherWith
                        slideOutHorizontally { width -> width } + fadeOut()
            }
        },
        label = "ScreenTransition"
    ) { screen ->
        when (screen) {
            ScreenRoute.MAIN_TABS -> {
                Scaffold(
                    bottomBar = {
                        NavigationBar(
                            modifier = Modifier.testTag("bottom_nav_bar")
                        ) {
                            NavigationTab.values().forEach { tab ->
                                val selected = activeTab == tab
                                NavigationBarItem(
                                    selected = selected,
                                    onClick = { activeTab = tab },
                                    label = { Text(tab.title) },
                                    icon = {
                                        Icon(
                                            imageVector = if (selected) tab.filledIcon else tab.outlinedIcon,
                                            contentDescription = tab.title
                                        )
                                    },
                                    modifier = Modifier.testTag("nav_tab_${tab.route}")
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
                        when (activeTab) {
                            NavigationTab.NOTES -> NotesTab(
                                viewModel = viewModel,
                                onNavigateToAddCustomer = { currentScreen = ScreenRoute.ADD_CUSTOMER }
                            )
                            NavigationTab.LIST -> ListTab(viewModel = viewModel)
                            NavigationTab.SETTINGS -> SettingsTab(viewModel = viewModel)
                            NavigationTab.PROFILE -> ProfileTab(viewModel = viewModel)
                        }
                    }
                }
            }
            ScreenRoute.ADD_CUSTOMER -> {
                AddLeadScreen(
                    viewModel = viewModel,
                    onNavigateBack = { currentScreen = ScreenRoute.MAIN_TABS }
                )
            }
        }
    }
}
