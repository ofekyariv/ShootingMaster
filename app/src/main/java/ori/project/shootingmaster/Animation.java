package ori.project.shootingmaster;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;

public class Animation {
    //frameInterval == 30ms
    private static final int FRAME_INTERVAL = 30;
    //whether animation play isFinished
    boolean isFinished = false;
    //lastFramePlayTime
    private long lastFramePlayTime = 0;
    //frameID
    private int frameID = 0;
    //frameCount
    private int frameNum = 0;
    //animation of picture resource
    private Bitmap[] frameBitmaps = null;
    //isLoop
    private boolean isLoop = false;

    //constructor
    public Animation(Bitmap[] frameBitmaps, boolean isLoop) {
        this.frameNum = frameBitmaps.length;
        this.frameBitmaps = frameBitmaps;
        this.isLoop = isLoop;
    }

    //reset animation
    public void reset() {
        lastFramePlayTime = 0;
        frameID = 0;
        isFinished = false;
    }

    //draw animation
    public boolean DrawAnimation(Canvas canvas, Paint paint, int x, int y) {
        //if not finished, continue
        if (!isFinished) {
            canvas.drawBitmap(frameBitmaps[frameID], x, y, paint);
            long time = System.currentTimeMillis();
            if (time - lastFramePlayTime > FRAME_INTERVAL) {
                frameID++;
                lastFramePlayTime = time;
                if (frameID >= frameNum) {
                    //animation play over
                    isFinished = true;
                    if (isLoop) {
                        //loop
                        isFinished = false;
                        frameID = 0;
                    }
                }
            }
        }
        return isFinished;
    }
}

