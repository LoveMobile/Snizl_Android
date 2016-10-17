package com.snizl.android.utilities;

import android.Manifest;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import com.snizl.android.AppConfig;
import com.snizl.android.AppController;

public class LocationTracker extends Service implements LocationListener {
    private static final float MIN_DISTANCE_CHANGE_FOR_UPDATES = 804.17f; // 0.5 mile  = 804.17 meters
    private static final long  MIN_TIME_BW_UPDATES             = 0;        // 1 minute

    private Context mContext;
    private LocationManager locationManager;

    public LocationTracker(Context context) {
        this.mContext = context;
        startLocationService();
    }

    private void startLocationService() {
        if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            AppController.mLocation = null;
            return;
        }

        locationManager = (LocationManager) mContext.getSystemService(LOCATION_SERVICE);

        boolean isGPSEnabled     = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        boolean isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

        if (isGPSEnabled || isNetworkEnabled) {
            Location location = null;

            if (isGPSEnabled) {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
                if (locationManager != null) {
                    location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                    if (location != null) {
                        Log.d(AppConfig.TAG, "GPS_PROVIDER");
                        AppController.mLocation = location;
                    }
                }
            }

            if (isNetworkEnabled) {
                if (location == null) {
                    locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, this);

                    if (locationManager != null) {
                        location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                        if (location != null) {
                            Log.d(AppConfig.TAG, "NETWORK_PROVIDER");
                            AppController.mLocation = location;
                        }
                    }
                }
            }
        }
    }

    public void stopLocationService() {
        if (locationManager != null) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }

            locationManager.removeUpdates(LocationTracker.this);
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.d(AppConfig.TAG, location.getLatitude() + " : " + location.getLongitude());
        AppController.mLocation = location;
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {
    }

    @Override
    public void onProviderEnabled(String s) {
    }

    @Override
    public void onProviderDisabled(String s) {
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
