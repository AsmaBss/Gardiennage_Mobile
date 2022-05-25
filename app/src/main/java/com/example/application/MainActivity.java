package com.example.application;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.util.HashMap;
import java.util.Map;


public class MainActivity extends AppCompatActivity {

    private static  final int REQUEST_LOCATION=1;

    Button getlocationBtn;
    TextView showLocationTxt;
    EditText longitudeTxt, latitudeTxt, idSignalTxt;
    String ajoutURL = "http://192.168.1.250/gardiennage/gps.php";
    LocationManager locationManager;
    String latitude,longitude;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        idSignalTxt=(EditText) findViewById(R.id.id_signal);

        ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION);

        showLocationTxt=findViewById(R.id.show_location);
        getlocationBtn=findViewById(R.id.getLocation);
        latitudeTxt=findViewById(R.id.show_latitude);
        longitudeTxt=findViewById(R.id.show_longitude);

        getlocationBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                locationManager=(LocationManager) getSystemService(Context.LOCATION_SERVICE);
                //Check gps is enable or not
                if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER))
                {
                    //Write Function To enable gps
                    OnGPS();
                }
                else
                {
                    //GPS is already On then
                    getLocation();
                }
                Thread t = new Thread(){
                    @Override
                    public void run(){
                        while(!isInterrupted()){
                            try {
                                Thread.sleep(30000);  //30000ms = 30 sec
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER))
                                        {
                                            //Write Function To enable gps
                                            OnGPS();
                                        }
                                        else
                                        {
                                            //GPS is already On then
                                            getLocation();
                                        }
                                        Toast.makeText(MainActivity.this, "Répèter ", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                };
                Toast.makeText(MainActivity.this, "Error", Toast.LENGTH_SHORT).show();
                t.start();

            }
        });

    }
    private void getLocation() {
        //Check Permissions again
        if (ActivityCompat.checkSelfPermission(MainActivity.this,Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(MainActivity.this,Manifest.permission.ACCESS_COARSE_LOCATION) !=PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(this,new String[]
                    {Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION);
        }
        else
        {
            android.location.Location LocationGps= locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            android.location.Location LocationNetwork=locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            android.location.Location LocationPassive=locationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);

            if (LocationGps !=null)
            {
                double lat=LocationGps.getLatitude();
                double longi=LocationGps.getLongitude();

                latitude=String.valueOf(lat);
                longitude=String.valueOf(longi);

                latitudeTxt.setText(latitude);
                longitudeTxt.setText(longitude);

                ajoutCoordonnees();
            }
            else if (LocationNetwork !=null)
            {
                double lat=LocationNetwork.getLatitude();
                double longi=LocationNetwork.getLongitude();

                latitude=String.valueOf(lat);
                longitude=String.valueOf(longi);

                latitudeTxt.setText(latitude);
                longitudeTxt.setText(longitude);

                ajoutCoordonnees();
            }
            else if (LocationPassive !=null)
            {
                double lat=LocationPassive.getLatitude();
                double longi=LocationPassive.getLongitude();

                latitude=String.valueOf(lat);
                longitude=String.valueOf(longi);

                latitudeTxt.setText(latitude);
                longitudeTxt.setText(longitude);

                ajoutCoordonnees();
            }
            else
            {
                Toast.makeText(this, "Impossible d'obtenir votre localisation", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void OnGPS() {

        final AlertDialog.Builder builder= new AlertDialog.Builder(this);

        builder.setMessage("Enable GPS").setCancelable(false).setPositiveButton("YES", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
            }
        }).setNegativeButton("NO", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                dialog.cancel();
            }
        });
        final AlertDialog alertDialog=builder.create();
        alertDialog.show();
    }

    private void ajoutCoordonnees(){
        final String id_signal = idSignalTxt.getText().toString().trim();
        final String latitude = latitudeTxt.getText().toString().trim();
        final String longitude = longitudeTxt.getText().toString().trim();

        StringRequest request = new StringRequest(Request.Method.POST, ajoutURL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                if (response.equalsIgnoreCase("Insertion")){
                    Toast.makeText(MainActivity.this, "Insertion", Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(MainActivity.this, response, Toast.LENGTH_SHORT).show();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(MainActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }) {

            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String,String> params  = new HashMap<String,String>();
                params.put("id_signal",id_signal);
                params.put("latitude",latitude);
                params.put("longitude",longitude);
                //Toast.makeText(MainActivity.this, "données ajoutées", Toast.LENGTH_SHORT).show();

                return params;
            }
        };
        RequestQueue requestQueue = Volley.newRequestQueue(MainActivity.this);
        requestQueue.add(request);
    }
}
