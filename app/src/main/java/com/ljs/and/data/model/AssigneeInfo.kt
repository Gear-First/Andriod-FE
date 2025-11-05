package com.ljs.and.data.model

import com.google.gson.annotations.SerializedName

data class AssigneeInfo(
    @SerializedName("assigneeName")
    val assigneeName: String,
    @SerializedName("assigneeDept")
    val assigneeDept: String,
    @SerializedName("assigneePhone")
    val assigneePhone: String
)
