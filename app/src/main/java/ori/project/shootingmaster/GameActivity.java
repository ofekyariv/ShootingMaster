package ori.project.shootingmaster;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.InputStream;
import java.util.ArrayList;

public class GameActivity extends AppCompatActivity {

    private static final boolean GAME_RUNNING = false;
    GameView gameView = null;
    int count = 0;
    static int screenWidth = 0;
    static int screenHeight = 0;
    DisplayMetrics displaymetrics = null;
    BroadcastReceiver receiver;
    boolean flag = true;
    private boolean gameState = GAME_RUNNING;
    private MediaPlayer mp_background;
    private MediaPlayer mp_bit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // get sreen height and weight
        displaymetrics = new DisplayMetrics();
        GameActivity.this.getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        screenWidth = displaymetrics.widthPixels;
        screenHeight = displaymetrics.heightPixels;
        
        gameView = new GameView(GameActivity.this);

        //audio
        mp_background = MediaPlayer.create(this, R.raw.main);
        mp_bit = MediaPlayer.create(this, R.raw.blaster);
        //play background music
        mp_background.start();

        setContentView(gameView);
    }

    @Override
    protected void onResume() {
        super.onResume();
        receiver = new BroadcastCharging();
        IntentFilter ifilter = new IntentFilter();
        ifilter.addAction(Intent.ACTION_POWER_CONNECTED);
        ifilter.addAction(Intent.ACTION_POWER_DISCONNECTED);
        registerReceiver(receiver, ifilter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(receiver);
    }

    @Override
    protected void onDestroy() {
        mp_background.stop();
        super.onDestroy();
    }


    public boolean onTouchEvent(MotionEvent event) {
        // get the touch coordinate
        int x = (int) event.getX();
        int y = (int) event.getY();
        switch (event.getAction()) {
            // ACTION_DOWN
            case MotionEvent.ACTION_DOWN:
                gameView.UpdateTouchEvent(x, y);
                break;
            // ACTION_MOVE
            case MotionEvent.ACTION_MOVE:
                gameView.UpdateTouchEvent(x, y);
                break;
            // ACTION_UP
            case MotionEvent.ACTION_UP:
                gameView.UpdateTouchEvent(x, y);
                break;
        }
        return false;
    }


    private class GameView extends SurfaceView implements SurfaceHolder.Callback, Runnable {
        final static int AIRCRAFT_ALIVE_FRAME_NUM = 6;
        final static int AIRCRAFT_DEAD_FRAME_NUM = 6;
        final static int ENEMY_NUM = 5;
        final static int ENEMY_ALIVE_FRAME_NUM = 1;
        final static int ENEMY_DEAD_FRAME_NUM = 6;
        final int ENEMY_POS_OFFSET = screenWidth / ENEMY_NUM;
        public int touchPosX = 600;
        public int touchPosY = 900;
        Paint paint;
        Aircraft aircraft = null;
        Enemy[] enemies = null;
        private ArrayList<Score> scores;
        private FirebaseAuth firebaseAuth;
        private Bitmap backgroundBitmap = null;

        //game thread
        private Thread mainThread = null;

        //thread loop flag
        private boolean isThreadRunning = false;
        private SurfaceHolder surfaceHolder;
        private Canvas canvas = null;
        private Context context;

        //constructor
        public GameView(Context context) {
            super(context);
            this.context = context;
            this.paint = new Paint();
            surfaceHolder = getHolder();
            surfaceHolder.addCallback(this);
            setFocusable(true);
            init();
        }

        private void init() {

            backgroundBitmap = Bitmap.createBitmap(displaymetrics, screenWidth, screenHeight, Bitmap.Config.RGB_565);
            //Here the enemy walking animation on a frame
            Bitmap[] aliveEnemyFrameBitmaps = new Bitmap[ENEMY_ALIVE_FRAME_NUM];
            aliveEnemyFrameBitmaps[0] = ReadBitMap(context, R.drawable.enemy);

            //Enemy Death Animation
            Bitmap[] deadEnemyFrameBitmaps = new Bitmap[ENEMY_DEAD_FRAME_NUM];
            for (int i = 0; i < ENEMY_DEAD_FRAME_NUM; i++) {
                deadEnemyFrameBitmaps[i] = ReadBitMap(context, R.drawable.bomb_enemy_0 + i);
            }
            //Create an enemy object
            enemies = new Enemy[ENEMY_NUM];
            for (int i = 0; i < ENEMY_NUM; i++) {
                enemies[i] = new Enemy(context, aliveEnemyFrameBitmaps, deadEnemyFrameBitmaps);
                enemies[i].init(i * ENEMY_POS_OFFSET, 0);
            }
            Bitmap[] aliveAircraftFrameBitmaps = new Bitmap[AIRCRAFT_ALIVE_FRAME_NUM];
            for (int i = 0; i < AIRCRAFT_ALIVE_FRAME_NUM; i++) {
                aliveAircraftFrameBitmaps[i] = ReadBitMap(context, R.drawable.spaceship);
            }
            Bitmap[] deadAircraftFrameBitmaps = new Bitmap[AIRCRAFT_DEAD_FRAME_NUM];
            for (int i = 0; i < AIRCRAFT_DEAD_FRAME_NUM; i++) {
                deadAircraftFrameBitmaps[i] = ReadBitMap(context, R.drawable.bomb_enemy_0 + i);
            }
            aircraft = new Aircraft(context, aliveAircraftFrameBitmaps, deadAircraftFrameBitmaps);
        }

        //Read the image of the local resource
        public Bitmap ReadBitMap(Context context, int resId) {
            BitmapFactory.Options opt = new BitmapFactory.Options();
            opt.inPreferredConfig = Bitmap.Config.RGB_565;
            InputStream is = context.getResources().openRawResource(resId);
            return BitmapFactory.decodeStream(is, null, opt);
        }

        protected void Draw() {
            if (gameState == GAME_RUNNING) {
                renderBg();
                updateBg();
            } else {
                paint = new Paint();
                paint.setTextSize((float) 100.0);
                paint.setColor(Color.RED);
                canvas.drawText("SCORE: " + count + "", 300, 400, paint);
                Score score = new Score();
                firebaseAuth = FirebaseAuth.getInstance();
                score.setUserName(firebaseAuth.getCurrentUser().getEmail());
                score.setKey(firebaseAuth.getUid());
                score.setScore(count);
                newScore(score);
                isThreadRunning = false;
            }
        }

        private void newScore(Score score) {
            FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
            DatabaseReference scoreDB = firebaseDatabase.getReference("Ranking");
            scores = new ArrayList<>();
            scoreDB.addListenerForSingleValueEvent(new ValueEventListener() {
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
                        } catch (Exception ignore) {}
                    }
                    for (Score s : scores) {
                        if (score.compareTo(s) > 0) {
                            if (s.getScore() < score.getScore()) {
                                scoreDB.child(score.getKey()).removeValue();
                            } else {
                                flag = false;
                            }
                        }
                    }
                    firebaseAuth = FirebaseAuth.getInstance();
                    if (flag) {
                        scoreDB.child(firebaseAuth.getUid()).setValue(score);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(GameActivity.this, "error", Toast.LENGTH_SHORT).show();
                }
            });
        }

        //refresh
        public void renderBg() {
            canvas.drawBitmap(backgroundBitmap, 0, 0, paint);
            canvas.drawBitmap(backgroundBitmap, 0, 0, paint);

            //refresh aircraft
            gameState = aircraft.DrawAircraft(canvas, paint);

            //refresh enemy
            for (int i = 0; i < ENEMY_NUM; i++) {
                enemies[i].DrawEnemy(canvas, paint);
            }
        }

        private void updateBg() {
            //renew xy
            aircraft.UpdateAircraft(touchPosX, touchPosY);

            //refresh enemy
            for (int i = 0; i < ENEMY_NUM; i++) {
                enemies[i].UpdateEnemy(screenHeight, ENEMY_NUM, ENEMY_POS_OFFSET);
            }

            //collide
            Collision(aircraft);
            Collision(enemies);
        }

        public void Collision(Aircraft aircraft) {
            //aircraft bullet collide enemy
            for (int i = 0; i < Aircraft.BULLET_NUM; i++) {
                for (int j = 0; j < ENEMY_NUM; j++) {
                    if (enemies[j].isAlive && (aircraft.aircraftBullets[i].bulletX >= enemies[j].enemyX - 10) && (aircraft.aircraftBullets[i].bulletX <= enemies[j].enemyX + 40)
                            && (aircraft.aircraftBullets[i].bulletY >= enemies[j].enemyY - 10) && (aircraft.aircraftBullets[i].bulletY <= enemies[j].enemyY + 10)) {
                        enemies[j].isAlive = false;
                        aircraft.aircraftBullets[i].isVisible = false;
                        mp_bit.start();
                        count++;
                    }
                }
            }
        }

        public void Collision(Enemy[] enemies) {
            //enemy or  bullet collide aircraft
            for (int i = 0; i < ENEMY_NUM; i++) {
                if ((aircraft.aircraftX >= enemies[i].enemyX - 40) && (aircraft.aircraftX <= enemies[i].enemyX + 40)
                        && (aircraft.aircraftY >= enemies[i].enemyY - 40) && (aircraft.aircraftY <= enemies[i].enemyY + 40)

                ) {
                    enemies[i].isAlive = false;
                    aircraft.isAlive = false;

                }
                for (int j = 0; j < Enemy.BULLET_NUM; j++) {
                    if ((aircraft.aircraftX >= enemies[i].enemyBullets[j].bulletX - 120) && (aircraft.aircraftX <= enemies[i].enemyBullets[j].bulletX)
                            && (aircraft.aircraftY >= enemies[i].enemyBullets[j].bulletY - 15) && (aircraft.aircraftY <= enemies[i].enemyBullets[j].bulletY + 15)
                    ) {
                        enemies[i].enemyBullets[j].isVisible = false;
                        aircraft.isAlive = false;
                    }
                }
            }
        }


        public void UpdateTouchEvent(int x, int y) {
            // renew position
            if (gameState == GAME_RUNNING) {
                touchPosX = x - 77;
                touchPosY = y - 400;
            }
        }

        @Override
        public void run() {
            while (isThreadRunning) {
                //thread lock
                synchronized (surfaceHolder) {
                    //lock canvas
                    canvas = surfaceHolder.lockCanvas();
                    Draw();
                    //unlock and display
                    surfaceHolder.unlockCanvasAndPost(canvas);
                }
                try {
                    Thread.sleep(25);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            if (!isThreadRunning) {
                try {
                    Thread.sleep(2000);
                    startActivity(new Intent(GameActivity.this, ScoresActivity.class));
                    finish();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        @Override
        public void surfaceChanged(SurfaceHolder arg0, int arg1, int arg2, int arg3) {}

        @Override
        public void surfaceCreated(SurfaceHolder arg0) {
            //start game thread
            isThreadRunning = true;
            mainThread = new Thread(this);
            mainThread.start();
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder arg0) {
            isThreadRunning = false;
        }
    }
}

