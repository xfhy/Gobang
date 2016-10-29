package com.xfhy.gobang.gobang.model;

/**
 * Created by XFHY on 2016/10/29.
 * 棋子类
 */

public class Chess {
    /**
     * 棋子类型
     */
    private ChessType chessType = ChessType.NOCHESS; //默认是未设置棋子属性

    /**
     * 获取当前棋子类型
     * @return
     */
    public ChessType getChessType() {
        return chessType;
    }

    /**
     * 设置当前棋子属性,黑棋或者白棋,或者无棋子
     * @param chessType
     */
    public void setChessType(ChessType chessType) {
        this.chessType = chessType;
    }
}
