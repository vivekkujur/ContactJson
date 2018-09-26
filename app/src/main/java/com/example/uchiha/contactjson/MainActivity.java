package com.example.uchiha.contactjson;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Handler;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "Main_activity";
    private boolean mContactPermissionGranted = false;
    private static final String READ_CONTACTS = Manifest.permission.READ_CONTACTS;
    private static final int CONTACTS_PERMISSION_REQUEST_CODE = 1234;
    private TextView textView;
    Handler handler;

    private ArrayList<String> mEntries=new ArrayList<>();
    private ArrayList<String> data=new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if(mContactPermissionGranted){
            getContactPermission();

        }

    }

    @Override
    protected void onStart() {
        super.onStart();

        ShowContacts();

    }

    public void ShowContacts(){

        if(mContactPermissionGranted) {

            handler = new Handler();

            textView = findViewById(R.id.texthello);
            final Cursor phpnes = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, null);


            Thread thread = new Thread() {
                @Override
                public void run() {

                    handler.post(new Runnable() {
                        @Override
                        public void run() {

                            data=cur2Json(phpnes);
                            textView.setText(data.toString());
                        }
                    });

                }
            };
            thread.start();

        }else{
            getContactPermission();
        }
        
    }


    private void getContactPermission() {


        if(ContextCompat.checkSelfPermission(this.getApplicationContext(),READ_CONTACTS)== PackageManager.PERMISSION_GRANTED){

            mContactPermissionGranted=true;
            ShowContacts();


        }else{
            ActivityCompat.requestPermissions(this, new String[]{READ_CONTACTS},
                    CONTACTS_PERMISSION_REQUEST_CODE);
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch(requestCode){

            case CONTACTS_PERMISSION_REQUEST_CODE:{

                if (grantResults.length > 0){
                    for (int i=0; i< grantResults.length;i++){

                        if( grantResults[i]!=PackageManager.PERMISSION_GRANTED){
                            mContactPermissionGranted=false;
                            Log.d(TAG, "onRequestPermissionsResult: permission failed");
                            return;
                        }
                    }

                    mContactPermissionGranted=true;
                    ShowContacts();

                }
            }

        }
    }


    public ArrayList cur2Json(Cursor cursor) {



        JSONArray resultSet = new JSONArray();
        cursor.moveToFirst();
        while (cursor.isAfterLast() == false) {

            int totalColumn = cursor.getColumnCount();
            JSONObject Object = new JSONObject();

            for (int i = 0; i < totalColumn; i++) {
                if (cursor.getColumnName(i) != null) {
                    try {
                        Object.put(cursor.getColumnName(i),
                                cursor.getString(i));
                    } catch (Exception e) {
                        Log.d(TAG, e.getMessage());
                    }
                }
            }
            resultSet.put(Object);
            cursor.moveToNext();
        }
        cursor.close();

        for(int i = 0; i < resultSet.length(); i++) {
            try {
                JSONObject jsonObject = resultSet.getJSONObject(i);
                mEntries.add(jsonObject.toString());
            }
            catch(JSONException e) {
                mEntries.add("Error: " + e.getLocalizedMessage());
            }
        }
        return mEntries;
    }
}

