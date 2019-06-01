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

    private void initComponent() {
        firebaseAuth = FirebaseAuth.getInstance();
        currentUser = firebaseAuth.getCurrentUser();
        oldPasswordEditText = findViewById(R.id.changePasswordOldPasswordEditText);
        newPasswordEditText = findViewById(R.id.changePasswordNewPasswordEditText);
        repeatNewPasswordEditText = findViewById(R.id.changePasswordRepeatNewPasswordEditText);
    }

    public String getNewPassword() {
        String newPassword = newPasswordEditText.getText().toString();
        String repeatedPassword = repeatNewPasswordEditText.getText().toString();
        if (newPassword.equals(repeatedPassword)) {
            return newPassword;
        } else {
            return null;
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (currentUser == null) {
            startActivity(new Intent(getApplicationContext(), MainActivity.class));
        }
    }


    public void changePassword(final View view) {
        String email = currentUser.getEmail();
        final String oldPassword = oldPasswordEditText.getText().toString();


        if (currentUser == null || email == null) {
            startActivity(new Intent(getApplicationContext(), MainActivity.class));
        } else {
            AuthCredential credential = EmailAuthProvider.getCredential(email, oldPassword);

            currentUser.reauthenticate(credential).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {

                        if (getNewPassword() != null) {
                            if (!getNewPassword().equals(oldPassword)) {
                                currentUser.updatePassword(getNewPassword()).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (!task.isSuccessful()) {
                                            Snackbar snackbarFail = Snackbar
                                                    .make(view, "Something went wrong. Check your internet connection or try again later please", Snackbar.LENGTH_LONG);
                                            snackbarFail.show();
                                        } else {
                                            Snackbar snackbarSuccessful = Snackbar
                                                    .make(view, "Password Successfully Modified", Snackbar.LENGTH_LONG);
                                            snackbarSuccessful.show();
                                        }
                                    }
                                });
                            } else {
                                Snackbar snackbarBad = Snackbar
                                        .make(view, "New password and old password cannot be same", Snackbar.LENGTH_LONG);
                                snackbarBad.show();
                            }
                        } else {
                            Snackbar snackbarNotSameNewPassword = Snackbar
                                    .make(view, "Your new password needs to be same in both fields!", Snackbar.LENGTH_LONG);
                            snackbarNotSameNewPassword.show();
                        }


                    } else {
                        Snackbar snackbarWrongPassword = Snackbar
                                .make(view, "Wrong old password", Snackbar.LENGTH_LONG);
                        snackbarWrongPassword.show();
                    }
                }
            });
        }
    }

    public void showSnackBar() {

    }
}
