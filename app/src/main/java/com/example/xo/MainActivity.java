package com.example.xo;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

public class MainActivity extends AppCompatActivity {

    private Button[][] buttons = new Button[3][3];
    private boolean playerXTurn = true;
    private boolean gameActive = true;
    private TextView textViewPlayerX;
    private TextView textViewPlayerO;
    private TextView textViewStatus;
    private RelativeLayout resultLayout;
    private TextView textViewResult;
    private TextView textViewTournamentInfo;

    private String playerXName = "Joueur1";
    private String playerOName = "Adversaire";

    // Variables pour les scores dynamiques
    private int playerXWins = 0;
    private int playerOWins = 0;
    private int draws = 0;

    // Variables pour le tournoi
    private boolean tournamentMode = false;
    private int tournamentSize = 5;
    private int currentTournamentGame = 0;
    private int tournamentXWins = 0;
    private int tournamentOWins = 0;
    private int tournamentDraws = 0;

    // Database helper pour l'archivage
    private DatabaseHelper databaseHelper;

    // Handler pour les animations
    private Handler animationHandler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialiser la base de données
        databaseHelper = new DatabaseHelper(this);

        initializeViews();
        setupGame();
    }

    @Override
    protected void onDestroy() {
        databaseHelper.close();
        animationHandler.removeCallbacksAndMessages(null);
        super.onDestroy();
    }

    private void initializeViews() {
        // Initialiser les TextViews
        textViewPlayerX = findViewById(R.id.playerXName);
        textViewPlayerO = findViewById(R.id.playerOName);
        textViewStatus = findViewById(R.id.tvPlayer);
        resultLayout = findViewById(R.id.resultLayout);
        textViewResult = findViewById(R.id.tvResult);
        textViewTournamentInfo = findViewById(R.id.tvTournamentInfo);

        // Appliquer l'effet néon aux textes des joueurs
        applyNeonTextEffect(textViewPlayerX, true);
        applyNeonTextEffect(textViewPlayerO, false);
        applyNeonTextEffect(textViewStatus, true);

        // Initialiser la grille de boutons
        buttons[0][0] = findViewById(R.id.btn00);
        buttons[0][1] = findViewById(R.id.btn01);
        buttons[0][2] = findViewById(R.id.btn02);
        buttons[1][0] = findViewById(R.id.btn10);
        buttons[1][1] = findViewById(R.id.btn11);
        buttons[1][2] = findViewById(R.id.btn12);
        buttons[2][0] = findViewById(R.id.btn20);
        buttons[2][1] = findViewById(R.id.btn21);
        buttons[2][2] = findViewById(R.id.btn22);

        // Ajouter les click listeners à tous les boutons de la grille
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                final int row = i;
                final int col = j;

                buttons[i][j].setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        onGridButtonClick(row, col);
                    }
                });
            }
        }

        // Initialiser les boutons d'action
        Button btnEditNames = findViewById(R.id.btnEditNames);
        Button btnChangePlayer = findViewById(R.id.btnChangePlayer);
        Button btnReset = findViewById(R.id.btnReset);
        Button btnScores = findViewById(R.id.btnScores);
        Button btnQuit = findViewById(R.id.btnQuit);
        Button btnNewGame = findViewById(R.id.btnNewGame);
        Button btnTournament = findViewById(R.id.btnTournament);

        // Appliquer des effets aux boutons d'action
        applyButtonNeonEffect(btnEditNames, Color.parseColor("#FF9800"));
        applyButtonNeonEffect(btnChangePlayer, Color.parseColor("#4CAF50"));
        applyButtonNeonEffect(btnReset, Color.parseColor("#2196F3"));
        applyButtonNeonEffect(btnScores, Color.parseColor("#9C27B0"));
        applyButtonNeonEffect(btnQuit, Color.parseColor("#F44336"));
        applyButtonNeonEffect(btnNewGame, Color.parseColor("#4CAF50"));
        applyButtonNeonEffect(btnTournament, Color.parseColor("#FFEB3B"));

        btnTournament.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showTournamentOptions();
            }
        });

        btnEditNames.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showEditNamesDialog();
            }
        });

        btnChangePlayer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showChangePlayerConfirmation();
            }
        });

        btnReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetScores();
            }
        });

        btnScores.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showScores();
            }
        });

        btnQuit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        btnNewGame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (tournamentMode && currentTournamentGame < tournamentSize) {
                    // En mode tournoi, passer à la partie suivante
                    startNextTournamentGame();
                } else if (tournamentMode && currentTournamentGame >= tournamentSize) {
                    // Tournoi terminé, proposer un nouveau tournoi
                    showTournamentOptions();
                } else {
                    // Mode normal, juste réinitialiser le jeu
                    resultLayout.setVisibility(View.GONE);
                    resetGame();
                }
            }
        });

        // Mettre à jour les noms des joueurs
        textViewPlayerX.setText(playerXName);
        textViewPlayerO.setText(playerOName);
        updateStatus();
        updateTournamentDisplay();
    }

    private void showTournamentOptions() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Démarrer un tournoi")
                .setItems(new String[]{"Tournoi de 5 parties", "Tournoi de 10 parties", "Tournoi de 15 parties"}, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case 0:
                                startTournament(5);
                                break;
                            case 1:
                                startTournament(10);
                                break;
                            case 2:
                                startTournament(15);
                                break;
                        }
                    }
                });
        builder.show();
    }

    private void startTournament(int size) {
        tournamentMode = true;
        tournamentSize = size;
        currentTournamentGame = 0;
        tournamentXWins = 0;
        tournamentOWins = 0;
        tournamentDraws = 0;

        // Masquer le bouton tournoi pendant le tournoi
        Button btnTournament = findViewById(R.id.btnTournament);
        btnTournament.setVisibility(View.GONE);

        // Adapter le bouton nouvelle partie
        Button btnNewGame = findViewById(R.id.btnNewGame);
        btnNewGame.setText("Commencer");

        // Cacher le résultat précédent
        resultLayout.setVisibility(View.GONE);

        updateTournamentDisplay();

        Toast.makeText(this, "Tournoi de " + size + " parties démarré!", Toast.LENGTH_SHORT).show();
    }

    private void startNextTournamentGame() {
        if (currentTournamentGame >= tournamentSize) {
            // Tournoi terminé
            endTournament();
            return;
        }

        currentTournamentGame++;
        updateTournamentDisplay();

        // Réinitialiser le jeu pour la nouvelle partie
        resultLayout.setVisibility(View.GONE);
        resetGame();

        // Mettre à jour le statut
        textViewStatus.setText("Tournoi - Partie " + currentTournamentGame + "/" + tournamentSize);
    }

    private void updateTournamentDisplay() {
        if (textViewTournamentInfo != null) {
            if (tournamentMode) {
                if (currentTournamentGame == 0) {
                    textViewTournamentInfo.setText("Tournoi: " + tournamentSize + " parties\nPrêt à commencer");
                } else if (currentTournamentGame <= tournamentSize) {
                    textViewTournamentInfo.setText("Tournoi - Partie " + currentTournamentGame + "/" + tournamentSize +
                            "\nX: " + tournamentXWins + " | O: " + tournamentOWins + " | Nuls: " + tournamentDraws);
                } else {
                    textViewTournamentInfo.setText("Tournoi terminé!\nX: " + tournamentXWins + " | O: " + tournamentOWins + " | Nuls: " + tournamentDraws);
                }
                textViewTournamentInfo.setVisibility(View.VISIBLE);
            } else {
                textViewTournamentInfo.setVisibility(View.GONE);
            }
        }
    }

    private void endTournament() {
        tournamentMode = false;

        // Déterminer le vainqueur du tournoi
        String tournamentWinner;
        if (tournamentXWins > tournamentOWins) {
            tournamentWinner = playerXName;
        } else if (tournamentOWins > tournamentXWins) {
            tournamentWinner = playerOName;
        } else {
            tournamentWinner = "Égalité";
        }

        // Enregistrer le tournoi dans la base de données
        databaseHelper.archiveSession(playerXName, playerOName, tournamentXWins, tournamentOWins, tournamentDraws);
        databaseHelper.clearOldArchives();

        // Afficher le résultat final du tournoi DIRECTEMENT dans l'écran principal
        String resultMessage = "🏆 TOURNOI TERMINÉ 🏆\n\n";
        resultMessage += "Score final:\n";
        resultMessage += playerXName + ": " + tournamentXWins + " victoires\n";
        resultMessage += playerOName + ": " + tournamentOWins + " victoires\n";
        resultMessage += "Matchs nuls: " + tournamentDraws + "\n\n";

        if (!tournamentWinner.equals("Égalité")) {
            resultMessage += "🎉 VAINQUEUR: " + tournamentWinner + " !";
        } else {
            resultMessage += "🤝 TOURNOI NUL !";
        }

        // Afficher le résultat dans le TextView de résultat existant
        textViewResult.setText(resultMessage);
        resultLayout.setVisibility(View.VISIBLE);

        // Adapter le bouton "Nouvelle Partie"
        Button btnNewGame = findViewById(R.id.btnNewGame);
        btnNewGame.setText("Nouveau Tournoi");

        // Réafficher le bouton tournoi
        Button btnTournament = findViewById(R.id.btnTournament);
        btnTournament.setVisibility(View.VISIBLE);

        // Mettre à jour l'affichage du tournoi
        updateTournamentDisplay();

        // Toast de confirmation de sauvegarde
        Toast.makeText(this, "Tournoi sauvegardé!", Toast.LENGTH_SHORT).show();
    }

    private void applyNeonTextEffect(TextView textView, boolean isRed) {
        if (isRed) {
            textView.setTextColor(Color.WHITE);
            textView.setShadowLayer(20, 0, 0, Color.parseColor("#FFFF1744"));
        } else {
            textView.setTextColor(Color.WHITE);
            textView.setShadowLayer(20, 0, 0, Color.parseColor("#FF2979FF"));
        }
    }

    private void applyButtonNeonEffect(Button button, int glowColor) {
        button.setTextColor(Color.WHITE);
        button.setShadowLayer(15, 0, 0, glowColor);

        // Animation de pulsation continue pour les boutons d'action
        ScaleAnimation pulse = new ScaleAnimation(1.0f, 1.05f, 1.0f, 1.05f,
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        pulse.setDuration(1000);
        pulse.setRepeatCount(Animation.INFINITE);
        pulse.setRepeatMode(Animation.REVERSE);
        button.startAnimation(pulse);
    }

    private void onGridButtonClick(int row, int col) {
        if (gameActive && buttons[row][col].getText().toString().equals("")) {
            // Animation de clic
            ScaleAnimation clickAnimation = new ScaleAnimation(1.0f, 0.8f, 1.0f, 0.8f,
                    Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
            clickAnimation.setDuration(100);
            buttons[row][col].startAnimation(clickAnimation);

            if (playerXTurn) {
                buttons[row][col].setText("X");
                applyNeonEffect(buttons[row][col], true); // Effet néon rouge pour X
            } else {
                buttons[row][col].setText("O");
                applyNeonEffect(buttons[row][col], false); // Effet néon bleu pour O
            }

            // Vérifier le résultat après un petit délai pour l'animation
            animationHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    checkGameResult(row, col);
                }
            }, 300);
        }
    }

    private void checkGameResult(int row, int col) {
        if (checkWin()) {
            gameActive = false;
            if (playerXTurn) {
                playerXWins++;
                if (tournamentMode) tournamentXWins++;
                showResult(playerXName + " gagne!");
                applyWinAnimation(true); // Animation victoire pour X
            } else {
                playerOWins++;
                if (tournamentMode) tournamentOWins++;
                showResult(playerOName + " gagne!");
                applyWinAnimation(false); // Animation victoire pour O
            }
        } else if (isBoardFull()) {
            gameActive = false;
            draws++;
            if (tournamentMode) tournamentDraws++;
            showResult("Match nul!");
            applyDrawAnimation();
        } else {
            playerXTurn = !playerXTurn;
            updateStatus();
        }
    }

    private void applyNeonEffect(Button button, boolean isX) {
        if (isX) {
            // Effet néon rouge pour X
            button.setTextColor(Color.WHITE);
            button.setShadowLayer(25, 0, 0, Color.parseColor("#FFFF1744"));

            // Animation de pulsation pour X
            button.animate()
                    .scaleX(1.3f)
                    .scaleY(1.3f)
                    .setDuration(200)
                    .withEndAction(new Runnable() {
                        @Override
                        public void run() {
                            button.animate()
                                    .scaleX(1.0f)
                                    .scaleY(1.0f)
                                    .setDuration(200);
                        }
                    });
        } else {
            // Effet néon bleu pour O
            button.setTextColor(Color.WHITE);
            button.setShadowLayer(25, 0, 0, Color.parseColor("#FF2979FF"));

            // Animation de pulsation pour O
            button.animate()
                    .scaleX(1.3f)
                    .scaleY(1.3f)
                    .setDuration(200)
                    .withEndAction(new Runnable() {
                        @Override
                        public void run() {
                            button.animate()
                                    .scaleX(1.0f)
                                    .scaleY(1.0f)
                                    .setDuration(200);
                        }
                    });
        }

        // Ajouter un effet de rotation
        button.animate()
                .rotationBy(360)
                .setDuration(800)
                .start();
    }

    private void applyWinAnimation(boolean isXWinner) {
        int[][] winningPositions = getWinningPositions();

        if (winningPositions != null) {
            final int neonColor = isXWinner ? Color.parseColor("#FFFF1744") : Color.parseColor("#FF2979FF");

            for (int i = 0; i < winningPositions.length; i++) {
                final int row = winningPositions[i][0];
                final int col = winningPositions[i][1];
                final Button winningButton = buttons[row][col];

                // Animation en cascade avec délai
                final int delay = i * 150;

                animationHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        // Animation de victoire
                        winningButton.animate()
                                .scaleX(1.5f)
                                .scaleY(1.5f)
                                .setDuration(300)
                                .withEndAction(new Runnable() {
                                    @Override
                                    public void run() {
                                        winningButton.animate()
                                                .scaleX(1.0f)
                                                .scaleY(1.0f)
                                                .setDuration(300);
                                    }
                                });

                        // Clignotement néon
                        startNeonBlink(winningButton, neonColor, 6);
                    }
                }, delay);
            }

            // Animation du texte de résultat
            applyResultTextAnimation(isXWinner ? playerXName + " gagne!" : playerOName + " gagne!");
        }
    }

    private void applyDrawAnimation() {
        // Faire clignoter toute la grille en violet pour match nul
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                final Button button = buttons[i][j];
                final int delay = (i * 3 + j) * 50;

                animationHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        startNeonBlink(button, Color.parseColor("#FFE040FB"), 3);
                    }
                }, delay);
            }
        }

        applyResultTextAnimation("Match nul!");
    }

    private void startNeonBlink(final Button button, final int neonColor, final int blinkCount) {
        final int[] currentBlink = {0};

        final Runnable blinkRunnable = new Runnable() {
            @Override
            public void run() {
                if (currentBlink[0] < blinkCount * 2) {
                    if (currentBlink[0] % 2 == 0) {
                        button.setShadowLayer(30, 0, 0, neonColor);
                    } else {
                        button.setShadowLayer(15, 0, 0, neonColor);
                    }
                    currentBlink[0]++;
                    animationHandler.postDelayed(this, 200);
                }
            }
        };

        animationHandler.post(blinkRunnable);
    }

    private void applyResultTextAnimation(String message) {
        textViewResult.setText(message);
        textViewResult.setShadowLayer(25, 0, 0, Color.parseColor("#FFE040FB"));

        // Animation du texte de résultat
        ScaleAnimation scaleAnimation = new ScaleAnimation(0.1f, 1.0f, 0.1f, 1.0f,
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        scaleAnimation.setDuration(500);
        textViewResult.startAnimation(scaleAnimation);

        resultLayout.setVisibility(View.VISIBLE);

        // Si mode tournoi, adapter le bouton "Nouvelle partie"
        if (tournamentMode) {
            Button btnNewGame = findViewById(R.id.btnNewGame);
            if (btnNewGame != null) {
                if (currentTournamentGame < tournamentSize) {
                    btnNewGame.setText("Partie suivante");
                } else {
                    btnNewGame.setText("Nouveau Tournoi");
                }
            }
        }
    }

    private void setupGame() {
        resetGame();
    }

    private boolean checkWin() {
        String[][] field = new String[3][3];

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                field[i][j] = buttons[i][j].getText().toString();
            }
        }

        // Vérifier les lignes
        for (int i = 0; i < 3; i++) {
            if (field[i][0].equals(field[i][1]) && field[i][0].equals(field[i][2]) && !field[i][0].equals("")) {
                return true;
            }
        }

        // Vérifier les colonnes
        for (int i = 0; i < 3; i++) {
            if (field[0][i].equals(field[1][i]) && field[0][i].equals(field[2][i]) && !field[0][i].equals("")) {
                return true;
            }
        }

        // Vérifier les diagonales
        if (field[0][0].equals(field[1][1]) && field[0][0].equals(field[2][2]) && !field[0][0].equals("")) {
            return true;
        }

        if (field[0][2].equals(field[1][1]) && field[0][2].equals(field[2][0]) && !field[0][2].equals("")) {
            return true;
        }

        return false;
    }

    private int[][] getWinningPositions() {
        String[][] field = new String[3][3];

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                field[i][j] = buttons[i][j].getText().toString();
            }
        }

        // Vérifier les lignes
        for (int i = 0; i < 3; i++) {
            if (field[i][0].equals(field[i][1]) && field[i][0].equals(field[i][2]) && !field[i][0].equals("")) {
                return new int[][]{{i, 0}, {i, 1}, {i, 2}};
            }
        }

        // Vérifier les colonnes
        for (int i = 0; i < 3; i++) {
            if (field[0][i].equals(field[1][i]) && field[0][i].equals(field[2][i]) && !field[0][i].equals("")) {
                return new int[][]{{0, i}, {1, i}, {2, i}};
            }
        }

        // Vérifier les diagonales
        if (field[0][0].equals(field[1][1]) && field[0][0].equals(field[2][2]) && !field[0][0].equals("")) {
            return new int[][]{{0, 0}, {1, 1}, {2, 2}};
        }

        if (field[0][2].equals(field[1][1]) && field[0][2].equals(field[2][0]) && !field[0][2].equals("")) {
            return new int[][]{{0, 2}, {1, 1}, {2, 0}};
        }

        return null;
    }

    private boolean isBoardFull() {
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (buttons[i][j].getText().toString().equals("")) {
                    return false;
                }
            }
        }
        return true;
    }

    private void updateStatus() {
        if (playerXTurn) {
            textViewStatus.setText("Tour: " + playerXName + " (X)");
            textViewStatus.setShadowLayer(20, 0, 0, Color.parseColor("#FFFF1744"));
        } else {
            textViewStatus.setText("Tour: " + playerOName + " (O)");
            textViewStatus.setShadowLayer(20, 0, 0, Color.parseColor("#FF2979FF"));
        }
    }

    private void showResult(String message) {
        textViewResult.setText(message);

        // En mode tournoi, adapter le message du bouton
        Button btnNewGame = findViewById(R.id.btnNewGame);
        if (tournamentMode && currentTournamentGame < tournamentSize) {
            btnNewGame.setText("Partie suivante");
        } else if (tournamentMode) {
            btnNewGame.setText("Nouveau Tournoi");
        }

        resultLayout.setVisibility(View.VISIBLE);
        updateTournamentDisplay();
    }

    private void resetGame() {
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                buttons[i][j].setText("");
                buttons[i][j].setTextColor(Color.WHITE);
                buttons[i][j].setShadowLayer(0, 0, 0, Color.TRANSPARENT);
                buttons[i][j].setScaleX(1.0f);
                buttons[i][j].setScaleY(1.0f);
                buttons[i][j].setRotation(0);
            }
        }

        gameActive = true;
        playerXTurn = true;
        updateStatus();
        resultLayout.setVisibility(View.GONE);
    }

    private void resetScores() {
        // Archiver les scores actuels avant de réinitialiser
        if (playerXWins > 0 || playerOWins > 0 || draws > 0) {
            databaseHelper.archiveSession(playerXName, playerOName, playerXWins, playerOWins, draws);
            databaseHelper.clearOldArchives();
        }

        playerXWins = 0;
        playerOWins = 0;
        draws = 0;
        resetGame();

        // Animation de confirmation
        Toast toast = Toast.makeText(this, "Scores réinitialisés et archivés!", Toast.LENGTH_SHORT);
        View toastView = toast.getView();
        if (toastView != null) {
            toastView.setBackgroundColor(Color.parseColor("#4CAF50"));
            TextView toastText = toastView.findViewById(android.R.id.message);
            toastText.setTextColor(Color.WHITE);
            toastText.setShadowLayer(10, 0, 0, Color.parseColor("#388E3C"));
        }
        toast.show();
    }

    private void showEditNamesDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_edit_names, null);

        final EditText editTextPlayer1 = dialogView.findViewById(R.id.edit_text_player1);
        final EditText editTextPlayer2 = dialogView.findViewById(R.id.edit_text_player2);

        // Appliquer l'effet néon aux EditText
        applyNeonEditTextEffect(editTextPlayer1, true);
        applyNeonEditTextEffect(editTextPlayer2, false);

        editTextPlayer1.setText(playerXName);
        editTextPlayer2.setText(playerOName);

        builder.setView(dialogView)
                .setTitle("Modifier les noms")
                .setPositiveButton("Sauvegarder", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        String newPlayerXName = editTextPlayer1.getText().toString();
                        String newPlayerOName = editTextPlayer2.getText().toString();

                        if (newPlayerXName.isEmpty()) newPlayerXName = "Joueur1";
                        if (newPlayerOName.isEmpty()) newPlayerOName = "Adversaire";

                        // Si les noms changent, archiver les anciens scores
                        if (!newPlayerXName.equals(playerXName) || !newPlayerOName.equals(playerOName)) {
                            if (playerXWins > 0 || playerOWins > 0 || draws > 0) {
                                databaseHelper.archiveSession(playerXName, playerOName, playerXWins, playerOWins, draws);
                                databaseHelper.clearOldArchives();
                                Toast.makeText(MainActivity.this, "Anciens scores archivés!", Toast.LENGTH_SHORT).show();
                            }

                            // Réinitialiser les scores pour les nouveaux joueurs
                            playerXWins = 0;
                            playerOWins = 0;
                            draws = 0;
                        }

                        playerXName = newPlayerXName;
                        playerOName = newPlayerOName;

                        textViewPlayerX.setText(playerXName);
                        textViewPlayerO.setText(playerOName);
                        updateStatus();
                    }
                })
                .setNegativeButton("Annuler", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void applyNeonEditTextEffect(EditText editText, boolean isRed) {
        editText.setBackgroundColor(Color.TRANSPARENT);
        editText.setTextColor(Color.WHITE);
        if (isRed) {
            editText.setShadowLayer(10, 0, 0, Color.parseColor("#FFFF1744"));
        } else {
            editText.setShadowLayer(10, 0, 0, Color.parseColor("#FF2979FF"));
        }
    }

    private void showChangePlayerConfirmation() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Changer les joueurs")
                .setMessage("Voulez-vous vraiment changer les joueurs ?\n\nLes scores actuels seront archivés et réinitialisés.")
                .setPositiveButton("Oui", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        switchPlayer();
                    }
                })
                .setNegativeButton("Non", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void switchPlayer() {
        // Archiver les scores actuels avant de changer
        if (playerXWins > 0 || playerOWins > 0 || draws > 0) {
            databaseHelper.archiveSession(playerXName, playerOName, playerXWins, playerOWins, draws);
            databaseHelper.clearOldArchives();
        }

        // Échanger les noms des joueurs
        String temp = playerXName;
        playerXName = playerOName;
        playerOName = temp;

        // Réinitialiser les scores pour la nouvelle session
        playerXWins = 0;
        playerOWins = 0;
        draws = 0;

        textViewPlayerX.setText(playerXName);
        textViewPlayerO.setText(playerOName);

        // Redémarrer le jeu
        resetGame();

        Toast.makeText(this, "Joueurs changés! Scores archivés.", Toast.LENGTH_SHORT).show();
    }

    private void showScores() {
        Intent intent = new Intent(this, ScoresActivity.class);
        // Passer les scores actuels ET l'accès à la base de données
        intent.putExtra("PLAYER_X_WINS", playerXWins);
        intent.putExtra("PLAYER_O_WINS", playerOWins);
        intent.putExtra("DRAWS", draws);
        intent.putExtra("PLAYER_X_NAME", playerXName);
        intent.putExtra("PLAYER_O_NAME", playerOName);
        startActivity(intent);
    }
}