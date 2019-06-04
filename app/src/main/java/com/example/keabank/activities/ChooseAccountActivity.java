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
import android.widget.ListView;

import com.example.keabank.R;
import com.example.keabank.fragments.TransferFragment;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/* Activity responsible for choosing an account (accountId) and used to get back, to activity or fragment which has started this activity,
the data.
 */
public class ChooseAccountActivity extends AppCompatActivity {
    private static final String TAG = "ChooseAccountActivity";
    private ListView accountsListView;
    private FirebaseAuth firebaseAuth;
    private FirebaseUser currentUser;
    private FirebaseFirestore firebaseFirestore;
    private List<String> allAccountsList;
    private Map<String, String> keyNameValueIdMap;
    private String chosenAccountId;
    private List<String> monthlyBasesRegularTransactionsAccounts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_account);
        initComponents();
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Check if user is signed in and if yes then update UI
        currentUser = firebaseAuth.getCurrentUser();
        if (currentUser != null) {
            String sendingClass = getIntent().getStringExtra("sendingClass"); // get the name of sending class

            if (sendingClass == null) { // if there is not extra "sendingClass"
                populateMapAndListOfAccounts();
            } else if (sendingClass.equals("RegularTransactionActivity")){
                populateMapAndListOfAccountsForRegularTransactions();
            }
        } else {
            startActivity(new Intent(getApplicationContext(), MainActivity.class));
        }
    }

    public void populateMapAndListOfAccounts() {
        CollectionReference collRef = firebaseFirestore.collection("users").document(currentUser.getUid())
                .collection("accounts");

        collRef.get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                allAccountsList.add(document.getId()); // list of accounts name which is displayed
                                // create map with accountId for each account name
                                keyNameValueIdMap.put(document.getId(), (String) document.get("accountId"));
                            }
                            populateListViewWithAccounts(); // populates ListView with allAccountList ArrayList
                            eventAfterClickOnListViewItem(); // sends a result with the data back
                        } else {
                            Log.w(TAG, "Error while loading the accounts, please check your internet connection or try later.", task.getException());
                            snackbarShow("Could not get accounts, try later!");
                        }
                    }
                });
    }

    /* Differs from populateMapAndListOfAccounts(), because it shows only savings and budget accounts if the user owns these accounts */
    public void populateMapAndListOfAccountsForRegularTransactions() {
        CollectionReference collRef = firebaseFirestore.collection("users").document(currentUser.getUid())
                .collection("accounts");

        collRef.get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                // only if the account from DB is same as the one from the ArrayList
                                for (String regularTransactionsAccount : monthlyBasesRegularTransactionsAccounts) {
                                    if (document.getId().equals(regularTransactionsAccount)) {
                                        allAccountsList.add(document.getId());
                                        keyNameValueIdMap.put(document.getId(), (String) document.get("accountId"));
                                    }
                                }
                            }
                            populateListViewWithAccounts();// populates ListView with allAccountList ArrayList
                            eventAfterClickOnListViewItem(); // sends a result with the data back
                        } else {
                            Log.w(TAG, "Error while loading the accounts, please check your internet connection or try later.", task.getException());
                            snackbarShow("Could not get accounts, try later!");                        }
                    }
                });
    }

    /* adapter populates ListView with allAccountsList ArrayList */
    public void populateListViewWithAccounts() {
        ArrayAdapter arrayAdapter =
                new ArrayAdapter<>(getApplicationContext(),android.R.layout.simple_list_item_1, allAccountsList);
        accountsListView.setAdapter(arrayAdapter);
    }

    /* sends data back to the sending component */
    public void eventAfterClickOnListViewItem() {
        accountsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // prints out the name of the clicked account
                Log.i(TAG, "User clicked " + allAccountsList.get(position));

                // id which correspondents with the clicked name of the account
                chosenAccountId = keyNameValueIdMap.get(allAccountsList.get(position));
                Log.i(TAG, "accountId is " + chosenAccountId);

                // send an Intent back with chosenAccountId
                Intent returnIntent = new Intent();
                returnIntent.putExtra("accountId",chosenAccountId);
                setResult(ChooseAccountActivity.RESULT_OK,returnIntent);
                finish();
            }
        });
    }

    public void initComponents() {
        accountsListView = findViewById(R.id.chooseAccountsListView);
        allAccountsList = new ArrayList<>();
        keyNameValueIdMap = new HashMap<>();
        firebaseAuth = FirebaseAuth.getInstance();
        currentUser = firebaseAuth.getCurrentUser();
        firebaseFirestore = FirebaseFirestore.getInstance();

        monthlyBasesRegularTransactionsAccounts = new ArrayList<>();
        monthlyBasesRegularTransactionsAccounts.add("savings");
        monthlyBasesRegularTransactionsAccounts.add("budget");
    }

    private void snackbarShow(String msg) {
        Snackbar snackbarBad = Snackbar
                .make(findViewById(android.R.id.content), msg, Snackbar.LENGTH_LONG);
        snackbarBad.show();
    }
}
