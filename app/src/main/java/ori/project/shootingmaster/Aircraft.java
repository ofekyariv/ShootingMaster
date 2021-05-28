package ori.project.shootingmaster;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;

import java.io.InputStream;

public class Aircraft {

    static final int AIRCRAFT_STEP = 50;
    static final int BULLET_NUM = 25;
    static final int BULLET_FRAME_NUM = 4;

    //BULLET_INTERVAL
    final static int BULLET_INTERVAL = 500;

    //BULLET_UP_OFFSET
    final static int BULLET_UP_OFFSET = 40;

    // BULLET_LEFT_OFFSET
    final static int BULLET_LEFT_OFFSET = -59;
    public int aircraftX = 600;
    public int aircraftY = 600;
    public Bullet[] aircraftBullets = null;

    //bulletCount
    public int bulletCount = 0;

    //lastBulletSendTime
    public Long lastBulletSendTime = 0L;

    //state
    boolean isAlive = true;
    Context context = null;

    //aliveEnemyAnimation
    private Animation aliveAircraftAnimation = null;

    //deadEnemyAnimation
    private Animation deadAircraftAnimation = null;

    public Aircraft(Context context, Bitmap[] frameBitmap, Bitmap[] deadBitmap) {
        this.context = context;
        aliveAircraftAnimation = new Animation(frameBitmap, true);
        deadAircraftAnimation = new Animation(deadBitmap, false);
        init(600, 600);
    }

    //inintialize position
    public void init(int x, int y) {
        aircraftX = x;
        aircraftY = y;

        isAlive = true;

        aliveAircraftAnimation.reset();
        deadAircraftAnimation.reset();

        Bitmap[] bulletFrameBitmaps = new Bitmap[BULLET_FRAME_NUM];
        for (int i = 0; i < BULLET_FRAME_NUM; i++) {
            bulletFrameBitmaps[i] = ReadBitMap(context, R.drawable.bullet);
        }
        aircraftBullets = new Bullet[BULLET_NUM];
        for (int i = 0; i < BULLET_NUM; i++) {
            aircraftBullets[i] = new Bullet(context, bulletFrameBitmaps);
        }
    }

    //draw
    public boolean DrawAircraft(Canvas canvas, Paint paint) {
        if (isAlive) {
            aliveAircraftAnimation.DrawAnimation(canvas, paint, aircraftX, aircraftY);
            //bullet annimation
            for (int i = 0; i < BULLET_NUM; i++) {
                aircraftBullets[i].DrawBullet(canvas, paint);
            }
            return false;
        } else {
            return deadAircraftAnimation.DrawAnimation(canvas, paint, aircraftX, aircraftY);

        }
    }

    //update
    public void UpdateAircraft(int touchPosX, int touchPosY) {
        if (isAlive) {
            if (aircraftX < touchPosX) {
                aircraftX += AIRCRAFT_STEP;
            } else {
                aircraftX -= AIRCRAFT_STEP;
            }
            if (aircraftY < touchPosY) {
                aircraftY += AIRCRAFT_STEP;
            } else {
                aircraftY -= AIRCRAFT_STEP;
            }
            if (Math.abs(aircraftX - touchPosX) <= AIRCRAFT_STEP) {
                aircraftX = touchPosX;
            }
            if (Math.abs(aircraftY - touchPosY) <= AIRCRAFT_STEP) {
                aircraftY = touchPosY;
            }
            //When the enemy state is dead and the death animation is finished, the enemy is not drawn
        }
        //update bullet
        for (int i = 0; i < BULLET_NUM; i++) {
            aircraftBullets[i].UpdateBullet(1);
        }

        //initilize bullet
        if (bulletCount < BULLET_NUM) {
            long now = System.currentTimeMillis();
            if (now - lastBulletSendTime >= BULLET_INTERVAL) {
                aircraftBullets[bulletCount].init(aircraftX - BULLET_LEFT_OFFSET, aircraftY - BULLET_UP_OFFSET);
                lastBulletSendTime = now;
                bulletCount++;
            }
        } else {
            bulletCount = 0;
        }
    }

    public Bitmap ReadBitMap(Context context, int resId) {
        BitmapFactory.Options opt = new BitmapFactory.Options();
        opt.inPreferredConfig = Bitmap.Config.RGB_565;
        InputStream is = context.getResources().openRawResource(resId);
        return BitmapFactory.decodeStream(is, null, opt);
    }
}
