package com.example.spotifywrapped.ui.account;

import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;

import android.app.AlertDialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.spotifywrapped.BaseActivity;
import com.example.spotifywrapped.Home;
import com.example.spotifywrapped.MainActivity;
import com.example.spotifywrapped.R;
import com.example.spotifywrapped.databinding.AccountBinding;
import com.example.spotifywrapped.databinding.FragmentRecsBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.spotify.sdk.android.auth.AuthorizationClient;
import com.spotify.sdk.android.auth.AuthorizationRequest;
import com.spotify.sdk.android.auth.AuthorizationResponse;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class AccountFragment extends Fragment {
    private AccountBinding binding;
    private static final String TAG = "EmailPassword";
    private FirebaseAuth mAuth;
    private Button loginButton, createAccountButton, homeButton;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        //return inflater.inflate(R.layout.fragment_recs, container, false);
        binding = AccountBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        createAccountButton = binding.createAccount;
        loginButton = binding.login;
        homeButton = binding.backToHome;

        mAuth = FirebaseAuth.getInstance();

        homeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().getSupportFragmentManager().popBackStack();
            }
        });
        createAccountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showCreateAccount();
            }
        });

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mAuth.getCurrentUser() != null) {
                    goToDashboard();
                } else {
                    showLogin();
                }
            }
        });

        return root;
    }

    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null){
            loginButton.setText("Go To Account");
        }
    }

    public void goToDashboard() {
        FirebaseUser user = mAuth.getCurrentUser();
        Fragment newFragment = new AccountDashboard();
        FragmentTransaction fm = getActivity().getSupportFragmentManager().beginTransaction();
        fm.replace(R.id.base_container, newFragment);
        fm.addToBackStack(null);

        Bundle bundle = new Bundle();
        newFragment.setArguments(bundle);

        fm.commit();
    }

    public void showLogin() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View dialogView = getLayoutInflater().inflate(R.layout.create_account, null);
        builder.setView(dialogView)
                .setTitle("Log In")
                .setPositiveButton("Log In", (dialog, which) -> {
                    EditText editTextEmail = dialogView.findViewById(R.id.username);
                    EditText editTextPassword = dialogView.findViewById(R.id.password);
                    String email = editTextEmail.getText().toString();
                    String password = editTextPassword.getText().toString();

                    mAuth.signInWithEmailAndPassword(email, password)
                            .addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()) {
                                        Toast.makeText(getContext(), "Logged in",
                                                Toast.LENGTH_SHORT).show();
                                        goToDashboard();
                                    } else {
                                        Toast.makeText(getContext(), "Authentication failed",
                                                Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                })
                .setNegativeButton("Cancel", (dialog, which) -> {
                });;
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    public void showCreateAccount() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Create Account");
        View dialogView = getLayoutInflater().inflate(R.layout.create_account, null);
        builder.setView(dialogView);
        builder.setPositiveButton("Create", (dialog, which) -> {
                    EditText editTextEmail = dialogView.findViewById(R.id.username);
                    EditText editTextPassword = dialogView.findViewById(R.id.password);
                    String email = editTextEmail.getText().toString();
                    String password = editTextPassword.getText().toString();
                    if (email.length() < 5) {
                        Toast.makeText(getContext(), "Not a valid email", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (password.length() < 8) {
                        Toast.makeText(getContext(), "Not a valid password", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    mAuth.createUserWithEmailAndPassword(email, password)
                            .addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()) {
                                        Toast.makeText(getContext(), "Created account",
                                                Toast.LENGTH_SHORT).show();
                                        loginButton.setText("Go To Account");
                                    } else {
                                        Toast.makeText(getContext(), "Authentication failed",
                                                Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                })
                .setNegativeButton("Cancel", (dialog, which) -> {
                });;
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }
}