package com.example.geocodingapi;

import static android.content.ContentValues.TAG;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.content.Intent;
import android.net.Uri;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import android.util.Log;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private EditText searchBox;
    private ListView searchResults;
    private ArrayAdapter<String> adapter;
    private ArrayList<String> searchResultAdapter;
    private RequestQueue requestQueue;
    // I've reached the rate limit of the API key
    private static final String API_KEY = "API key goes here";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        searchBox = findViewById(R.id.EditText_searchBox);
        searchResults = findViewById(R.id.ListView_searchResults);
        searchResultAdapter = new ArrayList<>();
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, searchResultAdapter);
        searchResults.setAdapter(adapter);

        requestQueue = Volley.newRequestQueue(this);

        searchBox.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                String query = charSequence.toString();
                if (query.isEmpty()) {
                    searchResultAdapter.clear();
                    adapter.notifyDataSetChanged();
                } else {
                    searchAddress(query);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
                searchAddress(editable.toString());
            }
        });
        searchResults.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String address = searchResultAdapter.get(i);
                openGoogleMaps(address);
            }
        });
    }

    private void searchAddress(String query) {
        try {
            String encodedQuery = URLEncoder.encode(query, "UTF-8");
            String url = "https://geocode.search.hereapi.com/v1/geocode?q=" + encodedQuery + "&apiKey=" + API_KEY;
            StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                    response -> {
                        searchResultAdapter.clear();
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            JSONArray items = jsonObject.getJSONArray("items");
                            for (int i = 0; i < items.length(); i++) {
                                JSONObject item = items.getJSONObject(i);
                                String address = item.getJSONObject("address").getString("label");
                                address = new String(address.getBytes("ISO-8859-1"), "UTF-8");
                                searchResultAdapter.add(address);
                            }
                            adapter.notifyDataSetChanged();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    },
                    error -> error.printStackTrace()
            );
            requestQueue.add(stringRequest);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    private void openGoogleMaps(String address) {
        Uri gmmIntentUri = Uri.parse("geo:0,0?q=" + Uri.encode(address));
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
        mapIntent.setPackage("com.google.android.apps.maps");

        Log.d(TAG, "Opening Google Maps for address: " + address);

        if (mapIntent.resolveActivity(getPackageManager()) != null) {
            startActivity(mapIntent);
            Log.d(TAG, "Google Maps opened successfully.");
        } else {
            Toast.makeText(this, "Google Maps app is not installed", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Google Maps app is not installed");
        }
    }
}