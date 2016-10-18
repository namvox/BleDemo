package me.namvo.bledemo;

import com.polidea.rxandroidble.RxBleConnection;
import com.polidea.rxandroidble.RxBleDevice;
import com.polidea.rxandroidble.RxBleScanResult;
import com.polidea.rxandroidble.mockrxandroidble.RxBleClientMock;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import java.util.UUID;

import rx.Observable;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.functions.Actions;
import rx.functions.Func1;
import rx.observers.TestSubscriber;
import rx.subjects.PublishSubject;

@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 21)
public class RxBleClientMockTest {

    private RxBleClientMock rxBleClient;
    private UUID characteristicNotifiedUUID;
    private PublishSubject characteristicNotificationSubject;
    private UUID characteristicUUID;
    @Before
    public void before() {
        final UUID serviceUUID = UUID.fromString("00001234-0000-0000-8000-000000000000");
        characteristicUUID = UUID.fromString("00002a29-0000-1000-8000-00805f9b34fb");
        characteristicNotifiedUUID = UUID.fromString("00002a29-0000-1000-8000-00805f9b34fb");
        final byte[] characteristicData = "Polidea".getBytes();
        final UUID descriptorUUID = UUID.fromString("00001337-0000-1000-8000-00805f9b34fb");
        final byte[] descriptorData = "Config".getBytes();
        characteristicNotificationSubject = PublishSubject.create();
        rxBleClient = new RxBleClientMock.Builder()
                .addDevice(
                        new RxBleClientMock.DeviceBuilder()
                                .deviceMacAddress("AA:BB:CC:DD:EE:FF")
                                .deviceName("TestDevice")
                                .scanRecord("ScanRecord".getBytes())
                                .rssi(42)
                                .notificationSource(characteristicNotifiedUUID, characteristicNotificationSubject)
                                .addService(
                                        serviceUUID,
                                        new RxBleClientMock.CharacteristicsBuilder()
                                                .addCharacteristic(
                                                        characteristicUUID,
                                                        characteristicData,
                                                        new RxBleClientMock.DescriptorsBuilder()
                                                                .addDescriptor(descriptorUUID, descriptorData)
                                                                .build()
                                                ).build()
                                ).build()
                ).build();
    }

    @Test
    public void scan_verify_device_name() {
        final TestSubscriber<String> subscriber = new TestSubscriber<>();

        rxBleClient.scanBleDevices()
                .map(new Func1<RxBleScanResult, String>() {
                    @Override
                    public String call(RxBleScanResult rxBleScanResult) {
                        return rxBleScanResult.getBleDevice().getName();
                    }
                })
                .subscribe(subscriber);
        subscriber.assertValue("TestDevice");
    }

    @Test
    public void scan_verify_mac_address() {
        final TestSubscriber<String> subscriber = new TestSubscriber<>();

        rxBleClient.scanBleDevices()
                .map(new Func1<RxBleScanResult, String>() {
                    @Override
                    public String call(RxBleScanResult rxBleScanResult) {
                        return rxBleScanResult.getBleDevice().getMacAddress();
                    }
                })
                .subscribe(subscriber);
        subscriber.assertValue("AA:BB:CC:DD:EE:FF");
    }

    @Test
    public void connect() {
        final TestSubscriber<RxBleConnection> subscriber = new TestSubscriber<>();

        rxBleClient.scanBleDevices()
                .take(1)
                .flatMap(new Func1<RxBleScanResult, Observable<RxBleConnection>>() {
                    @Override
                    public Observable<RxBleConnection> call(RxBleScanResult rxBleScanResult) {
                        return rxBleScanResult.getBleDevice().establishConnection(RuntimeEnvironment.application, false);
                    }
                })
                .subscribe(subscriber);
        subscriber.assertTerminalEvent();
    }

