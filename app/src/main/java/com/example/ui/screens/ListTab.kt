package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.LocationCity
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Lead
import com.example.ui.viewmodel.LeadViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListTab(
    viewModel: LeadViewModel
) {
    val leads by viewModel.allLeads.collectAsState()
    val filteredLeads by viewModel.filteredLeads.collectAsState()
    val areas by viewModel.distinctAreas.collectAsState()
    val selectedArea by viewModel.selectedArea.collectAsState()

    var dropdownExpanded by remember { mutableStateOf(false) }
    var leadToDelete by remember { mutableStateOf<Lead?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Danh sách phân loại",
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = (-0.5).sp
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Prominent Area Selector Header
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                tonalElevation = 1.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.FilterList,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                "Chọn khu vực lọc",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = selectedArea ?: "Tất cả khu vực (Phần loại nhóm)",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    Box {
                        Button(
                            onClick = { dropdownExpanded = true },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            ),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Chọn lọc", fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                        }

                        DropdownMenu(
                            expanded = dropdownExpanded,
                            onDismissRequest = { dropdownExpanded = false },
                            modifier = Modifier.background(MaterialTheme.colorScheme.surface)
                        ) {
                            DropdownMenuItem(
                                text = { Text("Tất cả khu vực", fontWeight = FontWeight.Bold) },
                                onClick = {
                                    viewModel.selectArea(null)
                                    dropdownExpanded = false
                                }
                            )
                            Divider(color = MaterialTheme.colorScheme.outlineVariant)
                            areas.forEach { areaName ->
                                DropdownMenuItem(
                                    text = { Text(areaName) },
                                    onClick = {
                                        viewModel.selectArea(areaName)
                                        dropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // Body Display
            Box(modifier = Modifier.fillMaxSize()) {
                if (leads.isEmpty()) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationCity,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                            modifier = Modifier.size(80.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "Không có dữ liệu!",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                } else {
                    if (selectedArea == null) {
                        // Display Leads grouped by Area headers
                        val groupedLeads = leads.groupBy { it.area }

                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            groupedLeads.forEach { (areaName, areaLeads) ->
                                item {
                                    AreaHeaderStyle(areaName = areaName)
                                }
                                items(areaLeads, key = { "grouped_${it.id}" }) { lead ->
                                    LeadCardItem(
                                        lead = lead,
                                        onDelete = { leadToDelete = lead },
                                        onCall = { /* Direct calls allowed */ }
                                    )
                                }
                            }
                        }
                    } else {
                        // Display leads belonging to this specific area filtered list
                        if (filteredLeads.isEmpty()) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(32.dp),
                                verticalArrangement = Arrangement.Center,
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    "Không tìm thấy khách hàng nào ở khu vực $selectedArea !",
                                    fontSize = 15.sp,
                                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                                    modifier = Modifier.padding(16.dp)
                                )
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                items(filteredLeads, key = { "filtered_${it.id}" }) { lead ->
                                    LeadCardItem(
                                        lead = lead,
                                        onDelete = { leadToDelete = lead },
                                        onCall = { /* Call trigger */ }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Delete Confirmation Dialog
        if (leadToDelete != null) {
            AlertDialog(
                onDismissRequest = { leadToDelete = null },
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Xác nhận xóa", fontWeight = FontWeight.Bold)
                    }
                },
                text = {
                    Text("Bạn có chắc chắn muốn xóa khách hàng \"${leadToDelete?.name}\" không? Hành động này không thể hoàn tác.")
                },
                confirmButton = {
                    Button(
                        onClick = {
                            leadToDelete?.let { viewModel.deleteLead(it) }
                            leadToDelete = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text("Xóa", fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { leadToDelete = null }) {
                        Text("Hủy", fontWeight = FontWeight.Medium)
                    }
                }
            )
        }
    }
}

@Composable
fun AreaHeaderStyle(areaName: String) {
    Surface(
        color = MaterialTheme.colorScheme.primaryContainer,
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp, 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.LocationCity,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Khu vực: $areaName",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}
