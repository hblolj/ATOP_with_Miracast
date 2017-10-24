package com.atop.main;

import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;

public class SurfaceViewMain extends SurfaceView implements Callback {
	private final String TAG = "Class::SurfaceViewMain";
	private SurfaceHolder mSurfaceHolder;
	private DrawThread thread;
	float screenWidth = getContext().getResources().getDisplayMetrics().widthPixels;
	float screenHeight = getContext().getResources().getDisplayMetrics().heightPixels;
	float smallScreenHeight = (float) ((getContext().getResources()
			.getDisplayMetrics().xdpi) * 4.1732283);
	Matrix matrix = new Matrix();

	public SurfaceViewMain(Context context) {
		super(context);
		mSurfaceHolder = getHolder();
		mSurfaceHolder.addCallback(this);
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {

		thread = new DrawThread(mSurfaceHolder, this);
		thread.setRunning(true);
		thread.start();

	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		boolean retry = true;
		thread.SetLoop(false);

		while (retry) {
			try {
				thread.join();
				retry = false;
			} catch (InterruptedException e) {
				Log.e(TAG, "surfaceDestroyed" + e);
				e.getStackTrace();
			}
		}

	}

	public void stopSurfaceView() {
		thread.isFinish(true);
		thread.setRunning(false);
	}

	public void setThreadimage(byte[] bitmap, int length, float height) {

		thread.setImage(bitmap, length, (int) screenWidth, (int) screenHeight);

	}

	public void setThreadimageBitmap(Bitmap bitmap, float height) {

		thread.setImageBitmap(bitmap, (int) screenWidth, (int) screenHeight);
	}

	public void receiveThreadBitmap(Bitmap bitmap) {
		boolean check = false;
		check = thread.getRunning();
		if (check) {
			thread.receiveBitmap(bitmap);
		}
	}

	public void startSurfaceView() {
		thread.isFinish(false);

	}
}

class DrawThread extends Thread {

	private final String TAG = "Class::SurfaceViewMain - Thread";

	SurfaceHolder mHolder;

	private Boolean running = false;
	private Bitmap image;
	private Boolean finish;
	

	// fps
	private long tick = 0;
	private int fps = 0;

	public DrawThread(SurfaceHolder Holder, SurfaceViewMain surfaceview) {

		this.mHolder = Holder;
		running = true;
		finish = false;
	}

	public void SetLoop(boolean b) {

		running = b;
	}

	public void isFinish(boolean b) {

		finish = b;

	}

	public void setRunning(Boolean run) {
		this.running = run;
	}

	public boolean getRunning() {
		return running;
	}

	@Override
	public void run() {

		android.os.Process.setThreadPriority(NORM_PRIORITY);
		super.run();
		Canvas canvas;
		
		while (running) {
			canvas = null;
			try {
				canvas = mHolder.lockCanvas();
				synchronized (mHolder) {

					if (image != null && !finish && canvas != null) {

						canvas.drawBitmap(image, 0, 0, null);
						
//						// 프레임수계산
//						if (System.currentTimeMillis() - tick > 1000) {
//							Log.d(TAG, "FPS : " + fps);
//							fps = 0;
//							tick = System.currentTimeMillis();
//						} else
//							fps++;

					} else
						continue;

				}
			} finally {
				if (canvas != null) {
					mHolder.unlockCanvasAndPost(canvas);
				}
			}
			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	// 화면사이즈로 이미지 크기 조정
	public void setImage(byte[] receiveByte, int length, int Width, int Height) {

		Bitmap Image;
			
		try {
			//BitmapFactory.Options options = new BitmapFactory.Options();
			//options.inPreferredConfig = Bitmap.Config.RGB_565;			
			Image = BitmapFactory.decodeByteArray(receiveByte, 0, length);
			if (Image != null) {
				
				image = Bitmap.createScaledBitmap(Image, Width, Height, true);
				Image.recycle();
				Image = null;
			}

		} catch (OutOfMemoryError ex) {
			Log.e(TAG, "OUT of Memory !!!!!");
		}

	}

	public void setImageBitmap(Bitmap bitmap, int width, int height) {

		if (image != null) {

			image = Bitmap.createScaledBitmap(bitmap, width, height, true);
		}
	}

	public void receiveBitmap(Bitmap bitmap) {
		
		image = Bitmap.createScaledBitmap(bitmap, 1920, 1080, true);

	}
	

}
