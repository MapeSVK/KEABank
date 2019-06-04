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
import android.widget.TextView;
import android.widget.Toast;

import com.example.keabank.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class AutoBillDetailsActivity extends AppCompatActivity {

    private static final String TAG = "AutoBillDetailsActivity";
    private String billIdFromIntent;
    private TextView amountTextView, dayTextView, billIdTextView;
    private Button deleteButton;
    private FirebaseAuth firebaseAuth;
    private FirebaseUser currentUser;
    private FirebaseFirestore firebaseFirestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auto_bill_details);
        initComponents();
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Check if user is signed in and if yes then update UI
        currentUser = firebaseAuth.getCurrentUser();
        if (currentUser != null) {
            loadAutoBill();
        } else {
            startActivity(new Intent(getApplicationContext(), MainActivity.class));
        }
    }

    /* load details about the bill (specified by billId from intent) */
    public void loadAutoBill() {
        DocumentReference docRef = firebaseFirestore.collection("users").document(currentUser.getUid())
                .collection("autoBills").document(billIdFromIntent);

        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document != null) {
                        if (document.exists()) {
                            // populate TextViews and activate delete button
                            dayTextView.setText(document.get("day").toString().trim());
                            amountTextView.setText(document.get("amount").toString().trim());
                            deleteButton.setEnabled(true);
                        } else {
                            Log.d(TAG, "No such bill");
                        }
                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                    snackbarShow("Something went wrong while loading details about the bil. Try again later.");
                }
            }
        });
    }

    /* deletes concrete bil
     */
    public void deleteBill(View view) {
        DocumentReference docRef = firebaseFirestore.collection("users").document(currentUser.getUid())
                .collection("autoBills").document(billIdFromIntent);

        docRef.delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        showDialogAfterDeletionIsDone();
                        Log.d(TAG, "Auto-bill with id: " + billIdFromIntent + " successfully deleted!");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        snackbarShow("Auto-bill could not be deleted. Please check your" +
                                        "internet connection or try it later.");
                        Log.w(TAG, "Error deleting auto-bill with id: " + billIdFromIntent, e);
                    }
                });
    }

    public void initComponents() {
        firebaseAuth = FirebaseAuth.getInstance();
        currentUser = firebaseAuth.getCurrentUser();
        firebaseFirestore = FirebaseFirestore.getInstance();

        billIdFromIntent = getIntent().getStringExtra("billId");
        billIdTextView = findViewById(R.id.autoBillDetailsBillIdTextView);
        billIdTextView.setText(billIdFromIntent);
        amountTextView = findViewById(R.id.AutoBillDetailsAmountTextView);
        dayTextView = findViewById(R.id.autoBillDetailsDayTextView);
        deleteButton = findViewById(R.id.autoBillDetailsDeleteButton);
        deleteButton.setEnabled(false);
    }

    private void snackbarShow(String msg) {
        Snackbar snackbarBad = Snackbar
                .make(findViewById(android.R.id.content), msg, Snackbar.LENGTH_LONG);
        snackbarBad.show();
    }

    /* DIALOG */
    public void showDialogAfterDeletionIsDone() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Auto-bill with id: " + billIdFromIntent + " successfully deleted!")
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
