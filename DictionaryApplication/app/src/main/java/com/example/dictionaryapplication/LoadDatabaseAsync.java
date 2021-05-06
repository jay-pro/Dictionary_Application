package com.example.dictionaryapplication;

import android.app.AlertDialog;
import android.os.AsyncTask;
import android.content.Context;
import android.text.Layout;
import android.view.LayoutInflater;
import android.view.View;

import androidx.loader.content.AsyncTaskLoader;

import java.io.IOException;

public class LoadDatabaseAsync extends AsyncTask<Void, Void, Boolean> {
    private Context context;
    private AlertDialog alertDialog;
    private DatabaseHelper myDbHelper;

    public LoadDatabaseAsync(Context context){
        this.context = context;
    }

    protected void onPreExcuse(){
        //TODO Auto-generated method stub
        super.onPreExecute();

        AlertDialog.Builder d = new AlertDialog.Builder(context,R.style.MyDialogTheme);
        LayoutInflater inflater = LayoutInflater.from(context);
        View dialogView = inflater.inflate(R.layout.alert_dialog_database_copying,null);
        d.setTitle("Loading Database...");
        d.setView(dialogView);
        alertDialog = d.create();

        alertDialog.setCancelable(false);
        alertDialog.show();
    }

    @Override
    protected Boolean doInBackground(Void... voids) {
        myDbHelper = new DatabaseHelper(context);
        try{
            myDbHelper.createDataBase();
        }
        catch (IOException e){
            throw new Error("Database was not created");
        }
        myDbHelper.close();
        return null;
    }

    protected void onProgressUpdate(Void... values){
        super.onProgressUpdate(values);
    }

    protected void onPostExcute(Boolean result){
        super.onPostExecute(result);
        alertDialog.dismiss();
        MainActivity.openDatabase();
    }

}
