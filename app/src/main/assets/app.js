/* =========================================================
   GLOW PRESTIGE - COMPREHENSIVE MARKETPLACE INTERACTION ENGINE
   Customer Multi-Service Booking • Partner 7-Day Matrix Calendar
   Admin Moderation (Salons & Reviews) • Smart Refund & Wallet Integration
========================================================= */

// Database of Services for Luxury Drawer
const beautyServicesDB = {
  nail: {
    title: '💅 Bộ Sưu Tập Nail Art Haute Couture',
    subtitle: 'Nghệ nhân tạo mẫu móng chuẩn Salon Châu Âu',
    items: [
      { name: 'Sơn Gel Hữu Cơ Pháp + Dưỡng Móng Tinh Dầu', price: '180.000đ', priceVal: 180000, durationVal: 45, rating: '4.99', tag: 'Signature' },
      { name: 'Nail Art Mắt Mèo Cực Quang & Tráng Gương', price: '280.000đ', priceVal: 280000, durationVal: 60, rating: '5.0', tag: 'Xu hướng' },
      { name: 'Đắp Móng Form Hoàng Gia & Đính Đá Swarovski', price: '450.000đ', priceVal: 450000, durationVal: 75, rating: '4.98', tag: 'Haute VIP' },
      { name: 'Chăm Sóc Móng Spa Tinh Chất Ngọc Trai', price: '220.000đ', priceVal: 220000, durationVal: 50, rating: '4.95', tag: 'Dưỡng sâu' },
    ]
  },
  spa: {
    title: '🌿 Trị Liệu Hoàng Gia & Spa Retreat',
    subtitle: 'Không gian tĩnh tại • Thảo mộc cung đình tuyển chọn',
    items: [
      { name: 'Dưỡng Sinh Hoàng Cung & Trị Liệu Cổ Vai Gáy', price: '189.000đ', priceVal: 189000, durationVal: 75, rating: '4.99', tag: 'Đặc quyền' },
      { name: 'Massage Body Đá Nóng Himalaya & Tinh Dầu Hoa Hồng', price: '380.000đ', priceVal: 380000, durationVal: 90, rating: '5.0', tag: 'Signature' },
      { name: 'Xông Hơi Đá Muối Tuyết & Tẩy Tế Bào Thảo Dược', price: '250.000đ', priceVal: 250000, durationVal: 60, rating: '4.95', tag: 'Retreat' },
    ]
  },
  skincare: {
    title: '✨ Chăm Sóc Da Y Khoa & Trẻ Hóa Thượng Lưu',
    subtitle: 'Thăm khám 1:1 cùng Bác sĩ Chuyên khoa Da liễu',
    items: [
      { name: 'Liệu Trình Phục Hồi Da Thủy Tinh Chuẩn Thụy Sĩ', price: '450.000đ', priceVal: 450000, durationVal: 75, rating: '5.0', tag: 'Haute Derma' },
      { name: 'Cấy DNA Cá Hồi Tinh Khiết & Tế Bào Tươi', price: '690.000đ', priceVal: 690000, durationVal: 60, rating: '4.99', tag: 'VIP' },
    ]
  },
  hair: {
    title: '💇 Haute Coiffure • Tạo Kiểu Tóc Thiết Kế',
    subtitle: 'Stylist Quốc tế cắt tạo dáng theo tỷ lệ vàng',
    items: [
      { name: 'Master Designer Cut & Tạo Kiểu Thượng Lưu', price: '280.000đ', priceVal: 280000, durationVal: 45, rating: '4.98', tag: 'Master' },
      { name: 'Uốn Sóng Lơi Pháp & Phục Hồi Kérastase', price: '790.000đ', priceVal: 790000, durationVal: 120, rating: '4.95', tag: 'Haute' },
    ]
  },
  makeup: {
    title: '💄 Trang Điểm Thượng Lưu & Dạ Tiệc',
    subtitle: 'Mỹ phẩm Tom Ford, Chanel, Dior chính hãng',
    items: [
      { name: 'Trang Điểm Dạ Tiệc & Thiết Kế Kiểu Tóc', price: '390.000đ', priceVal: 390000, durationVal: 60, rating: '4.98', tag: 'Prestige' },
      { name: 'Makeup Cô Dâu Hoàng Gia Luxury Bridal', price: '1.450.000đ', priceVal: 1450000, durationVal: 120, rating: '5.0', tag: 'VIP Bridal' },
    ]
  },
  herbal: {
    title: '🍃 Dưỡng Sinh Cung Đình & Thảo Mộc',
    subtitle: 'Bài thuốc Đông Y ngàn năm thanh lọc cơ thể',
    items: [
      { name: 'Ngâm Chân Bồn Gỗ Pơ Mu & Bấm Huyệt Thông Kinh Lạc', price: '180.000đ', priceVal: 180000, durationVal: 45, rating: '4.96', tag: 'An thần' },
    ]
  }
};

// Global App State
let currentRole = 'customer'; // 'customer' | 'partner' | 'admin'
let userWalletBalance = 3450000;
let holdTimerInterval = null;
let currentRatingScore = 5;
let isEarlyCancelSelected = true;

// Multi-service selection items in Booking Modal
let selectedBookingServices = [
  { name: 'Sơn Gel Hữu Cơ Pháp + Dưỡng Móng', price: 180000, duration: 45 }
];

// Partner 7-Day Matrix Calendar Database
const weekMatrixDB = [
  // Day 0: T2 (Hôm nay)
  [
    { time: '09:00 - 10:15', status: 'booked', customer: 'Hà An (Đã cọc 50k)' },
    { time: '10:30 - 11:45', status: 'locked', customer: '' },
    { time: '13:00 - 14:15', status: 'available', customer: '' },
    { time: '15:30 - 16:45', status: 'booked', customer: 'Khánh Linh (Đã cọc 50k)' },
    { time: '17:00 - 18:15', status: 'available', customer: '' },
    { time: '18:30 - 19:45', status: 'available', customer: '' }
  ],
  // Day 1: T3
  [
    { time: '09:00 - 10:15', status: 'available', customer: '' },
    { time: '10:30 - 11:45', status: 'booked', customer: 'Mai Phương (Đã cọc 100k)' },
    { time: '13:00 - 14:15', status: 'available', customer: '' },
    { time: '15:30 - 16:45', status: 'available', customer: '' },
    { time: '17:00 - 18:15', status: 'locked', customer: '' },
    { time: '18:30 - 19:45', status: 'available', customer: '' }
  ],
  // Day 2: T4
  [
    { time: '09:00 - 10:15', status: 'available', customer: '' },
    { time: '10:30 - 11:45', status: 'available', customer: '' },
    { time: '13:00 - 14:15', status: 'booked', customer: 'Thu Hương (Đã cọc 50k)' },
    { time: '15:30 - 16:45', status: 'available', customer: '' },
    { time: '17:00 - 18:15', status: 'available', customer: '' },
    { time: '18:30 - 19:45', status: 'locked', customer: '' }
  ],
  // Day 3: T5
  [
    { time: '09:00 - 10:15', status: 'available', customer: '' },
    { time: '10:30 - 11:45', status: 'available', customer: '' },
    { time: '13:00 - 14:15', status: 'available', customer: '' },
    { time: '15:30 - 16:45', status: 'booked', customer: 'Bảo Ngọc' },
    { time: '17:00 - 18:15', status: 'available', customer: '' },
    { time: '18:30 - 19:45', status: 'available', customer: '' }
  ],
  // Day 4: T6
  [
    { time: '09:00 - 10:15', status: 'locked', customer: '' },
    { time: '10:30 - 11:45', status: 'available', customer: '' },
    { time: '13:00 - 14:15', status: 'booked', customer: 'Vân Trang' },
    { time: '15:30 - 16:45', status: 'booked', customer: 'Yến Nhi' },
    { time: '17:00 - 18:15', status: 'available', customer: '' },
    { time: '18:30 - 19:45', status: 'available', customer: '' }
  ],
  // Day 5: T7 (Cao điểm)
  [
    { time: '09:00 - 10:15', status: 'booked', customer: 'Hoàng Yến' },
    { time: '10:30 - 11:45', status: 'booked', customer: 'Diệu Anh' },
    { time: '13:00 - 14:15', status: 'booked', customer: 'Thanh Hằng' },
    { time: '15:30 - 16:45', status: 'available', customer: '' },
    { time: '17:00 - 18:15', status: 'booked', customer: 'Minh Thư' },
    { time: '18:30 - 19:45', status: 'locked', customer: '' }
  ],
  // Day 6: CN
  [
    { time: '09:00 - 10:15', status: 'available', customer: '' },
    { time: '10:30 - 11:45', status: 'booked', customer: 'Kim Oanh' },
    { time: '13:00 - 14:15', status: 'available', customer: '' },
    { time: '15:30 - 16:45', status: 'booked', customer: 'Thảo My' },
    { time: '17:00 - 18:15', status: 'available', customer: '' },
    { time: '18:30 - 19:45', status: 'available', customer: '' }
  ]
];

let currentSelectedMatrixDay = 0;

// Initialize on DOM ready
document.addEventListener('DOMContentLoaded', () => {
  updateTime();
  setInterval(updateTime, 1000);
  initSearchTrigger();
  renderMatrixSlots(0);
  updateWalletDisplay();
});

// Update Realtime clock for iOS status bar
function updateTime() {
  const timeEl = document.getElementById('currentTime');
  if (!timeEl) return;
  const now = new Date();
  const hours = String(now.getHours()).padStart(2, '0');
  const minutes = String(now.getMinutes()).padStart(2, '0');
  timeEl.innerText = `${hours}:${minutes}`;
}

// Toast Notifications
function showToast(message, icon = 'fa-crown') {
  const container = document.getElementById('toastContainer');
  if (!container) return;
  const toast = document.createElement('div');
  toast.className = 'toast-msg';
  toast.innerHTML = `<i class="fa-solid ${icon}"></i> <span>${message}</span>`;
  container.appendChild(toast);
  setTimeout(() => {
    toast.style.opacity = '0';
    toast.style.transform = 'translateY(-10px)';
    toast.style.transition = 'all 0.3s ease';
    setTimeout(() => toast.remove(), 300);
  }, 2500);
}

function updateWalletDisplay() {
  const el = document.getElementById('walletBalanceDisplay');
  if (el) el.innerText = userWalletBalance.toLocaleString('vi-VN') + ' đ';
}

