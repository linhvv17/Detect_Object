package com.kma.detectobject;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.google.gson.Gson;
import com.kma.detectobject.search_more.DeviceData;
import com.kma.detectobject.search_more.FlickrClient;
import com.kma.detectobject.search_more.FlickrModel;
import com.kma.detectobject.search_more.FlickrServices;
import com.kma.detectobject.search_more.ItemFlick;
import com.mikepenz.fastadapter.commons.adapters.FastItemAdapter;
import com.squareup.picasso.Picasso;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class MainActivity2 extends AppCompatActivity {
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
        setContentView(R.layout.activity_main2);

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
//                    RecyclerView.LayoutManager layoutManager = new GridLayoutManager(MainActivity2.this, 2);
                    recycler.setLayoutManager(new GridLayoutManager(MainActivity2.this, 2));
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
        super.onDestroy();
        disposable.dispose();
    }
}