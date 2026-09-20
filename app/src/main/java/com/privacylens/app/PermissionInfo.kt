package com.privacylens.app

enum class PermissionCategory {
    SENSITIVE,
    SPECIAL,
    OTHER
}

data class PermissionInfo(
    val permission: String,
    val name: String,
    val description: String,
    val granted: Boolean,
    val category: PermissionCategory
)
