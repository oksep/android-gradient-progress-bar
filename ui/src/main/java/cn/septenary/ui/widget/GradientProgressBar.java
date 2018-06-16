package cn.septenary.ui.widget;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.SweepGradient;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;

/**
 * Created by Septenary on 2018/6/16.
 *
 * <p><img src="https://github.com/Ryfthink/android-gradient-progress-bar/blob/master/arts/preview.gif"></p>
 * <pre>
 * &lt;cn.septenary.ui.widget.GradientProgressBar
 *    android:layout_width="320dp"
 *    android:layout_height="320dp"
 *    app:rotate="-90"
 *    app:progress_max="100"
 *    app:progress="85"
 *    app:anim_duration="250"
 *    app:border_background="#EEE"
 *    app:start_color="#4CDEF6"
 *    app:end_color="#0070E3"
 *    app:border_width="80dp" /&gt;
 * </pre>
 */
public class GradientProgressBar extends View {

    private int startColor;
    private int endColor;
    private float borderWidth;
    private int borderBgColor;
    private int angleOffset;
    private float sweep;
    private int progress;
    private int progressMax;

    private Paint borderBackgroundPaint; // for border background
    private Paint borderGradientPaint; // for border foreground
    private Paint startCirclePaint; // for start circle
    private Paint endCirclePaint; // for end circle
    private Paint endCircleGradientPaint; // for end circle

    private Matrix rotateMatrix = new Matrix(); // for end circle gradient shader

    private RectF borderRect; // for border rect
    private RectF circleRect; // for start & end circle

    private int animDuration;
    private ValueAnimator animator;

    private Paint debugPaint;

    public GradientProgressBar(Context context, AttributeSet attrs) {
        super(context, attrs, 0);
        initConfig(context, attrs);
    }

