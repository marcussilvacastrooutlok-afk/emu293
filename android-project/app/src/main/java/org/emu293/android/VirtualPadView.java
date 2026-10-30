package org.emu293.android;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.util.SparseIntArray;
import android.view.MotionEvent;
import android.view.View;

public final class VirtualPadView extends View {
    private static final int SC_UP=82, SC_DOWN=81, SC_LEFT=80, SC_RIGHT=79;
    private static final int SC_Z=29, SC_X=27, SC_RETURN=40, SC_RSHIFT=229;
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final SparseIntArray active = new SparseIntArray();

    public VirtualPadView(Context context) {
        super(context);
        setLayerType(View.LAYER_TYPE_HARDWARE, null);
        setFocusable(false);
    }

    private void circle(Canvas c, float x, float y, float r, String label, boolean pressed) {
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(pressed ? 0xBFFFFFFF : 0x70808080);
        c.drawCircle(x, y, r, paint);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(Math.max(2f, r * 0.07f));
        paint.setColor(0xCFFFFFFF);
        c.drawCircle(x, y, r, paint);
        paint.setStyle(Paint.Style.FILL);
        paint.setTypeface(Typeface.create("sans-serif", Typeface.BOLD));
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setTextSize(r * 0.72f);
        paint.setColor(0xFFFFFFFF);
        c.drawText(label, x, y + paint.getTextSize() * 0.34f, paint);
    }

    private boolean isPressed(int scan) {
        for (int i=0;i<active.size();i++) if(active.valueAt(i)==scan) return true;
        return false;
    }

    @Override protected void onDraw(Canvas c) {
        super.onDraw(c);
        float w=getWidth(), h=getHeight();
        float r=Math.min(w,h)*0.055f;
        float dx=w*0.16f, dy=h*0.73f;
        circle(c,dx,dy-r*1.15f,r,"▲",isPressed(SC_UP));
        circle(c,dx,dy+r*1.15f,r,"▼",isPressed(SC_DOWN));
        circle(c,dx-r*1.15f,dy,r,"◀",isPressed(SC_LEFT));
        circle(c,dx+r*1.15f,dy,r,"▶",isPressed(SC_RIGHT));
        float ax=w*0.84f, ay=h*0.72f;
        circle(c,ax+r*1.1f,ay-r*0.75f,r*1.12f,"A",isPressed(SC_Z));
        circle(c,ax-r*1.1f,ay+r*0.75f,r*1.12f,"B",isPressed(SC_X));
        circle(c,w*0.44f,h*0.91f,r*0.72f,"SEL",isPressed(SC_RSHIFT));
        circle(c,w*0.56f,h*0.91f,r*0.72f,"START",isPressed(SC_RETURN));
    }

    private int hit(float x,float y) {
        float w=getWidth(),h=getHeight(),r=Math.min(w,h)*0.055f;
        float dx=w*0.16f,dy=h*0.73f;
        if (Math.hypot(x-dx,y-(dy-r*1.15f))<r*1.25f) return SC_UP;
        if (Math.hypot(x-dx,y-(dy+r*1.15f))<r*1.25f) return SC_DOWN;
        if (Math.hypot(x-(dx-r*1.15f),y-dy)<r*1.25f) return SC_LEFT;
        if (Math.hypot(x-(dx+r*1.15f),y-dy)<r*1.25f) return SC_RIGHT;
        float ax=w*0.84f,ay=h*0.72f;
        if (Math.hypot(x-(ax+r*1.1f),y-(ay-r*0.75f))<r*1.35f) return SC_Z;
        if (Math.hypot(x-(ax-r*1.1f),y-(ay+r*0.75f))<r*1.35f) return SC_X;
        if (Math.hypot(x-w*0.44f,y-h*0.91f)<r) return SC_RSHIFT;
        if (Math.hypot(x-w*0.56f,y-h*0.91f)<r) return SC_RETURN;
        return 0;
    }

    private void updatePointer(MotionEvent e,int index) {
        int id=e.getPointerId(index), old=active.get(id,0), next=hit(e.getX(index),e.getY(index));
        if(old!=next) {
            if(old!=0) Emu293SDLActivity.nativeSendKey(old,false);
            if(next!=0) Emu293SDLActivity.nativeSendKey(next,true);
            if(next==0) active.delete(id); else active.put(id,next);
        }
    }

    private void releasePointer(int id) {
        int old=active.get(id,0);
        if(old!=0) Emu293SDLActivity.nativeSendKey(old,false);
        active.delete(id);
    }

    @Override public boolean onTouchEvent(MotionEvent e) {
        int action=e.getActionMasked(), index=e.getActionIndex();
        if(action==MotionEvent.ACTION_DOWN || action==MotionEvent.ACTION_POINTER_DOWN) {
            updatePointer(e,index); invalidate(); return true;
        }
        if(action==MotionEvent.ACTION_MOVE) {
            for(int i=0;i<e.getPointerCount();i++) updatePointer(e,i);
            invalidate(); return true;
        }
        if(action==MotionEvent.ACTION_UP || action==MotionEvent.ACTION_POINTER_UP) {
            releasePointer(e.getPointerId(index)); invalidate(); return true;
        }
        if(action==MotionEvent.ACTION_CANCEL) {
            for(int i=active.size()-1;i>=0;i--) releasePointer(active.keyAt(i));
            invalidate(); return true;
        }
        return true;
    }
}
