package com.example.app_lam_dep.ui.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.applamdep.customer.data.local.UserLocalStorage
import com.applamdep.customer.domain.model.AppUser
import com.applamdep.customer.domain.model.UserRole
import com.example.app_lam_dep.ui.icons.GlowIcons
import com.example.app_lam_dep.ui.icons.IconMedallion
import com.example.app_lam_dep.ui.theme.*

/**
 * MÀN HÌNH ĐĂNG NHẬP / ĐĂNG KÝ TÀI KHOẢN (HAUTE COUTURE LUXURY)
 * - Chỉ sử dụng Gmail và Mật khẩu
 * - 3 loại tài khoản được cấp: Khách hàng - Cửa hàng - Admin
 * - Tài khoản Cửa hàng bắt buộc PHẢI được Admin duyệt mới có thể sử dụng
 */
@Composable
fun AuthScreen(
    onLoginSuccess: (AppUser) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val userStorage = remember { UserLocalStorage(context) }

    var isLoginTab by remember { mutableStateOf(true) } // true: Đăng nhập, false: Đăng ký

    // Form fields
    var gmail by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    // Register specific
    var selectedRole by remember { mutableStateOf(UserRole.CUSTOMER) }
    var salonName by remember { mutableStateOf("") }
    var adminSecretCode by remember { mutableStateOf("") }

    // Dialog state
    var pendingMerchantDialogMessage by remember { mutableStateOf<String?>(null) }
    var successRegisterDialogMessage by remember { mutableStateOf<String?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Dialog thông báo tài khoản Cửa hàng đang chờ Admin duyệt
    if (pendingMerchantDialogMessage != null) {
        AlertDialog(
            onDismissRequest = { pendingMerchantDialogMessage = null },
            icon = {
                Icon(
                    imageVector = Icons.Default.PendingActions,
                    contentDescription = null,
                    tint = WarningOrange,
                    modifier = Modifier.size(36.dp)
                )
            },
            title = {
                Text(
                    text = "Chờ Admin Phê Duyệt",
                    fontWeight = FontWeight.Bold,
                    color = DustyMerlot,
                    fontSize = 17.sp
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = pendingMerchantDialogMessage!!,
                        fontSize = 13.sp,
                        color = TextNoir,
                        lineHeight = 19.sp
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFFEF3C7))
                            .padding(10.dp)
                    ) {
                        Text(
                            text = "💡 Mẹo: Bạn có thể đăng nhập bằng tài khoản Admin (admin@gmail.com / admin) để duyệt ngay tài khoản này!",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFFB45309)
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        pendingMerchantDialogMessage = null
                        // Tự động điền tài khoản admin để tiện duyệt
                        gmail = "admin@gmail.com"
                        password = "admin"
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = DustyMerlot)
                ) {
                    Text("Đăng nhập Admin duyệt ngay", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingMerchantDialogMessage = null }) {
                    Text("Đóng", color = TextMuted)
                }
            }
        )
    }

    // Dialog đăng ký thành công cho Cửa hàng
    if (successRegisterDialogMessage != null) {
        AlertDialog(
            onDismissRequest = { successRegisterDialogMessage = null },
            icon = {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = SuccessGreen,
                    modifier = Modifier.size(36.dp)
                )
            },
            title = {
                Text(
                    text = "Đăng Ký Thành Công",
                    fontWeight = FontWeight.Bold,
                    color = DustyMerlot,
                    fontSize = 17.sp
                )
            },
            text = {
                Text(
                    text = successRegisterDialogMessage!!,
                    fontSize = 13.sp,
                    color = TextNoir,
                    lineHeight = 19.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        successRegisterDialogMessage = null
                        isLoginTab = true
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = DustyMerlot)
                ) {
                    Text("Đến trang Đăng nhập", fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = WarmWhite
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Logo & Header Brand
            IconMedallion(
                icon = GlowIcons.Diamond,
                size = 56.dp,
                iconSize = 28.dp,
                tint = ChampagneGold,
                contentDescription = "Glow Prestige Crest"
            )
            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "GLOW PRESTIGE",
                fontSize = 22.sp,
                fontWeight = FontWeight.Black,
                color = DustyMerlot,
                letterSpacing = 1.5.sp
            )
            Text(
                text = "Nền tảng Làm đẹp Thượng lưu & Đặt lịch Salon",
                fontSize = 12.sp,
                color = TextMuted
            )

            Spacer(modifier = Modifier.height(20.dp))

            // ───────────────── THANH CHUYỂN TAB: [ĐĂNG NHẬP | ĐĂNG KÝ] ─────────────────
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color.White,
                border = BorderStroke(1.dp, BorderSilk),
                shadowElevation = 1.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Tab Đăng nhập
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isLoginTab) DustyMerlot else Color.Transparent)
                            .clickable {
                                isLoginTab = true
                                errorMessage = null
                            }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "ĐĂNG NHẬP",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isLoginTab) Color.White else TextMuted
                        )
                    }

                    // Tab Đăng ký
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (!isLoginTab) DustyMerlot else Color.Transparent)
                            .clickable {
                                isLoginTab = false
                                errorMessage = null
                            }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "ĐĂNG KÝ GMAIL",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (!isLoginTab) Color.White else TextMuted
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Card Form Nhập Liệu
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, BorderSilk),
                elevation = CardDefaults.cardElevation(2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = if (isLoginTab) "Đăng nhập tài khoản" else "Đăng ký chỉ với Gmail & Mật khẩu",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextNoir
                    )

                    // Thông báo lỗi nếu có
                    if (errorMessage != null) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFFFEE2E2))
                                .padding(10.dp)
                        ) {
                            Text(
                                text = errorMessage!!,
                                fontSize = 12.sp,
                                color = Color(0xFFDC2626),
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    // 1. Ô nhập Gmail
                    OutlinedTextField(
                        value = gmail,
                        onValueChange = {
                            gmail = it
                            errorMessage = null
                        },
                        label = { Text("Gmail của bạn") },
                        placeholder = { Text("ví dụ: yourname@gmail.com", color = TextMuted) },
                        leadingIcon = {
                            Icon(imageVector = Icons.Default.Email, contentDescription = null, tint = DustyMerlot)
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = DustyMerlot,
                            unfocusedBorderColor = BorderSilk,
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White
                        )
                    )

                    // 2. Ô nhập Mật khẩu
                    OutlinedTextField(
                        value = password,
                        onValueChange = {
                            password = it
                            errorMessage = null
                        },
                        label = { Text("Mật khẩu") },
                        placeholder = { Text("Nhập mật khẩu...", color = TextMuted) },
                        leadingIcon = {
                            Icon(imageVector = Icons.Default.Lock, contentDescription = null, tint = DustyMerlot)
                        },
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = null,
                                    tint = TextMuted
                                )
                            }
                        },
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = DustyMerlot,
                            unfocusedBorderColor = BorderSilk,
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White
                        )
                    )

                    // ───────────────── PHẦN RIÊNG CỦA TAB ĐĂNG KÝ ─────────────────
                    if (!isLoginTab) {
                        Text(
                            text = "Chọn loại tài khoản được cấp:",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextNoir
                        )

                        // 3 Nút chọn Role: Khách hàng / Cửa hàng / Admin
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // 1. Khách hàng
                            OutlinedButton(
                                onClick = { selectedRole = UserRole.CUSTOMER },
                                modifier = Modifier.weight(1f).height(44.dp),
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(if (selectedRole == UserRole.CUSTOMER) 2.dp else 1.dp, if (selectedRole == UserRole.CUSTOMER) DustyMerlot else BorderSilk),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = if (selectedRole == UserRole.CUSTOMER) PinkPanther.copy(alpha = 0.3f) else Color.White
                                ),
                                contentPadding = PaddingValues(horizontal = 4.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Icon(imageVector = Icons.Default.Person, contentDescription = null, tint = DustyMerlot, modifier = Modifier.size(16.dp))
                                    Text("Khách", fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = DustyMerlot)
                                }
                            }

                            // 2. Cửa hàng
                            OutlinedButton(
                                onClick = { selectedRole = UserRole.MERCHANT },
                                modifier = Modifier.weight(1.2f).height(44.dp),
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(if (selectedRole == UserRole.MERCHANT) 2.dp else 1.dp, if (selectedRole == UserRole.MERCHANT) DustyMerlot else BorderSilk),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = if (selectedRole == UserRole.MERCHANT) PinkPanther.copy(alpha = 0.3f) else Color.White
                                ),
                                contentPadding = PaddingValues(horizontal = 4.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Icon(imageVector = Icons.Default.Storefront, contentDescription = null, tint = DustyMerlot, modifier = Modifier.size(16.dp))
                                    Text("Cửa hàng", fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = DustyMerlot)
                                }
                            }

                            // 3. Admin
                            OutlinedButton(
                                onClick = { selectedRole = UserRole.ADMIN },
                                modifier = Modifier.weight(1f).height(44.dp),
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(if (selectedRole == UserRole.ADMIN) 2.dp else 1.dp, if (selectedRole == UserRole.ADMIN) DustyMerlot else BorderSilk),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = if (selectedRole == UserRole.ADMIN) PinkPanther.copy(alpha = 0.3f) else Color.White
                                ),
                                contentPadding = PaddingValues(horizontal = 4.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Icon(imageVector = Icons.Default.Shield, contentDescription = null, tint = DustyMerlot, modifier = Modifier.size(16.dp))
                                    Text("Admin", fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = DustyMerlot)
                                }
                            }
                        }

                        // Ghi chú về phê duyệt cửa hàng
                        if (selectedRole == UserRole.MERCHANT) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFFFEF3C7))
                                    .padding(10.dp)
                            ) {
                                Text(
                                    text = "⚠️ Quy định: Tài khoản Cửa hàng sau khi đăng ký sẽ cần được ADMIN PHÊ DUYỆT mới có thể bắt đầu sử dụng.",
                                    fontSize = 11.sp,
                                    color = Color(0xFFB45309),
                                    fontWeight = FontWeight.Medium
                                )
                            }

                            OutlinedTextField(
                                value = salonName,
                                onValueChange = { salonName = it },
                                label = { Text("Tên Cửa hàng / Salon của bạn") },
                                placeholder = { Text("ví dụ: 1994 Nailsroom, Thì Là Spa...") },
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = DustyMerlot,
                                    unfocusedBorderColor = BorderSilk,
                                    focusedContainerColor = Color.White,
                                    unfocusedContainerColor = Color.White
                                )
                            )
                        } else if (selectedRole == UserRole.ADMIN) {
                            OutlinedTextField(
                                value = adminSecretCode,
                                onValueChange = { adminSecretCode = it },
                                label = { Text("Mã xác thực quyền Admin") },
                                placeholder = { Text("Nhập: GLOW_ADMIN") },
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = DustyMerlot,
                                    unfocusedBorderColor = BorderSilk,
                                    focusedContainerColor = Color.White,
                                    unfocusedContainerColor = Color.White
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // ───────────────── NÚT THỰC HIỆN CHÍNH ─────────────────
                    Button(
                        onClick = {
                            errorMessage = null
                            if (isLoginTab) {
                                // Thực hiện Đăng nhập
                                when (val res = userStorage.login(gmail, password)) {
                                    is UserLocalStorage.LoginResult.Success -> {
                                        Toast.makeText(context, "Chào mừng ${res.user.fullName} trở lại! ✨", Toast.LENGTH_SHORT).show()
                                        onLoginSuccess(res.user)
                                    }
                                    is UserLocalStorage.LoginResult.MerchantPendingApproval -> {
                                        pendingMerchantDialogMessage = res.message
                                    }
                                    is UserLocalStorage.LoginResult.Error -> {
                                        errorMessage = res.message
                                    }
                                }
                            } else {
                                // Thực hiện Đăng ký
                                if (selectedRole == UserRole.ADMIN && adminSecretCode.trim() != "GLOW_ADMIN") {
                                    errorMessage = "Mã xác thực Admin không hợp lệ! Vui lòng nhập: GLOW_ADMIN"
                                    return@Button
                                }

                                when (val res = userStorage.register(gmail, password, selectedRole, salonName)) {
                                    is UserLocalStorage.RegisterResult.Success -> {
                                        if (res.isPendingApproval) {
                                            successRegisterDialogMessage = "Tài khoản Cửa hàng (${res.user.gmail}) đã được tạo thành công!\n\nTheo quy chế sàn, hồ sơ của bạn đang CHỜ ADMIN PHÊ DUYỆT. Sau khi Admin chấp nhận, bạn sẽ có thể đăng nhập để quản lý salon."
                                        } else {
                                            Toast.makeText(context, "Đăng ký thành công! Đang tự động đăng nhập...", Toast.LENGTH_SHORT).show()
                                            onLoginSuccess(res.user)
                                        }
                                    }
                                    is UserLocalStorage.RegisterResult.Error -> {
                                        errorMessage = res.message
                                    }
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = DustyMerlot)
                    ) {
                        Text(
                            text = if (isLoginTab) "ĐĂNG NHẬP" else "ĐĂNG KÝ NGAY",
                            fontSize = 14.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // ───────────────── KHỐI ĐĂNG NHẬP NHANH 3 TÀI KHOẢN ĐƯỢC CẤP ─────────────────
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, BorderSilk),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Icon(imageVector = Icons.Default.Key, contentDescription = null, tint = ChampagneGold, modifier = Modifier.size(16.dp))
                        Text(
                            text = "Tài khoản mẫu được cấp sẵn (Bấm để điền nhanh):",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextNoir
                        )
                    }

                    // 1. Khách hàng
                    QuickAccountRow(
                        roleLabel = "Khách hàng",
                        email = "khach@gmail.com",
                        pass = "khach",
                        tagColor = SuccessGreen,
                        onClick = {
                            gmail = "khach@gmail.com"
                            password = "khach"
                            isLoginTab = true
                            errorMessage = null
                        }
                    )

                    // 2. Cửa hàng đã duyệt
                    QuickAccountRow(
                        roleLabel = "Cửa hàng",
                        email = "salon@gmail.com",
                        pass = "salon",
                        tagColor = Color(0xFF2563EB),
                        onClick = {
                            gmail = "salon@gmail.com"
                            password = "salon"
                            isLoginTab = true
                            errorMessage = null
                        }
                    )

                    // 3. Admin
                    QuickAccountRow(
                        roleLabel = "Admin",
                        email = "admin@gmail.com",
                        pass = "admin",
                        tagColor = DustyMerlot,
                        onClick = {
                            gmail = "admin@gmail.com"
                            password = "admin"
                            isLoginTab = true
                            errorMessage = null
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Bằng việc tiếp tục, bạn đồng ý với Điều khoản Sử dụng & Chính sách Bảo mật của Glow Prestige",
                fontSize = 11.sp,
                color = TextMuted,
                textAlign = TextAlign.Center,
                lineHeight = 16.sp
            )
        }
    }
}

@Composable
private fun QuickAccountRow(
    roleLabel: String,
    email: String,
    pass: String,
    tagColor: Color,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = Color(0xFFF9FAFB),
        border = BorderStroke(1.dp, Color(0xFFE5E7EB)),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(tagColor.copy(alpha = 0.15f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(text = roleLabel, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = tagColor)
                }
                Text(text = email, fontSize = 11.5.sp, fontWeight = FontWeight.Medium, color = TextNoir)
            }
            Text(text = "Dùng tài khoản >", fontSize = 10.5.sp, fontWeight = FontWeight.Bold, color = DustyMerlot)
        }
    }
}
