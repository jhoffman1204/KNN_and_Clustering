//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

import java.util.HashMap;
import java.util.Iterator;

public class InputVector {
    HashMap<String, Word> wordsVector = new HashMap();
    boolean isPositive;

    public InputVector(boolean var1) {
        this.isPositive = var1;
    }

    public boolean exists(String var1) {
        return this.wordsVector.containsKey(var1);
    }

    public void addWordToInputVector(String var1) {
        if(this.wordsVector.containsKey(var1)) {
            ((Word)this.wordsVector.get(var1)).incrementFrequency();
        } else {
            Word var2 = new Word(var1);
            this.wordsVector.put(var1, var2);
        }

    }

    public void printVectorData() {
        Iterator var1 = this.wordsVector.keySet().iterator();

        while(var1.hasNext()) {
            Object var2 = var1.next();
            System.out.println("word: " + ((Word)this.wordsVector.get(var2)).getWord() + " || frequency: " + ((Word)this.wordsVector.get(var2)).getFrequency() + " || weight: " + ((Word)this.wordsVector.get(var2)).getWeight());
        }

    }

    public HashMap<String, Word> getWordsVector() {
        return this.wordsVector;
    }
}
