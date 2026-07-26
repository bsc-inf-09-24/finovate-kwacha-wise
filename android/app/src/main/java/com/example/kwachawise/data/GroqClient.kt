package com.example.kwachawise.data

import android.util.Log
import com.example.kwachawise.BuildConfig
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

data class GroqRequest(
    val model: String = "llama-3.3-70b-versatile",
    val messages: List<GroqMessage>
)

data class GroqMessage(
    val role: String,
    val content: String
)

data class GroqResponse(
    val choices: List<Choice>
)

data class Choice(
    val message: GroqMessage
)

interface GroqService {
    @POST("v1/chat/completions")
    suspend fun getCompletion(
        @Header("Authorization") apiKey: String,
        @Body request: GroqRequest
    ): GroqResponse
}

object GroqClient {
    private const val TAG = "GroqClient"
    private const val BASE_URL = "https://api.groq.com/openai/"
    // Pulled from git-ignored keystore.properties via BuildConfig
    private val API_KEY = "Bearer ${BuildConfig.GROQ_API_KEY}"

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val service = retrofit.create(GroqService::class.java)

    suspend fun getFinancialInsights(transactionSummary: String): String? {
        val prompt = """
            You are a financial advisor for a small business owner in Malawi. 
            Analyze the following transaction summary and provide:
            1. A health signal (Healthy, Watch, or Danger).
            2. 3 actionable recommendations to improve financial health.
            
            Return the response in this exact format:
            SIGNAL: [Healthy/Watch/Danger]
            ADVICE: [Recommendation 1]
            ADVICE: [Recommendation 2]
            ADVICE: [Recommendation 3]
            
            Transactions:
            $transactionSummary
        """.trimIndent()

        return try {
            val response = service.getCompletion(
                API_KEY,
                GroqRequest(messages = listOf(GroqMessage(role = "user", content = prompt)))
            )
            response.choices.firstOrNull()?.message?.content
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching insights from Groq", e)
            null
        }
    }

    suspend fun generateBankReport(
        businessName: String,
        period: String,
        transactionSummary: String
    ): String? {
        val prompt = """
            You are a professional accountant. Generate a formal financial report for a bank.
            The report should be for the business "$businessName" for the period "$period".
            
            Use the following transaction data:
            $transactionSummary
            
            The report MUST include:
            1. A clear header: "FINANCIAL PERFORMANCE REPORT"
            2. Business Name and Period.
            3. Executive Summary (2-3 sentences).
            4. Income Statement Summary (Total Income, Total Expenses, Net Profit/Loss).
            5. Category Breakdown (e.g., Business Expenses vs Personal/Other).
            6. A formal conclusion regarding the business's creditworthiness or financial health.
            
            Format the report with professional language, using bullet points and clear sections.
            Avoid any conversational filler. Start directly with the report.
        """.trimIndent()

        return try {
            val response = service.getCompletion(
                API_KEY,
                GroqRequest(messages = listOf(GroqMessage(role = "user", content = prompt)))
            )
            response.choices.firstOrNull()?.message?.content
        } catch (e: Exception) {
            Log.e(TAG, "Error generating bank report from Groq", e)
            null
        }
    }
}
