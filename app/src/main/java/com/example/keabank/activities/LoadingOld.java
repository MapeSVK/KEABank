/*package com.example.keabank.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.RelativeLayout;

import com.example.keabank.R;


public class LoadingActivity extends AppCompatActivity {

    class Load extends AsyncTask<String, String, String> {

        ProgressDialog progDailog = new ProgressDialog(LoadingActivity.this);

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progDailog.setMessage("Loading...");
            progDailog.setIndeterminate(false);
            progDailog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progDailog.setCancelable(true);
            progDailog.show();
        }
        @Override
        protected String doInBackground(String... aurl) {
            for(int i=0;i<10000000;i++){
                System.out.print(i);
            }
            startActivity(new Intent(getApplicationContext(), NavigationDrawerActivity.class));
            return null;
        }
        @Override
        protected void onPostExecute(String unused) {
            super.onPostExecute(unused);
            progDailog.dismiss();
        }
    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading);

        new Load().execute();
    }

}












private void finishLoadList(){
    RelativeLayout loadingLayout = (RelativeLayout) findViewById(R.id.loading);
    ListView listView = (ListView) findViewById(R.id.list_view);

    loadingLayout.setVisibility(View.GONE);
    listView.setVisibility(View.VISIBLE);
}
















*/













