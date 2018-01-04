package com.example.root.myapplication;

import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
//import android.graphics.Region;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.microsoft.projectoxford.vision.VisionServiceClient;
import com.microsoft.projectoxford.vision.VisionServiceRestClient;
import com.microsoft.projectoxford.vision.contract.LanguageCodes;
import com.microsoft.projectoxford.vision.contract.Line;
import com.microsoft.projectoxford.vision.contract.OCR;
import com.microsoft.projectoxford.vision.contract.Word;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
//import java.util.stream.Stream;

public class MainActivity extends AppCompatActivity {
    private VisionServiceClient visionServiceClient = new VisionServiceRestClient("05f8de02106548489e664a58f0fe4c79","https://westcentralus.api.cognitive.microsoft.com/vision/v1.0");

    public static final String TAG="MAinActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Bitmap mBitmap = BitmapFactory.decodeResource(getResources(),R.drawable.kitten_cuteness_1);
        ImageView imageView = (ImageView)findViewById(R.id.image);
        imageView.setImageResource(R.drawable.kitten_cuteness_1);
        Button btnProcess = (Button)findViewById(R.id.btnProcess);
//        TextView textView = (TextView)findViewById(R.id.txtDescription);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        mBitmap.compress(Bitmap.CompressFormat.JPEG,100,outputStream);
        final ByteArrayInputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());
        btnProcess.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick: clicked");
                AsyncTask<InputStream, String,String> recognizeTextTask = new AsyncTask<InputStream, String, String>() {
                    ProgressDialog mDialog =  new ProgressDialog(MainActivity.this);
                    @Override
                    protected String doInBackground(InputStream... inputStreams) {
//                        return null;
                        try {
                            publishProgress("Recognizing..,");
                            Log.d(TAG, "doInBackground: background");
                            OCR ocr = visionServiceClient.recognizeText(inputStreams[0], LanguageCodes.English,true);
                            String result =  new Gson().toJson(ocr);
                            return result;
                        }catch (Exception e){
                            Log.d(TAG, "doInBackground: fail  "+e.getMessage());
                            return  null;
                        }
                    }
                    @Override
                    protected void onPreExecute() {
                        Log.d(TAG, "onPreExecute: preAsync");
                        mDialog.show();
                    }

                    @Override
                    protected void onPostExecute(String s) {
                        Log.d(TAG, "onPostExecute: postAsync");
//                        super.onPostExecute(s);
                        mDialog.dismiss();
                        OCR ocr  = new Gson().fromJson(s,OCR.class);
                        TextView txtDescription = (TextView)findViewById(R.id.txtDescription);
                        StringBuilder stringBuilder = new StringBuilder();
                        Log.d(TAG, "onPostExecute: "+ocr.regions.size());
                        for(com.microsoft.projectoxford.vision.contract.Region region:ocr.regions){

                            for (Line line:region.lines ) {
                                for (Word word : line.words)
                                    stringBuilder.append(word.text + " ");
                                stringBuilder.append("\n");
                            }
                            stringBuilder.append("\n\n");
                        }
                        txtDescription.setText(stringBuilder);
                    }
                    @Override
                    protected void onProgressUpdate(String... values) {
                        Log.d(TAG, "onProgressUpdate: progess");
//                        super.onProgressUpdate(values);
                        mDialog.setMessage(values[0]);
                    }
                };
                recognizeTextTask.execute(inputStream);
            }
        });
    }
}

