package com.gtp.hunter.structure.viewmodel;

import androidx.lifecycle.ViewModel;

import java.util.UUID;

public class ProcessViewModel extends ViewModel {

    private UUID forklift_id;
    private String pallet;
    private String destination;
    private boolean transporting;

    public UUID getForkliftId() {
        return forklift_id;
    }

    public void setForkliftId(UUID forklift_id) {
        this.forklift_id = forklift_id;
    }

    public String getPallet() {
        return pallet;
    }

    public void setPallet(String pallet) {
        this.pallet = pallet;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public boolean isTransporting() {
        return transporting;
    }

    public void setTransporting(boolean transporting) {
        this.transporting = transporting;
    }
}
