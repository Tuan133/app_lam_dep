package com.applamdep.merchant.ui.service

import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.DrawableRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.app_lam_dep.R
import com.applamdep.customer.core.util.ShopImageAssets
import com.applamdep.merchant.core.base.UiState
import com.applamdep.merchant.core.di.MerchantViewModelFactory
import com.applamdep.merchant.core.util.Formatters
import com.applamdep.merchant.domain.model.ServiceItem
import com.applamdep.merchant.domain.rule.ServiceRemovalPolicy
import com.applamdep.merchant.ui.component.EmptyStateView
import com.applamdep.merchant.ui.component.ErrorStateView
import com.applamdep.merchant.ui.component.LoadingStateView
import com.applamdep.merchant.ui.component.MerchantTopBar
import com.applamdep.merchant.ui.component.StatusBadge
import com.applamdep.merchant.ui.theme.*

/**
 * 1. TÍNH CHẤT OOP: ĐÓNG GÓI & TRỪU TƯỢNG
 * GIAI ĐOẠN 4: CHỨC NĂNG 3 - QUẢN LÝ DỊCH VỤ CỬA HÀNG (MERCHANT REQUIREMENT #3)
 * - Quản lý CRUD: Thêm, sửa, xóa dịch vụ, phân loại nhóm dịch vụ.
 * - Thiết lập giá niêm yết (VNĐ), thời lượng phục vụ (phút), mô tả.
 * - Cơ chế bảo vệ dữ liệu: Chuyển sang ẩn (Soft Delete) khi đang có khách đặt hẹn chưa hoàn tất.
 */
