package com.pes.fibness;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.regex.Pattern;

public class ResetPasswordActivity extends AppCompatActivity {

    private EditText newPassword, confirmPassword;
    private Button reset;
    private boolean checkPass, canReset;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);
        final String PASSWORD_REGEX = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{8,16}$";
        newPassword = findViewById(R.id.newPassword);
        confirmPassword = findViewById(R.id.confirm_password);
        reset = (Button) findViewById(R.id.btn_reset);


        newPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { }

            @Override
            public void afterTextChanged(Editable s) {
                final Pattern PASSWORD_PATTERN = Pattern.compile(PASSWORD_REGEX);

                if (newPassword.getText().toString().isEmpty()) {
                    checkPass = false;
                    newPassword.setError("Enter password");
                } else if (!PASSWORD_PATTERN.matcher(newPassword.getText().toString()).matches()) {
                    checkPass = false;
                    newPassword.setError("The password must have at least 8 characters, a lowercase, an uppercase, a number and a special character (!,@,#,&,%)");
                } else {
                    checkPass = true;
                    newPassword.setError(null);
                }
            }
        });

        //Listener for confirm password
        confirmPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { }

            @Override
            public void afterTextChanged(Editable s) {
                if (confirmPassword.getText().toString().isEmpty()) {
                    canReset = false;
                    confirmPassword.setError("Enter confirmation password");
                } else if (!confirmPassword.getText().toString().equals(newPassword.getText().toString())) {
                    canReset = false;
                    confirmPassword.setError("Passwords are different");
                } else {
                    if(checkPass) canReset=true;
                    confirmPassword.setError(null);
                }
            }
        });


        reset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(canReset) resetUserPassword();
                else {
                    Toast.makeText(getApplicationContext(), "Recovery Failed. Please, try again.", Toast.LENGTH_LONG).show();
                }
            }


        });


    }

    private void resetUserPassword() {

        Intent i = getIntent();
        String email = i.getStringExtra("email");
        System.out.println("email introducido: " + email);
        System.out.println("nueva contra: " + confirmPassword.getText().toString());
        //tengo que pasar email y la contrasena haseada, al final se avisa que se ha cambiado la contra (Toast)
        ConnetionAPI connetion = new ConnetionAPI(getApplicationContext(), "http://10.4.41.146:3001/user/resetPassword");
        connetion.resetPassword(email, confirmPassword.getText().toString()); //cuando termine tiene que llevar a la pagina principal
        homeActivity();
    }

    //press back to back login
    @Override
    public void onBackPressed() {
        Intent homePage = new Intent(ResetPasswordActivity.this, LoginActivity.class);
        startActivity(homePage);
        finish();
    }

    private void homeActivity() {
        Intent homePage = new Intent(ResetPasswordActivity.this, SplashActivity.class);
        startActivity(homePage);
        finish();
    }


}
