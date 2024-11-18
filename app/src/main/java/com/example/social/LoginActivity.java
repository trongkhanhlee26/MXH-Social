package com.example.social;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity {

    private EditText email, password;
    private TextView RegisterBtn;
    private Button LoginBtn, forgotpassword;
    private ProgressDialog progressdialog;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        mAuth = FirebaseAuth.getInstance();
        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        LoginBtn = findViewById(R.id.LoginBtn);
        forgotpassword = findViewById(R.id.forgotpassword);
        progressdialog = new ProgressDialog(this);
        RegisterBtn = findViewById(R.id.registerBtn);

        LoginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                login();
            }
        });

        forgotpassword.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                startActivity(new Intent(LoginActivity.this,ForgotPasswordActivity.class));
            }
        });
        RegisterBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
            }
        });
    }

    private void login() {
        String em, pass;
        em = email.getText().toString().trim();
        pass = password.getText().toString();
        if(TextUtils.isEmpty(em)){
            Toast.makeText(this,"Please Enter Email!",Toast.LENGTH_SHORT).show();
            return;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(em).matches()) {
            Toast.makeText(this, "Please enter a valid Email address!", Toast.LENGTH_SHORT).show();
            return;
        }
        if(TextUtils.isEmpty(pass)){
            Toast.makeText(this,"Please enter a Password!",Toast.LENGTH_SHORT).show();
            return;
        }
        if(pass.length() < 6){
            Toast.makeText(this,"Password must be at least 6 characters long!",Toast.LENGTH_SHORT).show();
            return;
        }
        progressdialog.show();
        mAuth.signInWithEmailAndPassword(em,pass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                progressdialog.dismiss();
                if(task.isSuccessful()){
                    Toast.makeText(getApplicationContext(),"Logged in successfully!",Toast.LENGTH_SHORT).show();
                    String userId = mAuth.getCurrentUser().getUid();
                    applyUserTheme(userId);
                    Intent i = new Intent(LoginActivity.this, HomePageActivity.class);
                    startActivity(i);
                }else {
                    Toast.makeText(getApplicationContext(),"Login unsuccessful!",Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void applyUserTheme(String userId) {
        SharedPreferences sharedPreferences = getSharedPreferences("USER_PREF_" + userId, Context.MODE_PRIVATE);
        boolean nightMode = sharedPreferences.getBoolean("night_mode", false);
        if (nightMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
    }
}
