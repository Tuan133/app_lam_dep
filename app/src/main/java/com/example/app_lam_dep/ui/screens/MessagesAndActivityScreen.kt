package com.example.app_lam_dep.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.applamdep.customer.data.repository.RealtimeChatRepository
import com.example.app_lam_dep.ui.icons.GlowIcon
import com.example.app_lam_dep.ui.icons.GlowIcons

data class ConversationItem(
    val id: Int,
    val name: String,
    val snippet: String,
    val time: String,
    val tag: String,
    val unreadCount: Int = 0,
    val hasUnreadDot: Boolean = false,
    val isStore: Boolean = false,
    val isSystem: Boolean = false,
    val isOnline: Boolean = false,
    val convId: String = ""
)

private data class ChatContactMeta(
    val convId: String,
    val displayName: String,
    val phone: String,
    val tag: String,
    val profileDetail: String,
    val appointmentNote: String,
    val isOnline: Boolean
)

private data class ActivityNotificationCardData(
    val title: String,
    val description: String,
    val timestampLabel: String,
    val icon: ImageVector
)

private val merchantDirectory = listOf(
    ChatContactMeta(
        convId = "salon_01",
        displayName = "Khánh Linh",
        phone = "0903 123 456",
        tag = "Khách hàng",
        profileDetail = "SĐT: 0903 123 456 · Khách đặt lịch thường xuyên tại 1994 Nailsroom",
        appointmentNote = "Đơn #BK-9024 · Sơn Gel Hữu Cơ Pháp · Tiền cọc: 100.000 đ",
        isOnline = true
    ),
    ChatContactMeta(
        convId = "cust_mai_phuong",
        displayName = "Mai Phương",
        phone = "0905 112 233",
        tag = "Khách đặt lịch",
        profileDetail = "SĐT: 0905 112 233 · Khách đặt khung giờ 09:00 - 10:00",
        appointmentNote = "Đơn #BK-9021 · Sơn Gel Hữu Cơ Pháp + Dưỡng Móng · Cọc: 100.000 đ",
        isOnline = true
    ),
    ChatContactMeta(
        convId = "cust_thao_my",
        displayName = "Thảo My",
        phone = "0914 556 778",
        tag = "Khách đặt lịch",
        profileDetail = "SĐT: 0914 556 778 · Lưu ý: Mắt hơi nhạy cảm với keo cay",
        appointmentNote = "Đơn #BK-9022 · Nối Mi Thiết Kế Sợi Lụa Tự Nhiên · Cọc: 100.000 đ",
        isOnline = true
    ),
    ChatContactMeta(
        convId = "cust_thu_ha",
        displayName = "Thu Hà",
        phone = "0912 345 678",
        tag = "Đánh giá 5★",
        profileDetail = "SĐT: 0912 345 678 · Đã đánh giá 5★ cho tiệm",
        appointmentNote = "Đã hoàn thành dịch vụ Nail Art thiết kế · Phản hồi tích cực",
        isOnline = false
    )
)

private val customerDirectory = listOf(
    ChatContactMeta(
        convId = "salon_01",
        displayName = "1994 Nailsroom",
        phone = "0762 792 671",
        tag = "Salon đối tác",
        profileDetail = "Địa chỉ: 172/5 Trần Phú, Hải Châu, Đà Nẵng · Giờ mở cửa: 09:00 - 20:00",
        appointmentNote = "Hotline: 0762 792 671 · Nail, Nối Mi & Chăm sóc móng chuyên nghiệp",
        isOnline = true
    ),
    ChatContactMeta(
        convId = "salon_02",
        displayName = "BONNIE NAIL",
        phone = "0912 154 434",
        tag = "Salon đối tác",
        profileDetail = "Địa chỉ: 205 Dương Đình Nghệ, Sơn Trà, Đà Nẵng · Giờ mở cửa: 09:00 - 20:30",
        appointmentNote = "Hotline: 0912 154 434 · Thiết kế Nail Hàn Quốc & Ombre",
        isOnline = true
    ),
    ChatContactMeta(
        convId = "salon_09",
        displayName = "Thì Là SPA",
        phone = "0905 668 991",
        tag = "Salon đối tác",
        profileDetail = "Địa chỉ: 48 Nguyễn Thị Minh Khai, Hải Châu, Đà Nẵng · Giờ mở cửa: 08:30 - 21:00",
        appointmentNote = "Hotline: 0905 668 991 · Gội đầu dưỡng sinh & Spa thư giãn",
        isOnline = false
    ),
    ChatContactMeta(
        convId = "salon_glamour",
        displayName = "The Glamour Hair Salon",
        phone = "0906 118 899",
        tag = "Salon đối tác",
        profileDetail = "Địa chỉ: 86 Lê Duẩn, Hải Châu, Đà Nẵng · Giờ mở cửa: 09:00 - 21:00",
        appointmentNote = "Hotline: 0906 118 899 · Chăm sóc tóc & Tạo kiểu cao cấp",
        isOnline = false
    )
)

