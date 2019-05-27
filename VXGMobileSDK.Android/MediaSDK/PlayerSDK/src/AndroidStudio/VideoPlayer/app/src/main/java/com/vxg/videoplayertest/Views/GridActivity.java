package com.vxg.videoplayertest.Views;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;

import com.vxg.videoplayertest.R;


public class GridActivity extends Activity {
    private FrameLayout  btnPlayer1;
    private FrameLayout  btnPlayer2;
    private FrameLayout  btnPlayer3;
    private FrameLayout  btnPlayer4;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_grid);
        btnPlayer1 = findViewById(R.id.btnPlayer1);
        btnPlayer2 = findViewById(R.id.btnPlayer2);
        btnPlayer3 = findViewById(R.id.btnPlayer3);
        btnPlayer4 = findViewById(R.id.btnPlayer4);

        btnPlayer1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(GridActivity.this,Player1Activity.class));
            }
        });

        btnPlayer3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(GridActivity.this,PlayBackActivity.class));
            }
        });

		btnPlayer4.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				startActivity(new Intent(GridActivity.this,Player3GridActivity.class));
			}
		});
		
    }
}