@Composable
fun MerchantServiceManagementScreen(
    onBack: () -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: MerchantServiceViewModel = viewModel(factory = MerchantViewModelFactory.DefaultFactory),
    shopId: String = "shop_01"
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()

    var editingService by remember { mutableStateOf<ServiceItem?>(null) }
    var isAddingNew by remember { mutableStateOf(false) }
    var serviceToDelete by remember { mutableStateOf<ServiceItem?>(null) }
    var selectedCategory by remember { mutableStateOf("Tất cả") }
    var searchQuery by remember { mutableStateOf("") }

    LaunchedEffect(shopId) {
        viewModel.loadServices(shopId)
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MerchantBackground,
        topBar = {
            MerchantTopBar(
                title = stringResource(R.string.merchant_service_title),
                onBackClick = onBack
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { isAddingNew = true },
                containerColor = MerchantPrimary,
                contentColor = Color.White,
                shape = CircleShape
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = stringResource(R.string.merchant_service_add))
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (val state = uiState) {
                is UiState.Idle, is UiState.Loading -> {
                    LoadingStateView(message = "Đang tải danh sách dịch vụ...")
                }
                is UiState.Error -> {
                    ErrorStateView(
                        message = state.message,
                        onRetry = { viewModel.loadServices(shopId) }
                    )
                }
                is UiState.Success -> {
                    val allServices = state.data
                    val categories = listOf("Tất cả") + allServices.map { it.category.ifBlank { "Làm đẹp" } }.distinct()

                    val filteredServices = allServices.filter { service ->
                        val matchCat = selectedCategory == "Tất cả" || service.category.equals(selectedCategory, ignoreCase = true)
                        val matchSearch = searchQuery.isBlank() || service.name.contains(searchQuery, ignoreCase = true)
                        matchCat && matchSearch
                    }

                    // Thanh tìm kiếm và bộ lọc nhóm danh mục
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MerchantSurface)
                            .padding(horizontal = 16.dp, vertical = 10.dp)
                    ) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = { Text("Tìm kiếm tên dịch vụ...", fontSize = 13.sp, color = MerchantTextHint) },
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = MerchantTextHint) },
                            trailingIcon = {
                                if (searchQuery.isNotEmpty()) {
                                    IconButton(onClick = { searchQuery = "" }) {
                                        Icon(Icons.Default.Close, contentDescription = "Clear", tint = MerchantTextHint)
                                    }
                                }
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MerchantPrimary,
                                unfocusedBorderColor = MerchantBorder,
                                focusedContainerColor = Color.White,
                                unfocusedContainerColor = Color.White
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Category Chips
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            categories.forEach { cat ->
                                val isSelected = cat == selectedCategory
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(20.dp))
                                        .background(if (isSelected) MerchantPrimary else MerchantCard)
                                        .border(1.dp, if (isSelected) MerchantPrimary else MerchantBorder, RoundedCornerShape(20.dp))
                                        .clickable { selectedCategory = cat }
                                        .padding(horizontal = 14.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = cat,
                                        fontSize = 12.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) Color.White else MerchantTextPrimary
                                    )
                                }
                            }
                        }
                    }

                    if (filteredServices.isEmpty()) {
                        EmptyStateView(
                            title = "Không tìm thấy dịch vụ phù hợp",
                            subtitle = if (allServices.isEmpty())
                                "Bấm nút '+' bên dưới để tạo dịch vụ đầu tiên cho salon của bạn."
                            else
                                "Thử đổi từ khóa tìm kiếm hoặc chọn danh mục khác."
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(filteredServices, key = { it.id }) { service ->
                                ServiceItemCard(
                                    service = service,
                                    onEdit = { editingService = service },
                                    onDelete = { serviceToDelete = service },
                                    onToggleActive = { updatedActive ->
                                        viewModel.updateService(
                                            service = service.copy(active = updatedActive),
                                            onSuccess = {
                                                Toast.makeText(
                                                    context,
                                                    if (updatedActive) "Đã bật kinh doanh dịch vụ" else "Đã tạm ẩn nhận khách",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            },
                                            onError = { Toast.makeText(context, it, Toast.LENGTH_SHORT).show() }
                                        )
                                    }
                                )
                            }
                        }
                    }
                }
                else -> Unit
            }
        }

        // Dialog Thêm hoặc Sửa dịch vụ
        if (isAddingNew || editingService != null) {
            val initialCategory = if (selectedCategory != "Tất cả") selectedCategory else "Nail Art"
            val initial = editingService ?: ServiceItem(
                id = "",
                shopId = shopId,
                name = "",
                category = initialCategory,
                price = 150000L,
                durationMinutes = 45,
                description = "",
                active = true,
                imageUrl = defaultPresetForCategory(initialCategory)
            )

            ServiceEditDialog(
                initialService = initial,
                isNew = isAddingNew,
                onDismiss = {
                    isAddingNew = false
                    editingService = null
                },
                onSave = { savedItem ->
                    if (isAddingNew) {
                        viewModel.addService(
                            service = savedItem,
                            onSuccess = {
                                Toast.makeText(context, "Thêm dịch vụ mới thành công!", Toast.LENGTH_SHORT).show()
                                isAddingNew = false
                            },
                            onError = { Toast.makeText(context, it, Toast.LENGTH_SHORT).show() }
                        )
                    } else {
                        viewModel.updateService(
                            service = savedItem,
                            onSuccess = {
                                Toast.makeText(context, "Cập nhật dịch vụ thành công!", Toast.LENGTH_SHORT).show()
                                editingService = null
                            },
                            onError = { Toast.makeText(context, it, Toast.LENGTH_SHORT).show() }
                        )
                    }
                }
            )
        }

        // Dialog xác nhận Xóa / Ẩn dịch vụ với cảnh báo ràng buộc đơn đặt lịch
        serviceToDelete?.let { s ->
            AlertDialog(
                onDismissRequest = { serviceToDelete = null },
                icon = {
                    Icon(
                        imageVector = Icons.Default.WarningAmber,
                        contentDescription = null,
                        tint = MerchantWarning,
                        modifier = Modifier.size(32.dp)
                    )
                },
                title = {
                    Text(
                        text = stringResource(R.string.merchant_service_delete),
                        fontWeight = FontWeight.Bold,
                        color = MerchantTextPrimary
                    )
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "Bạn có chắc chắn muốn xóa dịch vụ '${s.name}'?",
                            fontSize = 14.sp,
                            color = MerchantTextPrimary
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(MerchantWarning.copy(alpha = 0.1f))
                                .padding(10.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.merchant_service_in_use_warning),
                                fontSize = 12.sp,
                                color = MerchantTextSecondary,
                                lineHeight = 16.sp
                            )
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.deleteOrHideService(
                                serviceId = s.id,
                                shopId = shopId,
                                onDecision = { decision ->
                                    when (decision) {
                                        is ServiceRemovalPolicy.ServiceRemovalDecision.SoftDelete -> {
                                            Toast.makeText(context, decision.reason, Toast.LENGTH_LONG).show()
                                        }
                                        is ServiceRemovalPolicy.ServiceRemovalDecision.HardDelete -> {
                                            Toast.makeText(context, decision.message, Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                    serviceToDelete = null
                                },
                                onError = {
                                    Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
                                }
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MerchantError)
                    ) {
                        Text(text = "Đồng ý", fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { serviceToDelete = null }) {
                        Text(text = "Hủy", color = MerchantTextSecondary)
                    }
                }
            )
        }
    }
}

