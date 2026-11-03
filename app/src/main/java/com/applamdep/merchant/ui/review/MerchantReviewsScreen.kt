package com.applamdep.merchant.ui.review

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.app_lam_dep.R
import com.applamdep.merchant.core.base.UiState
import com.applamdep.merchant.core.di.MerchantViewModelFactory
import com.applamdep.merchant.domain.model.Review
import com.applamdep.merchant.ui.component.EmptyStateView
import com.applamdep.merchant.ui.component.ErrorStateView
import com.applamdep.merchant.ui.component.LoadingStateView
import com.applamdep.merchant.ui.component.MerchantTopBar
import com.applamdep.merchant.ui.theme.*

/**
 * QUẢN LÝ ĐÁNH GIÁ, PHẢN HỒI VÀ TỐ CÁO (MERCHANT REQUIREMENT #8)
 * - Hiển thị phản hồi từ khách hàng
 * - Phản hồi chính thức từ cửa hàng
 * - Báo cáo / Khiếu nại đánh giá vi phạm gửi tới Admin
 */
@Composable
fun MerchantReviewsScreen(
    onBack: () -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: MerchantReviewViewModel = viewModel(factory = MerchantViewModelFactory.DefaultFactory),
    shopId: String = "shop_01"
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()

    var replyingReview by remember { mutableStateOf<Review?>(null) }
    var replyInput by remember { mutableStateOf("") }
    var reportingReview by remember { mutableStateOf<Review?>(null) }
    var reportReasonInput by remember { mutableStateOf("") }
    var selectedStarFilter by remember { mutableStateOf(0) } // 0: Tất cả

    LaunchedEffect(shopId) {
        viewModel.loadReviews(shopId)
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MerchantBackground,
        topBar = {
            MerchantTopBar(
                title = stringResource(R.string.merchant_review_title),
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
                    LoadingStateView(message = "Đang tải đánh giá khách hàng...")
                }
                is UiState.Error -> {
                    ErrorStateView(
                        message = state.message,
                        onRetry = { viewModel.loadReviews(shopId) }
                    )
                }
                is UiState.Success -> {
                    val reviews = state.data
                    val filteredReviews = if (selectedStarFilter == 0) {
                        reviews
                    } else {
                        reviews.filter { it.rating == selectedStarFilter }
                    }

                    val avgRating = if (reviews.isNotEmpty()) reviews.map { it.rating }.average() else 5.0

                    // Header thống kê sao đánh giá
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MerchantCard)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = String.format("%.1f", avgRating),
                                    fontSize = 32.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MerchantVipGold
                                )
                                Row {
                                    repeat(5) {
                                        Icon(
                                            imageVector = Icons.Default.Star,
                                            contentDescription = null,
                                            tint = MerchantVipGold,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "${reviews.size} lượt đánh giá",
                                    fontSize = 11.sp,
                                    color = MerchantTextSecondary
                                )
                            }

                            Spacer(modifier = Modifier.width(20.dp))

                            // Filter Chips theo sao
                            Row(
                                modifier = Modifier
                                    .weight(1f)
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                listOf(0 to "Tất cả", 5 to "5 ★", 4 to "4 ★", 3 to "3 ★").forEach { (stars, label) ->
                                    val isSelected = selectedStarFilter == stars
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = { selectedStarFilter = stars },
                                        label = { Text(label, fontSize = 12.sp) }
                                    )
                                }
                            }
                        }
                    }

                    if (filteredReviews.isEmpty()) {
                        EmptyStateView(
                            title = "Không có đánh giá nào",
                            subtitle = "Salon chưa nhận được đánh giá nào trong mục này."
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(filteredReviews, key = { it.id }) { review ->
                                ReviewItemCard(
                                    review = review,
                                    onReplyClick = {
                                        replyingReview = review
                                        replyInput = review.shopReply ?: ""
                                    },
                                    onReportClick = {
                                        reportingReview = review
                                        reportReasonInput = ""
                                    }
                                )
                            }
                            item {
                                Spacer(modifier = Modifier.height(16.dp))
                            }
                        }
                    }
                }
                else -> Unit
            }
        }

        // Dialog Phản hồi khách hàng
        replyingReview?.let { rev ->
            AlertDialog(
                onDismissRequest = { replyingReview = null },
                title = {
                    Text(
                        text = "Phản hồi đánh giá của ${rev.customerName}",
                        fontWeight = FontWeight.Bold,
                        color = MerchantTextPrimary,
                        fontSize = 16.sp
                    )
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "Khách nhận xét: \"${rev.content}\"",
                            fontSize = 13.sp,
                            color = MerchantTextSecondary
                        )
                        OutlinedTextField(
                            value = replyInput,
                            onValueChange = { replyInput = it },
                            label = { Text("Lời phản hồi của salon") },
                            placeholder = { Text(stringResource(R.string.merchant_review_reply_hint)) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(100.dp)
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.replyReview(
                                reviewId = rev.id,
                                replyText = replyInput,
                                onSuccess = {
                                    Toast.makeText(context, "Đã gửi phản hồi thành công!", Toast.LENGTH_SHORT).show()
                                    replyingReview = null
                                },
                                onError = { Toast.makeText(context, it, Toast.LENGTH_SHORT).show() }
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MerchantPrimary)
                    ) {
                        Text(text = stringResource(R.string.merchant_review_reply_btn), fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { replyingReview = null }) {
                        Text(text = "Hủy", color = MerchantTextSecondary)
                    }
                }
            )
        }

        // Dialog Báo cáo vi phạm
        reportingReview?.let { rev ->
            AlertDialog(
                onDismissRequest = { reportingReview = null },
                title = {
                    Text(
                        text = "Báo cáo vi phạm đánh giá",
                        fontWeight = FontWeight.Bold,
                        color = MerchantTextPrimary,
                        fontSize = 16.sp
                    )
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "Yêu cầu khiếu nại đánh giá của khách hàng '${rev.customerName}' sẽ được chuyển tới Ban Quản Trị xem xét.",
                            fontSize = 13.sp,
                            color = MerchantTextSecondary
                        )
                        OutlinedTextField(
                            value = reportReasonInput,
                            onValueChange = { reportReasonInput = it },
                            label = { Text(stringResource(R.string.merchant_review_report_reason_hint)) },
                            placeholder = { Text("Ví dụ: Đánh giá chứa ngôn từ thô tục, sai sự thật...") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(90.dp)
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.reportReview(
                                reviewId = rev.id,
                                reason = reportReasonInput,
                                onSuccess = {
                                    Toast.makeText(context, "Đã gửi báo cáo vi phạm tới Admin!", Toast.LENGTH_SHORT).show()
                                    reportingReview = null
                                },
                                onError = { Toast.makeText(context, it, Toast.LENGTH_SHORT).show() }
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MerchantError)
                    ) {
                        Text(text = stringResource(R.string.merchant_review_report_btn), fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { reportingReview = null }) {
                        Text(text = "Hủy", color = MerchantTextSecondary)
                    }
                }
            )
        }
    }
}

