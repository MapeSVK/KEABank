package com.example.keabank.activities;

import android.content.Intent;
import android.support.annotation.NonNull;
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

public class NewAccountsActivity extends AppCompatActivity {
    private static final String TAG = "NewAccountsActivity";
    //private LinearLayout linearLayout;
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

    public void initComponents() {
        firebaseAuth = FirebaseAuth.getInstance();
        currentUser = firebaseAuth.getCurrentUser();
        firebaseFirestore = FirebaseFirestore.getInstance();
       // linearLayout = findViewById(R.id.newAccountsLinearLayout);
        listView = findViewById(R.id.listView);

        allPossibleToAddAccounts = new ArrayList<>();
        populateListWithAccounts();
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

    @Override
    protected void onResume() {
        super.onResume();
        populateListOfPossibleAccounts();
    }

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
                                       // System.out.println(allPossibleToAddAccounts.get(i));
                                        allPossibleToAddAccounts.remove(i);
                                       // System.out.println("accounts: " + allPossibleToAddAccounts);


                                    }
                                }
                            }
                            populateListViewWithAccounts();
                        } else {
                            Log.w(TAG, "Error while adding, try again please.", task.getException());
                        }
                    }
                });
    }

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


    public void populateListWithAccounts() {
        allPossibleToAddAccounts.add("savings");
        allPossibleToAddAccounts.add("pension");
        allPossibleToAddAccounts.add("business");
    }

    /*
    * intentom dostanes nazvy tych ktore tu nie su a obycajne ich iba displaynes
    * budu clickable a potom ak niekto klikne tak sa mu otvoria terms of use specificke pre dany account. klikne na add a to prida do firestoru
    * pod ID current usera novy account ak tieto podmienky splna. ak nie tak to ukaze toast
    *
    * vlastne vytvoris array v home fragmente a pozries ci sa nachadzaju nejake z toho`array v tomto fragmente, ak nie tak ulozis do arrayu ktory
    * bude potom poslany sem do tejto activity
    */


}
