package com.example.sundial.info;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.TextView;
import com.example.sundial.R;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.sundial.databinding.FragmentInfoBinding;

public class InfoFragment extends Fragment {

    private FragmentInfoBinding binding;
    TextView article3_link;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        InfoViewModel infoViewModel =
                new ViewModelProvider(this).get(InfoViewModel.class);

        View view = inflater.inflate(R.layout.fragment_info, container, false);

        binding = FragmentInfoBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        article3_link = view.findViewById(R.id.textView5);
        article3_link.setMovementMethod(LinkMovementMethod.getInstance());

        /* Button info_Button1 = (Button)view.findViewById(R.id.info_link1);
        info_Button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String url = "https://www.cdc.gov/nceh/features/uv-radiation-safety/index.html#:~:text=times%20a%20week.-,Risks,serious%20health%20issues%2C%20including%20cancer";
                Uri uri = Uri.parse(url);
                Intent i = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(i);
            }
        }); */
        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