@Composable
private fun ReviewItemCard(
    review: Review,
    onReplyClick: () -> Unit,
    onReportClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MerchantCard),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header: Khách hàng + Số sao
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(MerchantPrimary.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = review.customerName.take(1).uppercase(),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = MerchantPrimary
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = review.customerName,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = MerchantTextPrimary
                        )
                        Row {
                            repeat(review.rating) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = null,
                                    tint = MerchantVipGold,
                                    modifier = Modifier.size(13.dp)
                                )
                            }
                        }
                    }
                }

                IconButton(onClick = onReportClick, modifier = Modifier.size(28.dp)) {
                    Icon(
                        imageVector = Icons.Default.Flag,
                        contentDescription = "Báo cáo",
                        tint = MerchantTextHint,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Nội dung review
            Text(
                text = review.content,
                fontSize = 13.sp,
                color = MerchantTextPrimary,
                lineHeight = 18.sp
            )

            // Phản hồi từ Salon (nếu có)
            if (!review.shopReply.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(10.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(MerchantBackground)
                        .padding(12.dp)
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Storefront,
                                contentDescription = null,
                                tint = MerchantPrimary,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Phản hồi từ cửa hàng",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MerchantPrimary
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = review.shopReply,
                            fontSize = 12.sp,
                            color = MerchantTextSecondary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Nút phản hồi
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(
                    onClick = onReplyClick,
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Reply,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = MerchantPrimary
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (review.shopReply.isNullOrBlank()) "Gửi phản hồi" else "Chỉnh sửa phản hồi",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MerchantPrimary
                    )
                }
            }
        }
    }
}
