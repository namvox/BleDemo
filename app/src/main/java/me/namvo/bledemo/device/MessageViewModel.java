package me.namvo.bledemo.device;

import android.databinding.ObservableField;

import javax.inject.Inject;

public class MessageViewModel {
    public ObservableField<String> text = new ObservableField<>();

    @Inject public MessageViewModel() {}
}
