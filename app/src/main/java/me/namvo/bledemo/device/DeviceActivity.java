package me.namvo.bledemo.device;

import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;

import com.trello.rxlifecycle.ActivityEvent;
import com.trello.rxlifecycle.components.RxActivity;

import javax.inject.Inject;

import me.namvo.bledemo.App;
import me.namvo.bledemo.R;
import me.namvo.bledemo.databinding.ActivityDeviceBinding;
import rx.functions.Action1;
import rx.functions.Actions;

public class DeviceActivity extends RxActivity {
    private final static String BUNDLE_MAC_ADDRESS = "mac_address";

    @Inject
    DeviceViewModel viewModel;

    public static void start(Context context, String macAddress) {
        final Intent intent = new Intent(context, DeviceActivity.class);
        intent.putExtra(BUNDLE_MAC_ADDRESS, macAddress);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityDeviceBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_device);
        dagger();
        binding.setViewModel(viewModel);

        final boolean result = viewModel.setup(getIntent().getExtras().getString(BUNDLE_MAC_ADDRESS, ""));
        if (result) {
//            viewModel.repair().compose(bindUntilEvent(ActivityEvent.DESTROY))
//            .subscribe(Actions.empty(), new Action1<Throwable>() {
//                @Override
//                public void call(Throwable throwable) {
//                    throwable.printStackTrace();
//                }
//            });
            viewModel.connect().compose(bindUntilEvent(ActivityEvent.PAUSE))
            .subscribe(Actions.empty(), new Action1<Throwable>() {
                @Override
                public void call(Throwable throwable) {
                    throwable.printStackTrace();
                }
            });
        }
    }

    private void dagger() {
        DaggerDeviceComponent.builder()
                .appComponent(App.component())
                .build()
                .inject(this);
    }
}
