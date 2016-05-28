package mts.mts;

import android.app.Activity;
import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

/**
 * Created by vidce on 16. 05. 2016.
 */
public class MyLocationListener implements LocationListener {

    static String TAG = "LocationAcivity";
    private EditText editLocation = null;

    @Override
    public void onLocationChanged(Location loc) {
        editLocation.setText("");
        String longitude = "Longitude: " + loc.getLongitude();
        Log.v(TAG, longitude);
        String latitude = "Latitude: " + loc.getLatitude();
        Log.v(TAG, latitude);
        Snackbar.make(null, longitude+"  "+latitude, Snackbar.LENGTH_LONG)
                .setAction("Action", null).show();
    }

    @Override
    public void onProviderDisabled(String provider) {}

    @Override
    public void onProviderEnabled(String provider) {}

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {}


}
