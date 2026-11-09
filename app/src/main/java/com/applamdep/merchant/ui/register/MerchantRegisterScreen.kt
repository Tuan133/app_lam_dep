package com.applamdep.merchant.ui.register

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.app_lam_dep.R
import com.applamdep.merchant.core.base.UiState
import com.applamdep.merchant.core.constants.MerchantConstants
import com.applamdep.merchant.core.util.Formatters
import com.applamdep.merchant.domain.rule.PaymentResult
import com.applamdep.merchant.ui.component.MerchantTopBar
import com.applamdep.merchant.ui.theme.*
import androidx.lifecycle.viewmodel.compose.viewModel
import com.applamdep.merchant.core.di.MerchantViewModelFactory

@Composable
fun MerchantRegisterScreen(
    viewModel: MerchantRegisterViewModel = viewModel(factory = MerchantViewModelFactory.DefaultFactory),
    ownerUid: String = "merchant_user_01",
    onRegistrationSubmitted: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val form by viewModel.formData.collectAsState()
    val config by viewModel.config.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    val paymentModal by viewModel.paymentModal.collectAsState()

    var customDepositInput by remember { mutableStateOf(form.depositOptions.joinToString(", ")) }

    val currentFee = viewModel.calculateCurrentFee()

    Scaffold(
        topBar = {
            MerchantTopBar(
                title = stringResource(R.string.merchant_register_title),
                onBackClick = onBack
            )
        },
        containerColor = MerchantBackground,
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Banner giới thiệu
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MerchantPrimaryLight),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Hợp tác kinh doanh cùng AppLamDep",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = MerchantPrimaryDark
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = stringResource(R.string.merchant_register_subtitle),
                            fontSize = 13.sp,
                            color = MerchantTextSecondary,
                            lineHeight = 18.sp
                        )
                    }
                }

                // KHỐI 1: THÔNG TIN CƠ SỞ
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MerchantSurface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            text = "1. Thông tin cửa hàng",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = MerchantTextPrimary
                        )

                        OutlinedTextField(
                            value = form.name,
                            onValueChange = { viewModel.updateFormData { f -> f.copy(name = it) } },
                            label = { Text(stringResource(R.string.merchant_field_shop_name) + " *") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = form.ownerName,
                                onValueChange = { viewModel.updateFormData { f -> f.copy(ownerName = it) } },
                                label = { Text(stringResource(R.string.merchant_field_owner_name) + " *") },
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = form.phone,
                                onValueChange = { viewModel.updateFormData { f -> f.copy(phone = it) } },
                                label = { Text(stringResource(R.string.merchant_field_phone) + " *") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        OutlinedTextField(
                            value = form.email,
                            onValueChange = { viewModel.updateFormData { f -> f.copy(email = it) } },
                            label = { Text(stringResource(R.string.merchant_field_email)) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = form.address,
                            onValueChange = { viewModel.updateFormData { f -> f.copy(address = it) } },
                            label = { Text(stringResource(R.string.merchant_field_address) + " *") },
                            trailingIcon = {
                                Icon(Icons.Default.LocationOn, contentDescription = "Tọa độ", tint = MerchantPrimary)
                            },
                            modifier = Modifier.fillMaxWidth()
                        )

                        Text(
                            text = "Tọa độ GPS demo: Lat ${form.lat}, Lng ${form.lng}",
                            fontSize = 11.sp,
                            color = MerchantTextSecondary
                        )

                        OutlinedTextField(
                            value = form.openingHours,
                            onValueChange = { viewModel.updateFormData { f -> f.copy(openingHours = it) } },
                            label = { Text(stringResource(R.string.merchant_field_hours)) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = form.description,
                            onValueChange = { viewModel.updateFormData { f -> f.copy(description = it) } },
                            label = { Text(stringResource(R.string.merchant_field_description)) },
                            modifier = Modifier.fillMaxWidth().height(80.dp)
                        )
                    }
                }

                // KHỐI 2: CHỌN GÓI HỘI VIÊN & KỲ HẠN
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MerchantSurface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            text = "2. " + stringResource(R.string.merchant_field_plan_title),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = MerchantTextPrimary
                        )

                        // Gói Thường vs VIP
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            PlanOptionCard(
                                title = "Gói Tiêu chuẩn",
                                subtitle = "Hiển thị sàn & bản đồ",
                                isSelected = form.plan == MerchantConstants.PLAN_NORMAL,
                                isVip = false,
                                onClick = { viewModel.updateFormData { f -> f.copy(plan = MerchantConstants.PLAN_NORMAL) } },
                                modifier = Modifier.weight(1f)
                            )
                            PlanOptionCard(
                                title = "Gói Đối tác VIP",
                                subtitle = "Ưu tiên TOP tìm kiếm",
                                isSelected = form.plan == MerchantConstants.PLAN_VIP,
                                isVip = true,
                                onClick = { viewModel.updateFormData { f -> f.copy(plan = MerchantConstants.PLAN_VIP) } },
                                modifier = Modifier.weight(1f)
                            )
                        }

                        // Chọn kỳ hạn
                        Text(text = "Kỳ hạn đăng ký:", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = MerchantTextPrimary)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf(3 to "3 tháng", 6 to "6 tháng", 12 to "12 tháng").forEach { (term, label) ->
                                FilterChip(
                                    selected = form.planTermMonths == term,
                                    onClick = { viewModel.updateFormData { f -> f.copy(planTermMonths = term) } },
                                    label = { Text(label) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = MerchantPrimary,
                                        selectedLabelColor = Color.White
                                    )
                                )
                            }
                        }

                        // Hiển thị tính phí và khuyến mãi
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(if (config.isFirstMonthFree) Color(0xFFFFF9E6) else MerchantSurfaceVariant, RoundedCornerShape(12.dp))
                                .padding(12.dp)
                        ) {
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(text = "Phí niêm yết hệ thống:", fontSize = 13.sp, color = MerchantTextSecondary)
                                    Text(
                                        text = Formatters.formatCurrency(currentFee),
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MerchantTextPrimary
                                    )
                                }
                                if (config.isFirstMonthFree) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = stringResource(R.string.merchant_first_month_free),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MerchantVipGold
                                    )
                                }
                            }
                        }
                    }
                }

                // KHỐI 3: THIẾT LẬP CHÍNH SÁCH ĐẶT CỌC (ĐÚNG 5 MỆNH GIÁ)
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MerchantSurface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "3. " + stringResource(R.string.merchant_deposit_policy_title),
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MerchantTextPrimary
                                )
                                Text(
                                    text = stringResource(R.string.merchant_enable_deposit),
                                    fontSize = 12.sp,
                                    color = MerchantTextSecondary
                                )
                            }
                            Switch(
                                checked = form.requireDeposit,
                                onCheckedChange = { isChecked ->
                                    viewModel.updateFormData { f -> f.copy(requireDeposit = isChecked) }
                                },
                                colors = SwitchDefaults.colors(checkedThumbColor = MerchantPrimary)
                            )
                        }

                        if (form.requireDeposit) {
                            Text(
                                text = stringResource(R.string.merchant_deposit_options_hint),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MerchantTextPrimary
                            )

                            // 5 mức cọc được hiển thị trực quan
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                form.depositOptions.forEach { opt ->
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .background(MerchantPrimaryLight, RoundedCornerShape(8.dp))
                                            .padding(vertical = 8.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "${opt / 1000}k",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MerchantPrimaryDark
                                        )
                                    }
                                }
                            }

                            OutlinedTextField(
                                value = customDepositInput,
                                onValueChange = { input ->
                                    customDepositInput = input
                                    val parsed = input.split(",")
                                        .mapNotNull { it.trim().toLongOrNull() }
                                    viewModel.updateFormData { f -> f.copy(depositOptions = parsed) }
                                },
                                label = { Text("Tùy chỉnh 5 mức cọc cách nhau dấu phẩy (,)") },
                                placeholder = { Text("50000, 100000, 150000, 200000, 500000") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }

                // NÚT GỬI HỒ SƠ
                Button(
                    onClick = {
                        viewModel.submitRegistration(
                            ownerUid = ownerUid,
                            onSuccess = { shopId ->
                                Toast.makeText(
                                    context,
                                    "Gửi hồ sơ thành công! Mã shop: $shopId. Đang chờ duyệt.",
                                    Toast.LENGTH_LONG
                                ).show()
                                onRegistrationSubmitted()
                            },
                            onError = { errorMsg ->
                                Toast.makeText(context, errorMsg, Toast.LENGTH_LONG).show()
                            }
                        )
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MerchantPrimary),
                    enabled = uiState !is UiState.Loading,
                    modifier = Modifier.fillMaxWidth().height(52.dp)
                ) {
                    if (uiState is UiState.Loading) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                    } else {
                        Text(
                            text = stringResource(R.string.merchant_submit_registration),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // MODAL GIẢ LẬP THANH TOÁN QR (NẾU CÓ PHÍ)
            if (paymentModal is PaymentResult.QrPending) {
                val qrData = paymentModal as PaymentResult.QrPending
                PlanQrPaymentDialog(
                    qrUrl = qrData.qrUrl,
                    amount = qrData.amount,
                    onDismiss = { viewModel.dismissPaymentModal() },
                    onConfirm = {
                        viewModel.confirmPaymentAndFinish {
                            Toast.makeText(context, "Xác nhận thanh toán phí gói thành công!", Toast.LENGTH_SHORT).show()
                            onRegistrationSubmitted()
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun PlanOptionCard(
    title: String,
    subtitle: String,
    isSelected: Boolean,
    isVip: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val borderColor = if (isSelected) (if (isVip) MerchantVipGold else MerchantPrimary) else Color(0xFFE5E7EB)
    val bgColor = if (isSelected) (if (isVip) Color(0xFFFFFDF5) else Color(0xFFF0FDF4)) else MerchantSurface

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(bgColor)
            .border(width = if (isSelected) 2.dp else 1.dp, color = borderColor, shape = RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(12.dp)
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (isVip) {
                    Icon(Icons.Default.Star, contentDescription = "VIP", tint = MerchantVipGold, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                }
                Text(
                    text = title,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isVip) MerchantVipGold else MerchantTextPrimary
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = subtitle, fontSize = 11.sp, color = MerchantTextSecondary)
        }
    }
}

@Composable
private fun PlanQrPaymentDialog(
    qrUrl: String,
    amount: Long,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MerchantSurface),
            modifier = Modifier.fillMaxWidth().padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Thanh toán phí gói Hội viên",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = MerchantTextPrimary
                )

                Icon(
                    imageVector = Icons.Default.QrCode2,
                    contentDescription = "Mã QR",
                    tint = MerchantPrimary,
                    modifier = Modifier.size(140.dp)
                )

                Text(
                    text = "Số tiền thanh toán: ${Formatters.formatCurrency(amount)}",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = MerchantPrimary
                )

                Text(
                    text = "Quét mã để chuyển tới trang xác nhận thanh toán giả lập của sàn (Demo Web Portal).",
                    fontSize = 12.sp,
                    color = MerchantTextSecondary,
                    lineHeight = 16.sp
                )

                Button(
                    onClick = onConfirm,
                    colors = ButtonDefaults.buttonColors(containerColor = MerchantPrimary),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().height(48.dp)
                ) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Xác nhận đã thanh toán", fontWeight = FontWeight.Bold)
                }

                TextButton(onClick = onDismiss) {
                    Text("Bỏ qua / Để sau", color = MerchantTextSecondary)
                }
            }
        }
    }
}
