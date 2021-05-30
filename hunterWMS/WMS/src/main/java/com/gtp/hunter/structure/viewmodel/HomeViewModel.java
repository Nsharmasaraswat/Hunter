package com.gtp.hunter.structure.viewmodel;

import androidx.lifecycle.ViewModel;

import java.util.HashSet;
import java.util.Set;

public class HomeViewModel extends ViewModel {
    private Set<com.google.android.material.button.MaterialButton> permissionButtons = new HashSet<>();

    public Set<com.google.android.material.button.MaterialButton> getPermissionButtons() {
        return permissionButtons;
    }

    public void setPermissionButtons(Set<com.google.android.material.button.MaterialButton> permissionButtons) {
        this.permissionButtons = permissionButtons;
    }
}
