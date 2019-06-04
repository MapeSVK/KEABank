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
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;
import com.example.keabank.R;
import com.example.keabank.activities.ChooseAccountActivity;
import com.example.keabank.activities.MainActivity;
import com.example.keabank.activities.TransferCheckActivity;
import com.example.keabank.entities.Payment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

/* Fragment responsible for creating Payment parcelable object with a values from the input fields */
public class BillFragment extends Fragment {
    private static final String TAG = "BillFragment";
    private EditText amountEditText, billIdEditText;
    private Spinner autoDaySpinner;
    private TextView chosenPayerAccountTextView;
    private Button continueButton, choosePayerButton;
    private ToggleButton autoPaymentToggleButton;
    private int chosenNumberFromSpinner;
    private String accountIdOfPayerFromChooseAccountActivity;
    private FirebaseAuth firebaseAuth;
    private FirebaseUser currentUser;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_bill, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initComponents(view);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        firebaseAuth = FirebaseAuth.getInstance();
    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in
        currentUser = firebaseAuth.getCurrentUser();
        if (currentUser != null) {
            Log.d(TAG, "User " + currentUser.getEmail() + " is in BillFragment");
        } else {
            /* validation - if user is not logged in, it can cause problems. For safety reasons user
             * will be sent back to MainActivity */
            startActivity(new Intent(getContext(), MainActivity.class));
        }
    }

    /* expects data from ChooseAccountActivity  */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1) {
            if (resultCode == ChooseAccountActivity.RESULT_OK) {
                accountIdOfPayerFromChooseAccountActivity = data.getStringExtra("accountId"); // get chosen accountId
                chosenPayerAccountTextView.setText(accountIdOfPayerFromChooseAccountActivity); //set this accountId to textView
                Log.i(TAG, "accountId of payer in Bill fragment: " + accountIdOfPayerFromChooseAccountActivity);
            } else {
                showDialog("The system could not get payer's account number! Check your internet connection or try again.");
            }
        }
    }

    /* Method runs after continue button is pressed
     * if the spinner whose data are days is enabled then take a number (auto-bill day) from this spinner
     * otherwise write 0
     *
     * take amount, id of payer, billId and create new Payment Object and send it as an intent extra.
     * send name of this fragment too */
    public void continueButtonPressed() {
        if (autoDaySpinner.isEnabled()) {
            chosenNumberFromSpinner = Integer.parseInt(autoDaySpinner.getSelectedItem().toString().trim());
        } else {
            chosenNumberFromSpinner = 0;
        }

        long amount = Long.parseLong(amountEditText.getText().toString().trim());
        String billId = billIdEditText.getText().toString().trim();
        //create a Payment object
        Payment payment = new Payment(accountIdOfPayerFromChooseAccountActivity, amount,
               billId, chosenNumberFromSpinner);
        Intent intent = new Intent(getContext(), TransferCheckActivity.class);
        intent.putExtra("paymentParcelableObject", payment);
        intent.putExtra("nameOfSendingFragment", TAG);
        startActivity(intent);
    }

    public void initComponents(View view) {
        amountEditText = (EditText) view.findViewById(R.id.billFragmentAmountEditText);
        billIdEditText = (EditText) view.findViewById(R.id.billFragmentBillIdEditText);
        chosenPayerAccountTextView = (TextView) view.findViewById(R.id.billFragmentChosenAccountTextView);

        autoDaySpinner = view.findViewById(R.id.billFragmentAutoBillDaySpinner);
        autoDaySpinner.setEnabled(false); //will be set to true after toggle button is clicked

        /* CLICKABLE COMPONENTS */
        autoPaymentToggleButton = view.findViewById(R.id.billFragmentAutoDayToggleButton);
        autoPaymentToggleButton.setText("Auto-pay off");
        autoPaymentToggleButton.setTextOn("Auto-pay on");
        autoPaymentToggleButton.setTextOff("Auto-pay off");
        autoPaymentToggleButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked) //is on
                {
                    autoDaySpinner.setEnabled(true);
                }
                else {
                    autoDaySpinner.setEnabled(false);
                }

            }
        });

        choosePayerButton = (Button) view.findViewById(R.id.billFragmentChooseAccountButton);
        choosePayerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), ChooseAccountActivity.class);
                startActivityForResult(intent, 1);
            }
        });

        continueButton = (Button) view.findViewById(R.id.billFragmentContinueButton);
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
