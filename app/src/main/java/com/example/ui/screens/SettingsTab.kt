package com.example.ui.screens

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.viewmodel.LeadViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsTab(
    viewModel: LeadViewModel
) {
    val context = LocalContext.current
    val isDarkTheme by viewModel.isDarkTheme.collectAsState()
    val isSyncEnabled by viewModel.isSyncEnabled.collectAsState()

    var showAboutDialog by remember { mutableStateOf(false) }
    var showClearConfirmationDialog by remember { mutableStateOf(false) }

    val alarmManager = remember { context.getSystemService(android.content.Context.ALARM_SERVICE) as? android.app.AlarmManager }
    val powerManager = remember { context.getSystemService(android.content.Context.POWER_SERVICE) as? android.os.PowerManager }

    var isBackgroundAllowed by remember {
        mutableStateOf(
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M && powerManager != null) {
                powerManager.isIgnoringBatteryOptimizations(context.packageName)
            } else {
                true
            }
        )
    }

    var canScheduleExactAlarms by remember {
        mutableStateOf(
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S && alarmManager != null) {
                alarmManager.canScheduleExactAlarms()
            } else {
                true
            }
        )
    }

    LaunchedEffect(Unit) {
        while (true) {
            isBackgroundAllowed = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M && powerManager != null) {
                powerManager.isIgnoringBatteryOptimizations(context.packageName)
            } else {
                true
            }
            canScheduleExactAlarms = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S && alarmManager != null) {
                alarmManager.canScheduleExactAlarms()
            } else {
                true
            }
            kotlinx.coroutines.delay(2000)
        }
    }

    // Launcher for multiple permissions request
    val permissionsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissionsMap ->
        val grantedCount = permissionsMap.values.count { it }
        val totalCount = permissionsMap.size
        if (grantedCount == totalCount) {
            Toast.makeText(context, "Đã cấp tất cả các quyền thành công!", Toast.LENGTH_SHORT).show()
        } else if (grantedCount > 0) {
            Toast.makeText(context, "Đã cấp thêm $grantedCount/$totalCount quyền.", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "Yêu cầu cấp quyền bị từ chối.", Toast.LENGTH_SHORT).show()
        }
    }

    // Launcher for exporting CSV (Excel file)
    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/csv")
    ) { uri ->
        if (uri != null) {
            try {
                val csvContent = viewModel.getCsvContentToExport()
                context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                    outputStream.write(csvContent.toByteArray(Charsets.UTF_8))
                }
                Toast.makeText(context, "Xuất dữ liệu Excel thành công!", Toast.LENGTH_LONG).show()
            } catch (e: Exception) {
                Toast.makeText(context, "Lỗi khi lưu file: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    // Launcher for importing CSV (Excel file)
    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            try {
                context.contentResolver.openInputStream(uri)?.use { inputStream ->
                    val rawText = inputStream.bufferedReader(Charsets.UTF_8).readText()
                    viewModel.importLeadsFromCsv(
                        csvContent = rawText,
                        onComplete = { count ->
                            Toast.makeText(context, "Đã nhập thành công $count khách hàng từ file!", Toast.LENGTH_LONG).show()
                        },
                        onError = { errorMsg ->
                            Toast.makeText(context, "Nhập dữ liệu thất bại: $errorMsg", Toast.LENGTH_LONG).show()
                        }
                    )
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Lỗi khi đọc file: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Cài đặt",
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
        var newAreaInput by remember { mutableStateOf("") }
        val customAreas by viewModel.customAreas.collectAsState()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                "Tùy chọn cấu hình",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(start = 4.dp)
            )

            // Configuration List Card
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    // Option 1: Cloud Sync
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CloudSync,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text(
                                    "Đồng bộ hóa dữ liệu",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    "Đồng bộ ghi chú lên đám mây tự động",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Switch(
                            checked = isSyncEnabled,
                            onCheckedChange = { viewModel.setSyncEnabled(it) }
                        )
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                    // Option 2: Dark Mode
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.LightMode,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text(
                                    "Thay đổi chế độ sáng tối",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    "Bật/Tắt giao diện tối của hệ thống",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Switch(
                            checked = isDarkTheme,
                            onCheckedChange = { viewModel.setDarkTheme(it) }
                        )
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                    // Option 4: Enable all app permissions
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                val neededPerms = mutableListOf(
                                    android.Manifest.permission.ACCESS_FINE_LOCATION,
                                    android.Manifest.permission.ACCESS_COARSE_LOCATION,
                                    android.Manifest.permission.READ_CALENDAR,
                                    android.Manifest.permission.WRITE_CALENDAR,
                                    android.Manifest.permission.CAMERA
                                )
                                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                                    neededPerms.add(android.Manifest.permission.READ_MEDIA_IMAGES)
                                    neededPerms.add(android.Manifest.permission.POST_NOTIFICATIONS)
                                } else {
                                    neededPerms.add(android.Manifest.permission.READ_EXTERNAL_STORAGE)
                                }

                                val notGranted = neededPerms.filter { permission ->
                                    androidx.core.content.ContextCompat.checkSelfPermission(context, permission) != android.content.pm.PackageManager.PERMISSION_GRANTED
                                }

                                if (notGranted.isEmpty()) {
                                    Toast.makeText(context, "Tất cả quyền của ứng dụng đã được bật từ trước!", Toast.LENGTH_SHORT).show()
                                } else {
                                    permissionsLauncher.launch(notGranted.toTypedArray())
                                }
                            }
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Security,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text(
                                    "Bật tất cả các quyền của ứng dụng",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    "Yêu cầu cấp tất cả các quyền của hệ thống chưa cho phép",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                    // Option 4b: Cho phép chạy ngầm (Bỏ qua tối ưu pin)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                                    try {
                                        val intent = android.content.Intent(android.provider.Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                                            data = android.net.Uri.parse("package:${context.packageName}")
                                        }
                                        context.startActivity(intent)
                                    } catch (e: Exception) {
                                        try {
                                            val intent = android.content.Intent(android.provider.Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
                                            context.startActivity(intent)
                                        } catch (ex: Exception) {
                                            Toast.makeText(context, "Thiết bị không hỗ trợ cấu hình tự động. Vui lòng tắt thủ công trong cài đặt Pin.", Toast.LENGTH_LONG).show()
                                        }
                                    }
                                } else {
                                    Toast.makeText(context, "Thiết bị của bạn hỗ trợ sẵn chế độ chạy ngầm!", Toast.LENGTH_SHORT).show()
                                }
                            }
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Security,
                                contentDescription = null,
                                tint = if (isBackgroundAllowed) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = "Cho phép chạy trong nền",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.weight(1f, fill = false)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Surface(
                                        shape = RoundedCornerShape(100.dp),
                                        color = if (isBackgroundAllowed) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.errorContainer,
                                    ) {
                                        Text(
                                            text = if (isBackgroundAllowed) "Đã bật" else "Yêu cầu",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isBackgroundAllowed) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onErrorContainer,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                                Text(
                                    "Bỏ qua tối ưu hóa pin để nhận được thông báo hẹn gặp ngay cả khi tắt hoàn toàn ứng dụng",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                    // Option 4c: Quyền báo hẹn giờ chính xác
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                                    try {
                                        val intent = android.content.Intent(android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                                            data = android.net.Uri.parse("package:${context.packageName}")
                                        }
                                        context.startActivity(intent)
                                    } catch (e: Exception) {
                                        Toast.makeText(context, "Vui lòng cấp quyền báo thức chính xác trong cài đặt hệ thống.", Toast.LENGTH_LONG).show()
                                    }
                                } else {
                                    Toast.makeText(context, "Thiết bị của bạn hỗ trợ sẵn báo thức chính xác!", Toast.LENGTH_SHORT).show()
                                }
                            }
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Security,
                                contentDescription = null,
                                tint = if (canScheduleExactAlarms) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = "Lên lịch hẹn chính xác",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.weight(1f, fill = false)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Surface(
                                        shape = RoundedCornerShape(100.dp),
                                        color = if (canScheduleExactAlarms) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.errorContainer,
                                    ) {
                                        Text(
                                            text = if (canScheduleExactAlarms) "Đã bật" else "Yêu cầu",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (canScheduleExactAlarms) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onErrorContainer,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                                Text(
                                    "Cho phép hiển thị thông báo chính xác từng giây khi đến thời gian liên hệ tái tiếp xúc",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Text(
                "Quản lý dữ liệu",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(start = 4.dp, top = 8.dp)
            )

            // Import/Export Options Card
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    // Option 1: Export Data to Excel
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                exportLauncher.launch("danh_sach_khach_hang.csv")
                            }
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Download,
                                contentDescription = "Xuất dữ liệu",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text(
                                    "Xuất dữ liệu (Excel)",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    "Lưu danh sách khách hàng ra file Excel CSV",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                    // Option 2: Import Data from Excel
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                importLauncher.launch("*/*")
                            }
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Upload,
                                contentDescription = "Nhập dữ liệu",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text(
                                    "Nhập dữ liệu",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    "Khôi phục danh sách từ chính file đã xuất",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Text(
                "Khu vực & Phân loại",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(start = 4.dp, top = 8.dp)
            )

            // Custom Areas Manager Card
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        "Nhập khu vực của bạn",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = newAreaInput,
                            onValueChange = { newAreaInput = it },
                            label = { Text("Tên khu vực mới") },
                            placeholder = { Text("Ví dụ: Quận 1, Hà Nội...") },
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            trailingIcon = {
                                if (newAreaInput.isNotEmpty()) {
                                    IconButton(onClick = { newAreaInput = "" }) {
                                        Icon(Icons.Default.Close, contentDescription = "Xóa")
                                    }
                                }
                            }
                        )

                        Button(
                            onClick = {
                                if (newAreaInput.isNotBlank()) {
                                    viewModel.addCustomArea(newAreaInput)
                                    newAreaInput = ""
                                }
                            },
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Thêm")
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Thêm", fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Danh sách khu vực (${customAreas.size})",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        if (customAreas.isNotEmpty()) {
                            TextButton(
                                onClick = { showClearConfirmationDialog = true },
                                colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                            ) {
                                Icon(Icons.Default.DeleteSweep, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Xóa hết", fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    if (customAreas.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "Chưa có khu vực tự chọn nào.\nHãy nhập và nhấn nút Thêm phía trên!",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    } else {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            customAreas.forEach { areaName ->
                                Surface(
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 12.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Default.Place,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = areaName,
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Medium,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                        }

                                        IconButton(
                                            onClick = { viewModel.removeCustomArea(areaName) },
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Close,
                                                contentDescription = "Xóa",
                                                tint = MaterialTheme.colorScheme.error,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Text(
                "Thông tin",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(start = 4.dp, top = 8.dp)
            )

            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showAboutDialog = true }
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Thông tin ứng dụng",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(
                                "Thông tin ứng dụng",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                "Phiên bản, tác giả và chính sách",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // About Application Dialog
        if (showAboutDialog) {
            AlertDialog(
                onDismissRequest = { showAboutDialog = false },
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Thông tin ứng dụng", fontWeight = FontWeight.Bold)
                    }
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "Ghi chú khách hàng tiềm năng (Lead Notes)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text("Ứng dụng quản lý danh sách liên hệ khách hàng tiềm năng, tổ chức công việc và đặt lịch re-contact tự động chuyên nghiệp.")
                        Spacer(modifier = Modifier.height(8.dp))
                        Row {
                            Text("Phiên bản: ", fontWeight = FontWeight.Bold)
                            Text("2.1.6")
                        }
                        Row {
                            Text("Tác giả: ", fontWeight = FontWeight.Bold)
                            Text("Cường Lâm")
                        }
                        Text(
                            "Bản quyền © 2026. Mọi quyền được bảo lưu.",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = { showAboutDialog = false }
                    ) {
                        Text("Đóng", fontWeight = FontWeight.Bold)
                    }
                }
            )
        }

        // Clear Areas Confirmation Dialog
        if (showClearConfirmationDialog) {
            AlertDialog(
                onDismissRequest = { showClearConfirmationDialog = false },
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.DeleteSweep,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Xác nhận xóa", fontWeight = FontWeight.Bold)
                    }
                },
                text = {
                    Text("Bạn có chắc chắn muốn xóa toàn bộ danh sách khu vực tự chọn không? Hành động này không thể hoàn tác.")
                },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.clearAllCustomAreas()
                            showClearConfirmationDialog = false
                            Toast.makeText(context, "Đã xóa toàn bộ khu vực!", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text("Xóa hết", fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { showClearConfirmationDialog = false }
                    ) {
                        Text("Hủy", fontWeight = FontWeight.Medium)
                    }
                }
            )
        }
    }
}
