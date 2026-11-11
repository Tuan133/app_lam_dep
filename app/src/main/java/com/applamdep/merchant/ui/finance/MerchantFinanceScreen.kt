package com.applamdep.merchant.ui.finance

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.app_lam_dep.R
import com.applamdep.merchant.core.base.UiState
import com.applamdep.merchant.core.constants.MerchantConstants
import com.applamdep.merchant.core.di.MerchantViewModelFactory
import com.applamdep.merchant.core.util.Formatters
import com.applamdep.merchant.domain.model.Transaction
import com.applamdep.merchant.ui.component.EmptyStateView
import com.applamdep.merchant.ui.component.ErrorStateView
import com.applamdep.merchant.ui.component.LoadingStateView
import com.applamdep.merchant.ui.component.MerchantTopBar
import com.applamdep.merchant.ui.component.StatusBadge
import com.applamdep.merchant.ui.theme.*

/**
 * THEO DÕI ĐẶT CỌC & BẢO LÃNH TÀI CHÍNH ESCROW (MERCHANT REQUIREMENT #9)
 * - Quản lý số dư tiền cọc đang được sàn giữ an toàn (HELD)
 * - Minh bạch tiền cọc đã giải ngân cho tiệm (RELEASED) và tiền cọc hoàn khách (REFUNDED)
 */
@Composable
fun MerchantFinanceScreen(
    onBack: () -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: MerchantFinanceViewModel = viewModel(factory = MerchantViewModelFactory.DefaultFactory),
    shopId: String = "shop_01"
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(shopId) {
        viewModel.loadFinance(shopId)
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MerchantBackground,
        topBar = {
            MerchantTopBar(
                title = stringResource(R.string.merchant_deposit_title),
                onBackClick = onBack
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (val state = uiState) {
                is UiState.Idle, is UiState.Loading -> {
                    LoadingStateView(message = "Đang tải dữ liệu tiền cọc...")
                }
                is UiState.Error -> {
                    ErrorStateView(
                        message = state.message,
                        onRetry = { viewModel.loadFinance(shopId) }
                    )
                }
                is UiState.Success -> {
                    val summary = state.data
                    val transactions = summary.transactions

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // 3 Thẻ thống kê số dư tiền cọc
                        item {
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                // 1. Tiền cọc đã giải ngân cho tiệm
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(containerColor = MerchantPrimary)
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Default.AccountBalanceWallet,
                                                contentDescription = null,
                                                tint = Color.White,
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = stringResource(R.string.merchant_deposit_released),
                                                fontSize = 13.sp,
                                                color = Color.White.copy(alpha = 0.9f)
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = Formatters.formatCurrency(summary.releasedTotalBalance),
                                            fontSize = 24.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "Tiền cọc từ các lịch hẹn đã hoàn thành thành công",
                                            fontSize = 11.sp,
                                            color = Color.White.copy(alpha = 0.8f)
                                        )
                                    }
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    // 2. Cọc sàn đang giữ (Escrow Held)
                                    Card(
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = CardDefaults.cardColors(containerColor = MerchantCard)
                                    ) {
                                        Column(modifier = Modifier.padding(14.dp)) {
                                            Text(
                                                text = stringResource(R.string.merchant_deposit_held),
                                                fontSize = 11.sp,
                                                color = MerchantTextSecondary
                                            )
                                            Spacer(modifier = Modifier.height(6.dp))
                                            Text(
                                                text = Formatters.formatCurrency(summary.heldEscrowBalance),
                                                fontSize = 16.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MerchantWarning
                                            )
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                text = "Chờ đến giờ hẹn",
                                                fontSize = 10.sp,
                                                color = MerchantTextHint
                                            )
                                        }
                                    }

                                    // 3. Cọc đã hoàn khách (Refunded)
                                    Card(
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = CardDefaults.cardColors(containerColor = MerchantCard)
                                    ) {
                                        Column(modifier = Modifier.padding(14.dp)) {
                                            Text(
                                                text = stringResource(R.string.merchant_deposit_refunded),
                                                fontSize = 11.sp,
                                                color = MerchantTextSecondary
                                            )
                                            Spacer(modifier = Modifier.height(6.dp))
                                            Text(
                                                text = Formatters.formatCurrency(summary.refundedTotalBalance),
                                                fontSize = 16.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MerchantTextSecondary
                                            )
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                text = "Hủy đúng quy định",
                                                fontSize = 10.sp,
                                                color = MerchantTextHint
                                            )
                                        }
                                    }
                                }

                                // Banner lưu ý cơ chế Escrow
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(MerchantPrimary.copy(alpha = 0.08f))
                                        .padding(12.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Security,
                                            contentDescription = null,
                                            tint = MerchantPrimary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = stringResource(R.string.merchant_deposit_note),
                                            fontSize = 11.sp,
                                            color = MerchantPrimaryDark,
                                            lineHeight = 16.sp
                                        )
                                    }
                                }
                            }
                        }

                        // Danh sách giao dịch ký quỹ
                        item {
                            Text(
                                text = "Lịch sử giao dịch cọc",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = MerchantTextPrimary,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }

                        if (transactions.isEmpty()) {
                            item {
                                EmptyStateView(
                                    title = "Chưa có giao dịch tiền cọc nào",
                                    subtitle = "Khi có khách đặt lịch và đặt cọc, các giao dịch sẽ hiển thị tại đây."
                                )
                            }
                        } else {
                            items(transactions, key = { it.id }) { tx ->
                                TransactionItemCard(tx = tx)
                            }
                        }
                    }
                }
                else -> Unit
            }
        }
    }
}

@Composable
private fun TransactionItemCard(tx: Transaction) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = MerchantCard),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            when (tx.status) {
                                MerchantConstants.TX_STATUS_RELEASED_TO_SHOP -> MerchantSuccess.copy(alpha = 0.15f)
                                MerchantConstants.TX_STATUS_HELD -> MerchantWarning.copy(alpha = 0.15f)
                                else -> Color(0xFFF0F0F0)
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = when (tx.status) {
                            MerchantConstants.TX_STATUS_RELEASED_TO_SHOP -> Icons.Default.Check
                            MerchantConstants.TX_STATUS_HELD -> Icons.Default.Lock
                            else -> Icons.Default.Undo
                        },
                        contentDescription = null,
                        tint = when (tx.status) {
                            MerchantConstants.TX_STATUS_RELEASED_TO_SHOP -> MerchantSuccess
                            MerchantConstants.TX_STATUS_HELD -> MerchantWarning
                            else -> MerchantTextSecondary
                        },
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = if (tx.bookingId.isNotBlank()) "Mã đơn: #${tx.bookingId}" else "Mã GD: #${tx.id}",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = MerchantTextPrimary
                    )
                    Text(
                        text = "Tiền cọc dịch vụ",
                        fontSize = 11.sp,
                        color = MerchantTextSecondary
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "+${Formatters.formatCurrency(tx.amount)}",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (tx.status == MerchantConstants.TX_STATUS_RELEASED_TO_SHOP) MerchantPrimary else MerchantTextPrimary
                )
                Spacer(modifier = Modifier.height(2.dp))
                StatusBadge(status = tx.status)
            }
        }
    }
}
