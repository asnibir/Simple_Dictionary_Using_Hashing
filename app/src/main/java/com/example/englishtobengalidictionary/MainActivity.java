package com.example.englishtobengalidictionary;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;

import android.graphics.Color;
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
    int PRIME = 100007;

    //Arrays to store info and collisions
    int[][] hashArray = new int[M][3];
    int[] collisionArray = new int[M];

    //Arrays to store words
    ArrayList<Word> wordList;
    Word[][] secondWordList ;

    String strData = "", strLine = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //TextView
        showText = findViewById(R.id.textViewId);

        // Search View
        searchView = findViewById(R.id.searchViewId);
        searchView.onActionViewExpanded();
        searchView.setPadding(2, 0, 0, 0);
        searchView.setGravity(Gravity.CENTER_VERTICAL);
        searchView.setOnQueryTextListener(this);

        //Find A & B randomly
        A = (int) (Math.random()*13)+1;
        B = (int) (Math.random()*13);
        //A = getRandomNumberUsingNextInt(1, M);
        //B = getRandomNumberUsingNextInt(0, M);
        Log.d("A", "A = " + A);
        Log.d("B", "B = " + B);
        Log.d("Prime", "Prime: " + PRIME);

        //CollisionArray initialization with 0
        Arrays.fill(collisionArray,0);

        // ArrayList to store the words
        wordList = new ArrayList<>();


        //Json Object to String Conversion
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



        //Initializing key and value of object Word as null
        for(int i = 0; i < M; i++){
            Word word = new Word(null, null);
            wordList.add(word);
        }
        Log.d("slotNo", "onCreate: "+M);

        //To Check Collision Number in each slots
        try {
            jsonObject = new JSONObject(strData);
            Iterator keys = jsonObject.keys();
            while(keys.hasNext()) {

                String currentKey = (String)keys.next();
                String currentValue = null;
                currentValue = jsonObject.getString(currentKey);
                int Key = strToNumConversion(currentKey);
                int hash_value = primaryHash(Key);

                if(wordList.get(hash_value).getEnWord() == null){
                    Word word = new Word(currentKey, currentValue);
                    wordList.set(hash_value, word);
                }
                else {
                    collisionArray[hash_value]++;
                }


            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        // max collision number
        int[] temp = new int[M];
        temp = collisionArray;
        Arrays.sort(temp);
        int max_collision = temp[99999];
        Log.d("collisions", "Max Collision: "+max_collision);
        m = max_collision*max_collision;

        secondWordList = new Word[M][m];

        //collision wise determination of  m, a and b for second hash function
        for(int i = 0; i < M; i++)
        {
            int a = (int) ((Math.random() * (m - 1)) + 1);
            int b = (int) ((Math.random() * m) );
            int length = collisionArray[i] * collisionArray[i];
            hashArray[i][0] = length;
            hashArray[i][1] = a;
            hashArray[i][2] = b;
        }

        //call hash function
        hashFunction();
    }

    //Random number generator (Probably not needed)
    public int getRandomNumberUsingNextInt(int min, int max) {
        Random random = new Random();
        return random.nextInt(max - min) + min;
    }

    private int primaryHash(int k) {
        int mod = 98689;
        return (((A*k)%mod + B)%mod)%M;
    }

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

            if(collisionArray[hash_value] == 0){

                //bnWord.set( num,currentDynamicValue);
                Word word = new Word(currentKey, currentValue);
                wordList.set(hash_value, word);

            }
            else {
                int second_hash_value = secondaryHash(hash_value, hashArray[hash_value][1], hashArray[hash_value][2]);
                Word word = new Word(currentKey, currentValue);
                secondWordList[hash_value][second_hash_value] = word;
            }


        }

    }

    private int secondaryHash(int k, int a, int b) {
        return ((a*k + b)%PRIME)%m;
    }



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

    //To search words
    private void wordSearching(String wordForSearch) throws JSONException {

        String bngWord, engWord;
        wordForSearch = wordForSearch.toLowerCase();
        int uniqueKey = strToNumConversion(wordForSearch);

        int hash_value = primaryHash(uniqueKey);
        Word word;
        if(collisionArray[hash_value] == 0){


            word = wordList.get(hash_value);
            bngWord = word.getBnWord();
            engWord = word.getEnWord();

            if(wordForSearch.equals(engWord) && bngWord!=null){
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

    private int strToNumConversion(String text) {

        int number = 0;
        text = text.toLowerCase();

        for(int i=0; i<text.length();i++){
            if(text.charAt(i) >= 'a' && text.charAt(i) <= 'z'){
                number = (number*26 + (int)text.charAt(i)) % PRIME; // - 'a' // floorMod
            }

        }
        return number;
    }


    @Override
    public boolean onQueryTextChange(String newText) {
        return false;
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }
}