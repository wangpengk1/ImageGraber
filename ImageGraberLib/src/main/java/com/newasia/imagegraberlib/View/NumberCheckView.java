package com.newasia.imagegraberlib.View;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.media.MediaPlayer;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

public class NumberCheckView extends View {
    private int radius = 0;
    private int mNumber = -1;
    private Paint mPaint1 = new Paint();
    private Paint mPaint2 = new Paint();
    public NumberCheckView(Context context) {
        super(context);
        initPaint();
    }

    public NumberCheckView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initPaint();
    }

    public NumberCheckView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = getDefaultSize(getSuggestedMinimumWidth(), widthMeasureSpec);
        int height = getDefaultSize(getSuggestedMinimumHeight(), heightMeasureSpec);
        setMeasuredDimension(60,60);
        radius = 60;
    }


    public void setNumber(int val)
    {
        mNumber = val;
        invalidate();
    }

    public int getmNumber()
    {
        return mNumber;
    }

    private void initPaint()
    {
        mPaint1.setAntiAlias(true);
        mPaint1.setColor(Color.WHITE);
        mPaint1.setStyle(Paint.Style.STROKE);
        mPaint1.setStrokeWidth(4);

        mPaint2.setAntiAlias(true);
        mPaint2.setColor(Color.parseColor("#42e695"));
        mPaint2.setStyle(Paint.Style.FILL_AND_STROKE);

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if(mNumber>0)
        {
            canvas.drawCircle(radius/2,radius/2,radius/2-2,mPaint2);
            drawCenterText(canvas,mNumber+"");
        }else canvas.drawCircle(radius/2,radius/2,radius/2-2,mPaint1);

    }


    private void drawCenterText(Canvas canvas,String text)
    {
        Paint textPaint = new Paint();
        textPaint.setColor(Color.WHITE);
        textPaint.setTextSize(40);
        textPaint.setStyle(Paint.Style.FILL);
        //该方法即为设置基线上那个点究竟是left,center,还是right  这里我设置为center
        textPaint.setTextAlign(Paint.Align.CENTER);

        Paint.FontMetrics fontMetrics = textPaint.getFontMetrics();
        float top = fontMetrics.top;//为基线到字体上边框的距离,即上图中的top
        float bottom = fontMetrics.bottom;//为基线到字体下边框的距离,即上图中的bottom

        int baseLineY = (int) (radius/2 - top/2 - bottom/2);//基线中间点的y轴计算公式

        canvas.drawText(text,radius/2,baseLineY,textPaint);
    }
}
