package com.xfhy.gobang.gobang;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.media.AudioManager;
import android.media.SoundPool;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.xfhy.gobang.gobang.model.ChessType;
import com.xfhy.gobang.gobang.util.MyApplication;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
     * 游戏状态:开始
     */
    private final static int START = 1;
    /**
     * 游戏状态:结束
     */
    private final static int END = 2;
    /**
     * 游戏当前状态
     */
    private int gameState = START;
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

    //实例化AudioManager对象，控制声音
    private AudioManager audioManager =null;
    //最大音量
    float audioMaxVolumn;
    //当前音量
    float audioCurrentVolumn;
    float volumnRatio;
    //音效播放池
    private SoundPool playSound = new SoundPool(2,AudioManager.STREAM_MUSIC,0);
    //存放音效的HashMap
    private Map<Integer,Integer> map = new HashMap<Integer,Integer>();

    private Button btn_restart = null;
    private Button btn_undo = null;

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

    /**
     * 初始化游戏音效
     */
    private void initPlaySound(){
        //实例化AudioManager对象，控制声音
        audioManager = (AudioManager)MyApplication.getContext().
                getSystemService(MyApplication.getContext().AUDIO_SERVICE);

//最大音量
        audioMaxVolumn = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
//当前音量
        audioCurrentVolumn = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        volumnRatio = audioCurrentVolumn/audioMaxVolumn;
        map.put(0, playSound.load(MyApplication.getContext(),R.raw.chess_sound,1));
        map.put(1, playSound.load(MyApplication.getContext(),R.raw.chess_sound,1));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        locationInfo = MainActivity.locationInfo;
        btn_restart = MainActivity.btn_restart;
        btn_undo = MainActivity.btn_undo;
        btn_restart.setOnClickListener(new OnClickRestartListener());
        btn_undo.setOnClickListener(new OnClickUndoListener());
        //画棋盘上的线
        drawChessboardLines(canvas);

        //画棋子
        drawChesses(canvas);

        //初始化声音
        initPlaySound();
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
        //判断当前游戏状态
        if(gameState == END){
            return false;
        }

        //如果是点击事件
        if(event.getAction() == MotionEvent.ACTION_DOWN){
            int x = (int)event.getX();
            int y = (int)event.getY();
            //locationInfo.setText("当前坐标:("+x+","+y+")");

            //当前用户点击的位置
            Point currentLocation = new Point(x,y);

            //判断之前是否已经下了该位置的棋
            if(allBlackChessList.contains(currentLocation) ||
                    allWhiteChessList.contains(currentLocation)){
                return false;
            }

            /*------------------获取当前用户点击位置与哪个棋子位置最近----------------------------*/
            int distance = (int)Math.hypot((currentLocation.getX()- allChessCoord[0][0].getX()),
                    (currentLocation.getY()- allChessCoord[0][0].getY()));
            Point tempPointCoord = null;
            Point correctPoint = null;   //正确的点
            int row = 0,col = 0;   //需要添加的那个棋子在二维数组中的行列值
            for(int i=0; i<=maxY; i++){
                for(int j=0; j<=maxX; j++){
                    tempPointCoord = new Point(allChessCoord[i][j].getX(),
                            allChessCoord[i][j].getY());
                    int distance2 = (int)Math.hypot((currentLocation.getX()-
                                    tempPointCoord.getX()),
                            (currentLocation.getY()-tempPointCoord.getY()));
                    if(distance2 <= distance){
                        row = i;
                        col = j;
                        distance = distance2;
                        correctPoint = tempPointCoord;
                    }
                }
            }
            //判断之前是否已经下了该位置的棋
            if(allBlackChessList.contains(correctPoint) ||
                    allWhiteChessList.contains(correctPoint)){
                return false;
            }

            //没有找到正确的点
            if(correctPoint == null){
                return false;
            }

            //如果当前是黑子下棋,则添加到黑棋数据里面
            if (isBlack) {
                allChessCoord[row][col].setChessType(ChessType.BLACK);
                correctPoint.setChessType(ChessType.BLACK);  //设置棋子类型是黑子
                playSound.play(
                        map.get(0),//声音资源
                        volumnRatio,//左声道
                        volumnRatio,//右声道
                        1,//优先级
                        0,//循环次数，0是不循环，-1是一直循环
                        1);//回放速度，0.5~2.0之间，1为正常速度
                allBlackChessList.add(correctPoint);
            } else {
                allChessCoord[row][col].setChessType(ChessType.WHITE);
                //设置棋子类型是白子
                correctPoint.setChessType(ChessType.WHITE);
                playSound.play(
                        map.get(0),//声音资源
                        volumnRatio,//左声道
                        volumnRatio,//右声道
                        1,//优先级
                        0,//循环次数，0是不循环，-1是一直循环
                        1);//回放速度，0.5~2.0之间，1为正常速度
                allWhiteChessList.add(correctPoint);
            }
            Log.d("xfhy","添加"+correctPoint.toString());
            invalidate();         //View界面重绘
            judgeWhoWin(correctPoint,row,col);   //判断输赢
            isBlack = !isBlack;   //下了棋之后,下一个下棋的人不是自己.
        }
        return super.onTouchEvent(event);
    }

    /**
     * 判断输赢
     * @param point   当前下的棋子
     * @param row     该棋子在所有棋子二维数组中的横坐标   <= maxY
     * @param col     该棋子在所有棋子二维数组中的纵坐标   <= maxX
     */
    private void judgeWhoWin(Point point,int row,int col){
        int chessCount = 0;  //连着的棋子数量
        /*------------------------判断横向--------------------------*/
        //判断棋子右边
        for(int j=col+1; j <= maxX; j++){
            //从该棋子的右边一个开始
            if(allChessCoord[row][j].getChessType() == point.getChessType()){
                chessCount++;
            } else {
                break;
            }
        }
        showWin(point,chessCount);    //显示当前是谁(黑棋白棋)赢了

        //判断左边
        for(int j=col-1; j >= 0; j--){
            if(allChessCoord[row][j].getChessType() == point.getChessType()){
                chessCount++;
            } else {
                break;
            }
        }
        showWin(point,chessCount);    //显示当前是谁(黑棋白棋)赢了
        chessCount = 0;

        /*------------------------判断竖向--------------------------*/
        //判断正上方
        for(int i=row-1; i>=0; i--){
            if(allChessCoord[i][col].getChessType() == point.getChessType()){
                chessCount++;
            } else {
                break;
            }
        }
        showWin(point,chessCount);    //显示当前是谁(黑棋白棋)赢了

        //判断正下方
        for(int i=row+1; i <= maxY; i++){
            if(allChessCoord[i][col].getChessType() == point.getChessType()){
                chessCount++;
            } else {
                break;
            }
        }
        showWin(point,chessCount);    //显示当前是谁(黑棋白棋)赢了
        chessCount = 0;

        /*------------------------判断左斜方向--------------------------*/
        //判断西北方向 row-1,col-1
        for(int i=row-1,j=col-1; i>=0 && j>=0; i--,j--){
            if(allChessCoord[i][j].getChessType() == point.getChessType()){
                chessCount++;
            } else {
                break;
            }
        }
        showWin(point,chessCount);    //显示当前是谁(黑棋白棋)赢了

        //判断东南方向
        for(int i=row+1,j=col+1; i<=maxY && j<=maxX; i++,j++){
            if(allChessCoord[i][j].getChessType() == point.getChessType()){
                chessCount++;
            } else {
                break;
            }
        }
        showWin(point,chessCount);    //显示当前是谁(黑棋白棋)赢了
        chessCount = 0;

        /*------------------------判断右斜方向--------------------------*/
        //判断东北方向
        for(int i=row-1,j=col+1; i>=0 && j<=maxX; i--,j++){
            if(allChessCoord[i][j].getChessType() == point.getChessType()){
                chessCount++;
            } else {
                break;
            }
        }
        showWin(point,chessCount);    //显示当前是谁(黑棋白棋)赢了

        //判断西南方向
        for(int i=row+1,j=col-1; i<=maxY && j>=0; i++,j--){
            if(allChessCoord[i][j].getChessType() == point.getChessType()){
                chessCount++;
            } else {
                break;
            }
        }
        showWin(point,chessCount);    //显示当前是谁(黑棋白棋)赢了
    }

    /**
     * 显示当前是谁(黑棋白棋)赢了
     * @param point  //当判断已经有一方赢了的时候,最后下的那个点
     */
    private void showWin(Point point,int chessCount){
        if(chessCount>3){
            //胜负已分
            if(point.getChessType()==ChessType.BLACK){
                Log.d("xfhy","黑棋胜利!");
                locationInfo.setText("黑棋胜利!");
                gameState = END;   //游戏结束
            } else if(point.getChessType()==ChessType.WHITE){
                Log.d("xfhy","白棋胜利!");
                locationInfo.setText("白棋胜利!");
                gameState = END;   //游戏结束
            }
        }
    }

    //重新开始按钮功能
    class OnClickRestartListener implements View.OnClickListener{
        @Override
        public void onClick(View v) {
            locationInfo.setText("重新开始");
            //移除所有棋子
            allWhiteChessList.clear();
            allBlackChessList.clear();
            //设置棋盘上所有棋子为NOCHESS
            for (int i = 0; i < maxX+1; i++) {
                for (int j = 0; j < maxX+1; j++) {
                    allChessCoord[i][j].setChessType(ChessType.NOCHESS);
                }
            }
            gameState = START;  //游戏状态设为START
            isBlack = true;   //黑棋先开始
            invalidate();         //View界面重绘
        }
    }

    //悔棋按钮功能
    class OnClickUndoListener implements View.OnClickListener{
        @Override
        public void onClick(View v) {

            //首先判断是否棋盘上有棋子
            if(allWhiteChessList.size() == 0 && allBlackChessList.size() == 0){
                Toast.makeText(MyApplication.getContext(),"亲,现在棋盘上没有棋子哦",
                        Toast.LENGTH_SHORT).show();
                return ;
            }

            Point tempPoint = null;
            //如果当前是黑子下棋,则说明上一步是白子下的棋
            if(isBlack){
                if(allWhiteChessList.size() > 0 && gameState == START){
                    tempPoint = allWhiteChessList.remove(allWhiteChessList.size()-1);
                }
            } else {
                if(allBlackChessList.size() > 0 && gameState == START){
                    tempPoint = allBlackChessList.remove(allBlackChessList.size()-1);
                }
            }

            //设置那个棋盘位置为无棋子
            for (int i = 0; i < maxX+1; i++) {
                for (int j = 0; j < maxX+1; j++) {
                    if(allChessCoord[i][j].equals(tempPoint)){
                        allChessCoord[i][j].setChessType(ChessType.NOCHESS);
                        break;
                    }
                }
            }

            isBlack = !isBlack;   //下棋人调换
            invalidate();         //View界面重绘
        }
    }

}
