package com.example.tabbedtest;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.tabbedtest.Prevelent.Prevelent;

import io.paperdb.Paper;

public class LogInPage extends AppCompatActivity {
    EditText  password;
    Button login  ;
    ProgressDialog loadingBar;

    //String UserPasswordKey ;
    void id(){
        password=findViewById(R.id.login_password);
        login=findViewById(R.id.login);
        loadingBar = new ProgressDialog(this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_in_page);
        id();


        Paper.init(this);

        Paper.book().write(Prevelent.userPasswordKey,"igeepompe" );

        LogIn();
    }

    public void LogIn(){
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String pass = password.getText().toString();

                String UserPasswordKey = Paper.book().read(Prevelent.userPasswordKey);

                if(UserPasswordKey.equals(pass)){
                    //Toast.makeText(LogInPage.this,"Login in", Toast.LENGTH_LONG).show();
                    Intent i = new Intent(LogInPage.this , MainActivity.class);
                    startActivity(i);
                }else{
                    Toast.makeText(LogInPage.this,"Password wrong", Toast.LENGTH_LONG).show();
                }

                /*if (UserPasswordKey!=""){
                    if (!TextUtils.isEmpty(UserPasswordKey)){

                    }
                }*/
            }
        });
    }

}