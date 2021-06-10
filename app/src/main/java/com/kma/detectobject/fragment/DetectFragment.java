package com.kma.detectobject.fragment;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.ImageView;
import android.widget.Toast;

import com.kma.detectobject.BuildConfig;
import com.kma.detectobject.R;
import com.kma.detectobject.activity.DetectActivity;
import com.kma.detectobject.api.FlaskClient;
import com.kma.detectobject.api.ServiceGenerator;
import com.kma.detectobject.api.UploadResult;

import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import retrofit2.Call;

import static android.app.Activity.RESULT_OK;

public class DetectFragment extends Fragment implements View.OnClickListener {

    static final int REQUEST_IMAGE_CAPTURE = 100;
    private static final int REQUEST_IMAGE_UPLOAD = 101;

    private ImageView imageViewCamera, imageViewGallery;

    public static int count = 0;
    private String dir;


    private String pathImageSendToServer;


    private static final Pattern IP_ADDRESS
            = Pattern.compile(
            "((25[0-5]|2[0-4][0-9]|[0-1][0-9]{2}|[1-9][0-9]|[1-9])\\.(25[0-5]|2[0-4]"
                    + "[0-9]|[0-1][0-9]{2}|[1-9][0-9]|[1-9]|0)\\.(25[0-5]|2[0-4][0-9]|[0-1]"
                    + "[0-9]{2}|[1-9][0-9]|[1-9]|0)\\.(25[0-5]|2[0-4][0-9]|[0-1][0-9]{2}"
                    + "|[1-9][0-9]|[0-9]))");
    final int SELECT_MULTIPLE_IMAGES = 1;
    ArrayList<String> selectedImagesPaths; // Paths of the image(s) selected by the user.
    boolean imagesSelected = false; // Whether the user selected at least an image or not.


    public DetectFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_detect, container, false);
        imageViewCamera = view.findViewById(R.id.camera);
        imageViewGallery = view.findViewById(R.id.gallery);
        imageViewCamera.setOnClickListener(this);
        imageViewGallery.setOnClickListener(this);


        return view;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.camera:
                openCamera();
                break;

            case R.id.gallery:
                openGallery();
                break;
            default:
                break;
        }

    }

    private void openCamera() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        try {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        } catch (ActivityNotFoundException e) {
            // display error state to the user
        }
    }

    private void openGallery() {
        Intent i = new Intent(
                Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(i, REQUEST_IMAGE_UPLOAD);

    }

    // Khi kết quả được trả về từ Activity khác, hàm onActivityResult sẽ được gọi.
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {

            Uri selectedImageUri = data.getData();
            // Get the path from the Uri
            final String path = getPathFromURI(selectedImageUri);
            if (path != null) {
                File f = new File(path);
                selectedImageUri = Uri.fromFile(f);

                pathImageSendToServer = selectedImageUri.getPath();
            }
            Intent intent = new Intent(this.getContext(), DetectActivity.class);
            intent.putExtra("pathImage",pathImageSendToServer);
            startActivity(intent);
        }

        if (requestCode == REQUEST_IMAGE_UPLOAD && resultCode == RESULT_OK && null != data) {
            Uri selectedImageUri = data.getData();
            // Get the path from the Uri
            final String path = getPathFromURI(selectedImageUri);
            if (path != null) {
                File f = new File(path);
                selectedImageUri = Uri.fromFile(f);
                pathImageSendToServer = selectedImageUri.getPath();
            }
            // String picturePath contains the path of selected Image
            Intent intent = new Intent(this.getContext(), DetectActivity.class);
            intent.putExtra("pathImage",pathImageSendToServer);
            startActivity(intent);
        }


//        try {
//            if (requestCode == SELECT_MULTIPLE_IMAGES && resultCode == RESULT_OK && null != data) {
//                // When a single image is selected.
//                String currentImagePath;
//                selectedImagesPaths = new ArrayList<>();
////                TextView numSelectedImages = findViewById(R.id.numSelectedImages);
//                if (data.getData() != null) {
//                    Uri uri = data.getData();
//                    currentImagePath = getPath(getContext(), uri);
//                    Log.d("ImageDetails", "Single Image URI : " + uri);
//                    Log.d("ImageDetails", "Single Image Path : " + currentImagePath);
//                    selectedImagesPaths.add(currentImagePath);
//                    imagesSelected = true;
////                    numSelectedImages.setText("Number of Selected Images : " + selectedImagesPaths.size());
//                } else {
//                    // When multiple images are selected.
//                    // Thanks tp Laith Mihyar for this Stackoverflow answer : https://stackoverflow.com/a/34047251/5426539
//                    if (data.getClipData() != null) {
//                        ClipData clipData = data.getClipData();
//                        for (int i = 0; i < clipData.getItemCount(); i++) {
//
//                            ClipData.ItemFlick item = clipData.getItemAt(i);
//                            Uri uri = item.getUri();
//
//                            currentImagePath = getPath(getContext(), uri);
//                            selectedImagesPaths.add(currentImagePath);
//                            Log.d("ImageDetails", "Image URI " + i + " = " + uri);
//                            Log.d("ImageDetails", "Image Path " + i + " = " + currentImagePath);
//                            imagesSelected = true;
////                            numSelectedImages.setText("Number of Selected Images : " + selectedImagesPaths.size());
//                        }
//                    }
//                }
//            } else {
//                Toast.makeText(getContext(), "You haven't Picked any Image.", Toast.LENGTH_LONG).show();
//            }
//            Toast.makeText(getContext(), selectedImagesPaths.size() + " Image(s) Selected.", Toast.LENGTH_LONG).show();
//        } catch (Exception e) {
//            Toast.makeText(getContext(), "Something Went Wrong.", Toast.LENGTH_LONG).show();
//            e.printStackTrace();
//        }


    }



    public String getPathFromURI(Uri contentUri) {
        String res = null;
        String[] proj = {MediaStore.Images.Media.DATA};
        Cursor cursor = getContext().getContentResolver().query(contentUri, proj, null, null, null);
        if (cursor.moveToFirst()) {
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            res = cursor.getString(column_index);
        }
        cursor.close();
        return res;
    }






    public String getImageFilePath(Uri uri) {

        File file = new File(uri.getPath());
        String[] filePath = file.getPath().split(":");
        String image_id = filePath[filePath.length - 1];

        Cursor cursor = getContext().getContentResolver().query(android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI, null, MediaStore.Images.Media._ID + " = ? ", new String[]{image_id}, null);
        if (cursor != null) {
            cursor.moveToFirst();
            String imagePath = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));

            cursor.close();
            return imagePath;
        }
        return null;
    }





