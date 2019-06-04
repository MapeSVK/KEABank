package com.example.keabank.activities;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import com.example.keabank.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;

public class RegularTransactionsActivity extends AppCompatActivity {
    private static final String TAG = "RegularTransactionActivity";
    private Spinner autoDaySpinner;
    private int chosenDayFromSpinner;
    private EditText amountEditText;
    private TextView chosenAccountTextView;
    private String chosenAccountId;

    private FirebaseUser currentUser;
    private FirebaseFirestore firestoreDatabase;
    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_regular_transactions);
        initComponents();
    }

    @Override
    protected void onStart() {
        super.onStart();
        currentUser = firebaseAuth.getCurrentUser();
        if (currentUser == null) {
            startActivity(new Intent(getApplicationContext(), MainActivity.class));
        }
    }

    /* ChooseAccountActivity is once again reused. For choosing the account and rending back the result in the form of accountId */
    public void chooseAccount(View view) {
        Intent intent = new Intent(getApplicationContext(), ChooseAccountActivity.class);
        intent.putExtra("sendingClass", TAG);
        startActivityForResult(intent, 3);
    }

    /* Getting accountId from ChooseAccountActivity */
    @SuppressLint("LongLogTag")
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 3) {
            if (resultCode == ChooseAccountActivity.RESULT_OK) {
                chosenAccountId = data.getStringExtra("accountId");
                chosenAccountTextView.setText(chosenAccountId);
                Log.i(TAG, "receiving accountId in " + TAG + ": " + chosenAccountId);
            } else {
                showDialogAfterPaymentIsDone("The system could not get receiving account's ID! Check your internet connection or try again.");
                Log.i(TAG, "Could not get accountId in onActivityResult()");
            }
        }
    }

    /* set new regular transaction. This transaction is regular, on monthly bases and can be sent only to savings and budget accounts */
    public void setNewRegularTransaction(View view) {
        long amount = Long.parseLong(amountEditText.getText().toString().trim());
        chosenDayFromSpinner = Integer.parseInt(autoDaySpinner.getSelectedItem().toString().trim());

        // amount is from user's input, day from the spinner
        Map<String, Object> regularTransactionMap = new HashMap<>();
        regularTransactionMap.put("amount", amount);
        regularTransactionMap.put("day", chosenDayFromSpinner);

        // write the data to the DB
        firestoreDatabase.collection("users").document(currentUser.getUid()).collection("regularTransactions").document(chosenAccountId)
                .set(regularTransactionMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @SuppressLint("LongLogTag")
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "New regular transaction registered!");
                        showDialogAfterPaymentIsDone("New regular transaction registered!");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @SuppressLint("LongLogTag")
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error writing regular transaction to firestore", e);
                    }
                });
    }

    private void initComponents() {
        autoDaySpinner = findViewById(R.id.regularDaySpinner);
        amountEditText = findViewById(R.id.regularAmountEditText);
        chosenAccountTextView = findViewById(R.id.regularChosenReceivingAccount);
        firebaseAuth = FirebaseAuth.getInstance();
        firestoreDatabase = FirebaseFirestore.getInstance();
    }

    /* DIALOG */
    public void showDialogAfterPaymentIsDone(String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(RegularTransactionsActivity.this);
        builder.setMessage(message)
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }
}
