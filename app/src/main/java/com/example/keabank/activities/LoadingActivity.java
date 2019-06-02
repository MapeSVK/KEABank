package com.example.keabank.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.example.keabank.R;


public class LoadingActivity extends AppCompatActivity {

    class Load extends AsyncTask<String, String, String> {
        RelativeLayout relativeLayout;

        public Load(RelativeLayout relativeLayout) {
            this.relativeLayout = relativeLayout;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... aurl) {
            for(int i=0;i<1000000;i++){
                System.out.print(i);
            }
            return null;
        }
        @Override
        protected void onPostExecute(String unused) {
            super.onPostExecute(unused);
            relativeLayout.setVisibility(View.GONE);
            startActivity(new Intent(getApplicationContext(), NavigationDrawerActivity.class));
        }

        //methoda na subtraction
        // 4x callback. 1 bude na subtraction, 2. na to fetching data (obycajny


    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading);

        RelativeLayout loadingLayout = (RelativeLayout) findViewById(R.id.loading);
        new Load(loadingLayout).execute();
    }

}













