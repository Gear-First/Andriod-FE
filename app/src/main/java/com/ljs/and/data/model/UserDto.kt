package com.ljs.and.data.model

data class UserDto(
    val userId: Int,
    val username: String,
    val email: String,
    val rank: String,
    val region: String,
    val workType: String,
    val warehouseName: String? = null
)