/* =========================================================
   1. ROLE SWITCHER: CUSTOMER / PARTNER / ADMIN
========================================================= */
function switchRole(roleName) {
  currentRole = roleName;
  const custView = document.getElementById('customerView');
  const partView = document.getElementById('partnerView');
  const adminView = document.getElementById('adminView');
  const actView = document.getElementById('activityView');
  const headerSearch = document.getElementById('customerHeaderSearch');
  const headerSubtitle = document.getElementById('headerSubtitle');
  const roleLabel = document.getElementById('bottomNavRoleLabel');
  const navHomeLabel = document.getElementById('navHomeLabel');
  const navActivityLabel = document.getElementById('navActivityLabel');
  const navPayLabel = document.getElementById('navPayLabel');
  const navActivityIcon = document.getElementById('navActivityIcon');
  const navPayIcon = document.getElementById('navPayIcon');
  const navActivityBadge = document.getElementById('navActivityBadge');

  // Update pills
  document.getElementById('pillCustomer')?.classList.toggle('active', roleName === 'customer');
  document.getElementById('pillPartner')?.classList.toggle('active', roleName === 'partner');
  document.getElementById('pillAdmin')?.classList.toggle('active', roleName === 'admin');

  // Reset Bottom Nav highlight
  document.querySelectorAll('.nav-item').forEach(i => i.classList.remove('active'));

  if (roleName === 'customer') {
    if (custView) custView.style.display = 'block';
    if (partView) partView.style.display = 'none';
    if (adminView) adminView.style.display = 'none';
    if (actView) actView.style.display = 'none';
    if (headerSearch) headerSearch.style.display = 'flex';
    if (headerSubtitle) headerSubtitle.innerText = 'Khám phá vẻ đẹp của bạn';
    if (navHomeLabel) navHomeLabel.innerText = 'Khám phá';
    if (navActivityLabel) navActivityLabel.innerText = 'Lịch';
    if (navPayLabel) navPayLabel.innerText = 'Tin nhắn';
    if (roleLabel) roleLabel.innerText = 'Tôi';
    if (navActivityIcon) navActivityIcon.className = 'fa-regular fa-calendar-days';
    if (navPayIcon) navPayIcon.className = 'fa-regular fa-comment-dots';
    if (navActivityBadge) navActivityBadge.style.display = 'inline-block';
    document.getElementById('navHome')?.classList.add('active');
    showToast('Đang ở: Giao diện Khách Hàng Thượng Lưu', 'fa-user');
  } else if (roleName === 'partner') {
    if (custView) custView.style.display = 'none';
    if (partView) partView.style.display = 'block';
    if (adminView) adminView.style.display = 'none';
    if (actView) actView.style.display = 'none';
    if (headerSearch) headerSearch.style.display = 'none';
    if (headerSubtitle) headerSubtitle.innerText = 'Khu vực Đối tác Salon';
    if (navHomeLabel) navHomeLabel.innerText = 'Trang chủ';
    if (navActivityLabel) navActivityLabel.innerText = 'Lịch';
    if (navPayLabel) navPayLabel.innerText = 'Tin nhắn';
    if (roleLabel) roleLabel.innerText = 'Tôi';
    if (navActivityIcon) navActivityIcon.className = 'fa-regular fa-calendar-days';
    if (navPayIcon) navPayIcon.className = 'fa-regular fa-comment-dots';
    if (navActivityBadge) navActivityBadge.style.display = 'inline-block';
    document.getElementById('navHome')?.classList.add('active');
    showToast('Đang ở: Khu vực Đối tác Salon • L\'Amour Luxury', 'fa-store');
  } else if (roleName === 'admin') {
    if (custView) custView.style.display = 'none';
    if (partView) partView.style.display = 'none';
    if (adminView) adminView.style.display = 'block';
    if (actView) actView.style.display = 'none';
    if (headerSearch) headerSearch.style.display = 'none';
    if (headerSubtitle) headerSubtitle.innerText = 'Quản trị viên (Admin)';
    // 4 Fixed bottom tabs per Section 4
    if (navHomeLabel) navHomeLabel.innerText = 'Trang chủ';
    if (navActivityLabel) navActivityLabel.innerText = 'Cửa hàng';
    if (navPayLabel) navPayLabel.innerText = 'Khách hàng';
    if (roleLabel) roleLabel.innerText = 'Thêm';
    if (navActivityIcon) navActivityIcon.className = 'fa-solid fa-store';
    if (navPayIcon) navPayIcon.className = 'fa-solid fa-users';
    if (navActivityBadge) navActivityBadge.style.display = 'none';
    switchAdminSection('home');
    document.getElementById('navHome')?.classList.add('active');
    showToast('Đang ở: Kênh Quản Trị Hệ Thống & Kiểm Duyệt', 'fa-shield-halved');
  }
}

let salonIsActive = true;
function toggleSalonStatus() {
  salonIsActive = !salonIsActive;
  const statusText = document.getElementById('partnerStatusText');
  if (statusText) {
    statusText.innerText = salonIsActive ? 'Đang hoạt động' : 'Tạm nghỉ nhận khách';
  }
  showToast(
    salonIsActive ? 'Trạng thái gian hàng: Đang hoạt động nhận khách' : 'Đã tạm ngưng nhận khách mới hôm nay',
    salonIsActive ? 'fa-circle-check' : 'fa-pause'
  );
}

function openReviewsModal() {
  showToast('⭐ Điểm trung bình 4.9/5 (1.8k+ đánh giá từ khách hàng đã trải nghiệm)', 'fa-star');
}

function togglePartnerTools() {
  const content = document.getElementById('partnerToolsContent');
  const chevron = document.getElementById('toolsChevron');
  if (!content) return;
  const isHidden = content.style.display === 'none';
  content.style.display = isHidden ? 'block' : 'none';
  if (chevron) {
    chevron.style.transform = isHidden ? 'rotate(180deg)' : 'rotate(0deg)';
  }
}

function openRoleHubModal() {
  if (currentRole === 'admin') {
    openAdminMoreModal();
    return;
  }
  // Cyclical quick toggle when tapping bottom nav "Tôi"
  if (currentRole === 'customer') switchRole('partner');
  else if (currentRole === 'partner') switchRole('admin');
  else switchRole('customer');
}

/* =========================================================
   2. BOTTOM NAVIGATION & ACTIVITY HUB
========================================================= */
function switchNavTab(tabKey) {
  const custView = document.getElementById('customerView');
  const partView = document.getElementById('partnerView');
  const adminView = document.getElementById('adminView');
  const actView = document.getElementById('activityView');
  const headerSearch = document.getElementById('customerHeaderSearch');

  document.querySelectorAll('.nav-item').forEach(i => i.classList.remove('active'));

  // ADMIN ROLE BOTTOM NAVIGATION
  if (currentRole === 'admin') {
    if (tabKey === 'home') {
      document.getElementById('navHome')?.classList.add('active');
      switchAdminSection('home');
    } else if (tabKey === 'activity') {
      document.getElementById('navActivity')?.classList.add('active');
      switchAdminSection('shops');
    } else if (tabKey === 'payment') {
      document.getElementById('navPay')?.classList.add('active');
      switchAdminSection('customers');
    } else if (tabKey === 'roleHub' || tabKey === 'more') {
      document.getElementById('navRoleHub')?.classList.add('active');
      openAdminMoreModal();
    }
    return;
  }

  // CUSTOMER & PARTNER NAVIGATION
  if (tabKey === 'home') {
    document.getElementById('navHome')?.classList.add('active');
    if (currentRole === 'customer') {
      if (custView) custView.style.display = 'block';
      if (partView) partView.style.display = 'none';
      if (adminView) adminView.style.display = 'none';
      if (actView) actView.style.display = 'none';
      if (headerSearch) headerSearch.style.display = 'flex';
    } else {
      if (custView) custView.style.display = 'none';
      if (partView) partView.style.display = 'block';
      if (adminView) adminView.style.display = 'none';
      if (actView) actView.style.display = 'none';
      if (headerSearch) headerSearch.style.display = 'none';
    }
  } else if (tabKey === 'activity') {
    document.getElementById('navActivity')?.classList.add('active');
    if (custView) custView.style.display = 'none';
    if (partView) partView.style.display = 'none';
    if (adminView) adminView.style.display = 'none';
    if (actView) actView.style.display = 'block';
    if (headerSearch) headerSearch.style.display = 'none';
    switchMsgPane('activity');
  } else if (tabKey === 'payment' || tabKey === 'messages') {
    document.getElementById('navPay')?.classList.add('active');
    if (custView) custView.style.display = 'none';
    if (partView) partView.style.display = 'none';
    if (adminView) adminView.style.display = 'none';
    if (actView) actView.style.display = 'block';
    if (headerSearch) headerSearch.style.display = 'none';
    switchMsgPane('messages');
  }
}

function openActivityTab() {
  switchNavTab('activity');
}

/* =========================================================
   3. MULTI-SERVICE SELECTION & BOOKING CALCULATION
========================================================= */
let currentBookingRequiresDeposit = true;
let selectedDepositTierAmount = 50000;

function openBookingModal(serviceName = 'L’Amour Haute Beauty Lounge', defaultPrice = 180000, defaultDur = 45, requiresDeposit = true) {
  closeServiceDrawer();
  const modal = document.getElementById('bookingModal');
  const title = document.getElementById('bookingServiceTitle');
  if (title) title.innerText = serviceName;

  // Check if salon is An Yên (free deposit) or specified
  if (serviceName.toLowerCase().includes('yên') || serviceName.toLowerCase().includes('dưỡng sinh') || !requiresDeposit) {
    currentBookingRequiresDeposit = false;
  } else {
    currentBookingRequiresDeposit = true;
  }

  // Update UI for deposit vs no deposit
  const tiersRow = document.getElementById('depositTiersRow');
  const noDepAlert = document.getElementById('noDepositAlert');
  const depBadge = document.getElementById('depositShopBadge');
  const payGroup = document.getElementById('paymentGatewayGroup');

  if (currentBookingRequiresDeposit) {
    if (tiersRow) tiersRow.style.display = 'flex';
    if (noDepAlert) noDepAlert.style.display = 'none';
    if (depBadge) { depBadge.innerText = 'Shop yêu cầu cọc'; depBadge.className = 'badge-mini'; }
    if (payGroup) payGroup.style.display = 'block';
  } else {
    if (tiersRow) tiersRow.style.display = 'none';
    if (noDepAlert) noDepAlert.style.display = 'flex';
    if (depBadge) { depBadge.innerText = 'Shop miễn cọc (0đ)'; depBadge.className = 'badge-mini highlight'; }
    if (payGroup) payGroup.style.display = 'none';
  }
  
  // Initialize services selection
  selectedBookingServices = [{ name: 'Sơn Gel Hữu Cơ Pháp + Dưỡng Móng', price: defaultPrice, duration: defaultDur }];
  recalculateBookingCart();
  startSlotHoldTimer();
  if (modal) modal.classList.add('active');
}

function closeBookingModal() {
  const modal = document.getElementById('bookingModal');
  if (modal) modal.classList.remove('active');
  if (holdTimerInterval) clearInterval(holdTimerInterval);
}

function toggleMultiService(cardEl, priceVal, durationVal) {
  const checkbox = cardEl.querySelector('input[type="checkbox"]');
  const isSelected = cardEl.classList.contains('selected');
  const sName = cardEl.querySelector('.m-serv-info strong').innerText;

  if (isSelected) {
    if (selectedBookingServices.length <= 1) {
      showToast('Cần giữ ít nhất 1 dịch vụ để tiếp tục đặt lịch!', 'fa-circle-exclamation');
      return;
    }
    cardEl.classList.remove('selected');
    if (checkbox) checkbox.checked = false;
    selectedBookingServices = selectedBookingServices.filter(s => s.name !== sName);
  } else {
    cardEl.classList.add('selected');
    if (checkbox) checkbox.checked = true;
    selectedBookingServices.push({ name: sName, price: priceVal, duration: durationVal });
  }

  recalculateBookingCart();
}

