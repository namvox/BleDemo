package me.namvo.bledemo.scan;

import android.databinding.ObservableField;
import android.view.View;

import javax.inject.Inject;

import me.namvo.bledemo.device.DeviceActivity;

public class ResultViewModel {
    public ObservableField<String> name = new ObservableField<>();
    public ObservableField<String> mac = new ObservableField<>();

    @Inject public ResultViewModel() {}

    public void click(View v) {
        DeviceActivity.start(v.getContext(), mac.get());
    }
}
