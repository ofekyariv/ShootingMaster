package ori.project.shootingmaster;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class ScoresActivity extends AppCompatActivity implements AdapterView.OnItemLongClickListener {

    Button btnNewGame;
    DatabaseReference scoreDB;
    private ListView list;
    private ArrayList<Score> scores;
    private ScoresAdapter adapter;
    private FirebaseAuth firebaseAuth;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scores);
        list = findViewById(R.id.listScores);
        btnNewGame = findViewById(R.id.btnNewGame);
        btnNewGame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ScoresActivity.this, GameActivity.class));
            }
        });
        progressBar = findViewById(R.id.progressbar);
        progressBar.setVisibility(View.VISIBLE);

        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
        scoreDB = firebaseDatabase.getReference("Ranking");

        scores = new ArrayList<>();
        scoreDB.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                scores.clear();
                for (DataSnapshot data : dataSnapshot.getChildren()) {
                    Score temp = new Score();
                    try {
                        temp.setUserName(data.child("userName").getValue().toString());
                        temp.setScore(Integer.parseInt(data.child("score").getValue().toString()));
                        temp.setKey(data.child("key").getValue().toString());
                        scores.add(temp);
                    } catch (Exception ignore) {

                    }
                }
                Collections.sort(scores, new Comparator<Score>() {
                    public int compare(Score o1, Score o2) {
                        if (o1.getScore() > o2.getScore()) {
                            return 1;
                        } else {
                            return 0;
                        }
                    }
                });
                adapter = new ScoresAdapter(ScoresActivity.this, scores);
                list.setAdapter(adapter);
                list.setOnItemLongClickListener(ScoresActivity.this);
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ScoresActivity.this, "error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> adapterView, View view, final int position, long l) {
        //only admin can delete scores
        if (firebaseAuth.getUid().equals("K96QXiFWXYdkGIzOaPRHj4Qehnc2")) {
            AlertDialog.Builder alert = new AlertDialog.Builder(this);
            alert.setTitle("Delete Score");
            alert.setMessage("are you sure you want to delete the score?");
            alert.setPositiveButton("delete", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int which) {
                    String key = scores.get(position).getKey();
                    scoreDB.child(key).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            adapter.notifyDataSetChanged();
                        }
                    });
                    dialogInterface.dismiss();
                }
            });

            alert.setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.dismiss();
                }
            });
            alert.create();
            alert.show();
            adapter.notifyDataSetChanged();
            return true;
        } else {
            return false;
        }
    }
}