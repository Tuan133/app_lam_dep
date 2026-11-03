package com.example.app_lam_dep.ui.components

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.app_lam_dep.ui.icons.GlowIcon
import com.example.app_lam_dep.ui.icons.GlowIcons
import com.example.app_lam_dep.ui.theme.DustyMerlot
import com.example.app_lam_dep.ui.theme.PinkPanther
import com.example.app_lam_dep.ui.theme.TextMuted

private data class NavTab(val label: String, val icon: ImageVector)

@Composable
fun BottomNavigationBar(
    currentRole: Int, // 0: Customer, 1: Partner, 2: Admin
    currentTab: Int,
    onTabSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val tabs = when (currentRole) {
        2 -> {
            // Admin Tabs (Matching Image 3 & User Spec 4: Trang chủ, Cửa hàng, Khách hàng, Thêm)
            listOf(
                NavTab("Trang chủ", GlowIcons.Home),
                NavTab("Cửa hàng", GlowIcons.Store),
                NavTab("Khách hàng", GlowIcons.Users),
                NavTab("Thêm", GlowIcons.MoreHorizontal)
            )
        }
        1 -> {
            // Partner Tabs (Matching Image 2: Trang chủ, Lịch, Tin nhắn, Tôi)
            listOf(
                NavTab("Trang chủ", GlowIcons.Home),
                NavTab("Lịch", GlowIcons.Calendar),
                NavTab("Tin nhắn", GlowIcons.Chat),
                NavTab("Tôi", GlowIcons.Settings)
            )
        }
        else -> {
            // Customer Tabs (Matching Image 1: Khám phá, Lịch, Tin nhắn, Tôi)
            listOf(
                NavTab("Khám phá", GlowIcons.Sparkle),
                NavTab("Lịch", GlowIcons.Calendar),
                NavTab("Tin nhắn", GlowIcons.Chat),
                NavTab("Tôi", GlowIcons.User)
            )
        }
    }

    NavigationBar(
        modifier = modifier,
        containerColor = Color(0xFFFBF7E8),
        tonalElevation = 0.dp
    ) {
        tabs.forEachIndexed { index, tab ->
            val selected = currentTab == index
            NavigationBarItem(
                selected = selected,
                onClick = { onTabSelected(index) },
                icon = { GlowIcon(icon = tab.icon, tint = LocalContentColor.current, size = 22.dp, contentDescription = tab.label) },
                label = {
                    Text(
                        text = tab.label,
                        fontSize = 11.sp,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                    )
                },
                colors = navigationColors()
            )
        }
    }
}

@Composable
private fun navigationColors() = NavigationBarItemDefaults.colors(
    selectedIconColor = DustyMerlot,
    selectedTextColor = DustyMerlot,
    unselectedIconColor = TextMuted.copy(alpha = 0.85f),
    unselectedTextColor = TextMuted.copy(alpha = 0.85f),
    indicatorColor = Color(0xFFF4D7D3)
)
