package me.namvo.bledemo;

import android.content.Context;

import com.polidea.rxandroidble.RxBleClient;
import com.polidea.rxandroidble.mockrxandroidble.RxBleClientMock;

import java.util.UUID;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import rx.subjects.PublishSubject;

@Module
public class AppModule {
    private final Context context;

    public AppModule(Context context) {
        this.context = context;
    }

    @Provides
    @Singleton
    public Context context() {
        return context;
    }

    @Provides
    @Singleton
    public RxBleClient rxBleClient() {
        final UUID serviceUUID = UUID.fromString("00001234-0000-0000-8000-000000000000");
        final UUID characteristicUUID = UUID.fromString("00002a29-0000-1000-8000-00805f9b34fb");
        final UUID characteristicNotifiedUUID = UUID.fromString("00002a29-0000-1000-8000-00805f9b34fb");
        final byte[] characteristicData = "Polidea".getBytes();
        final UUID descriptorUUID = UUID.fromString("00001337-0000-1000-8000-00805f9b34fb");
        final byte[] descriptorData = "Config".getBytes();
        final PublishSubject characteristicNotificationSubject = PublishSubject.create();
        return new RxBleClientMock.Builder()
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
}
