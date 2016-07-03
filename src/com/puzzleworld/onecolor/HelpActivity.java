package com.puzzleworld.onecolor;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;

/*
 * 作品展示页面
 */

public class HelpActivity extends Activity {

    private ImageView ivBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);
        ivBack = (ImageView) findViewById(R.id.ivBack);

        ivBack.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                HelpActivity.this.finish();
            }
        });
    }
}
