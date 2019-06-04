package com.example.keabank.activities;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import com.example.keabank.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

/* Activity responsible for login
 * */
public class MainActivity extends AppCompatActivity {

	private static final String TAG = "MainActivity";
	private EditText loginEmailEditText;
	private EditText loginPasswordEditText;
	private FirebaseAuth firebaseAuth;
	private FirebaseUser currentUser;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		initComponents();
	}

	/* Check if the user is signed in. If so, then create an intent
	 *  */
	@Override
	public void onStart() {
		super.onStart();
		currentUser = firebaseAuth.getCurrentUser();
		if (currentUser != null) {
			startActivity(new Intent(getApplicationContext(), NavigationDrawerActivity.class));
			Log.d(TAG, "User " + currentUser.getEmail() + " was automatically logged in");
		}
	}

	/* After the login button is pressed
	 * Firstly get the data from edit texts (user input)
	 * Use method to login the user with email and password, hide the keyboard and eventually create an intent
	 * */
	public void login(final View view) {
		String email = loginEmailEditText.getText().toString().trim();
		String password = loginPasswordEditText.getText().toString().trim();

		firebaseAuth.signInWithEmailAndPassword(email, password)
				.addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
					@Override
					public void onComplete(@NonNull Task<AuthResult> task) {
						if (task.isSuccessful()) {
							// Sign in success, update UI with the signed-in user's information
							currentUser = firebaseAuth.getCurrentUser();
							Log.d(TAG, "User: " + (currentUser != null ? currentUser.getEmail() : null) + " has been successfully signed in!");

                            // hide keyboard
                            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
							imm.hideSoftInputFromWindow(view.getWindowToken(), 0);

							startActivity(new Intent(getApplicationContext(), NavigationDrawerActivity.class));
						} else {
							// If sign in fails, display a message to the user.
							Log.w(TAG, "Signing in failed! ", task.getException());
							snackbarShow("Login failed. Check your internet connection or try again later!");
						}
					}
				});
	}

	/* Start Reset Password activity after click on the button
	 * */
	public void resetPassword(View view) {
		Intent intent = new Intent(this, ResetPasswordActivity.class);
		startActivity(intent);
	}

	/* Start Register Activity after click on the button
	 * */
	public void registerNewUser(View view) {
		Intent intent = new Intent(this, RegisterActivity.class);
		startActivity(intent);
	}

	/* Initialise the components taken from the view
	 * */
	private void initComponents() {
		loginEmailEditText = findViewById(R.id.loginEmailEditText);
		loginPasswordEditText = findViewById(R.id.loginPasswordEditText);
		firebaseAuth = FirebaseAuth.getInstance();
	}

	private void snackbarShow(String msg) {
		Snackbar snackbarBad = Snackbar
				.make(findViewById(android.R.id.content), msg, Snackbar.LENGTH_LONG);
		snackbarBad.show();
	}
}
