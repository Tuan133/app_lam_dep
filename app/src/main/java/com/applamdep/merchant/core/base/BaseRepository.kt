package com.applamdep.merchant.core.base

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * 2. TÍNH CHẤT OOP: KẾ THỪA (INHERITANCE)
 * Lớp cha BaseRepository đóng gói các thao tác gọi dữ liệu ngoại vi an toàn (IO Dispatcher, try-catch Result).
 */
abstract class BaseRepository(
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    protected suspend fun <R> safeCall(call: suspend () -> R): Result<R> {
        return withContext(ioDispatcher) {
            try {
                Result.success(call())
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
}
