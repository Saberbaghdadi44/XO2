package com.example.xo;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class TournamentActivity extends AppCompatActivity {

    private TextView tvTournamentTitle;
    private TextView tvTournamentProgress;
    private Button btnStartTournament;
    private Button btnBack;

    private int gameCount = 0;
    private final int MAX_GAMES = 5;

    private int playerXTotalWins = 0;
    private int playerOTotalWins = 0;
    private int drawsTotal = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tournament);

        tvTournamentTitle = findViewById(R.id.tvTournamentTitle);
        tvTournamentProgress = findViewById(R.id.tvTournamentProgress);
        btnStartTournament = findViewById(R.id.btnStartTournament);
        btnBack = findViewById(R.id.btnBack);

        // Mettre à jour l'affichage du progrès
        updateTournamentProgress();

        btnStartTournament.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startTournament();
            }
        });

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void startTournament() {
        gameCount = 0;
        playerXTotalWins = 0;
        playerOTotalWins = 0;
        drawsTotal = 0;

        btnStartTournament.setEnabled(false);
        btnStartTournament.setText("Tournoi en cours...");

        startNextGame();
    }

    private void startNextGame() {
        if (gameCount >= MAX_GAMES) {
            // Tournoi terminé, afficher le résultat final
            showTournamentResult();
            return;
        }

        gameCount++;
        updateTournamentProgress();

        // Démarrer MainActivity pour jouer une seule partie
        Intent intent = new Intent(TournamentActivity.this, MainActivity.class);
        intent.putExtra("TOURNAMENT_MODE", true);
        intent.putExtra("GAME_NUMBER", gameCount);
        intent.putExtra("TOTAL_GAMES", MAX_GAMES);
        startActivityForResult(intent, 1); // Même requestCode pour toutes les parties
    }

    private void updateTournamentProgress() {
        if (tvTournamentProgress != null) {
            if (gameCount == 0) {
                tvTournamentProgress.setText("Tournoi de " + MAX_GAMES + " parties\nPrêt à commencer");
            } else if (gameCount <= MAX_GAMES) {
                tvTournamentProgress.setText("Partie " + gameCount + " / " + MAX_GAMES +
                        "\nX: " + playerXTotalWins + " | O: " + playerOTotalWins + " | Nuls: " + drawsTotal);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && data != null) {
            String winner = data.getStringExtra("WINNER");

            // Mettre à jour le score du tournoi
            if ("X".equals(winner)) {
                playerXTotalWins++;
            } else if ("O".equals(winner)) {
                playerOTotalWins++;
            } else if ("DRAW".equals(winner)) {
                drawsTotal++;
            }

            updateTournamentProgress();

            // Afficher le résultat de la partie terminée
            String gameResult = "Partie " + gameCount + " terminée: ";
            if ("X".equals(winner)) {
                gameResult += "Joueur X gagne !";
            } else if ("O".equals(winner)) {
                gameResult += "Joueur O gagne !";
            } else {
                gameResult += "Match nul !";
            }

            Toast.makeText(this, gameResult, Toast.LENGTH_LONG).show();

            // Attendre un peu avant de démarrer la partie suivante
            new android.os.Handler().postDelayed(
                    new Runnable() {
                        public void run() {
                            startNextGame();
                        }
                    },
                    2000); // 2 secondes de délai
        }
    }

    private void showTournamentResult() {
        String result = "🏆 TOURNOI TERMINÉ 🏆\n\n";
        result += "Score final:\n";
        result += "Joueur X: " + playerXTotalWins + " victoires\n";
        result += "Joueur O: " + playerOTotalWins + " victoires\n";
        result += "Matchs nuls: " + drawsTotal + "\n\n";

        if (playerXTotalWins > playerOTotalWins) {
            result += "🎉 VAINQUEUR: JOUEUR X !";
        } else if (playerOTotalWins > playerXTotalWins) {
            result += "🎉 VAINQUEUR: JOUEUR O !";
        } else {
            result += "🤝 TOURNOI NUL !";
        }

        // Afficher le résultat dans un Toast ou une AlertDialog
        Toast.makeText(this, result, Toast.LENGTH_LONG).show();

        // Réactiver le bouton pour recommencer
        btnStartTournament.setEnabled(true);
        btnStartTournament.setText("Recommencer le tournoi");

        // Mettre à jour l'affichage final
        tvTournamentProgress.setText("Tournoi terminé!\n" +
                "X: " + playerXTotalWins + " | O: " + playerOTotalWins + " | Nuls: " + drawsTotal);
    }
}