package kr.okitoki.livewallpaper.study;

import java.util.ArrayList;
import java.util.List;

import kr.okitoki.livewallpaper.study.LiveWallpaperPainting.TouchPoint;
import kr.okitoki.livewallpaper.study.StarWallPaperService.MyThread;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.service.wallpaper.WallpaperService;
import android.view.Display;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.WindowManager;

public class TestWallpaper extends WallpaperService {
	
	public static int width, height;
	
    //-----------------------------------
    // onCreate   
    //-----------------------------------
    @Override
    public void onCreate() {
         super.onCreate();
    }

    //-----------------------------------
    // onDestroy
    //-----------------------------------
    @Override
    public void onDestroy() {
         super.onDestroy();
    }

    //-----------------------------------
    // onCreateEngine
    //-----------------------------------
    @Override
    public Engine onCreateEngine() {
         return new StarEngine();
    }
    
    class StarEngine extends Engine {
         private MyThread mThread;
     
     //-----------------------------------
     // Constructor
     //-----------------------------------
     StarEngine() {
          SurfaceHolder holder = getSurfaceHolder();
     }
     
     //-----------------------------------
     // onCreate
     //-----------------------------------
     @Override
     public void onCreate(SurfaceHolder holder) {
          super.onCreate(holder);
          setTouchEventsEnabled(true);
          mThread = new MyThread(holder, getApplicationContext());
     }
     
     //-----------------------------------
     // onDestroy
     //-----------------------------------
     @Override
     public void onDestroy() {
          super.onDestroy();
          mThread.stopPainting();
     }
     
     //-----------------------------------
     // onVisibilityChanged
     //-----------------------------------
     @Override
     public void onVisibilityChanged(boolean visible) {
          if (visible)
               mThread.resumePainting();
          else
               mThread.pausePainting();
     }
     
     //-----------------------------------
     // onSurfaceCreated
     //-----------------------------------
     @Override
     public void onSurfaceCreated(SurfaceHolder holder) {
          super.onSurfaceCreated(holder);
          mThread.start();
     }

     //-----------------------------------
     // onSurfaceChanged
     //-----------------------------------
     @Override
     public void onSurfaceChanged(SurfaceHolder holder, int format, int width, int height) {
          super.onSurfaceChanged(holder, format, width, height);
     }
     
     //------------------------------
     // surfaceDestroyed
     //------------------------------
     @Override
     public void onSurfaceDestroyed(SurfaceHolder holder) {
          super.onSurfaceDestroyed(holder);
          boolean retry = true;
          mThread.stopPainting();
            while (retry) {
                 try {
                      mThread.join();
                      retry = false;
                 } catch (InterruptedException e) {     
                      // nothing
                 }
            } // while
     }
     
     //-----------------------------------
     // onOffsetsChanged
     //-----------------------------------
     @Override
     public void onOffsetsChanged(float xOffset, float yOffset, float xStep, float yStep,
                                          int xPixels, int yPixels) {
      
     }
     
     //-----------------------------------
     // onTouchEvent
     //-----------------------------------
     @Override
     public void onTouchEvent(MotionEvent event) {
          super.onTouchEvent(event);
     }
     
    } // end of Engine
    
    
    class MyThread extends Thread {
    	
    	private SurfaceHolder surfaceHolder;
    	private Context mContext;
    	
    	private Rect src = new Rect();
    	private Rect dst = new Rect();
    	
    	private Bitmap bgImage;
    	
    	private boolean wait;
    	private boolean run;
    	  //------------------------------
    	  // Constructor
    	  //------------------------------
	  public MyThread(SurfaceHolder holder, Context context) {
		  this.surfaceHolder = holder;
		  this.mContext = context;
		  this.wait = true;
		  
		  Display display = ((WindowManager) mContext.getSystemService(mContext.WINDOW_SERVICE))
		    .getDefaultDisplay();
		  width  = display.getWidth();   // 화면의 폭
	       height = display.getHeight();   // 화면의 높이
		  bgImage = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.cosmos_2);
	      bgImage = Bitmap.createScaledBitmap(bgImage, (int) (width * 1.2), (int) (height * 1.2), true);
	  }
    	  
    	  
		public void pausePainting() {
			this.wait = true;
			synchronized(this) {
				this.notify();
			}
		}
		
		public void resumePainting() {
			this.wait = false;
			synchronized(this) {
				this.notify();
			}
		}
		public void stopPainting() {
			this.run = false;
			synchronized(this) {
				this.notify();
			}
		}
		
		@Override
		public void run() {
			this.run = true;
			Canvas c = null;
			while (run) {
				try {
					c = this.surfaceHolder.lockCanvas(null);
					synchronized (this.surfaceHolder) {
						doDraw(c);
					}
				} finally {
					if (c != null) {
						this.surfaceHolder.unlockCanvasAndPost(c);
					}
				}
				// pause if no need to animate
				synchronized (this) {
					if (wait) {
						try {
							wait();
						} catch (Exception e) {}
					}
				}
			}
		}
		
		/**
		 * 실제 그리기 할 곳
		 * 
		 * @param canvas
		 */
		private void doDraw(Canvas canvas) {
			
			src.set(0, 0, width+200, height);
		    dst.set(0, 0, width, height);
		      
			canvas.drawBitmap(bgImage, src, dst, null);  // 버퍼에 그리기
		}
    }
}
