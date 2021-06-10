
package com.kma.detectobject.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Point;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.gson.Gson;
import com.kma.detectobject.R;
import com.kma.detectobject.api.DetectResult;
import com.kma.detectobject.api.FlaskClient;
import com.kma.detectobject.api.ServiceGenerator;
import com.kma.detectobject.database.DatabaseHandler;
import com.kma.detectobject.database.Item;
import com.kma.detectobject.search_more.DeviceData;
import com.kma.detectobject.search_more.FlickrClient;
import com.kma.detectobject.search_more.FlickrModel;
import com.kma.detectobject.search_more.FlickrServices;
import com.kma.detectobject.search_more.ItemFlick;
import com.kma.detectobject.translate_api;
import com.mikepenz.fastadapter.commons.adapters.FastItemAdapter;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import okhttp3.MediaType;
import okhttp3.RequestBody;

import retrofit2.Call;

public class DetectActivity extends AppCompatActivity implements View.OnClickListener{
    ImageView imageView;
    Intent intent;
    Button buttonSpeak;
    Button buttonSave;
    TextView textViewEnglish;
    TextView textViewMean;
    private TextToSpeech mTTS;
    private DatabaseHandler databaseHandler;
    private String pathImage;

    private String url = "http://192.168.0.103:5000/";
    private TextToSpeech myTTS;
    private String pathImageSendToServer;

    //

    RecyclerView recycler;
    Context context;
    EditText searchKey;
    ImageView fullScreenImg;
    String keyword;
    String oldKeyword;
    ProgressBar progressBar;
    Disposable disposable;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detect);
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int width = size.x;
        int height = size.y;
        intent = getIntent();
//        pathImage = intent.getStringExtra("BitmapImagePath");
//        Bitmap bitmap = (Bitmap) intent.getParcelableExtra("BitmapImage");
        imageView = findViewById(R.id.image_view_detect);
        ViewGroup.LayoutParams layoutParams = imageView.getLayoutParams();
        layoutParams.width = width;
        layoutParams.height = height/2;
        imageView.setLayoutParams(layoutParams);
//        if (pathImage!=null){
//            imageView.setImageBitmap(BitmapFactory.decodeFile(pathImage));
//        }
//        if (bitmap!=null){
//            imageView.setImageBitmap(bitmap);
//        }
        buttonSpeak = findViewById(R.id.btn_speak);
        buttonSpeak.setOnClickListener(this);
        buttonSave = findViewById(R.id.btn_save_result);
        buttonSave.setOnClickListener(this);


        textViewEnglish = findViewById(R.id.tv_english);
        textViewMean = findViewById(R.id.tv_mean);




//        mTTS = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
//            @Override
//            public void onInit(int status) {
//                if (status == TextToSpeech.SUCCESS) {
//                    int result = mTTS.setLanguage(Locale.ENGLISH);
//                    if (result == TextToSpeech.LANG_MISSING_DATA
//                            || result == TextToSpeech.LANG_NOT_SUPPORTED) {
//                        Log.e("TTS", "Language not supported");
//                    } else {
//                        buttonSpeak.setEnabled(true);
//                    }
//                } else {
//                    Log.e("TTS", "Initialization failed");
//                }
//            }
//        });

//        createDB();

        databaseHandler = new DatabaseHandler(this);

//        receiveDataFromServer();
        pathImageSendToServer = intent.getStringExtra("pathImage");
        Log.e("pathImageSendToServer",""+pathImageSendToServer);
        if (pathImageSendToServer!=null){
            uploadFiles(pathImageSendToServer);
        }


        //
        context = this;
        recycler = findViewById(R.id.recycler);
        searchKey = findViewById(R.id.keyword);
        fullScreenImg = findViewById(R.id.fullScreenImg);
        progressBar = findViewById(R.id.progressBar);
        DeviceData.getInstance().setDisplay(getWindowManager().getDefaultDisplay());

        searchKey.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                progressBar.setVisibility(View.VISIBLE);
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                keyword = s.toString();
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (!keyword.isEmpty() && !keyword.substring(keyword.length() - 1).equals(" ") && !keyword.equals(oldKeyword)) {
                    getImages();
                    oldKeyword = keyword;
                }
            }
        });

        searchKey.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                hideKeyboard();
                return true;
            }
            return false;
        });

    }


    String message = "How may I help you?";
    String mostRecentUtteranceID;

    private void speakEnglish() {
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
            public void onDone(String utteranceId) {

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        buttonSpeak.setBackgroundResource(R.drawable.btn_volume);
                    }
                });
                // only respond to the most recent utterance
                if (!utteranceId.equals(mostRecentUtteranceID)) {
                    return;
                } // else continue...
                boolean wasCalledFromBackgroundThread = (Thread.currentThread().getId() != 1);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
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
        myTTS.speak(textViewEnglish.getText().toString(), TextToSpeech.QUEUE_FLUSH, params);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_speak:
                myTTS = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
                    @Override
                    public void onInit(int status) {
                        if(myTTS.getEngines().size() == 0){

                        }else{
                            if (status == TextToSpeech.SUCCESS){
                                speakEnglish();
                            }
                        }
                    }
                });
                break;

            case R.id.btn_save_result:
                Toast.makeText(this, "SAVE",Toast.LENGTH_LONG).show();
                addResultToDatabase();
                break;

            default:
                break;
        }

    }

    private void addResultToDatabase() {
        Item item = new Item(textViewEnglish.getText().toString(),textViewMean.getText().toString(),pathImageSendToServer);
        databaseHandler.addItem(item);
        databaseHandler.updateItem(item);
    }

