package com.example.sundial.info;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class InfoViewModel extends ViewModel {

    private final MutableLiveData<String> mText;

    public InfoViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("Risks of UV Exposure:\n" +
                "Sunburn is a sign of short-term overexposure, while premature aging and skin cancer are side effects of prolonged UV exposure.\n" +
                "UV exposure increases the risk of potentially blinding eye diseases, if eye protection is not used.\n" +
                "Overexposure to UV radiation can lead to serious health issues, including cancer.\n" + "\n\n\n" +
                "Benefits of Vitamin D:\n" +
                "The production of vitamin D, a vitamin essential to human health.\n" +
                "Vitamin D helps the body absorb calcium and phosphorus from food and assists bone development. The World Health Organization (WHO) recommends 5 to 15 minutes of sun exposure 2 to 3 times a week.\n\n\n\n\n");
    }

    public LiveData<String> getText() {
        return mText;
    }
}
