package com.example.keabank.activities;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.example.keabank.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

public class ResetPasswordActivity extends AppCompatActivity {

	private static final String TAG = "ResetPasswordActivity";
	private EditText emailEditText;
	private FirebaseAuth firebaseAuth;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_reset_password);

		emailEditText = findViewById(R.id.ResetEmailEditText);
		firebaseAuth = FirebaseAuth.getInstance();
	}

	public void sendNewPassword(View view) {
		String email = emailEditText.getText().toString().trim();

		firebaseAuth.sendPasswordResetEmail(email)
				.addOnCompleteListener(new OnCompleteListener<Void>() {
					@Override
					public void onComplete(@NonNull Task<Void> task) {
						if (task.isSuccessful()) {
							Log.d(TAG, "Reset email sent!");
							Toast.makeText(ResetPasswordActivity.this, "Reset password was sent to the email!",
									Toast.LENGTH_SHORT).show();
						}
					}
				});
	}
}
