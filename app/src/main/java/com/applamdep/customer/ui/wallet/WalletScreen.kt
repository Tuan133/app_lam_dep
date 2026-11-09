package com.applamdep.customer.ui.wallet

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import android.widget.Toast
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.applamdep.customer.core.base.UiState
import com.applamdep.customer.core.constants.AppConstants
import com.applamdep.customer.core.di.ViewModelFactory
import com.applamdep.customer.core.util.CurrencyFormatter
import com.applamdep.customer.core.util.DateTimeFormatter
import com.applamdep.customer.domain.model.*
import com.applamdep.customer.ui.components.*
import com.applamdep.customer.ui.theme.*

/**
 * MÀN HÌNH VÍ TIỀN & LỊCH SỬ GIAO DỊCH (NHÓM 4 - P0)
 * - Hiển thị số dư ví theo tỷ giá sàn quy định
 * - Nạp tiền vào ví qua QR giả lập
 * - Lịch sử: Nạp ví, Sàn giữ cọc (HELD), Hoàn cọc (REFUNDED), Giải ngân cho tiệm (RELEASED_TO_SHOP)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WalletScreen(
    onNavigateToQrPayment: (TopUpTransaction) -> Unit,
    onBack: () -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: WalletViewModel = viewModel(factory = ViewModelFactory.Factory)
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showTopUpDialog by remember { mutableStateOf(false) }
    var showAddPaymentDialog by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = GrabBackground,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Ví GLOW Pay",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = GrabTextPrimary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Quay lại",
                            tint = GrabTextPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = GrabSurface)
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (val state = uiState) {
                is UiState.Idle, is UiState.Loading -> GrabLoadingState()
                is UiState.Error -> GrabEmptyState(
                    message = state.message,
                    onRetry = { viewModel.loadWalletData() }
                )
                is UiState.Success -> {
                    val walletData = state.data
                    val balance = walletData.wallet.balance
                    val transactions = walletData.transactions

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // 1. THẺ SỐ DƯ VÍ PHONG CÁCH GRABPAY
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(20.dp),
                                colors = CardDefaults.cardColors(containerColor = GrabGreen),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(20.dp)
                                ) {
                                    Text(
                                        text = "SỐ DƯ KHẢ DỤNG",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White.copy(alpha = 0.85f),
                                        letterSpacing = 1.sp
                                    )

                                    Spacer(modifier = Modifier.height(10.dp))

                                    Text(
                                        text = CurrencyFormatter.formatVnd(balance),
                                        fontSize = 28.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color.White
                                    )

                                    Spacer(modifier = Modifier.height(16.dp))

                                    // Nút nạp tiền
                                    Button(
                                        onClick = { showTopUpDialog = true },
                                        shape = RoundedCornerShape(12.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.AddCard,
                                            contentDescription = null,
                                            tint = GrabGreenDark,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "Nạp tiền vào ví",
                                            color = GrabGreenDark,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }

                        // 1.5. PHƯƠNG THỨC THANH TOÁN & LIÊN KẾT CỌC (TC_CUST_PROFILE_009)
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "Phương thức thanh toán & Cọc",
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = GrabTextPrimary
                                        )
                                        Text(
                                            text = "+ Thêm mới",
                                            fontSize = 12.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = GrabGreenDark,
                                            modifier = Modifier.clickable { showAddPaymentDialog = true }
                                        )
                                    }

                                    HorizontalDivider(color = Color(0xFFF0F0F0))

                                    // Phương thức 1: VietQR Napas247 (Mặc định)
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(36.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(Color(0xFFE8F5E9)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(imageVector = Icons.Default.QrCodeScanner, contentDescription = null, tint = GrabGreenDark, modifier = Modifier.size(20.dp))
                                        }
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(text = "VietQR Napas 24/7 (Quét mã tức thì)", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = GrabTextPrimary)
                                            Text(text = "Đã liên kết · Tự động khớp lệnh cọc trong 3s", fontSize = 11.sp, color = GrabTextSecondary)
                                        }
                                        Text(text = "Mặc định", fontSize = 11.sp, color = GrabGreenDark, fontWeight = FontWeight.Bold)
                                    }

                                    // Phương thức 2: Thẻ Quốc Tế Visa/Mastercard
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(36.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(Color(0xFFEDE7F6)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(imageVector = Icons.Default.CreditCard, contentDescription = null, tint = Color(0xFF5E35B1), modifier = Modifier.size(20.dp))
                                        }
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(text = "Visa / Mastercard ···· 8899", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = GrabTextPrimary)
                                            Text(text = "Hết hạn: 12/28 · Hỗ trợ thanh toán 1 chạm", fontSize = 11.sp, color = GrabTextSecondary)
                                        }
                                        Text(text = "Đã bật", fontSize = 11.sp, color = Color(0xFF5E35B1), fontWeight = FontWeight.Bold)
                                    }

                                    // Phương thức 3: Ví điện tử MoMo
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(36.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(Color(0xFFFCE4EC)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(imageVector = Icons.Default.AccountBalanceWallet, contentDescription = null, tint = Color(0xFFD81B60), modifier = Modifier.size(20.dp))
                                        }
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(text = "Ví MoMo e-Wallet", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = GrabTextPrimary)
                                            Text(text = "0903 *** 456 · Đã liên kết hoàn cọc tự động", fontSize = 11.sp, color = GrabTextSecondary)
                                        }
                                        Text(text = "Liên kết", fontSize = 11.sp, color = Color(0xFFD81B60), fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }

                        // 2. LỊCH SỬ GIAO DỊCH
                        item {
                            Text(
                                text = "Lịch sử giao dịch",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = GrabTextPrimary
                            )
                        }

                        if (transactions.isEmpty()) {
                            item {
                                GrabEmptyState(message = "Chưa có giao dịch tài chính nào.")
                            }
                        } else {
                            items(
                                items = transactions,
                                key = { it.id }
                            ) { tx ->
                                TransactionRowItem(transaction = tx)
                            }
                        }
                    }
                }
            }

            // DIALOG QUẢN LÝ & THÊM PHƯƠNG THỨC THANH TOÁN CỌC (TC_CUST_PROFILE_009)
            if (showAddPaymentDialog) {
                var methodType by remember { mutableStateOf("VietQR") }
                var cardNumber by remember { mutableStateOf("") }
                var cardHolder by remember { mutableStateOf("") }

                AlertDialog(
                    onDismissRequest = { showAddPaymentDialog = false },
                    title = {
                        Text(text = "Liên kết phương thức cọc & ví", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = GrabTextPrimary)
                    },
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text(
                                text = "Chọn phương thức thanh toán muốn liên kết với tài khoản GLOW Pay:",
                                fontSize = 13.sp,
                                color = GrabTextSecondary
                            )

                            listOf("VietQR Napas 24/7", "Thẻ Visa / Mastercard Quốc Tế", "Ví MoMo / ZaloPay").forEach { opt ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(if (methodType == opt) Color(0xFFE8F5E9) else Color(0xFFF9F9F9))
                                        .border(1.dp, if (methodType == opt) GrabGreenDark else Color(0xFFEEEEEE), RoundedCornerShape(10.dp))
                                        .clickable { methodType = opt }
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(text = opt, fontWeight = FontWeight.Medium, fontSize = 13.sp, color = GrabTextPrimary)
                                    if (methodType == opt) {
                                        Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = GrabGreenDark, modifier = Modifier.size(18.dp))
                                    }
                                }
                            }

                            if (methodType == "Thẻ Visa / Mastercard Quốc Tế") {
                                OutlinedTextField(
                                    value = cardNumber,
                                    onValueChange = { cardNumber = it },
                                    label = { Text("Số thẻ (16 số)") },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth()
                                )
                                OutlinedTextField(
                                    value = cardHolder,
                                    onValueChange = { cardHolder = it },
                                    label = { Text("Tên in trên thẻ") },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                showAddPaymentDialog = false
                                Toast.makeText(context, "Đã liên kết $methodType thành công! ✅", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = GrabGreenDark)
                        ) {
                            Text("Liên kết ngay", fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showAddPaymentDialog = false }) {
                            Text("Đóng", color = GrabTextSecondary)
                        }
                    },
                    containerColor = Color.White,
                    shape = RoundedCornerShape(16.dp)
                )
            }

            // BOTTOM SHEET / DIALOG CHỌN MỆNH GIÁ NẠP TIỀN
            if (showTopUpDialog) {
                TopUpAmountDialog(
                    onDismiss = { showTopUpDialog = false },
                    onConfirmAmount = { amount ->
                        showTopUpDialog = false
                        viewModel.createTopUp(
                            amount = amount,
                            onQrReady = { tx ->
                                onNavigateToQrPayment(tx)
                            }
                        )
                    }
                )
            }
        }
    }
}

/**
 * Item dòng giao dịch
 */
