package com.applamdep.merchant.ui.component

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.EventNote
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.app_lam_dep.R
import com.applamdep.merchant.ui.theme.MerchantBackground
import com.applamdep.merchant.ui.theme.MerchantPrimary
import com.applamdep.merchant.ui.theme.MerchantSurface
import com.applamdep.merchant.ui.theme.MerchantTextSecondary

enum class MerchantNavTab(val titleRes: Int, val icon: ImageVector) {
    DASHBOARD(R.string.merchant_nav_dashboard, Icons.Default.Dashboard),
    BOOKINGS(R.string.merchant_nav_bookings, Icons.AutoMirrored.Filled.EventNote),
    SERVICES(R.string.merchant_nav_services, Icons.Default.Spa),
    SCHEDULE(R.string.merchant_nav_schedule, Icons.Default.CalendarMonth),
    FINANCES(R.string.merchant_nav_finances, Icons.Default.AccountBalanceWallet)
}

/**
 * Bottom Navigation Bar phong cách Grab cho Merchant Portal
 */
@Composable
fun MerchantBottomBar(
    currentTab: MerchantNavTab,
    onTabSelected: (MerchantNavTab) -> Unit,
    modifier: Modifier = Modifier
) {
    NavigationBar(
        containerColor = MerchantSurface,
        tonalElevation = 8.dp,
        modifier = modifier
    ) {
        MerchantNavTab.entries.forEach { tab ->
            val isSelected = currentTab == tab
            NavigationBarItem(
                selected = isSelected,
                onClick = { onTabSelected(tab) },
                icon = {
                    Icon(
                        imageVector = tab.icon,
                        contentDescription = stringResource(tab.titleRes)
                    )
                },
                label = {
                    Text(
                        text = stringResource(tab.titleRes),
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MerchantPrimary,
                    selectedTextColor = MerchantPrimary,
                    unselectedIconColor = MerchantTextSecondary,
                    unselectedTextColor = MerchantTextSecondary,
                    indicatorColor = MerchantBackground
                )
            )
        }
    }
}
