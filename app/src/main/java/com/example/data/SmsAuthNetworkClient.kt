package com.example.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

object SmsAuthNetworkClient {
    private const val BASE_URL = "https://ais-pre-y2nw5fuzaehiyzh3k2sxfw-712942562588.asia-east1.run.app"

    data class SendOtpResult(
        val success: Boolean,
        val maskedPhone: String? = null,
        val cooldownSeconds: Int = 60,
        val expiresInSeconds: Int = 300,
        val error: String? = null
    )

    data class VerifyOtpResult(
        val success: Boolean,
        val token: String? = null,
        val error: String? = null
    )

    suspend fun sendOtp(phone: String, name: String): SendOtpResult = withContext(Dispatchers.IO) {
        try {
            val url = URL("$BASE_URL/api/auth/send-otp")
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "POST"
            conn.setRequestProperty("Content-Type", "application/json")
            conn.doOutput = true
            conn.connectTimeout = 8000
            conn.readTimeout = 8000

            val json = JSONObject().apply {
                put("phone", phone)
                put("name", name)
            }

            OutputStreamWriter(conn.outputStream).use { it.write(json.toString()) }

            val stream = if (conn.responseCode in 200..299) conn.inputStream else conn.errorStream
            val respText = if (stream != null) BufferedReader(InputStreamReader(stream)).use { it.readText() } else ""

            if (respText.trim().startsWith("<") || !respText.trim().startsWith("{")) {
                return@withContext SendOtpResult(
                    success = false,
                    error = "SMS service offline. Please tap 'Browse as Guest' to order directly without OTP!"
                )
            }

            val respJson = JSONObject(respText)

            if (conn.responseCode in 200..299 && respJson.optBoolean("success", false)) {
                SendOtpResult(
                    success = true,
                    maskedPhone = respJson.optString("maskedPhone", "+91 $phone"),
                    cooldownSeconds = respJson.optInt("cooldownSeconds", 60),
                    expiresInSeconds = respJson.optInt("expiresInSeconds", 300)
                )
            } else {
                SendOtpResult(
                    success = false,
                    error = respJson.optString("error", "Failed to send SMS OTP")
                )
            }
        } catch (e: Exception) {
            SendOtpResult(success = false, error = e.localizedMessage ?: "Network connection failed")
        }
    }

    suspend fun verifyOtp(phone: String, otp: String, name: String): VerifyOtpResult = withContext(Dispatchers.IO) {
        try {
            val url = URL("$BASE_URL/api/auth/verify-otp")
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "POST"
            conn.setRequestProperty("Content-Type", "application/json")
            conn.doOutput = true
            conn.connectTimeout = 8000
            conn.readTimeout = 8000

            val json = JSONObject().apply {
                put("phone", phone)
                put("otp", otp)
                put("name", name)
            }

            OutputStreamWriter(conn.outputStream).use { it.write(json.toString()) }

            val stream = if (conn.responseCode in 200..299) conn.inputStream else conn.errorStream
            val respText = if (stream != null) BufferedReader(InputStreamReader(stream)).use { it.readText() } else ""

            if (respText.trim().startsWith("<") || !respText.trim().startsWith("{")) {
                return@withContext VerifyOtpResult(
                    success = false,
                    error = "Verification service offline. Please continue as Guest."
                )
            }

            val respJson = JSONObject(respText)

            if (conn.responseCode in 200..299 && respJson.optBoolean("success", false)) {
                VerifyOtpResult(
                    success = true,
                    token = respJson.optString("token", "")
                )
            } else {
                VerifyOtpResult(
                    success = false,
                    error = respJson.optString("error", "Incorrect OTP code")
                )
            }
        } catch (e: Exception) {
            VerifyOtpResult(success = false, error = e.localizedMessage ?: "Verification network failed")
        }
    }
}
