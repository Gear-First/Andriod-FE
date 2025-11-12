package com.ljs.and.data.model

import com.google.gson.annotations.SerializedName

data class PaginatedData<T>(
    @SerializedName("content")
    val content: List<T>,
    @SerializedName("pageNumber")
    val pageNumber: Int,
    @SerializedName("pageSize")
    val pageSize: Int,
    @SerializedName("totalElements")
    val totalElements: Long,
    @SerializedName("totalPages")
    val totalPages: Int
)