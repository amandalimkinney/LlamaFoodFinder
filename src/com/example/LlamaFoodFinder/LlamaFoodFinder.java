package com.example.LlamaFoodFinder;

import android.app.DialogFragment;
import android.app.ListActivity;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.SQLException;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.RadioButton;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

// add second button of "view all" that will show a listview of all restaurant results (use Cursor returned by the dbhelper method
// change to a tag structure
// db editing and viewing:
//      add activity for edit/view
//      change the auto import db to a button to import from a .db
//      have a way to export db?

public class LlamaFoodFinder
        extends
        ListActivity
{
    private ArrayList<String> result;
    private int type = 2;
    private FoodDBHelper myDbHelper;
    private ListView mainListView = null;
    private ArrayList<String> selectedItems = new ArrayList<String>();
    final String SETTING_LLAMAFOOD = "LlamaFood";

    @Override
    public void onCreate( Bundle savedInstanceState )
    {
        super.onCreate( savedInstanceState );
        setContentView(R.layout.main);

        // get database
        myDbHelper = new FoodDBHelper( this );

        //create DB if needed
        try
        { myDbHelper.createDataBase(); }
        catch (IOException ioe)
        {  throw new Error( "Unable to create database" ); }

        //adapter for list of tags
        //lv_arr = (String[]) getTagList().toArray(new String[0]);

        this.mainListView = getListView();
        mainListView.setCacheColorHint(0);

        LayoutInflater inflater = LayoutInflater.from(this);
        View mTop    = inflater.inflate(R.layout.main_header, null);
        View mBottom = inflater.inflate(R.layout.main_footer, null);

        mainListView.addHeaderView(mTop);
        mainListView.addFooterView(mBottom);
        // add header and footer before setting adapter
        mainListView.setAdapter(new ArrayAdapter<String>(LlamaFoodFinder.this,
                android.R.layout.simple_list_item_multiple_choice, getTagList()));
        mainListView.setItemsCanFocus(false);
        mainListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        LoadSelections();
    }

    public void finishDB()
    {
        myDbHelper.close();
    }
    @Override
    public boolean onCreateOptionsMenu( Menu menu )
    {
        getMenuInflater().inflate( R.menu.main_menu, menu );
        return true;
    }

    public void onRadioButtonClicked( View view )
    {
        // Is the button now checked?
        boolean checked = ( (RadioButton) view ).isChecked();

        // Check which radio button was clicked
        switch (view.getId())
        {
            case R.id.radioFastFood:
                type = 1;
                break;
            case R.id.radioDineIn:
                type = 0;
                break;
            case R.id.radioBoth:
                type = 2;
                break;
        }
    }

    public ArrayList<String> getCheckedItems( )
    {
        ArrayList<String> temp = new ArrayList<String>();
        //String savedItems = "";

        int count = this.mainListView.getAdapter().getCount();
        for(int i = 0; i < count; i++)
        {
          if(this.mainListView.isItemChecked(i))
              temp.add((String)this.mainListView.getItemAtPosition(i));
        }
        return temp;
    }
    void showDialog(int type, String name) {
    DialogFragment newFragment = FoodDialogFragment.newInstance(type, name);
    newFragment.show(getFragmentManager(), "dialog");

}

    void showDialog(int type, ArrayList<String> list){
        DialogFragment newFragment = FoodDialogFragment.newInstance(type, list);
        newFragment.show(getFragmentManager(), "dialog");
    }

    private String getSavedItems() {
        String savedItems = "";

        int count = this.mainListView.getAdapter().getCount();

        for (int i = 0; i < count; i++) {

            if (this.mainListView.isItemChecked(i)) {
                if (savedItems.length() > 0) {
                    savedItems += "," + this.mainListView.getItemAtPosition(i);
                } else {
                    savedItems += this.mainListView.getItemAtPosition(i);
                }
            }

        }
        return savedItems;
    }

    private void SaveSelections() {

        // save the selections in the shared preference in private mode for the user

        SharedPreferences settingsActivity = getPreferences(MODE_PRIVATE);
        SharedPreferences.Editor prefEditor = settingsActivity.edit();

        String savedItems = getSavedItems();

        prefEditor.putString(SETTING_LLAMAFOOD, savedItems);

        prefEditor.commit();
    }

    private void LoadSelections() {
        // if the selections were previously saved load them

        SharedPreferences settingsActivity = getPreferences(MODE_PRIVATE);

        if (settingsActivity.contains(SETTING_LLAMAFOOD)) {
            String savedItems = settingsActivity
                    .getString(SETTING_LLAMAFOOD, "");

            this.selectedItems.addAll(Arrays.asList(savedItems.split(",")));
            int count = this.mainListView.getAdapter().getCount();

            for (int i = 0; i < count; i++) {
                String currentItem = (String) this.mainListView.getAdapter()
                        .getItem(i);
                if (this.selectedItems.contains(currentItem)) {
                    this.mainListView.setItemChecked(i, true);
                }

            }

        }
    }

//    private void ClearSelections() {
//
//        // user has clicked clear button so uncheck all the items
//
//        int count = this.mainListView.getAdapter().getCount();
//
//        for (int i = 0; i < count; i++) {
//            this.mainListView.setItemChecked(i, false);
//        }
//
//        // also clear the saved selections
//        SaveSelections();
//
//    }
    @Override
    protected void onPause() {
        // always handle the onPause to make sure selections are saved if user clicks back button
        SaveSelections();

        super.onPause();
    }

    private ArrayList<String> getTagList() {
        ArrayList<String> tags = new ArrayList<String>();
        try
        {  myDbHelper.openDataBase();
            Cursor c = myDbHelper.getAll();

            String[] temp = c.getColumnNames();
            if(temp != null)
            {
                for(String x : temp)
                {
                    if(! (x.equals("name") || x.equals("_id") || x.equals("fast")))
                        tags.add(x);
                }
            }
        }
        catch (SQLException sqle)
        { throw sqle;  }
         finally {
            finishDB();
        }

        return tags;
    }

    public void findFood(View view)
    {
        //open database
        System.out.println("FIND FOOD OPENING DB NOW");
        try
        {  myDbHelper.openDataBase(); }
        catch (SQLException sqle)
        { throw sqle; }

        // get Cursor of results
        result = myDbHelper.turnIntoList(myDbHelper.getFood( type, getCheckedItems() ));

        System.out.println("Result cursor created");

        //generate the random result using that Cursor
        getRandomFood();
        finishDB();
    }

    public void getRandomFood()
    {  System.out.println("NOW FOR RANDOM RESULT....");
        // choose random restaurant
        if ( result.isEmpty() )
        {      System.out.println("NO RESULTS!");
            showDialog( FoodDialogFragment.NO_RESULTS, "" );
            return;
        }

        Random random = new Random();
        String name = result.remove(random.nextInt(result.size()));
        System.out.println("RESULT:" + name);
        showDialog(FoodDialogFragment.RESULTS_FOUND, name);
    }

    public void findAllFood(View view)
    {
        System.out.println("FIND FOOD OPENING DB NOW");
        try
        {  myDbHelper.openDataBase(); }
        catch (SQLException sqle)
        { throw sqle; }

        // get results
         ArrayList<String> temp = myDbHelper.turnIntoList(myDbHelper.getFood(type, getCheckedItems()));
        if(temp.isEmpty())
        {
            System.out.println("NO RESULTS!");
            showDialog( FoodDialogFragment.NO_RESULTS, "" );
            return;
        }

        showDialog(FoodDialogFragment.ALL_RESULTS, temp);
        finishDB();
    }


}