//        // Kiểm tra requestCode có trùng với REQUEST_CODE vừa dùng
//        if(requestCode == REQUEST_IMAGE_CAPTURE) {
//
//            // resultCode được set bởi DetailActivity
//            // RESULT_OK chỉ ra rằng kết quả này đã thành công
//            if(resultCode == Activity.RESULT_OK) {
//                // Nhận dữ liệu từ Intent trả về
//
////                Bitmap image = (Bitmap) data.getExtras().get("data");
////                ImageView imageview = (ImageView) findViewById(R.id.ImageView01); //sets imageview as the bitmap
////                imageview.setImageBitmap(image);
//                final String result = data.getStringExtra(DetailActivity.EXTRA_DATA);
//
//                // Sử dụng kết quả result bằng cách hiện Toast
//                Toast.makeText(this.getContext(), "Result: " + result, Toast.LENGTH_LONG).show();
//            } else {
//                // DetailActivity không thành công, không có data trả về.
//            }
//        }


//    public void uploadFiles() {
//        if(imagesList.size() == 0) {
//            Toast.makeText(getContext(), "Can't choose pictures", Toast.LENGTH_SHORT).show();
//            return;
//        }
//        Map<String, RequestBody> files = new HashMap<>();
//        final FlaskClient service = ServiceGenerator.createService(FlaskClient.class);
//        for (int i = 0; i < imagesList.size(); i++) {
//            File file = new File(imagesList.get(i).path);
//            files.put("file" + i + "\"; filename=\"" + file.getName(), RequestBody.create(MediaType.parse(imagesList.get(i).mimeType), file));
//        }
//        Call<UploadResult> call = service.uploadMultipleFiles(files);
//        call.enqueue(new Callback<UploadResult>() {
//            @Override
//            public void onResponse(@NotNull okhttp3.Call call, @NotNull Response response) throws IOException {
//
//            }
//
//            @Override
//            public void onFailure(@NotNull okhttp3.Call call, @NotNull IOException e) {
//
//            }
//
//            @Override
//            public void onResponse(Call<UploadResult> call, Response<UploadResult> response) {
//                if (response.isSuccessful() && response.body().code == 1) {
//                    Toast.makeText(getContext(), "Upload success", Toast.LENGTH_SHORT).show();
//                    Log.i("orzangleli", "---------------------Upload success -----------------------");
//                    Log.i("orzangleli", "The base address is:" + ServiceGenerator.API_BASE_URL);
//                    Log.i("orzangleli", "The relative address of the picture is:" + listToString(response.body().image_urls,','));
//                    Log.i("orzangleli", "---------------------END-----------------------");
//                }
//            }
//            @Override
//            public void onFailure(Call<UploadResult> call, Throwable t) {
//                Toast.makeText(getContext(), "upload failed", Toast.LENGTH_SHORT).show();
//            }
//        });
//    }


//    public void uploadFiles() {
//        if(pathImageSendToServer==null) {
//            Toast.makeText(getContext(), "Can't choose pictures", Toast.LENGTH_SHORT).show();
//            return;
//        }
//        Map<String, RequestBody> files = new HashMap<>();
//        final FlaskClient service = ServiceGenerator.createService(FlaskClient.class);
////        for (int i = 0; i < imagesList.size(); i++) {
//            File file = new File(pathImageSendToServer);
//            files.put("file" + 0 + "\"; filename=\"" + file.getName(), RequestBody.create(MediaType.parse(getMimeType(pathImageSendToServer)), file));
////        }
//        Call<UploadResult> call = service.uploadMultipleFiles(files);
//
//        call.enqueue(new retrofit2.Callback<UploadResult>() {
//            @Override
//            public void onResponse(Call<UploadResult> call, retrofit2.Response<UploadResult> response) {
//
//                if (response.isSuccessful() && response.body().code == 1) {
//                    Toast.makeText(getContext(), "Upload success", Toast.LENGTH_SHORT).show();
//                    Log.i("orzangleli", "---------------------Upload success -----------------------");
//                    Log.i("orzangleli", "The base address is:" + ServiceGenerator.API_BASE_URL);
//                    Log.i("orzangleli", "The relative address of the picture is:" + response.body().image_urls);
//                    Log.i("orzangleli", "---------------------END-----------------------");
//
//                }
//
//
//
//            }
//
//            @Override
//            public void onFailure(Call<UploadResult> call, Throwable t) {
//
//                Toast.makeText(getContext(), "upload failed "+ t.getMessage(), Toast.LENGTH_SHORT).show();
//
//            }
//        });
//    }


    public static String getMimeType(String url) {
        String type = null;
        String extension = MimeTypeMap.getFileExtensionFromUrl(url);
        if (extension != null) {
            type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
        }
        return type;
    }




}