//    @Override
//    protected void onDestroy() {
//        if (mTTS != null) {
//            mTTS.stop();
//            mTTS.shutdown();
//        }
//        super.onDestroy();
//    }


    public void uploadFiles(String path) {
        ProgressDialog pg = new ProgressDialog(this);
        pg.show();
        if (path == null) {
            return;
        }
        Map<String, RequestBody> files = new HashMap<>();
        final FlaskClient service = ServiceGenerator.createService(FlaskClient.class);
        File file = new File(path);
        files.put("file" + 0 + "\"; filename=\"" + file.getName(), RequestBody.create(MediaType.parse(getMimeType(pathImageSendToServer)), file));
        Call<DetectResult> callDetect = service.uploadDetectFiles(files);

        callDetect.enqueue(new retrofit2.Callback<DetectResult>() {
            @Override
            public void onResponse(Call<DetectResult> call, retrofit2.Response<DetectResult> response) {
                if (response.isSuccessful() && response != null) {
                    for (int i = 0; i < response.body().getResponse().size(); i++) {
                        com.kma.detectobject.api.Response response1 = response.body().getResponse().get(i);
                        textViewEnglish.setText("" + response1.getDetections().get(0).getClass_());
                        Glide.with(DetectActivity.this)
                                .load(pathImageSendToServer)
                                .into(imageView);
                        //dich sang tieng viet
                        translate_api translate = new translate_api();
                        translate.setOnTranslationCompleteListener(new translate_api.OnTranslationCompleteListener() {
                            @Override
                            public void onStartTranslation() {
                            }
                            @Override
                            public void onCompleted(String text) {
                                textViewMean.setText(text);
                            }
                            @Override
                            public void onError(Exception e) {
                            }
                        });
                        translate.execute("" + response1.getDetections().get(0).getClass_(), "en", "vi");
                    }
                    pg.dismiss();
                }
            }
            @Override
            public void onFailure(Call<DetectResult> call, Throwable t) {
            }
        });
    }
    
    
