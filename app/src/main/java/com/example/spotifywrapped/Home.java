package com.example.spotifywrapped;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.example.spotifywrapped.ui.holiday.HolidayFragment;
import com.example.spotifywrapped.ui.recommendations.RecsFragment;
import com.example.spotifywrapped.ui.wrapped.WrappedFragment;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link Home#newInstance} factory method to
 * create an instance of this fragment.
 */
public class Home extends Fragment {

    Button goToRecs;
    Button goToWrapped;
    Button goToHoliday;
    //Button goToShare;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.home_page, container, false);

        goToRecs = view.findViewById(R.id.recButton);
        goToWrapped = view.findViewById(R.id.wrappedButton);
        goToHoliday = view.findViewById(R.id.holidayButton);
        //goToShare = view.findViewById(R.id.shareButton);

        goToRecs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Fragment recsFragment = new RecsFragment();

                FragmentTransaction fm = getActivity().getSupportFragmentManager().beginTransaction();
                fm.replace(R.id.base_container, recsFragment).commit();
            }
        });

        goToWrapped.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Fragment wrappedFragment = new WrappedFragment();

                FragmentTransaction fm = getActivity().getSupportFragmentManager().beginTransaction();
                fm.replace(R.id.base_container, wrappedFragment).commit();
            }
        });

        goToHoliday.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Fragment holidayFragment = new HolidayFragment();

                FragmentTransaction fm = getActivity().getSupportFragmentManager().beginTransaction();
                fm.replace(R.id.base_container, holidayFragment).commit();
            }
        });

        return view;
    }
}