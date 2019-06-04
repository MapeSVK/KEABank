package com.example.keabank.activities;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import com.example.keabank.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ChangePasswordActivity extends AppCompatActivity {
    private static final String TAG = "ChangePasswordActivity";
    private FirebaseUser currentUser;
    private FirebaseAuth firebaseAuth;
    private EditText oldPasswordEditText, newPasswordEditText, repeatNewPasswordEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);
        initComponent();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (currentUser == null) {
            startActivity(new Intent(getApplicationContext(), MainActivity.class));
        }
    }

    /* validation - check if the value from "password" input is same as value from "repeat password" input
    * returns this newPassword if they are same*/
    public String getNewPassword() {
        String newPassword = newPasswordEditText.getText().toString();
        String repeatedPassword = repeatNewPasswordEditText.getText().toString();
        if (newPassword.equals(repeatedPassword)) {
            return newPassword;
        } else {
            return null;
        }
    }

    /* The user firstly need to reauthenticate with the old credentials. Email is taken automatically and password (for the safety reasons) is
     * taken from the user's input. Then getNewPassword method checks if the password is same in newPassword and repeatNewPassword.
      * If yes, it triggers updatePassword method to change the password*/
    public void changePassword(final View view) {
        String email = currentUser.getEmail();
        final String oldPassword = oldPasswordEditText.getText().toString();


        if (currentUser == null || email == null) { // if it is not able to get an email, the user needs to sign in again
            startActivity(new Intent(getApplicationContext(), MainActivity.class));
        } else {
            AuthCredential credential = EmailAuthProvider.getCredential(email, oldPassword);

            currentUser.reauthenticate(credential).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        if (getNewPassword() != null) { // validation check
                            if (!getNewPassword().equals(oldPassword)) { // the new password cannot be same as the old one
                                // update password method
                                currentUser.updatePassword(getNewPassword()).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (!task.isSuccessful()) {
                                            snackbarShow("Something went wrong. Check your internet connection or try again later please");
                                        } else {
                                            snackbarShow("Password Successfully Modified");
                                        }
                                    }
                                });
                            } else {
                                snackbarShow("New password and old password cannot be same");
                            }
                        } else {
                            snackbarShow("Your new password needs to be same in both fields!");
                        }
                    } else {
                        snackbarShow("Wrong old password!");
                    }
                }
            });
        }
    }

    private void initComponent() {
        firebaseAuth = FirebaseAuth.getInstance();
        currentUser = firebaseAuth.getCurrentUser();
        oldPasswordEditText = findViewById(R.id.changePasswordOldPasswordEditText);
        newPasswordEditText = findViewById(R.id.changePasswordNewPasswordEditText);
        repeatNewPasswordEditText = findViewById(R.id.changePasswordRepeatNewPasswordEditText);
    }

    private void snackbarShow(String msg) {
        Snackbar snackbarBad = Snackbar
                .make(findViewById(android.R.id.content), msg, Snackbar.LENGTH_LONG);
        snackbarBad.show();
    }
}
