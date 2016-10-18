package me.namvo.bledemo.scan;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.widget.Toast;

import com.polidea.rxandroidble.RxBleScanResult;
import com.polidea.rxandroidble.exceptions.BleScanException;
import com.trello.rxlifecycle.components.RxActivity;

import javax.inject.Inject;

import me.namvo.bledemo.App;
import me.namvo.bledemo.R;
import me.namvo.bledemo.databinding.ActivityScanningBinding;
import rx.functions.Action1;
import rx.functions.Actions;

public class ScanActivity extends RxActivity {
    @Inject
    ScanViewModel scanningViewModel;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityScanningBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_scanning);

        dagger();
        binding.setViewModel(scanningViewModel);
        scanningViewModel.scan()
                .compose(this.<RxBleScanResult>bindToLifecycle())
                .subscribe(Actions.empty(), new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        onScanFailure(throwable);
                    }
                });
    }

    private void dagger() {
        DaggerScanComponent.builder()
                .appComponent(App.component())
                .build()
                .inject(this);
    }

    private void onScanFailure(Throwable throwable) {
        if (throwable instanceof BleScanException) {
            handleBleScanException((BleScanException) throwable);
        }
    }

    private void handleBleScanException(BleScanException bleScanException) {

        switch (bleScanException.getReason()) {
            case BleScanException.BLUETOOTH_NOT_AVAILABLE:
                Toast.makeText(ScanActivity.this, "Bluetooth is not available", Toast.LENGTH_SHORT).show();
                break;
            case BleScanException.BLUETOOTH_DISABLED:
                Toast.makeText(ScanActivity.this, "Enable bluetooth and try again", Toast.LENGTH_SHORT).show();
                break;
            case BleScanException.LOCATION_PERMISSION_MISSING:
                Toast.makeText(ScanActivity.this,
                        "On Android 6.0 location permission is required. Implement Runtime Permissions", Toast.LENGTH_SHORT).show();
                break;
            case BleScanException.LOCATION_SERVICES_DISABLED:
                Toast.makeText(ScanActivity.this, "Location services needs to be enabled on Android 6.0", Toast.LENGTH_SHORT).show();
                break;
            case BleScanException.BLUETOOTH_CANNOT_START:
            default:
                Toast.makeText(ScanActivity.this, "Unable to start scanning", Toast.LENGTH_SHORT).show();
                break;
        }
    }
}