function recalculateBookingCart() {
  const totalDuration = selectedBookingServices.reduce((acc, s) => acc + s.duration, 0);
  const totalPrice = selectedBookingServices.reduce((acc, s) => acc + s.price, 0);
  const depositPrice = currentBookingRequiresDeposit ? selectedDepositTierAmount : 0;
  const remainPrice = Math.max(0, totalPrice - depositPrice);

  const durEl = document.getElementById('multiTotalDuration');
  const subEl = document.getElementById('subtotalPrice');
  const depEl = document.getElementById('depositPrice');
  const finEl = document.getElementById('finalPrice');
  const btnEl = document.getElementById('btnSubmitDeposit');

  if (durEl) durEl.innerText = `${totalDuration} phút (${selectedBookingServices.length} dịch vụ)`;
  if (subEl) subEl.innerText = totalPrice.toLocaleString('vi-VN') + 'đ';
  if (depEl) depEl.innerText = currentBookingRequiresDeposit ? depositPrice.toLocaleString('vi-VN') + 'đ' : '0đ (Miễn cọc)';
  if (finEl) finEl.innerText = currentBookingRequiresDeposit ? remainPrice.toLocaleString('vi-VN') + 'đ' : totalPrice.toLocaleString('vi-VN') + 'đ';

  if (btnEl) {
    if (currentBookingRequiresDeposit) {
      btnEl.innerHTML = `<i class="fa-solid fa-shield-check"></i> Xác Nhận Đặt Cọc ${depositPrice.toLocaleString('vi-VN')}đ & Nhận Vé QR`;
    } else {
      btnEl.innerHTML = `<i class="fa-solid fa-circle-check"></i> Xác Nhận Giữ Chỗ Miễn Phí (0đ) & Nhận Vé QR`;
    }
  }
}

function selectDepositTier(amount, btnEl) {
  selectedDepositTierAmount = amount;
  document.querySelectorAll('.tier-pill-btn').forEach(b => b.classList.remove('active'));
  if (btnEl) btnEl.classList.add('active');
  recalculateBookingCart();
}

// 10-Minute Hold Slot Timer
function startSlotHoldTimer() {
  if (holdTimerInterval) clearInterval(holdTimerInterval);
  let timeLeft = 10 * 60 - 1; // 09:59
  const timerEl = document.getElementById('slotHoldTimer');

  holdTimerInterval = setInterval(() => {
    if (timeLeft <= 0) {
      clearInterval(holdTimerInterval);
      showToast('Hết thời gian tạm giữ khung giờ. Vui lòng chọn lại!', 'fa-clock');
      closeBookingModal();
      return;
    }
    const mins = Math.floor(timeLeft / 60);
    const secs = timeLeft % 60;
    if (timerEl) timerEl.innerText = `${String(mins).padStart(2, '0')}:${String(secs).padStart(2, '0')}`;
    timeLeft--;
  }, 1000);
}

function selectChip(el) {
  document.querySelectorAll('.date-chip').forEach(c => c.classList.remove('selected'));
  el.classList.add('selected');
}

function selectSlot(el) {
  document.querySelectorAll('.time-slot').forEach(s => s.classList.remove('selected'));
  el.classList.add('selected');
}

function selectStylist(el) {
  document.querySelectorAll('.stylist-chip').forEach(s => s.classList.remove('selected'));
  el.classList.add('selected');
}

function selectPayMethod(el) {
  document.querySelectorAll('.pay-method-opt').forEach(m => m.classList.remove('selected'));
  el.classList.add('selected');
}

// Confirm Booking with Customer Info Form
function confirmBookingWithDeposit() {
  const custName = document.getElementById('custNameInput')?.value || 'Khánh Linh';
  const custPhone = document.getElementById('custPhoneInput')?.value || '0903 123 456';
  const custNote = document.getElementById('custNoteInput')?.value || '';

  const totalPrice = selectedBookingServices.reduce((acc, s) => acc + s.price, 0);
  const depositPrice = currentBookingRequiresDeposit ? selectedDepositTierAmount : 0;
  const names = selectedBookingServices.map(s => s.name).join(' + ');

  closeBookingModal();
  if (currentBookingRequiresDeposit) {
    showToast(`🎉 Đặt cọc ${depositPrice.toLocaleString('vi-VN')}đ thành công! Đã gửi thông tin về Salon cho ${custName}`, 'fa-shield-check');
  } else {
    showToast(`🎉 Đặt lịch giữ chỗ miễn phí (0đ) thành công cho ${custName}! Đã gửi thông tin về Salon`, 'fa-circle-check');
  }

  // Update Activity Ticket info with customer info
  const servNameEl = document.getElementById('ticketServiceName');
  const totEl = document.getElementById('ticketTotalPrice');
  const depEl = document.getElementById('ticketDepositPaid');
  const remEl = document.getElementById('ticketRemainPrice');
  const statPill = document.getElementById('ticketStatusPill');

  if (servNameEl) servNameEl.innerHTML = `<strong>${names}</strong><br><span style="font-size:11px; color:var(--text-muted); font-weight:600;">Khách: ${custName} • ${custPhone}</span>`;
  if (totEl) totEl.innerText = totalPrice.toLocaleString('vi-VN') + 'đ';
  if (depEl) depEl.innerText = currentBookingRequiresDeposit ? `-${depositPrice.toLocaleString('vi-VN')}đ` : '0đ (Miễn cọc)';
  if (remEl) remEl.innerText = (currentBookingRequiresDeposit ? Math.max(0, totalPrice - depositPrice) : totalPrice).toLocaleString('vi-VN') + 'đ';
  if (statPill) {
    if (currentBookingRequiresDeposit) {
      statPill.innerHTML = '<i class="fa-solid fa-circle-check"></i> ĐÃ XÁC NHẬN & ĐẶT CỌC';
      statPill.style.background = '#dcfce7';
      statPill.style.color = '#15803d';
    } else {
      statPill.innerHTML = '<i class="fa-solid fa-circle-check"></i> ĐÃ XÁC NHẬN GIỮ CHỖ (0Đ)';
      statPill.style.background = '#dbeafe';
      statPill.style.color = '#1d4ed8';
    }
  }

  setTimeout(() => {
    openActivityTab();
  }, 700);
}

/* =========================================================
   4. CANCELLATION & AUTOMATIC REFUND ENGINE
========================================================= */
function openCancellationModal() {
  const modal = document.getElementById('cancellationModal');
  if (modal) modal.classList.add('active');
}

function closeCancellationModal() {
  const modal = document.getElementById('cancellationModal');
  if (modal) modal.classList.remove('active');
}

function selectCancelScenario(el, isEarly) {
  document.querySelectorAll('.scenario-opt').forEach(opt => opt.classList.remove('selected'));
  el.classList.add('selected');
  isEarlyCancelSelected = isEarly;
}

function executeCancellation() {
  closeCancellationModal();
  const statPill = document.getElementById('ticketStatusPill');
  const actionsRow = document.getElementById('ticketActionsRow');

  if (isEarlyCancelSelected) {
    // Early cancellation: 100% refund to wallet (50.000đ)
    userWalletBalance += 50000;
    updateWalletDisplay();
    if (statPill) {
      statPill.innerHTML = '<i class="fa-solid fa-rotate-left"></i> ĐÃ HỦY • ĐÃ HOÀN CỌC 100% (50k VÀO VÍ)';
      statPill.style.background = '#e0f2fe';
      statPill.style.color = '#0369a1';
    }
    showToast('🎉 Hủy thành công! Tiền cọc 50.000đ đã hoàn về Ví GlowPrivé của bạn', 'fa-wallet');
  } else {
    // Late cancellation: no refund
    if (statPill) {
      statPill.innerHTML = '<i class="fa-solid fa-triangle-exclamation"></i> ĐÃ HỦY • KHÔNG HOÀN CỌC (HỦY SÁT GIỜ)';
      statPill.style.background = '#fee2e2';
      statPill.style.color = '#b91c1c';
    }
    showToast('Đã hủy lịch. Tiền cọc không được hoàn lại do hủy sát giờ hẹn (< 2h).', 'fa-circle-exclamation');
  }

  if (actionsRow) {
    actionsRow.innerHTML = '<button class="ticket-btn secondary" style="width:100%" onclick="openBookingModal()"><i class="fa-solid fa-plus"></i> Đặt Lịch Hẹn Mới</button>';
  }
}

/* =========================================================
   5. PARTNER 7-DAY MATRIX CALENDAR LOGIC
========================================================= */
function switchPartnerTab(tabKey, btnEl) {
  document.querySelectorAll('.partner-tab-btn').forEach(b => b.classList.remove('active'));
  if (btnEl) btnEl.classList.add('active');

  const panes = {
    calendar: document.getElementById('paneCalendar'),
    services: document.getElementById('paneServices'),
    plans: document.getElementById('panePlans')
  };

  Object.keys(panes).forEach(k => {
    if (panes[k]) panes[k].style.display = (k === tabKey) ? 'block' : 'none';
  });
}

function switchMatrixDay(dayIdx, btnEl) {
  currentSelectedMatrixDay = dayIdx;
  document.querySelectorAll('.day-tab-btn').forEach(b => b.classList.remove('active'));
  if (btnEl) btnEl.classList.add('active');
  renderMatrixSlots(dayIdx);
}

function renderMatrixSlots(dayIdx) {
  const container = document.getElementById('matrixSlotContainer');
  if (!container) return;
  const slots = weekMatrixDB[dayIdx] || [];

  container.innerHTML = slots.map((slot, sIdx) => {
    if (slot.status === 'booked') {
      return `
        <div class="partner-slot-item booked" onclick="showToast('Khung giờ có khách: ${slot.customer}')">
          <div class="slot-time">${slot.time}</div>
          <div class="slot-status"><i class="fa-solid fa-user-check"></i> ${slot.customer}</div>
          <span class="slot-badge deposit-paid">Đã cọc 30%</span>
        </div>
      `;
    } else if (slot.status === 'locked') {
      return `
        <div class="partner-slot-item locked" onclick="toggleMatrixSlotItem(${dayIdx}, ${sIdx})">
          <div class="slot-time">${slot.time}</div>
          <div class="slot-status"><i class="fa-solid fa-lock"></i> Đang KHÓA (Bận)</div>
          <button class="btn-slot-toggle">Mở giờ này</button>
        </div>
      `;
    } else {
      return `
        <div class="partner-slot-item available" onclick="toggleMatrixSlotItem(${dayIdx}, ${sIdx})">
          <div class="slot-time">${slot.time}</div>
          <div class="slot-status"><i class="fa-solid fa-circle-check"></i> Đang mở nhận khách</div>
          <button class="btn-slot-toggle">Khóa giờ</button>
        </div>
      `;
    }
  }).join('');
}

function toggleMatrixSlotItem(dayIdx, sIdx) {
  const slot = weekMatrixDB[dayIdx][sIdx];
  if (slot.status === 'available') {
    slot.status = 'locked';
    showToast(`Đã KHÓA khung giờ ${slot.time}`, 'fa-lock');
  } else if (slot.status === 'locked') {
    slot.status = 'available';
    showToast(`Đã MỞ lại khung giờ ${slot.time} cho khách đặt`, 'fa-lock-open');
  }
  renderMatrixSlots(dayIdx);
}

/* =========================================================
   6. ADMIN PORTAL LOGIC (SALONS & REVIEWS)
========================================================= */
function switchAdminTab(tabKey, btnEl) {
  document.querySelectorAll('#adminView .partner-tab-btn').forEach(b => b.classList.remove('active'));
  if (btnEl) btnEl.classList.add('active');

  const pApprove = document.getElementById('paneAdminApprove');
  const pReviews = document.getElementById('paneAdminReviews');

  if (pApprove) pApprove.style.display = (tabKey === 'approve') ? 'block' : 'none';
  if (pReviews) pReviews.style.display = (tabKey === 'reviews') ? 'block' : 'none';
}

