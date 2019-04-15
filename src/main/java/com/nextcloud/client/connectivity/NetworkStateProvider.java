package com.nextcloud.client.connectivity;

public interface NetworkStateProvider {
    boolean isWalled();
    boolean isUnmetered();
}
