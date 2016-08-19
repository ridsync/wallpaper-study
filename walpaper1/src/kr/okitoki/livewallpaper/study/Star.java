package kr.okitoki.livewallpaper.study;
import java.util.Random;

import android.graphics.Paint;

public class Star {

     public Paint paint;
     public float x1, y1, x2, y2; // 점의 좌표
     public float speed;    // 속도
     private float rad;    // 반지름

     private float cx, cy;   // 중심점
     private float th;    // 각도 (radian)
     private float power;
 
     //------------------------------
     // Constructor
     //------------------------------
     public Star() {
          Random rnd = new Random();
          int r = rnd.nextInt(100) + 30;     // 최초의 반지름
          th = (float)(rnd.nextInt(360) * Math.PI / 180); // 각도
          cx = (float) (StarWallPaperService.cx + Math.cos(th) * r); // 중심점
          cy = (float) (StarWallPaperService.cy - Math.sin(th) * r);    
  
          rad = 1;       // 반지름
          power = (rnd.nextInt(50) + 10) / 100f + 1; // 이동 속도 (1.01~1.05)
          paint = new Paint();
          paint.setColor(StarWallPaperService.COLORS[rnd.nextInt(6)] + 0xFF000000); 
     }
 
     //------------------------------
     // MoveStar
     //------------------------------
     public boolean MoveStar() {
          speed = rad; 
          rad *= power;
          speed = (rad - speed);

          x2 = x1;
          y2 = y1;
          x1 = (float) (cx + Math.cos(th) * rad);
          y1 = (float) (cy - Math.sin(th) * rad);

          if (x1 < -100 || x1 > StarWallPaperService.Width + 100 || y1 < -100 || y2 > StarWallPaperService.Height + 100)
               return false;
          else
               return true;
     }
}
