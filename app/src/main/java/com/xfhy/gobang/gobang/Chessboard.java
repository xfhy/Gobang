package com.xfhy.gobang.gobang;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by XFHY on 2016/10/26.
 */

public class Chessboard extends View {

    /**
     *棋盘右下角的坐标值，即最大坐标值,横竖线数量
     */
    private static int maxX;
    private static int maxY;

    //第一点偏离左上角从像数，为了棋盘居中
    private static int xOffset;
    private static int yOffset;

    /**
     * 画笔对象
     */
    private Paint globalPaint;

    /**
     * 每一个格子的大小
     */
    private static int chequerSize = 50;

    /**
     * 用来显示当前点击位置坐标的TextView
     */
    private TextView locationInfo = null;

    private Bitmap whiteChessBitmap = null;  //白棋图片
    private Bitmap blackChessBitmap = null;  //黑棋图片
    /**
     * 是否该黑子下棋
     */
    private boolean isBlack = true;
    /**
     * 游戏当前状态   开始(true)    结束(false)
     */
    private boolean gameState = true;
    /**
     * 白棋子数据
     */
    private ArrayList<Point> allWhiteChessList = new ArrayList<>();
    /**
     * 黑棋子数据
     */
    private ArrayList<Point> allBlackChessList = new ArrayList<>();

    /**
     * 所有棋盘里面棋子的坐标
     */
    private Point[][] allChessCoord;

    /**
     * 棋盘的线(类)
     */
    class Line{
        float xStart,yStart,xStop,yStop;

        public Line(float xStart, float yStart, float xStop, float yStop) {
            this.xStart = xStart;
            this.yStart = yStart;
            this.xStop = xStop;
            this.yStop = yStop;
        }
    }

    //棋盘上面的线
    private List<Line> lines = new ArrayList<>();

    //构造函数
    public Chessboard(Context context) {
        super(context);
    }

    //这个构造方法必须实现,否则会加载出错    我的手机android4.4.4 默认调用的这个构造方法
    public Chessboard(Context context, AttributeSet attrs) {
        super(context, attrs);
        setBackgroundColor(0x44ff0000);//设置背景色
        initPaintAndBitmap();
    }

    public Chessboard(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setBackgroundColor(0x44ff0000);//设置背景色
        initPaintAndBitmap();
    }

    /**
     * 初始化画笔以及黑白棋图片
     */
    private void initPaintAndBitmap(){
        globalPaint = new Paint();
        globalPaint.setColor(0x88000000); //画笔颜色
        globalPaint.setAntiAlias(true);   //设置抗锯齿
        globalPaint.setDither(true);      //设置防抖动
        globalPaint.setStyle(Paint.Style.STROKE);  //设置图形是空心的

        //初始化黑白棋 棋子 图片
        whiteChessBitmap = BitmapFactory.decodeResource(getResources(),R.drawable.white_xfhy);
        blackChessBitmap = BitmapFactory.decodeResource(getResources(),R.drawable.black_xfhy);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        locationInfo = MainActivity.locationInfo;
        //画棋盘上的线
        drawChessboardLines(canvas);
        //画棋子
        drawChesses(canvas);
    }

    /**
     * 画棋盘
     * @param canvas
     */
    public void drawChessboardLines(Canvas canvas){
        for(Line line : lines){
            canvas.drawLine(line.xStart,line.yStart,line.xStop,line.yStop, globalPaint);
        }
    }

    /**
     * 当屏幕大小改变时
     * 初始横线和竖线的数目
     * @param w
     * @param h
     * @param oldw
     * @param oldh
     */
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        maxX = (int)Math.floor(w/chequerSize);         //屏幕宽度除以点的大小
        maxY = (int)Math.floor(h/chequerSize);   //棋盘
        Log.d("xfhy","maxX:--------------->"+Integer.toString(maxX));
        Log.d("xfhy","maxY:--------------->"+Integer.toString(maxY));

        //初始化所有棋子,注意,一个横排有maxY+1个
        allChessCoord = new Point[maxY+1][maxX+1];

        //设置X,Y坐标微调值,目的整个框居中
        xOffset = ((w - (chequerSize * maxX)) / 2);
        yOffset = ((h - (chequerSize * maxY)) / 2);
        Log.d("xfhy","xOffset:------------->"+String.valueOf(xOffset)+"dp");
        Log.d("xfhy","yOffset:------------->"+String.valueOf(yOffset)+"dp");

        whiteChessBitmap = Bitmap.createScaledBitmap(whiteChessBitmap,30,30,false);
        blackChessBitmap = Bitmap.createScaledBitmap(blackChessBitmap,30,30,false);