private fun normalizeDisplayChatName(rawName: String): String {
    return rawName.removePrefix("Khách hàng:").trim()
}

private fun resolveContactMeta(convId: String, chatName: String, isMerchant: Boolean): ChatContactMeta {
    val cleanName = normalizeDisplayChatName(chatName)
    val directory = if (isMerchant) merchantDirectory else customerDirectory
    return directory.firstOrNull { it.convId == convId }
        ?: directory.firstOrNull { it.displayName.equals(cleanName, ignoreCase = true) }
        ?: if (isMerchant) {
            ChatContactMeta(
                convId = convId,
                displayName = cleanName,
                phone = "0903 123 456",
                tag = "Khách hàng",
                profileDetail = "Khách hàng tương tác qua tin nhắn trực tiếp với cửa hàng.",
                appointmentNote = "Chưa có ghi chú lịch hẹn riêng",
                isOnline = true
            )
        } else {
            ChatContactMeta(
                convId = convId,
                displayName = cleanName,
                phone = "0762 792 671",
                tag = "Salon đối tác",
                profileDetail = "Đối tác dịch vụ làm đẹp trên hệ thống Glow Prestige.",
                appointmentNote = "Hỗ trợ đặt lịch và tư vấn trực tiếp",
                isOnline = true
            )
        }
}

