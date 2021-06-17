package com.pes.fibness;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import androidx.appcompat.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        boolean logged = getSharedPreferences();
        if (!logged) {
            Intent logIn = new Intent(SplashActivity.this, LoginActivity.class);
            startActivity(logIn);
            finish();
        }
        super.onCreate(savedInstanceState);
        // Duración en milisegundos que se mostrará el splash
        // 2 segundos
        int DURACION_SPLASH = 2000;
        new Handler().postDelayed(new Runnable(){
            public void run(){
                // Cuando pasen los 2 segundos, pasamos a la actividad principal de la aplicación

                if (logged) {
                    Intent intent = new Intent(SplashActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                }
            }
        }, DURACION_SPLASH);
    }

    private boolean getSharedPreferences(){
        SharedPreferences preferences = getSharedPreferences("credentials", Context.MODE_PRIVATE);

        String userName = preferences.getString("userName", "");
        String userEmail = preferences.getString("userEmail", "");
        String userPassword = preferences.getString("userPassword", "");

        if (userEmail != "" && userPassword != "" && userName == "") {
            checkUser(userEmail, userPassword);
            return true;
        }else if (userEmail != "" && userPassword != "" && userName != "") {
            checkUserFacebook(userName, userEmail, userPassword);
            return true;
        }
        return false;

    }

    private void checkUserFacebook(String userName, String userEmail, String userPassword) {
        ConnetionAPI connection = new ConnetionAPI(getApplicationContext(), "http://10.4.41.146:3001/user/fb");
        connection.fbUser(userName, userEmail, userPassword);

        getAllUserInfo();
    }

    private void checkUser(String userEmail, String userPassword) {

        ConnetionAPI connection = new ConnetionAPI(getApplicationContext(), "http://10.4.41.146:3001/user/validate");
        connection.validateUser(userEmail, userPassword);

        getAllUserInfo();

    }

    private void getAllUserInfo() {
        @SuppressLint("HandlerLeak") Handler h = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                ConnetionAPI connection;

                connection = new ConnetionAPI(getApplicationContext(), "http://10.4.41.146:3001/user/" + User.getInstance().getId() + "/trainings");
                connection.getUserTrainings();

                connection = new ConnetionAPI(getApplicationContext(), "http://10.4.41.146:3001/user/" + User.getInstance().getId() + "/routes");
                connection.getUserRoutes();

                connection = new ConnetionAPI(getApplicationContext(), "http://10.4.41.146:3001/user/" + User.getInstance().getId() + "/diets");
                connection.getUserDiets();

                connection = new ConnetionAPI(getApplicationContext(), "http://10.4.41.146:3001/user/" + User.getInstance().getId() + "/events");
                connection.getUserEvents();

                connection = new ConnetionAPI(getApplicationContext(), "http://10.4.41.146:3001/event");
                connection.getAllEvents();
            }
        };
        h.sendEmptyMessageDelayed(0, 600);
    }
}
