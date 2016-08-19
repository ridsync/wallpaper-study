package kr.okitoki.livewallpaper.study;
import java.util.ArrayList;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.service.wallpaper.WallpaperService;
import android.view.Display;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.WindowManager;
public class StarWallPaperService extends WallpaperService {
     public static int Width, Height;
     public static int cx, cy;

     public static int COLORS[] = {0xFFFFFF, 0xC0FFC0, 0xFFC0C0, 0xC0FFFF, 0xF8F0FF, 0xC0F0FF};
 
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

//----------------  StarEngine  ----------------------------------------------------

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
       mThread.setSurfaceSize(width, height);
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
       mThread.doTouchEvent(event);
  }
  
 } // end of Engine
 
//------------------  thread  -----------------------------------------------------

 class MyThread extends Thread {
  private SurfaceHolder mHolder;
  private Context mContext;
  private boolean wait = true;
  private boolean canRun = true;
  
  public int bx, by;
  private ArrayList<Star> stars = new ArrayList<Star>();
  private Bitmap imgBack;
  private Rect src = new Rect();
  private Rect dst = new Rect();
  private float  th;     // 각도
  private int    rad;      // 반지름
  
  //------------------------------
  // Constructor
  //------------------------------
  public MyThread(SurfaceHolder holder, Context context) {
       mHolder = holder;
       mContext = context;
   
       Display display = ((WindowManager) mContext.getSystemService(mContext.WINDOW_SERVICE))
    .getDefaultDisplay();
       Width  = display.getWidth();   // 화면의 폭
       Height = display.getHeight();   // 화면의 높이
          cx = Width / 2;
          cy = Height / 2;

       imgBack = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.cosmos_2);
       imgBack = Bitmap.createScaledBitmap(imgBack, (int) (Height * 1.2), (int) (Height * 1.2), true);
       bx = by = (int) (Height * 0.6);      // 이미지 중심
   
       for (int i = 1; i <= 50; i++)  //최초의 별의 갯수
            stars.add(new Star());
   
       dst.set(0, 0, Width, Height);
       rad = 15; // 반지름
  }

  //------------------------------
  // pause Painting
  //------------------------------
    public void pausePainting() {
         wait = true;
         synchronized (this) {
              this.notify();
          }
   }
  
  //------------------------------
  // resume Painting
  //------------------------------
    public void resumePainting() {
         wait = false;
         synchronized (this) {
              this.notify();
       }
    }
  
  //------------------------------
  // stop Painting
  //------------------------------
   public void stopPainting() {
         canRun = false;
         synchronized (this) {
              this.notify();
       }
   }
  
  //------------------------------
  // set SurfaceSize
  //------------------------------
   public void setSurfaceSize(int width, int height) {
       Width = width;
       Height = height;
       dst.set(0, 0, Width, Height);
        synchronized (this) {
              this.notify();
         }
    }
  
  //------------------------------
  // do TouchEvent
  //------------------------------
   public void doTouchEvent(MotionEvent event) {
         wait = false;
         synchronized (this) {
              this.notify();
          }
   }

  //------------------------------
  // run
  //------------------------------
   public void run() {
         canRun = true;
         Canvas canvas = null;          // canvas를 만든다
          while (canRun) {
               canvas = mHolder.lockCanvas();       // canvas를 잠그고 버퍼 할당
               try {
                    synchronized (mHolder) {       // 동기화 유지
                         canvas.drawBitmap(imgBack, src, dst, null);  // 버퍼에 그리기
                         MakeAndDisplayStars(canvas);
                    }
               } finally {
                if (canvas != null)
                      mHolder.unlockCanvasAndPost(canvas);    
               } // try

             synchronized (this) {
                  if (wait) {
                       try {
                            wait();
                       } catch (Exception e) {
                               // nothing
                      }
                  }
           } // sync
          } // while
        } // run

  //------------------------------
  // MakeAndDisplayStars
  //------------------------------
  private void MakeAndDisplayStars(Canvas canvas) {
       int x = (int) (bx - Math.cos(th) * rad); // 시계 반대방향으로 회전
       int y = (int) (by + Math.sin(th) * rad);    // 이미지 중심을 원점으로 설정
       int ax = Width / 2;
       int ay = Height / 2;
       src.set(x - ax, y - ay, x + ax, x + ay);    // cx, cy는 Star에서 사용중...
       th += 0.04;  // 각도
   
       for (int i = 1; i <= 5; i++)
            stars.add(new Star());  // 루프당 5개씩 추가
   
       for (int idx = stars.size() - 1; idx >= 0; idx--) {
            if (stars.get(idx).MoveStar() == false)
             stars.remove(idx);
       }
   
       for (Star mStar : stars) {
            if (mStar.speed <= 2)
                 canvas.drawCircle(mStar.x1, mStar.y1, 1, mStar.paint);
            else
                 canvas.drawLine(mStar.x2, mStar.y2, mStar.x1, mStar.y1, mStar.paint);
           }
      } // MakeAndDisplayStars
  
 } // Thread

} // end of Wallpaper