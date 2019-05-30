package com.example.keabank.activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.keabank.R;
import com.example.keabank.entities.Payment;
import com.example.keabank.fragments.HomeFragment;
import com.example.keabank.fragments.TransferFragment;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class TransferCheckActivity extends AppCompatActivity {

    private static final String TAG = "TransferCheckActivity";
    private Payment paymentFromIntent;
    private TextView payerTextView, amountTextView, receiverTextView, verificationTextView;
    private EditText verificationResultEditText;
    private Button transferButton;
    private HashMap<String, Integer> verificationMap = new HashMap<>();
    private Object randomKeyForVerification;
    private FirebaseUser currentUser;
    private FirebaseFirestore firestoreDatabase;
    private FirebaseAuth firebaseAuth;
    private boolean isSubtractionSuccessful;

    private String payerAccountIdFromIntent, receiverAccountIdFromIntent;
    private Long amountFromIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transfer_check);

        initComponents();

        populateTextViews();
        populateVerificationMap();
    }

    @Override
    protected void onStart() {
        super.onStart();

        // Check if user is signed in
        currentUser = firebaseAuth.getCurrentUser();
        if (currentUser != null) {
            Log.d(TAG, "User " + currentUser.getEmail() + " is in TransferCheckActivity");
            // every time activity starts it chooses new key
            setVerificationCode();

        } else {
            /* validation - if user is not logged in, it can cause problems. For safety reasons user
             * will be sent back to login activity */
            startActivity(new Intent(getApplicationContext(), MainActivity.class));
        }
    }

    public void initComponents() {
        firebaseAuth = FirebaseAuth.getInstance();
        firestoreDatabase = FirebaseFirestore.getInstance();

        paymentFromIntent = getIntent().getParcelableExtra("transferPaymentObject");
        payerAccountIdFromIntent = paymentFromIntent.getPayerAccountId();
        amountFromIntent = paymentFromIntent.getAmount();
        receiverAccountIdFromIntent = paymentFromIntent.getReceiverAccountId();

        transferButton = findViewById(R.id.transferCheckButton);
        payerTextView = findViewById(R.id.transferCheckPayerTextView);
        amountTextView = findViewById(R.id.transferCheckAmountTextView);
        receiverTextView = findViewById(R.id.transferCheckReceiverTextView);
        verificationTextView = findViewById(R.id.transferCheckVerificationTextView);
        verificationResultEditText = findViewById(R.id.transferCheckVerificationEditText);
    }

    /* values from intent */
    public void populateTextViews() {
        payerTextView.setText(payerAccountIdFromIntent);
        amountTextView.setText(String.valueOf(amountFromIntent) + " DKK");
        receiverTextView.setText(receiverAccountIdFromIntent);
    }



    public void transferButtonClicked(View view) {
        if (checkVerificationCode()) {
            // remove the used pair from the card - new method also with the validation if there is no more then the new card will be send to your home
            // pay
            paymentProcess();
        } else {
            Toast.makeText(getApplicationContext(), "Wrong verification number! Try again",
                    Toast.LENGTH_LONG).show();
        }

    }


    /* PAYMENT PROCESS */

    public void paymentProcess() {
        final CollectionReference collRef = firestoreDatabase.collection("users");

        collRef.get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull final Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {

                            subtractPayer(new FirestoreCallback() {
                                @Override
                                public void onCallback(boolean isSubtractionSuccessful) {
                                    if (isSubtractionSuccessful) {

                                        // what happens in the inner class stays in the inner class.. thats why final is okay
                                        for (QueryDocumentSnapshot user : task.getResult()){
                                            addMoneyToReceiver(user);
                                        }

                                        transferButton.setEnabled(false);
                                        showDialogAfterPaymentIsDone();
                                    }
                                }
                            });

                            Log.i(TAG, "Payment process was successfully finished!");

                        } else {
                            Log.w(TAG, "Error during payment process", task.getException());
                        }
                    }
                });


        // po tom ako methoda bude successful urobis dialog s buttonom(intent spat)
    }

    public void subtractPayer(final FirestoreCallback firestoreCallback) {
        final CollectionReference collRef = firestoreDatabase.collection("users").document(currentUser.getUid())
                .collection("accounts");

        collRef.get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot account : task.getResult()) {
                                Object accountId = account.get("accountId").toString();
                                if (accountId.equals(payerAccountIdFromIntent)) {
                                    long amountFromAccount = Long.parseLong(account.get("amount").toString());
                                    if(amountFromIntent > amountFromAccount){
                                        Toast.makeText(getApplicationContext(), "Your account has not enough money to process this payment",
                                                Toast.LENGTH_LONG).show();
                                    } else {
                                        collRef.document(account.getId()).update(
                                            "amount", amountFromAccount - amountFromIntent
                                        );
                                        Log.i(TAG, "amount was subtracted from payer");

                                        isSubtractionSuccessful = true;
                                        firestoreCallback.onCallback(isSubtractionSuccessful);
                                    }
                                }
                            }
                        } else {
                            Log.w(TAG, "Error getting 'accounts' collection", task.getException());
                        }
                    }
                });
    }

    public void addMoneyToReceiver(QueryDocumentSnapshot user) {
        final CollectionReference collRef = firestoreDatabase.collection("users").document(user.getId()).collection("accounts");

        collRef.get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot account : task.getResult()) {
                                Object accountId = account.get("accountId").toString();
                                if (accountId.equals(receiverAccountIdFromIntent)) {
                                    long amountFromAccount = Long.parseLong(account.get("amount").toString());
                                    collRef.document(account.getId()).update(
                                            "amount", amountFromAccount + amountFromIntent
                                    );
                                    Log.i(TAG, "amount was added to receiver's account");
                                }
                            }
                        }
                    }
                });
    }

    private interface FirestoreCallback {
        void onCallback(boolean isSubtractionSuccessful);
    }


    /* VERIFICATION SYSTEM */
    public void populateVerificationMap() {
        verificationMap.put("a1", 6566);
        verificationMap.put("a2", 9879);
        verificationMap.put("a3", 2891);

        verificationMap.put("b1", 7388);
        verificationMap.put("b2", 8734);
        verificationMap.put("b3", 1933);

        verificationMap.put("c1", 5455);
        verificationMap.put("c2", 7022);
        verificationMap.put("c3", 4799);
    }

    public void setVerificationCode() {
        Object[] verificationMapKeys = verificationMap.keySet().toArray();
        //will put random number to [] based on the length of verificationMapKeys array
        randomKeyForVerification = verificationMapKeys[new Random().nextInt(verificationMapKeys.length)];
        System.out.println("************ Random Value ************ \n" + randomKeyForVerification + " :: " + verificationMap.get(randomKeyForVerification));

        // set key as a textview
        verificationTextView.setText("Please write a number from the card: " + randomKeyForVerification);
    }

    public boolean checkVerificationCode() {
        int expectedValueFromVerification = verificationMap.get(randomKeyForVerification);
        int valueUserPutInEditText = Integer.parseInt(verificationResultEditText.getText().toString().trim());
        if (expectedValueFromVerification == valueUserPutInEditText) {
            return true;
        }
        return false;
    }


    /* DIALOG */
    public void showDialogAfterPaymentIsDone() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Payment successful")
                .setCancelable(true)
                .setPositiveButton("Go home", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        startActivity(new Intent(getApplicationContext(), NavigationDrawerActivity.class));
                        finish();
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }
}
