package com.example.spotifywrapped.ui.account;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
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
import com.example.spotifywrapped.databinding.PastDatesBinding;
import com.example.spotifywrapped.ui.wrapped.WrappedEntry;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;
import java.util.Map;

public class PastDates extends Fragment {
    private PastDatesBinding binding;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private FirebaseUser user;
    private ImageButton backButton;
    private Button[] dates;
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = PastDatesBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        user = mAuth.getCurrentUser();
        if (user == null) {
            notRetrieved();
        }

        backButton = binding.backButton;
        dates = new Button[]{binding.pastDateOne, binding.pastDateTwo, binding.pastDateThree,
            binding.pastDateFour, binding.pastDateFive};

        showDates();

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goBack();
            }
        });

        return root;
    }

    public void notRetrieved() {
        Toast.makeText(getContext(), "Cannot get user wraps", Toast.LENGTH_SHORT).show();
        goBack();
    }

    public void goBack() {
        getActivity().getSupportFragmentManager().popBackStack();
    }

    public void showDates() {
        DocumentReference userRef = db.collection("users").document(user.getUid());
        userRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot.exists()) {
                    Map<String, Object> wrappedEntriesList = (Map<String, Object>) documentSnapshot.get("wrappedEntries");
                    if (wrappedEntriesList != null) {
                        int i = wrappedEntriesList.size() - 1;
                        for (Object entry : wrappedEntriesList.values()) {
                            if (i >= 5) {
                                i--;
                                continue;
                            }
                            if (i < 0) {
                                break;
                            }
                            Map<String, String> wrappedEntry = (Map<String, String>) entry;
                            String date = wrappedEntry.get("date");
                            String artistString = wrappedEntry.get("artistString");
                            String trackString = wrappedEntry.get("trackString");
                            Button curButton = dates[i--];
                            curButton.setText(date);
                            curButton.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    showWrapped(artistString, trackString);
                                }
                            });
                        }
                    }
                } else {
                    notRetrieved();
                    Log.d("DOCUMENT NOT FOUND", "No such document");
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                notRetrieved();
                Log.d("COULD NOT RETRIEVE", "Error retrieving document", e);
            }
        });
    }

    public void showWrapped(String artistString, String trackString) {
        Fragment newFragment = new PastWrapped();
        FragmentTransaction fm = getActivity().getSupportFragmentManager().beginTransaction();
        fm.replace(R.id.base_container, newFragment);
        fm.addToBackStack(null);

        Bundle bundle = new Bundle();
        bundle.putString("artist-string", artistString);
        bundle.putString("track-string", trackString);
        newFragment.setArguments(bundle);

        fm.commit();
    }

}
