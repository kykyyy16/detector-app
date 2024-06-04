package com.example.tilapia_white_spot_disease_detection.Activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.example.tilapia_white_spot_disease_detection.Adapter.TrendsAdapter;
import com.example.tilapia_white_spot_disease_detection.Domain.TrendSDomain;
import com.example.tilapia_white_spot_disease_detection.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private RecyclerView.Adapter adapterTrendsList;
    private RecyclerView recyclerViewTrends;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initRecyclerView();

        FloatingActionButton detectBtn = (FloatingActionButton) findViewById(R.id.DetectBtn);

        detectBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, DetectionActivity.class));
            }
        });

        
    }

    private void initRecyclerView() {
        ArrayList<TrendSDomain> items = new ArrayList<>();

        items.add(new TrendSDomain("Fresh tilapia, offering a delicious and nutritious seafood option.","Healthy Tilapia","fish4"));
        items.add(new TrendSDomain("Quickly identifying signs of White Spot Disease in tilapia.","Detection","fish1"));
        items.add(new TrendSDomain("dentifying and treating the presence of White Spot Disease","Curing","fish2"));

        recyclerViewTrends = findViewById(R.id.view1);
        recyclerViewTrends.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL,false));

        adapterTrendsList = new TrendsAdapter(items);
        recyclerViewTrends.setAdapter(adapterTrendsList);
    }
}