@Composable
private fun TransactionRowItem(transaction: Transaction) {
    GrabCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon đại diện
            val (icon, iconBg, iconTint) = when (transaction) {
                is TopUpTransaction -> Triple(Icons.Default.ArrowDownward, GrabGreenLight, GrabGreenDark)
                is DepositTransaction -> Triple(Icons.Default.Lock, Color(0xFFFFF0F0), GrabStatusError)
                is RefundTransaction -> Triple(Icons.Default.Refresh, GrabGreenLight, GrabGreenDark)
                is ReleasedToShopTransaction -> Triple(Icons.Default.Storefront, Color(0xFFE6F0FF), Color(0xFF0056B3))
            }

            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(iconBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(20.dp))
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Chi tiết nội dung
            Column(modifier = Modifier.weight(1f)) {
                val title = when (transaction) {
                    is TopUpTransaction -> "Nạp tiền Ví GLOW Pay"
                    is DepositTransaction -> "Tạm giữ cọc đặt lịch (${transaction.bookingId})"
                    is RefundTransaction -> "Hoàn cọc hủy lịch (${transaction.bookingId})"
                    is ReleasedToShopTransaction -> "Giải ngân cọc cho tiệm"
                }

                Text(
                    text = title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = GrabTextPrimary
                )

                Text(
                    text = DateTimeFormatter.formatDateTime(transaction.createdAt),
                    fontSize = 11.sp,
                    color = GrabTextSecondary
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Số tiền & Trạng thái
            Column(horizontalAlignment = Alignment.End) {
                val isPositive = transaction is TopUpTransaction || transaction is RefundTransaction
                val prefix = if (isPositive) "+" else "-"
                val amountColor = when {
                    transaction.status == AppConstants.TX_STATUS_HELD -> GrabStatusError
                    isPositive -> GrabGreenDark
                    else -> GrabTextPrimary
                }

                Text(
                    text = "$prefix${CurrencyFormatter.formatVnd(transaction.amount)}",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = amountColor
                )

                val statusText = when (transaction.status) {
                    AppConstants.TX_STATUS_HELD -> "SÀN TẠM GIỮ"
                    AppConstants.TX_STATUS_SUCCESS -> "THÀNH CÔNG"
                    AppConstants.TX_STATUS_REFUNDED -> "ĐÃ HOÀN VÍ"
                    AppConstants.TX_STATUS_RELEASED -> "ĐÃ GIẢI NGÂN"
                    AppConstants.TX_STATUS_PENDING -> "CHỜ QUÉT QR"
                    else -> transaction.status
                }

                Text(
                    text = statusText,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (transaction.status == AppConstants.TX_STATUS_HELD) GrabStatusError else GrabTextSecondary
                )
            }
        }
    }
}

