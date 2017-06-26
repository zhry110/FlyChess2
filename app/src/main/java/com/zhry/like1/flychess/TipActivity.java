package com.zhry.like1.flychess;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by like1 on 2017/6/13.
 */

public class TipActivity extends Activity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tip);
        TextView t = (TextView) findViewById(R.id.textView);
        ImageView x = (ImageView) findViewById(R.id.imageView4);
        ImageView ok = (ImageView) findViewById(R.id.linearLayout);
        t.setText(getIntent().getStringExtra("tip"));
        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        };
        x.setOnClickListener(listener);
        ok.setOnClickListener(listener);
    }
}
