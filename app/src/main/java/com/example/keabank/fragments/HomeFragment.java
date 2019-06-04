package com.example.keabank.fragments;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.keabank.R;
import com.example.keabank.activities.MainActivity;
import com.example.keabank.activities.NavigationDrawerActivity;
import com.example.keabank.activities.NewAccountsActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import java.text.CollationElementIterator;
import java.util.ArrayList;
import java.util.List;

/* This Fragment shows all user's accounts as well as his total balance from all the accounts
 * User can also add new account by clicking "ADD NEW"
 * */
public class HomeFragment extends Fragment {
	private static final String TAG = "HomeFragment";
	private Button addNewAccountButton;
	private FirebaseUser currentUser;
	private FirebaseFirestore firestoreDatabase;
	private FirebaseAuth firebaseAuth;
	private LinearLayout linearLayout;
	private int totalBalance;

	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_home, container, false);
	}

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		firebaseAuth = FirebaseAuth.getInstance();
		firestoreDatabase = FirebaseFirestore.getInstance();
	}

	/* Initialisation of the components as this is the method where "view" is
	 * Button creates new intent to NewAccountsActivity
	 * */
	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		linearLayout = (LinearLayout) getView().findViewById(R.id.homeLinearLayout);
		addNewAccountButton = (Button) view.findViewById(R.id.addNewAccountButton);
		addNewAccountButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				startActivity(new Intent(getContext(), NewAccountsActivity.class));
			}
		});
	}

	/* Checks if the user is signed in. If yes it executes new thread with loading while fetching user's data from DB
	 * */
	@Override
	public void onStart() {
		super.onStart();
		currentUser = firebaseAuth.getCurrentUser();
		if (currentUser != null) {
			Log.d(TAG, "User " + currentUser.getEmail() + " is in HomeFragment");
			new Load().execute();
		} else {
			/* validation - if user is not logged in, it can cause problems. For safety reasons user
			* will be sent back to MainActivity */
			startActivity(new Intent(getContext(), MainActivity.class));
		}
	}

	/* getting all accounts of the signed-in user
	* creates first row with total balance on the account
	* for each account creates another row with the name and the balance
	* */
	public void loadAccountsAsTextFieldsInsideLinearView(final FirebaseLoadingCallback firebaseLoadingCallback) {
		CollectionReference collRef = firestoreDatabase.collection("users").document(currentUser.getUid())
				.collection("accounts");

		collRef.get()
				.addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
					@Override
					public void onComplete(@NonNull Task<QuerySnapshot> task) {
						if (task.isSuccessful()) {
							// total balance for all accounts
							for (QueryDocumentSnapshot document : task.getResult()) {
								totalBalance += (long) document.get("amount");
							}
							// create first row in vertical layout - total amount from all accounts
							createNewRowInLinearView("total", String.valueOf(totalBalance));

							// blank line to separate total amount from accounts
							createNewRowInLinearView("", "");

							for (QueryDocumentSnapshot document : task.getResult()) {
								// create row for each account user has and show its amount (balance)
								createNewRowInLinearView(document.getId(), String.valueOf(document.get("amount")));
							}
							// make a callback so if the process is done, loading dialog disappears
							firebaseLoadingCallback.onCallback();
						} else {
							Log.w(TAG, "Error fetching the data from the user", task.getException());
							snackbarShow("Could not get accounts! Check internet connection or try later!");
							startActivity(new Intent(getContext(), MainActivity.class));
						}
					}
				});
	}

	/* represents one row in horizontal layout
	 * creates vertical layout and populates with data from DB */
	public void createNewRowInLinearView(String documentID, String amountString) {
		/* children of parent linearlayout (vertical) */
		// 1. linearlayout horizontal
		LinearLayout linearLayoutHorizontal = new LinearLayout(getContext());
		linearLayoutHorizontal.setLayoutParams(new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.MATCH_PARENT, 100));
		linearLayoutHorizontal.setOrientation(LinearLayout.HORIZONTAL);

		// 2. first text view (on the left) with an account name
		TextView newTextViewAccount = new TextView(getContext());
		newTextViewAccount.setTextSize(18);
		newTextViewAccount.setTextColor(Color.parseColor("#000000"));
		newTextViewAccount.setText(documentID); //document.getId()
		newTextViewAccount.setLayoutParams(new LinearLayout.LayoutParams(
				600, LinearLayout.LayoutParams.MATCH_PARENT));
		linearLayoutHorizontal.addView(newTextViewAccount);

		// 3. second text view (on the right) with a balance
		TextView newTextViewAmount = new TextView(getContext());
		newTextViewAmount.setTextSize(18);
		newTextViewAmount.setTextColor(Color.parseColor("#000000"));
		newTextViewAmount.setText(amountString); //String.valueOf(document.get("amount"))
		newTextViewAmount.setLayoutParams(new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
		linearLayoutHorizontal.addView(newTextViewAmount);

		linearLayout.addView(linearLayoutHorizontal);
	}

	/* resets the table */
	@Override
	public void onResume() {
		super.onResume();
		totalBalance = 0;
		linearLayout.removeAllViewsInLayout();
	}

	/* shows snackbar */
	private void snackbarShow(String msg) {
		Snackbar snackbarBad = Snackbar
				.make(getActivity().findViewById(android.R.id.content), msg, Snackbar.LENGTH_LONG);
		snackbarBad.show();
	}

	/* Callback */
	private interface FirebaseLoadingCallback {
		void onCallback();
	}

	/* Loading progress - new thread */
	class Load extends AsyncTask<String, String, String> {

		ProgressDialog progDailog = new ProgressDialog(getContext());

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			progDailog.setMessage("Loading banking data...");
			progDailog.setIndeterminate(false);
			progDailog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			progDailog.setCancelable(false);
			progDailog.show();
		}
		@Override
		protected String doInBackground(String... aurl) {
			loadAccountsAsTextFieldsInsideLinearView(new FirebaseLoadingCallback() {
				@Override
				public void onCallback() {
					progDailog.dismiss();
				}
			});
			return null;
		}
	}
}
