package com.example.keabank.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.example.keabank.R;
import com.example.keabank.fragments.AutoBillListFragment;
import com.example.keabank.fragments.BillFragment;
import com.example.keabank.fragments.HomeFragment;
import com.example.keabank.fragments.SettingsFragment;
import com.example.keabank.fragments.TransferFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class NavigationDrawerActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

	private DrawerLayout drawer;
	private NavigationView navigationView;
	private FirebaseAuth firebaseAuth;
	private FirebaseUser currentUser;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_navigation_drawer); // main XML file, navigation "itself"

		initComponents();
		firstFragmentAfterActivityCreated(new HomeFragment(), savedInstanceState, R.id.nav_home); // set to home fragment
	}

	@Override
	public void onStart() {
		super.onStart();
		// Check if user is signed in
		currentUser = firebaseAuth.getCurrentUser();
		if (currentUser == null) {
			/* validation - if user is not logged in, it can cause problems. For safety reasons user
			 * will be sent back to login activity */
			startActivity(new Intent(NavigationDrawerActivity.this, MainActivity.class));
		}
	}

	private void initComponents() {
		// navigation view - navigation as the place where all menu items are
		navigationView = (NavigationView) findViewById(R.id.nav_view);
		// set listener to listen to item selection and implement your navigation logic.
		navigationView.setNavigationItemSelectedListener(this);

		//DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
		drawer = (DrawerLayout) findViewById(R.id.drawer_layout); // drawer init
		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar); // toolbar init - only the bar when menu button is placed
		// toggle init - menu icon which triggers menu opening
		ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
				this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);

		drawer.addDrawerListener(toggle); // assigning this toggle to drawer and listen to click
		toggle.syncState(); // assign method which recognise state - closed or open

		firebaseAuth = FirebaseAuth.getInstance();
	}

	/* show first fragment after activity onCreate method is called
	 * and set selected (checked) item in the menu to exact same fragment
	 * this happens only when activity is first time created (savedInstanceState is null) because we
	 * don't want to always create a new instance of the fragment, it is not efficient. (it would need to be loaded again)
	 * When it comes to setCheckedItem, it will stay checked after state changes
	 */
	public void firstFragmentAfterActivityCreated(Fragment fragment, Bundle savedInstanceState, int componentID) {
		if (savedInstanceState == null) {
			getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, fragment).commit();
			navigationView.setCheckedItem(componentID);
		}
	}


	/* if drawer (navigation) is open, back button will close it. Otherwise it will perform basing back operation to previous activity
	 from the super (parent) class */
	@Override
	public void onBackPressed() {
		//DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
		if (drawer.isDrawerOpen(GravityCompat.START)) {
			drawer.closeDrawer(GravityCompat.START);
		} else {
			super.onBackPressed();
		}
	}

	/* myslim si ze je to ta picovina so settings
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.navigation_drawer, menu); //instantiate menu XML files into Menu objects.
		return true;
	}
	*/

	/*@Override // toto vracia pravdepodobne spat tu pojebanu settings vec
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();

		//noinspection SimplifiableIfStatement
		if (id == R.id.action_settings) {
			return true;
		}

		return super.onOptionsItemSelected(item);
	}*/

	// handles click operations
	@Override
	public boolean onNavigationItemSelected(MenuItem item) {
		int id = item.getItemId(); // get an ID of menu item from the view

		if (id == R.id.nav_home) {
			getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new HomeFragment()).commit();
		} else if (id == R.id.nav_transfer) {
			getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new TransferFragment()).commit();
		} else if (id == R.id.nav_bill) {
			getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new BillFragment()).commit();
		} else if (id == R.id.nav_bill_list) {
			getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new AutoBillListFragment()).commit();
		} else if (id == R.id.nav_settings) {
			getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new SettingsFragment()).commit();
		} else if (id == R.id.nav_sign_out) {

		}


		// DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
		drawer.closeDrawer(GravityCompat.START); // close the drawer after click
		return true;
	}
}
