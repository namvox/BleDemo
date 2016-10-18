package me.namvo.bledemo.scan;

import android.databinding.ObservableArrayList;
import android.databinding.ObservableList;

import com.polidea.rxandroidble.RxBleClient;
import com.polidea.rxandroidble.RxBleScanResult;

import javax.inject.Inject;
import javax.inject.Provider;

import me.namvo.bledemo.BR;
import me.namvo.bledemo.R;
import me.tatarka.bindingcollectionadapter.ItemView;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;

public class ScanViewModel {
    public final ObservableList<ResultViewModel> devices = new ObservableArrayList<>();
    public final ItemView deviceView = ItemView.of(BR.viewModel, R.layout.item_device);

    private final Provider<ResultViewModel> deviceViewModelProvides;
    private final RxBleClient rxBleClient;

    @Inject
    public ScanViewModel(RxBleClient rxBleClient, Provider<ResultViewModel> deviceViewModelProvides) {
        this.rxBleClient = rxBleClient;
        this.deviceViewModelProvides = deviceViewModelProvides;
    }

    public Observable<RxBleScanResult> scan() {
        return rxBleClient.scanBleDevices()
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext(new Action1<RxBleScanResult>() {
                    @Override
                    public void call(RxBleScanResult rxBleScanResult) {
                        final ResultViewModel viewModel = deviceViewModelProvides.get();
                        viewModel.mac.set(rxBleScanResult.getBleDevice().getMacAddress());
                        viewModel.name.set(rxBleScanResult.getBleDevice().getName());
                        devices.add(viewModel);
                    }
                });

    }
}
