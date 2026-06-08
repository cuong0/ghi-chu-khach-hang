package com.example.ui.screens

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import com.example.ui.viewmodel.LeadViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileTab(
    viewModel: LeadViewModel
) {
    val isLoggedIn by viewModel.isLoggedIn.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Cá nhân",
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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            AnimatedContent(
                targetState = isLoggedIn,
                transitionSpec = {
                    fadeIn() togetherWith fadeOut()
                },
                label = "ProfileStateTransition"
            ) { logged ->
                if (logged) {
                    LoggedInView(viewModel = viewModel)
                } else {
                    NotLoggedInView(viewModel = viewModel)
                }
            }
        }
    }
}

@Composable
fun LoggedInView(viewModel: LeadViewModel) {
    val context = LocalContext.current
    val name by viewModel.userName.collectAsState()
    val phone by viewModel.userPhone.collectAsState()
    val email by viewModel.userEmail.collectAsState()
    val avatarUri by viewModel.userAvatarUri.collectAsState()
    val avatarScale by viewModel.userAvatarScale.collectAsState()
    val avatarOffsetX by viewModel.userAvatarOffsetX.collectAsState()
    val avatarOffsetY by viewModel.userAvatarOffsetY.collectAsState()

    var showCropDialog by remember { mutableStateOf(false) }
    var selectedRawUri by remember { mutableStateOf<Uri?>(null) }

    // Launcher for Android photo selector PhotoPicker
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            selectedRawUri = uri
            showCropDialog = true
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Avatar Section
        Box(
            modifier = Modifier
                .size(130.dp)
                .clickable {
                    photoPickerLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                },
            contentAlignment = Alignment.BottomEnd
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(4.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer,
                border = BorderStroke(3.dp, MaterialTheme.colorScheme.primary)
            ) {
                if (avatarUri.isNotEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                    ) {
                        AsyncImage(
                            model = avatarUri,
                            contentDescription = "Avatar",
                            modifier = Modifier
                                .fillMaxSize()
                                .graphicsLayer(
                                    scaleX = avatarScale,
                                    scaleY = avatarScale,
                                    translationX = avatarOffsetX,
                                    translationY = avatarOffsetY
                                ),
                            contentScale = ContentScale.Crop
                        )
                    }
                } else {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "No Avatar",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(60.dp)
                        )
                    }
                }
            }

            // Edit badge indicator
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(36.dp),
                shadowElevation = 4.dp
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    Icon(
                        imageVector = Icons.Default.CameraAlt,
                        contentDescription = "Sửa ảnh",
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        Text(
            text = "Nhấp vào ảnh để thay đổi avatar",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Personal details card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Badge, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text("Họ và tên", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(name, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Divider(color = MaterialTheme.colorScheme.outlineVariant)

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Phone, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text("Số điện thoại", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(phone, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                    }
                }

                Divider(color = MaterialTheme.colorScheme.outlineVariant)

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Email, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text("Email liên kết", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(email, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Signout CTA Button
        Button(
            onClick = { viewModel.logout() },
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.errorContainer,
                contentColor = MaterialTheme.colorScheme.onErrorContainer
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(14.dp)
        ) {
            Icon(Icons.Default.Logout, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Đăng xuất tài khoản", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }
    }

    // Interactive circular photo cropper simulation dialog (1:1 Aspect Ratio)
    if (showCropDialog && selectedRawUri != null) {
        var zoomScale by remember { mutableStateOf(1f) }
        var panOffset by remember { mutableStateOf(Offset.Zero) }
        AlertDialog(
            onDismissRequest = { showCropDialog = false },
            title = { Text("Căn chỉnh hình ảnh", fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Dùng hai ngón tay để phóng to/thu nhỏ hoặc kéo để căn chỉnh hình ảnh vào đúng khung.", fontSize = 13.sp)

                    // Image Canvas Container with clipping, pinch gesture & rule of thirds grid overlay
                    Box(
                        modifier = Modifier
                            .size(180.dp)
                            .clip(CircleShape)
                            .background(Color.DarkGray)
                            .pointerInput(Unit) {
                                detectTransformGestures { _, pan, zoom, _ ->
                                    zoomScale = (zoomScale * zoom).coerceIn(0.5f, 3.0f)
                                    panOffset = panOffset + pan
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        AsyncImage(
                            model = selectedRawUri,
                            contentDescription = "Selected Raw Image",
                            modifier = Modifier
                                .fillMaxSize()
                                .graphicsLayer(
                                    scaleX = zoomScale,
                                    scaleY = zoomScale,
                                    translationX = panOffset.x,
                                    translationY = panOffset.y
                                ),
                            contentScale = ContentScale.Crop
                        )

                        // 3x3 Grid Overlay for alignment
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val strokeWidth = 1.dp.toPx()
                            val lineColor = Color.White.copy(alpha = 0.4f)

                            // Horizontal Grid Lines
                            drawLine(
                                color = lineColor,
                                start = Offset(0f, size.height / 3f),
                                end = Offset(size.width, size.height / 3f),
                                strokeWidth = strokeWidth
                            )
                            drawLine(
                                color = lineColor,
                                start = Offset(0f, 2f * size.height / 3f),
                                end = Offset(size.width, 2f * size.height / 3f),
                                strokeWidth = strokeWidth
                            )

                            // Vertical Grid Lines
                            drawLine(
                                color = lineColor,
                                start = Offset(size.width / 3f, 0f),
                                end = Offset(size.width / 3f, size.height),
                                strokeWidth = strokeWidth
                            )
                            drawLine(
                                color = lineColor,
                                start = Offset(2f * size.width / 3f, 0f),
                                end = Offset(2f * size.width / 3f, size.height),
                                strokeWidth = strokeWidth
                            )
                        }
                    }

                    // zoom Slider synced with Gestures
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Thu phóng", fontSize = 12.sp, modifier = Modifier.width(60.dp))
                        Slider(
                            value = zoomScale,
                            onValueChange = { zoomScale = it },
                            valueRange = 0.5f..3.0f,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val localUri = copyUriToLocalFile(context, selectedRawUri!!, avatarUri)
                        val saveUri = localUri?.toString() ?: selectedRawUri.toString()
                        viewModel.updateAvatar(saveUri, zoomScale, panOffset.x, panOffset.y)
                        showCropDialog = false
                        Toast.makeText(context, "Đã căn chỉnh và cập nhật ảnh đại diện thành công!", Toast.LENGTH_SHORT).show()
                    }
                ) {
                    Text("Cắt & Lưu", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCropDialog = false }) {
                    Text("Hủy bỏ")
                }
            }
        )
    }
}

private fun copyUriToLocalFile(context: android.content.Context, uri: Uri, oldAvatarPath: String): Uri? {
    try {
        if (oldAvatarPath.isNotEmpty() && oldAvatarPath.startsWith("file://")) {
            try {
                val oldUri = Uri.parse(oldAvatarPath)
                oldUri.path?.let { path ->
                    val oldFile = java.io.File(path)
                    if (oldFile.exists()) {
                        oldFile.delete()
                    }
                }
            } catch (e: Exception) {
                // Ignore
            }
        }

        val inputStream = context.contentResolver.openInputStream(uri) ?: return null
        val newFilename = "user_avatar_${System.currentTimeMillis()}.jpg"
        val file = java.io.File(context.filesDir, newFilename)
        val outputStream = java.io.FileOutputStream(file)
        inputStream.use { input ->
            outputStream.use { output ->
                input.copyTo(output)
            }
        }
        return Uri.fromFile(file)
    } catch (e: Exception) {
        e.printStackTrace()
        return null
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotLoggedInView(viewModel: LeadViewModel) {
    val context = LocalContext.current

    // Sign up text inputs values
    var nameByManual by remember { mutableStateOf("") }
    var phoneByManual by remember { mutableStateOf("") }
    var emailByManual by remember { mutableStateOf("") }
    var verifyCodeByManual by remember { mutableStateOf("") }

    val isCodeSent by viewModel.isCodeSent.collectAsState()

    var showGoogleDialog by remember { mutableStateOf(false) }
    var googleName by remember { mutableStateOf("") }
    var googleEmail by remember { mutableStateOf("") }
    var googlePhone by remember { mutableStateOf("") }

    var showFacebookDialog by remember { mutableStateOf(false) }
    var facebookName by remember { mutableStateOf("") }
    var facebookEmail by remember { mutableStateOf("") }
    var facebookPhone by remember { mutableStateOf("") }

    if (showGoogleDialog) {
        AlertDialog(
            onDismissRequest = { showGoogleDialog = false },
            title = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(Color.White, CircleShape)
                            .border(BorderStroke(1.dp, Color(0xFFE0E0E0)), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "G",
                            color = Color(0xFF4285F4),
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Đăng nhập Google",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "Kết nối với tài khoản Google cá nhân của bạn",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                ) {
                    OutlinedTextField(
                        value = googleName,
                        onValueChange = { googleName = it },
                        label = { Text("Họ và tên") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = googleEmail,
                        onValueChange = { googleEmail = it },
                        label = { Text("Tài khoản Gmail") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Email),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = googlePhone,
                        onValueChange = { googlePhone = it },
                        label = { Text("Số điện thoại (tùy chọn)") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Phone),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (googleName.isBlank() || googleEmail.isBlank()) {
                            Toast.makeText(context, "Vui lòng nhập đầy đủ tên và email Google!", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        if (!googleEmail.contains("@") || !googleEmail.endsWith(".com")) {
                            Toast.makeText(context, "Vui lòng nhập định dạng email Google hợp lệ!", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        viewModel.loginWithGoogle(googleName.trim(), googleEmail.trim(), googlePhone.trim())
                        showGoogleDialog = false
                    },
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Đăng nhập")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showGoogleDialog = false }
                ) {
                    Text("Hủy")
                }
            }
        )
    }

    if (showFacebookDialog) {
        AlertDialog(
            onDismissRequest = { showFacebookDialog = false },
            title = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(Color(0xFF1877F2), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "f",
                            color = Color.White,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Đăng nhập Facebook",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "Kết nối với tài khoản Facebook cá nhân của bạn",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                ) {
                    OutlinedTextField(
                        value = facebookName,
                        onValueChange = { facebookName = it },
                        label = { Text("Họ và tên") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = facebookEmail,
                        onValueChange = { facebookEmail = it },
                        label = { Text("Tài khoản Email/SĐT") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Email),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = facebookPhone,
                        onValueChange = { facebookPhone = it },
                        label = { Text("Số điện thoại (tùy chọn)") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Phone),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (facebookName.isBlank() || facebookEmail.isBlank()) {
                            Toast.makeText(context, "Vui lòng nhập đầy đủ tên và email Facebook!", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        viewModel.loginWithFacebook(facebookName.trim(), facebookEmail.trim(), facebookPhone.trim())
                        showFacebookDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1877F2)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Đăng nhập")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showFacebookDialog = false }
                ) {
                    Text("Hủy")
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Lock Header visual
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primaryContainer,
            modifier = Modifier.size(80.dp)
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(40.dp)
                )
            }
        }

        Text(
            text = "Yêu cầu đăng nhập hoặc đăng ký để bật tính năng đồng bộ hóa dữ liệu trực tuyến",
            fontSize = 14.sp,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        // Part 1: Fast Authentication Buttons
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Button(
                onClick = { showGoogleDialog = true },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFEA4335), // Google Red brand
                    contentColor = Color.White
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Login, contentDescription = null)
                Spacer(modifier = Modifier.width(10.dp))
                Text("Đăng nhập bằng Google", fontWeight = FontWeight.Bold)
            }

            Button(
                onClick = { showFacebookDialog = true },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF1877F2), // Facebook Blue brand
                    contentColor = Color.White
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.ArrowForward, contentDescription = null)
                Spacer(modifier = Modifier.width(10.dp))
                Text("Đăng nhập bằng Facebook", fontWeight = FontWeight.Bold)
            }
        }

        // Forgot password text button
        TextButton(
            onClick = {
                viewModel.setManualSignUpInputs(nameByManual, phoneByManual, emailByManual)
                viewModel.sendResetPasswordCode()
            }
        ) {
            Text(
                "Quên mật khẩu?",
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.bodyMedium
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Profile divider
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Divider(modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.outlineVariant)
            Text(
                "HOẶC ĐĂNG KÝ THỦ CÔNG",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                modifier = Modifier.padding(horizontal = 8.dp)
            )
            Divider(modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.outlineVariant)
        }

        // Part 2: Manual register fields form
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = nameByManual,
                onValueChange = { nameByManual = it },
                label = { Text("Họ và tên") },
                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = phoneByManual,
                onValueChange = { phoneByManual = it },
                label = { Text("Số điện thoại") },
                leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Phone),
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = emailByManual,
                onValueChange = { emailByManual = it },
                label = { Text("Tài khoản Gmail liên kết") },
                leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Email),
                modifier = Modifier.fillMaxWidth()
            )

            // Verification verification code stage
            AnimatedVisibility(
                visible = isCodeSent,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            "Một mã số gồm 6 chữ số vừa được gửi đến email của bạn. Vui lòng nhập mã để hoàn thiện xác thực tài khoản.",
                            fontSize = 12.sp,
                            modifier = Modifier.padding(12.dp),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    OutlinedTextField(
                        value = verifyCodeByManual,
                        onValueChange = { verifyCodeByManual = it },
                        label = { Text("Nhập mã xác thực") },
                        leadingIcon = { Icon(Icons.Default.Key, contentDescription = null) },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Sign Up Action buttons
            if (!isCodeSent) {
                Button(
                    onClick = {
                        if (nameByManual.isBlank() || phoneByManual.isBlank() || emailByManual.isBlank()) {
                            Toast.makeText(context, "Vui lòng nhập đầy đủ 3 trường thông tin để đăng ký!", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        viewModel.setManualSignUpInputs(nameByManual, phoneByManual, emailByManual)
                        viewModel.sendVerificationCode()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Send, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Nhận mã xác minh qua Gmail", fontWeight = FontWeight.Bold)
                }
            } else {
                Button(
                    onClick = {
                        viewModel.authVerificationCode.value = verifyCodeByManual
                        val success = viewModel.verifyAndSignUp()
                        if (success) {
                            // Reset local states on success
                            nameByManual = ""
                            phoneByManual = ""
                            emailByManual = ""
                            verifyCodeByManual = ""
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Check, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Xác minh & Đăng ký ngay", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
