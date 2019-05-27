package com.example.keabank.activities;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.keabank.R;
import com.example.keabank.entities.Payment;

import java.util.HashMap;

public class TransferCheckActivity extends AppCompatActivity {

    private Payment paymentFromIntent;
    private TextView payerTextView, amountTextView, receiverTextView;
    private Button transferButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transfer_check);

        paymentFromIntent = getIntent().getParcelableExtra("transferPaymentObject");
        initComponents();
        populateTextViews();
    }

    public void initComponents() {
        payerTextView = findViewById(R.id.transferCheckPayerTextView);
        amountTextView = findViewById(R.id.transferCheckAmountTextView);
        receiverTextView = findViewById(R.id.transferCheckReceiverTextView);
        transferButton = findViewById(R.id.transferCheckButton);
        transferButton.setEnabled(false); //by default. True after verification
    }

    public void populateTextViews() {
        payerTextView.setText(paymentFromIntent.getPayerAccountId());
        amountTextView.setText(String.valueOf(paymentFromIntent.getAmount()));
        receiverTextView.setText(paymentFromIntent.getReceiverAccountId());
    }

    public void transferButtonClicked(View view) {

    }

    /*public HashMap<"">*/
}
