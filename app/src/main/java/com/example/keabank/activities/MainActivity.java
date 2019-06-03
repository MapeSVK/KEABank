package com.example.keabank.activities;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
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

	@Override
	public void onStart() {
		super.onStart();
		// Check if user is signed in and if yes then update UI
		currentUser = firebaseAuth.getCurrentUser();
		if (currentUser != null) {
			//updateUI(currentUser); intent ked user je automaticky prihlaseny
			Log.d(TAG, "User " + currentUser.getEmail() + " was automatically logged in");
		}
	}

	private void initComponents() {
		loginEmailEditText = findViewById(R.id.loginEmailEditText);
		loginPasswordEditText = findViewById(R.id.loginPasswordEditText);
		firebaseAuth = FirebaseAuth.getInstance(); // firebase instance
	}

	// after login button is pressed
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
							//finish(); // activity is done and should be closed
							//updateUI(user); // START INTENT

                            // hide keyboard
                            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
							imm.hideSoftInputFromWindow(view.getWindowToken(), 0);

							startActivity(new Intent(getApplicationContext(), NavigationDrawerActivity.class));
						} else {
							// If sign in fails, display a message to the user.
							Log.w(TAG, "Signing in failed! ", task.getException());
							Toast.makeText(MainActivity.this, "Login failed. Try again!",
									Toast.LENGTH_SHORT).show();
						}
					}
				});
	}

	public void resetPassword(View view) {
		Intent intent = new Intent(this, ResetPasswordActivity.class);
		startActivity(intent);
	}

	public void registerNewUser(View view) {
		Intent intent = new Intent(this, RegisterActivity.class);
		startActivity(intent);
	}




	public void logout(View view) {
		if(currentUser !=null){
			firebaseAuth.signOut();
			System.out.println("user signed out");
		}
	}

	public void navigation(View view) {
		startActivity(new Intent(getApplicationContext(), NavigationDrawerActivity.class));
	}
}
