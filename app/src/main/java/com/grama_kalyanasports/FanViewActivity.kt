package com.grama_kalyanasports

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.grama_kalyanasports.ui.theme.GramaKalyanaSportsTheme

class FanViewActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val tournamentKey = intent.getStringExtra("tournamentKey") ?: ""

        setContent {
            GramaKalyanaSportsTheme {
                FanViewScreen(
                    tournamentKey = tournamentKey,
                    onBack = { finish() }
                )
            }
        }
    }
}

@Composable
fun FanViewScreen(tournamentKey: String, onBack: () -> Unit) {
    var team1 by remember { mutableStateOf("Team 1") }
    var team2 by remember { mutableStateOf("Team 2") }
    var score1 by remember { mutableStateOf(0) }
    var score2 by remember { mutableStateOf(0) }
    var sport by remember { mutableStateOf("") }
    var status by remember { mutableStateOf("live") }
    var isLoading by remember { mutableStateOf(true) }

    // 🔥 Listen to Firebase in real-time
    LaunchedEffect(tournamentKey) {
        FirebaseHelper.tournamentsRef
            .child(tournamentKey)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    team1 = snapshot.child("team1").getValue(String::class.java) ?: "Team 1"
                    team2 = snapshot.child("team2").getValue(String::class.java) ?: "Team 2"
                    score1 = snapshot.child("score1").getValue(Int::class.java) ?: 0
                    score2 = snapshot.child("score2").getValue(Int::class.java) ?: 0
                    sport = snapshot.child("sport").getValue(String::class.java) ?: ""
                    status = snapshot.child("status").getValue(String::class.java) ?: "live"
                    isLoading = false
                }
                override fun onCancelled(error: DatabaseError) {
                    isLoading = false
                }
            })
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1B5E20))
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(32.dp))

        // LIVE badge
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFD32F2F))
        ) {
            Text(
                text = "  🔴 LIVE  ",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = when (sport) {
                "Volleyball" -> "🏐"
                "Kabaddi" -> "🤼"
                else -> "🏏"
            },
            fontSize = 40.sp
        )

        Text(
            text = "FAN VIEW",
            fontSize = 14.sp,
            color = Color(0xFF81C784),
            fontWeight = FontWeight.Bold,
            letterSpacing = 4.sp
        )

        Spacer(modifier = Modifier.height(32.dp))

        if (isLoading) {
            CircularProgressIndicator(color = Color.White)
            Text(
                text = "Loading live score...",
                color = Color.White,
                modifier = Modifier.padding(top = 16.dp)
            )
        } else {
            // Scoreboard
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF2E7D32)),
                elevation = CardDefaults.cardElevation(8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Team 1
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = team1,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text = "$score1",
                                fontSize = 80.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (score1 > score2) Color(0xFFFFD700) else Color.White
                            )
                        }

                        // VS
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "VS",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF81C784)
                            )
                        }

                        // Team 2
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = team2,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text = "$score2",
                                fontSize = 80.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (score2 > score1) Color(0xFFFFD700) else Color.White
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Divider(color = Color(0xFF81C784).copy(alpha = 0.3f))
                    Spacer(modifier = Modifier.height(12.dp))

                    // Winner indicator
                    val winnerText = when {
                        score1 > score2 -> "🏆 $team1 is Leading!"
                        score2 > score1 -> "🏆 $team2 is Leading!"
                        else -> "⚖️ It's a Tie!"
                    }
                    Text(
                        text = winnerText,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFFD700),
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Auto-refresh note
            Text(
                text = "⚡ Score updates automatically",
                fontSize = 13.sp,
                color = Color(0xFF81C784),
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        // Back Button
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