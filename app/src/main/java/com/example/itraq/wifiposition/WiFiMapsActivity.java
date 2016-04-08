package com.example.itraq.wifiposition;

import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class WiFiMapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private int count = 0;
    private WiFiPositionManager manager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wi_fi_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        manager = new WiFiPositionManager(this);

        final EditText numPointsText = (EditText) findViewById(R.id.editTextNumPoints);

        final LinearLayout buttonLayout = (LinearLayout) findViewById(R.id.layoutButtons);

        final Button updateButton1 = (Button) findViewById(R.id.buttonUpdate1);
        updateButton1.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                int numPoints = Integer.parseInt(numPointsText.getText().toString());
                manager.updateWifiPosition(true, false, numPoints);
                buttonLayout.setVisibility(View.INVISIBLE);
                final Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        buttonLayout.setVisibility(View.VISIBLE);
                    }
                }, 8000);
            }
        });

        final Button updateButton2 = (Button) findViewById(R.id.buttonUpdate2);
        updateButton2.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                int numPoints = Integer.parseInt(numPointsText.getText().toString());
                manager.updateWifiPosition(false, false, numPoints);
                buttonLayout.setVisibility(View.INVISIBLE);
                final Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        buttonLayout.setVisibility(View.VISIBLE);
                    }
                }, 8000);
            }
        });

        final Button updateButton3 = (Button) findViewById(R.id.buttonUpdate3);
        updateButton3.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                int numPoints = Integer.parseInt(numPointsText.getText().toString());
                manager.updateWifiPosition(false, true, numPoints);
                buttonLayout.setVisibility(View.INVISIBLE);
                final Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        buttonLayout.setVisibility(View.VISIBLE);
                    }
                }, 8000);
            }
        });

        Button clearButton = (Button) findViewById(R.id.buttonClear);
        clearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clearMap();
            }
        });
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
        LatLng sydney = new LatLng(-34, 151);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
        mMap.moveCamera(CameraUpdateFactory.zoomTo(14));
    }

    public void addLocation(LatLng location) {
        count++;
        mMap.addMarker(new MarkerOptions().position(location).title("Location #" + count));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(location));
    }

    public void setWiFiPointsNumber(int number) {
        TextView log = (TextView) findViewById(R.id.textViewLog);
        log.setText("Number of WiFi networks: " + number);
    }

    public void clearMap() {
        mMap.clear();
    }
}
