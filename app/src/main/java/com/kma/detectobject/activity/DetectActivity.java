package com.kma.detectobject.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.kma.detectobject.ConstantApp;
import com.kma.detectobject.R;
import com.kma.detectobject.api.DetectAPI;
import com.kma.detectobject.api.DetectResult;
import com.kma.detectobject.api.Detection;
import com.kma.detectobject.api.FlaskClient;
import com.kma.detectobject.api.ServiceGenerator;
import com.kma.detectobject.api.UploadResult;
import com.kma.detectobject.database.DatabaseHandler;
import com.kma.detectobject.database.Item;
import com.kma.detectobject.translate_api;

import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.regex.Matcher;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

import retrofit2.Call;

import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static android.util.Patterns.IP_ADDRESS;

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
    }


    String message = "How may I help you?";
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

    private void sendDataToServer(){

    }
    private void receiveDataFromServer(){
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.show();
        Log.e("RETROFIT","receiveDataFromServer");
        Retrofit.Builder builder = new Retrofit.Builder()
                .baseUrl(ConstantApp.DEFAULT_URL)
                .addConverterFactory(GsonConverterFactory.create());
        Retrofit retrofit = builder.build();
        retrofit.create(FlaskClient.class).getData()
                .enqueue(new Callback<Item>() {
                    @Override
                    public void onResponse(Call<Item> call, Response<Item> response) {
                        if (response.body()!=null){
                            progressDialog.dismiss();

                            Log.e("RETROFIT","get data success");
                        int  id = response.body().getId();
                        String  name = response.body().getName();
                        String  mean = response.body().getMean();
                        String  path = response.body().getPath();
//                        Toast.makeText(DetectActivity.this, ""+id  +" "+name + " "+mean + " "+path,Toast.LENGTH_LONG).show();
//                            Toast.makeText(DetectActivity.this, ""+response.body()
//                                    ,Toast.LENGTH_LONG).show();

                            textViewEnglish.setText(name);
                            textViewMean.setText(mean);
                            Glide.with(DetectActivity.this)
                                    .load(pathImageSendToServer)
                                    .into(imageView);

                            pathImage = pathImageSendToServer;
                        }

                    }

                    @Override
                    public void onFailure(Call<Item> call, Throwable t) {
                        Log.e("RETROFIT","onFailure" + t.getMessage());


                    }
                });
    }




    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_speak:
