package com.example.locationpinned;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.location.Address;
import android.location.Geocoder;

import androidx.annotation.Nullable;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class DBHelper extends SQLiteOpenHelper {

    private List<String> listOfAddresses;
    private Geocoder geocoder;
    private static final String DATABASE_NAME = "Location.db";
    private static final int DATABASE_VERSION = 1;
    private static final String TABLE_NAME = "Location";
    private static final String LOCATION_ID = "_id";
    public static final String LOCATION_NAME = "location_name";
    public static final String LOCATION_LATITUDE = "latitude";
    public static final String LOCATION_LONGITUDE = "longitude";

    public DBHelper(@Nullable Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        listOfAddresses = new ArrayList<>();
        geocoder = new Geocoder(context, Locale.getDefault());
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String query = "CREATE TABLE " + TABLE_NAME + "(" + LOCATION_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                LOCATION_NAME + " TEXT, " + LOCATION_LATITUDE + " TEXT, " + LOCATION_LONGITUDE + " TEXT);";
        db.execSQL(query);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    public void fillDatabaseFromInputFile(Context context)
    {
        SQLiteDatabase db = this.getWritableDatabase();

        String query = "SELECT count(*) FROM " + TABLE_NAME;
        Cursor cursor = db.rawQuery(query, null);
        cursor.moveToFirst();
        int row = cursor.getInt(0);
        cursor.close();

        if(row == 0)
        {
            try
            {
                InputStream inputStream = context.getResources().openRawResource(R.raw.input);
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                String line;

                while ((line = bufferedReader.readLine()) != null)
                {
                    String[] parts = line.split(",");
                    if (parts.length == 3)
                    {
                        String placeName = parts[0];
                        String latitude = parts[1];
                        String longitude = parts[2];

                        ContentValues contentValues = new ContentValues();
                        contentValues.put(LOCATION_NAME, placeName);
                        contentValues.put(LOCATION_LATITUDE, latitude);
                        contentValues.put(LOCATION_LONGITUDE, longitude);
                        db.insert(TABLE_NAME, null, contentValues);
                    }
                }
            }
            catch (IOException e)
            {e.printStackTrace();}

            db.close();
        }
    }

    public void updateListOfAddresses()
    {
        SQLiteDatabase db = this.getReadableDatabase();
        String[] columns = {LOCATION_LATITUDE, LOCATION_LONGITUDE};
        Cursor cursor = db.query(TABLE_NAME, columns, null,null,null,null,null);
        if(cursor != null)
        {
            while(cursor.moveToNext())
            {
                double latitude = cursor.getDouble(cursor.getColumnIndex(LOCATION_LATITUDE));
                double longitude = cursor.getDouble(cursor.getColumnIndex(LOCATION_LONGITUDE));
                String address = convertToAddress(latitude,longitude);
                if(address != null)
                    listOfAddresses.add(address);
            }
            cursor.close();
        }
    }

    private String convertToAddress(double latitude, double longitude)
    {
        try
        {
            List<Address> addresses = geocoder.getFromLocation(latitude,longitude,1);
            if(addresses != null && !addresses.isEmpty())
                return addresses.get(0).getAddressLine(0);

        }
        catch (IOException e)
        {e.printStackTrace();}

        return null;
    }

    public boolean stringContainsAddress(String placeName)
    {
        for (String fullAddress : listOfAddresses)
        {
            if(fullAddress.contains(placeName))
                return true;
        }
        return false;
    }

    public void addPlace(String placeName, String latitude, String longitude)
    {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues contentValues = new ContentValues();
        contentValues.put(LOCATION_NAME, placeName);
        contentValues.put(LOCATION_LATITUDE,latitude);
        contentValues.put(LOCATION_LONGITUDE,longitude);
        db.insert(TABLE_NAME,null,contentValues);

        db.close();
    }

    public void updatePlace(String placeName, double latitude, double longitude)
    {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues contentValues = new ContentValues();
        contentValues.put(LOCATION_NAME, placeName);
        contentValues.put(LOCATION_LATITUDE, String.valueOf(latitude));
        contentValues.put(LOCATION_LONGITUDE, String.valueOf(longitude));
        db.update(TABLE_NAME, contentValues, LOCATION_NAME + "=?", new String[]{placeName});

        db.close();
    }

    public void deletePlace(String placeName)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_NAME,LOCATION_NAME + "=?", new String[]{placeName});
    }

}