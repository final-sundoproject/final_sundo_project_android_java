package com.example.sundo_project_app.compass;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.example.sundo_project_app.R;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

public class CompassActivity extends AppCompatActivity implements SensorEventListener {

    private FusedLocationProviderClient fusedLocationClient;
    private TextView locationText, azimuthText;
    private ImageView compassImage;
    private SensorManager sensorManager;
    private Sensor accelerometer, magnetometer;

    private float[] gravity = new float[3];
    private float[] geomagnetic = new float[3];

    private float azimuth = 0f;
    private float currentAzimuth = 0f;

    private Handler handler = new Handler();
    private boolean isCameraLaunched = false;

    private static final int AZIMUTH_TOLERANCE = 5;  // 허용 범위 설정

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_compass);

        // Initialize views
        locationText = findViewById(R.id.location_text);
        azimuthText = findViewById(R.id.azimuth);
        compassImage = findViewById(R.id.compass_image);

        // Initialize location client
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Initialize sensors
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        // Request location
        getLocation();
    }

    @Override
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI);
        sensorManager.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_UI);
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this, accelerometer);
        sensorManager.unregisterListener(this, magnetometer);
    }

    private void getLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 100);
            return;
        }

        // Fetch the last known location
        fusedLocationClient.getLastLocation()
                .addOnCompleteListener(new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        if (task.isSuccessful() && task.getResult() != null) {
                            Location location = task.getResult();
                            double latitude = location.getLatitude();
                            double longitude = location.getLongitude();

                            // Update the location text view
                            locationText.setText("Latitude: " + latitude + ", Longitude: " + longitude);
                        } else {
                            locationText.setText("Unable to get location");
                        }
                    }
                });
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            gravity = lowPass(event.values.clone(), gravity);
        }
        if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            geomagnetic = lowPass(event.values.clone(), geomagnetic);
        }
        if (gravity != null && geomagnetic != null) {
            float R[] = new float[9];
            float I[] = new float[9];
            boolean success = SensorManager.getRotationMatrix(R, I, gravity, geomagnetic);
            if (success) {
                float orientation[] = new float[3];
                SensorManager.getOrientation(R, orientation);
                azimuth = (float) Math.toDegrees(orientation[0]);
                azimuth = (azimuth + 360) % 360;

                RotateAnimation ra = new RotateAnimation(
                        currentAzimuth,
                        -azimuth,
                        Animation.RELATIVE_TO_SELF, 0.5f,
                        Animation.RELATIVE_TO_SELF, 0.5f);

                ra.setDuration(1000);  // 애니메이션을 1000ms로 설정하여 부드러운 움직임을 유도합니다.
                ra.setFillAfter(true);

                compassImage.startAnimation(ra);
                currentAzimuth = -azimuth;

                azimuthText.setText("Azimuth: " + Math.round(azimuth));

                // Check if azimuth is within tolerance range
                if (Math.abs(Math.round(azimuth)) <= AZIMUTH_TOLERANCE && !isCameraLaunched) {
                    isCameraLaunched = true;
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            launchCamera();
                        }
                    }, 1000); // Launch camera after 2 seconds
                }
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Not used
    }

    private void launchCamera() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 200);
            return;
        }

        Intent intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        }
    }

    private float[] lowPass(float[] input, float[] output) {
        final float ALPHA = 0.15f;  // ALPHA 값을 낮추어 필터링을 더 강하게 적용합니다.
        if (output == null) return input;
        for (int i = 0; i < input.length; i++) {
            output[i] = output[i] + ALPHA * (input[i] - output[i]);
        }
        return output;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 200) { // 카메라 권한 요청 코드
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // 권한이 승인되었으므로 카메라 실행
                launchCamera();
            } else {
                // 권한이 거부되었을 때의 처리 (옵션)
                Toast.makeText(this, "카메라 권한이 필요합니다.", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
