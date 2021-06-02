package com.wdcloud.wdanalytics.analyticscenter.util;

import android.content.Intent;
import android.os.Bundle;

import com.wdcloud.wdanalytics.R;
import com.wdcloud.wdanalytics.analyticscenter.service.AnalyticsService;

import androidx.appcompat.app.AppCompatActivity;

public class AnalyticActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_analytic);
        Intent serviceintent = new Intent(AnalyticActivity.this, AnalyticsService.class);
        startService(serviceintent);
        finish();
    }
}
