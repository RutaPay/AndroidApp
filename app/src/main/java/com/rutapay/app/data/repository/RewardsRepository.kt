package com.rutapay.app.data.repository

import android.util.Log
import com.rutapay.app.data.model.RedeemRewardDto
import com.rutapay.app.data.model.RedeemResultDto
import com.rutapay.app.data.model.RewardDto
import com.rutapay.app.data.model.UserRewardDto
import com.rutapay.app.data.remote.ApiClient
import com.rutapay.app.ui.theme.WebErrors
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.isSuccess

class RewardsRepository(
    private val apiClient: ApiClient
) {
    suspend fun getRewards(): Result<List<RewardDto>> {
        return try {
            val base = apiClient.baseUrl()
            val response = apiClient.client.get("$base/api/rewards")
            if (response.status.isSuccess()) {
                try {
                    Result.success(apiClient.parseAndLog<List<RewardDto>>(response, "RewardsRepo", "GET rewards"))
                } catch (e: Exception) {
                    Log.e("RewardsRepo", "getRewards: parseo fallido", e)
                    Result.failure(Exception(WebErrors.NETWORK_ERROR))
                }
            } else {
                val body = response.bodyAsText()
                Log.w("RewardsRepo", "getRewards failed ${response.status}: ${body.take(300)}")
                Result.failure(Exception(WebErrors.NETWORK_ERROR))
            }
        } catch (e: Exception) {
            Log.e("RewardsRepo", "getRewards error", e)
            Result.failure(Exception(WebErrors.NETWORK_ERROR))
        }
    }

    suspend fun getMyRewards(): Result<List<UserRewardDto>> {
        return try {
            val base = apiClient.baseUrl()
            val response = apiClient.client.get("$base/api/rewards/my-rewards")
            if (response.status.isSuccess()) {
                try {
                    Result.success(apiClient.parseAndLog<List<UserRewardDto>>(response, "RewardsRepo", "GET my-rewards"))
                } catch (e: Exception) {
                    Log.e("RewardsRepo", "getMyRewards: parseo fallido", e)
                    Result.success(emptyList())
                }
            } else {
                Result.success(emptyList())
            }
        } catch (e: Exception) {
            Log.e("RewardsRepo", "getMyRewards error", e)
            Result.success(emptyList())
        }
    }

    suspend fun redeem(rewardId: Int): Result<RedeemResultDto> {
        return try {
            val base = apiClient.baseUrl()
            val response = apiClient.client.post("$base/api/rewards/redeem") {
                setBody(RedeemRewardDto(rewardId))
            }
            if (response.status.isSuccess()) {
                Result.success(response.body<RedeemResultDto>())
            } else {
                val msg = response.bodyAsText().ifBlank { WebErrors.NETWORK_ERROR }
                Result.failure(Exception(msg))
            }
        } catch (e: Exception) {
            Log.e("RewardsRepo", "redeem error", e)
            Result.failure(Exception(WebErrors.REDEEM_NO_CONNECTION))
        }
    }
}
