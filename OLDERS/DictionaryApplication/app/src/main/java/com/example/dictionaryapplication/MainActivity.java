package com.example.dictionaryapplication;
// chuyển sang search view bản v7 nha Nhi
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.SQLException;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.cursoradapter.widget.CursorAdapter;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import

public class MainActivity extends AppCompatActivity {

    SearchView search;

    static DatabaseHelper myDbHelper;
    static boolean databaseOpened = false;

    SimpleCursorAdapter suggestionAdapter;

    ArrayList<History> historyList;
    ArrayList<History> histories;//bỏ nó đi
    RecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManager;
    RecyclerView.Adapter historyAdapter;

    RelativeLayout emptyHistory;
    Cursor cursorHistory;

    boolean doubleBackToExitPresedOnce = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        search = (SearchView) findViewById(R.id.search_view);
        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                search.setIconified(false);
                //Intent intent = new Intent(MainActivity.this, WordMeaningActivity.class);
                //startActivity(intent);
            }
        });

        myDbHelper = new DatabaseHelper(this);
        if(myDbHelper.checkDataBase()){
            openDatabase();
        }
        else{
            LoadDatabaseAsync task = new LoadDatabaseAsync(MainActivity.this);
            task.execute();
        }

        //setup SimpleCursorAdapter
        final String[] from = new String[] {"en_word"};
        final int[] to = new int[] {R.id.suggestion_text};
        suggestionAdapter = new SimpleCursorAdapter(MainActivity.this, R.layout.suggestion_row, null, from, to,0){
            @Override
            public void changeCursor(Cursor cursor){
                super.swapCursor(cursor);
            }
        };
        //search.setSuggestionsAdapter(suggestionAdapter);//35:30
        search.setOnSuggestionListener(new SearchView.OnSuggestionListener() {
            @Override
            public boolean onSuggestionSelect(int position) {
                //Your code here
                return true;
            }

            @Override
            public boolean onSuggestionClick(int position) {
                //Add clicked text to search box
                CursorAdapter ca = search.getSuggestionsAdapter();
                Cursor cursor = ca.getCursor();
                cursor.moveToPosition(position);
                String clicked_word = cursor.getString(cursor.getColumnIndex("en_word"));
                search.setQuery(clicked_word,false);
                //search.setQuery("",false);
                search.clearFocus();
                search.setFocusable(false);

                Intent intent = new Intent(MainActivity.this,WordMeaningActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString("en_word",clicked_word);
                intent.putExtras(bundle);
                startActivity(intent);

                return true;
            }
        });

        search.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                String text = search.getQuery().toString();

                Pattern p = Pattern.compile("[A-Za-z \\-.]{1,25}");
                Matcher m = p.matcher(text);

                if(m.matches()){

                    Cursor c = myDbHelper.getMeaning(text);

                    if(c.getCount()==0){
                        showAlertDialog();
                    /*
                    search.setQuery("",false);
                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this, R.style.MyDialogTheme);
                    builder.setTitle("Word Not Found");
                    builder.setMessage("Please search again");
                    String positiveText = getString(android.R.string.ok);
                    builder.setPositiveButton(positiveText, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //positice button logic
                        }
                    });
                    String negativeText = getString(android.R.string.cancel);
                    builder.setNegativeButton(negativeText, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            search.clearFocus();
                        }
                    });
                    AlertDialog dialog = builder.create();
                    //display dialog
                    dialog.show();
                     */
                    }
                    else {
                        //Search.setQuery("".false);
                        search.clearFocus();
                        search.setFocusable(false);

                        Intent intent = new Intent(MainActivity.this, WordMeaningActivity.class);
                        Bundle bundle = new Bundle();
                        bundle.putString("en_word",text);
                        intent.putExtras(bundle);
                        startActivity(intent);
                    }
                }
                else{
                    showAlertDialog();
                }

                return false;
            }

            @Override
            public boolean onQueryTextChange(final String s) {
                search.setIconifiedByDefault(false);//Give suggestion list margins

                Pattern p = Pattern.compile("[A-Za-z \\-.]{1,25}");
                Matcher m = p.matcher(s);

                if(m.matches()){
                    Cursor cursorSuggestion=myDbHelper.getSuggestions(s);
                    suggestionAdapter.changeCursor(cursorSuggestion);
                }
                return false;
            }
        });

        emptyHistory = (RelativeLayout) findViewById(R.id.empty_history);

        //recycler View
        recyclerView = (RecyclerView) findViewById(R.id.recycler_view_history);
        layoutManager = new LinearLayoutManager(MainActivity.this);

        recyclerView.setLayoutManager(layoutManager);
        fetch_history();
    }

    protected static void openDatabase(){
        try{
            myDbHelper.openDatabase();
            databaseOpened=true;
        }
        catch(SQLException e){
            e.printStackTrace();
        }
    }

    private void fetch_history(){
        historyList = new ArrayList<>();//55:42
        historyAdapter = new RecyclerViewAdapterHistory(this,historyList);
        recyclerView.setAdapter(historyAdapter);
        History h;

        if(databaseOpened){
            cursorHistory = myDbHelper.getHistory();
            if(cursorHistory.moveToFirst()){
                do{
                    h = new History(cursorHistory.getString(cursorHistory.getColumnIndex("word")),cursorHistory.getString(cursorHistory.getColumnIndex("en_definition")));
                    historyList.add(h);
                }
                while (cursorHistory.moveToNext());
            }
            historyAdapter.notifyDataSetChanged();

            if(historyAdapter.getItemCount() == 0){
                emptyHistory.setVisibility(View.VISIBLE);
            }
            else{
                emptyHistory.setVisibility(View.GONE);
            }
        }
    }

    private void showAlertDialog(){
        search.setQuery("",false);
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this, R.style.MyDialogTheme);
        builder.setTitle("Word Not Found");
        builder.setMessage("Please search again");
        String positiveText = getString(android.R.string.ok);
        builder.setPositiveButton(positiveText, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //positice button logic
            }
        });
        String negativeText = getString(android.R.string.cancel);
        builder.setNegativeButton(negativeText, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                search.clearFocus();
            }
        });
        AlertDialog dialog = builder.create();
        //display dialog
        dialog.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //Inflate the menu_main; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main,menu);
        return true;
        //return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        //Handle action bar item clicks here. The action bar will
        //automatically handle clicks on the Home/Up button
        int id = item.getItemId();

        if(id == R.id.action_settings){
            Intent intent = new Intent(MainActivity.this, SettingActivity.class);
            startActivity(intent);
            return true;
        }

        if(id == R.id.action_exit){
            System.exit(0);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume(){
        super.onResume();
        fetch_history();
    }

    //@Override
    public void onBackPresessed(){
        if(doubleBackToExitPresedOnce){
            super.onBackPressed();
        }
        this.doubleBackToExitPresedOnce = true;
        Toast.makeText(this, "Pres BACK again to exit", Toast.LENGTH_SHORT).show();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                doubleBackToExitPresedOnce = false;
            }
        },2000);
    }
}
