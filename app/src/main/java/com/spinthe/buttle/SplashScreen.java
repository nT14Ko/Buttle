package com.spinthe.buttle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;

import java.sql.Time;
import java.util.Timer;
import java.util.TimerTask;

public class SplashScreen extends AppCompatActivity {

    private Timer timer;
    private TimerTask timerTask;

    private FirebaseRemoteConfig remoteConfig;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        remoteConfig = FirebaseRemoteConfig.getInstance();

        FirebaseRemoteConfigSettings settings = new FirebaseRemoteConfigSettings.Builder()
                .build();

        remoteConfig.setConfigSettingsAsync(settings);

        remoteConfig.setDefaultsAsync(R.xml.network_security_config);

        vidget();

        getData();
    }

    private void getData(){
        remoteConfig.fetch(0).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isComplete()){
                    String check = remoteConfig.getString("pochemytak");
                    
                    if(!check.equals("false") && check != null) {
                        Toast.makeText(SplashScreen.this, "Congratulation", Toast.LENGTH_SHORT).show();
                    } else
                        vidget();
                }
            }
        });
    }

    private void vidget(){
        timer = new Timer();
        timerTask = new TimerTask() {
            public void run() {
                Intent intent = new Intent(SplashScreen.this, Game.class);
                startActivity(intent);
                finish();

            }
        };
        timer.schedule(timerTask, 4000);
    }
}