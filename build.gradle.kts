// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.compose) apply false
}

// Đưa thư mục build ra ngoài OneDrive để tránh OneDrive khóa file khi build
// (lỗi "Unable to delete directory ... build\intermediates ...").
// val externalBuildRoot = file("E:/.gradle-builds/app-lam-dep")
// allprojects {
//     layout.buildDirectory.set(File(externalBuildRoot, project.name))
// }