    public GradientProgressBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initConfig(context, attrs);
    }

    private void initConfig(Context context, AttributeSet attrs) {
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.GradientProgressBar);

        this.startColor = a.getColor(R.styleable.GradientProgressBar_start_color, Color.GREEN);
        this.endColor = a.getColor(R.styleable.GradientProgressBar_end_color, Color.BLUE);
        this.borderWidth = a.getDimensionPixelSize(R.styleable.GradientProgressBar_border_width, 8);
        this.borderBgColor = a.getColor(R.styleable.GradientProgressBar_border_background, Color.LTGRAY);
        this.angleOffset = a.getInteger(R.styleable.GradientProgressBar_rotate, 0);
        this.progress = a.getInteger(R.styleable.GradientProgressBar_progress, 0);
        this.progressMax = a.getInteger(R.styleable.GradientProgressBar_progress_max, 100);
        this.animDuration = a.getColor(R.styleable.GradientProgressBar_anim_duration, 250);

        a.recycle();

        borderBackgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        borderBackgroundPaint.setStyle(Paint.Style.STROKE);
        borderBackgroundPaint.setStrokeWidth(this.borderWidth);
        borderBackgroundPaint.setColor(borderBgColor);

        borderGradientPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        borderGradientPaint.setStyle(Paint.Style.STROKE);
        borderGradientPaint.setStrokeWidth(this.borderWidth);

        startCirclePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        startCirclePaint.setStyle(Paint.Style.FILL);
        startCirclePaint.setColor(startColor);

        endCirclePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        endCirclePaint.setStyle(Paint.Style.FILL);
        endCirclePaint.setColor(endColor);

        endCircleGradientPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        endCircleGradientPaint.setStyle(Paint.Style.FILL);

        debugPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        debugPaint.setStyle(Paint.Style.STROKE);
        debugPaint.setColor(Color.DKGRAY);

        borderRect = new RectF();
        circleRect = new RectF();

        this.setProgress(this.progress, false);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        int width, height;

        switch (widthMode) {
            case MeasureSpec.EXACTLY:
                width = widthSize;
                break;
            case MeasureSpec.AT_MOST:
                width = Math.min(widthSize, heightSize);
                break;
            case MeasureSpec.UNSPECIFIED:
            default:
                width = 100;
                break;
        }

        switch (heightMode) {
            case MeasureSpec.EXACTLY:
                height = heightSize;
                break;
            case MeasureSpec.AT_MOST:
                height = Math.min(widthSize, heightSize);
                break;
            case MeasureSpec.UNSPECIFIED:
            default:
                height = 100;
                break;
        }

        int size = Math.min(width, height);
        int newMeasureSpec = MeasureSpec.makeMeasureSpec(size, MeasureSpec.EXACTLY);
        super.onMeasure(newMeasureSpec, newMeasureSpec);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldW, int oldH) {
        super.onSizeChanged(w, h, oldW, oldH);
        this.reset(w, h);
    }

    private void reset(int width, int height) {
        // border rect
        borderRect.set(0, 0, width, height);
        borderRect.inset(this.borderWidth / 2, this.borderWidth / 2);

        // start & end circle rect
        circleRect.set(0, 0, borderWidth, 0 + borderWidth);
        circleRect.offsetTo(borderRect.width(), borderRect.height() / 2);

        // border gradient
        borderGradientPaint.setShader(new SweepGradient(borderRect.centerX(), borderRect.centerY(), startColor, endColor));

        // end circle gradient
        SweepGradient sweepGradient = new SweepGradient(borderRect.centerX(), borderRect.centerY(), startColor, endColor);
        rotateMatrix.reset();
        sweepGradient.setLocalMatrix(rotateMatrix);
        endCircleGradientPaint.setShader(sweepGradient);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        int saveCount = canvas.save();
        canvas.rotate(angleOffset, borderRect.centerX(), borderRect.centerY());

        drawBorderBackground(canvas);

        if (sweep > 0) {
            drawBorderArc(canvas);
            if (sweep < 180) {
                drawEndGradientCircle(canvas);
                drawStartSemicircle(canvas);
            } else {
                drawStartSemicircle(canvas);
                drawEndGradientCircle(canvas);
            }
            drawEndSemicircle(canvas);
        }

        canvas.restoreToCount(saveCount);
    }

    private void drawBorderBackground(Canvas canvas) {
        canvas.drawArc(borderRect, 0, 360, false, borderBackgroundPaint);
    }

    // border gradient
    private void drawBorderArc(Canvas canvas) {
        canvas.drawArc(borderRect, 0, sweep, false, borderGradientPaint);
    }

    // start circle
    private void drawStartSemicircle(Canvas canvas) {
        canvas.drawArc(circleRect, 10, -200, true, startCirclePaint);

    }

    // end gradient circle
    private void drawEndGradientCircle(Canvas canvas) {
        int count = canvas.save();
        canvas.rotate(sweep, borderRect.centerX(), borderRect.centerY());
        rotateMatrix.setRotate(-sweep, borderRect.centerX(), borderRect.centerY());
        endCircleGradientPaint.getShader().setLocalMatrix(rotateMatrix);
        canvas.drawOval(circleRect, endCircleGradientPaint);
        canvas.restoreToCount(count);
    }

    // end circle
    private void drawEndSemicircle(Canvas canvas) {
        int count = canvas.save();
        canvas.rotate(sweep, borderRect.centerX(), borderRect.centerY());
        endCirclePaint.setColor(computeCentralColor(startColor, endColor, sweep / 360));
        canvas.drawArc(circleRect, 0, 180, true, endCirclePaint);
        canvas.restoreToCount(count);
    }

    // for debug
    private void drawGuidelines(Canvas canvas) {
        canvas.drawRect(1, 1, getWidth() - 1, getHeight() - 1, debugPaint);
        canvas.drawCircle(borderRect.centerX(), borderRect.centerY(), borderRect.width() / 2, debugPaint);
        canvas.drawLine(0, getHeight() / 2, getWidth(), getHeight() / 2, debugPaint);
        canvas.drawLine(getWidth() / 2, 0, getWidth() / 2, getHeight(), debugPaint);
    }

    /**
     * <a href="http://androidxref.com/7.1.2_r36/xref/frameworks/base/tools/layoutlib/bridge/src/android/graphics/Gradient_Delegate.java#127">Ref</a>
     */
    private int computeCentralColor(int start, int end, float percent) {
        int a = computeChannel(start >> 24 & 0xFF, end >> 24 & 0xFF, percent);
        int r = computeChannel(start >> 16 & 0xFF, end >> 16 & 0xFF, percent);
        int g = computeChannel(start >> 8 & 0xFF, end >> 8 & 0xFF, percent);
        int b = computeChannel(start & 0xFF, end & 0xFF, percent);
        return a << 24 | r << 16 | g << 8 | b;
    }

    // get value in c1..c2
    private int computeChannel(int c1, int c2, float percent) {
        return c1 + (int) ((percent * (c2 - c1)) + .5);
    }

    // invalidate sweep drawing
    private void updateSweep(float value) {
        float sweep = (value / progressMax) * 360;
        this.sweep = sweep < 360 ? sweep : 359;
        this.invalidate();
    }

    public void setProgress(int value, boolean anim) {
        if (value >= progressMax) {
            value = progressMax;
        }
        if (value < 0) {
            value = 0;
        }
        if (anim) {
            if (animator != null) {
                animator.cancel();
            }
            animator = ValueAnimator.ofFloat(this.progress, value);
            animator.setInterpolator(new AccelerateDecelerateInterpolator());
            animator.setDuration(this.animDuration);
            animator.setTarget(this.sweep);
            animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    updateSweep((float) animation.getAnimatedValue());
                }
            });
            animator.start();
        } else {
            updateSweep(value);
        }
        this.progress = value;
    }
}