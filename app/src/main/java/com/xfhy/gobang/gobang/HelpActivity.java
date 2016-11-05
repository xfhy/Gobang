package com.xfhy.gobang.gobang;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class HelpActivity extends AppCompatActivity {

    private Button btn_back = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);
        btn_back = (Button)findViewById(R.id.btn_back);
        btn_back.setOnClickListener(new BackListener());
    }

    //返回按钮监听器
    class BackListener implements View.OnClickListener{
        @Override
        public void onClick(View v) {
            HelpActivity.this.finish();
        }
    }

}
