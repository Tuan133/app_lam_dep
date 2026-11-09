package com.example.app_lam_dep.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.app_lam_dep.ui.icons.GlowIcon
import com.example.app_lam_dep.ui.icons.GlowIcons
import com.example.app_lam_dep.ui.theme.ChampagneGold
import com.example.app_lam_dep.ui.theme.GoldSoft

@Composable
fun AppHeader(
    currentRole: Int, // 0: Customer, 1: Partner, 2: Admin
    onRoleChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(Color(0xFF360814))
            .statusBarsPadding()
            .padding(horizontal = 10.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // App Logo & Title (Weighted so it shrinks text if needed without pushing the role pills)
        Row(
            modifier = Modifier
                .weight(1f)
                .padding(end = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color.White.copy(alpha = 0.08f))
                    .border(1.dp, Brush.linearGradient(listOf(GoldSoft, ChampagneGold.copy(alpha = 0.5f))), RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                GlowIcon(icon = GlowIcons.Diamond, tint = GoldSoft, size = 18.dp, contentDescription = "Glow Prestige")
            }
            Column {
                Text(
                    text = "GLOW PRESTIGE",
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 0.5.sp,
                    maxLines = 1
                )
                Text(
                    text = when (currentRole) {
                        1 -> "Đối tác Salon"
                        2 -> "Quản trị Admin"
                        else -> "Khám phá vẻ đẹp"
                    },
                    color = Color(0xFFE5B8C2),
                    fontSize = 9.5.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        // 3-Role Switcher Pills (Khách Hàng | Salon | Admin) - Compact & guaranteed to fit on mobile screens
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(Color(0xFF1B0308).copy(alpha = 0.85f))
                .border(0.75.dp, Color(0xFFE5B8C2).copy(alpha = 0.2f), RoundedCornerShape(20.dp))
                .padding(2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RoleTabButton(
                text = "Khách Hàng",
                isSelected = currentRole == 0,
                onClick = { onRoleChange(0) }
            )
            RoleTabButton(
                text = "Salon",
                isSelected = currentRole == 1,
                onClick = { onRoleChange(1) }
            )
            RoleTabButton(
                text = "Admin",
                isSelected = currentRole == 2,
                onClick = { onRoleChange(2) }
            )
        }
    }
}

@Composable
private fun RoleTabButton(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = if (isSelected) Color.White else Color.Transparent
    val textColor = if (isSelected) Color(0xFF360814) else Color.White.copy(alpha = 0.85f)

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(backgroundColor)
            .clickable { onClick() }
            .padding(horizontal = 7.dp, vertical = 5.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = textColor,
            fontSize = 10.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
        )
    }
}
