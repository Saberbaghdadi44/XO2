package com.example.xo;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class UserSelectionActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_selection);

        Button buttonSinglePlayer = findViewById(R.id.button_single_player);
        Button buttonTwoPlayers = findViewById(R.id.button_two_players);

        buttonSinglePlayer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startMainActivity("Joueur", "IA");
            }
        });

        buttonTwoPlayers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startMainActivity("Joueur1", "Joueur2");
            }
        });
    }

    private void startMainActivity(String player1, String player2) {
        Intent intent = new Intent(this, MainActivity.class);
        // Tu peux passer les noms en extras si besoin
        startActivity(intent);
        finish();
    }
}