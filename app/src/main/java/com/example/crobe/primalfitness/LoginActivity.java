package com.example.crobe.primalfitness;

import android.content.Intent;
import android.content.res.Configuration;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    private Button signIn, register, test;
    public static EditText passwordLogin;
    private EditText emailAddress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        Configuration config = getResources().getConfiguration();

        signIn = (Button) this.findViewById(R.id.signIn);
        signIn.setOnClickListener(this);
        test = (Button) this.findViewById(R.id.test);
        test.setOnClickListener(this);
        register = (Button) this.findViewById(R.id.register);
        register.setOnClickListener(this);
        emailAddress = (EditText)findViewById(R.id.username);
        emailAddress.setText("");
        passwordLogin = (EditText)findViewById(R.id.password);
        passwordLogin.setText("");

    }
    @Override
    public void onClick(View v) {
        if(v.equals(signIn)) {
            String username = emailAddress.getText().toString();
            String password = passwordLogin.getText().toString();
            String type = "login";
            try {
                password = AESCrypt.encrypt(password);
            } catch (Exception e) {
                e.printStackTrace();
            }
            BackgroundWorker backgroundWorker = new BackgroundWorker(this);
            backgroundWorker.execute(type, username, password);
        }else if(v.equals(register)){
            Intent in = new Intent(this, RegisterActivity.class);
            startActivity(in);
            LoginActivity.this.finish();
        } else if(v.equals(test)){
            Intent in = new Intent(this, NavigationActivity.class);
            startActivity(in);
            LoginActivity.this.finish();
        }
    }
}
