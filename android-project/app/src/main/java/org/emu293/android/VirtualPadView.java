package org.emu293.android;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.util.SparseIntArray;
import android.view.MotionEvent;
import android.view.View;

/** Touch overlay: separated D-pad, analog-style 8-way stick and action buttons. */
public final class VirtualPadView extends View {
    private static final int UP=82, DOWN=81, LEFT=80, RIGHT=79;
    private static final int A=29, B=27, START=40, SELECT=229;
    private static final int[] KEYS={UP,DOWN,LEFT,RIGHT,A,B,START,SELECT};
    private final Paint p=new Paint(Paint.ANTI_ALIAS_FLAG);
    // Pointer -> bit mask. Refcounts prevent one finger releasing a key held by another.
    private final SparseIntArray pointers=new SparseIntArray();
    private final SparseIntArray dpadPointers=new SparseIntArray();
    private final int[] refs=new int[KEYS.length];
    private final int[] dpadRefs=new int[4];
    private float stickX,stickY,stickRadius;

    public VirtualPadView(Context c) {
        super(c);
        setLayerType(View.LAYER_TYPE_HARDWARE,null);
        setFocusable(false);
    }
    private int bit(int key) {
        for(int i=0;i<KEYS.length;i++) if(KEYS[i]==key) return 1<<i;
        return 0;
    }
    private boolean pressed(int key) { int b=bit(key); return b!=0 && refs[Integer.numberOfTrailingZeros(b)]>0; }
    private boolean dpadPressed(int key) {
        int i=key==UP?0:key==DOWN?1:key==LEFT?2:key==RIGHT?3:-1;
        return i>=0 && dpadRefs[i]>0;
    }
    private void setDpadPointer(int id,int key) {
        int old=dpadPointers.get(id,0);
        if(old==key)return;
        int oi=old==UP?0:old==DOWN?1:old==LEFT?2:old==RIGHT?3:-1;
        int ni=key==UP?0:key==DOWN?1:key==LEFT?2:key==RIGHT?3:-1;
        if(oi>=0)dpadRefs[oi]--;
        if(ni>=0)dpadRefs[ni]++;
        if(ni<0)dpadPointers.delete(id);else dpadPointers.put(id,key);
    }
    private void changeMask(int id,int mask) {
        int before=pointers.get(id,0);
        if(before==mask) return;
        for(int i=0;i<KEYS.length;i++) {
            int b=1<<i;
            if((before&b)!=0 && (mask&b)==0 && --refs[i]==0) Emu293SDLActivity.nativeSendKey(KEYS[i],false);
        }
        for(int i=0;i<KEYS.length;i++) {
            int b=1<<i;
            if((before&b)==0 && (mask&b)!=0 && refs[i]++==0) Emu293SDLActivity.nativeSendKey(KEYS[i],true);
        }
        if(mask==0) pointers.delete(id); else pointers.put(id,mask);
        invalidate();
    }
    private void disk(Canvas c,float x,float y,float r,String label,boolean down) {
        p.setStyle(Paint.Style.FILL); p.setColor(down?0xC0FFFFFF:0x68808080); c.drawCircle(x,y,r,p);
        p.setStyle(Paint.Style.STROKE); p.setStrokeWidth(Math.max(2,r*.055f)); p.setColor(0xDFFFFFFF); c.drawCircle(x,y,r,p);
        p.setStyle(Paint.Style.FILL); p.setTypeface(Typeface.create("sans-serif",Typeface.BOLD));
        p.setTextAlign(Paint.Align.CENTER); p.setTextSize(Math.max(12,r*.58f)); p.setColor(0xFFFFFFFF);
        c.drawText(label,x,y+p.getTextSize()*.34f,p);
    }
    private float unit() { return Math.min(getWidth(),getHeight()); }
    @Override protected void onDraw(Canvas c) {
        super.onDraw(c);
        float w=getWidth(),h=getHeight(),u=unit();
        float r=u*.065f, cx=w*.145f, cy=h*.70f, gap=r*1.55f;
        disk(c,cx,cy-gap,r,"▲",dpadPressed(UP)); disk(c,cx,cy+gap,r,"▼",dpadPressed(DOWN));
        disk(c,cx-gap,cy,r,"◀",dpadPressed(LEFT)); disk(c,cx+gap,cy,r,"▶",dpadPressed(RIGHT));
        stickX=w*.405f; stickY=h*.70f; stickRadius=u*.115f;
        p.setStyle(Paint.Style.FILL); p.setColor(0x50808080); c.drawCircle(stickX,stickY,stickRadius,p);
        p.setStyle(Paint.Style.STROKE); p.setStrokeWidth(Math.max(2,u*.004f)); p.setColor(0xBFFFFFFF);
        c.drawCircle(stickX,stickY,stickRadius,p);
        p.setStyle(Paint.Style.FILL); p.setColor(0xBFC0C0C0);
        c.drawCircle(stickX,stickY,stickRadius*.34f,p);
        float ax=w*.85f, ay=h*.69f, ar=u*.061f;
        disk(c,ax+ar*1.2f,ay-ar*.85f,ar,"A",pressed(A));
        disk(c,ax-ar*1.2f,ay+ar*.85f,ar,"B",pressed(B));
        disk(c,w*.455f,h*.92f,u*.042f,"SEL",pressed(SELECT));
        disk(c,w*.555f,h*.92f,u*.042f,"START",pressed(START));
    }
    private int dpadHit(float x,float y) {
        float w=getWidth(),h=getHeight(),r=unit()*.065f,cx=w*.145f,cy=h*.70f,g=r*1.55f;
        if(Math.hypot(x-cx,y-(cy-g))<r*1.28f)return UP;
        if(Math.hypot(x-cx,y-(cy+g))<r*1.28f)return DOWN;
        if(Math.hypot(x-(cx-g),y-cy)<r*1.28f)return LEFT;
        if(Math.hypot(x-(cx+g),y-cy)<r*1.28f)return RIGHT;
        return 0;
    }
    private int buttonHit(float x,float y) {
        float w=getWidth(),h=getHeight(),u=unit(),ax=w*.85f,ay=h*.69f,r=u*.061f;
        if(Math.hypot(x-(ax+r*1.2f),y-(ay-r*.85f))<r*1.25f)return A;
        if(Math.hypot(x-(ax-r*1.2f),y-(ay+r*.85f))<r*1.25f)return B;
        if(Math.hypot(x-w*.455f,y-h*.92f)<u*.05f)return SELECT;
        if(Math.hypot(x-w*.555f,y-h*.92f)<u*.05f)return START;
        return 0;
    }
    private int stickMask(float x,float y) {
        float dx=x-stickX,dy=y-stickY,dist=(float)Math.hypot(dx,dy);
        if(dist<stickRadius*.22f)return 0;
        double a=Math.atan2(dy,dx), threshold=Math.PI/8;
        int m=0;
        if(a>=-3*threshold && a<3*threshold)m|=1<<3;
        if(a>=threshold && a<7*threshold)m|=1<<1;
        if(a>=5*threshold || a< -5*threshold)m|=1<<2;
        if(a>=-7*threshold && a< -threshold)m|=1<<0;
        return m;
    }
    private void update(MotionEvent e,int i) {
        int id=e.getPointerId(i); float x=e.getX(i),y=e.getY(i);
        int dpadKey=dpadHit(x,y);
        setDpadPointer(id,dpadKey);
        // Preserve the standard D-pad mapping: Up->Up, Down->Down,
        // Left->Left and Right->Right.
        int key=dpadKey;
        if(key==0)key=buttonHit(x,y);
        int mask=key==0?0:bit(key);
        float sx=getWidth()*.405f,sy=getHeight()*.70f,sr=unit()*.115f;
        if(Math.hypot(x-sx,y-sy)<=sr*1.35f) {
            stickX=sx;stickY=sy;stickRadius=sr;
            mask=stickMask(x,y);
        }
        changeMask(id,mask);
    }
    private void release(int id) { setDpadPointer(id,0); changeMask(id,0); }
    @Override public boolean onTouchEvent(MotionEvent e) {
        int a=e.getActionMasked(),i=e.getActionIndex();
        if(a==MotionEvent.ACTION_DOWN||a==MotionEvent.ACTION_POINTER_DOWN){update(e,i);return true;}
        if(a==MotionEvent.ACTION_MOVE){for(int n=0;n<e.getPointerCount();n++)update(e,n);return true;}
        if(a==MotionEvent.ACTION_UP||a==MotionEvent.ACTION_POINTER_UP){release(e.getPointerId(i));return true;}
        if(a==MotionEvent.ACTION_CANCEL){
            while(dpadPointers.size()>0) release(dpadPointers.keyAt(dpadPointers.size()-1));
            while(pointers.size()>0) release(pointers.keyAt(pointers.size()-1));
            return true;
        }
        return true;
    }
}