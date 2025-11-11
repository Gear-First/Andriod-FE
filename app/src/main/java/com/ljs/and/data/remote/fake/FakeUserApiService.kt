package com.ljs.and.data.remote.fake

import com.ljs.and.data.model.UserDto
import com.ljs.and.data.remote.UserApiService
import kotlinx.coroutines.delay

class FakeUserApiService : UserApiService {

    override suspend fun getUser(token: String, userId: Long): UserDto {
        delay(350)
        return UserDto(
            userId = userId.toInt(),
            username = "이지수",
            email = "ljsoo@gearfirst.com",
            rank = "팀장",
            region = "수원",
            workType = "창고",
            warehouseName = "수원센터"
        )
    }
}
