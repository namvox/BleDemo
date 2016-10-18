package me.namvo.bledemo;

import android.app.Application;

public class App extends Application {
    private static AppComponent component;

    public App() {
        component = DaggerAppComponent.builder()
                .appModule(new AppModule(this))
                .build();
    }

    public static AppComponent component() {
        return component;
    }
}
