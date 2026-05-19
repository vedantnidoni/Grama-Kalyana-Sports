package com.grama_kalyanasports

import com.google.firebase.database.FirebaseDatabase

object FirebaseHelper {

    val database = FirebaseDatabase.getInstance(
        "https://gramakalyanasports-e306a-default-rtdb.asia-southeast1.firebasedatabase.app"
    )

    val tournamentsRef = database.getReference("tournaments")
    val teamsRef = database.getReference("teams")
    val playersRef = database.getReference("players")
    val scoresRef = database.getReference("scores")
    val matchesRef = database.getReference("matches")
}