package com.example.itraq.wifiposition;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.widget.ArrayAdapter;

import com.google.android.gms.maps.model.LatLng;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Created by dreamwalker on 4/6/2016.
 */
public class WiFiPositionManager {
    private WiFiMapsActivity wiFiMapsActivity;
    private WifiManager wifi;
    private List<ScanResult> wifis;
    private WifiScanReceiver wifiReciever;
    private boolean isScanning = false;

    private boolean breakScan = false;
    private boolean includeRssi = false;
    private int numPoints = 10;

    public WiFiPositionManager(WiFiMapsActivity wiFiMapsActivity) {
        this.wiFiMapsActivity = wiFiMapsActivity;
    }

    private void scanWifiNetworks() {
        if (isScanning) {
            return;
        }
        wifi = (WifiManager) wiFiMapsActivity.getSystemService(Context.WIFI_SERVICE);
        wifiReciever = new WifiScanReceiver();
        wiFiMapsActivity.registerReceiver(wifiReciever, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
        wifi.startScan();
        isScanning = true;

        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                try {
                    wiFiMapsActivity.unregisterReceiver(wifiReciever);
                } catch (Exception e) {

                }

                if (!isScanning) {
                    return;
                }
                isScanning = false;
                getPositionByWifiNetworks();
            }
        }, 8000);
    }

    private void getPositionByWifiNetworks() {
        wiFiMapsActivity.setWiFiPointsNumber(wifis.size());
        if (wifis.size() < 1) {
            return;
        }

        if (wifis.size() > numPoints) {
            wifis = wifis.subList(0, numPoints - 1);
        }

        JSONArray positionsArray = new JSONArray();

        for (int i = 0; i < wifis.size(); i++) {
            Map positionMap = new HashMap();
            positionMap.put("macAddress", wifis.get(i).BSSID);
            if (includeRssi) {
                positionMap.put("signalStrength", wifis.get(i).level);
            }
            JSONObject positionObject = new JSONObject(positionMap);
            positionsArray.put(positionObject);
        }

        JSONObject googleRequest = new JSONObject();

        try {
            googleRequest.put("wifiAccessPoints", positionsArray);

            final HttpClient client = new DefaultHttpClient();
            final HttpPost post = new HttpPost("https://www.googleapis.com/geolocation/v1/geolocate?key=AIzaSyBZsYVffqBKOCU0kUSSXubszTo03xwnvI4");

            StringEntity se = null;
            se = new StringEntity(googleRequest.toString());
            se.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
            post.setEntity(se);

            Thread thread = new Thread(new Runnable(){
                @Override
                public void run() {
                    try {
                        HttpResponse response = client.execute(post);
                        String responseText = EntityUtils.toString(response.getEntity());
                        JSONObject responseObject = new JSONObject(responseText);
                        double lat = responseObject.getJSONObject("location").getDouble("lat");
                        double lng = responseObject.getJSONObject("location").getDouble("lng");

                        final LatLng latLng = new LatLng(lat, lng);
                        wiFiMapsActivity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                wiFiMapsActivity.addLocation(latLng);
                            }
                        });

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
            thread.start();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void updateWifiPosition(boolean breakScan, boolean includeRssi, int numPoints) {
        this.breakScan = breakScan;
        this.includeRssi = includeRssi;
        this.numPoints = numPoints;
        scanWifiNetworks();
    }

    private class WifiScanReceiver extends BroadcastReceiver {
        public void onReceive(Context c, Intent intent) {
            List<ScanResult> wifiScanList = wifi.getScanResults();
            Collections.sort(wifiScanList, new Comparator<ScanResult>() {
                @Override
                public int compare(ScanResult lhs, ScanResult rhs) {
                    return WifiManager.compareSignalLevel(rhs.level, lhs.level);
                }
            });
            wifis = wifiScanList;
            if (breakScan && (wifis.size() >= numPoints)) {
                try {
                    wiFiMapsActivity.unregisterReceiver(wifiReciever);
                } catch (Exception e) {

                }
                if (!isScanning) {
                    return;
                }
                isScanning = false;
                getPositionByWifiNetworks();
            }
        }
    }
}
