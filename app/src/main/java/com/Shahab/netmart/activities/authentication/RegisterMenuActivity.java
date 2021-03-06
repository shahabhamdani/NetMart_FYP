package com.Shahab.netmart.activities.authentication;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;

import com.Shahab.netmart.R;


public class RegisterMenuActivity extends AppCompatActivity {


    private ImageView imgUser;
    private ImageView imgSeller;
    private ImageView imgRider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_register_menu);

        imgUser = findViewById(R.id.imgUser);
        imgSeller = findViewById(R.id.imgSeller);
        imgRider =  findViewById((R.id.imgRider));

        imgUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                startActivity( new Intent(RegisterMenuActivity.this, RegisterUserActivity.class ));
            }
        });

        imgSeller.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                startActivity( new Intent(RegisterMenuActivity.this, RegisterSellerActivity.class ));
            }
        });

        imgRider.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                startActivity( new Intent(RegisterMenuActivity.this, RegisterRiderActivity.class ));
            }
        });
    }
}