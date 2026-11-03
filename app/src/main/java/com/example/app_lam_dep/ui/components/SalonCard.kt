package com.example.app_lam_dep.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.app_lam_dep.ui.icons.GlowIcon
import com.example.app_lam_dep.ui.icons.GlowIcons
import com.example.app_lam_dep.ui.icons.IconMedallion
import com.example.app_lam_dep.ui.theme.ChampagneGold
import com.example.app_lam_dep.ui.theme.DustyMerlot
import com.example.app_lam_dep.ui.theme.PinkPanther
import com.example.app_lam_dep.ui.theme.TextMuted

data class SalonItem(
    val id: String,
    val name: String,
    val category: String,
    val rating: Double,
    val reviewCount: Int,
    val distance: String,
    val address: String,
    val imageUrl: String = ""
)

@Composable
fun SalonCard(
    salon: SalonItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Thumbnail Image hiển thị ảnh thực tế
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFFF3ECE6)),
                contentAlignment = Alignment.Center
            ) {
                val imgRes = com.applamdep.customer.core.util.ShopImageAssets.getShopDrawableRes(salon.id.ifBlank { salon.name })
                androidx.compose.foundation.Image(
                    painter = androidx.compose.ui.res.painterResource(id = imgRes),
                    contentDescription = salon.name,
                    contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                
                // Distance Tag Top Right
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.Black.copy(alpha = 0.6f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = salon.distance,
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Info Section
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = salon.name,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = DustyMerlot
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = salon.category,
                        fontSize = 13.sp,
                        color = TextMuted
                    )
                }

                // Rating
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(PinkPanther.copy(alpha = 0.3f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    GlowIcon(icon = GlowIcons.StarFilled, tint = ChampagneGold, size = 13.dp)
                    Text(
                        text = "${salon.rating} (${salon.reviewCount})",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = DustyMerlot
                    )
                }
            }

            // Address
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                GlowIcon(icon = GlowIcons.MapPin, tint = DustyMerlot.copy(alpha = 0.7f), size = 14.dp)
                Text(
                    text = salon.address,
                    fontSize = 12.sp,
                    color = TextMuted
                )
            }
        }
    }
}
