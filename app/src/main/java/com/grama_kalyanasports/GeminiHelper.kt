package com.grama_kalyanasports

import com.google.ai.client.generativeai.GenerativeModel

object GeminiHelper {

    // ✅ Your Gemini API Key
    private const val API_KEY = "AIzaSyCNDMa3Ixfv_sK0ZVNIdSE4xDF19Y_Q7Pg"

    private val model = GenerativeModel(
        modelName = "gemini-pro",
        apiKey = API_KEY
    )

    suspend fun generateCommentary(
        sport: String,
        team1: String,
        team2: String,
        score1: Int,
        score2: Int,
        manOfMatch: String
    ): String {
        return try {
            val prompt = """
                Generate a short exciting match commentary (3-4 sentences) for a village level $sport match.
                Team 1: $team1 scored $score1 points.
                Team 2: $team2 scored $score2 points.
                Man of the Match: $manOfMatch
                Make it feel like a professional sports commentary.
                End with who won or is leading.
                Keep it simple and exciting!
            """.trimIndent()

            val response = model.generateContent(prompt)
            response.text ?: "Great match between $team1 and $team2!"
        } catch (e: Exception) {
            "🏆 What a match! $team1 scored $score1 vs $team2 scored $score2. Man of the Match: $manOfMatch!"
        }
    }

    fun generateScorecard(
        sport: String,
        tournamentName: String,
        team1: String,
        team2: String,
        score1: Int,
        score2: Int,
        manOfMatch: String,
        commentary: String
    ): String {
        val winner = when {
            score1 > score2 -> "🏆 Winner: $team1"
            score2 > score1 -> "🏆 Winner: $team2"
            else -> "⚖️ It's a Tie!"
        }
        return """
🏆 *GRAMA-KALYANA SPORTS*
━━━━━━━━━━━━━━━━━━
🎮 *$sport Match Scorecard*
📋 Tournament: $tournamentName

⚔️ *$team1  $score1 - $score2  $team2*

$winner

⭐ *Man of the Match:* $manOfMatch

📝 *Match Summary:*
$commentary

━━━━━━━━━━━━━━━━━━
📱 Powered by Grama-Kalyana Sports App
        """.trimIndent()
    }
}