//                buttonSpeak.setBackgroundResource(R.drawable.btn_pronounce);
//                speak();
                myTTS = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
                    @Override
                    public void onInit(int status) {
                        if(myTTS.getEngines().size() == 0){
                            Toast.makeText(DetectActivity.this,"No Engines Installed",Toast.LENGTH_LONG).show();
                        }else{
                            if (status == TextToSpeech.SUCCESS){
                                ttsInitialized();
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

//    private void speak() {
//        String text = textViewEnglish.getText().toString();
////        float pitch = (float) mSeekBarPitch.getProgress() / 50;
//////        if (pitch < 0.1) pitch = 0.1f;
//////        float speed = (float) mSeekBarSpeed.getProgress() / 50;
//////        if (speed < 0.1) speed = 0.1f;
//////        mTTS.setPitch(pitch);
//////        mTTS.setSpeechRate(speed);
//        mTTS.speak(text, TextToSpeech.QUEUE_FLUSH, null);
//
//        if (mTTS.isSpeaking()) {
//            mTTS.stop();
//            buttonSpeak.setBackgroundResource(R.drawable.btn_volume);
//        } else {
//            mTTS.setOnUtteranceProgressListener(new OnUtteranceProgressListener() {
//                public onDone(String utteranceId) {
//                    buttonSpeak.setBackgroundResource(R.drawable.btn_volume);
//                }
//                void onError(String utteranceId) {
//                    buttonSpeak.setBackgroundResource(R.drawable.btn_pronounce);
//                }
//                void onStart(String utteranceId) { }
//            });
//
//            if (textViewEnglish.equals("")) {
////                speech("No text");
//                mTTS.speak("No text", TextToSpeech.QUEUE_FLUSH, null);
//            }
//            else {
//                mTTS.speak(text, TextToSpeech.QUEUE_FLUSH, null);
//            }
//        }
//
//
//    }

    @Override
    protected void onDestroy() {
        if (mTTS != null) {
            mTTS.stop();
            mTTS.shutdown();
        }
        super.onDestroy();
    }

    private void createDB() {
        SQLiteDatabase db = null;
        try {
            db = openOrCreateDatabase("mydb.db", Context.MODE_PRIVATE, null);
        }

        catch (SQLiteException ex) {
            //Lỗi kết nối
            Log.e("TagSQL", ex.getMessage());
        }
        // ... Các lệnh truy cấn đến DB ...
        // Khi không dùng đến kết nối, cần đóng lại
        db.close();
    }

    //true nếu bảng tồn tại
    private  boolean isTableExist(SQLiteDatabase db, String table) {
        Cursor cursor = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table' AND name=?", new String[]{table});
        boolean tableExist = (cursor.getCount() != 0);
        cursor.close();
        return tableExist;
    }




    public void connectServer(View v) {
//        TextView responseText = findViewById(R.id.responseText);
        if (pathImage == null) { // This means no image is selected and thus nothing to upload.
//            responseText.setText("No Image Selected to Upload. Select Image(s) and Try Again.");
            return;
        }
//        responseText.setText("Sending the Files. Please Wait ...");

//        EditText ipv4AddressView = findViewById(R.id.IPAddress);
//        String ipv4Address = ipv4AddressView.getText().toString();
//        EditText portNumberView = findViewById(R.id.portNumber);
//        String portNumber = portNumberView.getText().toString();

        Matcher matcher = IP_ADDRESS.matcher("localhost");
        if (!matcher.matches()) {
//            responseText.setText("Invalid IPv4 Address. Please Check Your Inputs.");
            return;
        }

        String postUrl = "http://" + "localhost" + ":" + 6868 + "/";

        MultipartBody.Builder multipartBodyBuilder = new MultipartBody.Builder().setType(MultipartBody.FORM);

//        for (int i = 0; i < selectedImagesPaths.size(); i++) {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inPreferredConfig = Bitmap.Config.RGB_565;

            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            try {
                // Read BitMap by file path.
                Bitmap bitmap = BitmapFactory.decodeFile(pathImage, options);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
            } catch (Exception e) {
//                responseText.setText("Please Make Sure the Selected File is an Image.");
                return;
            }
            byte[] byteArray = stream.toByteArray();

            multipartBodyBuilder.addFormDataPart("image" + pathImage, "Android_Flask_" + pathImage + ".jpg", RequestBody.create(MediaType.parse("image/*jpg"), byteArray));
//        }

        RequestBody postBodyImage = multipartBodyBuilder.build();

//        RequestBody postBodyImage = new MultipartBody.Builder()
//                .setType(MultipartBody.FORM)
//                .addFormDataPart("image", "androidFlask.jpg", RequestBody.create(MediaType.parse("image/*jpg"), byteArray))
//                .build();

        postRequest(postUrl, postBodyImage);
    }

    void postRequest(String postUrl, RequestBody postBody) {

        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url(postUrl)
                .post(postBody)
                .build();

        client.newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onResponse(@NotNull okhttp3.Call call, @NotNull okhttp3.Response response) throws IOException {

                // In order to access the TextView inside the UI thread, the code is executed inside runOnUiThread()
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
//                        TextView responseText = findViewById(R.id.responseText);
                        //                            responseText.setText("Server's Response\n" + response.body().string());
                    }
                });
            }

            @Override
            public void onFailure(@NotNull okhttp3.Call call, @NotNull IOException e) {
                // Cancel the post on failure.
                call.cancel();
                Log.d("FAIL", e.getMessage());

                // In order to access the TextView inside the UI thread, the code is executed inside runOnUiThread()
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
//                        TextView responseText = findViewById(R.id.responseText);
//                        responseText.setText("Failed to Connect to Server. Please Try Again.");
                    }
                });
            }

        });
    }

