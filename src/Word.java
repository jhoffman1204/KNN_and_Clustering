/**
 * Created by James on 4/15/2017.
 */
public class Word {
    String word;
    int weight;
    int frequency;

    public Word(String word){
        this.word = word;
        weight = 0;
        frequency= 1;
    }
    public void incrementFrequency(){
        this.frequency++;
    }
    public String getWord(){
        return word;
    }
    public int getWeight(){
        return weight;
    }
    public int getFrequency(){
        return frequency;
    }
}
