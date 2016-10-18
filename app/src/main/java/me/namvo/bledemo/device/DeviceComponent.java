package me.namvo.bledemo.device;

import dagger.Component;
import me.namvo.bledemo.ActivityScope;
import me.namvo.bledemo.AppComponent;

@ActivityScope
@Component(
        modules = DeviceModule.class,
        dependencies = AppComponent.class)
public interface DeviceComponent {
    void inject(DeviceActivity activity);
}
