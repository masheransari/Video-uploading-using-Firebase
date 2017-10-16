package com.example.asheransari.video_firebase;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class login extends AppCompatActivity {

    EditText email, psk;
    FirebaseAuth firebaseAuth;
    Button signUp;
    private void init(){
        email = (EditText)findViewById(R.id.email);
        psk = (EditText)findViewById(R.id.psk);
        signUp = (Button)findViewById(R.id.signUp);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        firebaseAuth = FirebaseAuth.getInstance();
        init();
        if (firebaseAuth.getCurrentUser() != null){
            Intent i = new Intent(login.this,MainActivity.class);
            startActivity(i);
            finish();
        }

        signUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (TextUtils.isEmpty(email.getText().toString()) || TextUtils.isEmpty(psk.getText().toString())){
                    Toast.makeText(login.this, "Filled detail first..!!", Toast.LENGTH_SHORT).show();
                }
                else{
                    firebaseAuth.createUserWithEmailAndPassword(email.getText().toString(),psk.getText().toString())
                    .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                        @Override
                        public void onSuccess(AuthResult authResult) {
                            if (authResult.getUser() != null){
                                Intent i = new Intent(login.this,MainActivity.class);
                                startActivity(i);
                                finish();
                            }
                            else{
                                Toast.makeText(login.this, "try another email addresss", Toast.LENGTH_SHORT).show();
                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(login.this, "Check internet connection", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });

    }
}
