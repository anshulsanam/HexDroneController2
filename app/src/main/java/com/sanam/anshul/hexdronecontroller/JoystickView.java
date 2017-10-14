package com.sanam.anshul.hexdronecontroller;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

import java.util.Timer;


/**
 * Created by Anshul on 8/2/2017.
 */

public class JoystickView extends SurfaceView implements SurfaceHolder.Callback, View.OnTouchListener{

    private float centerX;
    private float centerY;
    private float baseRadius;
    private float hatRadius;
    private float bottomY;

    private float joystickX = 0;
    private float joystickY = 1;

    int initX;
    int initY;

    public JoystickListener joystickCallback;

    public void setupDimensions(){
        centerX = getWidth() / 2;
        centerY = getHeight() / 2;
        baseRadius = Math.min(getWidth(), getHeight()) / 3;
        hatRadius = Math.min(getWidth(), getHeight()) / 7;
        bottomY = 680;
        joystickX = 0;
        joystickY = 1;
    }

    public JoystickView(Context context, int x, int y) {
        super(context);
        getHolder().addCallback(this);
        setOnTouchListener(this);
        if(context instanceof JoystickListener)
            joystickCallback = (JoystickListener) context;
        joystickX = 0;
        joystickY = 1;
        initX = x;
        initY = y;
    }


    public JoystickView(Context context, AttributeSet attrs) {
        super(context, attrs);
        getHolder().addCallback(this);
        setOnTouchListener(this);
        if(context instanceof JoystickListener)
            joystickCallback = (JoystickListener) context;
        joystickX = 0;
        joystickY = 1;
    }

    public JoystickView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        getHolder().addCallback(this);
        setOnTouchListener(this);
        if(context instanceof JoystickListener)
            joystickCallback = (JoystickListener) context;
        joystickX = 0;
        joystickY = 1;
    }

    public JoystickView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        getHolder().addCallback(this);
        setOnTouchListener(this);
        if(context instanceof JoystickListener)
            joystickCallback = (JoystickListener) context;
        joystickX = 0;
        joystickY = 1;
    }

    public void drawJoystick(float newX, float newY)
    {
        joystickX = 0;
        joystickY = 1;
        if(getHolder().getSurface().isValid())
        {
            Canvas myCanvas = this.getHolder().lockCanvas(); //stuff to draw
            Paint colors = new Paint();
            myCanvas.drawColor(getResources().getColor(R.color.colorPrimaryLight)); //clear the BG
            colors.setColor(getResources().getColor(R.color.colorPrimary)); //color of joystick base
            myCanvas.drawCircle(centerX, centerY, baseRadius, colors); //Draw the joystick base
            colors.setColor(getResources().getColor(R.color.colorAccent));
            myCanvas.drawCircle(newX, newY, hatRadius, colors);
            getHolder().unlockCanvasAndPost(myCanvas);

        }
    }


    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        joystickX = 0;
        joystickY = 1;
        setupDimensions();
        drawJoystick(centerX, bottomY);
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int format, int width, int height) {
        joystickX = 0;
        joystickY = 1;
        setupDimensions();
        drawJoystick(centerX, bottomY);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        joystickX = 0;
        joystickY = 1;
        setupDimensions();
        drawJoystick(centerX, bottomY);
    }

    public boolean onTouch(View v, MotionEvent e)
    {
        joystickX = 0;
        joystickY = 1;
        if(v.equals(this))
        {
            joystickX = 0;
            joystickY = 1;
            if(e.getAction() != e.ACTION_UP)
            {

                float displacement = (float) Math.sqrt((Math.pow(e.getX() - centerX, 2)) + Math.pow(e.getY() - centerY, 2));
                if(displacement < baseRadius)
                {
                    drawJoystick(e.getX(), e.getY());
                  //  joystickCallback.onJoystickMoved((e.getX() - centerX)/baseRadius, (e.getY() - centerY)/baseRadius, getId());
                    joystickX = (e.getX() - centerX)/baseRadius;
                    joystickY = (e.getY() - centerY)/baseRadius;
                }
                else
                {
                    float ratio = baseRadius / displacement;
                    float constrainedX = centerX + (e.getX() - centerX) * ratio;
                    float constrainedY = centerY + (e.getY() - centerY) * ratio;
                    drawJoystick(constrainedX, constrainedY);
                   // joystickCallback.onJoystickMoved((constrainedX-centerX)/baseRadius, (constrainedY-centerY)/baseRadius, getId());
                    joystickX = (constrainedX-centerX)/baseRadius;
                    joystickY = (constrainedY-centerY)/baseRadius;
                }
            }
            else {
                drawJoystick(centerX, bottomY); //This resets the joystick to its center position when the user lets go.
                //joystickCallback.onJoystickMoved(0, 0, getId());

                joystickX = 0;
                joystickY = 1;
            }
        }

        joystickCallback.onJoystickMoved(getJoystickX(), getJoystickY(), getId());
        return true;
    }

    public float getJoystickX()
    {
        return joystickX;
    }


    public float getJoystickY()
    {
        return joystickY;
    }

    public void setJoystickX(int x)
    {
        joystickX = x;
    }

    public void setJoystickY(int y)
    {
        joystickY = y;
    }

    public interface JoystickListener
    {

        void onJoystickMoved(float xPercent, float yPercent, int source);

    }
}
