package com.applamdep.customer.core.util

import com.example.app_lam_dep.R

/**
 * Tiện ích quản lý hình ảnh cửa hàng và dịch vụ làm đẹp.
 * Đảm bảo 100% hình ảnh luôn được tải thành công từ bộ nhớ máy (Offline Drawables)
 * ngay cả khi không có kết nối mạng internet.
 */
object ShopImageAssets {

    /**
     * Lấy resource ID hình ảnh đại diện của từng cửa hàng theo ID hoặc Tên
     */
    fun getShopDrawableRes(shopIdOrName: String?): Int {
        if (shopIdOrName.isNullOrBlank()) return R.drawable.salon_luxury_interior

        val key = shopIdOrName.lowercase()
        return when {
            key == "salon_01" || key.contains("1994") || key.contains("l'amour") || key.contains("l’amour") ->
                R.drawable.salon_01_1994_nailsroom
            key == "salon_02" || key.contains("bonnie") ->
                R.drawable.salon_02_bonnie_nail
            key == "salon_03" || key.contains("gà") || key.contains("ga spa") ->
                R.drawable.salon_03_ga_spa
            key == "salon_04" || key.contains("hien") || key.contains("hienz") ->
                R.drawable.salon_04_hien_hair_studio
            key == "salon_05" || key.contains("kỳ duyên") || key.contains("ky duyen") || key.contains("duyên") ->
                R.drawable.salon_05_ky_duyen_tri_mun
            key == "salon_06" || key.contains("mnh") || key.contains("thuy") || key.contains("thủy") ->
                R.drawable.salon_06_mnh_thuy_makeup
            key == "salon_07" || key.contains("1996") ->
                R.drawable.salon_07_nails_1996
            key == "salon_08" || key.contains("si mê") || key.contains("sime") ->
                R.drawable.salon_08_si_me_beauty
            key == "salon_09" || key.contains("thì là") || key.contains("thi la") || key.contains("an yên") ->
                R.drawable.salon_09_thi_la_spa
            key == "salon_10" || key.contains("tun") ->
                R.drawable.salon_10_tun_beauty_salon
            key == "salon_11" || key.contains("1m65") || key.contains("𝟏𝐦𝟔𝟓") ->
                R.drawable.salon_11_1m65_nail
            else -> R.drawable.salon_01_1994_nailsroom
        }
    }

    /**
     * Lấy resource ID hình ảnh dịch vụ làm đẹp theo ID dịch vụ hoặc tên dịch vụ
     */
    fun getServiceDrawableRes(serviceIdOrName: String?): Int {
        if (serviceIdOrName.isNullOrBlank()) return R.drawable.srv_nail_gel

        val key = serviceIdOrName.lowercase()
        return when {
            key.contains("mắt mèo") || key.contains("art") || key.contains("đá") || key.contains("ombre") || key.contains("anime") || key == "srv_02" || key == "srv_05" || key == "srv_21" || key == "srv_30" ->
                R.drawable.srv_nail_art
            key.contains("nail") || key.contains("gel") || key.contains("móng") || key == "srv_01" || key == "srv_04" || key == "srv_06" || key == "srv_20" || key == "srv_31" ->
                R.drawable.srv_nail_gel
            key.contains("cắt") || key.contains("layer") || key.contains("wolf") || key == "srv_11" || key == "srv_28" ->
                R.drawable.srv_hair_cut
            key.contains("uốn") || key.contains("nhuộm") || key.contains("balayage") || key.contains("tóc") || key == "srv_12" || key == "srv_13" || key == "srv_29" ->
                R.drawable.srv_hair_wave
            key.contains("mụn") || key.contains("vitamin") || key.contains("co2") || key.contains("collagen") || key.contains("triệt") || key.contains("da") || key == "srv_08" || key == "srv_09" || key == "srv_15" || key == "srv_22" || key == "srv_24" ->
                R.drawable.srv_spa_facial
            key.contains("gội") || key.contains("massage") || key.contains("dưỡng sinh") || key.contains("bồ kết") || key.contains("đá nóng") || key == "srv_25" || key == "srv_26" || key == "srv_27" ->
                R.drawable.srv_spa_massage
            key.contains("makeup") || key.contains("cô dâu") || key.contains("dạ tiệc") || key.contains("kỷ yếu") || key == "srv_17" || key == "srv_18" || key == "srv_19" ->
                R.drawable.srv_makeup_bride
            key.contains("mi") || key.contains("mày") || key == "srv_03" ->
                R.drawable.srv_eyelash
            else -> R.drawable.srv_nail_gel
        }
    }
}

