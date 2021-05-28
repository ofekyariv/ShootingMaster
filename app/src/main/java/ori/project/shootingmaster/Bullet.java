package ori.project.shootingmaster;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;

public class Bullet {
    //position of bullet
    public int bulletX = 0;
    public int bulletY = 0;

    //BULLET_STEP_X
    int BULLET_STEP_X = 0;

    // BULLET_STEP_Y
    int BULLET_STEP_Y = 15;

    // whether  bullet  is disappeared
    boolean isVisible = false;
    Context context;

    //bulletAnimation
    private Animation bulletAnimation;

    public Bullet(Context context, Bitmap[] frameBitmaps) {
        this.context = context;
        bulletAnimation = new Animation(frameBitmaps, true);
    }

    // initial position
    public void init(int x, int y) {
        bulletX = x;
        bulletY = y;
        isVisible = true;
    }

    // draw bullet
    public void DrawBullet(Canvas canvas, Paint paint) {
        if (isVisible) {
            bulletAnimation.DrawAnimation(canvas, paint, bulletX, bulletY);
        }
    }

     // update postion
    public void UpdateBullet(int direction) {
        if (isVisible) {
            bulletY -= direction * BULLET_STEP_Y;
            bulletX -= BULLET_STEP_X;
            if (bulletY < 0 || bulletY > GameActivity.screenHeight) {
                isVisible = false;
            }
            if (bulletX < 0 || bulletX > GameActivity.screenWidth) {
                isVisible = false;
            }
        }
    }
}
