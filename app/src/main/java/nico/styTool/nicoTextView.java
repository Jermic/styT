package nico.styTool;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Shader;
import android.util.AttributeSet;

public class nicoTextView extends android.support.v7.widget.AppCompatTextView
{

    private LinearGradient mLinearGradient;
    private Matrix mGradientMatrix;
    private Paint mPaint;
    private int mViewWidth = 0;
    private int mTranslate = 0;

    private boolean mAnimating = true;

    public nicoTextView(Context context, AttributeSet attrs)
    {
	super(context, attrs);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh)
    {
	super.onSizeChanged(w, h, oldw, oldh);
	if (mViewWidth == 0)
	{
	    mViewWidth = getMeasuredWidth();
	    if (mViewWidth > 0)
	    {
		mPaint = getPaint();
		mLinearGradient = new LinearGradient(-mViewWidth, 0, 0, 0,new int[] { 0x45B3B5, 0x45B3B5, 0x33ffffff },new float[] { 0, 0.5f, 1 }, Shader.TileMode.CLAMP);
		mPaint.setShader(mLinearGradient);
		mGradientMatrix = new Matrix();
	    }
	}
    }

    @Override
    protected void onDraw(Canvas canvas)
    {
	super.onDraw(canvas);
	if (mAnimating && mGradientMatrix != null)
	{
	    mTranslate += mViewWidth / 10;
	    if (mTranslate > 2 * mViewWidth)
	    {
		mTranslate = -mViewWidth;
	    }
	    mGradientMatrix.setTranslate(mTranslate, 0);
	    mLinearGradient.setLocalMatrix(mGradientMatrix);
	    postInvalidateDelayed(50);
	}
    }

}
