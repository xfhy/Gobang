package com.xfhy.gobang.gobang;

/**
 * Created by XFHY on 2016/10/27.
 */

public class Point {
    private int x;
    private int y;
    private final int PRIMEX = 13;
    private final int PRIMEY = 17;

    public Point(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    @Override
    public String toString() {
        return "("+x+","+y+")";
    }

    @Override
    public int hashCode() {
        int result = PRIMEX * x;
        result += PRIMEY * y;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj == null){
            return false;
        }
        if(this == obj){
            return true;
        }
        if(obj instanceof Point){
            Point other = (Point) obj;
            if(x != other.x){
                return false;
            }
            if(y != other.y){
                return false;
            }
        }
        return true;
    }
}
