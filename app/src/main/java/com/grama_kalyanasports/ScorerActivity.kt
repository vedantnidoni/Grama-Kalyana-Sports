package com.grama_kalyanasports

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.grama_kalyanasports.ui.theme.GramaKalyanaSportsTheme

class ScorerActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val tournamentKey = intent.getStringExtra("tournamentKey") ?: ""
        val team1 = intent.getStringExtra("team1") ?: "Team 1"
        val team2 = intent.getStringExtra("team2") ?: "Team 2"
        val sport = intent.getStringExtra("sport") ?: "Volleyball"

        setContent {
            GramaKalyanaSportsTheme {
                ScorerScreen(
                    tournamentKey = tournamentKey,
                    team1 = team1,
                    team2 = team2,
                    sport = sport,
                    onBack = { finish() }
                )
            }
        }
    }
}

@Composable
fun ScorerScreen(
    tournamentKey: String,
    team1: String,
    team2: String,
    sport: String,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var score1 by remember { mutableStateOf(0) }
    var score2 by remember { mutableStateOf(0) }
    var statusMessage by remember { mutableStateOf("") }

    fun updateScore() {
        FirebaseHelper.tournamentsRef
            .child(tournamentKey)
            .updateChildren(mapOf("score1" to score1, "score2" to score2))
            .addOnSuccessListener {
                statusMessage = "✅ Score updated!"
            }
            .addOnFailureListener {
                statusMessage = "❌ Update failed!"
            }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1B5E20))
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(32.dp))

        // Sport Icon
        Text(
            text = when (sport) {
                "Volleyball" -> "🏐"
                "Kabaddi" -> "🤼"
                else -> "🏏"
            },
            fontSize = 40.sp
        )

        Text(
            text = "LIVE SCORER",
            fontSize = 14.sp,
            color = Color(0xFF81C784),
            fontWeight = FontWeight.Bold,
            letterSpacing = 4.sp
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Scoreboard Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF2E7D32))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Team 1
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = team1,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "$score1",
                        fontSize = 72.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Row {
                        IconButton(onClick = {
                            if (score1 > 0) {
                                score1--
                                updateScore()
                            }
                        }) {
                            Text("➖", fontSize = 24.sp)
                        }
                        IconButton(onClick = {
                            score1++
                            updateScore()
                        }) {
                            Text("➕", fontSize = 24.sp)
                        }
                    }
                }

                // VS
                Text(
                    text = "VS",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF81C784)
                )

                // Team 2
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = team2,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "$score2",
                        fontSize = 72.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Row {
                        IconButton(onClick = {
                            if (score2 > 0) {
                                score2--
                                updateScore()
                            }
                        }) {
                            Text("➖", fontSize = 24.sp)
                        }
                        IconButton(onClick = {
                            score2++
                            updateScore()
                        }) {
                            Text("➕", fontSize = 24.sp)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Status Message
        if (statusMessage.isNotEmpty()) {
            Text(
                text = statusMessage,
                color = Color(0xFF81C784),
                fontSize = 14.sp
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 🏅 Player Stats Button
        Button(
            onClick = {
                val intent = Intent(context, PlayerStatsActivity::class.java)
                intent.putExtra("tournamentKey", tournamentKey)
                intent.putExtra("team1", team1)
                intent.putExtra("team2", team2)
                context.startActivity(intent)
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFFFD700)
            )
        ) {
            Text(
                text = "🏅 Player Stats & Man of Match",
                color = Color(0xFF1B5E20),
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 📋 Scorecard & WhatsApp Button
        Button(
            onClick = {
                val intent = Intent(context, ScorecardActivity::class.java)
                intent.putExtra("tournamentKey", tournamentKey)
                context.startActivity(intent)
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF25D366)
            )
        ) {
            Text(
                text = "📋 Scorecard & WhatsApp Share",
                color = Color.White,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 🔄 Reset Button
        OutlinedButton(
            onClick = {
                score1 = 0
                score2 = 0
                updateScore()
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = Color.White
            )
        ) {
            Text("🔄 Reset Scores")
        }

        Spacer(modifier = Modifier.height(12.dp))

        // ← Back Button
        OutlinedButton(
            onClick = onBack,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = Color.White
            )
        ) {
            Text("← Back")
        }
    }
}