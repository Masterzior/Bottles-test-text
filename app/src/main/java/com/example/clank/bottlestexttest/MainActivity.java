package com.example.clank.bottlestexttest;

import android.Manifest;
import android.app.Activity;
import android.app.Notification;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.Image;
import android.net.Uri;
import android.provider.DocumentsProvider;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.TextureView;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
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
    public static final String EXTRA_MESSAGE = "com.example.myfirstapp.MESSAGE"; //Dont know what this does tbh
    private static final int PICK_IMAGE = 1;

    private CameraHandler cameraHandler;


    CameraHandler.OnTextRecognizedListener onTextRecognizedListener = new CameraHandler.OnTextRecognizedListener() {
        @Override
        public void onTextRecognized(FirebaseVisionDocumentText text) {

            //TODO Work with Firebasetext object here?
           /* Toast t = Toast.makeText(getApplicationContext(),text.getText(), Toast.LENGTH_LONG);
            t.setGravity(Gravity.TOP,0,0);
            t.show();*/
            getImageStrings(text);
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FirebaseApp.initializeApp(this);
        setContentView(R.layout.activity_main);

        ActivityCompat.requestPermissions(this,new String[]
                {Manifest.permission.CAMERA,Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
    }

    @Override
    protected void onStop() {
        super.onStop();

        if(cameraHandler != null) {
            cameraHandler.closeCamera();
            cameraHandler = null;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        if(cameraHandler != null) {
            cameraHandler.closeCamera();
            cameraHandler = null;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        cameraHandler = new CameraHandler(this, (TextureView) findViewById(R.id.previewWindow),(ImageButton)findViewById(R.id.snapshotBtn));

        cameraHandler.setOnTextRecognizedListener(onTextRecognizedListener);
    }

/*    public void onClickButton(View view){
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


    }*/
    public void DisplayDrug(JSONObject drugs)
    {
        try {
            String drug = drugs.toString(2); //tostring(2) is a formater for the string. //TODO should extract the relevant text and cleanup the object before displaying it. Right now it looks bad
            Intent intent = new Intent(this, Main2Activity.class); // Creates and intent to show the info
            intent.putExtra(EXTRA_MESSAGE,drug); // put text into
            startActivity(intent); // start the activity which contains the drug information.
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    public void getImageStrings(FirebaseVisionDocumentText result){
        RequestQueue queue = Volley.newRequestQueue(this);
        final CloudLabelManipulator Apistr = new CloudLabelManipulator(result); // Creates a cloudlabelmanipulator object out of the result from the firebasedocumenttext object we made earlier. we use functions in this class to find relevant text
        String url="http://213.66.251.184/Bottles/BottlesService.asmx/FirstSearch?name="+Apistr.getFirstStr()+"&strength="+Apistr.getDosage()+"&language=sv&fbclid=IwAR00DSzecqYioxMBf3h53q42YNhFrjCbpfjE1BWDGsPg3yZkCqQqg3nxWko";
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        if (response.equals("[]")){  // The API we have sends this if there is nothing to fetch so this is the same as 404
                            Toast.makeText(getApplicationContext(), "Error With Image", Toast.LENGTH_SHORT).show(); //TODO Better Error Handling please.
                            return;
                        }
                        try {
                            JSONArray reader = new JSONArray(response); // Makes a reader of the response we got from the API
                            DisplayDrug(Apistr.getDrug(reader)); //Uses the function inside apistr to get the drugs out of it.
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
/*    public void Chooseimage(View view){
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, "Select Picture"),PICK_IMAGE); // Starts an activity for choosing an image and tries to fetch URI of the image. See below for result handling
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE && resultCode == RESULT_OK && data != null && data.getData() != null) {

            Uri uri = data.getData(); // Gets the URI of the chosen image
            FirebaseVisionImage image; // creates a FirebasevisionIMAGE object called image. we will manipulate this object into a FirebasevisionDOCUMENTTEXT object.
            try {
                image = FirebaseVisionImage.fromFilePath(this, uri); // Creates a firebaseVisionimage object. This is reduntant from above and should be fixed.
                FirebaseVisionDocumentTextRecognizer detector = FirebaseVision.getInstance()
                        .getCloudDocumentTextRecognizer();
                detector.processImage(image)
                        .addOnSuccessListener(new OnSuccessListener<FirebaseVisionDocumentText>() { // Calls the api to get a firebasevisiondocument from the image we sent to the api.
                            @Override
                            public void onSuccess(FirebaseVisionDocumentText result) {
                            getImageStrings(result); // Here we extracts the relevant information out of the object.
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
    }*/

    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 1: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }
            // other 'case' lines to check for other
            // permissions this app might request
        }
    }
}
