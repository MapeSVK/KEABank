package com.example.keabank.activities;

import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.example.keabank.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/* Responsible for registering new user
 * */
public class RegisterActivity extends AppCompatActivity {

	private static final String TAG = "ResetPasswordActivity";
	private FirebaseAuth firebaseAuth;
	private FirebaseFirestore firestoreDatabase;
	private EditText firstnameEditText, lastnameEditText, dateOfBirthEditText, passwordEditText, emailEditText;
	private String email, password, firstname, lastname, dateOfBirthString;
	private Timestamp dateOfBirthTimestamp;
	private SimpleDateFormat simpleDateFormat;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_register);
		initComponents();
	}

	/* Triggered by button -> firstly gets user inputs, converting birthday to TimeStamp (for firebase usage)
	 * Then validates inputs
	 * Calls method for creating new user in Firebase Auth
	 * */
	public void register(View view){
		email = emailEditText.getText().toString().trim();
		password = passwordEditText.getText().toString().trim();
		firstname = firstnameEditText.getText().toString().trim();
		lastname = lastnameEditText.getText().toString().trim();

		simpleDateFormat = new SimpleDateFormat("MM/dd/yyyy");
		dateOfBirthString = dateOfBirthEditText.getText().toString().trim();
		try {
			Date dateOfBirthDate = simpleDateFormat.parse(dateOfBirthString);
			dateOfBirthTimestamp = new Timestamp(dateOfBirthDate);
		} catch (ParseException e) {
			e.printStackTrace();
			// validation - date must be in format: month/day/year
		}

		emptyFieldValidation(email, password, firstname, lastname, dateOfBirthEditText);
		createNewUserInAuthentication(email, password);
	}

	/* Creates new user in FirebaseAuth system
	 * */
	private void createNewUserInAuthentication(final String email, String password) {
		firebaseAuth.createUserWithEmailAndPassword(email, password)
				.addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
					@Override
					public void onComplete(@NonNull Task<AuthResult> task) {
						try {
							if (task.isSuccessful()) {
								Log.d(TAG, "A new user was created!");
								FirebaseUser newUser = firebaseAuth.getCurrentUser(); // getting new user object

								/* firebase firestore database - creating new document with the same uID as authentication has */
								if (newUser != null) {
									createNewUserInDatabase(newUser.getUid(), firstname, lastname, dateOfBirthTimestamp, email);
								} else {
									Log.d(TAG, "A new user was NOT created! His uID is null");
									snackbarShow("Error while registering the user. Try later please!");
								}
							}else {
								snackbarShow("Error while registering the user. Check your internet connection or try later please!");
							}
						}catch (Exception e){
							e.printStackTrace();
							Log.d(TAG, "Couldn't register to FirebaseAuth system");
							snackbarShow("Error while registering the user. Check your internet connection or try later please!");
						}
					}
				});
	}

	/* Method responsible for saving the new user to the database - firestore with the same uID as he has in Auth system
	 * */
	private void createNewUserInDatabase(final String uID, String firstname, String lastname, Timestamp dateOfBirth, String email) {
		Map<String, Object> mainUserInfo = new HashMap<>();
		mainUserInfo.put("first_name", firstname);
		mainUserInfo.put("last_name", lastname);
		mainUserInfo.put("date_of_birth", dateOfBirth);
		mainUserInfo.put("email", email);

		firestoreDatabase.collection("users").document(uID)
				.set(mainUserInfo)
				.addOnSuccessListener(new OnSuccessListener<Void>() {
					@Override
					public void onSuccess(Void aVoid) {
						// create default accounts - same for all users (default, budget)
						createUserAccountsCollectionWithDefaultAccounts(uID);
						Log.d(TAG, "new user document successfully written to firestore!");
						snackbarShow("Registration successful! You can now sign in!");
						finish();
					}
				})
				.addOnFailureListener(new OnFailureListener() {
					@Override
					public void onFailure(@NonNull Exception e) {
						snackbarShow("Something went wrong! Please check your internet connection or try later.");
					}
				});
	}

	/* Method responsible for creating default accounts for the new user (default, budget)
	 * Each account has ist own accountId where first part of the id is shortcut of the name of the account (def-, bud-,...)
	 * and the rest is user ID
	 * */
	private void createUserAccountsCollectionWithDefaultAccounts(String uID) {
		Map<String, Object> defAccountInfo = new HashMap<>();
		defAccountInfo.put("accountId", "def-"+uID);
		defAccountInfo.put("amount", 0);

		Map<String, Object> budAccountInfo = new HashMap<>();
		budAccountInfo.put("accountId", "bud-"+uID);
		budAccountInfo.put("amount", 0);

		CollectionReference collRef = firestoreDatabase.collection("users").document(uID)
				.collection("accounts");

		collRef.document("default").set(defAccountInfo);
		collRef.document("budget").set(budAccountInfo);
	}

	/* Validation - empty field
	 * */
	private void emptyFieldValidation(String email, String password, String firstname, String lastname, EditText dateOfBirthEditText) {
		if (TextUtils.isEmpty(email)){
			snackbarShow("Email field is empty");
			return;
		}
		if (TextUtils.isEmpty(password)) {
			snackbarShow("Password field is empty");
			return;
		}
		if (TextUtils.isEmpty(firstname)) {
			snackbarShow("Firstname field is empty");
			return;
		}
		if (TextUtils.isEmpty(lastname)) {
			snackbarShow("Lastname field is empty");
			return;
		}
		if ("".contentEquals(dateOfBirthEditText.getText())) {
			snackbarShow("Date Of Birth field is empty");
		}
	}

	private void initComponents() {
		firebaseAuth = FirebaseAuth.getInstance();
		firestoreDatabase = FirebaseFirestore.getInstance();
		firstnameEditText = findViewById(R.id.firstnameEditText);
		lastnameEditText = findViewById(R.id.lastnameEditText);
		dateOfBirthEditText = findViewById(R.id.dateOfBirthEditText);
		passwordEditText = findViewById(R.id.passwordEditText);
		emailEditText = findViewById(R.id.emailEditText);
	}

	private void snackbarShow(String msg) {
		Snackbar snackbar = Snackbar
				.make(findViewById(android.R.id.content), msg, Snackbar.LENGTH_LONG);
		snackbar.show();
	}
	/*

		Date date = new Date();
		System.out.println("formated day: " + myFormat.format(date));
		System.out.println(date.getClass());

		String inputString1 = "23.01.1997";
		String inputString2 = "27.04.1997";

		try {
			Date date1 = myFormat.parse(inputString1);
			Date date2 = myFormat.parse(inputString2);
			long diff = date2.getTime() - date1.getTime();
			System.out.println ("Days: " + TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS));
		} catch (ParseException e) {
			e.printStackTrace();
		}

	  */
}