/**
 * Dialog chọn mệnh giá nạp tiền
 */
@Composable
private fun TopUpAmountDialog(
    onDismiss: () -> Unit,
    onConfirmAmount: (Long) -> Unit
) {
    val predefinedAmounts = listOf(100000L, 200000L, 500000L, 1000000L, 2000000L)
    var selectedAmount by remember { mutableLongStateOf(500000L) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = GrabSurface,
        shape = RoundedCornerShape(16.dp),
        title = {
            Text(
                text = "Nạp tiền vào ví",
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = GrabTextPrimary
            )
        },
        text = {
            Column {
                Text(
                    text = "Chọn mệnh giá bạn muốn nạp (VietQR giả lập):",
                    fontSize = 13.sp,
                    color = GrabTextSecondary
                )
                Spacer(modifier = Modifier.height(14.dp))

                predefinedAmounts.forEach { amount ->
                    val isSelected = amount == selectedAmount
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isSelected) GrabGreenLight else GrabSurface)
                            .border(
                                width = 1.dp,
                                color = if (isSelected) GrabGreen else GrabBorder,
                                shape = RoundedCornerShape(10.dp)
                            )
                            .clickable { selectedAmount = amount }
                            .padding(horizontal = 14.dp, vertical = 10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = CurrencyFormatter.formatVnd(amount),
                                fontSize = 14.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) GrabGreenDark else GrabTextPrimary
                            )
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = GrabGreenDark,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            GrabPrimaryButton(
                text = "Tạo mã QR thanh toán",
                onClick = { onConfirmAmount(selectedAmount) }
            )
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Hủy bỏ", color = GrabTextSecondary)
            }
        }
    )
}
