package ori.project.shootingmaster;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    Button btnGame, btnHowToPlay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnGame = findViewById(R.id.btnGame);
        btnHowToPlay = findViewById(R.id.btnHowToPlay);
        btnGame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent svc = new Intent(MainActivity.this, BackgroundSoundService.class);
                stopService(svc);
                startActivity(new Intent(MainActivity.this, GameActivity.class));
            }
        });
        btnHowToPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, InstructionsActivity.class));
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.share) {
            Toast.makeText(this, "Thanks for Sharing the app!", Toast.LENGTH_SHORT).show();
            Intent sendIntent = new Intent();
            sendIntent.setAction(Intent.ACTION_SEND);
            sendIntent.putExtra(Intent.EXTRA_TEXT, "I'm playing ShootingMaster!");
            sendIntent.setType("text/plain");
            startActivity(sendIntent);
        } else if (item.getItemId() == R.id.scores) {
            Intent intent = new Intent(this, ScoresActivity.class);
            startActivity(intent);
        }
        return true;
    }
}