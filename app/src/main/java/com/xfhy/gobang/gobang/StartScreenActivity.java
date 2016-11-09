package com.xfhy.gobang.gobang;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import com.xfhy.gobang.gobang.model.GamePattern;
import net.youmi.android.AdManager;
import net.youmi.android.normal.banner.*;

/**
 * 2016年11月5日16:22:35
 * 开始界面
 * 发布ID:ce5360bb2cb842a5
 * 应用密匙:0e7c7db390b22793
 */
public class StartScreenActivity extends AppCompatActivity {

    private Button startgamebymanandmachine = null;   //人机按钮
    private Button startgamebymanandman = null;       //人人按钮
    private Button helpbtn = null;                    //帮助按钮
    private Button aboutbtn = null;                   //关于按钮
    private Button exitbtn = null;                    //退出按钮

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_screen);
        startgamebymanandmachine = (Button)findViewById(R.id.startgamebymanandmachine);
        startgamebymanandman = (Button)findViewById(R.id.startgamebymanandman);
        helpbtn = (Button)findViewById(R.id.helpbtn);
        aboutbtn = (Button)findViewById(R.id.aboutbtn);
        exitbtn = (Button)findViewById(R.id.exitbtn);

        startgamebymanandmachine.setOnClickListener(new StartGameListener());
        startgamebymanandman.setOnClickListener(new StartGameListener());
        helpbtn.setOnClickListener(new HelpListener());
        aboutbtn.setOnClickListener(new AboutListener());
        exitbtn.setOnClickListener(new ExitListener());
        /*
        参数说明：
appId 和 appSecret 分别为应用的发布 ID 和密钥，由有米后台自动生成，通过在有米后台 > 应用详细信息 可以获得。
isTestModel : 是否开启测试模式，true 为是，false 为否。（上传有米审核及发布到市场版本，请设置为 false）
isEnableYoumiLog: 是否开启有米的Log输出，默认为开启状态
上传到有米主站进行审核时，务必开启有米的Log，这样才能保证通过审核
开发者发布apk到各大市场的时候，强烈建议关闭有米的Log
         */
        AdManager.getInstance(StartScreenActivity.this).
                init("ce5360bb2cb842a5", "0e7c7db390b22793",
        false, true);
        // 获取广告条
        View bannerView = BannerManager.getInstance(StartScreenActivity.this)
              .getBannerView(
                      new net.youmi.android.normal.banner.BannerViewListener(){

                          /**
                           * 请求广告成功
                           */
                          @Override
                          public void onRequestSuccess() {
                              Log.d("xfhy","请求广告成功");
                          }

                          /**
                           * 切换广告条
                           */
                          @Override
                          public void onSwitchBanner() {
                              Log.d("xfhy","切换广告条");
                          }

                          /**
                           * 请求广告失败
                           */
                          @Override
                          public void onRequestFailed() {
                              Log.d("xfhy","Log.d(\"xfhy\",\"切换广告条\");");
                          }
                      });

        // 获取要嵌入广告条的布局
        LinearLayout bannerLayout = (LinearLayout) findViewById(R.id.ll_banner);

        // 将广告条加入到布局中
        bannerLayout.addView(bannerView);
    }

    //开始游戏  按钮监听器
    class StartGameListener implements View.OnClickListener{
        @Override
        public void onClick(View v) {
            switch (v.getId()){
                case R.id.startgamebymanandmachine:
                    Chessboard.setGamePattern(GamePattern.MANANDMACHINE);
                    break;
                case R.id.startgamebymanandman:
                    Chessboard.setGamePattern(GamePattern.MANANDMAN);
                    break;
                default:
                    break;
            }
            Intent intent = new Intent(StartScreenActivity.this,MainActivity.class);
            startActivity(intent);
        }
    }

    //帮助按钮监听器
    class HelpListener implements View.OnClickListener{
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(StartScreenActivity.this,HelpActivity.class);
            startActivity(intent);
        }
    }

    //关于按钮监听器
    class AboutListener implements View.OnClickListener{
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(StartScreenActivity.this,AboutActivity.class);
            startActivity(intent);
        }
    }

    //退出按钮监听器
    class ExitListener implements View.OnClickListener{
        @Override
        public void onClick(View v) {
            StartScreenActivity.this.finish();
        }
    }

}
