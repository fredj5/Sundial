package com.example.sundial.sun;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.sundial.databinding.FragmentSunBinding;

public class SunFragment extends Fragment {

    private FragmentSunBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        SunViewModel sunViewModel =
                new ViewModelProvider(this).get(SunViewModel.class);

        binding = FragmentSunBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        final TextView textView = binding.textSun;
        sunViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);
        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
