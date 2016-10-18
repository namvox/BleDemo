package me.namvo.bledemo.scan;

import dagger.Component;
import me.namvo.bledemo.ActivityScope;
import me.namvo.bledemo.AppComponent;

@ActivityScope
@Component(
        modules = ScanModule.class,
        dependencies = AppComponent.class)
public interface ScanComponent {
    void inject(ScanActivity activity);
}
