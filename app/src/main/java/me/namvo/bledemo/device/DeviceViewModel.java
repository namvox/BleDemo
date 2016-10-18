package me.namvo.bledemo.device;

import android.content.Context;
import android.databinding.ObservableField;

import com.polidea.rxandroidble.RxBleClient;
import com.polidea.rxandroidble.RxBleConnection;
import com.polidea.rxandroidble.RxBleDevice;

import javax.inject.Inject;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;

public class DeviceViewModel {
    public ObservableField<String> status = new ObservableField<>();
    private final RxBleClient rxBleClient;
    private final Context context;
    private RxBleDevice device;

    @Inject
    public DeviceViewModel(Context context, RxBleClient rxBleClient) {
        this.rxBleClient = rxBleClient;
        this.context = context;
    }

    public Observable<RxBleConnection> connect() {
        return device.establishConnection(context, false)
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext(new Action1<RxBleConnection>() {
                    @Override
                    public void call(RxBleConnection rxBleConnection) {
                        status.set("Connected");
                    }
                });
    }

    public Observable<RxBleConnection.RxBleConnectionState> repair() {
        return device.observeConnectionStateChanges()
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext(new Action1<RxBleConnection.RxBleConnectionState>() {
                    @Override
                    public void call(RxBleConnection.RxBleConnectionState rxBleConnectionState) {
                        status.set(rxBleConnectionState.toString());
                    }
                });
    }

    public boolean setup(String macAddress) {
        if (macAddress.isEmpty())
            return false;

        device = rxBleClient.getBleDevice(macAddress);
        return true;
    }
}
