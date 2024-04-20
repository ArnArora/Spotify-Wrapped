package com.example.spotifywrapped.ui.account;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.spotifywrapped.R;
import com.example.spotifywrapped.databinding.AccountBinding;
import com.example.spotifywrapped.databinding.AccountDashboardBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class AccountDashboard extends Fragment {
    private AccountDashboardBinding binding;
    private FirebaseUser user;
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = AccountDashboardBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        user = FirebaseAuth.getInstance().getCurrentUser();

        ImageButton backButton = binding.backButton;
        Button viewPast = binding.viewPastTitles;
        Button deleteAccountButton = binding.deleteAccount;
        Button logoutButton = binding.logout;
        Button updateButton = binding.updateAccount;

        viewPast.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Fragment newFragment = new PastDates();
                FragmentTransaction fm = getActivity().getSupportFragmentManager().beginTransaction();
                fm.replace(R.id.base_container, newFragment);
                fm.addToBackStack(null);

                Bundle bundle = new Bundle();
                newFragment.setArguments(bundle);

                fm.commit();
            }
        });

        updateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showUpdateAccount();
            }
        });

        deleteAccountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setTitle("Confirm Deletion")
                        .setPositiveButton("Confirm", (dialog, which) -> {
                            user.delete()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                getActivity().getSupportFragmentManager().popBackStack();
                                            }
                                        }
                                    });
                        })
                        .setNegativeButton("Cancel", (dialog, which) -> {
                        });
            }
        });

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().getSupportFragmentManager().popBackStack();
            }
        });

        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                Toast.makeText(getContext(), "Signed out", Toast.LENGTH_SHORT);
                getActivity().getSupportFragmentManager().popBackStack();
            }
        });

        return root;
    }

    public void showUpdateAccount() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View dialogView = getLayoutInflater().inflate(R.layout.create_account, null);
        builder.setView(dialogView)
                .setTitle("Update Account")
                .setPositiveButton("OK", (dialog, which) -> {
                    EditText editTextEmail = dialogView.findViewById(R.id.username);
                    EditText editTextPassword = dialogView.findViewById(R.id.password);
                    editTextEmail.setText(user.getEmail());
                    String email = editTextEmail.getText().toString();
                    String password = editTextPassword.getText().toString();
                    user.updateEmail(email);
                    user.updatePassword(password)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        Toast.makeText(getContext(), "User account updated", Toast.LENGTH_SHORT);
                                    }
                                }
                            });
                })
                .setNegativeButton("Cancel", (dialog, which) -> {
                });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

}