function adminApproveSalon(id, salonName) {
  const badge = document.getElementById(`statusBadge-${id}`);
  const card = document.getElementById(`salonReq-${id}`);
  if (badge) {
    badge.className = 'admin-status-badge active';
    badge.innerText = 'ĐÃ KÍCH HOẠT';
  }
  if (card) {
    const actions = card.querySelector('.admin-actions-row');
    if (actions) actions.innerHTML = '<span style="font-size:11px; font-weight:700; color:#15803d;"><i class="fa-solid fa-circle-check"></i> Hồ sơ hợp lệ • Đã hiển thị trên Marketplace</span>';
  }
  // Decrement pending counter
  const pendEl = document.getElementById('adminPendingSalons');
  const actEl = document.getElementById('adminActiveSalons');
  if (pendEl && actEl) {
    let p = parseInt(pendEl.innerText) || 1;
    let a = parseInt(actEl.innerText) || 18;
    pendEl.innerText = Math.max(0, p - 1);
    actEl.innerText = a + 1;
  }
  showToast(`✅ Đã phê duyệt & kích hoạt đối tác: ${salonName}!`, 'fa-check');
}

function adminRejectSalon(id, salonName) {
  const badge = document.getElementById(`statusBadge-${id}`);
  const card = document.getElementById(`salonReq-${id}`);
  if (badge) {
    badge.className = 'admin-status-badge rejected';
    badge.innerText = 'TỪ CHỐI';
  }
  if (card) {
    const actions = card.querySelector('.admin-actions-row');
    if (actions) actions.innerHTML = '<span style="font-size:11px; font-weight:700; color:#b91c1c;"><i class="fa-solid fa-circle-xmark"></i> Hồ sơ bị từ chối (Thiếu giấy tờ)</span>';
  }
  const pendEl = document.getElementById('adminPendingSalons');
  if (pendEl) {
    let p = parseInt(pendEl.innerText) || 1;
    pendEl.innerText = Math.max(0, p - 1);
  }
  showToast(`Đã từ chối hồ sơ: ${salonName}`, 'fa-xmark');
}

function adminToggleReview(id, btnEl) {
  const item = document.getElementById(`admReview-${id}`);
  if (!item) return;
  const isHidden = item.getAttribute('data-hidden') === 'true';

  if (!isHidden) {
    item.setAttribute('data-hidden', 'true');
    item.style.opacity = '0.4';
    btnEl.innerHTML = '<i class="fa-solid fa-eye"></i> Hiện lại';
    showToast('Đã ẨN đánh giá vi phạm khỏi Marketplace', 'fa-eye-slash');
  } else {
    item.removeAttribute('data-hidden');
    item.style.opacity = '1';
    btnEl.innerHTML = '<i class="fa-solid fa-eye-slash"></i> Ẩn đánh giá';
    showToast('Đã KHÔI PHỤC hiển thị đánh giá', 'fa-eye');
  }
}

/* =========================================================
   7. POST-SERVICE RATING & FEEDBACK MODAL
========================================================= */
function openReviewModal(salonName = 'An Yên Thảo Dược & Luxury Retreat') {
  const modal = document.getElementById('reviewModal');
  const title = document.getElementById('reviewSalonTitle');
  if (title) title.innerText = 'Đánh giá ' + salonName;
  if (modal) modal.classList.add('active');
}

function closeReviewModal() {
  const modal = document.getElementById('reviewModal');
  if (modal) modal.classList.remove('active');
}

function setStarRating(score) {
  currentRatingScore = score;
  const stars = document.querySelectorAll('.star-rate');
  const descEl = document.getElementById('starRatingDesc');
  const descs = {
    1: 'Chưa hài lòng • 1/5 sao',
    2: 'Tạm ổn • 2/5 sao',
    3: 'Hài lòng • 3/5 sao',
    4: 'Rất tốt • 4/5 sao',
    5: 'Tuyệt vời • 5/5 sao'
  };

  stars.forEach((star, idx) => {
    star.classList.toggle('active', idx < score);
  });

  if (descEl) descEl.innerText = descs[score] || `${score}/5 sao`;
}

function toggleReviewTag(btn) {
  btn.classList.toggle('active');
}

function submitReviewFeedback() {
  closeReviewModal();
  userWalletBalance += 10000; // 100 GlowXu
  updateWalletDisplay();
  showToast('🎉 Cảm ơn bạn đã gửi đánh giá! Bạn đã nhận được +100 GlowXu thưởng vào ví', 'fa-gem');
}

/* =========================================================
   8. SERVICE DETAIL DRAWER & FILTERING
========================================================= */
function openServiceDetail(serviceKey) {
  const data = beautyServicesDB[serviceKey] || beautyServicesDB.nail;
  const drawer = document.getElementById('serviceDrawer');
  const title = document.getElementById('serviceCategoryTitle');
  const sub = document.getElementById('serviceCategorySubtitle');
  const list = document.getElementById('serviceDrawerList');

  if (title) title.innerText = data.title;
  if (sub) sub.innerText = data.subtitle;

  if (list) {
    list.innerHTML = data.items.map(item => `
      <div class="service-opt" onclick="openBookingModal('${item.name}', ${item.priceVal}, ${item.durationVal})">
        <div style="flex:1">
          <div style="display:flex; align-items:center; gap:6px; margin-bottom:2px;">
            <strong style="font-size:12.5px; color:var(--text-noir);">${item.name}</strong>
            <span style="background:#fdf2ef; color:var(--dusty-merlot); border:0.5px solid var(--pink-primary); font-size:9px; font-weight:700; padding:1px 5px; border-radius:4px;">${item.tag}</span>
          </div>
          <div style="font-size:10.5px; color:var(--text-muted);">
            <i class="fa-regular fa-clock"></i> ${item.durationVal} phút • <i class="fa-solid fa-star star-filled"></i> ${item.rating} • <span style="color:var(--dusty-merlot); font-weight:700;">Cọc 30%: ${Math.round(item.priceVal*0.3/1000)}k</span>
          </div>
        </div>
        <div style="text-align:right">
          <div style="font-size:13px; font-weight:800; color:var(--dusty-merlot);">${item.price}</div>
          <button style="margin-top:4px; padding:3px 8px; font-size:10px; font-weight:700; background:var(--dusty-merlot); color:var(--warm-white); border:1px solid var(--pink-primary); border-radius:6px; cursor:pointer;">Đặt cọc</button>
        </div>
      </div>
    `).join('');
  }

  if (drawer) drawer.classList.add('active');
}

function closeServiceDrawer() {
  const drawer = document.getElementById('serviceDrawer');
  if (drawer) drawer.classList.remove('active');
}

function filterSalons(category, btnElement) {
  document.querySelectorAll('.pill-btn').forEach(b => b.classList.remove('active'));
  if (btnElement) btnElement.classList.add('active');

  document.querySelectorAll('.salon-card').forEach(card => {
    const cardCat = card.getAttribute('data-category');
    card.style.display = (category === 'all' || cardCat === category) ? 'block' : 'none';
  });
}

function toggleFavorite(event, button) {
  event.stopPropagation();
  const icon = button.querySelector('i');
  if (icon.classList.contains('fa-regular')) {
    icon.className = 'fa-solid fa-heart active';
    showToast('Đã lưu Salon vào Danh mục Yêu thích Thượng lưu', 'fa-heart');
  } else {
    icon.className = 'fa-regular fa-heart';
    showToast('Đã bỏ lưu khỏi danh mục yêu thích');
  }
}

function openSalonDetail(id) {
  const salonNames = {
    1: 'L’Amour Haute Nail & Lash Lounge (Đồng Khởi, Q1)',
    2: 'An Yên Thảo Dược & Luxury Retreat (Quận 3)',
    3: 'The Muse Haute Coiffure Boutique (Thảo Điền)'
  };
  openBookingModal(salonNames[id] || 'Salon Làm Đẹp Thượng Lưu');
}

function openQrCheckinModal() {
  openActivityTab();
  showToast('Mở vé lịch hẹn và mã QR Check-in VIP', 'fa-qrcode');
}

function toggleFilterDrawer() {
  showToast('Đang lọc: Bán kính < 3km • Tiêu chuẩn 5★ • Phòng riêng VIP');
}

function handleSearchInput(query) {
  const clearBtn = document.getElementById('searchClearBtn');
  if (clearBtn) {
    clearBtn.style.display = query.trim().length > 0 ? 'flex' : 'none';
  }

  const q = query.trim().toLowerCase();
  const salonCards = document.querySelectorAll('.salon-card');
  let matchCount = 0;

  salonCards.forEach(card => {
    const title = card.querySelector('.salon-title')?.innerText.toLowerCase() || '';
    const services = card.querySelector('.salon-services')?.innerText.toLowerCase() || '';
    const location = card.querySelector('.meta-item')?.innerText.toLowerCase() || '';

    if (!q || title.includes(q) || services.includes(q) || location.includes(q)) {
      card.style.display = 'block';
      matchCount++;
    } else {
      card.style.display = 'none';
    }
  });
}

function clearSearchInput(event) {
  if (event) event.stopPropagation();
  const input = document.getElementById('searchInput');
  const clearBtn = document.getElementById('searchClearBtn');
  if (input) {
    input.value = '';
    input.focus();
  }
  if (clearBtn) clearBtn.style.display = 'none';
  handleSearchInput('');
}

function initSearchTrigger() {
  // Search input is directly focusable and live searchable
}

function togglePhoneFrame() {
  const container = document.querySelector('.device-container');
  if (!container) return;
  if (container.style.maxWidth === '100%') {
    container.style.maxWidth = '440px';
    container.style.height = '90vh';
    container.style.borderRadius = '46px';
    container.style.border = '9px solid #2d0e16';
    showToast('Chế độ xem: Mô phỏng iPhone');
  } else {
    container.style.maxWidth = '100%';
    container.style.height = '100vh';
    container.style.borderRadius = '0';
    container.style.border = 'none';
    showToast('Chế độ xem: Toàn màn hình Responsive');
  }
}

/* =========================================================
   6. GÓI ĐỐI TÁC (3, 6, 12 THÁNG) & ƯU ĐÃI FREE THÁNG ĐẦU
========================================================= */
function selectPlanCycle(months, btnEl) {
  document.querySelectorAll('.cycle-pill-btn').forEach(b => b.classList.remove('active'));
  if (btnEl) btnEl.classList.add('active');

  const stdPriceEl = document.getElementById('planStandardPrice');
  const vipPriceEl = document.getElementById('planVipPrice');
  const vipRegEl = document.getElementById('vipRegularPart');
  const vipAdEl = document.getElementById('vipAdPart');

  let stdFee = 1090000;
  let vipAd = 1700000;

  if (months === 3) {
    stdFee = 590000;
    vipAd = 900000;
  } else if (months === 12) {
    stdFee = 1990000;
    vipAd = 3000000;
  }

  const vipTotal = stdFee + vipAd;

  if (stdPriceEl) stdPriceEl.innerHTML = `${stdFee.toLocaleString('vi-VN')}đ <span>/ ${months} tháng</span>`;
  if (vipPriceEl) vipPriceEl.innerHTML = `${vipTotal.toLocaleString('vi-VN')}đ <span>/ ${months} tháng</span>`;
  if (vipRegEl) vipRegEl.innerText = `• Phí thường niên mở sàn: ${stdFee.toLocaleString('vi-VN')}đ`;
  if (vipAdEl) vipAdEl.innerText = `• Phí quảng cáo đẩy top đề xuất: ${vipAd.toLocaleString('vi-VN')}đ`;
}