//    public void selectImage(View v) {
//        Intent intent = new Intent();
//        intent.setType("*/*");
//        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
//        intent.setAction(Intent.ACTION_GET_CONTENT);
//        startActivityForResult(Intent.createChooser(intent, "Select Picture"), SELECT_MULTIPLE_IMAGES);
//    }


    // Implementation of the getPath() method and all its requirements is taken from the StackOverflow Paul Burke's answer: https://stackoverflow.com/a/20559175/5426539
    public static String getPath(final Context context, final Uri uri) {

        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

        // DocumentProvider
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }

            }
            // DownloadsProvider
            else if (isDownloadsDocument(uri)) {

                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));

                return getDataColumn(context, contentUri, null, null);
            }
            // MediaProvider
            else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }

                final String selection = "_id=?";
                final String[] selectionArgs = new String[]{
                        split[1]
                };

                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        }
        // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {
            return getDataColumn(context, uri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }

        return null;
    }

    public static String getDataColumn(Context context, Uri uri, String selection,
                                       String[] selectionArgs) {

        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {
                column
        };

        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
                    null);
            if (cursor != null && cursor.moveToFirst()) {
                final int column_index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(column_index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }

    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }


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
        Call<DetectResult> callDetect = service.uploadDetectMultipleFiles(files);
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
    
    
    private void detectFileFromUpload(){
        OkHttpClient client = new OkHttpClient().newBuilder()
                .build();
        MediaType mediaType = MediaType.parse("text/plain");
        RequestBody body = new MultipartBody.Builder().setType(MultipartBody.FORM)
                .addFormDataPart("images","/F:/DOANTOTNGHIEP/Object-Detection-API-master/Object-Detection-API-master/data/images/dog.jpg",
                        RequestBody.create(MediaType.parse("application/octet-stream"),
                                new File("/F:/DOANTOTNGHIEP/Object-Detection-API-master/Object-Detection-API-master/data/images/dog.jpg")))
                .build();
        Request request = new Request.Builder()
                .url("http://localhost:5000/image")
                .method("POST", body)
                .build();

        new AsyncTask<Void, Void, okhttp3.Response>() {
            okhttp3.Response response;
            @Override
            protected okhttp3.Response doInBackground(Void... voids) {
                try {
                    response = client.newCall(request).execute();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return response;
            }

            @Override
            protected void onPostExecute(okhttp3.Response response) {
                super.onPostExecute(response);

                Log.e("onPostExecute",""+response.message());
            }


        }.execute();

    }

    private void detectFile(String path) {

        ProgressDialog pg = new ProgressDialog(this);
        pg.show();

        if(path==null) {
            Toast.makeText(this, "Can't choose pictures", Toast.LENGTH_SHORT).show();
            return;
        }
        Map<String, RequestBody> files = new HashMap<>();
        final FlaskClient service = ServiceGenerator.createService(FlaskClient.class);
//        for (int i = 0; i < imagesList.size(); i++) {
        File file = new File(path);
        files.put("file" + 0 + "\"; filename=\"" + file.getName(), RequestBody.create(MediaType.parse(getMimeType(pathImageSendToServer)), path));
//        files.put("file" + 0 + "\"; filename=\"" + file.getName(), RequestBody.create(MediaType.parse(getMimeType(pathImageSendToServer)), file));
//        }
        Call<DetectResult> call = service.detectFiles(files);////////////////////bugggggggggggggggggggggggggggggggggggggg

        call.enqueue(new retrofit2.Callback<DetectResult>() {
            @Override
            public void onResponse(Call<DetectResult> call, retrofit2.Response<DetectResult> response) {

                if (response.isSuccessful()) {
                    pg.dismiss();
                    Toast.makeText(DetectActivity.this, "detectFile success", Toast.LENGTH_SHORT).show();
//                    Log.i("orzangleli", "---------------------Upload success -----------------------");
//                    Log.i("orzangleli", "The base address is:" + ServiceGenerator.API_BASE_URL);
////                    Log.i("orzangleli", "The relative address of the picture is:" + response.body().image_urls);
//                    Log.i("orzangleli", "---------------------END-----------------------");
//                    //goi detect o day
//
////                    Log.e("data",""+response.body().getResponse());
////                    detectFile("/F:/DOANTOTNGHIEP/Object-Detection-API-master/Object-Detection-API-master/data/images/dog.jpg");
//                    List<com.kma.detectobject.api.Response> response1 = response.body().getResponse();
//                    Detection detection = (Detection) response1.get(0).getDetections();
//                    String cls = detection.getClass_();
//                    float confidence  = detection.getConfidence();
//                    Log.e("data",""+cls + "&&"+ confidence);
                }

            }

            @Override
            public void onFailure(Call<DetectResult> call, Throwable t) {

                Toast.makeText(DetectActivity.this, "detect failed", Toast.LENGTH_SHORT).show();

            }
        });

    }


    public static String getMimeType(String url) {
        String type = null;
        String extension = MimeTypeMap.getFileExtensionFromUrl(url);
        if (extension != null) {
            type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
        }
        return type;
    }




}