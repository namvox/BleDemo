package me.namvo.bledemo;

import android.content.Context;

import com.polidea.rxandroidble.RxBleClient;

import javax.inject.Singleton;

import dagger.Component;

@Singleton
@Component(modules = AppModule.class)
public interface AppComponent {
    Context context();
    RxBleClient rxBleClient();
}
