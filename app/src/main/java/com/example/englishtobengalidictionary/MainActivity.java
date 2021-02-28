package com.example.englishtobengalidictionary;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;

import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Random;

public class MainActivity extends AppCompatActivity implements SearchView.OnQueryTextListener{

    TextView showText;
    SearchView searchView;
    JSONObject jsonObject;

    //Variables for implementing Hash Functions
    int M = 100000; //total slots
    int m, A, B;
    int PRIME = 1000000007;
    int collisions = 0;

    //Arrays to store info and collisions
    int[][] hashArray = new int[M][3];
    int[] collisionArray = new int[M];

    //Arrays to store words
    ArrayList<Word> wordList;
    Word[][] secondWordList ;

    String strData = "", strLine = "";

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Call function to find TexView and SearchView
        findView();

        //Find A & B randomly
        A = getRandomNumberUsingNextInt(1, M);
        B = getRandomNumberUsingNextInt(0, M);

        //CollisionArray initialization with 0
        Arrays.fill(collisionArray,0);

        // ArrayList to store the words
        wordList = new ArrayList<>();

        // Call function to convert Json to String
        jsonToStringConversion();

        //Initializing key and value of object Word as null
        for(int i = 0; i < M; i++){
            Word word = new Word(null, null);
            wordList.add(word);
        }

        // Function to check collision in each slot
        checkCollisionsInEachSlot();

        // calculate maximum collision number
        int[] temp = new int[M];
        temp = collisionArray;
        Arrays.sort(temp);
        int max_collision = temp[M-1];
        //Log.d("collisions", "Max Collision: "+max_collision);
        m = max_collision*max_collision;

        secondWordList = new Word[M][m];

        //collision wise determination of  m, a and b for second hash function
        determinationOfABM();

