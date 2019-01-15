package com.example.crobe.primalfitness;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

public class RegisterActivity extends AppCompatActivity implements View.OnClickListener {

    private EditText firstName, surname, emailAddress, password;
    private Spinner type;
    private Button submit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        Spinner spinner = (Spinner) findViewById(R.id.userTypeChoice);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.user_type_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        firstName = (EditText)findViewById(R.id.firstName);
        surname = (EditText)findViewById(R.id.surname);
        emailAddress = (EditText)findViewById(R.id.emailAddress);
        password = (EditText)findViewById(R.id.password);
        type =(Spinner) findViewById(R.id.userTypeChoice);

        submit = (Button) this.findViewById(R.id.submit);
        submit.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        String firstName_str = firstName.getText().toString();
        String surname_str = surname.getText().toString();
        String emailAddress_str = emailAddress.getText().toString();
        String password_str = password.getText().toString();
        String type_str = type.getSelectedItem().toString();
        if (!inputValid(emailAddress_str) && !checkString(password_str)){
            Toast.makeText(this, "Invalid Email & Password", Toast.LENGTH_SHORT).show();
        } else if(!checkString(password_str)){
            Toast.makeText(this, "Invalid Password", Toast.LENGTH_SHORT).show();
        } else if(!inputValid(emailAddress_str)){
            Toast.makeText(this, "Invalid Email", Toast.LENGTH_SHORT).show();
        } else{
            String action = "register";
            try {
                password_str = AESCrypt.encrypt(password_str);
            } catch (Exception e) {
                e.printStackTrace();
            }
            BackgroundWorker backgroundWorker = new BackgroundWorker(this);
            backgroundWorker.execute(action, firstName_str, surname_str, emailAddress_str, password_str, type_str);
        }
    }

    private static boolean inputValid(String email){
        if((!TextUtils.isEmpty(email) && Patterns.EMAIL_ADDRESS.matcher(email).matches())){
            return true;
        }else{
            return false;
        }

    }

    private static boolean checkString(String str) {
        char ch;
        boolean capitalFlag = false;
        boolean lowerCaseFlag = false;
        boolean numberFlag = false;
        if(str.length() < 6){
            return false;
        }else{
            for(int i=0;i < str.length();i++) {
                ch = str.charAt(i);
                if( Character.isDigit(ch)) {
                    numberFlag = true;
                }
                else if (Character.isUpperCase(ch)) {
                    capitalFlag = true;
                } else if (Character.isLowerCase(ch)) {
                    lowerCaseFlag = true;
                }
                if(numberFlag && capitalFlag && lowerCaseFlag)
                    return true;
            }
        }
        return false;
    }
}