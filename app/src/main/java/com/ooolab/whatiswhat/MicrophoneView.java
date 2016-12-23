package com.ooolab.whatiswhat;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.speech.SpeechRecognizer;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;


/**
 * TODO: document your custom view class.
 */
public class MicrophoneView extends View {

    private static final int STATE_RECORDING = 0;
    private static final int STATE_PRESSED = 1;
    private int mState = STATE_RECORDING;

    private Bitmap mRecordingBitmap;
    private Bitmap mPressedBitmap;

    private Paint mPaint;
    private AnimatorSet mAnimatorSet = new AnimatorSet();

    private float mMinRadius;
    private float mMaxRadius;
    private float mCurrentRadius;

    public MicrophoneView(Context context) {
        super(context);
        init(null, 0);
    }

    public MicrophoneView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public MicrophoneView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs, defStyle);
    }

    private void init(AttributeSet attrs, int defStyle) {
        mRecordingBitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.mic_btn);
        mPressedBitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.mic_btn_pressed);

        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setColor(Color.argb(255, 0x9e, 0x9e, 0x9e));

        mMinRadius = dp2px(getContext(), 96) / 2;
        mCurrentRadius = mMinRadius;

        setClickable(true);
    }

    public static int dp2px(Context context, int dp){
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, context.getResources().getDisplayMetrics());
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mMaxRadius = Math.min(w, h) / 2;
        Log.d("", "MaxRadius: " + mMaxRadius);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int width = canvas.getWidth();
        int height = canvas.getHeight();

        if(mCurrentRadius > mMinRadius){
            canvas.drawCircle(width / 2, height / 2, mCurrentRadius, mPaint);
        }

        switch (mState){
            case STATE_RECORDING:
                canvas.drawBitmap(mRecordingBitmap, width / 2 - mMinRadius,  height / 2 - mMinRadius, mPaint);
                break;
            case STATE_PRESSED:
                canvas.drawBitmap(mPressedBitmap, width / 2 - mMinRadius,  height / 2 - mMinRadius, mPaint);
                break;
        }
    }

    public void animateRadius(float radius){
        if(radius <= mCurrentRadius){
            return;
        }
        if(radius > mMaxRadius){
            radius = mMaxRadius;
        }else if(radius < mMinRadius){
            radius = mMinRadius;
        }
        if(radius == mCurrentRadius){
            return;
        }
        if(mAnimatorSet.isRunning()){
            mAnimatorSet.cancel();
        }
        mAnimatorSet.playSequentially(
                ObjectAnimator.ofFloat(this, "CurrentRadius", getCurrentRadius(), radius).setDuration(50),
                ObjectAnimator.ofFloat(this, "CurrentRadius", radius, mMinRadius).setDuration(600)
        );
        mAnimatorSet.start();
    }

    public float getCurrentRadius() {
        return mCurrentRadius;
    }

    public void setCurrentRadius(float currentRadius) {
        mCurrentRadius = currentRadius;
        invalidate();
    }
}
