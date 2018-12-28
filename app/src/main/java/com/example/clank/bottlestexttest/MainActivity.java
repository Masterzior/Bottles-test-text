package com.example.clank.bottlestexttest;

import android.app.Activity;
import android.app.Notification;
import android.content.Intent;
import android.provider.DocumentsProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;

import org.json.JSONArray;
import org.json.JSONException;

public class MainActivity extends AppCompatActivity {
    public static final String EXTRA_MESSAGE = "com.example.myfirstapp.MESSAGE";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }
    public void onClickButton(View view){
        EditText product = findViewById(R.id.product_text);
        EditText dosage = findViewById(R.id.dosage_text);
        String message = product.getText().toString() +" "+ dosage.getText().toString();
        RequestQueue queue = Volley.newRequestQueue(this);
        String url="http://213.66.251.184/Bottles/BottlesService.asmx/FirstSearch?name="+product.getText().toString()+"&strength="+dosage.getText().toString()+"&language=sv&fbclid=IwAR00DSzecqYioxMBf3h53q42YNhFrjCbpfjE1BWDGsPg3yZkCqQqg3nxWko";
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONArray reader = new JSONArray(response); //TODO Fel vi får tillbaka en array
                            DisplayDrug(reader);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        // Display the first 500 characters of the response string.
 //                       mTextView.setText("Response is: "+ response.substring(0,500));
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
//                mTextView.setText("That didn't work!");
            }
        });

// Add the request to the RequestQueue.
        queue.add(stringRequest);


    }
    public void DisplayDrug(JSONArray drugs)
    {
        try {
            String drug = drugs.getJSONObject(0).toString(2);
            Intent intent = new Intent(this, Main2Activity.class);
            intent.putExtra(EXTRA_MESSAGE,drug);
            startActivity(intent);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    public void Chooseimage(View view){
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivity(intent);
//        FirebaseVisionImage image;
//        try {
//            image = FirebaseVisionImage.fromFilePath(this, uri);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
    }
}
