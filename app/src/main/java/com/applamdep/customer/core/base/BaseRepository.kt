package com.applamdep.customer.core.base

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * 2. TÍNH CHẤT OOP: KẾ THỪA (INHERITANCE)
 * BaseRepository là lớp cha cung cấp các phương thức dùng chung cho tầng Data (Repository Implementation),
 * bao bọc các cuộc gọi I/O từ Firebase/Retrofit vào Result<T> và chuyển thread Dispatchers.IO an toàn.
 */
abstract class BaseRepository(
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    /**
     * Bọc các tác vụ gọi mạng/cơ sở dữ liệu an toàn, tự động bắt Exception
     */
    protected suspend fun <T> safeCall(apiCall: suspend () -> T): Result<T> {
        return withContext(ioDispatcher) {
            try {
                Result.success(apiCall())
            } catch (throwable: Throwable) {
                Result.failure(throwable)
            }
        }
    }
}