//    private void detectFileFromUpload(){
//        OkHttpClient client = new OkHttpClient().newBuilder()
//                .build();
//        MediaType mediaType = MediaType.parse("text/plain");
//        RequestBody body = new MultipartBody.Builder().setType(MultipartBody.FORM)
//                .addFormDataPart("images","/F:/DOANTOTNGHIEP/Object-Detection-API-master/Object-Detection-API-master/data/images/dog.jpg",
//                        RequestBody.create(MediaType.parse("application/octet-stream"),
//                                new File("/F:/DOANTOTNGHIEP/Object-Detection-API-master/Object-Detection-API-master/data/images/dog.jpg")))
//                .build();
//        Request request = new Request.Builder()
//                .url("http://localhost:5000/image")
//                .method("POST", body)
//                .build();
//
//        new AsyncTask<Void, Void, okhttp3.Response>() {
//            okhttp3.Response response;
//            @Override
//            protected okhttp3.Response doInBackground(Void... voids) {
//                try {
//                    response = client.newCall(request).execute();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//                return response;
//            }
//
//            @Override
//            protected void onPostExecute(okhttp3.Response response) {
//                super.onPostExecute(response);
//
//                Log.e("onPostExecute",""+response.message());
//            }
//
//
//        }.execute();
//
//    }
//
//    private void detectFile(String path) {
//
//        ProgressDialog pg = new ProgressDialog(this);
//        pg.show();
//
//        if(path==null) {
//            Toast.makeText(this, "Can't choose pictures", Toast.LENGTH_SHORT).show();
//            return;
//        }
//        Map<String, RequestBody> files = new HashMap<>();
//        final FlaskClient service = ServiceGenerator.createService(FlaskClient.class);
////        for (int i = 0; i < imagesList.size(); i++) {
//        File file = new File(path);
//        files.put("file" + 0 + "\"; filename=\"" + file.getName(), RequestBody.create(MediaType.parse(getMimeType(pathImageSendToServer)), path));
////        files.put("file" + 0 + "\"; filename=\"" + file.getName(), RequestBody.create(MediaType.parse(getMimeType(pathImageSendToServer)), file));
////        }
//        Call<DetectResult> call = service.detectFiles(files);////////////////////bugggggggggggggggggggggggggggggggggggggg
//
//        call.enqueue(new retrofit2.Callback<DetectResult>() {
//            @Override
//            public void onResponse(Call<DetectResult> call, retrofit2.Response<DetectResult> response) {
//
//                if (response.isSuccessful()) {
//                    pg.dismiss();
//                    Toast.makeText(DetectActivity.this, "detectFile success", Toast.LENGTH_SHORT).show();
////                    Log.i("orzangleli", "---------------------Upload success -----------------------");
////                    Log.i("orzangleli", "The base address is:" + ServiceGenerator.API_BASE_URL);
//////                    Log.i("orzangleli", "The relative address of the picture is:" + response.body().image_urls);
////                    Log.i("orzangleli", "---------------------END-----------------------");
////                    //goi detect o day
////
//////                    Log.e("data",""+response.body().getResponse());
//////                    detectFile("/F:/DOANTOTNGHIEP/Object-Detection-API-master/Object-Detection-API-master/data/images/dog.jpg");
////                    List<com.kma.detectobject.api.Response> response1 = response.body().getResponse();
////                    Detection detection = (Detection) response1.get(0).getDetections();
////                    String cls = detection.getClass_();
////                    float confidence  = detection.getConfidence();
////                    Log.e("data",""+cls + "&&"+ confidence);
//                }
//
//            }
//
//            @Override
//            public void onFailure(Call<DetectResult> call, Throwable t) {
//
//                Toast.makeText(DetectActivity.this, "detect failed", Toast.LENGTH_SHORT).show();
//
//            }
//        });
//
//    }


    public static String getMimeType(String url) {
        String type = null;
        String extension = MimeTypeMap.getFileExtensionFromUrl(url);
        if (extension != null) {
            type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
        }
        return type;
    }

    private void getImages() {
        final FlickrServices apiService = FlickrClient.getInstance().create(FlickrServices.class);
        recycler.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);

        disposable = apiService.requestForPosts(keyword)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map(jsonString -> {
                    String json = jsonString.substring(15, jsonString.length() - 1);
                    FlickrModel flickrModel = new Gson().fromJson(json, FlickrModel.class);
                    return flickrModel.itemFlicks;
                })
                .subscribe(items -> {
                    FastItemAdapter<ItemFlick> fastAdapter = new FastItemAdapter<>();
                    fastAdapter.add(items);
                    fastAdapter.withSelectable(true);
                    fastAdapter.withOnClickListener((v, adapter, item, position) -> {
                        try {
                            getSupportActionBar().hide();
                        } catch (NullPointerException npe) {
                            npe.printStackTrace();
                        }
                        fullScreenImg.setVisibility(View.VISIBLE);
                        String url = item.media.m.replace("_m.jpg", "_b.jpg");
                        Picasso.get().load(url).into(fullScreenImg);
                        System.out.println("image " + url);
                        return false;
                    });

                    RecyclerView.LayoutManager layoutManager = new GridLayoutManager(DetectActivity.this, 2);
                    recycler.setLayoutManager(layoutManager);
                    recycler.setAdapter(fastAdapter);
                    recycler.setVisibility(View.VISIBLE);
                    progressBar.setVisibility(View.GONE);
                }, throwable -> {
                    recycler.setVisibility(View.VISIBLE);
                    progressBar.setVisibility(View.GONE);
                    System.out.println(throwable.getMessage());
                }, () -> {
                    recycler.setVisibility(View.VISIBLE);
                    progressBar.setVisibility(View.GONE);
                });
    }

    private void hideKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    public void hideFullScreenImg(View view) {
        view.setVisibility(View.GONE);
        try {
            getSupportActionBar().show();
        } catch (NullPointerException npe) {
            npe.printStackTrace();
        }
    }

    @Override
    public void onBackPressed() {
        if (fullScreenImg.getVisibility() == View.VISIBLE) {
            fullScreenImg.setVisibility(View.GONE);
            try {
                getSupportActionBar().show();
            } catch (NullPointerException npe) {
                npe.printStackTrace();
            }
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onDestroy() {
        disposable.dispose();
        if (mTTS != null) {
            mTTS.stop();
            mTTS.shutdown();
        }
        super.onDestroy();
    }




}