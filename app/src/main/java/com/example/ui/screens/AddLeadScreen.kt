package com.example.ui.screens

import android.Manifest
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.PopupProperties
import androidx.core.content.ContextCompat
import com.example.ui.Utility
import com.example.ui.viewmodel.LeadViewModel
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddLeadScreen(
    viewModel: LeadViewModel,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current

    val calendarPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val writeGranted = permissions[Manifest.permission.WRITE_CALENDAR] ?: false
        if (writeGranted) {
            viewModel.saveLead {
                onNavigateBack()
            }
        } else {
            Toast.makeText(context, "Quyền truy cập lịch bị từ chối. Đã lưu khách hàng nhưng không thể tự động tạo lịch hẹn.", Toast.LENGTH_LONG).show()
            viewModel.saveLead {
                onNavigateBack()
            }
        }
    }

    // Form states from ViewModel to survive any recomposition
    val name by viewModel.nameInput.collectAsState()
    val phone by viewModel.phoneInput.collectAsState()
    val address by viewModel.addressInput.collectAsState()
    val lat by viewModel.pickedLatitude.collectAsState()
    val lng by viewModel.pickedLongitude.collectAsState()
    val area by viewModel.areaInput.collectAsState()
    val notes by viewModel.notesInput.collectAsState()
    val recontactDate by viewModel.recontactDateInput.collectAsState()
    val editingLeadId by viewModel.editingLeadId.collectAsState()
    val isEditMode = editingLeadId != null

    // Keep local states for instant, synchronous IME feedback in Vietnamese without breaking composition
    var nameLocal by remember { mutableStateOf(viewModel.nameInput.value) }
    var phoneLocal by remember { mutableStateOf(viewModel.phoneInput.value) }
    var addressLocal by remember { mutableStateOf(viewModel.addressInput.value) }
    var areaLocal by remember { mutableStateOf(viewModel.areaInput.value) }
    var notesLocal by remember { mutableStateOf(viewModel.notesInput.value) }

    // Sync external/asynchronous changes (like map picker, clear trigger) without interrupting direct text inputs
    LaunchedEffect(name) { if (name != nameLocal) nameLocal = name }
    LaunchedEffect(phone) { if (phone != phoneLocal) phoneLocal = phone }
    LaunchedEffect(address) { if (address != addressLocal) addressLocal = address }
    LaunchedEffect(area) { if (area != areaLocal) areaLocal = area }
    LaunchedEffect(notes) { if (notes != notesLocal) notesLocal = notes }

    // Existing areas list to populate searchable dropdown suggestions
    val existingAreas by viewModel.distinctAreas.collectAsState()

    var areaDropdownExpanded by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEditMode) "Chỉnh sửa khách hàng" else "Thêm khách hàng mới", fontWeight = FontWeight.Bold, fontSize = 21.sp) },
                navigationIcon = {
                    IconButton(onClick = {
                        focusManager.clearFocus()
                        onNavigateBack()
                    }) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Quay lại")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.primary,
                    navigationIconContentColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .pointerInput(Unit) {
                    detectTapGestures(onTap = {
                        focusManager.clearFocus()
                    })
                }
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // General Title Label
            Text(
                "Thông tin khách hàng liên hệ",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.primary
            )

            // Form Inputs Card
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Tên Khách Hàng field
                    OutlinedTextField(
                        value = nameLocal,
                        onValueChange = {
                            nameLocal = it
                            viewModel.nameInput.value = it
                        },
                        label = { Text("Tên khách hàng (*)") },
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().testTag("add_name_field")
                    )

                    // Số điện thoại field
                    OutlinedTextField(
                        value = phoneLocal,
                        onValueChange = {
                            phoneLocal = it
                            viewModel.phoneInput.value = it
                        },
                        label = { Text("Số điện thoại (*)") },
                        leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().testTag("add_phone_field")
                    )

                    // Địa chỉ with Map Picker adjacent trigger
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = addressLocal,
                            onValueChange = {
                                addressLocal = it
                                viewModel.addressInput.value = it
                            },
                            label = { Text("Địa chỉ") },
                            leadingIcon = { Icon(Icons.Default.Home, contentDescription = null, tint = MaterialTheme.colorScheme.secondary) },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        )

                        Button(
                            onClick = {
                                Utility.openGoogleMaps(
                                    context = context,
                                    address = addressLocal,
                                    latitude = lat,
                                    longitude = lng
                                )
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                            ),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 12.dp),
                            modifier = Modifier.height(56.dp).padding(top = 4.dp)
                        ) {
                            Icon(Icons.Default.Map, contentDescription = null)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Bản đồ", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }



                    // Selected Coordinates Indicator
                    if (lat != null && lng != null) {
                        Surface(
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.LocationOn, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    "Tọa độ: (${String.format("%.5f", lat)}, ${String.format("%.5f", lng)})",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }

                    // Khu Vực Selector Field (Dropdown Search suggestion)
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = areaLocal,
                            onValueChange = {
                                areaLocal = it
                                viewModel.areaInput.value = it
                                areaDropdownExpanded = true
                            },
                            label = { Text("Khu vực (*)") },
                            leadingIcon = { Icon(Icons.Default.LocationCity, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                            trailingIcon = {
                                IconButton(onClick = { areaDropdownExpanded = !areaDropdownExpanded }) {
                                    Icon(Icons.Default.ArrowDropDown, contentDescription = "Gợi ý")
                                }
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        )

                        DropdownMenu(
                            expanded = areaDropdownExpanded,
                            onDismissRequest = { areaDropdownExpanded = false },
                            properties = PopupProperties(focusable = false),
                            modifier = Modifier.fillMaxWidth(0.9f)
                        ) {
                            existingAreas.forEach { areaSuggestion ->
                                DropdownMenuItem(
                                    text = { Text(areaSuggestion) },
                                    onClick = {
                                        areaLocal = areaSuggestion
                                        viewModel.areaInput.value = areaSuggestion
                                        areaDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    // Multiline Nội Dung Quan Tâm field
                    OutlinedTextField(
                        value = notesLocal,
                        onValueChange = {
                            notesLocal = it
                            viewModel.notesInput.value = it
                        },
                        label = { Text("Nội dung khách hàng quan tâm") },
                        leadingIcon = { Icon(Icons.Default.Notes, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(110.dp),
                        maxLines = 4
                    )
                }
            }

            // Contact Alarm Date picker trigger
            Text(
                "Tạo lịch hẹn",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.primary
            )

            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "Thời gian Re-contact",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = Utility.formatDateTime(recontactDate),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Button(
                        onClick = {
                            val calendar = Calendar.getInstance().apply { timeInMillis = recontactDate }
                            val datePickerDialog = DatePickerDialog(
                                context,
                                { _, year, month, dayOfMonth ->
                                    calendar.set(Calendar.YEAR, year)
                                    calendar.set(Calendar.MONTH, month)
                                    calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)

                                    // Launch Timepicker immediately after date selection
                                    TimePickerDialog(
                                        context,
                                        { _, hourOfDay, minute ->
                                            calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                                            calendar.set(Calendar.MINUTE, minute)
                                            viewModel.recontactDateInput.value = calendar.timeInMillis
                                        },
                                        calendar.get(Calendar.HOUR_OF_DAY),
                                        calendar.get(Calendar.MINUTE),
                                        true
                                    ).show()
                                },
                                calendar.get(Calendar.YEAR),
                                calendar.get(Calendar.MONTH),
                                calendar.get(Calendar.DAY_OF_MONTH)
                            )
                            datePickerDialog.show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Event, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Chọn lịch", fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Save Action Button
            Button(
                onClick = {
                    focusManager.clearFocus()
                    val hasWritePermission = ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.WRITE_CALENDAR
                    ) == PackageManager.PERMISSION_GRANTED

                    val hasReadPermission = ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.READ_CALENDAR
                    ) == PackageManager.PERMISSION_GRANTED

                    if (hasWritePermission && hasReadPermission) {
                        viewModel.saveLead {
                            onNavigateBack()
                        }
                    } else {
                        calendarPermissionLauncher.launch(
                            arrayOf(
                                Manifest.permission.WRITE_CALENDAR,
                                Manifest.permission.READ_CALENDAR
                            )
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("submit_lead_button"),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Icon(imageVector = Icons.Default.Save, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isEditMode) "Lưu & Cập nhật khách hàng" else "Lưu khách hàng & Tạo cuộc hẹn",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }


}
