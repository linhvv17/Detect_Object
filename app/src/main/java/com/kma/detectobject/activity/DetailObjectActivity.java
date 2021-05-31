package com.kma.detectobject.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.kma.detectobject.R;
import com.kma.detectobject.database.DatabaseHandler;
import com.kma.detectobject.database.Item;
import com.kma.detectobject.translate_api;

import java.util.HashMap;
import java.util.Locale;
import java.util.Random;

public class DetailObjectActivity extends AppCompatActivity implements View.OnClickListener {

    int idItem;
    private DatabaseHandler databaseHandler;
    ImageView imageView;
    Button buttonSpeak;
    TextView textViewEnglish;
    TextView textViewMean;
    private TextToSpeech mTTS;
    private TextToSpeech myTTS;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_object);
        Intent intent = getIntent();
        idItem = intent.getIntExtra("idItem",1);
        databaseHandler = new DatabaseHandler(this);

        imageView = findViewById(R.id.img_view_detail);

        textViewEnglish = findViewById(R.id.tv_english_detail);
        textViewMean = findViewById(R.id.tv_mean_detail);
        buttonSpeak = findViewById(R.id.btn_speak_detail);
        buttonSpeak.setOnClickListener(this);

        mTTS = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS) {
                    int result = mTTS.setLanguage(Locale.ENGLISH);
                    if (result == TextToSpeech.LANG_MISSING_DATA
                            || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                        Log.e("TTS", "Language not supported");
                    } else {
                        buttonSpeak.setEnabled(true);
                    }
                } else {
                    Log.e("TTS", "Initialization failed");
                }
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        Item item = databaseHandler.getItem(idItem);
        textViewEnglish.setText(item.getName());
//        textViewMean.setText(item.getMean());
        Glide.with(this).load(item.getPath())
                .into(imageView);
        translate_api translate=new translate_api();
        translate.setOnTranslationCompleteListener(new translate_api.OnTranslationCompleteListener() {
            @Override
            public void onStartTranslation() {
                // here you can perform initial work before translated the text like displaying progress bar
            }

            @Override
            public void onCompleted(String text) {
                // "text" variable will give you the translated text
                textViewMean.setText(text);
//                textViewMean.setText(item.getMean());
            }

            @Override
            public void onError(Exception e) {

            }
        });
        translate.execute(""+item.getName(),"en","vi");

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_speak_detail:
//                buttonSpeak.setBackgroundResource(R.drawable.btn_pronounce);
//                speak();
                myTTS = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
                    @Override
                    public void onInit(int status) {
                        if(myTTS.getEngines().size() == 0){
                            Toast.makeText(DetailObjectActivity.this,"No Engines Installed",Toast.LENGTH_LONG).show();
                        }else{
                            if (status == TextToSpeech.SUCCESS){
                                ttsInitialized();
                            }
                        }
                    }
                });
                break;


            default:
                break;

        }
    }

    private void speak() {
        String text = textViewEnglish.getText().toString();
        mTTS.speak(text, TextToSpeech.QUEUE_FLUSH, null);

    }


    String mostRecentUtteranceID;
    private void ttsInitialized() {

        // *** set UtteranceProgressListener AFTER tts is initialized ***
        myTTS.setOnUtteranceProgressListener(new UtteranceProgressListener() {
            @Override
            public void onStart(String utteranceId) {


                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        buttonSpeak.setBackgroundResource(R.drawable.btn_pronounce);
                    }
                });

            }

            @Override
            // this method will always called from a background thread.
            public void onDone(String utteranceId) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        buttonSpeak.setBackgroundResource(R.drawable.btn_volume);
                    }
                });


                // only respond to the most recent utterance
                if (!utteranceId.equals(mostRecentUtteranceID)) {
                    Log.i("XXX", "onDone() blocked: utterance ID mismatch.");
                    return;
                } // else continue...

                boolean wasCalledFromBackgroundThread = (Thread.currentThread().getId() != 1);
                Log.i("XXX", "was onDone() called on a background thread? : " + wasCalledFromBackgroundThread);

                Log.i("XXX", "onDone working.");

                // for demonstration only... avoid references to
                // MainActivity (unless you use a WeakReference)
                // inside the onDone() method, as it
                // can cause a memory leak.
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // *** toast will not work if called from a background thread ***
//                        Toast.makeText(DetectActivity.this,"onDone working.",Toast.LENGTH_LONG).show();
                    }
                });
            }

            @Override
            public void onError(String utteranceId) {

            }
        });

        // set Language
        myTTS.setLanguage(Locale.ENGLISH);

        // set unique utterance ID for each utterance
        mostRecentUtteranceID = (new Random().nextInt() % 9999999) + ""; // "" is String force

        // set params
        // *** this method will work for more devices: API 19+ ***
        HashMap<String, String> params = new HashMap<>();
        params.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, mostRecentUtteranceID);

        myTTS.speak(textViewEnglish.getText().toString(),TextToSpeech.QUEUE_FLUSH,params);


    }


}