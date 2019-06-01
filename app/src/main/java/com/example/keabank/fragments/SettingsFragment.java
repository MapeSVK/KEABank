package com.example.keabank.fragments;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import com.example.keabank.R;
import com.example.keabank.activities.ChangePasswordActivity;
import com.example.keabank.activities.RegularTransactionsActivity;

public class SettingsFragment extends Fragment {
    private static final String TAG = "SettingsFragment";
    private Button changePasswordButton, regularTransactionsButton;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_settings, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initComponents(view);
    }

    public void initComponents(View view) {
        changePasswordButton = (Button) view.findViewById(R.id.settingsChangePasswordButton);
        changePasswordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getContext(), ChangePasswordActivity.class));

            }
        });

        regularTransactionsButton = (Button) view.findViewById(R.id.settingsRegularTransactionsButton);
        regularTransactionsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getContext(), RegularTransactionsActivity.class));

            }
        });
    }


}
