package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.os.Bundle;
import android.widget.Toast;

import java.util.concurrent.TimeUnit;

import io.reactivex.Completable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Action;

public class SplashScreenActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        delaysplahscreen();
    }
    void delaysplahscreen()
    {
        Completable.timer(5, TimeUnit.SECONDS, AndroidSchedulers.mainThread()).subscribe(() -> Toast.makeText(SplashScreenActivity.this,"Splash Screen Done!!",Toast.LENGTH_SHORT).show());
        System.out.println("You");
    }
}