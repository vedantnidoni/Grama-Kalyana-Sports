package com.grama_kalyanasports

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.grama_kalyanasports.ui.theme.GramaKalyanaSportsTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // ✅ Test Firebase connection
        FirebaseHelper.database.getReference("connection_test")
            .setValue("Grama-Kalyana Sports Connected! ✅")
            .addOnSuccessListener {
                android.util.Log.d("Firebase", "✅ Connected successfully!")
            }
            .addOnFailureListener {
                android.util.Log.e("Firebase", "❌ Failed: ${it.message}")
            }

        setContent {
            GramaKalyanaSportsTheme {

                // 🔥 Fetch latest tournament key from Firebase
                var latestTournamentKey by remember { mutableStateOf("") }

                LaunchedEffect(Unit) {
                    FirebaseHelper.tournamentsRef
                        .limitToLast(1)
                        .addValueEventListener(object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                for (child in snapshot.children) {
                                    latestTournamentKey = child.key ?: ""
                                }
                            }
                            override fun onCancelled(error: DatabaseError) {
                                android.util.Log.e("Firebase", "Failed: ${error.message}")
                            }
                        })
                }

                HomeScreen(
                    onSportSelected = { sport ->
                        val intent = Intent(this, TournamentSetupActivity::class.java)
                        intent.putExtra("sport", sport)
                        startActivity(intent)
                    },
                    onWatchLive = {
                        if (latestTournamentKey.isNotEmpty()) {
                            val intent = Intent(this, FanViewActivity::class.java)
                            intent.putExtra("tournamentKey", latestTournamentKey)
                            startActivity(intent)
                        } else {
                            android.util.Log.d("Navigation", "No tournament found yet")
                        }
                    },
                    latestTournamentKey = latestTournamentKey
                )
            }
        }
    }
}

@Composable
fun HomeScreen(
    onSportSelected: (String) -> Unit = {},
    onWatchLive: () -> Unit = {},
    latestTournamentKey: String = ""
) {
    val sports = listOf(
        Pair("🏐", "Volleyball"),
        Pair("🤼", "Kabaddi"),
        Pair("🏏", "Cricket")
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF1F8E9))
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(48.dp))

        // 🏆 Header
        Text(
            text = "🏆",
            fontSize = 56.sp
        )
        Text(
            text = "Grama-Kalyana Sports",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1B5E20),
            textAlign = TextAlign.Center
        )
        Text(
            text = "Digital Scoreboard for Village Tournaments",
            fontSize = 13.sp,
            color = Color(0xFF558B2F),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 6.dp, bottom = 40.dp)
        )

        // 🏅 Sport Selection
        Text(
            text = "Select a Sport to Begin",
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF33691E),
            modifier = Modifier.padding(bottom = 16.dp)
        )

        sports.forEach { (emoji, name) ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF2E7D32)
                ),
                elevation = CardDefaults.cardElevation(6.dp),
                onClick = {
                    onSportSelected(name)
                }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = emoji, fontSize = 32.sp)
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = name,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Text(text = "▶", color = Color.White, fontSize = 18.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 📺 Watch Live Score Button
        OutlinedButton(
            onClick = onWatchLive,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = Color(0xFF1B5E20)
            ),
            enabled = latestTournamentKey.isNotEmpty()
        ) {
            Text(
                text = if (latestTournamentKey.isNotEmpty())
                    "📺  Watch Live Score"
                else
                    "📺  No Live Match Yet",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 🔥 Firebase status + live match info
        if (latestTournamentKey.isNotEmpty()) {
            Text(
                text = "🔴 Live: ${latestTournamentKey.replace("_", " ")}",
                fontSize = 12.sp,
                color = Color(0xFFD32F2F),
                fontWeight = FontWeight.SemiBold
            )
        } else {
            Text(
                text = "🔥 Firebase Connected",
                fontSize = 12.sp,
                color = Color(0xFF81C784)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    GramaKalyanaSportsTheme {
        HomeScreen()
    }
}