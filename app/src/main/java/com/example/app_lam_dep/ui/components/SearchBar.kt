package com.example.app_lam_dep.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.app_lam_dep.ui.icons.GlowIcon
import com.example.app_lam_dep.ui.icons.GlowIcons
import com.example.app_lam_dep.ui.theme.DustyMerlot
import com.example.app_lam_dep.ui.theme.TextMuted

@Composable
fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onFilterClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White)
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Filter / Menu Icon Button
        IconButton(
            onClick = onFilterClick,
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(DustyMerlot.copy(alpha = 0.1f))
        ) {
            GlowIcon(icon = GlowIcons.Settings, tint = DustyMerlot, size = 18.dp, contentDescription = "Bộ lọc")
        }

        // Search Input TextField
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
            contentAlignment = Alignment.CenterStart
        ) {
            if (query.isEmpty()) {
                Text(
                    text = "Tìm kiếm salon, nail, spa, dịch vụ...",
                    color = TextMuted,
                    fontSize = 14.sp
                )
            }
            androidx.compose.foundation.text.BasicTextField(
                value = query,
                onValueChange = onQueryChange,
                textStyle = LocalTextStyle.current.copy(
                    color = Color.Black,
                    fontSize = 14.sp
                ),
                modifier = Modifier.fillMaxWidth()
            )
        }

        // Search Icon
        GlowIcon(icon = GlowIcons.Search, tint = DustyMerlot.copy(alpha = 0.75f), size = 20.dp, contentDescription = "Tìm kiếm")
    }
}
