package com.applamdep.customer.data.local

import android.content.Context
import android.content.SharedPreferences
import com.applamdep.customer.domain.model.AccountStatus
import com.applamdep.customer.domain.model.AppUser
import com.applamdep.customer.domain.model.UserRole
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

/**
 * QUẢN LÝ LƯU TRỮ VÀ XÁC THỰC TÀI KHOẢN (LOCAL STORAGE ĐỒNG BỘ BỘ NHỚ MÁY)
 * - Đăng ký chỉ cần Gmail + Mật khẩu
 * - Cấp 3 nhóm tài khoản: Khách hàng - Cửa hàng - Admin
 * - Tài khoản Cửa hàng PHẢI được Admin phê duyệt mới có thể đăng nhập sử dụng
 */
class UserLocalStorage(private val context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("glow_user_accounts_pref", Context.MODE_PRIVATE)
    private val gson = Gson()

    private val KEY_USERS_JSON = "key_users_json"
    private val KEY_LOGGED_IN_EMAIL = "key_logged_in_email"

    sealed interface LoginResult {
        data class Success(val user: AppUser) : LoginResult
        data class MerchantPendingApproval(val message: String) : LoginResult
        data class Error(val message: String) : LoginResult
    }

    sealed interface RegisterResult {
        data class Success(val user: AppUser, val isPendingApproval: Boolean) : RegisterResult
        data class Error(val message: String) : RegisterResult
    }

    init {
        initializeDefaultAccountsIfEmpty()
    }

    /**
     * Khởi tạo sẵn 3 tài khoản mặc định đại diện cho 3 vai trò
     */
    private fun initializeDefaultAccountsIfEmpty() {
        val currentUsers = getAllUsers()
        if (currentUsers.isEmpty()) {
            val defaults = listOf(
                // 1. Tài khoản Admin hệ thống
                AppUser(
                    gmail = "admin@gmail.com",
                    password = "admin",
                    role = UserRole.ADMIN,
                    status = AccountStatus.APPROVED,
                    fullName = "Quản trị viên Cấp cao",
                    phone = "0900 888 999"
                ),
                // 2. Tài khoản Cửa hàng mẫu (Đã duyệt sẵn để kiểm thử)
                AppUser(
                    gmail = "salon@gmail.com",
                    password = "salon",
                    role = UserRole.MERCHANT,
                    status = AccountStatus.APPROVED,
                    fullName = "Chủ tiệm Khánh Linh",
                    salonName = "1994 Nailsroom",
                    phone = "0762792671"
                ),
                // 3. Tài khoản Khách hàng mẫu
                AppUser(
                    gmail = "khach@gmail.com",
                    password = "khach",
                    role = UserRole.CUSTOMER,
                    status = AccountStatus.APPROVED,
                    fullName = "Khánh Linh",
                    phone = "0903 123 456"
                )
            )
            saveAllUsers(defaults)
        }
    }

    /**
     * Lấy danh sách toàn bộ tài khoản trong máy
     */
    fun getAllUsers(): List<AppUser> {
        val json = prefs.getString(KEY_USERS_JSON, null) ?: return emptyList()
        return try {
            val type = object : TypeToken<List<AppUser>>() {}.type
            gson.fromJson(json, type) ?: emptyList()
        } catch (_: Exception) {
            emptyList()
        }
    }

    /**
     * Lưu danh sách tài khoản vào bộ nhớ máy
     */
    private fun saveAllUsers(users: List<AppUser>) {
        val json = gson.toJson(users)
        prefs.edit().putString(KEY_USERS_JSON, json).apply()
    }

    /**
     * Tìm tài khoản theo Gmail
     */
    fun findByGmail(gmail: String): AppUser? {
        val normalized = gmail.trim().lowercase()
        return getAllUsers().firstOrNull { it.gmail.trim().lowercase() == normalized }
    }

    /**
     * ĐĂNG KÝ TÀI KHOẢN MỚI
     * - Chỉ cần nhập Gmail và Mật khẩu
     * - Khách hàng: Dùng được ngay
     * - Cửa hàng: Chờ Admin chấp nhận
     */
    fun register(
        gmail: String,
        password: String,
        role: UserRole,
        salonName: String = ""
    ): RegisterResult {
        val cleanGmail = gmail.trim().lowercase()
        val cleanPass = password.trim()

        // 1. Kiểm tra định dạng Gmail
        if (!cleanGmail.contains("@gmail.com") && !cleanGmail.contains("@")) {
            return RegisterResult.Error("Vui lòng nhập đúng định dạng Gmail (ví dụ: yourname@gmail.com)")
        }

        // 2. Kiểm tra mật khẩu
        if (cleanPass.length < 4) {
            return RegisterResult.Error("Mật khẩu phải có ít nhất 4 ký tự")
        }

        // 3. Kiểm tra trùng Gmail
        if (findByGmail(cleanGmail) != null) {
            return RegisterResult.Error("Gmail này đã được đăng ký trên hệ thống. Vui lòng đăng nhập!")
        }

        // 4. Thiết lập trạng thái theo yêu cầu:
        // Cửa hàng bắt buộc phải PENDING_APPROVAL chờ Admin chấp nhận
        val initialStatus = when (role) {
            UserRole.MERCHANT -> AccountStatus.PENDING_APPROVAL
            UserRole.CUSTOMER -> AccountStatus.APPROVED
            UserRole.ADMIN -> AccountStatus.APPROVED
        }

        val newUser = AppUser(
            gmail = cleanGmail,
            password = cleanPass,
            role = role,
            status = initialStatus,
            fullName = if (role == UserRole.MERCHANT) salonName.ifBlank { "Chủ Cửa Hàng" } else "Khách Hàng",
            salonName = salonName,
            createdAt = System.currentTimeMillis()
        )

        val updatedList = getAllUsers().toMutableList().apply { add(newUser) }
        saveAllUsers(updatedList)

        return RegisterResult.Success(
            user = newUser,
            isPendingApproval = initialStatus == AccountStatus.PENDING_APPROVAL
        )
    }

    /**
     * ĐĂNG NHẬP
     * - Kiểm tra mật khẩu
     * - Kiểm tra quyền phê duyệt của Admin đối với tài khoản Cửa hàng
     */
    fun login(gmail: String, password: String): LoginResult {
        val cleanGmail = gmail.trim().lowercase()
        val cleanPass = password.trim()

        val user = findByGmail(cleanGmail)
            ?: return LoginResult.Error("Không tìm thấy tài khoản Gmail này. Vui lòng đăng ký!")

        if (user.password != cleanPass) {
            return LoginResult.Error("Mật khẩu không chính xác. Vui lòng thử lại!")
        }

        // RÀNG BUỘC THEO YÊU CẦU:
        // Tài khoản cửa hàng phải được Admin chấp nhận mới có thể sử dụng
        if (user.role == UserRole.MERCHANT) {
            when (user.status) {
                AccountStatus.PENDING_APPROVAL -> {
                    return LoginResult.MerchantPendingApproval(
                        "⚠️ Tài khoản Cửa hàng (${user.gmail}) đang trong trạng thái CHỜ ADMIN PHÊ DUYỆT.\n\nVui lòng chờ Admin chấp nhận hồ sơ mở sàn để bắt đầu sử dụng!"
                    )
                }
                AccountStatus.REJECTED -> {
                    return LoginResult.Error(
                        "❌ Hồ sơ đăng ký Cửa hàng này đã bị Admin từ chối xét duyệt. Vui lòng liên hệ bộ phận hỗ trợ!"
                    )
                }
                AccountStatus.LOCKED -> {
                    return LoginResult.Error(
                        "🔒 Tài khoản Cửa hàng này hiện đang bị tạm khóa."
                    )
                }
                AccountStatus.APPROVED -> {
                    // Đã duyệt -> Cho phép vào
                }
            }
        } else if (user.status == AccountStatus.LOCKED) {
            return LoginResult.Error("Tài khoản của bạn hiện đang bị tạm khóa.")
        }

        // Ghi nhận phiên đăng nhập
        saveCurrentLoggedInEmail(user.gmail)
        return LoginResult.Success(user)
    }

    /**
     * PHÊ DUYỆT TÀI KHOẢN CỬA HÀNG (Dành cho Admin)
     */
    fun approveMerchant(gmail: String): Boolean {
        val users = getAllUsers().toMutableList()
        val index = users.indexOfFirst { it.gmail.trim().lowercase() == gmail.trim().lowercase() }
        if (index != -1) {
            val old = users[index]
            users[index] = old.copy(status = AccountStatus.APPROVED)
            saveAllUsers(users)
            return true
        }
        return false
    }

    /**
     * TỪ CHỐI TÀI KHOẢN CỬA HÀNG (Dành cho Admin)
     */
    fun rejectMerchant(gmail: String): Boolean {
        val users = getAllUsers().toMutableList()
        val index = users.indexOfFirst { it.gmail.trim().lowercase() == gmail.trim().lowercase() }
        if (index != -1) {
            val old = users[index]
            users[index] = old.copy(status = AccountStatus.REJECTED)
            saveAllUsers(users)
            return true
        }
        return false
    }

    /**
     * KHÓA / MỞ KHÓA TÀI KHOẢN (Áp dụng cho cả Khách hàng và Cửa hàng)
     */
    fun toggleUserLock(gmail: String): AccountStatus? {
        val users = getAllUsers().toMutableList()
        val index = users.indexOfFirst { it.gmail.trim().lowercase() == gmail.trim().lowercase() }
        if (index != -1) {
            val old = users[index]
            val newStatus = if (old.status == AccountStatus.LOCKED) AccountStatus.APPROVED else AccountStatus.LOCKED
            users[index] = old.copy(status = newStatus)
            saveAllUsers(users)
            return newStatus
        }
        return null
    }

    /**
     * Lấy danh sách các tài khoản Cửa hàng đang chờ Admin duyệt
     */
    fun getPendingMerchants(): List<AppUser> {
        return getAllUsers().filter { it.role == UserRole.MERCHANT && it.status == AccountStatus.PENDING_APPROVAL }
    }

    fun saveCurrentLoggedInEmail(gmail: String) {
        prefs.edit().putString(KEY_LOGGED_IN_EMAIL, gmail).apply()
    }

    fun getCurrentLoggedInUser(): AppUser? {
        val email = prefs.getString(KEY_LOGGED_IN_EMAIL, null) ?: return null
        return findByGmail(email)
    }

    fun logout() {
        prefs.edit().remove(KEY_LOGGED_IN_EMAIL).apply()
    }
}

