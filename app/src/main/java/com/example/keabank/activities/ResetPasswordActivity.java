package com.example.keabank.activities;

import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
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

/* Activity responsible for password reset
 * */
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

	/* FirebaseAuth method to send password reset to the user's email
	 * */
	public void sendNewPassword(final View view) {
		String email = emailEditText.getText().toString().trim();

		firebaseAuth.sendPasswordResetEmail(email)
				.addOnCompleteListener(new OnCompleteListener<Void>() {
					@Override
					public void onComplete(@NonNull Task<Void> task) {
						if (task.isSuccessful()) {
							Log.d(TAG, "Reset email sent!");

							Snackbar snackbarBad = Snackbar
									.make(view, "New password was sent to the email!", Snackbar.LENGTH_LONG);
							snackbarBad.show();
						}
					}
				});
	}
}