@Composable
private fun ServiceItemCard(
    service: ServiceItem,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onToggleActive: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MerchantCard),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AsyncImage(
                    model = resolveServiceImageModel(service),
                    contentDescription = service.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(68.dp)
                        .clip(RoundedCornerShape(10.dp))
                )

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = service.name,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = MerchantTextPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = Formatters.formatCurrency(service.price),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MerchantPrimary
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.AccessTime,
                                contentDescription = null,
                                tint = MerchantTextSecondary,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "${service.durationMinutes} phút",
                                fontSize = 12.sp,
                                color = MerchantTextSecondary
                            )
                        }

                        Text(text = "·", color = MerchantTextHint)

                        Text(
                            text = service.category.ifBlank { "Làm đẹp" },
                            fontSize = 11.sp,
                            color = MerchantGoldDark,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            if (service.description.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = service.description,
                    fontSize = 12.sp,
                    color = MerchantTextSecondary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 10.dp),
                thickness = 0.5.dp,
                color = MerchantDivider
            )

            // Bottom action row: Active status switch + Sửa/Xóa
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Switch(
                        checked = service.active,
                        onCheckedChange = onToggleActive,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = MerchantPrimary,
                            uncheckedThumbColor = Color.White,
                            uncheckedTrackColor = MerchantBorder
                        ),
                        modifier = Modifier.graphicsLayer {
                            scaleX = 0.8f
                            scaleY = 0.8f
                            transformOrigin = TransformOrigin(0f, 0.5f)
                        }
                    )
                    Text(
                        text = if (service.active) stringResource(R.string.merchant_service_active) else stringResource(R.string.merchant_service_inactive),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (service.active) MerchantPrimary else MerchantTextHint,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    OutlinedButton(
                        onClick = onEdit,
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier.height(34.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MerchantPrimary),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MerchantPrimary)
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "Sửa", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(34.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = stringResource(R.string.merchant_service_delete),
                            tint = MerchantError,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ServiceEditDialog(
    initialService: ServiceItem,
    isNew: Boolean,
    onDismiss: () -> Unit,
    onSave: (ServiceItem) -> Unit
) {
    val context = LocalContext.current
    var name by remember { mutableStateOf(initialService.name) }
    var priceText by remember { mutableStateOf(if (initialService.price > 0) initialService.price.toString() else "") }
    var durationText by remember { mutableStateOf(initialService.durationMinutes.toString()) }
    var category by remember { mutableStateOf(initialService.category.ifBlank { "Nail Art" }) }
    var imageUrl by remember { mutableStateOf(initialService.imageUrl) }
    var showUrlInput by remember {
        mutableStateOf(
            !initialService.imageUrl.isNullOrBlank() &&
                initialService.imageUrl.startsWith("http", ignoreCase = true)
        )
    }
    var description by remember { mutableStateOf(initialService.description) }
    var active by remember { mutableStateOf(initialService.active) }
    var errorText by remember { mutableStateOf<String?>(null) }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            try {
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (_: Exception) {
            }
            imageUrl = uri.toString()
            showUrlInput = false
        }
    }

    val popularCategories = listOf("Nail Art", "Nối Mi & Mày", "Gội Đầu Dưỡng Sinh", "Chăm Sóc Da", "Make-up", "Massage")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (isNew) stringResource(R.string.merchant_service_add) else stringResource(R.string.merchant_service_edit),
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = MerchantTextPrimary
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (errorText != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(MerchantError.copy(alpha = 0.1f))
                            .padding(8.dp)
                    ) {
                        Text(text = errorText ?: "", fontSize = 12.sp, color = MerchantError)
                    }
                }

                // Chỉnh sửa / Chọn hình ảnh dịch vụ
                Text(text = "Hình ảnh dịch vụ:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MerchantTextSecondary)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    AsyncImage(
                        model = resolveServiceImageModel(imageUrl, name.ifBlank { category }),
                        contentDescription = "Ảnh xem trước dịch vụ",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(72.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .border(1.dp, MerchantBorder, RoundedCornerShape(10.dp))
                            .clickable {
                                imagePickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            }
                    )

                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                imagePickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            border = BorderStroke(1.dp, MerchantPrimary),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = MerchantPrimary),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(34.dp)
                        ) {
                            Icon(Icons.Default.PhotoLibrary, contentDescription = null, modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = "Chọn ảnh từ máy", fontSize = 11.5.sp, fontWeight = FontWeight.SemiBold)
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            FilterChip(
                                selected = showUrlInput,
                                onClick = { showUrlInput = !showUrlInput },
                                label = { Text("Nhập link URL", fontSize = 11.sp) }
                            )
                        }
                    }
                }

                if (showUrlInput) {
                    OutlinedTextField(
                        value = if (imageUrl?.startsWith("preset:") == true) "" else (imageUrl ?: ""),
                        onValueChange = { imageUrl = it },
                        label = { Text("Link hình ảnh (URL)") },
                        placeholder = { Text("https://...") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // Ảnh mẫu có sẵn ngoại tuyến để chọn nhanh
                Text(text = "Hoặc chọn nhanh ảnh mẫu:", fontSize = 11.sp, color = MerchantTextSecondary)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SERVICE_IMAGE_PRESETS.forEach { preset ->
                        val isSelected = imageUrl == preset.key
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable {
                                    imageUrl = preset.key
                                    showUrlInput = false
                                }
                                .padding(2.dp)
                        ) {
                            AsyncImage(
                                model = preset.drawableRes,
                                contentDescription = preset.label,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .border(
                                        width = if (isSelected) 2.dp else 1.dp,
                                        color = if (isSelected) MerchantPrimary else MerchantBorder,
                                        shape = RoundedCornerShape(8.dp)
                                    )
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = preset.label,
                                fontSize = 10.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) MerchantPrimary else MerchantTextSecondary
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it; errorText = null },
                    label = { Text(stringResource(R.string.merchant_service_name)) },
                    placeholder = { Text("Ví dụ: Sơn gel Hàn Quốc cao cấp") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Nhóm dịch vụ
                Text(text = "Nhóm dịch vụ:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MerchantTextSecondary)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    popularCategories.forEach { cat ->
                        FilterChip(
                            selected = category == cat,
                            onClick = { category = cat },
                            label = { Text(cat, fontSize = 11.sp) }
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = priceText,
                        onValueChange = { priceText = it; errorText = null },
                        label = { Text(stringResource(R.string.merchant_service_price), maxLines = 1) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1.1f)
                    )

                    OutlinedTextField(
                        value = durationText,
                        onValueChange = { durationText = it; errorText = null },
                        label = { Text("Thời lượng (phút)", maxLines = 1) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(0.9f)
                    )
                }

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Mô tả dịch vụ") },
                    placeholder = { Text("Các bước thực hiện, công nghệ sử dụng, xuất xứ mỹ phẩm...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(84.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = active,
                        onCheckedChange = { active = it },
                        colors = CheckboxDefaults.colors(checkedColor = MerchantPrimary)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Mở kinh doanh ngay sau khi lưu",
                        fontSize = 13.sp,
                        color = MerchantTextPrimary
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val p = priceText.toLongOrNull()
                    val d = durationText.toIntOrNull()
                    if (name.trim().length < 3) {
                        errorText = "Tên dịch vụ phải có ít nhất 3 ký tự"
                        return@Button
                    }
                    if (p == null || p < 0L) {
                        errorText = "Giá dịch vụ không hợp lệ"
                        return@Button
                    }
                    if (d == null || d <= 0) {
                        errorText = "Thời lượng phục vụ phải lớn hơn 0 phút"
                        return@Button
                    }

                    val normalizedImageUrl = imageUrl?.trim()?.ifBlank { null }
                    val updated = initialService.copy(
                        name = name.trim(),
                        category = category.trim(),
                        price = p,
                        durationMinutes = d,
                        imageUrl = normalizedImageUrl,
                        description = description.trim(),
                        active = active
                    )
                    onSave(updated)
                },
                colors = ButtonDefaults.buttonColors(containerColor = MerchantPrimary)
            ) {
                Text(text = if (isNew) "Tạo mới" else "Lưu thay đổi", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = "Hủy", color = MerchantTextSecondary)
            }
        }
    )
}

private data class ServiceImagePreset(
    val key: String,
    val label: String,
    @DrawableRes val drawableRes: Int
)

private val SERVICE_IMAGE_PRESETS = listOf(
    ServiceImagePreset("preset:srv_nail_gel", "Sơn Gel", R.drawable.srv_nail_gel),
    ServiceImagePreset("preset:srv_nail_art", "Nail Art", R.drawable.srv_nail_art),
    ServiceImagePreset("preset:nail_french_gel", "French Gel", R.drawable.nail_french_gel),
    ServiceImagePreset("preset:nail_cateye_aurora", "Mắt Mèo", R.drawable.nail_cateye_aurora),
    ServiceImagePreset("preset:srv_eyelash", "Nối Mi", R.drawable.srv_eyelash),
    ServiceImagePreset("preset:srv_spa_facial", "Chăm Da", R.drawable.srv_spa_facial),
    ServiceImagePreset("preset:srv_spa_massage", "Massage", R.drawable.srv_spa_massage),
    ServiceImagePreset("preset:srv_hair_cut", "Cắt Tóc", R.drawable.srv_hair_cut),
    ServiceImagePreset("preset:srv_hair_wave", "Uốn Tóc", R.drawable.srv_hair_wave),
    ServiceImagePreset("preset:srv_makeup_bride", "Makeup", R.drawable.srv_makeup_bride)
)

private fun defaultPresetForCategory(category: String): String {
    return when {
        category.contains("Mi", ignoreCase = true) -> "preset:srv_eyelash"
        category.contains("Da", ignoreCase = true) -> "preset:srv_spa_facial"
        category.contains("Gội", ignoreCase = true) || category.contains("Massage", ignoreCase = true) -> "preset:srv_spa_massage"
        category.contains("Make", ignoreCase = true) -> "preset:srv_makeup_bride"
        category.contains("Tóc", ignoreCase = true) -> "preset:srv_hair_cut"
        else -> "preset:srv_nail_gel"
    }
}

private fun resolveServiceImageModel(service: ServiceItem): Any {
    return resolveServiceImageModel(service.imageUrl, service.id.ifBlank { service.name.ifBlank { service.category } })
}

private fun resolveServiceImageModel(imageUrl: String?, fallbackKey: String): Any {
    if (!imageUrl.isNullOrBlank()) {
        val preset = SERVICE_IMAGE_PRESETS.find { it.key == imageUrl }
        if (preset != null) return preset.drawableRes
        return imageUrl
    }
    return ShopImageAssets.getServiceDrawableRes(fallbackKey)
}