private fun dialPhoneNumber(context: android.content.Context, phone: String, targetName: String) {
    val digits = phone.replace(Regex("[^0-9+]"), "")
    if (digits.isNotBlank()) {
        try {
            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$digits"))
            context.startActivity(intent)
            return
        } catch (_: Exception) {
        }
    }
    Toast.makeText(context, "Đang gọi tới $targetName ($phone)...", Toast.LENGTH_SHORT).show()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessagesAndActivityScreen(
    onChatClick: (String) -> Unit,
    isMerchant: Boolean = false,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    LaunchedEffect(Unit) {
        RealtimeChatRepository.init(context)
    }
    val allMessagesMap by RealtimeChatRepository.messagesFlow.collectAsState()
    val readCountsMap by RealtimeChatRepository.readCountsFlow.collectAsState()

    var selectedTab by remember { mutableIntStateOf(1) } // 0: Hoạt động, 1: Tin nhắn
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("Tất cả") }
    var showComposeDialog by remember { mutableStateOf(false) }

    val filterOptions = listOf("Tất cả", "Chưa đọc")
    val viewerRole = if (isMerchant) "MERCHANT" else "CUSTOMER"

    // Xây dựng danh sách hội thoại đồng bộ giữa Khách hàng & Cửa hàng
    val conversations = remember(allMessagesMap, readCountsMap, isMerchant) {
        val baseDirectory = if (isMerchant) merchantDirectory else customerDirectory
        val mapped = baseDirectory.mapIndexed { index, contact ->
            val thread = allMessagesMap[contact.convId].orEmpty()
            val latest = thread.lastOrNull()
            val unread = RealtimeChatRepository.getUnreadCount(contact.convId, viewerRole)
            ConversationItem(
                id = index + 1,
                name = contact.displayName,
                snippet = latest?.text ?: "Chưa có tin nhắn",
                time = latest?.time ?: "Hôm nay",
                tag = contact.tag,
                unreadCount = unread,
                hasUnreadDot = unread > 0,
                isStore = !isMerchant,
                isOnline = contact.isOnline,
                convId = contact.convId
            )
        }

        // Bổ sung các cuộc trò chuyện mới được tạo qua nút Soạn tin (conv_*)
        val knownConvIds = baseDirectory.map { it.convId }.toSet()
        val extraConvItems = allMessagesMap.entries
            .filter { (id, msgs) -> id.startsWith("conv_") && id !in knownConvIds && msgs.isNotEmpty() }
            .mapIndexed { idx, (id, msgs) ->
                val latest = msgs.last()
                val title = msgs.firstOrNull { !it.senderRole.equals(viewerRole, ignoreCase = true) }?.senderName
                    ?: id.removePrefix("conv_").replace('_', ' ')
                        .replaceFirstChar { it.uppercase() }
                val unread = RealtimeChatRepository.getUnreadCount(id, viewerRole)
                ConversationItem(
                    id = 100 + idx,
                    name = title,
                    snippet = latest.text,
                    time = latest.time,
                    tag = if (isMerchant) "Khách hàng" else "Salon đối tác",
                    unreadCount = unread,
                    hasUnreadDot = unread > 0,
                    isStore = !isMerchant,
                    isOnline = true,
                    convId = id
                )
            }

        mapped + extraConvItems
    }

    val filteredConversations = conversations.filter { item ->
        val matchesSearch = searchQuery.isEmpty() ||
            item.name.contains(searchQuery, ignoreCase = true) ||
            item.snippet.contains(searchQuery, ignoreCase = true)
        val matchesFilter = when (selectedFilter) {
            "Chưa đọc" -> item.hasUnreadDot || item.unreadCount > 0
            else -> true
        }
        matchesSearch && matchesFilter
    }

    val totalUnreadConversations = conversations.count { it.unreadCount > 0 }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFFBF7E8))
    ) {
        // 1. Top Tabs Bar: Hoạt động | Tin nhắn + Nút soạn tin nhắn
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(horizontal = 20.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.clickable { selectedTab = 0 },
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Hoạt động",
                        fontSize = 17.sp,
                        fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Medium,
                        color = if (selectedTab == 0) Color(0xFF6B1228) else Color(0xFF7A686D)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Box(
                        modifier = Modifier
                            .width(50.dp)
                            .height(3.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(if (selectedTab == 0) Color(0xFF6B1228) else Color.Transparent)
                    )
                }

                Column(
                    modifier = Modifier.clickable { selectedTab = 1 },
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "Tin nhắn",
                            fontSize = 17.sp,
                            fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Medium,
                            color = if (selectedTab == 1) Color(0xFF6B1228) else Color(0xFF7A686D)
                        )
                        if (totalUnreadConversations > 0) {
                            Box(
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(Color(0xFFE11D48))
                                    .padding(horizontal = 6.dp, vertical = 1.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = totalUnreadConversations.toString(),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Box(
                        modifier = Modifier
                            .width(50.dp)
                            .height(3.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(if (selectedTab == 1) Color(0xFF6B1228) else Color.Transparent)
                    )
                }
            }

            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .clickable { showComposeDialog = true },
                contentAlignment = Alignment.Center
            ) {
                GlowIcon(
                    icon = GlowIcons.EditPencil,
                    tint = Color(0xFF6B1228),
                    size = 20.dp,
                    contentDescription = "Soạn tin nhắn"
                )
            }
        }

        if (selectedTab == 0) {
            ActivityTabContent(isMerchant = isMerchant)
        } else {
            // 2. Search Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .clip(RoundedCornerShape(22.dp))
                        .background(Color(0xFFF5EFEF))
                        .padding(horizontal = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    GlowIcon(icon = GlowIcons.Search, tint = Color(0xFF8E7A80), size = 18.dp)
                    BasicTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        singleLine = true,
                        textStyle = TextStyle(
                            fontSize = 13.5.sp,
                            color = Color(0xFF24090F),
                            fontWeight = FontWeight.Medium
                        ),
                        modifier = Modifier.weight(1f),
                        decorationBox = { innerTextField ->
                            Box(
                                modifier = Modifier.fillMaxWidth(),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                if (searchQuery.isEmpty()) {
                                    Text(
                                        text = if (isMerchant) "Tìm kiếm khách hàng, tin nhắn..." else "Tìm kiếm tin nhắn...",
                                        color = Color(0xFF8E7A80),
                                        fontSize = 13.sp
                                    )
                                }
                                innerTextField()
                            }
                        }
                    )
                    if (searchQuery.isNotEmpty()) {
                        IconButton(
                            onClick = { searchQuery = "" },
                            modifier = Modifier.size(22.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Xóa tìm kiếm",
                                tint = Color(0xFF8E7A80),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            // 3. Filter Pills Row
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filterOptions) { filter ->
                    val isSelected = filter == selectedFilter
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(18.dp))
                            .background(if (isSelected) Color(0xFF62202F) else Color.White)
                            .border(
                                width = if (isSelected) 0.dp else 1.dp,
                                color = if (isSelected) Color.Transparent else Color(0xFFEBDCD3),
                                shape = RoundedCornerShape(18.dp)
                            )
                            .clickable { selectedFilter = filter }
                            .padding(horizontal = 16.dp, vertical = 7.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (filter == "Chưa đọc" && totalUnreadConversations > 0) {
                                "Chưa đọc ($totalUnreadConversations)"
                            } else filter,
                            fontSize = 12.5.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) Color.White else Color(0xFF7A686D)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            if (filteredConversations.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (selectedFilter == "Chưa đọc") {
                            "Tất cả tin nhắn đã được đọc"
                        } else {
                            "Không tìm thấy cuộc trò chuyện phù hợp"
                        },
                        fontSize = 14.sp,
                        color = Color(0xFF7A686D)
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    contentPadding = PaddingValues(bottom = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredConversations, key = { it.id }) { conv ->
                        ConversationRowCard(
                            conv = conv,
                            onClick = {
                                val resolvedId = conv.convId.ifBlank { RealtimeChatRepository.getConversationId(conv.name) }
                                RealtimeChatRepository.markAsRead(resolvedId, viewerRole)
                                onChatClick(conv.name)
                            }
                        )
                    }
                }
            }
        }
    }

    if (showComposeDialog) {
        ComposeChatDialog(
            isMerchant = isMerchant,
            onSelectRecipient = { recipientName ->
                showComposeDialog = false
                val convId = RealtimeChatRepository.getConversationId(recipientName)
                RealtimeChatRepository.markAsRead(convId, viewerRole)
                onChatClick(recipientName)
            },
            onDismiss = { showComposeDialog = false }
        )
    }
}

@Composable
private fun ActivityTabContent(isMerchant: Boolean) {
    val items = remember(isMerchant) {
        if (isMerchant) {
            listOf(
                ActivityNotificationCardData(
                    title = "Đơn hẹn mới chờ duyệt (#BK-9021)",
                    description = "Khách hàng Mai Phương vừa đặt Sơn Gel Hữu Cơ Pháp + Dưỡng Móng · Tiền cọc: 100.000 đ.",
                    timestampLabel = "5 phút trước · Chờ xác nhận",
                    icon = GlowIcons.Calendar
                ),
                ActivityNotificationCardData(
                    title = "Lịch hẹn đã hoàn tất & cộng doanh thu",
                    description = "Tiền cọc từ các lịch hẹn đã hoàn thành được tự động cộng dồn vào doanh thu cửa hàng.",
                    timestampLabel = "Cập nhật tự động · Đối soát tiền cọc",
                    icon = GlowIcons.Coins
                ),
                ActivityNotificationCardData(
                    title = "Tin nhắn mới từ khách hàng",
                    description = "Khánh Linh vừa gửi tin nhắn xác nhận giờ ghé tiệm 1994 Nailsroom.",
                    timestampLabel = "Hôm nay · Hộp thoại Chat",
                    icon = GlowIcons.Bell
                )
            )
        } else {
            listOf(
                ActivityNotificationCardData(
                    title = "Nhắc lịch hẹn sắp tới",
                    description = "1994 Nailsroom: Sơn Gel Hữu Cơ vào lúc 15:30 Hôm nay.",
                    timestampLabel = "Vừa xong · Thời gian thực",
                    icon = GlowIcons.Calendar
                ),
                ActivityNotificationCardData(
                    title = "Tiền cọc giữ chỗ đã được bảo vệ",
                    description = "Khoản cọc 100.000đ cho lịch hẹn tại 1994 Nailsroom được sàn giữ an toàn và chỉ chuyển cho salon khi dịch vụ hoàn tất.",
                    timestampLabel = "2 giờ trước · Giao dịch an toàn",
                    icon = GlowIcons.Coins
                )
            )
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = if (isMerchant) "Hoạt động cửa hàng & Nhắc lịch" else "Cập nhật hoạt động & Nhắc hẹn",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF24090F)
            )
        }
        items(items) { activity ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFF9EFEF)),
                        contentAlignment = Alignment.Center
                    ) {
                        GlowIcon(icon = activity.icon, tint = Color(0xFF6B1228), size = 20.dp)
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            text = activity.title,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Color(0xFF24090F)
                        )
                        Text(
                            text = activity.description,
                            fontSize = 12.sp,
                            color = Color(0xFF7A686D)
                        )
                        Text(
                            text = activity.timestampLabel,
                            fontSize = 10.5.sp,
                            color = Color(0xFF9E8A90)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ConversationRowCard(
    conv: ConversationItem,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .width(10.dp)
                    .padding(end = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                if (conv.hasUnreadDot) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFE11D48))
                    )
                }
            }

            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFF9EFEF)),
                contentAlignment = Alignment.Center
            ) {
                val icon = when {
                    conv.isStore -> GlowIcons.Store
                    conv.isSystem -> GlowIcons.Bell
                    else -> GlowIcons.User
                }
                GlowIcon(
                    icon = icon,
                    tint = Color(0xFF6B1228),
                    size = 22.dp,
                    contentDescription = conv.name
                )

                if (conv.isOnline) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .size(11.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF10B981))
                            .border(1.5.dp, Color.White, CircleShape)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Text(
                    text = conv.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = Color(0xFF24090F),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = conv.snippet,
                    fontSize = 12.sp,
                    color = if (conv.unreadCount > 0) Color(0xFF24090F) else Color(0xFF7A686D),
                    fontWeight = if (conv.unreadCount > 0) FontWeight.Medium else FontWeight.Normal,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFFFCEEEB))
                        .padding(horizontal = 7.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = conv.tag,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF6B1228)
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = conv.time,
                    fontSize = 11.sp,
                    color = Color(0xFF9E8A90)
                )

                if (conv.unreadCount > 0) {
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF62202F)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = conv.unreadCount.toString(),
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                } else {
                    Spacer(modifier = Modifier.size(20.dp))
                }
            }
        }
    }
}

