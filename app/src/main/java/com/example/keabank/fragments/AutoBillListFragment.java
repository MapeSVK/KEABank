package com.example.keabank.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.example.keabank.R;
import com.example.keabank.activities.AutoBillDetailsActivity;
import com.example.keabank.activities.ChooseAccountActivity;
import com.example.keabank.activities.MainActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;

public class AutoBillListFragment extends Fragment {
    private ListView autoBillListView;
    private static final String TAG = "AutoBillListFragment";
    private FirebaseAuth firebaseAuth;
    private FirebaseUser currentUser;
    private ArrayList<String> allAutoBills;
    private FirebaseFirestore firebaseFirestore;
    private String chosenBill;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_auto_bill_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initComponents(view);
    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in and if yes then update UI
        currentUser = firebaseAuth.getCurrentUser();
        if (currentUser != null) {
            allAutoBills.clear();
            populateListOfBills(new LoadAutoBillsInterface() {
                @Override
                public void onCallback(ArrayList<String> allAutoBills) {
                    populateListView(allAutoBills);
                    eventAfterClickOnListViewItem();
                }
            });
        } else {
            startActivity(new Intent(getContext(), MainActivity.class));
        }
    }

    public void populateListOfBills(final LoadAutoBillsInterface loadAutoBillsInterface) {
        CollectionReference collRef = firebaseFirestore.collection("users").document(currentUser.getUid())
                .collection("autoBills");

        collRef.get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {

                            for (QueryDocumentSnapshot document : task.getResult()) {
                                allAutoBills.add(document.getId());
                            }

                            loadAutoBillsInterface.onCallback(allAutoBills);
                        } else {
                            Log.w(TAG, "Error while loading the bills, please check your internet connection or try later.", task.getException());
                        }
                    }
                });
    }

    private interface LoadAutoBillsInterface {
        void onCallback(ArrayList<String> allAutoBills);
    }

    public void populateListView(ArrayList<String> allAutoBills) {
        ArrayAdapter arrayAdapter =
                new ArrayAdapter<>(getContext(),android.R.layout.simple_list_item_1, allAutoBills);
        autoBillListView.setAdapter(arrayAdapter);
    }

    public void eventAfterClickOnListViewItem() {
        autoBillListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                chosenBill = allAutoBills.get(position).trim();
                Log.i(TAG, "User clicked " + chosenBill);
                Intent intent = new Intent(getContext(), AutoBillDetailsActivity.class);
                intent.putExtra("billId",chosenBill);
                startActivity(intent);
            }
        });
    }

    public void initComponents(View view) {
        autoBillListView = view.findViewById(R.id.autoBillListView);
        allAutoBills = new ArrayList<>();
        firebaseAuth = FirebaseAuth.getInstance();
        currentUser = firebaseAuth.getCurrentUser();
        firebaseFirestore = FirebaseFirestore.getInstance();
    }
}