        //创建棋盘上的线条
        createLines();
        //初始化所有棋子
        initAllChesses();
    }

    /**
     * 绘制棋子
     * @param canvas
     */
    private void drawChesses(Canvas canvas){
        Log.d("xfhy","绘制棋子");
        int wSize = allWhiteChessList.size();
        for(int i=0; i<wSize; i++){
            Point wp = allWhiteChessList.get(i);
            canvas.drawBitmap(whiteChessBitmap,wp.getX()-xOffset,wp.getY()-yOffset,null);
        }
        int bSize = allBlackChessList.size();
        for(int i=0; i<bSize; i++){
            Point bp = allBlackChessList.get(i);
            canvas.drawBitmap(blackChessBitmap,bp.getX()-xOffset,bp.getY()-yOffset,null);
        }
    }

    /**
     * 创建棋盘上的线条
     */
    private void createLines() {

        float xStart = 0,yStart = 0,xStop = 0,yStop = 0;
        //竖线
        for (int i = 0; i <= maxX; i++) {
            xStart = xOffset+i*chequerSize;
            yStart = yOffset;
            xStop = xOffset+i*chequerSize;
            yStop = yOffset + maxY*chequerSize;
            lines.add(new Line(xStart,yStart,xStop,yStop));
        }
        //横线
        for (int i = 0; i <= maxY; i++) {
            xStart = xOffset;
            yStart = yOffset + i*chequerSize;
            xStop = xOffset + maxX*chequerSize;
            yStop = yOffset + i*chequerSize;
            lines.add(new Line(xStart,yStart,xStop,yStop));
        }
    }

    /**
     * 初始化所有的棋子
     */
    private void initAllChesses(){
        for(int i=0; i<=maxY; i++){
            for(int j=0; j<=maxX; j++){
                allChessCoord[i][j] = new Point(xOffset+j*chequerSize,yOffset+i*chequerSize);
            }
        }
    }

    /**
     * 获取用户点击事件
     * @param event
     * @return
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        //如果是点击事件
        if(event.getAction() == MotionEvent.ACTION_DOWN){
            int x = (int)event.getX();
            int y = (int)event.getY();
            locationInfo.setText("当前坐标:("+x+","+y+")");

            //当前用户点击的位置
            Point currentLocation = new Point(x,y);

            //判断之前是否已经下了该位置的棋
            if(allBlackChessList.contains(currentLocation) ||
                    allWhiteChessList.contains(currentLocation)){
                return false;
            }

            //如果当前是黑子下棋,则添加到黑棋数据里面
            if(isBlack){
                int distance = (int)Math.hypot((currentLocation.getX()- allChessCoord[0][0].getX()),
                        (currentLocation.getY()- allChessCoord[0][0].getY()));
                Point tempPointCoord = null;   
                Point correctPoint = null;
                for(int i=0; i<=maxY; i++){
                    for(int j=0; j<=maxX; j++){
                        tempPointCoord = new Point(allChessCoord[i][j].getX(), allChessCoord[i][j].getY());
                        int distance2 = (int)Math.hypot((currentLocation.getX()-tempPointCoord.getX()),
                                (currentLocation.getY()-tempPointCoord.getY()));
                        if(distance2 <= distance){
                            distance = distance2;
                            correctPoint = tempPointCoord;
                        }
                    }
                }
                //判断之前是否已经下了该位置的棋
                if(allBlackChessList.contains(correctPoint) || allWhiteChessList.contains(correctPoint)){
                    return false;
                }
                if(correctPoint != null){
                    allBlackChessList.add(correctPoint);
                }
                Log.d("xfhy","添加"+correctPoint.toString());
            } else {
                int distance = (int)Math.hypot((currentLocation.getX()- allChessCoord[0][0].getX()),
                        (currentLocation.getY()- allChessCoord[0][0].getY()));
                Point tempPointCoord = null;
                Point correctPoint = null;
                for(int i=0; i<=maxY; i++){
                    for(int j=0; j<=maxX; j++){
                        tempPointCoord = new Point(allChessCoord[i][j].getX(), allChessCoord[i][j].getY());
                        int distance2 = (int)Math.hypot((currentLocation.getX()-tempPointCoord.getX()),
                                (currentLocation.getY()-tempPointCoord.getY()));
                        if(distance2 <= distance){
                            distance = distance2;
                            correctPoint = tempPointCoord;
                        }
                    }
                }
                //判断之前是否已经下了该位置的棋
                if(allBlackChessList.contains(correctPoint) || allWhiteChessList.contains(correctPoint)){
                    return false;
                }
                if(correctPoint != null){
                    allWhiteChessList.add(correctPoint);
                }
                Log.d("xfhy","添加"+correctPoint.toString());
            }
            invalidate();         //View界面重绘
            isBlack = !isBlack;   //下了棋之后,下一个下棋的人不是自己.
        }
        return super.onTouchEvent(event);
    }

}
