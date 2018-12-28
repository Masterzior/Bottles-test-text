package com.example.clank.bottlestexttest;

import android.app.Activity;
import android.app.Notification;
import android.content.Intent;
import android.media.Image;
import android.net.Uri;
import android.provider.DocumentsProvider;
import android.support.annotation.NonNull;
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
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.document.FirebaseVisionDocumentText;
import com.google.firebase.ml.vision.document.FirebaseVisionDocumentTextRecognizer;
import com.google.firebase.ml.vision.text.FirebaseVisionText;
import com.google.firebase.ml.vision.text.FirebaseVisionTextRecognizer;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {
    public static final String EXTRA_MESSAGE = "com.example.myfirstapp.MESSAGE";
    private static final int PICK_IMAGE = 1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FirebaseApp.initializeApp(this);
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

                            DisplayDrug(reader.getJSONObject(0));
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
    public void DisplayDrug(JSONObject drugs)
    {
        try {
            String drug = drugs.toString(2);
            Intent intent = new Intent(this, Main2Activity.class);
            intent.putExtra(EXTRA_MESSAGE,drug);
            startActivity(intent);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    public void getImageStrings(FirebaseVisionDocumentText result){
        RequestQueue queue = Volley.newRequestQueue(this);
        final CloudLabelManipulator Apistr = new CloudLabelManipulator(result);
        String url="http://213.66.251.184/Bottles/BottlesService.asmx/FirstSearch?name="+Apistr.getFirstStr()+"&strength="+Apistr.getDosage()+"&language=sv&fbclid=IwAR00DSzecqYioxMBf3h53q42YNhFrjCbpfjE1BWDGsPg3yZkCqQqg3nxWko";
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        if (response.equals("[]")){
                            Toast.makeText(getApplicationContext(), "Error With Image", Toast.LENGTH_SHORT).show(); //TODO Better Error Handling please.
                            return;
                        }
                        try {
                            JSONArray reader = new JSONArray(response); //TODO Fel vi får tillbaka en array
                            DisplayDrug(Apistr.getDrug(reader));
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
    public void Chooseimage(View view){
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, "Select Picture"),PICK_IMAGE);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE && resultCode == RESULT_OK && data != null && data.getData() != null) {

            Uri uri = data.getData();
            FirebaseVisionImage image;
            try {
                image = FirebaseVisionImage.fromFilePath(this, uri);
                FirebaseVisionDocumentTextRecognizer detector = FirebaseVision.getInstance()
                        .getCloudDocumentTextRecognizer();
                detector.processImage(image)
                        .addOnSuccessListener(new OnSuccessListener<FirebaseVisionDocumentText>() {
                            @Override
                            public void onSuccess(FirebaseVisionDocumentText result) {
                            getImageStrings(result);
                                // Task completed successfully
                                // ...
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                // Task failed with an exception
                                // ...
                            }
                        });
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }
}
