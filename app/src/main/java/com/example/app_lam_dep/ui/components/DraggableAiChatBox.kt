package com.example.app_lam_dep.ui.components

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.*
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
                verticalArrangement = Arrangement.Center
            ) {
                GlowIcon(icon = GlowIcons.Sparkle, tint = GoldSoft, size = 22.dp)
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "AI GLOW",
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    letterSpacing = 0.5.sp
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
                    text = "Xin chào quý khách! Em là Trợ lý AI GLOW ✨. Em được nạp toàn bộ dữ liệu 11 salon làm đẹp thực tế tại Đà Nẵng (tri thức từ file Excel). Em có thể giúp bạn tìm tiệm nail, uốn tóc, spa gội đầu, trị mụn hoặc lọc tiệm MIỄN CỌC 0Đ theo yêu cầu!",
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
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Brush.linearGradient(listOf(ChampagneGold, GoldSoft))),
                    contentAlignment = Alignment.Center
                ) {
                    GlowIcon(icon = GlowIcons.Sparkle, tint = DustyMerlot, size = 18.dp)
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Trợ Lý AI GLOW • RAG Thông Minh",
                        fontSize = 14.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = if (hasGeminiKey) "Google Gemini Live • Dữ liệu 11 Salon Excel" else "RAG Cục Bộ • Bấm bánh răng ⚙️ để thêm Gemini Key",
                        fontSize = 10.5.sp,
                        color = PinkPanther
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

                // Trạng thái AI
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (hasGeminiKey) SuccessGreen.copy(alpha = 0.25f) else ChampagneGold.copy(alpha = 0.25f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = if (hasGeminiKey) "Gemini Live" else "RAG Ready",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (hasGeminiKey) SuccessGreen else ChampagneGold
                    )
                }
            }

            // Quick Prompt Chips theo tập dữ liệu 11 Salon
            val promptChips = listOf(
                "💅 Tiệm Nail ở Hải Châu",
                "🌿 Gội đầu dưỡng sinh thư giãn",
                "💆‍♀️ Trị mụn phục hồi da uy tín",
                "✂️ Uốn tóc sóng lơi Hàn Quốc",
                "🎉 Tiệm giữ chỗ 0đ không cần cọc",
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
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color.White)
                            .border(1.dp, ChampagneGold.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
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
                                    elevation = CardDefaults.cardElevation(2.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(start = 36.dp)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(12.dp),
                                        verticalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = rec.salonName,
                                                fontSize = 13.5.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = DustyMerlot,
                                                modifier = Modifier.weight(1f)
                                            )
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(2.dp)
                                            ) {
                                                GlowIcon(icon = GlowIcons.StarFilled, tint = ChampagneGold, size = 12.dp)
                                                Text(text = "${rec.rating}", fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = Color(0xFF24090F))
                                            }
                                        }

                                        Text(text = "✨ Dịch vụ gợi ý: ${rec.serviceSuggestion}", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF3A101A))
                                        Text(text = "📍 ${rec.distance}", fontSize = 11.sp, color = TextMuted)
                                        Text(text = "💡 ${rec.matchReason}", fontSize = 11.sp, color = Color(0xFF4B5563), maxLines = 2)

                                        // Deposit badge
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(if (rec.requiresDeposit) PinkPanther.copy(alpha = 0.5f) else SuccessGreen.copy(alpha = 0.15f))
                                                .padding(horizontal = 8.dp, vertical = 3.dp)
                                        ) {
                                            Text(
                                                text = if (rec.requiresDeposit) "📌 ${rec.depositInfo}" else "🎉 Không cần cọc • Giữ chỗ miễn phí",
                                                fontSize = 10.5.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (rec.requiresDeposit) DustyMerlot else SuccessGreen
                                            )
                                        }

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            OutlinedButton(
                                                onClick = { onNavigateToSalon(rec.salonName) },
                                                shape = RoundedCornerShape(8.dp),
                                                modifier = Modifier.weight(1f).height(32.dp),
                                                contentPadding = PaddingValues(0.dp)
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
                    placeholder = { Text("Hỏi AI (Ví dụ: tiệm nail gần đây, spa không cọc...)", fontSize = 11.5.sp, color = TextMuted) },
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
