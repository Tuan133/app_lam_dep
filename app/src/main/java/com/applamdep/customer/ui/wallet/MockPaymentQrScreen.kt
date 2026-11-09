package com.applamdep.customer.ui.wallet

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.applamdep.customer.core.util.CurrencyFormatter
import com.applamdep.customer.domain.model.TopUpTransaction
import com.applamdep.customer.ui.components.GrabCard
import com.applamdep.customer.ui.components.GrabPrimaryButton
import com.applamdep.customer.ui.theme.*
import java.net.URLEncoder

/**
 * MÀN HÌNH QUÉT MÃ QR THANH TOÁN GIẢ LẬP (NHÓM 4 - P0)
 * - Hiển thị mã QR chứa liên kết tới trang web xác nhận riêng
 * - Cho phép mở trực tiếp trang xác nhận trên thiết bị để demo
 * - Có nút "Mô phỏng bấm xác nhận ngay"
 * - Lắng nghe realtime: Khi giao dịch SUCCESS thì tự động chuyển màn hình
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MockPaymentQrScreen(
    transaction: TopUpTransaction,
    onPaymentSuccess: () -> Unit,
    onBack: () -> Unit,
    viewModel: WalletViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val webUrl = transaction.qrUrl ?: "https://applamdep-customer.web.app/mock-payment.html?txId=${transaction.id}&amount=${transaction.amount}"
    val qrApiUrl = "https://api.qrserver.com/v1/create-qr-code/?size=300x300&data=" + URLEncoder.encode(webUrl, "UTF-8")

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = GrabBackground,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Thanh toán giả lập",
                        fontSize = 17.sp,
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
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            GrabCard(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Quét mã QR để thanh toán",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = GrabTextPrimary
                    )
                    Text(
                        text = "Sử dụng camera hoặc mở trang web để xác nhận",
                        fontSize = 12.sp,
                        color = GrabTextSecondary
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Ảnh mã QR sinh theo link trang web xác nhận
                    Box(
                        modifier = Modifier
                            .size(200.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .border(1.dp, GrabBorder, RoundedCornerShape(12.dp))
                            .background(Color.White)
                            .padding(10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        AsyncImage(
                            model = qrApiUrl,
                            contentDescription = "QR Code",
                            contentScale = ContentScale.Fit,
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Số tiền & Mã giao dịch
                    Text(
                        text = "Số tiền nạp:",
                        fontSize = 12.sp,
                        color = GrabTextSecondary
                    )
                    Text(
                        text = CurrencyFormatter.formatVnd(transaction.amount),
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = GrabGreenDark
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Mã giao dịch: ${transaction.id}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = GrabTextHint
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Nút 1: Mở trang web xác nhận trên trình duyệt
            Button(
                onClick = {
                    try {
                        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(webUrl))
                        context.startActivity(browserIntent)
                    } catch (e: Exception) {
                        Toast.makeText(context, "Không thể mở trình duyệt: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = GrabGreenLight),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.OpenInBrowser,
                    contentDescription = null,
                    tint = GrabGreenDark,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Mở trang web xác nhận thanh toán",
                    color = GrabGreenDark,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Nút 2: Mô phỏng xác nhận thành công ngay (Demo 1 thiết bị)
            GrabPrimaryButton(
                text = "Mô phỏng bấm 'Xác nhận' ngay (Demo)",
                onClick = {
                    viewModel.confirmPaymentDemo(
                        txId = transaction.id,
                        onSuccess = {
                            Toast.makeText(context, "Nạp tiền thành công vào Ví GLOW Pay!", Toast.LENGTH_LONG).show()
                            onPaymentSuccess()
                        }
                    )
                }
            )

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "Hệ thống đang lắng nghe thời gian thực (snapshot listener). Khi trang web bấm xác nhận, app sẽ tự động nhận tiền và quay lại ví.",
                fontSize = 11.sp,
                color = GrabTextHint,
                textAlign = TextAlign.Center,
                lineHeight = 15.sp
            )
        }
    }
}
