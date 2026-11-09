package com.applamdep.customer.ui.review

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.applamdep.customer.core.base.UiState
import com.applamdep.customer.core.di.ViewModelFactory
import com.applamdep.customer.core.util.CurrencyFormatter
import com.applamdep.customer.ui.components.GrabButton
import com.applamdep.customer.ui.components.GrabCard
import com.applamdep.customer.ui.theme.*

/**
 * MÀN HÌNH VIẾT ĐÁNH GIÁ DỊCH VỤ (CUSTOMER APP - NHÓM CHỨC NĂNG 5)
 * - Đánh giá cửa hàng sau khi hoàn thành dịch vụ
 * - Chấm điểm 1-5 sao, chọn tag trải nghiệm nhanh
 * - Viết nhận xét chi tiết và đính kèm hình ảnh thực tế
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WriteReviewScreen(
    bookingId: String,
    onBack: () -> Unit,
    onReviewSubmitted: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: WriteReviewViewModel = viewModel(factory = ViewModelFactory.Factory)
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val booking by viewModel.targetBooking.collectAsStateWithLifecycle()
    val rating by viewModel.rating.collectAsStateWithLifecycle()
    val comment by viewModel.comment.collectAsStateWithLifecycle()
    val selectedTags by viewModel.selectedTags.collectAsStateWithLifecycle()
    val selectedImages by viewModel.selectedImages.collectAsStateWithLifecycle()

    LaunchedEffect(bookingId) {
        viewModel.loadBooking(bookingId)
    }

    val sampleCriteriaTags = listOf(
        "Kỹ thuật chuyên nghiệp",
        "Không gian sang trọng",
        "Nhân viên chu đáo",
        "Đúng giờ hẹn",
        "Sản phẩm cao cấp",
        "Giá cả xứng đáng"
    )

    val demoReviewImages = listOf(
        "https://images.unsplash.com/photo-1604654894610-df63bc536371?w=500&auto=format&fit=crop",
        "https://images.unsplash.com/photo-1519014816548-bf5fe059798b?w=500&auto=format&fit=crop",
        "https://images.unsplash.com/photo-1560750588-73207b1ef5b8?w=500&auto=format&fit=crop"
    )

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = GrabBackground,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Đánh giá dịch vụ",
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
            // 1. Thẻ tóm tắt đơn dịch vụ đã hoàn thành
            booking?.let { b ->
                GrabCard {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(GrabGreenLight),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Spa,
                                contentDescription = null,
                                tint = GrabGreenDark
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = b.shopName,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = GrabTextPrimary
                            )
                            Text(
                                text = b.serviceName,
                                fontSize = 13.sp,
                                color = GrabTextSecondary
                            )
                            Text(
                                text = "${b.bookingTime} · ${b.bookingDate}",
                                fontSize = 12.sp,
                                color = GrabGreenDark,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }

            // 2. Khu vực Chấm sao (1 - 5 sao)
            GrabCard {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Trải nghiệm của bạn thế nào?",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = GrabTextPrimary
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // 5 Ngôi sao lớn
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        for (i in 1..5) {
                            val isSelected = i <= rating
                            IconButton(
                                onClick = { viewModel.setRating(i) },
                                modifier = Modifier.size(48.dp)
                            ) {
                                Icon(
                                    imageVector = if (isSelected) Icons.Filled.Star else Icons.Filled.StarBorder,
                                    contentDescription = "$i sao",
                                    tint = if (isSelected) Color(0xFFFFB800) else GrabBorder,
                                    modifier = Modifier.size(36.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    val ratingLabel = when (rating) {
                        5 -> "Cực kỳ hài lòng & Sẽ quay lại! (5/5)"
                        4 -> "Rất hài lòng về dịch vụ (4/5)"
                        3 -> "Dịch vụ ở mức ổn (3/5)"
                        2 -> "Chưa thực sự ưng ý (2/5)"
                        else -> "Rất thất vọng (1/5)"
                    }

                    Text(
                        text = ratingLabel,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (rating >= 4) GrabGreenDark else if (rating == 3) Color(0xFFB77900) else GrabStatusError
                    )
                }
            }

            // 3. Tiêu chí khen ngợi / góp ý nhanh (Tags)
            GrabCard {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Điểm bạn ấn tượng nhất:",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = GrabTextPrimary
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Hiển thị các chip tags tự động xuống dòng linh hoạt
                    @OptIn(ExperimentalLayoutApi::class)
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        sampleCriteriaTags.forEach { tag ->
                            val isChecked = selectedTags.contains(tag)
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isChecked) GrabGreenLight else Color(0xFFF9F9F9))
                                    .border(1.dp, if (isChecked) GrabGreenDark else Color(0xFFE5E7EB), RoundedCornerShape(8.dp))
                                    .clickable { viewModel.toggleTag(tag) }
                                    .padding(horizontal = 12.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    text = tag,
                                    fontSize = 12.sp,
                                    fontWeight = if (isChecked) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isChecked) GrabGreenDark else GrabTextPrimary,
                                    maxLines = 1,
                                    softWrap = false
                                )
                            }
                        }
                    }
                }
            }

            // 4. Ô nhập nhận xét chi tiết
            GrabCard {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Chia sẻ chi tiết trải nghiệm:",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = GrabTextPrimary
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = comment,
                        onValueChange = { viewModel.setComment(it) },
                        placeholder = {
                            Text(
                                text = "Hãy chia sẻ cảm nhận về tay nghề kỹ thuật viên, không gian tiệm, độ bền của móng/tóc...",
                                fontSize = 13.sp,
                                color = GrabTextHint
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GrabGreen,
                            unfocusedBorderColor = GrabBorder
                        )
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "${comment.length}/500 ký tự (Tối thiểu 5 ký tự)",
                        fontSize = 11.sp,
                        color = if (comment.length >= 5) GrabTextSecondary else GrabStatusError,
                        modifier = Modifier.align(Alignment.End)
                    )
                }
            }

            // 5. Đính kèm hình ảnh trải nghiệm làm đẹp
            GrabCard {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Ảnh thực tế kết quả làm đẹp:",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = GrabTextPrimary
                        )

                        TextButton(
                            onClick = {
                                val nextImg = demoReviewImages.getOrNull(selectedImages.size % demoReviewImages.size)
                                    ?: demoReviewImages.first()
                                viewModel.addMockImage(nextImg)
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.AddPhotoAlternate,
                                contentDescription = null,
                                tint = GrabGreen,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = "Thêm ảnh", color = GrabGreen, fontSize = 13.sp)
                        }
                    }

                    if (selectedImages.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(80.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .border(1.dp, GrabBorder, RoundedCornerShape(8.dp))
                                .clickable {
                                    viewModel.addMockImage(demoReviewImages.first())
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Default.CameraAlt,
                                    contentDescription = null,
                                    tint = GrabTextHint
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Bấm để đính kèm ảnh thành phẩm sau khi làm",
                                    fontSize = 12.sp,
                                    color = GrabTextHint
                                )
                            }
                        }
                    } else {
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.padding(top = 8.dp)
                        ) {
                            itemsIndexed(selectedImages) { index, imgUrl ->
                                Box(
                                    modifier = Modifier
                                        .size(90.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                ) {
                                    AsyncImage(
                                        model = imgUrl,
                                        contentDescription = "Review Image $index",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )

                                    // Nút xóa ảnh
                                    IconButton(
                                        onClick = { viewModel.removeImage(index) },
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                            .size(24.dp)
                                            .padding(2.dp)
                                            .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Remove",
                                            tint = Color.White,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 6. Nút Gửi Đánh Giá
            GrabButton(
                text = "Gửi đánh giá dịch vụ",
                onClick = {
                    viewModel.submitReview(
                        onSuccess = {
                            Toast.makeText(context, "Cảm ơn bạn! Đánh giá đã được gửi thành công đến salon.", Toast.LENGTH_LONG).show()
                            onReviewSubmitted()
                        },
                        onError = { err ->
                            Toast.makeText(context, err, Toast.LENGTH_SHORT).show()
                        }
                    )
                },
                enabled = uiState !is UiState.Loading && comment.trim().length >= 5,
                isLoading = uiState is UiState.Loading,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
