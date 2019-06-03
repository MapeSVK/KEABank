package com.example.keabank.activities;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.example.keabank.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

/* List of all possible-to-add accounts */
public class NewAccountsActivity extends AppCompatActivity {
    private static final String TAG = "NewAccountsActivity";
    private ListView listView;
    private FirebaseAuth firebaseAuth;
    private FirebaseUser currentUser;
    private FirebaseFirestore firebaseFirestore;
    private List<String> allPossibleToAddAccounts;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_accounts);
        initComponents();
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Check if user is signed in and if yes then update UI
        currentUser = firebaseAuth.getCurrentUser();
        if (currentUser != null) {
            populateListOfPossibleAccounts();
        } else {
            startActivity(new Intent(getApplicationContext(), MainActivity.class));
        }
    }

    /* Gets all accounts from DB, loop through them, and if some of the accounts is same as accounts specified in arraylist
     * it removes that account. Finally it calls listView populating
     *  */
    public void populateListOfPossibleAccounts() {
        CollectionReference collRef = firebaseFirestore.collection("users").document(currentUser.getUid())
                .collection("accounts");

        collRef.get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                for (int i = 0; i < allPossibleToAddAccounts.size(); i++) {
                                    if (document.getId().equals(allPossibleToAddAccounts.get(i))) {
                                        allPossibleToAddAccounts.remove(i);
                                    }
                                }
                            }
                            // populate the listView
                            populateListViewWithAccounts();
                        } else {
                            Log.w(TAG, "Error while getting possible accounts", task.getException());
                            snackbarShow("Could not get data, please try later!");
                            finish();
                        }
                    }
                });
    }

    /* using adapter populates the listView */
    public void populateListViewWithAccounts() {
        ArrayAdapter arrayAdapter =
                new ArrayAdapter<>(getApplicationContext(),android.R.layout.simple_list_item_1, allPossibleToAddAccounts);
        listView.setAdapter(arrayAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.i(TAG, "User clicked " + allPossibleToAddAccounts.get(position));
                Intent intent = new Intent(getApplicationContext(), AddNewAccountActivity.class);
                intent.putExtra("accountName", allPossibleToAddAccounts.get(position));
                startActivity(intent);
            }
        });
    }

    /* possible accounts to add (if not added already) */
    public void populateListWithAccounts() {
        allPossibleToAddAccounts.add("savings");
        allPossibleToAddAccounts.add("pension");
        allPossibleToAddAccounts.add("business");
    }

    public void initComponents() {
        firebaseAuth = FirebaseAuth.getInstance();
        currentUser = firebaseAuth.getCurrentUser();
        firebaseFirestore = FirebaseFirestore.getInstance();
        listView = findViewById(R.id.listView);
        allPossibleToAddAccounts = new ArrayList<>();
        populateListWithAccounts();
    }

    private void snackbarShow(String msg) {
        Snackbar snackbarBad = Snackbar
                .make(findViewById(android.R.id.content), msg, Snackbar.LENGTH_LONG);
        snackbarBad.show();
    }
}
