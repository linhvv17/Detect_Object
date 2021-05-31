package com.kma.detectobject.fragment;

import android.os.Bundle;

import androidx.annotation.Nullable;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


import com.kma.detectobject.R;
import com.kma.detectobject.adapter.ItemAdapter;
import com.kma.detectobject.database.DatabaseHandler;
import com.kma.detectobject.database.Item;

import java.util.ArrayList;
import java.util.List;


public class GalleryFragment extends Fragment {


    private RecyclerView recyclerView;
    private ArrayList<Item> itemArrayList ;
    private ItemAdapter itemAdapter ;
    private DatabaseHandler databaseHandler;
    private String pathFolderApp = "/storage/emulated/0/DetectObject/Images/";

    public GalleryFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public void onResume() {
        super.onResume();
        itemArrayList = (ArrayList<Item>) databaseHandler.getAllItems();
        Log.d("SIZE",""+itemArrayList.size());

        itemAdapter = new ItemAdapter(getContext(),itemArrayList,databaseHandler);
        recyclerView.setAdapter(itemAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_gallery, container, false);
        recyclerView = view.findViewById(R.id.rc_gallery);
        databaseHandler = new DatabaseHandler(getContext());
        databaseHandler.deleteAllItems();

        addResultToDatabase(pathFolderApp);


        return view;

    }

    private void addResultToDatabase(String pathFolderApp) {
        databaseHandler.addItem(new Item("Person","Người",pathFolderApp+"person.jpg"));
        databaseHandler.addItem(new Item("House","Nhà",pathFolderApp+"house.jpg"));
        databaseHandler.addItem(new Item("Car","Ô tô",pathFolderApp+"car.jpg"));
        databaseHandler.addItem(new Item("Cat","Mèo",pathFolderApp+"cat.jpg"));
        databaseHandler.addItem(new Item("Dog","Chó",pathFolderApp+"dog.jpg"));
        databaseHandler.addItem(new Item("Motobike","Xe máy",pathFolderApp+"motobike.jpg"));
        databaseHandler.addItem(new Item("Tree","Cây",pathFolderApp+"tree.png"));
        databaseHandler.addItem(new Item("Motobile","Điện thoại",pathFolderApp+"mobile.png"));
//        databaseHandler.deleteAllItems();
    }


}