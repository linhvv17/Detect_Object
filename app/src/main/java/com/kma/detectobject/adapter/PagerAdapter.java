package com.kma.detectobject.adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.fragment.app.FragmentStatePagerAdapter;

import com.kma.detectobject.fragment.DetectFragment;
import com.kma.detectobject.fragment.GalleryFragment;
import com.kma.detectobject.fragment.SettingFragment;

import java.util.ArrayList;

public class PagerAdapter extends FragmentPagerAdapter {
    private ArrayList<Fragment> fragments;
    private ArrayList<String> strings;

    public PagerAdapter(@NonNull FragmentManager fm) {
        super(fm);
        this.fragments = new ArrayList<>();
        this.strings = new ArrayList<>();
    }


    @Override
    public Fragment getItem(int position) {
        return fragments.get(position);
    }

    @Override
    public int getCount() {
        return fragments.size();
    }
    public void addFragment( Fragment fragment, String title){
        this.fragments.add(fragment);
        this.strings.add(title);
    }
}