@Composable
private fun ComposeChatDialog(
    isMerchant: Boolean,
    onSelectRecipient: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var customName by remember { mutableStateOf("") }
    val directory = if (isMerchant) merchantDirectory else customerDirectory

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (isMerchant) "Nhắn tin cho khách hàng" else "Nhắn tin tới Salon",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = Color(0xFF24090F)
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 360.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = customName,
                    onValueChange = { customName = it },
                    label = { Text(if (isMerchant) "Nhập tên khách hàng mới" else "Nhập tên salon") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                if (customName.trim().length >= 2) {
                    Button(
                        onClick = { onSelectRecipient(customName.trim()) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6B1228)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Bắt đầu chat với \"${customName.trim()}\"")
                    }
                }

                HorizontalDivider(color = Color(0xFFEBDCD3))
                Text(
                    text = if (isMerchant) "Khách hàng gần đây:" else "Salon đối tác:",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF7A686D)
                )

                directory.forEach { contact ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFFFBF7E8))
                            .border(1.dp, Color(0xFFEBDCD3), RoundedCornerShape(10.dp))
                            .clickable { onSelectRecipient(contact.displayName) }
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        GlowIcon(
                            icon = if (isMerchant) GlowIcons.User else GlowIcons.Store,
                            tint = Color(0xFF6B1228),
                            size = 18.dp
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = contact.displayName,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.5.sp,
                                color = Color(0xFF24090F)
                            )
                            Text(
                                text = contact.phone,
                                fontSize = 11.5.sp,
                                color = Color(0xFF7A686D)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Đóng", color = Color(0xFF7A686D))
            }
        }
    )
}

