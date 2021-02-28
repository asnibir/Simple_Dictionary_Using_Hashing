package com.example.englishtobengalidictionary;

public class Word {
    String enWord;
    String bnWord;

    public Word(String enWord, String bnWord) {
        this.enWord = enWord;
        this.bnWord = bnWord;
    }

    public String getEnWord() {
        return enWord;
    }

    public void setEnWord(String enWord) {
        this.enWord = enWord;
    }

    public String getBnWord() {
        return bnWord;
    }

    public void setBnWord(String bnWord) {
        this.bnWord = bnWord;
    }
}