    @Test
    public void connect_verify_state() {
        final TestSubscriber<RxBleConnection.RxBleConnectionState> stateChangesSubscribe = new TestSubscriber<>();


        final RxBleDevice rxBleDevice = rxBleClient.getBleDevice("AA:BB:CC:DD:EE:FF");
        rxBleDevice.observeConnectionStateChanges()
                .subscribe(stateChangesSubscribe);
        rxBleDevice.establishConnection(RuntimeEnvironment.application, false)
                .subscribe(Actions.empty(), new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        throwable.printStackTrace();
                    }
                });
        stateChangesSubscribe.assertValues(RxBleConnection.RxBleConnectionState.DISCONNECTED, RxBleConnection.RxBleConnectionState.CONNECTING, RxBleConnection.RxBleConnectionState.CONNECTED);
    }

    @Test
    public void return_notification() {
        final TestSubscriber<String> subscriber = new TestSubscriber<>();


        final RxBleDevice rxBleDevice = rxBleClient.getBleDevice("AA:BB:CC:DD:EE:FF");
        rxBleDevice.establishConnection(RuntimeEnvironment.application, false)
                .flatMap(new Func1<RxBleConnection, Observable<Observable<byte[]>>>() {
                    @Override
                    public Observable<Observable<byte[]>> call(RxBleConnection rxBleConnection) {
                        return rxBleConnection.setupNotification(characteristicNotifiedUUID);
                    }
                })
        .subscribe(new Action1<Observable<byte[]>>() {
            @Override
            public void call(Observable<byte[]> observable) {
                observable.map(new Func1<byte[], String>() {
                    @Override
                    public String call(byte[] bytes) {
                        return new String(bytes);
                    }
                }).subscribe(subscriber);
            }
        }, new Action1<Throwable>() {
            @Override
            public void call(Throwable throwable) {
                throwable.printStackTrace();
            }
        });

        characteristicNotificationSubject.onNext("Hello".getBytes());
        subscriber.assertValues("Hello");
    }

    @Test
    public void write_to_characteristic() {
        final TestSubscriber<String> subscriber = new TestSubscriber<>();

        final RxBleDevice rxBleDevice = rxBleClient.getBleDevice("AA:BB:CC:DD:EE:FF");
        rxBleDevice.establishConnection(RuntimeEnvironment.application, false)
                .flatMap(new Func1<RxBleConnection, Observable<byte[]>>() {
                    @Override
                    public Observable<byte[]> call(RxBleConnection rxBleConnection) {
                        return rxBleConnection.writeCharacteristic(characteristicUUID, "Hello".getBytes());
                    }
                })
                .map(new Func1<byte[], String>() {
                    @Override
                    public String call(byte[] bytes) {
                        return new String(bytes);
                    }
                }).subscribe(subscriber);

        characteristicNotificationSubject.onNext("Hello".getBytes());
        subscriber.assertValues("Hello");
    }

    @Test
    public void write_to_characteristic_then_notify() {
        final TestSubscriber<String> subscriber = new TestSubscriber<>();
        final TestSubscriber<String> notificationSubscriber = new TestSubscriber<>();

        final RxBleDevice rxBleDevice = rxBleClient.getBleDevice("AA:BB:CC:DD:EE:FF");
        rxBleDevice.establishConnection(RuntimeEnvironment.application, false)
                .doOnNext(new Action1<RxBleConnection>() {
                    @Override
                    public void call(RxBleConnection rxBleConnection) {
                        rxBleConnection.setupNotification(characteristicNotifiedUUID)
                                .subscribe(new Action1<Observable<byte[]>>() {
                                    @Override
                                    public void call(Observable<byte[]> observable) {
                                        observable.map(new Func1<byte[], String>() {
                                            @Override
                                            public String call(byte[] bytes) {
                                                return new String(bytes);
                                            }
                                        }).subscribe(notificationSubscriber);
                                    }
                                }, new Action1<Throwable>() {
                                    @Override
                                    public void call(Throwable throwable) {
                                        throwable.printStackTrace();
                                    }
                                });
                    }
                })
                .flatMap(new Func1<RxBleConnection, Observable<byte[]>>() {
                    @Override
                    public Observable<byte[]> call(RxBleConnection rxBleConnection) {
                        return rxBleConnection.writeCharacteristic(characteristicUUID, "Hello".getBytes());
                    }
                })
                .map(new Func1<byte[], String>() {
                    @Override
                    public String call(byte[] bytes) {
                        return new String(bytes);
                    }
                })
                .doOnSubscribe(new Action0() {
                    @Override
                    public void call() {
                        characteristicNotificationSubject.onNext("Hi".getBytes());
                    }
                })
                .subscribe(subscriber);

        characteristicNotificationSubject.onNext("Hello".getBytes());
        notificationSubscriber.assertValues("Hello");
    }
}
