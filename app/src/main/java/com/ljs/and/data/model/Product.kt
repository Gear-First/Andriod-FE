package com.ljs.and.data.model

import com.google.gson.annotations.SerializedName

data class Product(
    @SerializedName("id")
    val id: Long = 0L,
    @SerializedName("lot")
    val lot: String = "",
    @SerializedName("serial")
    val serial: String = "",
    @SerializedName("name")
    val name: String = "",
    @SerializedName("imgUrl")
    val imgUrl: String = ""
)
