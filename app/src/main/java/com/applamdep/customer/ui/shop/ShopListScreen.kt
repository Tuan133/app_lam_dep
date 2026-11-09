package com.applamdep.customer.ui.shop

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.applamdep.customer.core.base.UiState
import com.applamdep.customer.core.di.ViewModelFactory
import com.applamdep.customer.ui.components.GrabEmptyState
import com.applamdep.customer.ui.components.GrabLoadingState
import com.applamdep.customer.ui.components.GrabSearchBar
import com.applamdep.customer.ui.shop.components.ShopCardItem
import com.applamdep.customer.ui.shop.components.ShopFilterBottomSheet
import com.applamdep.customer.ui.theme.*

/**
 * MÀN HÌNH DANH SÁCH & TÌM KIẾM SHOP (NHÓM 2 - P0)
 * - Thanh tìm kiếm Debounce kiểu Grab
 * - Lọc theo số sao, cọc/không cọc, đối tác VIP
 * - Tự động ưu tiên shop VIP lên đầu
 */
@Composable
fun ShopListScreen(
    onNavigateToShopDetail: (String) -> Unit,
    onBack: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    viewModel: ShopListViewModel = viewModel(factory = ViewModelFactory.Factory)
) {
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val filterState by viewModel.filterState.collectAsStateWithLifecycle()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    var showFilterSheet by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = GrabBackground,
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(GrabSurface)
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                // Thanh tìm kiếm kết hợp nút lọc
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (onBack != null) {
                        IconButton(
                            onClick = onBack,
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = GrabTextPrimary
                            )
                        }
                    }
                    GrabSearchBar(
                        query = searchQuery,
                        onQueryChange = viewModel::onSearchQueryChanged,
                        placeholder = "Tìm salon, spa tại Đà Nẵng...",
                        modifier = Modifier.weight(1f)
                    )

                    // Nút mở BottomSheet bộ lọc
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .clip(RoundedCornerShape(25.dp))
                            .background(if (filterState != ShopFilter()) GrabGreenLight else GrabSurface)
                            .border(
                                width = 1.dp,
                                color = if (filterState != ShopFilter()) GrabGreen else GrabBorder,
                                shape = RoundedCornerShape(25.dp)
                            )
                            .clickable { showFilterSheet = true },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = "Filter",
                            tint = if (filterState != ShopFilter()) GrabGreenDark else GrabTextPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Thanh Quick Chips lọc nhanh
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        QuickFilterChip(
                            label = "Tất cả",
                            isSelected = filterState == ShopFilter(),
                            onClick = { viewModel.resetFilter() }
                        )
                    }
                    item {
                        QuickFilterChip(
                            label = "★ Đối tác VIP",
                            isSelected = filterState.onlyVip,
                            onClick = {
                                viewModel.onFilterChanged(filterState.copy(onlyVip = !filterState.onlyVip))
                            }
                        )
                    }
                    item {
                        QuickFilterChip(
                            label = "Không cần cọc",
                            isSelected = filterState.requireDeposit == false,
                            onClick = {
                                val next = if (filterState.requireDeposit == false) null else false
                                viewModel.onFilterChanged(filterState.copy(requireDeposit = next))
                            }
                        )
                    }
                    item {
                        QuickFilterChip(
                            label = "Cần cọc",
                            isSelected = filterState.requireDeposit == true,
                            onClick = {
                                val next = if (filterState.requireDeposit == true) null else true
                                viewModel.onFilterChanged(filterState.copy(requireDeposit = next))
                            }
                        )
                    }
                    item {
                        QuickFilterChip(
                            label = "★ 4.5+",
                            isSelected = filterState.minRating == 4.5,
                            onClick = {
                                val next = if (filterState.minRating == 4.5) 0.0 else 4.5
                                viewModel.onFilterChanged(filterState.copy(minRating = next))
                            }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (val state = uiState) {
                is UiState.Idle, is UiState.Loading -> {
                    GrabLoadingState()
                }
                is UiState.Error -> {
                    GrabEmptyState(
                        message = state.message,
                        onRetry = { viewModel.onSearchQueryChanged(searchQuery) }
                    )
                }
                is UiState.Success -> {
                    val shops = state.data
                    if (shops.isEmpty()) {
                        GrabEmptyState(
                            message = "Không tìm thấy cửa hàng làm đẹp phù hợp với tìm kiếm của bạn.",
                            onRetry = { viewModel.resetFilter() }
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            items(
                                items = shops,
                                key = { it.id }
                            ) { shop ->
                                ShopCardItem(
                                    shop = shop,
                                    onClick = { onNavigateToShopDetail(shop.id) }
                                )
                            }
                        }
                    }
                }
                else -> Unit
            }

            // BottomSheet bộ lọc chi tiết
            if (showFilterSheet) {
                ShopFilterBottomSheet(
                    currentFilter = filterState,
                    onApplyFilter = viewModel::onFilterChanged,
                    onDismiss = { showFilterSheet = false }
                )
            }
        }
    }
}

@Composable
private fun QuickFilterChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(if (isSelected) GrabGreenLight else GrabSurface)
            .border(
                width = 1.dp,
                color = if (isSelected) GrabGreen else GrabBorder,
                shape = RoundedCornerShape(16.dp)
            )
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = if (isSelected) GrabGreenDark else GrabTextSecondary
        )
    }
}
