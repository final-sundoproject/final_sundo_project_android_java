package com.example.sundo_project_app.regulatedArea;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.example.sundo_project_app.R;
import com.naver.maps.map.CameraUpdate;
import com.naver.maps.map.MapFragment;
import com.naver.maps.map.NaverMap;
import com.naver.maps.map.overlay.GroundOverlay;
import com.naver.maps.map.overlay.OverlayImage;
import com.naver.maps.geometry.LatLng;
import com.naver.maps.geometry.LatLngBounds;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

public class RegulatedArea extends AppCompatActivity {

    private static final String TAG = "RegulatedArea";
    private NaverMap naverMap;
    private static final String BASE_WMS_URL = "https://apis.data.go.kr/1192000/apVhdService_OpzFh/getOpnOpzFhWMS";
    private static final String SERVICE_KEY = "xyigcn2H+16RENHs6SNbyOXjPjW0t0Tastu/ePEl3PW6jMKcyrxrFErPO4Rzc+GgV2G44DvWYE/HGIeUhEIxCw==";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.map);

        MapFragment mapFragment = (MapFragment) getSupportFragmentManager().findFragmentById(R.id.map_fragment);
        if (mapFragment != null) {
            mapFragment.getMapAsync(naverMap -> {
                this.naverMap = naverMap;
                moveToKorea();

                double minX = 718918.25;
                double minY = 1433106.875;
                double maxX = 1249928.75;
                double maxY = 2071446.875;

                double[] wgs84Bounds = CoordinateConverter.convertEPSG5179ToWGS84(minX, minY, maxX, maxY);

                String wmsUrl = createWMSUrl(wgs84Bounds[0], wgs84Bounds[1], wgs84Bounds[2], wgs84Bounds[3], 400, 654);
                new DownloadImageTask().execute(wmsUrl);
            });
        }
    }

    private String createWMSUrl(double minLon, double minLat, double maxLon, double maxLat, int width, int height) {
        try {
            String encodedServiceKey = URLEncoder.encode(SERVICE_KEY, "UTF-8");
            return String.format("%s?serviceKey=%s&srs=EPSG:4326&bbox=%f,%f,%f,%f&width=%d&height=%d",
                    BASE_WMS_URL, encodedServiceKey, minLon, minLat, maxLon, maxLat, width, height);
        } catch (Exception e) {
            Log.e(TAG, "Error encoding service key: " + e.getMessage());
            return "";
        }
    }

    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        @Override
        protected Bitmap doInBackground(String... urls) {
            String url = urls[0];
            Bitmap bitmap = null;
            try {
                URL imageUrl = new URL(url);
                HttpURLConnection connection = (HttpURLConnection) imageUrl.openConnection();
                connection.setDoInput(true);
                connection.connect();
                InputStream input = connection.getInputStream();
                bitmap = BitmapFactory.decodeStream(input);
                Log.d(TAG, "Image downloaded successfully.");
            } catch (Exception e) {
                Log.e(TAG, "Error downloading image: " + e.getMessage());
            }
            return bitmap;
        }

        @Override
        protected void onPostExecute(Bitmap result) {
            if (result != null) {
                addWMSOverlay(result);
            } else {
                Log.e(TAG, "Failed to download image.");
            }
        }
    }

    private void moveToKorea() {
        if (naverMap != null) {
            LatLng koreaCenter = new LatLng(36.5, 127.5);
            CameraUpdate cameraUpdate = CameraUpdate.scrollAndZoomTo(koreaCenter, 5);
            naverMap.moveCamera(cameraUpdate);
            Log.d(TAG, "Camera moved to Korea center.");
        }
    }

    private void addWMSOverlay(Bitmap wmsBitmap) {
        if (naverMap == null) {
            Log.e(TAG, "NaverMap is not initialized.");
            return;
        }

        LatLng southwest = new LatLng(32.9, 124.5);
        LatLng northeast = new LatLng(38.5, 130.4);
        LatLngBounds bounds = new LatLngBounds(southwest, northeast);

        GroundOverlay groundOverlay = new GroundOverlay();
        groundOverlay.setImage(OverlayImage.fromBitmap(wmsBitmap));
        groundOverlay.setBounds(bounds);
        groundOverlay.setMap(naverMap);

        Log.d(TAG, "WMS overlay added to map.");
    }
}
