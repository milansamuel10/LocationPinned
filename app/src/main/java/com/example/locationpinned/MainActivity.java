package com.example.locationpinned;

import androidx.appcompat.app.AppCompatActivity;

import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    EditText addressToLookup, placeNameEntry;
    Button query,delete,update,add;
    TextView coordinatesResult, operationResult;
    DBHelper database;
    String address;
    Geocoder geocoder;
    List<Address> addresses;
    double latitude, longitude;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setSupportActionBar(findViewById(R.id.header));
        placeNameEntry = findViewById(R.id.nameOfPlace);
        add = findViewById(R.id.addPlaceButton);
        delete = findViewById(R.id.deletePlaceButton);
        update = findViewById(R.id.updatePlaceButton);
        operationResult = findViewById(R.id.updateResult);
        addressToLookup = findViewById(R.id.lookupAddress);
        query = findViewById(R.id.queryButton);
        coordinatesResult = findViewById(R.id.coordinatesResult);


        //calls the function to populate DB with locations.txt values
        database = new DBHelper(this);
        database.fillDatabaseFromInputFile(this);
        database.updateListOfAddresses(); //function to populate the arraylist with addresses of the coordinates in DB

        geocoder = new Geocoder(this);

        query.setOnClickListener(v -> {
            String userInput = addressToLookup.getText().toString();
            address = userInput;

            if(getCoordinatesFromEnteredAddress())
            {
                if(!database.stringContainsAddress(userInput))
                    coordinatesResult.setText("Road address does not exist in the database.");
            }
        });

        delete.setOnClickListener(v -> {
            String userInput = placeNameEntry.getText().toString();
            if(!userInput.isEmpty())
            {
                address = userInput;
                if(getCoordinatesFromEnteredAddress())
                {
                    if(database.stringContainsAddress(userInput))
                    {
                        database.deletePlace(userInput);
                        operationResult.setText(userInput + " has been deleted from the database.");
                        database.updateListOfAddresses();
                    }
                }
            }
            else
                Toast.makeText(v.getContext(),"Please enter an address if you wish to delete.",Toast.LENGTH_SHORT).show();
        });

        update.setOnClickListener(v -> {
            String userInput = placeNameEntry.getText().toString();

            if(!userInput.isEmpty())
            {
                address = userInput;
                if(getCoordinatesFromEnteredAddress())
                {
                    if(database.stringContainsAddress(userInput))
                    {
                        database.updatePlace(userInput,latitude,longitude);
                        operationResult.setText(userInput + " has had its co-ordinates updated in the database.");
                        database.updateListOfAddresses();
                    }
                    else
                        operationResult.setText("Address does not exist in the database.");
                }

            } else
                Toast.makeText(v.getContext(),"Please enter an address if you wish to update its co-ordinates.",Toast.LENGTH_SHORT).show();
        });

        add.setOnClickListener(v -> {
            String userInput = placeNameEntry.getText().toString();
            address = userInput;
            if(!userInput.isEmpty())
            {
                if(getCoordinatesFromEnteredAddress())
                {
                    database.addPlace(userInput,String.valueOf(latitude), String.valueOf(longitude));
                    operationResult.setText(userInput + " has been added to the database.");
                    database.updateListOfAddresses();
                }
            } else
                Toast.makeText(v.getContext(),"Please enter an address if you wish to add.",Toast.LENGTH_SHORT).show();
        });
    }

    public boolean getCoordinatesFromEnteredAddress()
    {
        try
        {
            addresses = geocoder.getFromLocationName(address,1);
            if (addresses != null && !addresses.isEmpty())
            {
                latitude = addresses.get(0).getLatitude();
                longitude = addresses.get(0).getLongitude();
                coordinatesResult.setText("Latitude: " + latitude + ", Longitude: " + longitude);
                return true;
            }
            else
                coordinatesResult.setText("Co-ordinates not found for the given invalid text.");

        }
        catch (IOException e)
        {e.printStackTrace();}

        return false;
    }
}