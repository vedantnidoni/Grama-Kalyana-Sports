package com.grama_kalyanasports

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.grama_kalyanasports.ui.theme.GramaKalyanaSportsTheme
import kotlinx.coroutines.launch

class ScorecardActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val tournamentKey = intent.getStringExtra("tournamentKey") ?: ""
        setContent {
            GramaKalyanaSportsTheme {
                ScorecardScreen(
                    tournamentKey = tournamentKey,
                    onBack = { finish() }
                )
            }
        }
    }
}

@Composable
fun ScorecardScreen(tournamentKey: String, onBack: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var team1 by remember { mutableStateOf("Team 1") }
    var team2 by remember { mutableStateOf("Team 2") }
    var score1 by remember { mutableStateOf(0) }
    var score2 by remember { mutableStateOf(0) }
    var sport by remember { mutableStateOf("Cricket") }
    var manOfMatch by remember { mutableStateOf("Loading...") }
    var commentary by remember { mutableStateOf("") }
    var scorecard by remember { mutableStateOf("") }
    var isGenerating by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(true) }

    // 🔥 Load tournament data from Firebase
    LaunchedEffect(tournamentKey) {
        FirebaseHelper.tournamentsRef
            .child(tournamentKey)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    team1 = snapshot.child("team1").getValue(String::class.java) ?: "Team 1"
                    team2 = snapshot.child("team2").getValue(String::class.java) ?: "Team 2"
                    score1 = snapshot.child("score1").getValue(Int::class.java) ?: 0
                    score2 = snapshot.child("score2").getValue(Int::class.java) ?: 0
                    sport = snapshot.child("sport").getValue(String::class.java) ?: "Cricket"
                    isLoading = false
                }
                override fun onCancelled(error: DatabaseError) { isLoading = false }
            })

        // Load Man of Match
        FirebaseHelper.playersRef
            .child(tournamentKey)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    var topPlayer = ""
                    var topPoints = 0
                    for (child in snapshot.children) {
                        val points = child.child("points").getValue(Int::class.java) ?: 0
                        val name = child.child("name").getValue(String::class.java) ?: ""
                        if (points > topPoints) {
                            topPoints = points
                            topPlayer = "$name ($points pts)"
                        }
                    }
                    manOfMatch = topPlayer.ifEmpty { "Not assigned yet" }
                }
                override fun onCancelled(error: DatabaseError) {}
            })
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF1F8E9))
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(32.dp))

        Text(text = "📋", fontSize = 48.sp)
        Text(
            text = "Match Scorecard",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1B5E20)
        )
        Text(
            text = tournamentKey.replace("_", " "),
            fontSize = 13.sp,
            color = Color(0xFF558B2F),
            modifier = Modifier.padding(top = 4.dp, bottom = 24.dp)
        )

        if (isLoading) {
            CircularProgressIndicator(color = Color(0xFF2E7D32))
        } else {

            // Score Summary Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF2E7D32)),
                elevation = CardDefaults.cardElevation(8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = when (sport) {
                            "Volleyball" -> "🏐"
                            "Kabaddi" -> "🤼"
                            else -> "🏏"
                        },
                        fontSize = 32.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = team1,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "$score1",
                                fontSize = 56.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (score1 > score2) Color(0xFFFFD700) else Color.White
                            )
                        }
                        Text(
                            text = "VS",
                            fontSize = 18.sp,
                            color = Color(0xFF81C784),
                            fontWeight = FontWeight.Bold
                        )
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = team2,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "$score2",
                                fontSize = 56.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (score2 > score1) Color(0xFFFFD700) else Color.White
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(color = Color(0xFF81C784).copy(alpha = 0.4f))
                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = when {
                            score1 > score2 -> "🏆 $team1 Wins!"
                            score2 > score1 -> "🏆 $team2 Wins!"
                            else -> "⚖️ It's a Tie!"
                        },
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFFD700)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ⭐ Man of Match Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFD700)),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "⭐", fontSize = 28.sp)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "MAN OF THE MATCH",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF5D4037),
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = manOfMatch,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1B5E20)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 🤖 AI Commentary Result
            if (commentary.isNotEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
                    elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "🤖 AI Commentary",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1B5E20)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = commentary,
                            fontSize = 14.sp,
                            color = Color(0xFF33691E),
                            lineHeight = 20.sp
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // 🤖 Generate AI Commentary Button
            Button(
                onClick = {
                    isGenerating = true
                    scope.launch {
                        commentary = GeminiHelper.generateCommentary(
                            sport = sport,
                            team1 = team1,
                            team2 = team2,
                            score1 = score1,
                            score2 = score2,
                            manOfMatch = manOfMatch
                        )
                        scorecard = GeminiHelper.generateScorecard(
                            sport = sport,
                            tournamentName = tournamentKey.replace("_", " "),
                            team1 = team1,
                            team2 = team2,
                            score1 = score1,
                            score2 = score2,
                            manOfMatch = manOfMatch,
                            commentary = commentary
                        )
                        isGenerating = false
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF1565C0)
                ),
                enabled = !isGenerating
            ) {
                Text(
                    text = if (isGenerating) "🤖 Generating..." else "🤖 Generate AI Commentary",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 📤 Share on WhatsApp Button
            Button(
                onClick = {
                    val finalScorecard = if (scorecard.isNotEmpty()) scorecard else
                        GeminiHelper.generateScorecard(
                            sport = sport,
                            tournamentName = tournamentKey.replace("_", " "),
                            team1 = team1,
                            team2 = team2,
                            score1 = score1,
                            score2 = score2,
                            manOfMatch = manOfMatch,
                            commentary = "Great match played between $team1 and $team2!"
                        )
                    val intent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        setPackage("com.whatsapp")
                        putExtra(Intent.EXTRA_TEXT, finalScorecard)
                    }
                    try {
                        context.startActivity(intent)
                    } catch (e: Exception) {
                        val fallback = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_TEXT, finalScorecard)
                        }
                        context.startActivity(Intent.createChooser(fallback, "Share Scorecard"))
                    }
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
                    text = "📤 Share on WhatsApp",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedButton(
                onClick = onBack,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("← Back", color = Color(0xFF2E7D32))
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}