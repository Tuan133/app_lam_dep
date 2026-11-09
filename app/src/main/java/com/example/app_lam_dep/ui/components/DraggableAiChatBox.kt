package com.example.app_lam_dep.ui.components

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.applamdep.customer.core.ai.AiRagRetriever
import com.applamdep.customer.core.ai.GeminiAiService
import com.applamdep.customer.core.config.AiConfig
import com.example.app_lam_dep.model.AiShopRecommendation
import com.example.app_lam_dep.ui.icons.GlowIcon
import com.example.app_lam_dep.ui.icons.GlowIcons
import com.example.app_lam_dep.ui.theme.ChampagneGold
import com.example.app_lam_dep.ui.theme.DustyMerlot
import com.example.app_lam_dep.ui.theme.GoldSoft
import com.example.app_lam_dep.ui.theme.PinkPanther
import com.example.app_lam_dep.ui.theme.SuccessGreen
import com.example.app_lam_dep.ui.theme.TextMuted
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

data class ChatMessage(
    val isAi: Boolean,
    val text: String,
    val recommendations: List<AiShopRecommendation> = emptyList(),
    val isGeminiLive: Boolean = false
)

@Composable
fun DraggableFloatingAiButton(
    onNavigateToSalon: (String) -> Unit,
    onSelectServiceForBooking: (String, String) -> Unit,
    modifier: Modifier = Modifier
) {
    var showAiSheet by remember { mutableStateOf(false) }

    val configuration = LocalConfiguration.current
    val density = LocalDensity.current
    val screenWidthPx = with(density) { configuration.screenWidthDp.dp.toPx() }
    val screenHeightPx = with(density) { configuration.screenHeightDp.dp.toPx() }
    val buttonSizePx = with(density) { 60.dp.toPx() }

    // Start near right edge, vertically center-bottom
    var offsetX by remember { mutableStateOf(screenWidthPx - buttonSizePx - 20f) }
    var offsetY by remember { mutableStateOf(screenHeightPx * 0.65f) }

    Box(
        modifier = modifier.fillMaxSize()
    ) {
        // Floating Draggable Circular Button
        Box(
            modifier = Modifier
                .offset { IntOffset(offsetX.roundToInt(), offsetY.roundToInt()) }
                .size(60.dp)
                .shadow(12.dp, CircleShape, spotColor = DustyMerlot)
                .clip(CircleShape)
                .background(
                    Brush.linearGradient(
                        listOf(Color(0xFF2E0913), DustyMerlot, Color(0xFF882C42), ChampagneGold)
                    )
                )
                .border(2.dp, Brush.sweepGradient(listOf(ChampagneGold, GoldSoft, PinkPanther, ChampagneGold)), CircleShape)
                .pointerInput(Unit) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        offsetX = (offsetX + dragAmount.x).coerceIn(16f, screenWidthPx - buttonSizePx - 16f)
                        offsetY = (offsetY + dragAmount.y).coerceIn(100f, screenHeightPx - buttonSizePx - 100f)
                    }
                }
                .clickable { showAiSheet = true },
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy((-2).dp)
            ) {
                GlowIcon(
                    icon = GlowIcons.Sparkle,
                    tint = GoldSoft,
                    size = 24.dp
                )
                Text(
                    text = "AI GLOW",
                    fontSize = 8.5.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    letterSpacing = 0.6.sp
                )
            }
        }
    }

    if (showAiSheet) {
        AiShopAdvisorSheet(
            onDismiss = { showAiSheet = false },
            onNavigateToSalon = { sName ->
                showAiSheet = false
                onNavigateToSalon(sName)
            },
            onSelectServiceForBooking = { sName, sShop ->
                showAiSheet = false
                onSelectServiceForBooking(sName, sShop)
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiShopAdvisorSheet(
    onDismiss: () -> Unit,
    onNavigateToSalon: (String) -> Unit,
    onSelectServiceForBooking: (String, String) -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val aiService = remember { GeminiAiService(context) }

    var queryText by remember { mutableStateOf("") }
    var isThinking by remember { mutableStateOf(false) }
    var showApiKeyDialog by remember { mutableStateOf(false) }
    var hasGeminiKey by remember { mutableStateOf(AiConfig.hasApiKey(context)) }

    // Initial RAG Top Salons
    val initialRag = remember { AiRagRetriever.retrieve("salon nổi bật Đà Nẵng") }

    var messages by remember {
        mutableStateOf(
            listOf(
                ChatMessage(
                    isAi = true,
                    text = "Dạ em chào quý khách! Em là Trợ lý AI GLOW ✨. Em có thể giúp bạn tìm salon uy tín, chọn dịch vụ phù hợp hoặc lọc tiệm giữ chỗ 0đ không cần cọc.",
                    recommendations = initialRag.recommendations
                )
            )
        )
    }

    // Modal cấu hình Gemini API Key
    if (showApiKeyDialog) {
        var inputKey by remember { mutableStateOf(AiConfig.getUserApiKey(context)) }

        AlertDialog(
            onDismissRequest = { showApiKeyDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(imageVector = Icons.Default.Key, contentDescription = null, tint = DustyMerlot)
                    Text("Cấu hình Google Gemini API", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = DustyMerlot)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Thêm API Key để kích hoạt trí tuệ nhân tạo Gemini 1.5 Flash trả lời theo thời gian thực kết hợp RAG từ dữ liệu Excel của bạn.",
                        fontSize = 12.5.sp,
                        color = Color(0xFF4B5563)
                    )

                    OutlinedTextField(
                        value = inputKey,
                        onValueChange = { inputKey = it },
                        label = { Text("Gemini API Key (AIzaSy...)") },
                        placeholder = { Text("Dán API Key vào đây...") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFF3F4F6))
                            .clickable {
                                try {
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://aistudio.google.com/app/apikey"))
                                    context.startActivity(intent)
                                } catch (_: Exception) {}
                            }
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Lấy Key miễn phí tại Google AI Studio",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Medium,
                            color = DustyMerlot
                        )
                        Icon(imageVector = Icons.Default.OpenInNew, contentDescription = null, tint = DustyMerlot, modifier = Modifier.size(16.dp))
                    }

                    Text(
                        text = "💡 Ngoài ra, bạn cũng có thể mở file AiConfig.kt trong source code để gán trực tiếp vào biến GEMINI_API_KEY.",
                        fontSize = 11.sp,
                        color = TextMuted
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        AiConfig.saveUserApiKey(context, inputKey)
                        hasGeminiKey = AiConfig.hasApiKey(context)
                        showApiKeyDialog = false
                        Toast.makeText(context, if (hasGeminiKey) "Đã lưu Gemini API Key thành công! ✨" else "Đã xóa API Key, chuyển sang RAG Cục bộ.", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = DustyMerlot)
                ) {
                    Text("Lưu Key", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showApiKeyDialog = false }) {
                    Text("Đóng")
                }
            }
        )
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFFFBF7E8)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp)
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(DustyMerlot)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(Brush.linearGradient(listOf(ChampagneGold, GoldSoft))),
                    contentAlignment = Alignment.Center
                ) {
                    GlowIcon(icon = GlowIcons.Sparkle, tint = DustyMerlot, size = 20.dp)
                }

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Trợ Lý AI GLOW",
                            fontSize = 15.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (hasGeminiKey) Color(0x334ADE80) else Color(0x33FFD700))
                                .padding(horizontal = 7.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = if (hasGeminiKey) "● Trực tuyến" else "● AI Sẵn sàng",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (hasGeminiKey) Color(0xFF86EFAC) else ChampagneGold
                            )
                        }
                    }
                    Text(
                        text = if (hasGeminiKey) "Tư vấn làm đẹp & Đặt lịch thông minh" else "Tư vấn làm đẹp • Sẵn sàng hỗ trợ",
                        fontSize = 11.sp,
                        color = Color(0xFFF3D5DC)
                    )
                }

                // Nút cài đặt API Key
                IconButton(
                    onClick = { showApiKeyDialog = true },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Cấu hình API Key",
                        tint = ChampagneGold,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Quick Prompt Chips theo tập dữ liệu 11 Salon
            val promptChips = listOf(
                "💅 Tiệm Nail tại Hải Châu",
                "🌿 Gội đầu dưỡng sinh thư giãn",
                "🎉 Tiệm giữ chỗ 0đ không cần cọc",
                "💆‍♀️ Trị mụn & Phục hồi da",
                "✂️ Uốn tóc sóng lơi Hàn Quốc",
                "🌙 Tiệm mở cửa muộn sau 20h"
            )

            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(promptChips) { chip ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(18.dp))
                            .background(Color.White)
                            .border(1.dp, Color(0xFFEADBCE), RoundedCornerShape(18.dp))
                            .clickable {
                                if (!isThinking) {
                                    val userText = chip
                                    val newMsgList = messages.toMutableList()
                                    newMsgList.add(ChatMessage(isAi = false, text = userText))
                                    messages = newMsgList
                                    isThinking = true

                                    coroutineScope.launch {
                                        val aiResponse = aiService.askAiGlow(userText)
                                        val updatedList = messages.toMutableList()
                                        updatedList.add(
                                            ChatMessage(
                                                isAi = true,
                                                text = aiResponse.replyText,
                                                recommendations = aiResponse.recommendations,
                                                isGeminiLive = aiResponse.isGeminiLive
                                            )
                                        )
                                        messages = updatedList
                                        isThinking = false
                                    }
                                }
                            }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(text = chip, fontSize = 11.5.sp, fontWeight = FontWeight.Medium, color = DustyMerlot)
                    }
                }
            }

            // Message Stream
            LazyColumn(
                modifier = Modifier
                    .weight(1f, fill = false)
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(messages) { msg ->
                    if (msg.isAi) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .clip(CircleShape)
                                        .background(DustyMerlot),
                                    contentAlignment = Alignment.Center
                                ) {
                                    GlowIcon(icon = GlowIcons.Sparkle, tint = ChampagneGold, size = 15.dp)
                                }
                                Card(
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color.White),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Text(
                                            text = msg.text,
                                            fontSize = 12.5.sp,
                                            color = Color(0xFF24090F),
                                            lineHeight = 18.sp
                                        )
                                    }
                                }
                            }

                            // If message has recommendations, render cards
                            msg.recommendations.forEach { rec ->
                                Card(
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color.White),
                                    elevation = CardDefaults.cardElevation(1.5.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(
                                        modifier = Modifier.padding(12.dp),
                                        verticalArrangement = Arrangement.spacedBy(7.dp)
                                    ) {
                                        // Row 1: Tên salon + Đánh giá sao
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = rec.salonName,
                                                fontSize = 13.5.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF24090F),
                                                modifier = Modifier.weight(1f),
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Row(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(6.dp))
                                                    .background(Color(0xFFFFF9E6))
                                                    .padding(horizontal = 6.dp, vertical = 2.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(3.dp)
                                            ) {
                                                GlowIcon(icon = GlowIcons.StarFilled, tint = Color(0xFFD4A017), size = 11.dp)
                                                Text(
                                                    text = "${rec.rating}",
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color(0xFF7A5800)
                                                )
                                            }
                                        }

                                        // Row 2: Dịch vụ gợi ý nổi bật
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(Color(0xFFFAF2EE))
                                                .padding(horizontal = 8.dp, vertical = 5.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                                        ) {
                                            GlowIcon(icon = GlowIcons.Sparkle, tint = Color(0xFF882C42), size = 12.dp)
                                            Text(
                                                text = rec.serviceSuggestion,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = Color(0xFF5A1525),
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }

                                        // Row 3: Khoảng cách & Chính sách cọc
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(3.dp)
                                            ) {
                                                Text(text = "📍", fontSize = 10.sp)
                                                Text(text = rec.distance, fontSize = 11.sp, color = Color(0xFF6B585D))
                                            }

                                            Text(text = "•", fontSize = 10.sp, color = Color(0xFFD5C4C8))

                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(6.dp))
                                                    .background(if (rec.requiresDeposit) Color(0xFFFCEEEC) else Color(0xFFE8F5E9))
                                                    .padding(horizontal = 7.dp, vertical = 2.dp)
                                            ) {
                                                Text(
                                                    text = if (rec.requiresDeposit) rec.depositInfo else "Miễn cọc 0đ • Giữ chỗ",
                                                    fontSize = 10.5.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (rec.requiresDeposit) Color(0xFF8B263E) else Color(0xFF2E7D32)
                                                )
                                            }
                                        }

                                        // Row 4: Mô tả lý do gợi ý (tinh gọn)
                                        Text(
                                            text = rec.matchReason,
                                            fontSize = 11.sp,
                                            color = Color(0xFF5A4D51),
                                            lineHeight = 15.sp,
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis
                                        )

                                        // Row 5: Nút hành động
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            OutlinedButton(
                                                onClick = { onNavigateToSalon(rec.salonName) },
                                                shape = RoundedCornerShape(8.dp),
                                                modifier = Modifier.weight(1f).height(32.dp),
                                                contentPadding = PaddingValues(0.dp),
                                                border = BorderStroke(1.dp, DustyMerlot)
                                            ) {
                                                Text("Xem Shop", fontSize = 11.sp, color = DustyMerlot, fontWeight = FontWeight.Bold)
                                            }
                                            Button(
                                                onClick = { onSelectServiceForBooking(rec.serviceSuggestion, rec.salonName) },
                                                colors = ButtonDefaults.buttonColors(containerColor = DustyMerlot),
                                                shape = RoundedCornerShape(8.dp),
                                                modifier = Modifier.weight(1f).height(32.dp),
                                                contentPadding = PaddingValues(0.dp)
                                            ) {
                                                Text("Đặt lịch ngay", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        // User message
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(DustyMerlot)
                                    .padding(horizontal = 14.dp, vertical = 8.dp)
                            ) {
                                Text(text = msg.text, color = Color.White, fontSize = 12.5.sp, fontWeight = FontWeight.Medium)
                            }
                        }
                    }
                }

                // Trạng thái AI đang suy nghĩ
                if (isThinking) {
                    item {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(vertical = 4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(DustyMerlot),
                                contentAlignment = Alignment.Center
                            ) {
                                GlowIcon(icon = GlowIcons.Sparkle, tint = ChampagneGold, size = 12.dp)
                            }
                            Card(
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White)
                            ) {
                                Text(
                                    text = "AI GLOW đang tra cứu dữ liệu 11 salon & suy nghĩ... ✨",
                                    fontSize = 11.5.sp,
                                    color = DustyMerlot,
                                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Chat Input Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = queryText,
                    onValueChange = { queryText = it },
                    placeholder = { Text("Hỏi AI (tiệm nail, spa gội đầu, miễn cọc...)", fontSize = 11.5.sp, color = TextMuted) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(20.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedBorderColor = DustyMerlot,
                        unfocusedBorderColor = Color(0xFFEBDCD3)
                    ),
                    singleLine = true
                )

                Button(
                    onClick = {
                        if (queryText.isNotBlank() && !isThinking) {
                            val userText = queryText
                            queryText = ""
                            val newMsgList = messages.toMutableList()
                            newMsgList.add(ChatMessage(isAi = false, text = userText))
                            messages = newMsgList
                            isThinking = true

                            coroutineScope.launch {
                                val aiResponse = aiService.askAiGlow(userText)
                                val updatedList = messages.toMutableList()
                                updatedList.add(
                                    ChatMessage(
                                        isAi = true,
                                        text = aiResponse.replyText,
                                        recommendations = aiResponse.recommendations,
                                        isGeminiLive = aiResponse.isGeminiLive
                                    )
                                )
                                messages = updatedList
                                isThinking = false
                            }
                        }
                    },
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(containerColor = DustyMerlot),
                    contentPadding = PaddingValues(0.dp),
                    modifier = Modifier.size(46.dp)
                ) {
                    GlowIcon(icon = GlowIcons.Sparkle, tint = ChampagneGold, size = 20.dp)
                }
            }
        }
    }
}
