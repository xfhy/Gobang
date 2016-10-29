package com.xfhy.gobang.gobang;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    private Chessboard chess = null;
    public static TextView locationInfo = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        chess = (Chessboard)findViewById(R.id.chess);
        locationInfo = (TextView)findViewById(R.id.location);
    }
}
