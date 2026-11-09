package com.applamdep.customer.ui.review

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.RateReview
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.applamdep.customer.core.base.UiState
import com.applamdep.customer.core.di.ViewModelFactory
import com.applamdep.customer.domain.model.Review
import com.applamdep.customer.ui.components.GrabCard
import com.applamdep.customer.ui.components.GrabEmptyState
import com.applamdep.customer.ui.components.GrabLoadingState
import com.applamdep.customer.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * MÀN HÌNH XEM CÁC ĐÁNH GIÁ ĐÃ ĐĂNG CỦA KHÁCH HÀNG (NHÓM CHỨC NĂNG 5)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerReviewsScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: CustomerReviewsViewModel = viewModel(factory = ViewModelFactory.Factory)
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.loadReviews()
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = GrabBackground,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Đánh giá của tôi",
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
        when (val state = uiState) {
            is UiState.Idle, is UiState.Loading -> {
                GrabLoadingState(modifier = Modifier.padding(innerPadding))
            }
            is UiState.Error -> {
                GrabEmptyState(
                    title = "Có lỗi xảy ra",
                    subtitle = state.message,
                    modifier = Modifier.padding(innerPadding)
                )
            }
            is UiState.Success -> {
                val reviews = state.data
                if (reviews.isEmpty()) {
                    GrabEmptyState(
                        icon = Icons.Default.RateReview,
                        title = "Chưa có đánh giá nào",
                        subtitle = "Sau khi hoàn thành dịch vụ làm đẹp, hãy gửi đánh giá để nhận xu tích lũy nhé!",
                        modifier = Modifier.padding(innerPadding)
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        items(reviews) { review ->
                            ReviewItemCard(review = review)
                        }
                    }
                }
            }
            else -> Unit
        }
    }
}

@Composable
private fun ReviewItemCard(review: Review) {
    val dateStr = remember(review.createdAt) {
        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        sdf.format(Date(review.createdAt))
    }

    GrabCard {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Store,
                        contentDescription = null,
                        tint = GrabGreenDark,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Đánh giá dịch vụ",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = GrabTextPrimary
                    )
                }

                Text(
                    text = dateStr,
                    fontSize = 11.sp,
                    color = GrabTextHint
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Hiển thị sao
            Row(verticalAlignment = Alignment.CenterVertically) {
                for (i in 1..5) {
                    Icon(
                        imageVector = Icons.Filled.Star,
                        contentDescription = null,
                        tint = if (i <= review.rating) Color(0xFFFFB800) else GrabBorder,
                        modifier = Modifier.size(16.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "${review.rating}/5 sao",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFFFB800)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Nội dung nhận xét
            Text(
                text = review.comment,
                fontSize = 13.sp,
                color = GrabTextPrimary,
                lineHeight = 18.sp
            )

            // Tags
            if (review.tags.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                @OptIn(ExperimentalLayoutApi::class)
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    review.tags.forEach { tag ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(GrabGreenLight)
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = tag,
                                fontSize = 11.sp,
                                color = GrabGreenDark,
                                fontWeight = FontWeight.Medium,
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    }
                }
            }

            // Ảnh đính kèm
            if (review.images.isNotEmpty()) {
                Spacer(modifier = Modifier.height(10.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(review.images) { imgUrl ->
                        AsyncImage(
                            model = imgUrl,
                            contentDescription = "Review attachment",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(70.dp)
                                .clip(RoundedCornerShape(8.dp))
                        )
                    }
                }
            }

            // Phản hồi chính thức từ Salon nếu có
            if (!review.replyComment.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(12.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFF7F8F9))
                        .padding(12.dp)
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Store,
                                contentDescription = null,
                                tint = GrabGreenDark,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Phản hồi từ chủ Salon:",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = GrabGreenDark
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = review.replyComment,
                            fontSize = 12.sp,
                            color = GrabTextSecondary,
                            lineHeight = 17.sp
                        )
                    }
                }
            }
        }
    }
}
