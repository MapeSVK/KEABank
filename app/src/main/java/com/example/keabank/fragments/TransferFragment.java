package com.example.keabank.fragments;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import com.example.keabank.R;
import com.example.keabank.activities.ChooseAccountActivity;
import com.example.keabank.activities.MainActivity;
import com.example.keabank.activities.TransferCheckActivity;
import com.example.keabank.entities.Payment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

/* Fragment responsible for normal transaction between the accounts */
public class TransferFragment extends Fragment {

    private static final String TAG = "TransferFragment";
    private Button choosePayerButton, continueButton;
    private EditText amountEditText, receiversIdEditText;
    private TextView chooseYourAccountInsteadTextView, chosenPayerAccountTextView;
    private FirebaseUser currentUser;
    private FirebaseAuth firebaseAuth;
    private String accountIdOfPayerFromChooseAccountActivity;
    private String accountIdOfReceiverFromChooseAccountActivity;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.i(TAG, "onCreateView called");
        return inflater.inflate(R.layout.fragment_transfer, container, false);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        firebaseAuth = FirebaseAuth.getInstance();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initComponents(view);
    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in and if so, update UI
        currentUser = firebaseAuth.getCurrentUser();
        if (currentUser != null) {
            Log.d(TAG, "User " + currentUser.getEmail() + " is in TransferFragment");
        } else {
            /* validation - if user is not logged in, it can cause problems. For safety reasons user
             * will be sent back to MainActivity */
            startActivity(new Intent(getContext(), MainActivity.class));
        }
    }

    /* Requires result from specified activity. In this case the application uses this to get accountId from the accounts */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1) { // 1 is for payer's account
            if(resultCode == ChooseAccountActivity.RESULT_OK){ // if activity sends back data
                accountIdOfPayerFromChooseAccountActivity =data.getStringExtra("accountId"); // get back accountId
                chosenPayerAccountTextView.setText(accountIdOfPayerFromChooseAccountActivity); // set this accountId to textview
                Log.i(TAG,"accountId of payer in transfer fragment: " + accountIdOfPayerFromChooseAccountActivity);
            }
            else {
                showDialog("The system could not get payer's account number! Check your internet connection or try again.");
            }
        }

        // Responsible for getting back data about user's receiving account - transaction between accounts
        if (requestCode == 2) { // 2 is for receiver's account
            if(resultCode == ChooseAccountActivity.RESULT_OK){ // if activity sends back data
                accountIdOfReceiverFromChooseAccountActivity =data.getStringExtra("accountId"); // get back accountId
                receiversIdEditText.setText(accountIdOfReceiverFromChooseAccountActivity); // set this accountId to textview
                Log.i(TAG,"accountId of receiver in transfer fragment: " + accountIdOfReceiverFromChooseAccountActivity);
            }
            else {
                showDialog("The system could not get receiver's account number! Check your internet connection or try again.");
            }
        }
    }

    /* Firstly gets amount from user's input.
     * Then gets receiver's accountId
     * Creates new Payment (parcelable) and pass it via Intent to TransferCheck activity
    * */
    public void continueButtonPressed() {
        long amount = Long.parseLong(amountEditText.getText().toString().trim());
        String receiverId = receiversIdEditText.getText().toString().trim();
        //create a Payment object
        Payment payment = new Payment(accountIdOfPayerFromChooseAccountActivity, amount,
                receiverId,0);
        Intent intent = new Intent(getContext(), TransferCheckActivity.class);
        intent.putExtra("paymentParcelableObject", payment);
        intent.putExtra("nameOfSendingFragment", TAG);
        startActivity(intent);
    }

    public void initComponents(View view) {
        amountEditText = (EditText) view.findViewById(R.id.transferAmountEditText);
        receiversIdEditText = (EditText) view.findViewById(R.id.transferReceiverIdEditText);
        chosenPayerAccountTextView = (TextView) view.findViewById(R.id.chosenAccountTextView);

        /* CLICKABLE COMPONENTS */
        choosePayerButton = (Button) view.findViewById(R.id.transferChoosePayerButton);
        choosePayerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), ChooseAccountActivity.class);
                // starts activity, expecting result in a form of accountId
                startActivityForResult(intent, 1);
            }
        });

        chooseYourAccountInsteadTextView = (TextView) view.findViewById(R.id.transferChooseYourAccountTextView);
        chooseYourAccountInsteadTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // starts activity, expecting result in a form of accountId
                Intent intent = new Intent(getContext(), ChooseAccountActivity.class);
                startActivityForResult(intent, 2);
            }
        });

        continueButton = (Button) view.findViewById(R.id.transferContinueButton);
        continueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                continueButtonPressed();
            }
        });
    }

    /* DIALOG */
    public void showDialog(String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
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
