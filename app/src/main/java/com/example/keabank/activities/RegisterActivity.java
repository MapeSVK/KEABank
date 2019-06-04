package com.example.keabank.activities;

import android.annotation.SuppressLint;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

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
	private EditText firstnameEditText, lastnameEditText, dateOfBirthEditText, passwordEditText, repeatPasswordEditText,emailEditText;
	private String email, password,repeatedPassword, firstname, lastname, dateOfBirthString;
	private Timestamp dateOfBirthTimestamp;
	private SimpleDateFormat simpleDateFormat;
	private Date dateOfBirthDate;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_register);
		initComponents();
	}

	/* Triggered by button -> firstly gets user inputs
	 * Then validates inputs
	 * converting birthday to TimeStamp (for firebase usage)
	 * Calls method for creating new user in Firebase Auth
	 * */
	public void register(View view){
		email = emailEditText.getText().toString().trim();
		firstname = firstnameEditText.getText().toString().trim();
		lastname = lastnameEditText.getText().toString().trim();
		password = passwordEditText.getText().toString();
		repeatedPassword = repeatPasswordEditText.getText().toString();
		dateOfBirthString = dateOfBirthEditText.getText().toString().trim();

		// checks if inputs are not empty
		if (emptyFieldValidation(email, password, repeatedPassword, firstname, lastname, dateOfBirthEditText)) {
			if (isDateValid(dateOfBirthString)) { //checks if date in String form can be converted to date in Date form
				dateOfBirthTimestamp = new Timestamp(dateOfBirthDate); // if yes then create new Timestamp with value taken from Date
				if (isSamePassword(password, repeatedPassword)) { // check if the passwords are identical
					createNewUserInAuthentication(email, password);
				}
			}
		}
	}

	/* date validation - date in String form from user input needs to have specified simpleDateFormat
	* setLenient(false) means that the input must be strictly in the same format as simpleDateFormat. If not, it interrupts
	* the process with exception */
	@SuppressLint("SimpleDateFormat")
	public boolean isDateValid(String dateOfBirthString) {
		simpleDateFormat = new SimpleDateFormat("MM/dd/yyyy");
		simpleDateFormat.setLenient(false); // the date in string form needs
		try {
			dateOfBirthDate = simpleDateFormat.parse(dateOfBirthString);
			return true;
		} catch (ParseException e) {
			Log.w(TAG, "date from user input (String) could not be parsed to date (validation)");
			snackbarShow("Wrong date of birth! Must be in this form: \"MM/dd/yyyy\" (e.g. 04/26/1955)");
			return false;
		}
	}


	/* Creates new user in FirebaseAuth system
	 * */
	public void createNewUserInAuthentication(final String email, String password) {
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

	/* validation - check if the value from "password" input is same as value from "repeat password" input */
	public boolean isSamePassword(String password, String repeatPassword) {
		if (password.equals(repeatPassword)) {
			return true;
		} else {
			snackbarShow("Password and repeated password needs to be same");
			return false;
		}
	}

	/* Validation - empty field
	 * */
	private boolean emptyFieldValidation(String email, String password, String repeatPassword, String firstname, String lastname, EditText dateOfBirthEditText) {
		if (TextUtils.isEmpty(email)){
			snackbarShow("Email field is empty");
			return false;
		}
		if (TextUtils.isEmpty(password)) {
			snackbarShow("Password field is empty");
			return false;
		}
		if (TextUtils.isEmpty(repeatPassword)) {
			snackbarShow("Repeat password field is empty");
			return false;
		}
		if (TextUtils.isEmpty(firstname)) {
			snackbarShow("Firstname field is empty");
			return false;
		}
		if (TextUtils.isEmpty(lastname)) {
			snackbarShow("Lastname field is empty");
			return false;
		}
		if ("".contentEquals(dateOfBirthEditText.getText())) {
			snackbarShow("Date Of Birth field is empty");
			return false;
		}
		return true;
	}

	private void initComponents() {
		firebaseAuth = FirebaseAuth.getInstance();
		firestoreDatabase = FirebaseFirestore.getInstance();
		firstnameEditText = findViewById(R.id.firstnameEditText);
		lastnameEditText = findViewById(R.id.lastnameEditText);
		dateOfBirthEditText = findViewById(R.id.dateOfBirthEditText);
		passwordEditText = findViewById(R.id.passwordEditText);
		repeatPasswordEditText = findViewById(R.id.passwordRepeatEditText);
		emailEditText = findViewById(R.id.emailEditText);
	}

	private void snackbarShow(String msg) {
		Snackbar snackbar = Snackbar
				.make(findViewById(android.R.id.content), msg, Snackbar.LENGTH_LONG);
		snackbar.show();
	}
}
