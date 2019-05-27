package com.example.keabank.activities;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.format.DateUtils;
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
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.sql.Time;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class RegisterActivity extends AppCompatActivity {
	/*
		Map<String, Object> docData = new HashMap<>();
		docData.put("stringExample", "Hello world!");
		docData.put("booleanExample", true);
		docData.put("numberExample", 3.14159265);
		docData.put("dateExample", new Timestamp(new Date()));
		docData.put("listExample", Arrays.asList(1, 2, 3));
		docData.put("nullExample", null);
		*/

	private static final String TAG = "ResetPasswordActivity";
	private FirebaseAuth firebaseAuth;
	FirebaseFirestore firestoreDatabase;
	private Button createAccountButton;
	private EditText firstnameEditText, lastnameEditText, dateOfBirthEditText, passwordEditText, pinEditText, emailEditText;
	private String email, password, firstname, lastname, dateOfBirthString;
	private int pin;
	private Timestamp dateOfBirthTimestamp;
	private SimpleDateFormat simpleDateFormat;
	//FirebaseUser newUser;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_register);

		initComponents();
	}

	private void initComponents() {
		firebaseAuth = FirebaseAuth.getInstance();
		firestoreDatabase = FirebaseFirestore.getInstance();
		firstnameEditText = findViewById(R.id.firstnameEditText);
		lastnameEditText = findViewById(R.id.lastnameEditText);
		dateOfBirthEditText = findViewById(R.id.dateOfBirthEditText);
		passwordEditText = findViewById(R.id.passwordEditText);
		pinEditText = findViewById(R.id.pinEditText);
		emailEditText = findViewById(R.id.emailEditText);
	}


	public void register(View view){
		/* getting values */
		email = emailEditText.getText().toString().trim();
		password = passwordEditText.getText().toString().trim();
		firstname = firstnameEditText.getText().toString().trim();
		lastname = lastnameEditText.getText().toString().trim();
		pin = Integer.parseInt(pinEditText.getText().toString().trim());

		simpleDateFormat = new SimpleDateFormat("MM/dd/yyyy");
		dateOfBirthString = dateOfBirthEditText.getText().toString().trim();
		try {
			Date dateOfBirthDate = simpleDateFormat.parse(dateOfBirthString);
			dateOfBirthTimestamp = new Timestamp(dateOfBirthDate);
		} catch (ParseException e) {
			e.printStackTrace();
			// validation - date must be in format: month/day/year
		}

		/* validations */
		emptyFieldValidation(email, password, firstname, lastname, dateOfBirthEditText, pinEditText);
		// validation for pin, that it needs to be a number

		/* firebase authentication - email/password user creating */
		createNewUserInAuthentication(email, password);
	}

	private void createNewUserInAuthentication(final String email, String password) {
		firebaseAuth.createUserWithEmailAndPassword(email, password)
				.addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
					@Override
					public void onComplete(@NonNull Task<AuthResult> task) {
						try {
							//check if successful
							if (task.isSuccessful()) {
								//User is successfully registered and logged in
								Toast.makeText(RegisterActivity.this, "registration successful, you can now log in",
										Toast.LENGTH_SHORT).show();
								//finish();
								//startActivity(new Intent(getApplicationContext(), ProfileActivity.class));


								// getting new user object
								// users ID then can be used to save document to firestore in the method responsible for creating
								// new document in collection users
								FirebaseUser newUser = firebaseAuth.getCurrentUser(); // getting new user object

								/* firebase firestore database - creating new document with same uID as authentication has */
								createNewUserInDatabase(newUser.getUid(), firstname, lastname, dateOfBirthTimestamp, pin, email);


							}else {
								Toast.makeText(RegisterActivity.this, "Couldn't register, try again",
										Toast.LENGTH_SHORT).show();
							}
						}catch (Exception e){
							e.printStackTrace();
							Log.d(TAG, "Couldn't register");
						}
					}
				});
	}

	private void createNewUserInDatabase(final String uID, String firstname, String lastname, Timestamp dateOfBirth, int pin, String email) {
		Map<String, Object> mainUserInfo = new HashMap<>();
		mainUserInfo.put("first_name", firstname);
		mainUserInfo.put("last_name", lastname);
		mainUserInfo.put("date_of_birth", dateOfBirth);
		//mainUserInfo.put("pin", pin); - only shared pref. - naco DB?
		mainUserInfo.put("email", email);


		firestoreDatabase.collection("users").document(uID)
				.set(mainUserInfo)
				.addOnSuccessListener(new OnSuccessListener<Void>() {
					@Override
					public void onSuccess(Void aVoid) {
						createUserAccountsCollectionWithDefaultAccounts(uID);
						Log.d(TAG, "new user document successfully written to firestore!");
						finish();
						//startActivity(new Intent(getApplicationContext(), HomeActivity.class));
					}
				})
				.addOnFailureListener(new OnFailureListener() {
					@Override
					public void onFailure(@NonNull Exception e) {
						Log.w(TAG, "Error writing document to firestore", e);
					}
				});
	}

	private void createUserAccountsCollectionWithDefaultAccounts(String uID) {
		Map<String, Object> defAccountInfo = new HashMap<>();
		defAccountInfo.put("accountId", "def-"+uID);
		defAccountInfo.put("amount", 0);

		Map<String, Object> budAccountInfo = new HashMap<>();
		budAccountInfo.put("accountId", "bud-"+uID);
		budAccountInfo.put("amount", 0);

		/*Map<String, Object> budgetAccount = new HashMap<>();
		budgetAccount.put("amount", 0);*/

		CollectionReference collRef = firestoreDatabase.collection("users").document(uID)
				.collection("accounts");

		collRef.document("default").set(defAccountInfo);
		collRef.document("budget").set(budAccountInfo);
	}

	/* Validations section */
	// urob to tak ze vsetko na jednom mieste

	private void emptyFieldValidation(String email, String password, String firstname, String lastname, EditText dateOfBirthEditText, EditText pinEditText) {
		if (TextUtils.isEmpty(email)){
			Toast.makeText(this, "Email field is empty", Toast.LENGTH_SHORT).show();
			return;
		}
		if (TextUtils.isEmpty(password)) {
			Toast.makeText(this, "Password field is empty", Toast.LENGTH_SHORT).show();
			return;
		}
		if (TextUtils.isEmpty(firstname)) {
			Toast.makeText(this, "Firstname field is empty", Toast.LENGTH_SHORT).show();
			return;
		}
		if (TextUtils.isEmpty(lastname)) {
			Toast.makeText(this, "Lastname field is empty", Toast.LENGTH_SHORT).show();
			return;
		}
		if ("".contentEquals(dateOfBirthEditText.getText())) {
			Toast.makeText(this, "Date Of Birth field is empty", Toast.LENGTH_SHORT).show();
			return;
		}
		if ("".contentEquals(pinEditText.getText())) {
			Toast.makeText(this, "Pin field is empty", Toast.LENGTH_SHORT).show();
			return;
		}
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
