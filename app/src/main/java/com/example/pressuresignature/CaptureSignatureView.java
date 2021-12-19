package com.example.pressuresignature;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.os.Build;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.RequiresApi;

import java.time.LocalDateTime;

public class CaptureSignatureView extends View {

    private Bitmap _Bitmap;
    private Canvas _Canvas;
    private final Path _Path;
    private final Paint _BitmapPaint;
    private final Paint _paint;
    private float _mX;
    private float _mY;
    SignatureDataModel _data;
    private float _X;
    private float _Y;
    public int count = 1;
    private int startTime = 0;
    private int endTime = 0;

    DisplayMetrics displayMetrics = this.getResources().getDisplayMetrics();
    float screenHeight = displayMetrics.heightPixels;
    float screenWidth = displayMetrics.widthPixels;


    public CaptureSignatureView(Context context, AttributeSet attr) {
        super(context, attr);
        _Path = new Path();
        _BitmapPaint = new Paint(Paint.DITHER_FLAG);
        _paint = new Paint();
        _paint.setAntiAlias(true);
        _paint.setDither(true);
        _paint.setColor(Color.argb(255, 0, 0, 0));
        _paint.setStyle(Paint.Style.STROKE);
        _paint.setStrokeJoin(Paint.Join.ROUND);
        _paint.setStrokeCap(Paint.Cap.ROUND);
        float lineThickness = 10;
        _paint.setStrokeWidth(lineThickness);
        _data = new SignatureDataModel();

    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        _Bitmap = Bitmap.createBitmap(w, (h > 0 ? h : ((View) this.getParent()).getHeight()), Bitmap.Config.ARGB_8888);
        _Canvas = new Canvas(_Bitmap);
        drawTemplate(count);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawColor(Color.WHITE);
        canvas.drawBitmap(_Bitmap, 0, 0, _BitmapPaint);
        canvas.drawPath(_Path, _paint);
    }

    private double easyfun1(double theta) { // done
        return 200;
    }
    private double easyfun2(double theta) { // done
        return 2 * 100 + 0.3 * 100 * Math.sin(2 * theta);
    }
    private double easyfun3(double theta) { // done
        return 0.7 * 100 * theta; // done
    }
    private double medfun1(double theta) { // done
        return 200 + 200 * Math.sin(3 * theta);
    }
    private double medfun2(double theta) { // done
        return 300 - 0.8 * 100 * Math.sin(3 * theta) * Math.cos(6 * theta);
    }
    private double medfun3(double theta) { // done
        return 300 - 150 * Math.sin(4 * theta) * Math.cos(theta);
    }
    private double hardfun1(double theta) { // done
        return 180 + 200 * Math.sin(6 * theta);
    }
    private double hardfun2(double theta) { // done
        return 200 + 100 * Math.cos(10 * theta) + 100 * Math.sin(5*theta);
    }

    public void drawTemplate(int count) {
        _paint.setColor(Color.argb(255, 190, 190, 190));
        for (int i = 0; i <= 360; i++) {
            double theta =  Math.toRadians(i);
            double r;
            switch(count) {
                case 1:
                    r = easyfun1(theta);
                    break;
                case 2:
                    r = easyfun2(theta);
                    break;
                case 3:
                    r = easyfun3(theta);
                    break;
                case 4:
                    r = medfun1(theta);
                    break;
                case 5:
                    r = medfun2(theta);
                    break;
                case 6:
                    r = medfun3(theta);
                    break;
                case 7:
                    r = hardfun1(theta);
                    break;
                case 8:
                    r = hardfun2(theta);
                    break;
                default:
                    throw new IllegalStateException("Unexpected value: " + count);
            }

            float x = (float) (r * Math.cos(theta));
            float y = (float) (r * Math.sin(theta));

            float screenx = x + screenWidth/2;
            float screeny = -y + screenHeight/2;

            if (i == 0) {
                _Path.moveTo(screenx, screeny);
                _X = screenx;
                _Y = screeny;
            } else {
                _Path.quadTo(_X, _Y, (screenx + _X) / 2, (screeny + _Y) / 2);
                _Path.lineTo(screenx, screeny);
                _X = screenx;
                _Y = screeny;
                _Canvas.drawPath(_Path, _paint);
                _Path.moveTo(screenx, screeny);
            }
        }
    }



    @RequiresApi(api = Build.VERSION_CODES.O)
    private void TouchStart(float x, float y) {
        _paint.setColor(Color.argb(255, 0, 0, 0));
        float cartx = x - screenWidth/2;
        float carty = -y + screenHeight/2;
        float r     = (float) Math.sqrt(cartx*cartx + carty*carty);
        float theta = (float) Math.toDegrees(Math.atan2(carty, cartx));

        _data.dataPoints.add(new SignaturePointModel(r, theta));
        _Path.reset();
        _Path.moveTo(x, y);
        _mX = x;
        _mY = y;

        if (startTime == 0) {
            LocalDateTime date = LocalDateTime.now();
            startTime = date.toLocalTime().toSecondOfDay();
        }
    }

    private void TouchMove(float x, float y) {
        float dx = Math.abs(x - _mX);
        float dy = Math.abs(y - _mY);

        float touchTolerance = 4;
        if (dx >= touchTolerance || dy >= touchTolerance) {
            float cartx = x - screenWidth/2;
            float carty = -y + screenHeight/2;
            float r     = (float) Math.sqrt(cartx*cartx + carty*carty);
            float theta = (float) Math.toDegrees(Math.atan2(carty, cartx));

            _data.dataPoints.add(new SignaturePointModel(r, theta));
            _Path.quadTo(_mX, _mY, (x + _mX) / 2, (y + _mY) / 2);
            _mX = x;
            _mY = y;
            _Path.lineTo(_mX, _mY);
            _Canvas.drawPath(_Path, _paint);
            _Path.reset();
            _Path.moveTo(x, y);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void TouchUp() {
        float cartx = _mX - screenWidth/2;
        float carty = -_mY + screenHeight/2;
        float r     = (float) Math.sqrt(cartx*cartx + carty*carty);
        float theta = (float) Math.toDegrees(Math.atan2(carty, cartx));

        _data.dataPoints.add(new SignaturePointModel(r, theta));
        _Canvas.drawPoint(_mX, _mY, _paint);
        _Path.reset();

        LocalDateTime date = LocalDateTime.now();
        endTime = date.toLocalTime().toSecondOfDay();
    }


    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public boolean onTouchEvent(MotionEvent e) {
        super.onTouchEvent(e);
        float x = e.getX();
        float y = e.getY();
        switch (e.getAction()) {
            case MotionEvent.ACTION_DOWN:
                TouchStart(x, y);
                invalidate();
                break;
            case MotionEvent.ACTION_MOVE:
                TouchMove(x, y);
                invalidate();
                break;
            case MotionEvent.ACTION_UP:
                TouchUp();
                invalidate();
                break;
        }
        return true;
    }

    public void ClearCanvas() {
        _Canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
        invalidate();
    }

    public int getTime() {
        return endTime - startTime;
    }

    public void resetData() {
        _data.dataPoints.clear();
        startTime = 0;
        endTime = 0;
        if (count <=8) {
            drawTemplate(count);
        }
    }

    public void updateCount() {
        count++;
    }
}