        //call hash function for insertion in Hash Table
        hashFunction();
    }

    // Find TextView and SearchView
    public void findView(){
        //TextView
        showText = findViewById(R.id.textViewId);

        // Search View
        searchView = findViewById(R.id.searchViewId);
        searchView.onActionViewExpanded();
        searchView.setPadding(2, 0, 0, 0);
        searchView.setGravity(Gravity.CENTER_VERTICAL);
        searchView.setOnQueryTextListener(this);
    }

    //Random number generator
    public int getRandomNumberUsingNextInt(int min, int max) {
        Random random = new Random();
        return random.nextInt(max - min) + min;
    }

    //Json Object to String Conversion
    public void jsonToStringConversion(){
        try {
            BufferedReader bufferedReader = new BufferedReader(
                    new InputStreamReader(getAssets().open("dictionary.json")));
            while (strLine != null)
            {
                strData += strLine;
                strLine = bufferedReader.readLine();

            }
            bufferedReader.close();
        } catch (FileNotFoundException e) {
            System.err.println("File not found");
        } catch (IOException e) {
            System.err.println("Unable to read the file.");
        }
    }

    // To check collisions in each slot
    @RequiresApi(api = Build.VERSION_CODES.N)
    public void checkCollisionsInEachSlot(){

        try {
            jsonObject = new JSONObject(strData);
            Iterator keys = jsonObject.keys();
            while(keys.hasNext()) {

                String currentKey = (String)keys.next();
                String currentValue = null;
                currentValue = jsonObject.getString(currentKey);
                int Key = strToNumConversion(currentKey);
                int hash_value = primaryHash(Key);

                Word word = wordList.get(hash_value);
                collisionArray[hash_value]++;
                /*
                if(wordList.get(hash_value).getEnWord() == null){
                    Word word = new Word(currentKey, currentValue);
                    wordList.set(hash_value, word);
                }
                else {
                    collisionArray[hash_value]++;
                }*/

            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    // Collision wise determination of a, b, m for secondary hash function
    public void determinationOfABM(){
        for(int i = 0; i < M; i++)
        {
            int a = (int) ((Math.random() * (m - 1)) + 1);
            int b = (int) ((Math.random() * m) );
            int length = collisionArray[i] * collisionArray[i];
            hashArray[i][0] = length;
            hashArray[i][1] = a;
            hashArray[i][2] = b;
        }
    }

    // Hash Function
    @RequiresApi(api = Build.VERSION_CODES.N)
    private void hashFunction() {

        //Initializing key and value of object Word as null
        for(int i = 0; i < M; i++){
            Word word = new Word(null, null);
            wordList.add(word);
        }

        Iterator key = jsonObject.keys();
        while(key.hasNext()) {
            String currentKey = (String)key.next();
            String currentValue = null;
            try {
                currentValue = jsonObject.getString(currentKey);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            int uniqueKey = strToNumConversion(currentKey);
            int hash_value = primaryHash(uniqueKey);

            currentKey = currentKey.toLowerCase();
            //if(collisionArray[hash_value] == 0)
            if(collisionArray[hash_value] <= 1){

                Word word = new Word(currentKey, currentValue);
                wordList.set(hash_value, word);

            }
            else {
                int second_hash_value = secondaryHash(hash_value, hashArray[hash_value][1], hashArray[hash_value][2]);
                Word word = new Word(currentKey, currentValue);
                secondWordList[hash_value][second_hash_value] = word;
            }


        }

        int col = 0;
        for(int i=0; i<collisionArray.length; i++){
            col+=collisionArray[i];
        }
        Log.d("second", "hashFunction: "+collisions+" "+col);

    }



    // String to Number Conversion
    @RequiresApi(api = Build.VERSION_CODES.N)
    private int strToNumConversion(String text) {
        int number = 0;
        text = text.toLowerCase();

        for(int i=0; i<text.length();i++){
            if(text.charAt(i) >= 'a' && text.charAt(i) <= 'z'){
                number = (number*26 + (int)text.charAt(i));
                number = Math.floorMod(number, PRIME);
            }

        }
        return number;
    }

    // Primary Hash Function
    @RequiresApi(api = Build.VERSION_CODES.N)
    private int primaryHash(int k) {
        //return (((A*k)%mod + B)%mod)%M;
        int temp = A*k + B;
        int temp2 = Math.floorMod(temp, PRIME);
        return Math.floorMod(temp2, M);
    }

    // Secondary Hash Function
    private int secondaryHash(int k, int a, int b) {
        return ((a*k + b)%PRIME)%m; // No need to do floorMod
    }

    // wordSearching method is called here
    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public boolean onQueryTextSubmit(String query) {
        query = query.toLowerCase();

        try {
            wordSearching(query);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return false;
    }

    //To search words in hash table
    @RequiresApi(api = Build.VERSION_CODES.N)
    private void wordSearching(String wordForSearch) throws JSONException {

        String bngWord, engWord;
        wordForSearch = wordForSearch.toLowerCase();
        int uniqueKey = strToNumConversion(wordForSearch);

        int hash_value = primaryHash(uniqueKey);
        Word word;
        if(collisionArray[hash_value] <= 1){

            word = wordList.get(hash_value);
            bngWord = word.getBnWord();
            engWord = word.getEnWord();

            if(wordForSearch.equals(engWord)){
                showText.setText(bngWord);
            }
            else{
                showText.setText("আপনার শব্দের অর্থটি পাওয়া যায় নি!");
            }
        }
        else {
            int second_hash_value = secondaryHash(hash_value, hashArray[hash_value][1], hashArray[hash_value][2]);


            word = secondWordList[hash_value][second_hash_value];
            bngWord = word.getBnWord();
            engWord = word.getEnWord();
            if(wordForSearch.equals(engWord) && bngWord!=null){
                showText.setText(bngWord);
            }
            else{
                showText.setText("আপনার শব্দের অর্থটি পাওয়া যায় নি!");
            }

        }

    }


    @Override
    public boolean onQueryTextChange(String newText) {
        return false;
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }
}