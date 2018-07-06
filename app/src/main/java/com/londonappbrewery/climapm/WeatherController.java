package com.londonappbrewery.climapm;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.PermissionChecker;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;


public class WeatherController extends AppCompatActivity {

    // Constants:
    final int REQUEST_CODE = 123;
    final String WEATHER_URL = "http://api.openweathermap.org/data/2.5/weather";
    // App ID to use OpenWeather data
    final String APP_ID = "8ac74bd7a942efbb2969e3d9f9cb8a9e";
    // Time between location updates (5000 milliseconds or 5 seconds)
    final long MIN_TIME = 5000;
    // Distance between location updates (1000m or 1km)
    final float MIN_DISTANCE = 1000;

    // TODO: Set LOCATION_PROVIDER here:
    String LOCATION_PROVIDER = LocationManager.GPS_PROVIDER;


    // Member Variables:
    TextView mCityLabel;
    ImageView mWeatherImage;
    TextView mTemperatureLabel;

    // TODO: Declare a LocationManager and a LocationListener here:
    LocationManager locationManager;
    LocationListener locationListener;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.weather_controller_layout);

        // Linking the elements in the layout to Java code
        mCityLabel = (TextView) findViewById(R.id.locationTV);
        mWeatherImage = (ImageView) findViewById(R.id.weatherSymbolIV);
        mTemperatureLabel = (TextView) findViewById(R.id.tempTV);
        ImageButton changeCityButton = (ImageButton) findViewById(R.id.changeCityButton);

        // TODO: Add an OnClickListener to the changeCityButton here:
        changeCityButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(WeatherController.this, ChangeCityController.class);
                startActivity(intent);
            }
        });


    }


    // TODO: Add onResume() here:
    @Override
    protected void onResume() {
        super.onResume();
        Log.d("xxx", "onResume() called");

        Intent intent = getIntent();
        String newCity = intent.getStringExtra("city");

        if (newCity != null) {
            Log.d("xxx", "getting weather for city name");
            getWeatherForCityName(newCity);
        } else {
            Log.d("xxx", "getting weather for current location");
            getWeatherForCurrentLocation();
        }
    }


    // TODO: Add getWeatherForNewCity(String city) here:
    private void getWeatherForCityName(String newCity) {
        RequestParams params = new RequestParams();
        params.put("q", newCity);
        params.put("appid", APP_ID);

        getWeatherData(params);
    }


    // TODO: Add getWeatherForCurrentLocation() here:
    private void getWeatherForCurrentLocation() {
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                Log.d("xxx", "onLocationChanged() called");

                Double lat = location.getLatitude();
                Double lon = location.getLongitude();

                Log.d("xxx", String.valueOf(lat));
                Log.d("xxx", String.valueOf(lon));

                RequestParams params = new RequestParams();
                params.put("lat", lat);
                params.put("lon", lon);
                params.put("appid", APP_ID);

                getWeatherData(params);

            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {
                Log.d("xxx", "onProviderDisabled() called");

            }
        };

        if (PermissionChecker.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE);
        } else {
            locationManager.requestLocationUpdates(LOCATION_PROVIDER, MIN_TIME, MIN_DISTANCE, locationListener);
        }
    }

    private void getWeatherData(RequestParams params) {
        AsyncHttpClient httpClient = new AsyncHttpClient();
        httpClient.get(WEATHER_URL, params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                super.onSuccess(statusCode, headers, response);
                Log.d("xxx", "success " + response.toString());

                WeatherDataModel weatherData = WeatherDataModel.fromJson(response);
                updateUI(weatherData);

            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                super.onFailure(statusCode, headers, throwable, errorResponse);
                Log.d("xxx", "error " + errorResponse.toString());
                Log.d("xxx", "Status Code " + statusCode);
                Toast.makeText(WeatherController.this, "Request Failed ", Toast.LENGTH_SHORT).show();
            }
        });
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d("xxx", "Premission Granted");
                getWeatherForCurrentLocation();
            } else {
                Log.d("xxx", "Premission Denied");
            }
        }
    }

    // TODO: Add letsDoSomeNetworking(RequestParams params) here:


    // TODO: Add updateUI() here:
    private void updateUI(WeatherDataModel weather) {
        mCityLabel.setText(weather.getCity());
        mTemperatureLabel.setText(weather.getTemperature());

        int resourceId = getResources().getIdentifier(weather.getIconName(), "drawable", getPackageName());
        mWeatherImage.setImageResource(resourceId);
    }


    // TODO: Add onPause() here:


    @Override
    protected void onPause() {
        super.onPause();
        if (locationManager != null) {
            locationManager.removeUpdates(locationListener);
        }
    }
}
