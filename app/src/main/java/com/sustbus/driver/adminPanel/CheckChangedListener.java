package com.sustbus.driver.adminPanel;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.firestore.DocumentSnapshot;

public interface CheckChangedListener {
    void onSwitchStateChanged(boolean isChecked, DocumentSnapshot snapshot);
    void onItemClicked(String uId);
}