package com.example.keabank.activities;

import android.content.Intent;
import android.support.annotation.NonNull;
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

public class ChooseAccountActivity extends AppCompatActivity {
    private static final String TAG = "ChooseAccountActivity";
    private ListView accountsListView;
    private FirebaseAuth firebaseAuth;
    private FirebaseUser currentUser;
    private FirebaseFirestore firebaseFirestore;
    private List<String> allAccountsList;
    private Map<String, String> keyNameValueIdMap;
    private String chosenAccountId;

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
            populateMapAndListOfAccounts();
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
                                allAccountsList.add(document.getId());
                                keyNameValueIdMap.put(document.getId(), (String) document.get("accountId"));
                            }
                            populateListViewWithAccounts();
                            eventAfterClickOnListViewItem();
                        } else {
                            Log.w(TAG, "Error while loading the account, please check your internet connection or try later.", task.getException());
                        }
                    }
                });
    }

    public void populateListViewWithAccounts() {
        ArrayAdapter arrayAdapter =
                new ArrayAdapter<>(getApplicationContext(),android.R.layout.simple_list_item_1, allAccountsList);
        accountsListView.setAdapter(arrayAdapter);
    }

    public void eventAfterClickOnListViewItem() {
        accountsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.i(TAG, "User clicked " + allAccountsList.get(position));


                chosenAccountId = keyNameValueIdMap.get(allAccountsList.get(position));
                Log.i(TAG, "accountId is " + chosenAccountId);


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
    }
}
