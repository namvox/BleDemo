package me.namvo.bledemo.device;

import android.content.Context;
import android.databinding.ObservableArrayList;
import android.databinding.ObservableField;
import android.databinding.ObservableList;
import android.text.TextUtils;
import android.view.View;

import com.polidea.rxandroidble.RxBleClient;
import com.polidea.rxandroidble.RxBleConnection;
import com.polidea.rxandroidble.RxBleDevice;

import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;

import me.namvo.bledemo.BR;
import me.namvo.bledemo.R;
import me.tatarka.bindingcollectionadapter.ItemView;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Actions;
import rx.functions.Func1;
import rx.subjects.PublishSubject;

public class DeviceViewModel {
    final Provider<MessageViewModel> messageViewModelProvider;
    private final RxBleClient rxBleClient;
    private final Context context;
    private final UUID characteristicUUID;
    final UUID characteristicNotifiedUUID;
    final PublishSubject characteristicNotificationSubject;

    private RxBleDevice device;

    public ObservableField<String> status = new ObservableField<>();
    public ObservableField<String> text = new ObservableField<>();
    public final ObservableList<MessageViewModel> messages = new ObservableArrayList<>();
    public final ItemView messageView = ItemView.of(BR.viewModel, R.layout.item_message);
    RxBleConnection connection;
    private Observable.Transformer<Object, Object> transformer;

    @Inject
    public DeviceViewModel(Context context, RxBleClient rxBleClient,
                           Provider<MessageViewModel> messageViewModelProvider,
                           @Named("NotifiedUUID") UUID characteristicNotifiedUUID,
                           @Named("UUID") UUID characteristicUUID,
                           PublishSubject characteristicNotificationSubject) {
        this.rxBleClient = rxBleClient;
        this.context = context;
        this.messageViewModelProvider = messageViewModelProvider;
        this.characteristicUUID = characteristicUUID;
        this.characteristicNotifiedUUID = characteristicNotifiedUUID;
        this.characteristicNotificationSubject = characteristicNotificationSubject;
    }

    public Observable<String> connect() {
        return device.establishConnection(context, false)
                .doOnNext(new Action1<RxBleConnection>() {
                    @Override
                    public void call(RxBleConnection rxBleConnection) {
                        DeviceViewModel.this.connection = rxBleConnection;
                    }
                })
                .flatMap(new Func1<RxBleConnection, Observable<Observable<byte[]>>>() {
                    @Override
                    public Observable<Observable<byte[]>> call(RxBleConnection rxBleConnection) {
                        return rxBleConnection.setupNotification(characteristicNotifiedUUID);
                    }
                })
                .flatMap(new Func1<Observable<byte[]>, Observable<String>>() {
                    @Override
                    public Observable<String> call(Observable<byte[]> observable) {
                        return observable.map(new Func1<byte[], String>() {
                            @Override
                            public String call(byte[] bytes) {
                                return new String(bytes);
                            }
                        });
                    }
                })
                .doOnNext(new Action1<String>() {
                    @Override
                    public void call(String s) {
                        final MessageViewModel viewModel = messageViewModelProvider.get();
                        viewModel.text.set("Client: " + s);
                        messages.add(0, viewModel);
                    }
                });
    }

    public void send(View view) {
        if (TextUtils.isEmpty(this.text.get()))
            return;

        final MessageViewModel viewModel = messageViewModelProvider.get();
        viewModel.text.set("Host: " + this.text.get());
        messages.add(0, viewModel);
        connection.writeCharacteristic(characteristicUUID, text.get().getBytes())
                .map(new Func1<byte[], String>() {
                    @Override
                    public String call(byte[] bytes) {
                        return new String(bytes);
                    }
                })
                .doOnNext(new Action1<String>() {
                    @Override
                    public void call(String s) {
                        characteristicNotificationSubject.onNext(s.getBytes());
                        DeviceViewModel.this.text.set("");
                    }
                })
                .compose(transformer)
                .subscribe(Actions.empty(), new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        throwable.printStackTrace();
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

    public void setEvent(Observable.Transformer<Object, Object> transformer) {
        this.transformer = transformer;
    }
}
