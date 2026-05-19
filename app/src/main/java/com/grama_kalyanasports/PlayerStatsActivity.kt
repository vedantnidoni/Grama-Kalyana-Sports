package com.grama_kalyanasports

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.foundation.lazy.itemsIndexed
import com.grama_kalyanasports.ui.theme.GramaKalyanaSportsTheme

data class Player(
    val name: String = "",
    val points: Int = 0,
    val team: String = "",
    val tournamentKey: String = ""
)

class PlayerStatsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val tournamentKey = intent.getStringExtra("tournamentKey") ?: ""
        val team1 = intent.getStringExtra("team1") ?: "Team 1"
        val team2 = intent.getStringExtra("team2") ?: "Team 2"

        setContent {
            GramaKalyanaSportsTheme {
                PlayerStatsScreen(
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
fun PlayerStatsScreen(
    tournamentKey: String,
    team1: String,
    team2: String,
    onBack: () -> Unit
) {
    var playerName by remember { mutableStateOf("") }
    var playerPoints by remember { mutableStateOf("") }
    var selectedTeam by remember { mutableStateOf(team1) }
    var players by remember { mutableStateOf(listOf<Player>()) }
    var statusMessage by remember { mutableStateOf("") }
    var isSaving by remember { mutableStateOf(false) }

    // 🔥 Load players from Firebase in real-time
    LaunchedEffect(tournamentKey) {
        FirebaseHelper.playersRef
            .child(tournamentKey)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val list = mutableListOf<Player>()
                    for (child in snapshot.children) {
                        val player = child.getValue(Player::class.java)
                        if (player != null) list.add(player)
                    }
                    // Sort by points descending
                    players = list.sortedByDescending { it.points }
                }
                override fun onCancelled(error: DatabaseError) {}
            })
    }

    // Man of the Match = player with highest points
    val manOfMatch = players.firstOrNull()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF1F8E9))
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            Spacer(modifier = Modifier.height(32.dp))

            Text(text = "🏅", fontSize = 48.sp)
            Text(
                text = "Player Stats",
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

            // 🏆 Man of the Match Card
            if (manOfMatch != null) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFFFD700)
                    ),
                    elevation = CardDefaults.cardElevation(8.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = "⭐", fontSize = 36.sp)
                        Text(
                            text = "MAN OF THE MATCH",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 2.sp,
                            color = Color(0xFF5D4037)
                        )
                        Text(
                            text = manOfMatch.name,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1B5E20),
                            modifier = Modifier.padding(top = 4.dp)
                        )
                        Text(
                            text = "${manOfMatch.team}  •  ${manOfMatch.points} pts",
                            fontSize = 14.sp,
                            color = Color(0xFF5D4037),
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }

            // Add Player Section
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "➕ Add Player",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1B5E20),
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    // Player Name
                    OutlinedTextField(
                        value = playerName,
                        onValueChange = { playerName = it },
                        label = { Text("Player Name") },
                        placeholder = { Text("e.g. Raju") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF2E7D32),
                            focusedLabelColor = Color(0xFF2E7D32)
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Points
                    OutlinedTextField(
                        value = playerPoints,
                        onValueChange = { playerPoints = it.filter { c -> c.isDigit() } },
                        label = { Text("Points / Score") },
                        placeholder = { Text("e.g. 12") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF2E7D32),
                            focusedLabelColor = Color(0xFF2E7D32)
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Team Selection
                    Text(
                        text = "Select Team:",
                        fontSize = 14.sp,
                        color = Color(0xFF558B2F),
                        fontWeight = FontWeight.SemiBold
                    )
                    Row(modifier = Modifier.padding(top = 8.dp)) {
                        listOf(team1, team2).forEach { team ->
                            FilterChip(
                                selected = selectedTeam == team,
                                onClick = { selectedTeam = team },
                                label = { Text(team) },
                                modifier = Modifier.padding(end = 8.dp),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color(0xFF2E7D32),
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Save Button
                    Button(
                        onClick = {
                            if (playerName.isNotEmpty() && playerPoints.isNotEmpty()) {
                                isSaving = true
                                val player = Player(
                                    name = playerName,
                                    points = playerPoints.toInt(),
                                    team = selectedTeam,
                                    tournamentKey = tournamentKey
                                )
                                FirebaseHelper.playersRef
                                    .child(tournamentKey)
                                    .child(playerName.replace(" ", "_"))
                                    .setValue(player)
                                    .addOnSuccessListener {
                                        isSaving = false
                                        statusMessage = "✅ ${playerName} added!"
                                        playerName = ""
                                        playerPoints = ""
                                    }
                                    .addOnFailureListener {
                                        isSaving = false
                                        statusMessage = "❌ Error: ${it.message}"
                                    }
                            } else {
                                statusMessage = "⚠️ Fill player name and points!"
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF2E7D32)
                        ),
                        enabled = !isSaving
                    ) {
                        Text(
                            text = if (isSaving) "Saving..." else "💾 Save Player",
                            fontWeight = FontWeight.Bold
                        )
                    }

                    if (statusMessage.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = statusMessage,
                            fontSize = 13.sp,
                            color = if (statusMessage.startsWith("✅"))
                                Color(0xFF2E7D32) else Color(0xFFC62828)
                        )
                    }
                }
            }

            // Player List Header
            if (players.isNotEmpty()) {
                Text(
                    text = "🏆 Leaderboard",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1B5E20),
                    modifier = Modifier.padding(bottom = 12.dp)
                )
            }
        }

        // Player List
        itemsIndexed(players) { index, player ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = when (index) {
                        0 -> Color(0xFFFFD700) // Gold
                        1 -> Color(0xFFE0E0E0) // Silver
                        2 -> Color(0xFFFFCC80) // Bronze
                        else -> Color.White
                    }
                ),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = when (index) {
                            0 -> "🥇"
                            1 -> "🥈"
                            2 -> "🥉"
                            else -> "${index + 1}."
                        },
                        fontSize = 22.sp,
                        modifier = Modifier.width(40.dp)
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = player.name,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1B5E20)
                        )
                        Text(
                            text = player.team,
                            fontSize = 12.sp,
                            color = Color(0xFF558B2F)
                        )
                    }
                    Text(
                        text = "${player.points} pts",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1B5E20)
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedButton(
                onClick = onBack,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("← Back", color = Color(0xFF2E7D32))
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

