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
import android.widget.Toast;

import com.example.keabank.R;
import com.example.keabank.activities.ChooseAccountActivity;
import com.example.keabank.activities.MainActivity;
import com.example.keabank.activities.TransferCheckActivity;
import com.example.keabank.entities.Payment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import org.greenrobot.eventbus.EventBus;

public class TransferFragment extends Fragment {

    private static final String TAG = "TransferFragment";
    private Button choosePayerButton, continueButton;
    private EditText amountEditText, dateEditText, receiversIdEditText;
    private TextView chooseYourAccountInsteadTextView, chosenPayerAccountTextView;
    private FirebaseUser currentUser;
    private FirebaseFirestore firestoreDatabase;
    private FirebaseAuth firebaseAuth;
    private String accountIdOfPayerFromChooseAccountActivity;
    private String accountIdOfReceiverFromChooseAccountActivity;

    // 2
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.i(TAG, "onCreateView called");
        return inflater.inflate(R.layout.fragment_transfer, container, false);
    }

    // 1
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        Log.i(TAG, "onCreate called");

        super.onCreate(savedInstanceState);

//        EventBus.getDefault().register(this);

        firebaseAuth = FirebaseAuth.getInstance();
        firestoreDatabase = FirebaseFirestore.getInstance();
    }

    // 3
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        Log.i(TAG, "onViewCreated called");

        super.onViewCreated(view, savedInstanceState);

        initComponents(view);
    }

    // 4
    @Override
    public void onStart() {
        Log.i(TAG, "onStart called");

        super.onStart();
        // Check if user is signed in
        currentUser = firebaseAuth.getCurrentUser();
        if (currentUser != null) {
            Log.d(TAG, "User " + currentUser.getEmail() + " is in TransferFragment");
            //loadAccountsAsTextFieldsInsideLinearView();

            Log.i(TAG, "payer: " + accountIdOfPayerFromChooseAccountActivity);
            Log.i(TAG, "receiver: " + accountIdOfReceiverFromChooseAccountActivity);

        } else {
            /* validation - if user is not logged in, it can cause problems. For safety reasons user
             * will be sent back to login activity */
            startActivity(new Intent(getContext(), MainActivity.class));
        }
    }

    public void initComponents(View view) {
        amountEditText = (EditText) view.findViewById(R.id.transferAmountEditText);
        //dateEditText = (EditText) view.findViewById(R.id.transferDateEditText);
        receiversIdEditText = (EditText) view.findViewById(R.id.transferReceiverIdEditText);
        chosenPayerAccountTextView = (TextView) view.findViewById(R.id.chosenAccountTextView);

        /* CLICKABLE COMPONENTS */

        choosePayerButton = (Button) view.findViewById(R.id.transferChoosePayerButton);
        choosePayerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), ChooseAccountActivity.class);
                startActivityForResult(intent, 1);
            }
        });

        chooseYourAccountInsteadTextView = (TextView) view.findViewById(R.id.transferChooseYourAccountTextView);
        chooseYourAccountInsteadTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == 1) {
            if(resultCode == ChooseAccountActivity.RESULT_OK){
                accountIdOfPayerFromChooseAccountActivity =data.getStringExtra("accountId");
                chosenPayerAccountTextView.setText(accountIdOfPayerFromChooseAccountActivity);
                Log.i(TAG,"accountId of payer in transfer fragment: " + accountIdOfPayerFromChooseAccountActivity);
            }
            else {
                showDialogAfterPaymentIsDone("The system could not get payer's account number! Check your internet connection or try again.");
            }
        }

        if (requestCode == 2) {
            if(resultCode == ChooseAccountActivity.RESULT_OK){
                accountIdOfReceiverFromChooseAccountActivity =data.getStringExtra("accountId");
                receiversIdEditText.setText(accountIdOfReceiverFromChooseAccountActivity);
                Log.i(TAG,"accountId of receiver in transfer fragment: " + accountIdOfReceiverFromChooseAccountActivity);
            }
            else {
                showDialogAfterPaymentIsDone("The system could not get receiver's account number! Check your internet connection or try again.");

            }
        }
    }

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

    /* DIALOG */
    public void showDialogAfterPaymentIsDone(String message) {
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
