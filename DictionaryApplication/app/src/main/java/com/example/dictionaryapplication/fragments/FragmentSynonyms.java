package com.example.dictionaryapplication.fragments;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.dictionaryapplication.R;
import com.example.dictionaryapplication.WordMeaningActivity;

public class FragmentSynonyms extends Fragment {
    public FragmentSynonyms(){
        //Required empty public constructor

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        //Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_definition,container, false);//inflate layout

        Context context = getActivity();
        TextView text = (TextView) view.findViewById(R.id.textViewD);

        String synonyms = ((WordMeaningActivity)context).synonyms;

        if(synonyms != null){
            synonyms = synonyms.replaceAll(",",",\n");
            text.setText(synonyms);
        }
        if(synonyms == null){
            text.setText("No definition found");
        }

        return view;
        //return super.onCreateView(inflater, container, savedInstanceState);
    }
}
