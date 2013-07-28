package com.example.LlamaFoodFinder;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

public class FoodDBHelper extends SQLiteOpenHelper{

    private static String DB_PATH = "/data/data/com.llama.foodfinder/databases/";
    private static String DB_NAME = "food.db";
    private SQLiteDatabase myDataBase;
    private final Context myContext;

    public FoodDBHelper(Context context) {

        super(context, DB_NAME, null, 1);
        this.myContext = context;
    }

    /**
     * Creates a empty database on the system and rewrites it with your own database.
     * */
    public void createDataBase() throws IOException{
        System.out.println("CREATE DB ---------------------------------");
        boolean dbExist = checkDataBase();

        if(dbExist){
            //do nothing - database already exist
            System.out.println("DB exists");
        }else{
            System.out.println("DB doesn't exist");
            //By calling this method and empty database will be created into the default system path
            //of your application so we are gonna be able to overwrite that database with our database.
            this.getReadableDatabase();

            try {

                copyDataBase();

            } catch (IOException e) {

                throw new Error("Error copying database");

            }
        }
    }

    /**
     * Check if the database already exist to avoid re-copying the file each time you open the application.
     * @return true if it exists, false if it doesn't
     */
    private boolean checkDataBase(){

        SQLiteDatabase checkDB = null;

        try{
            String myPath = DB_PATH + DB_NAME;
            checkDB = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READONLY);

        }catch(SQLiteException e){

            //database does't exist yet.

        }

        if(checkDB != null){

            checkDB.close();

        }

        return checkDB != null ? true : false;
    }

    /**
     * Copies your database from your local assets-folder to the just created empty database in the
     * system folder, from where it can be accessed and handled.
     * This is done by transfering bytestream.
     * */
    private void copyDataBase() throws IOException{
        System.out.println("COPY ---------------------------------");
        //Open your local db as the input stream
        InputStream myInput = myContext.getAssets().open(DB_NAME);

        // Path to the just created empty db
        String outFileName = DB_PATH + DB_NAME;

        //Open the empty db as the output stream
        OutputStream myOutput = new FileOutputStream(outFileName);

        //transfer bytes from the inputfile to the outputfile
        byte[] buffer = new byte[1024];
        int length;
        while ((length = myInput.read(buffer))>0){
            myOutput.write(buffer, 0, length);
            System.out.println(buffer + " -- " + length);
        }
        System.out.println("COPY complete ---------------------------------");
        //Close the streams
        myOutput.flush();
        myOutput.close();
        myInput.close();
        TESTprintDB();

    }
     public void TESTprintDB()
     {   openDataBase();
         System.out.println("NOW PRINTING THE DATABASE---------") ;
         String text = "";
         String[] a = new String[1]; a[0] = "name";
        Cursor c = myDataBase.query("food", a,
                 null,
                 null,
                 null,
                 null,
                 null);
         if (c.moveToFirst())
         {
             do {
                 text +=
                         c.getString(0) + " ";
             } while (c.moveToNext());
         }
           System.out.println(text);
     }
    public void openDataBase() throws SQLException{

        //Open the database
        String myPath = DB_PATH + DB_NAME;
        myDataBase = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READONLY);

    }

    @Override
    public synchronized void close() {

        if(myDataBase != null)
            myDataBase.close();

        super.close();

    }

    @Override
    public void onCreate(SQLiteDatabase db) {

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

//    public Cursor getFood(int fast, int adventure, int breakfast, int cheap, int late)
//    {
//        String query = "SELECT name FROM food WHERE new =" + adventure;
//        if (fast == 1 || fast == 0)
//            query += " AND fast=" + fast;
//        if (cheap == 1)
//            query += " AND cheap=" + cheap;
//        if (breakfast == 1)
//            query += " AND breakfast=" + breakfast;
//        if (late == 1)
//            query += " AND late=" + late;
//        return myDataBase.rawQuery( query, null);
//    }

    public Cursor getFood(int type, ArrayList<String> selectedTags)
    {
        String query = "SELECT name FROM food WHERE ";
        int tagcount = 0;
        if (type < 2)
        {
            query += " fast=" + type;
            tagcount++;
        }
        for(String x : selectedTags)
        {
            if(tagcount == 0)
                query += x + "=1";
            else
                query += " AND " + x + "=1";
            tagcount++;
        }
        return myDataBase.rawQuery( query, null);
    }

    public ArrayList<String> turnIntoList(Cursor c)
    {
        ArrayList<String> results = new ArrayList<String>();
        if (c != null ) {
            if  (c.moveToFirst()) {
                do {
                    results.add(c.getString(c.getColumnIndex("name")));
                }while (c.moveToNext());
            }
        }
        return results;
    }

    public Cursor getAll() {
         return myDataBase.rawQuery("SELECT * FROM food", null);
    }

}