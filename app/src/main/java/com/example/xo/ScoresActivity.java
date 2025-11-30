package com.example.xo;

import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TabHost;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;
import java.util.List;

public class ScoresActivity extends AppCompatActivity {

    private ListView listViewCurrentScores;
    private ListView listViewArchives;
    private Button buttonBack;
    private DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scores);

        databaseHelper = new DatabaseHelper(this);

        initializeTabs();
        loadCurrentScores();
        loadArchives();
    }

    @Override
    protected void onDestroy() {
        databaseHelper.close();
        super.onDestroy();
    }

    private void initializeTabs() {
        TabHost tabHost = findViewById(R.id.tabHost);
        tabHost.setup();

        // Tab 1: Scores Actuels
        TabHost.TabSpec spec1 = tabHost.newTabSpec("Scores Actuels");
        spec1.setContent(R.id.tabCurrentScores);
        spec1.setIndicator("Scores Actuels");
        tabHost.addTab(spec1);

        // Tab 2: Archives
        TabHost.TabSpec spec2 = tabHost.newTabSpec("Archives");
        spec2.setContent(R.id.tabArchives);
        spec2.setIndicator("Archives");
        tabHost.addTab(spec2);

        listViewCurrentScores = findViewById(R.id.list_view_current_scores);
        listViewArchives = findViewById(R.id.list_view_archives);
        buttonBack = findViewById(R.id.button_back);

        buttonBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void loadCurrentScores() {
        Bundle extras = getIntent().getExtras();
        int playerXWins = extras != null ? extras.getInt("PLAYER_X_WINS", 0) : 0;
        int playerOWins = extras != null ? extras.getInt("PLAYER_O_WINS", 0) : 0;
        int draws = extras != null ? extras.getInt("DRAWS", 0) : 0;
        String playerXName = extras != null ? extras.getString("PLAYER_X_NAME", "Joueur1") : "Joueur1";
        String playerOName = extras != null ? extras.getString("PLAYER_O_NAME", "Adversaire") : "Adversaire";

        List<String> scores = new ArrayList<>();
        scores.add("🎮 SESSION EN COURS");
        scores.add(playerXName + ": " + playerXWins + " victoire" + (playerXWins > 1 ? "s" : ""));
        scores.add(playerOName + ": " + playerOWins + " victoire" + (playerOWins > 1 ? "s" : ""));
        scores.add("Matchs nuls: " + draws);

        if (playerXWins > playerOWins) {
            scores.add("🏆 Meilleur: " + playerXName);
        } else if (playerOWins > playerXWins) {
            scores.add("🏆 Meilleur: " + playerOName);
        } else if (playerXWins > 0 || playerOWins > 0) {
            scores.add("🤝 Égalité parfaite!");
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1, scores);
        listViewCurrentScores.setAdapter(adapter);
    }

    private void loadArchives() {
        List<String> archives = new ArrayList<>();
        archives.add("📁 SESSIONS ARCHIVÉES");

        Cursor cursor = databaseHelper.getAllArchives();
        if (cursor != null && cursor.moveToFirst()) {
            do {
                String player1 = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PLAYER1_NAME));
                String player2 = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PLAYER2_NAME));
                int wins1 = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PLAYER1_WINS));
                int wins2 = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PLAYER2_WINS));
                int draws = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_DRAWS));
                String date = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_DATE));

                archives.add("📅 " + date);
                archives.add("   " + player1 + " vs " + player2);
                archives.add("   " + wins1 + "-" + wins2 + " (" + draws + " nuls)");
                archives.add("");

            } while (cursor.moveToNext());
            cursor.close();
        } else {
            archives.add("Aucune archive pour le moment");
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1, archives);
        listViewArchives.setAdapter(adapter);
    }
}