package com.kma.detectobject;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.viewpager.widget.ViewPager;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.material.tabs.TabLayout;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.DexterError;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.PermissionRequestErrorListener;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.kma.detectobject.adapter.PagerAdapter;
import com.kma.detectobject.fragment.DetectFragment;
import com.kma.detectobject.fragment.GalleryFragment;
import com.kma.detectobject.fragment.SettingFragment;

import java.io.File;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_PERMISSION_CODE = 100;
    private TabLayout tabLayout;
    private ViewPager viewPager;
    private PagerAdapter pagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        pagerAdapter = new PagerAdapter(getSupportFragmentManager());
        tabLayout = findViewById(R.id.tab_layout);

        viewPager = findViewById(R.id.view_pager);
        //list icon cua tab khong duoc chon
        //list icon cua tab duoc chon
        requestPermission();
    }

    public void requestPermission() {
        Dexter.withActivity(this).withPermissions(
                Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.CAMERA)
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport report) {
                        // check if all permissions are granted or not
                        if (report.areAllPermissionsGranted()) {

                            //them cac fragment
                            pagerAdapter.addFragment(new DetectFragment(),"DETECT");
                            pagerAdapter.addFragment(new GalleryFragment(),"GALLERY");
                            pagerAdapter.addFragment(new SettingFragment(),"SETTING");

                            viewPager.setAdapter(pagerAdapter);
                            tabLayout.setupWithViewPager(viewPager);

                            tabLayout.getTabAt(0).setIcon(R.drawable.ic_search);
                            tabLayout.getTabAt(1).setIcon(R.drawable.ic_saved);
                            tabLayout.getTabAt(2).setIcon(R.drawable.ic_settings);

                            

                            tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
                                @Override
                                public void onTabSelected(TabLayout.Tab tab) {
                                    
                                }
                                @Override
                                public void onTabUnselected(TabLayout.Tab tab) {
                                    
                                }

                                @Override
                                public void onTabReselected(TabLayout.Tab tab) {
                                    Log.d("POSITION","REPOSITION"+tab.getPosition());

                                }
                            });


                            createFolderApp();


                        }
                        // check for permanent denial of any permission show alert dialog
                        if (report.isAnyPermissionPermanentlyDenied()) {
                            // open Settings activity
                            showSettingsDialog();
                        }
                    }
                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {
                        token.continuePermissionRequest();
                    }
                }).withErrorListener(new PermissionRequestErrorListener() {
            @Override
            public void onError(DexterError error) {
                Toast.makeText(MainActivity.this.getApplicationContext(), "Error occurred! ", Toast.LENGTH_SHORT).show();
            }
        })
                .onSameThread()
                .check();
    }

    private void showSettingsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.message_need_permission));
        builder.setMessage(getString(R.string.message_permission));
        builder.setPositiveButton(getString(R.string.title_go_to_setting), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
                MainActivity.this.openSettings();
            }
        });
        builder.show();
    }
    // navigating settings app
    private void openSettings() {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", this.getPackageName(), null);
        intent.setData(uri);
        startActivityForResult(intent, REQUEST_PERMISSION_CODE);
    }

    public void createFolderApp() {
        //create folder
        File file = new File(Environment.getExternalStorageDirectory() + "/DetectObject/Images");
        if (!file.mkdirs()) {
            file.mkdirs();
        }

    }

}