function subscribePlan(planName) {
  showToast(`🎉 Kích hoạt ${planName} thành công! Được áp dụng ưu đãi MIỄN PHÍ THÁNG ĐẦU TIÊN (30 ngày 0đ)!`, 'fa-crown');
}

/* =========================================================
   7. DRAGGABLE FLOATING AI CHATBOT BUTTON (Edge movable)
========================================================= */
function initDraggableAiFab() {
  const fab = document.getElementById('draggableAiFab');
  if (!fab) return;

  let isDragging = false;
  let startX, startY, initialLeft, initialTop;

  fab.addEventListener('mousedown', startDrag);
  fab.addEventListener('touchstart', startDrag, { passive: false });

  function startDrag(e) {
    isDragging = false;
    const clientX = e.touches ? e.touches[0].clientX : e.clientX;
    const clientY = e.touches ? e.touches[0].clientY : e.clientY;

    startX = clientX;
    startY = clientY;
    const rect = fab.getBoundingClientRect();
    initialLeft = rect.left;
    initialTop = rect.top;

    document.addEventListener('mousemove', onDrag);
    document.addEventListener('touchmove', onDrag, { passive: false });
    document.addEventListener('mouseup', endDrag);
    document.addEventListener('touchend', endDrag);
  }

  function onDrag(e) {
    const clientX = e.touches ? e.touches[0].clientX : e.clientX;
    const clientY = e.touches ? e.touches[0].clientY : e.clientY;

    const dx = clientX - startX;
    const dy = clientY - startY;

    if (Math.abs(dx) > 4 || Math.abs(dy) > 4) {
      isDragging = true;
      if (e.cancelable) e.preventDefault();
      
      const newX = Math.max(10, Math.min(window.innerWidth - 70, initialLeft + dx));
      const newY = Math.max(60, Math.min(window.innerHeight - 80, initialTop + dy));

      fab.style.left = newX + 'px';
      fab.style.top = newY + 'px';
      fab.style.right = 'auto';
      fab.style.bottom = 'auto';
    }
  }

  function endDrag() {
    document.removeEventListener('mousemove', onDrag);
    document.removeEventListener('touchmove', onDrag);
    document.removeEventListener('mouseup', endDrag);
    document.removeEventListener('touchend', endDrag);
  }

  fab.addEventListener('click', (e) => {
    if (!isDragging) {
      openAiAdvisorModal();
    }
  });
}

function openAiAdvisorModal() {
  const modal = document.getElementById('aiAdvisorModal');
  if (modal) modal.classList.add('active');
}

function closeAiAdvisorModal() {
  const modal = document.getElementById('aiAdvisorModal');
  if (modal) modal.classList.remove('active');
}

function askAiPrompt(promptText) {
  const input = document.getElementById('aiQueryInput');
  if (input) input.value = promptText;
  sendAiQuery();
}

function sendAiQuery() {
  const input = document.getElementById('aiQueryInput');
  const stream = document.getElementById('aiChatStream');
  if (!input || !stream) return;

  const query = input.value.trim();
  if (!query) return;

  // Append user message bubble
  const userRow = document.createElement('div');
  userRow.className = 'ai-msg-row user';
  userRow.innerHTML = `<div class="ai-msg-content">${query}</div>`;
  stream.appendChild(userRow);
  input.value = '';

  const isNoDeposit = query.toLowerCase().includes('không cọc') || query.toLowerCase().includes('miễn cọc');

  // Generate AI Response with direct shop recommendation
  setTimeout(() => {
    const aiRow = document.createElement('div');
    aiRow.className = 'ai-msg-row ai';

    let recHtml = '';
    if (isNoDeposit) {
      recHtml = `
        <div class="ai-shop-rec-card">
          <div class="rec-title">🌿 An Yên Thảo Dược & Luxury Retreat</div>
          <div class="rec-meta">★ 4.97 (1.1k+ đánh giá) • 1.5 km • Q.3</div>
          <div class="rec-service">Dịch vụ: Gội Đầu Dưỡng Sinh Cung Đình 75p (250.000đ)</div>
          <div class="rec-badge no-deposit">🎉 KHÔNG CẦN CỌC • GIỮ CHỖ 0Đ</div>
          <div class="rec-actions">
            <button class="btn-rec-book" onclick="closeAiAdvisorModal(); openBookingModal('Gội Đầu Dưỡng Sinh Cung Đình', 250000, 45, false)">Đặt Giữ Chỗ Miễn Phí</button>
          </div>
        </div>
      `;
    } else {
      recHtml = `
        <div class="ai-shop-rec-card">
          <div class="rec-title">👑 L'Amour Haute Nail & Lash Lounge (VIP Sponsor)</div>
          <div class="rec-meta">★ 4.99 (1.8k+ đánh giá) • 0.8 km • Đồng Khởi, Q.1</div>
          <div class="rec-service">Dịch vụ: Sơn Gel Hữu Cơ Pháp + Dưỡng Móng (180.000đ)</div>
          <div class="rec-badge deposit">📌 CÓ CỌC (Shop set 5 mức từ 50.000đ)</div>
          <div class="rec-actions">
            <button class="btn-rec-book" onclick="closeAiAdvisorModal(); openBookingModal('Sơn Gel Hữu Cơ Pháp', 180000, 45, true)">Đặt Lịch Cọc 5 Mệnh Giá</button>
          </div>
        </div>
      `;
    }

    aiRow.innerHTML = `
      <div class="ai-msg-avatar"><i class="fa-solid fa-sparkles"></i></div>
      <div class="ai-msg-content">
        Dạ em đã chọn lọc salon uy tín hoàn hảo theo yêu cầu của bạn:
        ${recHtml}
      </div>
    `;
    stream.appendChild(aiRow);
    stream.scrollTop = stream.scrollHeight;
  }, 400);

  stream.scrollTop = stream.scrollHeight;
}

// Auto init Draggable AI Button on load
document.addEventListener('DOMContentLoaded', () => {
  initDraggableAiFab();
});
setTimeout(() => {
  initDraggableAiFab();
}, 500);

/* =========================================================
   18. CUSTOMER & MESSAGES INTERACTIONS (IMAGE 1 & 2)
========================================================= */

function toggleFavorite(e, btn) {
  e.stopPropagation();
  btn.classList.toggle('active');
  const icon = btn.querySelector('i');
  if (btn.classList.contains('active')) {
    icon.className = 'fa-solid fa-heart';
    showToast("Đã lưu L'Amour Luxury vào danh sách yêu thích!", 'fa-heart');
  } else {
    icon.className = 'fa-regular fa-heart';
    showToast("Đã bỏ lưu khỏi danh sách yêu thích");
  }
}

function openAllCategoriesModal() {
  showToast('Khám phá toàn bộ 8 danh mục dịch vụ làm đẹp cao cấp', 'fa-crown');
}

function openSalonDetail(id) {
  showToast("Xem chi tiết salon L'Amour Luxury Spa & Nail • Q.1", 'fa-store');
}

// Messages & Activity Pane Switcher
function switchMsgPane(paneName) {
  const pMsg = document.getElementById('paneMsgMessages');
  const pAct = document.getElementById('paneMsgActivity');
  const tAct = document.getElementById('tabActivityToggle');
  const tMsg = document.getElementById('tabMessagesToggle');

  if (paneName === 'messages') {
    if (pMsg) pMsg.style.display = 'block';
    if (pAct) pAct.style.display = 'none';
    if (tMsg) tMsg.classList.add('active');
    if (tAct) tAct.classList.remove('active');
  } else {
    if (pMsg) pMsg.style.display = 'none';
    if (pAct) pAct.style.display = 'block';
    if (tAct) tAct.classList.add('active');
    if (tMsg) tMsg.classList.remove('active');
  }
}

// Messages Filter Pills
function filterMsgCategory(tag, btn) {
  document.querySelectorAll('.msg-pill').forEach(b => b.classList.remove('active'));
  if (btn) btn.classList.add('active');

  const items = document.querySelectorAll('.msg-conv-item');
  items.forEach(item => {
    if (tag === 'all') {
      item.style.display = 'flex';
    } else if (tag === 'unread') {
      const hasUnread = !item.querySelector('.conv-unread-dot')?.classList.contains('hide') || !!item.querySelector('.conv-badge');
      item.style.display = hasUnread ? 'flex' : 'none';
    } else {
      item.style.display = item.getAttribute('data-tag') === tag ? 'flex' : 'none';
    }
  });
}

// Messages Search
function handleMsgSearch(query) {
  const q = query.toLowerCase().trim();
  const items = document.querySelectorAll('.msg-conv-item');
  items.forEach(item => {
    const text = item.innerText.toLowerCase();
    item.style.display = text.includes(q) ? 'flex' : 'none';
  });
}

// Live Chat Modal (Interactive conversation)
function openChatModal(recipient) {
  const modal = document.getElementById('chatModal');
  const title = document.getElementById('chatModalRecipient');
  if (title) title.innerText = recipient;
  if (modal) modal.classList.add('active');
}

function closeChatModal() {
  const modal = document.getElementById('chatModal');
  if (modal) modal.classList.remove('active');
}

function sendLiveChatMessage() {
  const input = document.getElementById('liveChatInput');
  const stream = document.getElementById('chatStream');
  if (!input || !stream) return;

  const msg = input.value.trim();
  if (!msg) return;

  // Append user row
  const userRow = document.createElement('div');
  userRow.className = 'ai-msg-row user';
  userRow.innerHTML = `<div class="ai-msg-content">${msg}</div>`;
  stream.appendChild(userRow);
  input.value = '';

  // Auto reply after 600ms
  setTimeout(() => {
    const replyRow = document.createElement('div');
    replyRow.className = 'ai-msg-row';
    replyRow.innerHTML = `
      <div class="ai-msg-avatar" style="background:#fae4dc; color:var(--dusty-merlot);"><i class="fa-solid fa-store"></i></div>
      <div class="ai-msg-content">
        Dạ vâng ạ! Em đã lưu yêu cầu "<em>${msg}</em>" vào hồ sơ lịch hẹn của chị rồi nhé! ✨ Chúc chị ngày mới tuyệt vời!
      </div>
    `;
    stream.appendChild(replyRow);
    stream.scrollTop = stream.scrollHeight;
  }, 600);

  stream.scrollTop = stream.scrollHeight;
}


/* =========================================================
   19. ADMIN MANAGEMENT INTERACTIONS (IMAGE 3 & SPEC A-G)
========================================================= */

