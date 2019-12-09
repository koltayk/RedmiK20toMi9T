package com.android.kk.redmik20tomi9t;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

public class Splash extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_pre);
        startActivity(new Intent(Splash.this, MainActivity.class));
        finish();

//        Handler handler = new Handler();
//        handler.postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                Intent openMainActivity =  new Intent(Splash.this, MainActivity.class);
//                startActivity(openMainActivity);
//                finish();
//
//            }
//        }, 50);
    }
}
