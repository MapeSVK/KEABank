package com.example.keabank.activities;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.keabank.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.Map;

/* final step to add new account to DB */
public class AddNewAccountActivity extends AppCompatActivity {
    private static final String TAG = "AddNewAccountActivity";
    private FirebaseAuth firebaseAuth;
    private FirebaseUser currentUser;
    private FirebaseFirestore firebaseFirestore;
    private String accountNameFromIntent;
    private TextView accountNameTextView, termsTextView;
    private Button addNewAccountButton;
    // get the shortcut of the accountNameFromIntent to firstly merge it with uID and then save it to DB as an accountId
    private String accountNameFromIntentShortcut;
    private int requiredAmountForBusinessAccount = 70000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_new_account);
        initComponents();
    }

    public void initComponents() {
        firebaseAuth = FirebaseAuth.getInstance();
        currentUser = firebaseAuth.getCurrentUser();
        firebaseFirestore = FirebaseFirestore.getInstance();
        //get extra - accountName (String) from intent from NewAccountsActivity
        accountNameFromIntent = getIntent().getExtras().getString("accountName");

        accountNameTextView = findViewById(R.id.addNewAccountNameTextView);
        termsTextView = findViewById(R.id.addNewAccountTermsTextView);
        addNewAccountButton = findViewById(R.id.addNewAccountToDBButton);
        addNewAccountButton.setEnabled(false); //button is disabled by default because some accounts may not meet requirements
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Check if user is signed in and if yes then update UI
        currentUser = firebaseAuth.getCurrentUser();
        if (currentUser != null) {
            accountNameTextView.setText(accountNameFromIntent);
            addTextToTermsTextView();
        } else {
            startActivity(new Intent(AddNewAccountActivity.this, MainActivity.class));
        }
    }

    /* After button click - save new account with accountId (merged name shortcut of the account and uID)
    * Set default value to 0
    * */
    public void addDataToDatabase(View view) {
        CollectionReference collRef = firebaseFirestore.collection("users").document(currentUser.getUid())
                .collection("accounts");

        Map<String, Object> newAccount = new HashMap<>();
        newAccount.put("accountId", accountNameFromIntentShortcut+"-"+currentUser.getUid());
        newAccount.put("amount", 0);

        /* add new account to DB */
        collRef.document(accountNameFromIntent)
                .set(newAccount)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "Document successfully written!");
                        snackbarShow("Account was successfully added!");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error writing document", e);
                        snackbarShow("Account could not be added! Please check you internet connection or try later");
                    }
                });
        finish();
    }

    /* method firstly checks what was the accountName put in Intent
     * then creates shortcut of the account name
     * after that setting button to be clickable
     *  */
    public void addTextToTermsTextView() {
        switch (accountNameFromIntent) {
            case "savings":
                accountNameFromIntentShortcut = "sav";
                termsTextView.setText(savingsText());
                addNewAccountButton.setEnabled(true);
                break;
            case "pension":
                accountNameFromIntentShortcut = "pen";
                termsTextView.setText(pensionText());
                addNewAccountButton.setEnabled(true);
                break;
            case "business":
                accountNameFromIntentShortcut = "bus";
                termsTextView.setText(businessText());
                validateBusinessAccountAddition(); // validates business account before able to add
                break;
        }
    }


    /* validate if the user is able to add business account - depends on the amount of money he has on his default account
    *
    */
    public void validateBusinessAccountAddition() {
        DocumentReference docRef = firebaseFirestore.collection("users").document(currentUser.getUid())
                .collection("accounts").document("default");

        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    long defaultAccountAmount = document.getLong("amount"); //amount on default account
                    if (defaultAccountAmount >= requiredAmountForBusinessAccount) { // if the amount is bigger than specified amount
                        // activate the button
                        addNewAccountButton.setEnabled(true);
                    } else {
                        snackbarShow("You need to have at least " + requiredAmountForBusinessAccount +"DKK on your default account");
                    }
                } else {
                    Log.d(TAG, "Could not find user's default account ", task.getException());
                    snackbarShow("Could not get information about your accounts, please try later");
                    startActivity(new Intent(AddNewAccountActivity.this, NewAccountsActivity.class));
                }
            }
        });
    }

    private void snackbarShow(String msg) {
        Snackbar snackbarBad = Snackbar
                .make(findViewById(android.R.id.content), msg, Snackbar.LENGTH_LONG);
        snackbarBad.show();
    }


    /* Dummy text to show in text view */
    public String savingsText() {
        return "Savings account and Organizations with whom\n" +
                "Diners Club International and/or VISA International and /\n" +
                "or MasterCard and / or any of their franchises have signed\n" +
                "a contract and who have agreed to sell the currencies of\n" +
                "their loyalty programme at a prefixed rate to Diners Club\n" +
                "International and/or VISA International and / or Mastercard\n" +
                "and / or any of their respective franchises";
    }

    public String pensionText() {
        return "Pension account asmdaksdmkasmdasd" +
                "asdasdasda dsadasdasdasdasdajks dksaam djkasdkasmdkasmd" +
                "mkoasmdkamdkasmdkasd" +
                "\n\n AND CAN BE WITHDRAWED at age 77";
    }

    public String businessText() {
        return "Business account asmdaksdmkasmdasd" +
                "asdasdasda dsadasdasdasdasdajks dksaam djkasdkasmdkasmd" +
                "mkoasmdkamdkasmdkasd" +
                "\n\n AND CAN BE ADDED only if the user has at least 14 000DKK on his default account";
    }
}