// Switch admin sub-sections (Home, Shops, Customers, Finance)
function switchAdminSection(sectionName, subTab = "revenue") {
  const adminView = document.getElementById("adminView");
  if (adminView) adminView.style.display = "block";

  const custView = document.getElementById("customerView");
  const partView = document.getElementById("partnerView");
  const actView = document.getElementById("activityView");
  if (custView) custView.style.display = "none";
  if (partView) partView.style.display = "none";
  if (actView) actView.style.display = "none";

  closeAdminMoreModal();

  const allPanes = {
    home: document.getElementById("adminPaneHome"),
    shops: document.getElementById("adminPaneShops"),
    customers: document.getElementById("adminPaneCustomers"),
    finance: document.getElementById("adminPaneFinance"),
    bookings: document.getElementById("adminPaneBookings"),
    services: document.getElementById("adminPaneServices"),
    reviews: document.getElementById("adminPaneReviews"),
    notifications: document.getElementById("adminPaneNotifications"),
    staff: document.getElementById("adminPaneStaff"),
    settings: document.getElementById("adminPaneSettings")
  };

  Object.keys(allPanes).forEach(k => {
    if (allPanes[k]) {
      allPanes[k].style.display = (sectionName === k) ? "block" : "none";
    }
  });

  if (sectionName === "finance") {
    switchFinanceTab(subTab);
  } else if (sectionName === "bookings" && typeof renderAdminBookings === "function") {
    renderAdminBookings();
  } else if (sectionName === "services" && typeof renderAdminServices === "function") {
    renderAdminServices();
  } else if (sectionName === "reviews" && typeof renderAdminReviews === "function") {
    renderAdminReviews();
  } else if (sectionName === "notifications" && typeof renderAdminNotifs === "function") {
    renderAdminNotifs();
  } else if (sectionName === "staff" && typeof renderAdminStaff === "function") {
    renderAdminStaff();
  }

  if (currentRole === "admin") {
    document.querySelectorAll(".nav-item").forEach(i => i.classList.remove("active"));
    if (sectionName === "home") document.getElementById("navHome")?.classList.add("active");
    else if (sectionName === "shops") document.getElementById("navActivity")?.classList.add("active");
    else if (sectionName === "customers") document.getElementById("navPay")?.classList.add("active");
  }

  if (adminView) adminView.scrollTop = 0;
}

// Finance Tab switcher (Doanh thu sàn vs Tiền cọc)
function switchFinanceTab(tabKey) {
  const pRev = document.getElementById('finPaneRevenue');
  const pDep = document.getElementById('finPaneDeposit');
  const bRev = document.getElementById('btnFinRevenue');
  const bDep = document.getElementById('btnFinDeposit');

  if (tabKey === 'revenue') {
    if (pRev) pRev.style.display = 'block';
    if (pDep) pDep.style.display = 'none';
    if (bRev) bRev.classList.add('active');
    if (bDep) bDep.classList.remove('active');
  } else {
    if (pRev) pRev.style.display = 'none';
    if (pDep) pDep.style.display = 'block';
    if (bDep) bDep.classList.add('active');
    if (bRev) bRev.classList.remove('active');
  }
}

// Shop filters & search
function filterAdminShopStatus(status, btn) {
  document.querySelectorAll('.adm-filter-pills .adm-pill').forEach(b => b.classList.remove('active'));
  if (btn) btn.classList.add('active');

  const cards = document.querySelectorAll('#adminShopsContainer .adm-shop-card');
  cards.forEach(card => {
    if (status === 'all' || card.getAttribute('data-status') === status) {
      card.style.display = 'block';
    } else {
      card.style.display = 'none';
    }
  });
}

function filterAdminShops(query) {
  const q = query.toLowerCase().trim();
  const cards = document.querySelectorAll('#adminShopsContainer .adm-shop-card');
  cards.forEach(card => {
    card.style.display = card.innerText.toLowerCase().includes(q) ? 'block' : 'none';
  });
}

function filterAdminCustomers(query) {
  const q = query.toLowerCase().trim();
  const cards = document.querySelectorAll('#adminCustContainer .adm-shop-card');
  cards.forEach(card => {
    card.style.display = card.innerText.toLowerCase().includes(q) ? 'block' : 'none';
  });
}

// Modals: Shop details (4 groups)
function openShopDetailModal(shopId) {
  const modal = document.getElementById('shopDetailModal');
  const title = document.getElementById('admShopModalTitle');
  const name = document.getElementById('admShopName');
  const code = document.getElementById('admShopCode');

  if (shopId === '1') {
    if (title) title.innerText = "L'Amour Luxury Spa & Nail";
    if (name) name.innerText = "L'Amour Luxury Spa & Nail";
    if (code) code.innerText = "SALON-001";
  } else if (shopId === '2') {
    if (title) title.innerText = "An Yên Thảo Dược Retreat";
    if (name) name.innerText = "An Yên Thảo Dược Retreat";
    if (code) code.innerText = "SALON-002";
  } else if (shopId === '3') {
    if (title) title.innerText = "The Royal Nail & Lash Lounge";
    if (name) name.innerText = "The Royal Nail & Lash Lounge";
    if (code) code.innerText = "SALON-003";
  }

  if (modal) modal.classList.add('active');
}

function closeShopDetailModal() {
  const modal = document.getElementById('shopDetailModal');
  if (modal) modal.classList.remove('active');
}

// Modals: Customer details (4 groups)
function openCustomerDetailModal(custId) {
  const modal = document.getElementById('customerDetailModal');
  const title = document.getElementById('admCustModalTitle');
  const name = document.getElementById('admCustName');
  const code = document.getElementById('admCustCode');

  if (custId === '1') {
    if (title) title.innerText = "Khánh Linh";
    if (name) name.innerText = "Khánh Linh";
    if (code) code.innerText = "CUST-001";
  } else {
    if (title) title.innerText = "Nguyễn Thị Mai";
    if (name) name.innerText = "Nguyễn Thị Mai";
    if (code) code.innerText = "CUST-002";
  }

  if (modal) modal.classList.add('active');
}

function closeCustomerDetailModal() {
  const modal = document.getElementById('customerDetailModal');
  if (modal) modal.classList.remove('active');
}

// Modals: Admin More Menu
function openAdminMoreModal() {
  const modal = document.getElementById('adminMoreModal');
  if (modal) modal.classList.add('active');
}

function closeAdminMoreModal() {
  const modal = document.getElementById('adminMoreModal');
  if (modal) modal.classList.remove('active');
}

// Admin Actions
function approveRefund(cardId, amount) {
  const card = document.getElementById(cardId);
  if (card) {
    card.style.background = '#f0fdf4';
    card.style.borderColor = '#86efac';
    card.innerHTML = `
      <div style="display:flex; justify-content:space-between; align-items:center; padding:4px 0;">
        <span style="font-size:12px; font-weight:700; color:#15803d;"><i class="fa-solid fa-circle-check"></i> Đã duyệt hoàn ${amount} tự động về Ví khách hàng</span>
        <span style="font-size:11px; color:#7a5e66;">Vừa xong</span>
      </div>
    `;
  }
  showToast(`Đã duyệt hoàn ${amount} thành công! Tiền cọc đã chuyển về Ví khách hàng.`, 'fa-circle-check');
}

function rejectRefund(cardId) {
  const card = document.getElementById(cardId);
  if (card) {
    card.style.opacity = '0.5';
    card.innerHTML = `
      <div style="padding:4px 0; font-size:12px; color:#dc2626; font-weight:700;">
        <i class="fa-solid fa-circle-xmark"></i> Yêu cầu hoàn cọc đã bị từ chối theo chính sách hủy muộn &lt; 2h
      </div>
    `;
  }
  showToast('Đã từ chối yêu cầu hoàn cọc. Đã gửi thông báo cho khách hàng.', 'fa-ban');
}

function actionShopApprove() {
  showToast('Hồ sơ salon đã được DUYỆT thành công! Gian hàng đã kích hoạt trên Marketplace.', 'fa-circle-check');
  closeShopDetailModal();
}

function actionShopReject() {
  showToast('Đã từ chối duyệt hồ sơ. Yêu cầu salon bổ sung thông tin giấy phép.', 'fa-triangle-exclamation');
  closeShopDetailModal();
}

function actionShopLock() {
  showToast('Đã cập nhật trạng thái Tạm khóa / Mở khóa gian hàng.', 'fa-lock');
  closeShopDetailModal();
}

function actionShopTerminate() {
  showToast('Đã ngưng hợp tác với đối tác. Các lịch hẹn hiện có vẫn được bảo lưu để đối soát.', 'fa-triangle-exclamation');
  closeShopDetailModal();
}

function actionCustomerLock() {
  showToast('Đã thay đổi trạng thái Khóa / Mở khóa tài khoản khách hàng!', 'fa-user-lock');
  closeCustomerDetailModal();
}



/* =========================================================
   20. ADMIN - DỮ LIỆU MẪU (BOOKINGS, SERVICES, REVIEWS)
   Spec 4 (Dịch vụ), Spec 5 (Booking), Spec 7 (Đánh giá)
========================================================= */

// --- DATA: BOOKINGS ---
const adminBookingsData = [
  {
    id: 'BK-9021', code: 'BK-9021',
    customer: 'Nguyễn Thị Mai', phone: '0902 333 444',
    salon: "L'Amour Luxury Spa & Nail", service: 'Sơn Gel Hữu Cơ Pháp + Dưỡng Móng',
    datetime: '12/10/2025 · 15:30', depositAmt: '50.000đ',
    status: 'confirmed', statusLabel: 'Đã xác nhận'
  },
  {
    id: 'BK-8842', code: 'BK-8842',
    customer: 'Hoàng Kim Oanh', phone: '0901 222 333',
    salon: 'An Yên Thảo Dược Retreat', service: 'Massage Body Đá Nóng Himalaya',
    datetime: '10/10/2025 · 10:00', depositAmt: '84.000đ',
    status: 'completed', statusLabel: 'Hoàn thành'
  },
  {
    id: 'BK-7701', code: 'BK-7701',
    customer: 'Khánh Linh', phone: '0901 888 777',
    salon: "L'Amour Luxury Spa & Nail", service: 'Nail Art Mắt Mèo Cực Quang VIP',
    datetime: '08/10/2025 · 09:30', depositAmt: '100.000đ',
    status: 'cancelled', statusLabel: 'Đã hủy'
  },
  {
    id: 'BK-9305', code: 'BK-9305',
    customer: 'Lê Ngọc Hương', phone: '0908 555 666',
    salon: 'The Royal Nail & Lash Lounge', service: 'Trang Điểm Dạ Tiệc VIP',
    datetime: '14/10/2025 · 14:00', depositAmt: '200.000đ',
    status: 'confirmed', statusLabel: 'Đã xác nhận'
  }
];

// --- DATA: SERVICES ---
const adminServicesData = [
  {
    id: 'SVC-001', name: 'Sơn Gel Hữu Cơ Pháp + Dưỡng Móng', salon: "L'Amour Luxury",
    category: 'Nail', price: '180.000đ', deposit: '30%',
    status: 'active', statusLabel: 'Đang hoạt động'
  },
  {
    id: 'SVC-002', name: 'Dịch vụ Thư Giãn "Liệu Pháp Bí Ẩn"', salon: 'Spa Không Tên',
    category: 'Spa', price: '950.000đ', deposit: '0%',
    status: 'pending', statusLabel: 'Chờ kiểm duyệt'
  },
  {
    id: 'SVC-003', name: 'Massage Toàn Thân + Combo Đặc Biệt', salon: 'Nhà Nghỉ XYZ',
    category: 'Spa', price: '1.200.000đ', deposit: '50%',
    status: 'violation', statusLabel: 'Vi phạm'
  },
  {
    id: 'SVC-004', name: 'Nail Art Mắt Mèo Cực Quang VIP', salon: "L'Amour Luxury",
    category: 'Nail', price: '280.000đ', deposit: '30%',
    status: 'active', statusLabel: 'Đang hoạt động'
  },
  {
    id: 'SVC-005', name: 'Gói Làm Đẹp Tinh Chất Nhau Thai Cừu', salon: 'Beauty Corner',
    category: 'Skincare', price: '2.500.000đ', deposit: '50%',
    status: 'pending', statusLabel: 'Chờ kiểm duyệt'
  }
];

