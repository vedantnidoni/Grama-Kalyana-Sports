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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.grama_kalyanasports.ui.theme.GramaKalyanaSportsTheme

class TournamentSetupActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val sport = intent.getStringExtra("sport") ?: "Volleyball"

        setContent {
            GramaKalyanaSportsTheme {

                TournamentSetupScreen(
                    sport = sport,

                    onBack = {
                        finish()
                    },

                    onStartScorer = { key, t1, t2 ->

                        // ✅ Open Cricket scorer for Cricket
                        if (sport == "Cricket") {

                            val intent =
                                Intent(this, CricketScorerActivity::class.java)

                            intent.putExtra("tournamentKey", key)
                            intent.putExtra("team1", t1)
                            intent.putExtra("team2", t2)

                            startActivity(intent)

                        } else {

                            // ✅ Open normal scorer for other sports
                            val intent =
                                Intent(this, ScorerActivity::class.java)

                            intent.putExtra("tournamentKey", key)
                            intent.putExtra("team1", t1)
                            intent.putExtra("team2", t2)
                            intent.putExtra("sport", sport)

                            startActivity(intent)
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun TournamentSetupScreen(
    sport: String,
    onBack: () -> Unit,
    onStartScorer: (String, String, String) -> Unit
) {

    var tournamentName by remember { mutableStateOf("") }
    var team1Name by remember { mutableStateOf("") }
    var team2Name by remember { mutableStateOf("") }

    var isSaving by remember { mutableStateOf(false) }
    var savedMessage by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF1F8E9))
            .padding(24.dp),

        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Spacer(modifier = Modifier.height(32.dp))

        // Sport Emoji
        Text(
            text = when (sport) {
                "Volleyball" -> "🏐"
                "Kabaddi" -> "🤼"
                else -> "🏏"
            },

            fontSize = 48.sp
        )

        // Title
        Text(
            text = "$sport Tournament Setup",

            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,

            color = Color(0xFF1B5E20),

            modifier = Modifier.padding(
                top = 8.dp,
                bottom = 32.dp
            )
        )

        // Tournament Name
        OutlinedTextField(
            value = tournamentName,

            onValueChange = {
                tournamentName = it
            },

            label = {
                Text("Tournament Name")
            },

            placeholder = {
                Text("e.g. Grama Cup 2026")
            },

            modifier = Modifier.fillMaxWidth(),

            shape = RoundedCornerShape(12.dp),

            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF2E7D32),
                focusedLabelColor = Color(0xFF2E7D32)
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Team 1
        OutlinedTextField(
            value = team1Name,

            onValueChange = {
                team1Name = it
            },

            label = {
                Text("Team 1 Name")
            },

            placeholder = {
                Text("e.g. Village Lions")
            },

            modifier = Modifier.fillMaxWidth(),

            shape = RoundedCornerShape(12.dp),

            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF2E7D32),
                focusedLabelColor = Color(0xFF2E7D32)
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Team 2
        OutlinedTextField(
            value = team2Name,

            onValueChange = {
                team2Name = it
            },

            label = {
                Text("Team 2 Name")
            },

            placeholder = {
                Text("e.g. Village Tigers")
            },

            modifier = Modifier.fillMaxWidth(),

            shape = RoundedCornerShape(12.dp),

            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF2E7D32),
                focusedLabelColor = Color(0xFF2E7D32)
            )
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Start Tournament Button
        Button(

            onClick = {

                if (
                    tournamentName.isNotEmpty() &&
                    team1Name.isNotEmpty() &&
                    team2Name.isNotEmpty()
                ) {

                    isSaving = true

                    val key = tournamentName
                        .trim()
                        .replace(" ", "_")

                    val tournamentData = mapOf(

                        "name" to tournamentName,
                        "sport" to sport,

                        "team1" to team1Name,
                        "team2" to team2Name,

                        "score1" to 0,
                        "score2" to 0,

                        "status" to "live"
                    )

                    FirebaseHelper.tournamentsRef
                        .child(key)

                        .setValue(tournamentData)

                        .addOnSuccessListener {

                            isSaving = false

                            // ✅ Open scorer screen
                            onStartScorer(
                                key,
                                team1Name,
                                team2Name
                            )
                        }

                        .addOnFailureListener {

                            isSaving = false

                            savedMessage =
                                "❌ Error: ${it.message}"
                        }

                } else {

                    savedMessage =
                        "⚠️ Please fill all fields!"
                }
            },

            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),

            shape = RoundedCornerShape(16.dp),

            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF2E7D32)
            ),

            enabled = !isSaving

        ) {

            Text(
                text =
                    if (isSaving)
                        "Saving..."
                    else
                        "🏆 Start Tournament",

                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Back Button
        OutlinedButton(

            onClick = onBack,

            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),

            shape = RoundedCornerShape(12.dp)

        ) {

            Text(
                text = "← Back to Home",
                color = Color(0xFF2E7D32)
            )
        }

        // Message Card
        if (savedMessage.isNotEmpty()) {

            Spacer(modifier = Modifier.height(16.dp))

            Card(

                modifier = Modifier.fillMaxWidth(),

                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFFFEBEE)
                ),

                shape = RoundedCornerShape(12.dp)

            ) {

                Text(

                    text = savedMessage,

                    modifier = Modifier.padding(16.dp),

                    fontSize = 14.sp,

                    color = Color(0xFFC62828)
                )
            }
        }
    }
}