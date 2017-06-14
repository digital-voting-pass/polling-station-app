package com.digitalvotingpass.camera;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

import com.digitalvotingpass.digitalvotingpass.R;

/**
 * Creates a grey overlay with a rectangular transparent field, set by setRect()
 */
public class Overlay extends View {
    /**
     * Transparency of overlaid part in hex, 0-255
     */
    private static final int TRANSPARENCY = 0xA0;

    private Paint paint = new Paint();
    private Paint transparentPaint = new Paint();
    private Rect rect = new Rect(0,0,0,0);
    private PorterDuffXfermode xfermode;

    public Overlay(Context context, AttributeSet set) {
        super(context, set);
        xfermode = new PorterDuffXfermode(PorterDuff.Mode.CLEAR);
    }

    public void setMargins (int left, int top, int right, int bottom) {
        if (getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {
            ViewGroup.MarginLayoutParams p = (ViewGroup.MarginLayoutParams) getLayoutParams();
            p.setMargins(left, top, right, bottom);
            requestLayout();
        }
    }

    public void setRect(Rect rect) {
        this.rect = rect;
    }

    @Override
    public void onDraw(Canvas canvas) {
        setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        // The color I chose is just a random existing grey color
        paint.setColor(getResources().getColor(R.color.cardview_dark_background));
        paint.setAlpha(TRANSPARENCY);
        canvas.drawRect(0, 0, canvas.getWidth(), canvas.getHeight(), paint);

        transparentPaint.setAlpha(0xFF);
        transparentPaint.setXfermode(xfermode);
        canvas.drawRect(rect, transparentPaint);
    }
}