// --- DATA: REVIEWS ---
const adminReviewsData = [
  {
    id: 'REV-001', customer: 'Khánh Linh', salon: "L'Amour Luxury Spa & Nail",
    stars: 5, comment: 'Dịch vụ tuyệt vời, nhân viên rất chuyên nghiệp!',
    flagged: false, flagReason: ''
  },
  {
    id: 'REV-002', customer: 'Nguyễn Văn A', salon: 'An Yên Thảo Dược Retreat',
    stars: 1, comment: 'Dịch vụ kém, nhân viên thô lỗ. Đây là spam review để hại đối thủ!',
    flagged: true, flagReason: 'Báo cáo: Nội dung spam, không đúng thực tế'
  },
  {
    id: 'REV-003', customer: 'Hoàng Kim Oanh', salon: "L'Amour Luxury Spa & Nail",
    stars: 4, comment: 'Không gian đẹp, tay nghề tốt. Chỉ hơi đợi lâu.',
    flagged: false, flagReason: ''
  },
  {
    id: 'REV-004', customer: 'Tài khoản ẩn danh', salon: 'The Royal Nail & Lash',
    stars: 1, comment: 'Chủ tài khoản cạnh tranh không lành mạnh, đăng review giả.',
    flagged: true, flagReason: 'Báo cáo: Review giả, tài khoản không có lịch hẹn tại đây'
  }
];

/* =========================================================
   21. ADMIN - BOOKING MANAGEMENT (SPEC 5)
========================================================= */

let _bookingFilter = 'all';
let _bookingSearch = '';

function renderAdminBookings() {
  const container = document.getElementById('adminBookingsContainer');
  if (!container) return;

  const filtered = adminBookingsData.filter(b => {
    const matchStatus = _bookingFilter === 'all' || b.status === _bookingFilter;
    const q = _bookingSearch.toLowerCase();
    const matchSearch = !q || b.code.toLowerCase().includes(q) ||
      b.customer.toLowerCase().includes(q) || b.phone.includes(q) ||
      b.salon.toLowerCase().includes(q);
    return matchStatus && matchSearch;
  });

  if (filtered.length === 0) {
    container.innerHTML = '<p style="text-align:center; color:#9a8a8e; padding:24px 0; font-size:13px;">Không tìm thấy đơn đặt lịch nào.</p>';
    return;
  }

  const statusColor = { confirmed: '#2563eb', completed: '#16a34a', cancelled: '#dc2626' };

  container.innerHTML = filtered.map(b => `
    <div class="adm-shop-card" data-bk-status="${b.status}" style="margin-bottom:12px;">
      <div class="adm-card-top">
        <div>
          <h4 class="adm-card-title">${b.code}</h4>
          <p class="adm-card-code">${b.datetime} · ${b.salon}</p>
        </div>
        <span class="adm-status-tag" style="background:${statusColor[b.status] || '#6b7280'}1a; color:${statusColor[b.status] || '#6b7280'}; border:1px solid ${statusColor[b.status] || '#6b7280'}33;">${b.statusLabel}</span>
      </div>
      <div class="adm-card-divider"></div>
      <div class="adm-card-bottom">
        <div>
          <span style="font-size:12px; color:#24090F;"><i class="fa-solid fa-user" style="color:#6B1228;"></i> ${b.customer} · ${b.phone}</span><br>
          <span style="font-size:12px; color:#7A686D;"><i class="fa-solid fa-scissors" style="color:#6B1228;"></i> ${b.service} · Cọc: ${b.depositAmt}</span>
        </div>
      </div>
      ${b.status !== 'completed' ? `
      <div class="adm-card-actions-row" style="margin-top:10px;">
        <button class="btn-adm-reject" style="font-size:12px;" onclick="adminCancelBooking('${b.id}')">
          <i class="fa-solid fa-ban"></i> Can thiệp hủy
        </button>
        <button class="btn-plan outline" style="font-size:12px;" onclick="showToast('Đã xem chi tiết đơn ${b.code}', 'fa-eye')">
          <i class="fa-solid fa-eye"></i> Xem chi tiết
        </button>
      </div>` : ''}
    </div>
  `).join('');
}

function filterAdminBookingStatus(status, btn) {
  _bookingFilter = status;
  document.querySelectorAll('#bookingFilterPills .adm-pill').forEach(b => b.classList.remove('active'));
  if (btn) btn.classList.add('active');
  renderAdminBookings();
}

function filterAdminBookings(query) {
  _bookingSearch = query;
  renderAdminBookings();
}

function adminCancelBooking(bookingId) {
  const item = adminBookingsData.find(b => b.id === bookingId);
  if (item) {
    item.status = 'cancelled';
    item.statusLabel = 'Đã hủy';
    showToast(`Đã can thiệp hủy đơn ${bookingId}. Hoàn cọc xử lý tự động.`, 'fa-ban');
    renderAdminBookings();
  }
}

/* =========================================================
   22. ADMIN - SERVICE MANAGEMENT (SPEC 4)
========================================================= */

let _serviceFilter = 'all';

function renderAdminServices() {
  const container = document.getElementById('adminServicesContainer');
  if (!container) return;

  const filtered = adminServicesData.filter(s =>
    _serviceFilter === 'all' || s.status === _serviceFilter
  );

  if (filtered.length === 0) {
    container.innerHTML = '<p style="text-align:center; color:#9a8a8e; padding:24px 0; font-size:13px;">Không có dịch vụ nào.</p>';
    return;
  }

  const statusStyle = {
    active: { bg: '#f0fdf4', color: '#16a34a', border: '#86efac' },
    pending: { bg: '#fffbeb', color: '#d97706', border: '#fcd34d' },
    violation: { bg: '#fef2f2', color: '#dc2626', border: '#fca5a5' }
  };

  container.innerHTML = filtered.map(s => {
    const st = statusStyle[s.status] || statusStyle.active;
    return `
    <div class="adm-shop-card" data-svc-status="${s.status}" style="margin-bottom:12px;">
      <div class="adm-card-top">
        <div style="flex:1;">
          <h4 class="adm-card-title" style="font-size:13.5px;">${s.name}</h4>
          <p class="adm-card-code">Salon: ${s.salon} · Danh mục: ${s.category}</p>
        </div>
        <span class="adm-status-tag" style="background:${st.bg}; color:${st.color}; border:1px solid ${st.border}; white-space:nowrap;">${s.statusLabel}</span>
      </div>
      <div class="adm-card-divider"></div>
      <div class="adm-card-bottom">
        <span style="font-size:12px;"><i class="fa-solid fa-tag" style="color:#6B1228;"></i> Giá: ${s.price} · Cọc: ${s.deposit}</span>
      </div>
      <div class="adm-card-actions-row" style="margin-top:10px;">
        ${s.status === 'pending' ? `<button class="btn-adm-approve" style="font-size:12px;" onclick="approveAdminService('${s.id}')"><i class="fa-solid fa-check"></i> Duyệt</button>` : ''}
        ${s.status !== 'active' ? '' : `<button class="btn-plan outline" style="font-size:12px; color:#d97706; border-color:#fcd34d;" onclick="flagAdminService('${s.id}')"><i class="fa-solid fa-flag"></i> Gắn cờ vi phạm</button>`}
        ${s.status === 'violation' ? `<button class="btn-adm-reject" style="font-size:12px;" onclick="flagAdminService('${s.id}')"><i class="fa-solid fa-trash"></i> Gỡ xuống</button>` : ''}
      </div>
    </div>`;
  }).join('');
}

function filterAdminServiceStatus(status, btn) {
  _serviceFilter = status;
  document.querySelectorAll('#serviceFilterPills .adm-pill').forEach(b => b.classList.remove('active'));
  if (btn) btn.classList.add('active');
  renderAdminServices();
}

function approveAdminService(id) {
  const item = adminServicesData.find(s => s.id === id);
  if (item) {
    item.status = 'active';
    item.statusLabel = 'Đang hoạt động';
    showToast(`Đã duyệt dịch vụ "${item.name}" — Hiển thị trên marketplace.`, 'fa-circle-check');
    renderAdminServices();
  }
}

function flagAdminService(id) {
  const item = adminServicesData.find(s => s.id === id);
  if (item) {
    item.status = 'violation';
    item.statusLabel = 'Vi phạm';
    showToast(`Đã gắn cờ vi phạm và gỡ dịch vụ "${item.name}" khỏi marketplace.`, 'fa-flag');
    renderAdminServices();
  }
}

/* =========================================================
   23. ADMIN - REVIEW MANAGEMENT (SPEC 7)
========================================================= */

let _reviewFilter = 'all';

function renderAdminReviews() {
  const container = document.getElementById('adminReviewsContainer');
  if (!container) return;

  const filtered = adminReviewsData.filter(r =>
    _reviewFilter === 'all' || (_reviewFilter === 'flagged' && r.flagged)
  );

  if (filtered.length === 0) {
    container.innerHTML = '<p style="text-align:center; color:#9a8a8e; padding:24px 0; font-size:13px;">Không có đánh giá nào.</p>';
    return;
  }

  container.innerHTML = filtered.map(r => {
    const stars = '★'.repeat(r.stars) + '☆'.repeat(5 - r.stars);
    return `
    <div class="adm-shop-card" id="reviewCard-${r.id}" data-rv-flagged="${r.flagged}" style="margin-bottom:12px; ${r.flagged ? 'border-left:3px solid #dc2626;' : ''}">
      <div class="adm-card-top">
        <div style="flex:1;">
          <div style="display:flex; align-items:center; gap:8px; margin-bottom:4px;">
            <div class="cust-icon-circle" style="width:30px; height:30px; font-size:13px;"><i class="fa-solid fa-user"></i></div>
            <div>
              <strong style="font-size:13px; color:#24090F;">${r.customer}</strong>
              <p style="font-size:11px; color:#7A686D; margin:0;">${r.salon}</p>
            </div>
          </div>
          <span style="color:#d97706; font-size:14px; letter-spacing:2px;">${stars}</span>
        </div>
        ${r.flagged ? `<span class="adm-status-tag" style="background:#fef2f2; color:#dc2626; border:1px solid #fca5a5; white-space:nowrap; font-size:11px;"><i class="fa-solid fa-flag"></i> Bị báo cáo</span>` : ''}
      </div>
      <div class="adm-card-divider"></div>
      <p style="font-size:12.5px; color:#24090F; margin: 8px 0; line-height:1.5;">"${r.comment}"</p>
      ${r.flagged ? `<p style="font-size:11px; color:#dc2626; font-style:italic; margin-bottom:8px;"><i class="fa-solid fa-circle-exclamation"></i> ${r.flagReason}</p>` : ''}
      <div class="adm-card-actions-row">
        ${r.flagged ? `
          <button class="btn-adm-reject" style="font-size:12px;" onclick="hideAdminReview('${r.id}')"><i class="fa-solid fa-eye-slash"></i> Ẩn đánh giá</button>
          <button class="btn-plan outline" style="font-size:12px;" onclick="keepAdminReview('${r.id}')"><i class="fa-solid fa-check"></i> Bỏ qua, giữ lại</button>
        ` : `
          <button class="btn-plan outline" style="font-size:12px; color:#d97706; border-color:#fcd34d;" onclick="flagAdminReview('${r.id}')"><i class="fa-solid fa-flag"></i> Gắn cờ vi phạm</button>
        `}
      </div>
    </div>`;
  }).join('');
}

