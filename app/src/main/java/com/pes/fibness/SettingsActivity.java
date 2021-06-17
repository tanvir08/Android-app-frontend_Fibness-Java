package com.pes.fibness;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.util.Locale;

import static android.view.View.OnClickListener;

public class SettingsActivity extends AppCompatActivity {

    private Switch switchAge, switchDistance, switchInvitation, switchFollower,switchMessage;
    private TextView textContact, logOut, done, changeLanguage, delete;
    private ImageView backButton;
    private boolean switchOn1, switchOn2, switchOn3, switchOn4, switchOn5, switchOn6;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        loadLocale(); //load language
        setContentView(R.layout.activity_settings);


        /*press back button to back Fragment_perfil*/
        backButton = (ImageView) findViewById(R.id.backImgButton);
        switchAge = findViewById(R.id.switchAge);
        switchDistance = findViewById(R.id.switchDistance);
        switchInvitation = findViewById(R.id.switchInvitation);
        switchFollower = findViewById(R.id.switchFollower);
        switchMessage = findViewById(R.id.switchMessage);
        textContact = findViewById(R.id.textContact);
        done = findViewById(R.id.done);
        logOut = findViewById(R.id.logOut);
        changeLanguage = findViewById(R.id.changeLanguage);
        delete = findViewById(R.id.deleteAccount);

        /*to go back*/
        backButton.setOnClickListener(new OnClickListener() {
            @SuppressLint("ResourceType")
            @Override
            public void onClick(View v) {
                onBackPressed();
                finish();

            }
        });

        /*save settings data*/
        done.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                saveSettingsData();
                onBackPressed();
                finish();
            }
        });


        textContact.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                sendMail();
            }
        });


        /*change language*/
        changeLanguage.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                showChangeLanguageDialog();
            }
        });


        /*press log out to close session*/
        logOut.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                setSharedPreferences();
                Intent intent = new Intent(SettingsActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });

        /*press to delete account*/
        delete.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                showWarningMessage();
            }
        });

        loadSetting();


    }

    private void setSharedPreferences(){

        SharedPreferences preferences = getSharedPreferences("credentials", Context.MODE_PRIVATE);

        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("userEmail", "");
        editor.putString("userPassword", "");
        editor.putString("userName", "");
        editor.apply();

    }


    /*send email to fibness*/
    private void sendMail() {
        Intent i = new Intent(Intent.ACTION_SEND);
        i.putExtra(Intent.EXTRA_EMAIL, "fibnessinc@gmail.com");
        i.putExtra(Intent.EXTRA_SUBJECT, "Feedback");
        i.putExtra(Intent.EXTRA_TEXT, "");

        i.setType("message/rfc822");
        startActivity(Intent.createChooser(i, "Send us an email"));

    }


    /*before We have to create a new string.xml*/
    private void showChangeLanguageDialog() {
        /*array of languages to display in alert dialog*/
        final String[] listLanguages = {"English", "Spanish", "Catalan", "French"};
        AlertDialog.Builder message = new AlertDialog.Builder(this);
        message.setTitle("Choose Language");
        message.setSingleChoiceItems(listLanguages, -1, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if(i == 0){
                    System.out.println("opcion elegida: " + "english");
                    setLocale("en");
                    recreate();
                }
                else if(i == 1){
                    System.out.println("opcion elegida: " + "spanish");
                    setLocale("es");
                    recreate();
                }
                else if(i == 2){
                    System.out.println("opcion elegida: " + "catalan");
                    setLocale("cat");
                    recreate();
                }
                else if(i == 3){
                    System.out.println("opcion elegida: " + "french");
                    setLocale("fr");
                    recreate();
                }
                /*dismiis alert dialog when language selected*/
                dialogInterface.dismiss();
            }
        });

        AlertDialog alertDialog = message.create();
        alertDialog.show();
    }

    private void setLocale(String lang) {
        Locale locale = new Locale(lang);
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.setLocale(locale);
        getBaseContext().getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());;

        SharedPreferences.Editor editor = getSharedPreferences("SettingLanguage", MODE_PRIVATE).edit();
        editor.putString("myLang", lang);
        editor.apply();

    }

    private void loadLocale(){
        SharedPreferences preferences = getSharedPreferences("SettingLanguage", Activity.MODE_PRIVATE);
        System.out.println("my prefs value :" + preferences.getAll());
        String lang = preferences.getString("myLang", "");
        System.out.println("my prefs lang :" + lang);
        setLocale(lang);
    }




    private void saveSettingsData() {
        boolean[] s = {switchAge.isChecked(), switchDistance.isChecked(), switchInvitation.isChecked(), switchFollower.isChecked(),switchMessage.isChecked()};
        User u = User.getInstance();
        u.setSettings(s);
        String route = "http://10.4.41.146:3001/user/"+u.getId()+"/settings";
        ConnetionAPI connetion = new ConnetionAPI(getApplicationContext(), route);
        connetion.postUserSettings(s);
    }



    private void loadSetting(){
        boolean[] s = User.getInstance().getSettings();
        switchAge.setChecked(s[0]);
        switchDistance.setChecked(s[1]);
        switchInvitation.setChecked(s[2]);
        switchFollower.setChecked(s[3]);
        switchMessage.setChecked(s[4]);
    }



    private void showWarningMessage() {
        AlertDialog.Builder message = new AlertDialog.Builder(this);

        message.setMessage("Are you sure you want to delete your account?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        System.out.println("entro para borrar");
                        User u = User.getInstance();
                        ConnetionAPI connetion = new ConnetionAPI(getApplicationContext(), "http://10.4.41.146:3001/user/"+u.getId());
                        connetion.deleteUser();
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.cancel();
                    }
                });
        AlertDialog alertDialog = message.create();
        alertDialog.show();


    }

}