data class ChatMessage(
    val text: String,
    val isMe: Boolean,
    val time: String
)

@Composable
fun ChatDetailScreen(
    chatName: String,
    isMerchant: Boolean = false,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var messageText by remember { mutableStateOf("") }
    var showMenu by remember { mutableStateOf(false) }
    var showProfileDialog by remember { mutableStateOf(false) }
    var showEmojiBar by remember { mutableStateOf(false) }
    var showQuickTemplatesDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        RealtimeChatRepository.init(context)
    }

    val convId = remember(chatName) {
        RealtimeChatRepository.getConversationId(chatName)
    }
    val contactMeta = remember(convId, chatName, isMerchant) {
        resolveContactMeta(convId = convId, chatName = chatName, isMerchant = isMerchant)
    }
    val viewerRole = if (isMerchant) "MERCHANT" else "CUSTOMER"
    val mySenderName = if (isMerchant) "1994 Nailsroom" else "Khánh Linh"

    val allMessagesMap by RealtimeChatRepository.messagesFlow.collectAsState()
    val messages = allMessagesMap[convId] ?: emptyList()

    LaunchedEffect(convId, messages.size, viewerRole) {
        RealtimeChatRepository.markAsRead(convId, viewerRole)
    }

    val listState = rememberLazyListState()
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    val sendChatMessage: (String) -> Unit = { content ->
        RealtimeChatRepository.sendMessage(
            conversationId = convId,
            senderRole = viewerRole,
            senderName = mySenderName,
            text = content,
            autoReply = !isMerchant
        )
    }

    val quickReplies = remember(isMerchant) {
        if (isMerchant) {
            listOf(
                "Dạ tiệm còn slot ạ ✨",
                "Em đã xác nhận lịch hẹn cho mình nhé 📅",
                "Địa chỉ tiệm: 172/5 Trần Phú 📍"
            )
        } else {
            listOf(
                "Tiệm còn slot hôm nay không ạ?",
                "Cho em xin bảng giá dịch vụ nhé",
                "Địa chỉ salon ở đâu ạ?"
            )
        }
    }

    val quickEmojis = listOf("✨", "💅", "💖", "📅", "📍", "👍", "🌸", "🙏")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFBF7E8))
            .imePadding()
    ) {
        // Chat Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF360814))
                .statusBarsPadding()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color.White.copy(alpha = 0.15f))
                    .clickable { onBack() },
                contentAlignment = Alignment.Center
            ) {
                GlowIcon(icon = GlowIcons.ChevronLeft, tint = Color.White, size = 20.dp, contentDescription = "Quay lại")
            }

            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFFCEEEB)),
                contentAlignment = Alignment.Center
            ) {
                GlowIcon(
                    icon = if (isMerchant) GlowIcons.User else GlowIcons.Crown,
                    tint = Color(0xFF6B1228),
                    size = 20.dp
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = contactMeta.displayName,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = if (isMerchant) {
                        "${contactMeta.tag} · ${if (contactMeta.isOnline) "Đang trực tuyến" else "Ngoại tuyến"}"
                    } else {
                        "Đang trực tuyến · Trả lời tức thì"
                    },
                    fontSize = 11.sp,
                    color = Color(0xFFF3C2CA)
                )
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                GlowIcon(
                    icon = GlowIcons.Phone,
                    tint = Color.White,
                    size = 18.dp,
                    contentDescription = "Gọi thoại",
                    modifier = Modifier.clickable {
                        dialPhoneNumber(context, contactMeta.phone, contactMeta.displayName)
                    }
                )

                Box {
                    IconButton(
                        onClick = { showMenu = true },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Menu tiện ích",
                            tint = Color.White
                        )
                    }

                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false },
                        modifier = Modifier.background(Color.White)
                    ) {
                        if (isMerchant) {
                            DropdownMenuItem(
                                text = { Text("Xem thông tin khách hàng", color = Color(0xFF24090F), fontWeight = FontWeight.Medium) },
                                onClick = {
                                    showMenu = false
                                    showProfileDialog = true
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Gửi nhắc lịch hẹn", color = Color(0xFF24090F), fontWeight = FontWeight.Medium) },
                                onClick = {
                                    showMenu = false
                                    sendChatMessage("1994 Nailsroom nhắc bạn lịch hẹn hôm nay tại tiệm đã sẵn sàng đón tiếp bạn ạ 📅")
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Gửi địa chỉ Salon", color = Color(0xFF24090F), fontWeight = FontWeight.Medium) },
                                onClick = {
                                    showMenu = false
                                    sendChatMessage("Địa chỉ 1994 Nailsroom: 172/5 Trần Phú, Hải Châu, Đà Nẵng 📍")
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Gọi điện cho khách hàng", color = Color(0xFF6B1228), fontWeight = FontWeight.Bold) },
                                onClick = {
                                    showMenu = false
                                    dialPhoneNumber(context, contactMeta.phone, contactMeta.displayName)
                                }
                            )
                        } else {
                            DropdownMenuItem(
                                text = { Text("Xem hồ sơ Salon", color = Color(0xFF24090F), fontWeight = FontWeight.Medium) },
                                onClick = {
                                    showMenu = false
                                    showProfileDialog = true
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Gửi lịch hẹn đã đặt", color = Color(0xFF24090F), fontWeight = FontWeight.Medium) },
                                onClick = {
                                    showMenu = false
                                    sendChatMessage("Em gửi thông tin lịch hẹn: 15:30 Hôm nay tại ${contactMeta.displayName} ạ 📅")
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Gửi vị trí hiện tại", color = Color(0xFF24090F), fontWeight = FontWeight.Medium) },
                                onClick = {
                                    showMenu = false
                                    sendChatMessage("Vị trí của em: 172/5 Trần Phú, Hải Châu, Đà Nẵng 📍")
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Gọi Hotline Salon", color = Color(0xFF6B1228), fontWeight = FontWeight.Bold) },
                                onClick = {
                                    showMenu = false
                                    dialPhoneNumber(context, contactMeta.phone, contactMeta.displayName)
                                }
                            )
                        }
                    }
                }
            }
        }

        // Messages list (Realtime Connected)
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(messages, key = { it.id }) { msg ->
                val isMyMessage = msg.senderRole.equals(viewerRole, ignoreCase = true)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = if (isMyMessage) Arrangement.End else Arrangement.Start
                ) {
                    Column(
                        horizontalAlignment = if (isMyMessage) Alignment.End else Alignment.Start,
                        verticalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .widthIn(max = 280.dp)
                                .clip(RoundedCornerShape(18.dp))
                                .background(if (isMyMessage) Color(0xFF62202F) else Color.White)
                                .border(
                                    1.dp,
                                    if (isMyMessage) Color.Transparent else Color(0xFFEBDCD3),
                                    RoundedCornerShape(18.dp)
                                )
                                .padding(horizontal = 16.dp, vertical = 10.dp)
                        ) {
                            Text(
                                text = msg.text,
                                fontSize = 13.5.sp,
                                color = if (isMyMessage) Color.White else Color(0xFF24090F)
                            )
                        }
                        Text(
                            text = "${msg.time} · ${if (isMyMessage) "Đã gửi" else msg.senderName}",
                            fontSize = 10.sp,
                            color = Color(0xFF9E8A90)
                        )
                    }
                }
            }
        }

        // Quick Reply Chips Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 12.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            quickReplies.forEach { reply ->
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.White)
                        .border(1.dp, Color(0xFFEBDCD3), RoundedCornerShape(16.dp))
                        .clickable { sendChatMessage(reply) }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = reply,
                        fontSize = 11.5.sp,
                        color = Color(0xFF62202F),
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        if (showEmojiBar) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                quickEmojis.forEach { emoji ->
                    Text(
                        text = emoji,
                        fontSize = 20.sp,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { messageText += emoji }
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
        }

        // Bottom Message Input Bar
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding(),
            color = Color.White,
            shadowElevation = 8.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = messageText,
                    onValueChange = { messageText = it },
                    placeholder = { Text("Nhập tin nhắn...", color = Color(0xFF8E7A80), fontSize = 13.sp) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(24.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF62202F),
                        unfocusedBorderColor = Color(0xFFEBDCD3),
                        focusedContainerColor = Color(0xFFFAF6F0),
                        unfocusedContainerColor = Color(0xFFFAF6F0)
                    ),
                    maxLines = 3
                )

                if (messageText.isNotBlank()) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF62202F))
                            .clickable {
                                val textToSend = messageText.trim()
                                messageText = ""
                                showEmojiBar = false
                                sendChatMessage(textToSend)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        GlowIcon(icon = GlowIcons.Check, tint = Color.White, size = 18.dp)
                    }
                } else {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        GlowIcon(
                            icon = GlowIcons.Mic,
                            tint = Color(0xFF7A686D),
                            size = 20.dp,
                            contentDescription = "Gửi câu nhanh",
                            modifier = Modifier.clickable {
                                Toast.makeText(
                                    context,
                                    "Chọn gợi ý phía trên hoặc nhập nội dung để gửi nhanh.",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        )
                        GlowIcon(
                            icon = GlowIcons.Smile,
                            tint = if (showEmojiBar) Color(0xFF6B1228) else Color(0xFF7A686D),
                            size = 20.dp,
                            contentDescription = "Biểu cảm",
                            modifier = Modifier.clickable { showEmojiBar = !showEmojiBar }
                        )
                        GlowIcon(
                            icon = GlowIcons.Image,
                            tint = Color(0xFF7A686D),
                            size = 20.dp,
                            contentDescription = "Gửi mẫu / bảng giá",
                            modifier = Modifier.clickable { showQuickTemplatesDialog = true }
                        )
                    }
                }
            }
        }
    }

    if (showProfileDialog) {
        AlertDialog(
            onDismissRequest = { showProfileDialog = false },
            title = {
                Text(
                    text = if (isMerchant) "Thông tin khách hàng" else "Hồ sơ Salon",
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF24090F)
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = contactMeta.displayName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = Color(0xFF6B1228)
                    )
                    Text(text = contactMeta.profileDetail, fontSize = 13.sp, color = Color(0xFF24090F))
                    Text(text = contactMeta.appointmentNote, fontSize = 12.5.sp, color = Color(0xFF7A686D))
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showProfileDialog = false
                        dialPhoneNumber(context, contactMeta.phone, contactMeta.displayName)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6B1228))
                ) {
                    Text("Gọi ${contactMeta.phone}")
                }
            },
            dismissButton = {
                TextButton(onClick = { showProfileDialog = false }) {
                    Text("Đóng", color = Color(0xFF7A686D))
                }
            }
        )
    }

    if (showQuickTemplatesDialog) {
        val sampleItems = if (isMerchant) {
            listOf(
                "💅 Bảng mẫu Nail thiết kế mới nhất" to "Gửi bạn bảng mẫu Nail thiết kế hot trend tuần này kèm giá ưu đãi ạ ✨",
                "📋 Bảng giá dịch vụ 1994 Nailsroom" to "Sơn Gel Hữu Cơ 180K · Nối Mi Lụa 250K · Chăm sóc da cấp ẩm 350K ạ 💖",
                "📍 Hình ảnh & hướng dẫn đường đến tiệm" to "Tiệm ở 172/5 Trần Phú, Hải Châu, Đà Nẵng (sát cầu Rồng, có chỗ đỗ xe) 📍"
            )
        } else {
            listOf(
                "💅 Ảnh mẫu móng muốn làm" to "Em gửi tham khảo mẫu Sơn Gel Thạch hồng tự nhiên, nhờ salon tư vấn giúp em nhé ✨",
                "📅 Xác nhận giờ hẹn" to "Chiều nay em sẽ ghé đúng giờ theo lịch đã đặt ạ 📅"
            )
        }

        AlertDialog(
            onDismissRequest = { showQuickTemplatesDialog = false },
            title = {
                Text(
                    text = if (isMerchant) "Gửi mẫu & Thông tin nhanh" else "Gửi yêu cầu mẫu nhanh",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color(0xFF24090F)
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    sampleItems.forEach { (label, body) ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFFFBF7E8))
                                .border(1.dp, Color(0xFFEBDCD3), RoundedCornerShape(10.dp))
                                .clickable {
                                    showQuickTemplatesDialog = false
                                    sendChatMessage(body)
                                }
                                .padding(12.dp)
                        ) {
                            Text(text = label, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF24090F))
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showQuickTemplatesDialog = false }) {
                    Text("Đóng", color = Color(0xFF7A686D))
                }
            }
        )
    }
}
