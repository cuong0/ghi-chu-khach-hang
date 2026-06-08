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
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.font.FontWeight

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

    // Live list of all leads
    val allLeads by viewModel.allLeads.collectAsState()
    
    // Tracks already notified leads in the current app session to avoid spamming
    var notifiedLeadsMap by remember { mutableStateOf(setOf<Int>()) }
    var currentAlertLead by remember { mutableStateOf<com.example.data.model.Lead?>(null) }

    // On-screen notification time checker (runs periodically every 4 seconds)
    LaunchedEffect(allLeads) {
        while (true) {
            val now = System.currentTimeMillis()
            // Find any lead whose re-contact date has reached or passed (within a reasonable 48 hour past window)
            val dueLead = allLeads.firstOrNull { lead ->
                lead.recontactDate <= now &&
                lead.recontactDate > (now - 48 * 60 * 60 * 1000) &&
                lead.id !in notifiedLeadsMap
            }
            if (dueLead != null) {
                currentAlertLead = dueLead
            }
            kotlinx.coroutines.delay(4000)
        }
    }

    // Gorgeous custom alert dialog for in-app popups
    if (currentAlertLead != null) {
        val context = androidx.compose.ui.platform.LocalContext.current
        val lead = currentAlertLead!!
        AlertDialog(
            onDismissRequest = {
                notifiedLeadsMap = notifiedLeadsMap + lead.id
                currentAlertLead = null
            },
            icon = {
                Icon(
                    imageVector = Icons.Default.Schedule,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp)
                )
            },
            title = {
                Text(
                    text = "Lịch hẹn tiếp xúc lại!",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            },
            text = {
                Column {
                    Text(
                        text = "Đến giờ hẹn liên hệ với khách hàng:",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = lead.name,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (lead.phone.isNotEmpty()) {
                        Text(
                            text = "SĐT: ${lead.phone}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    if (lead.notes.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Nội dung: ${lead.notes}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 3,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        notifiedLeadsMap = notifiedLeadsMap + lead.id
                        currentAlertLead = null
                        try {
                            val intent = Intent(Intent.ACTION_DIAL).apply {
                                data = Uri.parse("tel:${lead.phone}")
                            }
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            // Fallback in case of device limitations
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Icon(Icons.Default.Call, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Gọi điện", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        notifiedLeadsMap = notifiedLeadsMap + lead.id
                        currentAlertLead = null
                    }
                ) {
                    Text("Đóng", fontWeight = FontWeight.Medium)
                }
            }
        )
    }

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
