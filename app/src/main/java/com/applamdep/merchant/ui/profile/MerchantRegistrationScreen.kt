package com.applamdep.merchant.ui.profile

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.applamdep.customer.core.di.ViewModelFactory
import com.applamdep.customer.ui.components.GrabButton
import com.applamdep.customer.ui.components.GrabCard
import com.applamdep.customer.ui.theme.*
import com.applamdep.merchant.domain.model.MerchantProfile

/**
 * ĐĂNG KÝ CƠ SỞ MỚI & GỬI HỒ SƠ XÉT DUYỆT (MERCHANT REQUIREMENT #1)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MerchantRegistrationScreen(
    onBack: () -> Unit,
    onRegistrationSubmitted: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: MerchantProfileViewModel = viewModel(factory = ViewModelFactory.Factory)
) {
    val context = LocalContext.current

    var shopName by remember { mutableStateOf("") }
    var ownerName by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var licenseNumber by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var plan by remember { mutableStateOf("NORMAL") }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = GrabBackground,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Đăng ký Cửa hàng Mới",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = GrabTextPrimary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = GrabTextPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = GrabSurface)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            GrabCard {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Thông tin đăng ký cơ sở làm đẹp",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = GrabTextPrimary
                    )

                    OutlinedTextField(
                        value = shopName,
                        onValueChange = { shopName = it },
                        label = { Text("Tên cơ sở / Salon *") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = ownerName,
                            onValueChange = { ownerName = it },
                            label = { Text("Chủ cơ sở *") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = phone,
                            onValueChange = { phone = it },
                            label = { Text("Hotline *") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email liên hệ") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = address,
                        onValueChange = { address = it },
                        label = { Text("Địa chỉ đăng ký kinh doanh *") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = licenseNumber,
                        onValueChange = { licenseNumber = it },
                        label = { Text("Số giấy phép / Mã số thuế cơ sở") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Giới thiệu ngắn về dịch vụ chủ đạo") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(80.dp)
                    )

                    // Chọn gói
                    Text(
                        text = "Chọn gói dịch vụ đối tác:",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = GrabTextPrimary
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = plan == "NORMAL",
                            onClick = { plan = "NORMAL" },
                            label = { Text("Gói Tiêu chuẩn (Miễn phí)") }
                        )
                        FilterChip(
                            selected = plan == "VIP",
                            onClick = { plan = "VIP" },
                            label = { Text("Gói VIP Sponsor (Ưu tiên TOP)") }
                        )
                    }
                }
            }

            GrabButton(
                text = "Gửi hồ sơ xét duyệt",
                onClick = {
                    if (shopName.isBlank() || ownerName.isBlank() || phone.isBlank() || address.isBlank()) {
                        Toast.makeText(context, "Vui lòng điền đầy đủ các mục đánh dấu (*)", Toast.LENGTH_SHORT).show()
                        return@GrabButton
                    }

                    val newProfile = MerchantProfile(
                        id = "",
                        ownerId = "owner_" + System.currentTimeMillis().toString().takeLast(4),
                        shopName = shopName.trim(),
                        ownerName = ownerName.trim(),
                        phone = phone.trim(),
                        email = email.trim(),
                        address = address.trim(),
                        description = description.trim(),
                        licenseNumber = licenseNumber.trim(),
                        membershipPlan = plan,
                        status = "PENDING"
                    )

                    viewModel.registerNewShop(
                        profile = newProfile,
                        onSuccess = { newId ->
                            Toast.makeText(
                                context,
                                "Gửi hồ sơ thành công! Mã cơ sở: $newId. Hồ sơ đang được Admin thẩm định.",
                                Toast.LENGTH_LONG
                            ).show()
                            onRegistrationSubmitted()
                        },
                        onError = { Toast.makeText(context, it, Toast.LENGTH_SHORT).show() }
                    )
                },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