function filterAdminReviewStatus(status, btn) {
  _reviewFilter = status;
  document.querySelectorAll('#reviewFilterPills .adm-pill').forEach(b => b.classList.remove('active'));
  if (btn) btn.classList.add('active');
  renderAdminReviews();
}

function hideAdminReview(id) {
  const card = document.getElementById(`reviewCard-${id}`);
  if (card) {
    card.style.opacity = '0.4';
    card.style.pointerEvents = 'none';
    card.querySelector('.adm-card-actions-row').innerHTML =
      '<span style="font-size:12px; color:#dc2626; font-weight:700;"><i class="fa-solid fa-eye-slash"></i> Đánh giá đã bị ẩn khỏi Marketplace</span>';
  }
  showToast('Đã ẩn đánh giá vi phạm. Người dùng sẽ nhận thông báo.', 'fa-eye-slash');
}

function keepAdminReview(id) {
  const item = adminReviewsData.find(r => r.id === id);
  if (item) {
    item.flagged = false;
    item.flagReason = '';
    showToast('Đã bỏ qua báo cáo. Đánh giá được giữ lại trên Marketplace.', 'fa-check');
    renderAdminReviews();
  }
}

function flagAdminReview(id) {
  const item = adminReviewsData.find(r => r.id === id);
  if (item) {
    item.flagged = true;
    item.flagReason = 'Admin gắn cờ thủ công để xem xét';
    showToast('Đã gắn cờ đánh giá để xem xét vi phạm.', 'fa-flag');
    renderAdminReviews();
  }
}

/* =========================================================
   PATCH switchAdminSection — mở rộng cho Bookings, Services, Reviews
========================================================= */

// switchAdminSection unified definition above

/* =========================================================
   24. ADMIN - NOTIFICATIONS, STAFF & SETTINGS MODULES
   Hoàn thiện toàn bộ các chức năng trong Admin Hub
========================================================= */

// --- DỮ LIỆU THÔNG BÁO HỆ THỐNG ---
const adminNotifsData = [
  {
    id: 'NTF-01',
    title: 'Hồ sơ salon mới chờ phê duyệt',
    desc: 'Salon The Royal Nail & Lash Lounge vừa gửi hồ sơ đăng ký mở gian hàng.',
    time: '10 phút trước',
    type: 'alert',
    unread: true,
    actionText: 'Duyệt hồ sơ',
    actionFn: "switchAdminSection('shops')"
  },
  {
    id: 'NTF-02',
    title: 'Yêu cầu hoàn cọc từ khách hàng #BK-9021',
    desc: 'Khách hàng Nguyễn Thị Mai yêu cầu hủy lịch trước 24h và xin hoàn 50.000đ cọc.',
    time: '45 phút trước',
    type: 'alert',
    unread: true,
    actionText: 'Xem tài chính',
    actionFn: "switchAdminSection('finance', 'deposit')"
  },
  {
    id: 'NTF-03',
    title: 'Dịch vụ mới được Salon đăng tải',
    desc: 'Beauty Corner đăng tải dịch vụ mới: "Gói Làm Đẹp Tinh Chất Nhau Thai Cừu".',
    time: '2 giờ trước',
    type: 'info',
    unread: false,
    actionText: 'Kiểm duyệt',
    actionFn: "switchAdminSection('services')"
  },
  {
    id: 'NTF-04',
    title: 'Đối soát doanh thu tuần tự động',
    desc: 'Hệ thống đã tổng hợp báo cáo hoa hồng tuần cho 12 salon đối tác.',
    time: 'Hôm qua',
    type: 'info',
    unread: false,
    actionText: 'Báo cáo',
    actionFn: "switchAdminSection('finance', 'revenue')"
  }
];

let _notifFilter = 'all';

function renderAdminNotifs() {
  const container = document.getElementById('adminNotifsContainer');
  if (!container) return;

  const filtered = adminNotifsData.filter(n => {
    if (_notifFilter === 'unread') return n.unread;
    if (_notifFilter === 'alert') return n.type === 'alert';
    return true;
  });

  if (filtered.length === 0) {
    container.innerHTML = '<p style="text-align:center; color:#9a8a8e; padding:24px 0; font-size:13px;">Không có thông báo nào.</p>';
    return;
  }

  container.innerHTML = filtered.map(n => `
    <div class="adm-shop-card" style="margin-bottom:10px; ${n.unread ? 'border-left: 3px solid #6B1228; background:#fffcfb;' : 'opacity:0.85;'}">
      <div style="display:flex; justify-content:space-between; align-items:flex-start;">
        <div style="display:flex; gap:10px; align-items:flex-start;">
          <div style="width:34px; height:34px; border-radius:10px; background:${n.type === 'alert' ? '#fef2f2' : '#f0fdf4'}; color:${n.type === 'alert' ? '#dc2626' : '#16a34a'}; display:flex; align-items:center; justify-content:center; font-size:14px; flex-shrink:0;">
            <i class="fa-solid ${n.type === 'alert' ? 'fa-triangle-exclamation' : 'fa-bell'}"></i>
          </div>
          <div>
            <h4 style="font-size:13px; font-weight:700; color:#24090F; margin:0 0 2px;">${n.title}</h4>
            <p style="font-size:11.5px; color:#7A686D; margin:0 0 4px; line-height:1.4;">${n.desc}</p>
            <span style="font-size:10.5px; color:#9a8a8e;"><i class="fa-regular fa-clock"></i> ${n.time}</span>
          </div>
        </div>
        ${n.unread ? '<span style="width:8px; height:8px; border-radius:50%; background:#dc2626; display:inline-block; margin-top:4px;"></span>' : ''}
      </div>
      <div class="adm-card-actions-row" style="margin-top:8px; justify-content:flex-end;">
        <button class="btn-plan outline" style="font-size:11px; padding:4px 10px;" onclick="${n.actionFn}">
          ${n.actionText} <i class="fa-solid fa-chevron-right" style="font-size:10px;"></i>
        </button>
      </div>
    </div>
  `).join('');
}

function filterAdminNotifStatus(status, btn) {
  _notifFilter = status;
  document.querySelectorAll('#notifFilterPills .adm-pill').forEach(b => b.classList.remove('active'));
  if (btn) btn.classList.add('active');
  renderAdminNotifs();
}

function adminBroadcastNotice() {
  const msg = prompt('Nhập nội dung thông báo muốn phát sóng đến toàn sàn:');
  if (msg && msg.trim()) {
    adminNotifsData.unshift({
      id: 'NTF-' + Date.now(),
      title: '📢 Thông báo Admin: ' + msg.trim(),
      desc: 'Thông báo phát sóng tự động đến tất cả người dùng ứng dụng.',
      time: 'Vừa xong',
      type: 'info',
      unread: true,
      actionText: 'Chi tiết',
      actionFn: "showToast('Thông báo phát sóng đã gửi thành công!', 'fa-paper-plane')"
    });
    renderAdminNotifs();
    showToast('Đã phát sóng thông báo thành công đến toàn hệ thống!', 'fa-bullhorn');
  }
}

// --- DỮ LIỆU TÀI KHOẢN & PHÂN QUYỀN ---
const adminStaffData = [
  {
    id: 'STF-01',
    name: 'Nguyễn Tuấn (Bạn)',
    role: 'Admin Tổng (Super Admin)',
    email: 'admin.tuan@glowprestige.vn',
    status: 'active',
    permissions: 'Toàn quyền hệ thống, phê duyệt tài chính, cấu hình sàn'
  },
  {
    id: 'STF-02',
    name: 'Trần Minh Quân',
    role: 'Kế toán & Đối soát (Finance)',
    email: 'quan.tm@glowprestige.vn',
    status: 'active',
    permissions: 'Quản lý doanh thu, duyệt hoàn cọc, xuất hóa đơn'
  },
  {
    id: 'STF-03',
    name: 'Lê Thùy Dung',
    role: 'Kiểm duyệt viên (Moderator)',
    email: 'dung.lt@glowprestige.vn',
    status: 'active',
    permissions: 'Duyệt hồ sơ salon, kiểm duyệt dịch vụ & đánh giá'
  }
];

function renderAdminStaff() {
  const container = document.getElementById('adminStaffContainer');
  if (!container) return;

  container.innerHTML = adminStaffData.map(s => `
    <div class="adm-shop-card" style="margin-bottom:12px;">
      <div class="adm-card-top">
        <div style="display:flex; align-items:center; gap:10px;">
          <div class="cust-icon-circle" style="width:36px; height:36px; font-size:15px; background:#fae4dc; color:#6B1228;">
            <i class="fa-solid fa-user-shield"></i>
          </div>
          <div>
            <h4 class="adm-card-title">${s.name}</h4>
            <p class="adm-card-code">${s.role} · ${s.email}</p>
          </div>
        </div>
        <span class="adm-status-tag active">Hoạt động</span>
      </div>
      <div class="adm-card-divider"></div>
      <p style="font-size:11.5px; color:#7A686D; margin:4px 0;"><i class="fa-solid fa-key" style="color:#6B1228;"></i> Quyền hạn: ${s.permissions}</p>
      <div class="adm-card-actions-row" style="margin-top:8px;">
        <button class="btn-plan outline" style="font-size:11.5px;" onclick="showToast('Cập nhật quyền hạn cho ${s.name}', 'fa-user-pen')">
          <i class="fa-solid fa-pen"></i> Phân quyền
        </button>
        ${s.id !== 'STF-01' ? `
        <button class="btn-adm-reject" style="font-size:11.5px;" onclick="showToast('Đã tạm ngưng quyền truy cập của ${s.name}', 'fa-lock')">
          <i class="fa-solid fa-lock"></i> Khóa quyền
        </button>` : ''}
      </div>
    </div>
  `).join('');
}

function adminAddNewStaff() {
  const name = prompt('Nhập họ và tên nhân viên quản trị mới:');
  if (!name || !name.trim()) return;
  const email = prompt('Nhập email công vụ:');
  const role = prompt('Chọn vai trò (1: Kế toán, 2: Kiểm duyệt viên, 3: CSKH):', '2');
  const roleName = role === '1' ? 'Kế toán & Đối soát' : (role === '3' ? 'Hỗ trợ khách hàng' : 'Kiểm duyệt viên');
  
  adminStaffData.push({
    id: 'STF-0' + (adminStaffData.length + 1),
    name: name.trim(),
    role: roleName,
    email: email || 'nv@glowprestige.vn',
    status: 'active',
    permissions: 'Theo phân cấp vai trò ' + roleName
  });
  renderAdminStaff();
  showToast(`Đã thêm thành công nhân sự ${name.trim()} với vai trò ${roleName}!`, 'fa-circle-check');
}

// --- CÀI ĐẶT HỆ THỐNG ---
function adminSaveSettings() {
  const fee = document.getElementById('inputPlatformFee')?.value || '10';
  const refundWin = document.getElementById('selectRefundWindow')?.value || '2';
  const depRate = document.getElementById('selectDepositRate')?.value || '30';
  showToast(`Đã lưu cấu hình: Phí sàn ${fee}% • Hoàn cọc trước ${refundWin}h • Mức cọc ${depRate}%`, 'fa-circle-check');
}
