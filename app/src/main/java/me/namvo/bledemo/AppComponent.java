package me.namvo.bledemo;

import android.content.Context;

import com.polidea.rxandroidble.RxBleClient;

import java.util.UUID;

import javax.inject.Named;
import javax.inject.Singleton;

import dagger.Component;
import rx.subjects.PublishSubject;

@Singleton
@Component(modules = AppModule.class)
public interface AppComponent {
    Context context();
    RxBleClient rxBleClient();
    PublishSubject publishSubject();
    @Named("UUID") UUID characteristicUUID();
    @Named("NotifiedUUID") UUID characteristicNotifiedUUID();
}
