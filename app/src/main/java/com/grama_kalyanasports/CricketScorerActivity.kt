package com.grama_kalyanasports

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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

class CricketScorerActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val tournamentKey = intent.getStringExtra("tournamentKey") ?: ""
        val team1 = intent.getStringExtra("team1") ?: "Team A"
        val team2 = intent.getStringExtra("team2") ?: "Team B"

        setContent {
            GramaKalyanaSportsTheme {
                CricketScorerScreen(
                    tournamentKey = tournamentKey,
                    team1 = team1,
                    team2 = team2,
                    onBack = { finish() }
                )
            }
        }
    }
}

@Composable
fun CricketScorerScreen(
    tournamentKey: String,
    team1: String,
    team2: String,
    onBack: () -> Unit
) {
    val context = LocalContext.current

    // Batting team state
    var battingTeam by remember { mutableStateOf(1) } // 1 = team1, 2 = team2
    var inning by remember { mutableStateOf(1) }

    // Team 1 innings
    var runs1 by remember { mutableStateOf(0) }
    var wickets1 by remember { mutableStateOf(0) }
    var balls1 by remember { mutableStateOf(0) }
    var extras1 by remember { mutableStateOf(0) }

    // Team 2 innings
    var runs2 by remember { mutableStateOf(0) }
    var wickets2 by remember { mutableStateOf(0) }
    var balls2 by remember { mutableStateOf(0) }
    var extras2 by remember { mutableStateOf(0) }

    // Current over balls (for over display)
    var currentOverBalls by remember { mutableStateOf(mutableListOf<String>()) }

    // Match state
    var matchOver by remember { mutableStateOf(false) }
    var matchResult by remember { mutableStateOf("") }
    var statusMessage by remember { mutableStateOf("") }
    var showInningSwitch by remember { mutableStateOf(false) }

    // Current batting values
    val currentRuns = if (inning == 1) runs1 else runs2
    val currentWickets = if (inning == 1) wickets1 else wickets2
    val currentBalls = if (inning == 1) balls1 else balls2
    val currentExtras = if (inning == 1) extras1 else extras2
    val currentTeam = if (inning == 1) team1 else team2
    val bowlingTeam = if (inning == 1) team2 else team1

    val overs = currentBalls / 6
    val ballsInOver = currentBalls % 6
    val maxOvers = 20
    val maxWickets = 10

    // Check if innings over
    fun checkInningsOver(): Boolean {
        val w = if (inning == 1) wickets1 else wickets2
        val b = if (inning == 1) balls1 else balls2
        return w >= maxWickets || b >= maxOvers * 6
    }

    // Check match result
    fun checkMatchResult() {
        if (inning == 2) {
            when {
                runs2 > runs1 -> matchResult = "🏆 $team2 Wins by ${maxWickets - wickets2} wickets!"
                runs1 > runs2 -> matchResult = "🏆 $team1 Wins by ${runs1 - runs2} runs!"
                else -> matchResult = "⚖️ It's a Tie!"
            }
            matchOver = true
        }
    }

    // Update Firebase
    fun updateFirebase() {
        val data = mapOf(
            "team1" to team1,
            "team2" to team2,
            "sport" to "Cricket",
            "runs1" to runs1,
            "wickets1" to wickets1,
            "balls1" to balls1,
            "runs2" to runs2,
            "wickets2" to wickets2,
            "balls2" to balls2,
            "inning" to inning,
            "status" to if (matchOver) "finished" else "live"
        )
        FirebaseHelper.tournamentsRef
            .child(tournamentKey)
            .updateChildren(data)
    }

    // Add runs function
    fun addRuns(r: Int) {
        if (matchOver || showInningSwitch) return
        if (inning == 1) { runs1 += r; balls1++ }
        else { runs2 += r; balls2++ }
        currentOverBalls = currentOverBalls.toMutableList().also { it.add("$r") }
        if (currentOverBalls.size >= 6) currentOverBalls = mutableListOf()
        statusMessage = "+$r runs"
        if (checkInningsOver()) {
            if (inning == 1) showInningSwitch = true
            else checkMatchResult()
        }
        // Check if team2 chased in inning 2
        if (inning == 2 && runs2 > runs1) checkMatchResult()
        updateFirebase()
    }

    // Add wicket function
    fun addWicket() {
        if (matchOver || showInningSwitch) return
        if (inning == 1) { wickets1++; balls1++ }
        else { wickets2++; balls2++ }
        currentOverBalls = currentOverBalls.toMutableList().also { it.add("W") }
        if (currentOverBalls.size >= 6) currentOverBalls = mutableListOf()
        statusMessage = "WICKET! 🎉"
        if (checkInningsOver()) {
            if (inning == 1) showInningSwitch = true
            else checkMatchResult()
        }
        updateFirebase()
    }

    // Add extra function
    fun addExtra(type: String) {
        if (matchOver || showInningSwitch) return
        if (inning == 1) { runs1 += 1; extras1++ }
        else { runs2 += 1; extras2++ }
        currentOverBalls = currentOverBalls.toMutableList().also { it.add(type) }
        statusMessage = "$type - Extra run!"
        updateFirebase()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1B5E20))
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        // Match Result Banner
        if (matchOver) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFD700))
            ) {
                Text(
                    text = matchResult,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1B5E20),
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        // Inning Switch Banner
        if (showInningSwitch && !matchOver) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFF6F00))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "🏏 Innings Break!",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "$team1: $runs1/${wickets1} (${balls1/6}.${balls1%6} ov)",
                        fontSize = 14.sp,
                        color = Color.White,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                    Text(
                        text = "$team2 needs ${runs1 + 1} runs to win",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFFD700),
                        modifier = Modifier.padding(top = 4.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = {
                            inning = 2
                            battingTeam = 2
                            currentOverBalls = mutableListOf()
                            showInningSwitch = false
                            statusMessage = "$team2 batting now!"
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White)
                    ) {
                        Text(
                            "▶ Start 2nd Innings",
                            color = Color(0xFF1B5E20),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        // Main Scoreboard
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF2E7D32)),
            elevation = CardDefaults.cardElevation(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Inning indicator
                Text(
                    text = "🏏 ${if (inning == 1) "1st" else "2nd"} Innings — $currentTeam batting",
                    fontSize = 13.sp,
                    color = Color(0xFF81C784),
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Main Score
                Text(
                    text = "$currentRuns/$currentWickets",
                    fontSize = 64.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                // Overs
                Text(
                    text = "Overs: $overs.$ballsInOver / $maxOvers.0",
                    fontSize = 16.sp,
                    color = Color(0xFF81C784)
                )

                // Run Rate
                val runRate = if (currentBalls > 0)
                    String.format("%.2f", currentRuns.toFloat() / (currentBalls.toFloat() / 6))
                else "0.00"
                Text(
                    text = "CRR: $runRate",
                    fontSize = 14.sp,
                    color = Color(0xFF81C784)
                )

                // Target in 2nd innings
                if (inning == 2) {
                    val target = runs1 + 1
                    val runsNeeded = target - runs2
                    val ballsLeft = (maxOvers * 6) - balls2
                    val rrr = if (ballsLeft > 0)
                        String.format("%.2f", runsNeeded.toFloat() / (ballsLeft.toFloat() / 6))
                    else "0.00"
                    Spacer(modifier = Modifier.height(8.dp))
                    Divider(color = Color(0xFF81C784).copy(alpha = 0.4f))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Target: $target  |  Need: $runsNeeded off $ballsLeft balls",
                        fontSize = 13.sp,
                        color = Color(0xFFFFD700),
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "RRR: $rrr",
                        fontSize = 13.sp,
                        color = Color(0xFFFFCC80)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))
                Divider(color = Color(0xFF81C784).copy(alpha = 0.3f))
                Spacer(modifier = Modifier.height(8.dp))

                // Extras
                Text(
                    text = "Extras: $currentExtras  |  Bowling: $bowlingTeam",
                    fontSize = 12.sp,
                    color = Color(0xFF81C784)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Wicket dots display
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF2E7D32))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "Wickets",
                    fontSize = 13.sp,
                    color = Color(0xFF81C784),
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    for (i in 1..10) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .background(
                                    if (i <= currentWickets) Color(0xFFD32F2F)
                                    else Color(0xFF81C784).copy(alpha = 0.3f),
                                    CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (i <= currentWickets) "W" else "$i",
                                fontSize = 8.sp,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Current over balls
                Text(
                    text = "This Over",
                    fontSize = 13.sp,
                    color = Color(0xFF81C784),
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    currentOverBalls.forEach { ball ->
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .background(
                                    when (ball) {
                                        "W" -> Color(0xFFD32F2F)
                                        "4" -> Color(0xFF1565C0)
                                        "6" -> Color(0xFF6A1B9A)
                                        "Wd", "Nb" -> Color(0xFFE65100)
                                        else -> Color(0xFF558B2F)
                                    },
                                    CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = ball,
                                fontSize = 11.sp,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    // Empty slots
                    repeat(6 - currentOverBalls.size) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .border(1.dp, Color(0xFF81C784).copy(alpha = 0.4f), CircleShape)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Status message
        if (statusMessage.isNotEmpty()) {
            Text(
                text = statusMessage,
                color = Color(0xFFFFD700),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        // Scoring Buttons - only show if match not over
        if (!matchOver && !showInningSwitch) {

            // Run Buttons
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF2E7D32))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Add Runs",
                        fontSize = 14.sp,
                        color = Color(0xFF81C784),
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(bottom = 10.dp)
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        listOf(0, 1, 2, 3).forEach { run ->
                            Button(
                                onClick = { addRuns(run) },
                                modifier = Modifier.size(56.dp),
                                shape = CircleShape,
                                contentPadding = PaddingValues(0.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF558B2F)
                                )
                            ) {
                                Text(
                                    text = "$run",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                        // Four
                        Button(
                            onClick = { addRuns(4) },
                            modifier = Modifier.size(56.dp),
                            shape = CircleShape,
                            contentPadding = PaddingValues(0.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF1565C0)
                            )
                        ) {
                            Text(
                                text = "4",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                        // Six
                        Button(
                            onClick = { addRuns(6) },
                            modifier = Modifier.size(56.dp),
                            shape = CircleShape,
                            contentPadding = PaddingValues(0.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF6A1B9A)
                            )
                        ) {
                            Text(
                                text = "6",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Wicket + Extras Row
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF2E7D32))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Wicket & Extras",
                        fontSize = 14.sp,
                        color = Color(0xFF81C784),
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(bottom = 10.dp)
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        // Wicket
                        Button(
                            onClick = { addWicket() },
                            modifier = Modifier
                                .weight(1f)
                                .height(52.dp)
                                .padding(end = 4.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFD32F2F)
                            )
                        ) {
                            Text(
                                text = "🎯 WICKET",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                        // Wide
                        Button(
                            onClick = { addExtra("Wd") },
                            modifier = Modifier
                                .weight(1f)
                                .height(52.dp)
                                .padding(horizontal = 4.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFE65100)
                            )
                        ) {
                            Text(
                                text = "Wide",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                        // No Ball
                        Button(
                            onClick = { addExtra("Nb") },
                            modifier = Modifier
                                .weight(1f)
                                .height(52.dp)
                                .padding(start = 4.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFE65100)
                            )
                        ) {
                            Text(
                                text = "No Ball",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Both innings summary when inning 2
        if (inning == 2 || matchOver) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF2E7D32))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "📊 Match Summary",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF81C784)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = team1,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "$runs1/$wickets1 (${balls1/6}.${balls1%6})",
                            fontSize = 14.sp,
                            color = Color.White
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = team2,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "$runs2/$wickets2 (${balls2/6}.${balls2%6})",
                            fontSize = 14.sp,
                            color = Color.White
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        // Scorecard & WhatsApp Button
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

        Spacer(modifier = Modifier.height(10.dp))

        // Player Stats Button
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

        Spacer(modifier = Modifier.height(10.dp))

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

        Spacer(modifier = Modifier.height(24.dp))
    }
}