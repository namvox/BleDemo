package me.namvo.bledemo;

import android.content.Context;

import com.polidea.rxandroidble.RxBleClient;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

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
    public RxBleClient rxBleClient(Context context) {
        return RxBleClient.create(context);
    }
}
