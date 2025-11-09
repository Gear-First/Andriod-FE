package com.ljs.and.data.model

import com.google.gson.annotations.SerializedName

data class UserInfo(
    @SerializedName("sub")
    val sub: String?
)
