package com.creativeminds.remotedroid;

import android.Manifest;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.provider.Telephony;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private TextView textView,server_status;
    List<SMSModel> smsModels = new ArrayList<>();
    private static final int DEFAULT_PORT = 6969;
    private AndroidWebServer androidWebServer;
    private static boolean isStarted = false;
    private Button start_btn,stop_btn;
    private EditText port_no_et;
    private ImageView imageView_status;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textView = (TextView) findViewById(R.id.textview);
        start_btn = (Button) findViewById(R.id.start_id);
        stop_btn =(Button)  findViewById(R.id.stop_id);
        server_status = (TextView) findViewById(R.id.server_status_id);
        imageView_status = (ImageView) findViewById(R.id.imageView);
        port_no_et = (EditText) findViewById(R.id.port_et_id);
        port_no_et.setText(String.valueOf(DEFAULT_PORT));

        checkpermissions();

        start_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isStarted=startAndroidWebServer();
            }
        });

        stop_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isStarted=stopAndroidWebServer();
            }
        });


    }

    private boolean startAndroidWebServer(){
        if (!isStarted) {
            int port = getPortFromEditText();
            try {
                if (port == 0) {
                    throw new Exception();
                }
                androidWebServer = new AndroidWebServer(MainActivity.this,port);
                androidWebServer.start();
                server_status.setText("Server Running :"+getIpAccess());
                imageView_status.setImageResource(R.drawable.ic_settings_remote_lime_a700_36dp);
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                server_status.setText("Port Number Error");
                imageView_status.setImageResource(R.drawable.ic_settings_remote_red_a400_36dp);
                Toast.makeText(MainActivity.this, "The PORT " + port + " doesn't work, please change it between 1000 and 9999.", Toast.LENGTH_LONG).show();
            }
        }
        return false;
    }

    private boolean stopAndroidWebServer(){
        if (isStarted && androidWebServer != null) {
            androidWebServer.stop();
            server_status.setText("Server Stopped");
            imageView_status.setImageResource(R.drawable.ic_settings_remote_red_a400_36dp);
            return true;
        }
        return false;
    }

    private int getPortFromEditText() {
        String valueEditText = port_no_et.getText().toString();
        return (valueEditText.length() > 0) ? Integer.parseInt(valueEditText) : DEFAULT_PORT;
    }

    private String getIpAccess() {
        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
        int ipAddress = wifiManager.getConnectionInfo().getIpAddress();
        final String formatedIpAddress = String.format("%d.%d.%d.%d", (ipAddress & 0xff), (ipAddress >> 8 & 0xff), (ipAddress >> 16 & 0xff), (ipAddress >> 24 & 0xff));
        return "http://" + formatedIpAddress + ":"+getPortFromEditText();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopAndroidWebServer();
        isStarted = false;
        /*if (broadcastReceiverNetworkState != null) {
            unregisterReceiver(broadcastReceiverNetworkState);
        }*/
    }

    public void checkpermissions() {
        if(ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.CALL_PHONE,Manifest.permission.SEND_SMS}, 1);
        } else if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.CALL_PHONE}, 1);
        } else if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.SEND_SMS}, 1);
        }
    }


}
