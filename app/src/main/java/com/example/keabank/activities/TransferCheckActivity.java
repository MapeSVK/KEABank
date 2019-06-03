package com.example.keabank.activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
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
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class TransferCheckActivity extends AppCompatActivity {

    private static final String TAG = "TransferCheckActivity";
    //components bellow
    private Payment paymentFromIntent;
    private TextView payerTextView, amountTextView, receiverTextView, verificationTextView;
    private EditText verificationResultEditText;
    private Button transferButton;
    private HashMap<String, Integer> verificationMap = new HashMap<>();
    private Object randomKeyForVerification;
    private FirebaseUser currentUser;
    private FirebaseFirestore firestoreDatabase;
    private FirebaseAuth firebaseAuth;
    //intent data bellow
    private String payerAccountIdFromIntent, receiverAccountOrBillIdFromIntent;
    private Long amountFromIntent;
    private String nameOfSendingFragmentFromIntent;
    private int autoBillDayFromIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transfer_check);
        initComponents();
        populateTextViews(); // populates textViews with data from intent - payer's and receiver's ID's and amount
        populateVerificationMap(); // populates "verification card" - populates map
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Check if user is signed in. If yes then get random "verification code" from "card" (map)
        currentUser = firebaseAuth.getCurrentUser();
        if (currentUser != null) {
            Log.d(TAG, "User " + currentUser.getEmail() + " is in TransferCheckActivity");
            // every time activity starts it chooses random code which value needs to be verified
            setVerificationCode();
        } else {
            /* validation - if user is not logged in, it can cause problems. For safety reasons user
             * will be sent back to MainActivity */
            startActivity(new Intent(getApplicationContext(), MainActivity.class));
        }
    }


    // ------------------------------------------------------------ //
    /* TRANSACTIONS AND PAYMENT PROCESS */

    private interface FirebaseCallback {
        void onCallback();
    }

    /* main method which triggers all events and it's triggered by clicking on the button
    *  checks if the verification process returns true - correct value was passed from the card
    *  Depending on the value taken from the intent (fragment name which sends data here), appropriate method is used
    * */
    public void transferButtonClicked(View view) {
        if (checkVerificationCode()) {
            if (nameOfSendingFragmentFromIntent.trim().equals("TransferFragment")) { // if TransferFragment ran this method
                paymentProcessTransferFragment();

            } else if (nameOfSendingFragmentFromIntent.trim().equals("BillFragment")) { // if BillFragment ran this method
                paymentProcessBillFragment(new FirebaseCallback() { // user subtraction and then bill payment
                    @Override
                    public void onCallback() {
                            Log.i(TAG, "The user's account successfully subtracted the money, then the bill was successfully paid");
                            if (autoBillDayFromIntent != 0) { // if user also specified auto bill day, it will run method for saving this bill
                                /* save bill - amount, day, and documentId will be same as billId
                                 this bill will be saved to DB and it is possible to see all the bills user saved in Auto-bill list
                                 in the navigation. Deletion of the auto-bill is also possible from this list. */
                                addNewBillToAutoBillList(amountFromIntent, autoBillDayFromIntent, receiverAccountOrBillIdFromIntent);
                            }
                            else {
                                showDialogAfterPaymentIsDone();
                                Log.i(TAG, "The bill was successfully paid, but without saving to auto-bill list");
                            }
                    }
                });
            }
        } else {
            snackbarShow("Wrong verification code! Try again!");
        }
    }

    /* Responsible only for subtracting the amount from user balance on particular account.
     * It firstly gets the account user chose (accountId from intent) and then it will try to subtract the money from the account
     * If the account has not enough money, it warns him
     * If it has enough money, it will trigger onCallback method so we can be sure that subtraction will be finished until the next method
     * (e.g. sending money to receiver or paying bill) is ran
    * */
    public void subtractPayer(final FirebaseCallback subtractionCallback) {
        final CollectionReference collRef = firestoreDatabase.collection("users").document(currentUser.getUid())
                .collection("accounts");

        collRef.get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot account : task.getResult()) { // iterate through accounts
                                Object accountId = account.get("accountId").toString(); // get an ID of the account
                                if (accountId.equals(payerAccountIdFromIntent)) { // check if the ID is same as the one from the intent
                                    long amountFromAccount = Long.parseLong(account.get("amount").toString()); // if yes then get the amount from acc
                                    if(amountFromIntent > amountFromAccount){ // if the amount is less then payment amount, it shows warning
                                        snackbarShow("Your account has not enough money to process this payment");
                                    } else {
                                        // if the account has enough money, then it subtract the amount from the user's account
                                        collRef.document(account.getId()).update(
                                                "amount", amountFromAccount - amountFromIntent
                                        );
                                        Log.i(TAG, "The amount was subtracted from the payer");
                                        subtractionCallback.onCallback();
                                    }
                                } else {
                                    snackbarShow("The system could not find your account. Please try later");
                                }
                            }
                        } else {
                            Log.w(TAG, "Error getting 'accounts' collection", task.getException());
                            snackbarShow("The system could not find account. Please try later");
                        }
                    }
                });
    }

    /* BASIC TRANSFER */
    /* Method processing money adding to receiver after subtraction is done
     *  */
    public void paymentProcessTransferFragment() {
        final CollectionReference collRef = firestoreDatabase.collection("users");

        collRef.get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull final Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {

                            subtractPayer(new FirebaseCallback() {
                                @Override
                                public void onCallback() {
                                    for (QueryDocumentSnapshot user : task.getResult()){
                                        addMoneyToReceiver(user); //iterates through all users and find receiver (his Id) and then add money to his acc
                                    }

                                    transferButton.setEnabled(false);
                                    showDialogAfterPaymentIsDone();
                                    Log.i(TAG, "Payment process was successfully finished!");
                                }
                            });
                        } else {
                            Log.w(TAG, "Couldn't fetch the data from user collection during payment process", task.getException());
                        }
                    }
                });
    }

    /* find the receiver's account (its ID) and then update (add) the amount from intent to his original balance
    * */
    public void addMoneyToReceiver(QueryDocumentSnapshot user) {
        final CollectionReference collRef = firestoreDatabase.collection("users").document(user.getId()).collection("accounts");

        collRef.get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot account : task.getResult()) {
                                Object accountId = account.get("accountId").toString();
                                if (accountId.equals(receiverAccountOrBillIdFromIntent)) { // find the receiver
                                    long amountFromAccount = Long.parseLong(account.get("amount").toString());
                                    // add money
                                    collRef.document(account.getId()).update("amount", amountFromAccount + amountFromIntent);
                                    Log.i(TAG, "The amount was added to receiver's account");
                                } else {
                                    snackbarShow("The receiver's account could not be found! Please try again!");
                                }
                            }
                        } else {
                            snackbarShow("Could not get receiver's accounts! Please try again!");
                        }
                    }
                });
    }

    /* BILL TRANSFER */
    public void paymentProcessBillFragment(final FirebaseCallback billPaymentCallback) {
        final CollectionReference collRef = firestoreDatabase.collection("bills");

        collRef.get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull final Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            subtractPayer(new FirebaseCallback() { // firstly subtract the payer
                                @Override
                                public void onCallback() {
                                    Log.i(TAG, "paymentProcessBillFragment() begins");
                                        if (task.getResult() != null) {
                                            for (QueryDocumentSnapshot bill : task.getResult()) {
                                                // if bill from user's input equals the bill id from DB
                                                if (receiverAccountOrBillIdFromIntent.equals(bill.getId())) {
                                                    Long billAmount = Long.parseLong(bill.get("amount").toString());
                                                    // if amount from intent equals the amount of bill in DB
                                                    if (billAmount.equals(amountFromIntent)) {
                                                        // updates isPaid from false to true because bill is now paid and it is good idea
                                                        // to track if the bill was paid by the payer
                                                        collRef.document(bill.getId()).update("isPaid", true);
                                                        transferButton.setEnabled(false);

                                                        billPaymentCallback.onCallback(); // ensure that this method is completed before saving the bill
                                                        // to the auto-bill list
                                                    } else {
                                                        Log.i(TAG, "Amount of the bill is: " + billAmount + " not " + amountFromIntent);
                                                        snackbarShow("Wrong amount! " + billAmount + "DKK needs to be paid!");
                                                    }
                                                } else {
                                                    Log.i(TAG, "Bill with this billId could not be found");
                                                    snackbarShow("Wrong bill ID!");
                                                }
                                            }
                                        } else {
                                            Log.i(TAG, "Couldn't find bills in DB");
                                            snackbarShow("Could not find bill in the system. Please try later!");
                                        }
                                }
                            });
                        }
                    }
                });
    }


    /* Add new bill to the auto-bill list. This list can be found after navigation item "auto-bill list" is clicked. After click on
    * different auto-bill, this auto-bill can be deleted too.
    *
    * New bill will be registered with specified day, amount and payer's account so the system know where to subtract from
    * */
    public void addNewBillToAutoBillList(final long amount, final int day, final String autoBillId) {
        Map<String, Object> autoBillMap = new HashMap<>();
        autoBillMap.put("amount", amount);
        autoBillMap.put("day", day);
        autoBillMap.put("accountId", payerAccountIdFromIntent);

        firestoreDatabase.collection("users").document(currentUser.getUid()).collection("autoBills").document(autoBillId)
                .set(autoBillMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "New bill was registered. BillId: "+autoBillId +" Day: " + day + " Amount: " + amount + " from acc: " + payerAccountIdFromIntent);
                        showDialogAfterPaymentIsDone();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error writing auto bill to firestore", e);
                        snackbarShow("Could not save your auto-bill to the system. Please check your internet connection or try later!");
                    }
                });
    }


    // ------------------------------------------------------------ //

    /* VERIFICATION SYSTEM */
    /* populate map with key displayed in activity for the user and value which he would be able to find on "the card" */
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

    /* generates random number which is then placed into [] and therefore code can be written and verified */
    public void setVerificationCode() {
        Object[] verificationMapKeys = verificationMap.keySet().toArray();
        //will put random number to [] based on the length of verificationMapKeys array
        randomKeyForVerification = verificationMapKeys[new Random().nextInt(verificationMapKeys.length)];

        // for testing purposes and faster process this value is also printed out in console
        Log.i(TAG, "**** RANDOM CODE FOR: " + randomKeyForVerification + "is: " + verificationMap.get(randomKeyForVerification));

        // set the random generated key as a text in the textView so user is able to find this key on his card.
        verificationTextView.setText("Find code in your card: " + randomKeyForVerification);
    }

    /* checks if an input from the user is same as the value generated by the application */
    public boolean checkVerificationCode() {
        int expectedValueFromVerification = verificationMap.get(randomKeyForVerification); //generated value
        int valueUserPutInEditText = Integer.parseInt(verificationResultEditText.getText().toString().trim()); //input from the user
        if (expectedValueFromVerification == valueUserPutInEditText) {
            return true;
        }
        return false;
    }

    // ------------------------------------------------------------ //

    /* values from intent will be written in activity textViews*/
    public void populateTextViews() {
        payerTextView.setText(payerAccountIdFromIntent);
        amountTextView.setText(String.valueOf(amountFromIntent) + " DKK");
        receiverTextView.setText(receiverAccountOrBillIdFromIntent);
    }

    /* initialisation of the components */
    public void initComponents() {
        firebaseAuth = FirebaseAuth.getInstance();
        firestoreDatabase = FirebaseFirestore.getInstance();

        // getting parcelable payment via intent
        paymentFromIntent = getIntent().getParcelableExtra("paymentParcelableObject");
        payerAccountIdFromIntent = paymentFromIntent.getPayerAccountId();
        amountFromIntent = paymentFromIntent.getAmount();
        receiverAccountOrBillIdFromIntent = paymentFromIntent.getReceiverAccountOrBillId();
        autoBillDayFromIntent = paymentFromIntent.getAutoBillDay();
        // name of sending fragment is also important to find out if this activity should process transaction or bill transaction
        nameOfSendingFragmentFromIntent = getIntent().getStringExtra("nameOfSendingFragment");

        transferButton = findViewById(R.id.transferCheckButton);
        payerTextView = findViewById(R.id.transferCheckPayerTextView);
        amountTextView = findViewById(R.id.transferCheckAmountTextView);
        receiverTextView = findViewById(R.id.transferCheckReceiverTextView);
        verificationTextView = findViewById(R.id.transferCheckVerificationTextView);
        verificationResultEditText = findViewById(R.id.transferCheckVerificationEditText);
    }

    /* dialog after the payment is done */
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

    private void snackbarShow(String msg) {
        Snackbar snackbarBad = Snackbar
                .make(findViewById(android.R.id.content), msg, Snackbar.LENGTH_LONG);
        snackbarBad.show();
    }
}
