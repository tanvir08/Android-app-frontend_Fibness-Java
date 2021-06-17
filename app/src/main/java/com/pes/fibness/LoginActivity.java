package com.pes.fibness;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;

import org.json.JSONException;
import org.json.JSONObject;

public class LoginActivity extends AppCompatActivity {

    private EditText emailAddress;
    private EditText password;
    private Button login;
    private LoginButton loginFb;
    private TextView forgotPassword;
    private TextView newAccount;
    private CallbackManager callbackManager;
    private long backPressedTime; //time will be in ms of the click
    private Toast backToast;
    private boolean forgetEntry = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        setTheme(R.style.AppTheme_NoActionBar);
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        callbackManager = CallbackManager.Factory.create();
        loginFb = (LoginButton) findViewById(R.id.login_button);
        loginFb.setReadPermissions("email");
        emailAddress = (EditText)findViewById(R.id.username);
        password = (EditText)findViewById(R.id.password);
        login = (Button) findViewById(R.id.btn_login);
        forgotPassword = (TextView) findViewById(R.id.forgot_password);
        newAccount = (TextView) findViewById(R.id.new_account);

        //press to register a new account
        newAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent registerIntent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(registerIntent);
                finish();

            }
        });


        //press to login (an example)
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                boolean b = verifyEmail(emailAddress.getEditableText().toString().trim());

                if (!b)
                    emailAddress.setError("Enter a valid email.");
                else {
                    setSharedPreferences(emailAddress.getText().toString(), password.getText().toString());
                }


                //homeActivity();
            }
        });


        /*send an email to user with random code and verify the code to change password*/
        forgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /*Pre: email has to be belong google provider*/
                showRecoverPasswordDialog();
            }
        });




        // Callback registration
        loginFb.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {

            @Override
            public void onSuccess(LoginResult loginResult) {
                GraphRequest request = GraphRequest.newMeRequest(AccessToken.getCurrentAccessToken(), new GraphRequest.GraphJSONObjectCallback() {
                    @Override
                    public void onCompleted(JSONObject object, GraphResponse response) {
                        System.out.println("FB1");
                        JSONObject json = response.getJSONObject();
                        try {
                            System.out.println("FB2");
                            if(json != null){
                                String email = json.getString("email");
                                Toast.makeText(getApplicationContext(), "User email: " + email, Toast.LENGTH_SHORT).show();
                                String name = json.getString("name");
                                String id = json.getString("id");
                                setSharedPreferencesFacebook(name,email,id);

                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
                Bundle parameters = new Bundle();
                parameters.putString("fields", "id,name,link,email,picture");
                request.setParameters(parameters);
                request.executeAsync();

            }

            @Override
            public void onCancel() {
                // App code

                Toast.makeText(getApplicationContext(), R.string.cancel_login, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(FacebookException exception) {
                // App code
                Toast.makeText(getApplicationContext(),R.string.error_login, Toast.LENGTH_SHORT).show();
            }
        });
    }



    private void showRecoverPasswordDialog() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Get a verification code");

        LinearLayout linearLayout = new LinearLayout(this);
        final EditText emailEd = new EditText(this);
        emailEd.setHint("Email address");
        emailEd.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        linearLayout.addView(emailEd);
        linearLayout.setPadding(10,10,10,10);
        builder.setView(linearLayout);

        builder.setPositiveButton("Next", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                String email = emailEd.getText().toString().trim();
                boolean b = verifyEmail(email);
                if (!b)
                    Toast.makeText(getApplicationContext(), "Enter a valid gmail.", Toast.LENGTH_LONG).show();
                else
                    sendEmail(email);
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int i) {
                dialog.cancel();
            }
        });

        builder.create().show();

    }


    private boolean verifyEmail(String checkEmail) {
        final String regex = "(?:[^<>()\\[\\].,;:\\s@\"]+(?:\\.[^<>()\\[\\].,;:\\s@\"]+)*|\"[^\\n\"]+\")@(?:[^<>()\\[\\].,;:\\s@\"]+\\.)+[^<>()\\[\\]\\.,;:\\s@\"]{2,63}";
        return checkEmail.matches(regex);
    }


    private void sendEmail(final String email) {

        final String verifactionCode = Password.generateCode();
        User.getInstance().setRecoveryCode(verifactionCode);

        final ProgressDialog dialog = new ProgressDialog(LoginActivity.this);
        dialog.setTitle("Sending Email");
        dialog.setMessage("Please wait");
        dialog.show();
        Thread sender = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    GmailSender sender = new GmailSender("fibnessinc@gmail.com", "Pes_asw_20");
                    sender.sendMail("Reset your password",
                            "Verification code is: " + verifactionCode,
                            "fibnessinc@gmail.com",
                            email);
                    System.out.println("Email enviado"); //activar email  https://www.google.com/settings/security/lesssecureapps
                    dialog.dismiss();
                } catch (Exception e) {
                    Log.e("mylog", "Error: " + e.getMessage());
                }
            }
        });
        sender.start();

        //verify code
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Enter the code");

        LinearLayout linearLayout = new LinearLayout(this);
        final EditText code = new EditText(this);
        code.setHint("Verification code");
        code.setInputType(InputType.TYPE_CLASS_TEXT);
        linearLayout.addView(code);
        linearLayout.setPadding(10,10,10,10);
        builder.setView(linearLayout);

        builder.setPositiveButton("Next", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                String s = code.getText().toString().trim();
                System.out.println("codigo introducido: "+ s);
                System.out.println("codigo guardado: " + User.getInstance().getRecoveryCode());

                if (s.equals(User.getInstance().getRecoveryCode())) {
                    Intent resetPasswordPage = new Intent(LoginActivity.this, ResetPasswordActivity.class);
                    resetPasswordPage.putExtra("email", email);
                    startActivity(resetPasswordPage);
                }
                else {
                    Toast.makeText(getApplicationContext(), "The code is incorrect.", Toast.LENGTH_LONG).show();
                }
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int i) {
                dialog.cancel();
            }
        });

        builder.create().show();


    }



    //validate user
    private void checkUser(String userEmail, String userPassword) {
        ConnetionAPI connection = new ConnetionAPI(getApplicationContext(), "http://10.4.41.146:3001/user/validate");
        connection.validateUser(userEmail, userPassword);

        @SuppressLint("HandlerLeak") Handler h = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                homeActivity();
                finish();
            }
        };
        h.sendEmptyMessageDelayed(0, 1000);

    }

    private void getSharedPreferences(){
        SharedPreferences preferences = getSharedPreferences("credentials", Context.MODE_PRIVATE);

        String userEmail = preferences.getString("userEmail", "");
        String userPassword = preferences.getString("userPassword", "");

        if (userEmail != "" && userPassword != "") checkUser(userEmail, userPassword);

    }

    private void setSharedPreferencesFacebook(String name, String email, String id) {
        SharedPreferences preferences = getSharedPreferences("credentials", Context.MODE_PRIVATE);

        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("userName", name);
        editor.putString("userEmail", email);
        editor.putString("userPassword", id);
        editor.apply();

        homeActivity();
    }

    private void setSharedPreferences(String userEmail, String userPassword){

        SharedPreferences preferences = getSharedPreferences("credentials", Context.MODE_PRIVATE);

        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("userEmail", userEmail);
        editor.putString("userPassword", userPassword);
        editor.apply();

        homeActivity();

    }

    private void homeActivity() {
        Intent homePage = new Intent(LoginActivity.this, SplashActivity.class);
        startActivity(homePage);
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        callbackManager.onActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);
    }



    //press back to exit but before show a message to confirm
    @Override
    public void onBackPressed() {
        AlertDialog.Builder message = new AlertDialog.Builder(this);

        message.setMessage("Are you sure you want to Exit?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        finishAffinity();
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
