import com.sun.org.apache.xpath.internal.operations.Bool;

import java.io.*;
import java.nio.file.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.TreeMap;

/**
 * Created by James on 4/15/2017.
 */
public class NearestCentroid {

    public static int totalPositive = 0;
    public static int totalNegative = 0;

    public static InputVector[] positiveMovieReviews = new InputVector[1000];
    public static InputVector[] negativeMovieReviews = new InputVector[1000];

    public static InputVector[] fold1 = new InputVector[400];
    public static InputVector[] fold2 = new InputVector[400];
    public static InputVector[] fold3 = new InputVector[400];
    public static InputVector[] fold4 = new InputVector[400];
    public static InputVector[] fold5 = new InputVector[400];

    public static int movieReviewCounter = 0;
    public static double ratio = 0;
    public static int totalPositiveDistance = 0;
    public static int totalNegativeDistance = 0;

    public static int truePositive = 0;
    public static int trueNegative = 0;
    public static int falsePostiive = 0;
    public static int falseNegative = 0;

    public static String selectedDistanceMetric = "euclidean";
    public static String punctuationMetric = "NoPunctuation";   // NoPunctuation controls whether punctuation is counted as points int the vector
    public static String frequencyMetric = "frequency";     // controls whether variables are counted as frequencies or just binary

    public static void main(String args[]){

        System.out.println("Testing KNearestNeighbors");
        Scanner in = new Scanner(System.in);
        System.out.println("Punctuation (y) or no punctuation (n)");
        String punct = in.nextLine();
        if(punct.equalsIgnoreCase("n")){
            punctuationMetric = "NoPunctuation";
        }
        else if(punct.equalsIgnoreCase("y")){
            punctuationMetric = "Punctuation";
        }
        else{
            System.out.println("neither selected");
        }
        System.out.println("Frequency (f) or no binary (b)");
        String freq = in.nextLine();
        if(freq.equalsIgnoreCase("f")){
            frequencyMetric = "frequency";
        }
        else if(freq.equalsIgnoreCase("b")){
            frequencyMetric = "binary";
        }
        else{
            System.out.println("neither selected");
        }
        System.out.println("Euclidean (e) or Manhattan (m)");
        String dist = in.nextLine();
        if(dist.equalsIgnoreCase("e")){
            selectedDistanceMetric = "euclidean";
        }
        else if(dist.equalsIgnoreCase("m")){
            selectedDistanceMetric = "manhattan";
        }
        else{
            System.out.println("neither selected");
        }

        // Transition the vectors from being text files to vectors that we can anaylze
        // true/false is for the boolean isPositive
        initializeMovieReviewVectors("src\\txt_sentoken\\pos", positiveMovieReviews, true);
        initializeMovieReviewVectors("src\\txt_sentoken\\neg", negativeMovieReviews, false);

        // Split the movie review points into folds, with those folds alternating positive and negative reviews
        initializeFolds();

        // calculate smoothing ratio based on the frequency of potisitve and negative words in the folds
        ratio = calculateRatioPositiveToNegative();

        // classify the movie reviews
        classifyAllFolds();
    }
    public static void classifyAllFolds(){
        System.out.println("testing fold 1...");
        for(int i = 0; i < fold1.length; i++) {
            findNearestCentroid(fold1[i], 1);
        }
        printFoldStatistics(1);
        resetCounter();
        System.out.println("testing fold 2...");
        for(int i = 0; i < fold1.length; i++) {
            findNearestCentroid(fold2[i], 2);
        }
        printFoldStatistics(2);
        resetCounter();
        System.out.println("testing fold 3...");
        for(int i = 0; i < fold1.length; i++) {
            findNearestCentroid(fold3[i], 3);
        }
        printFoldStatistics(3);
        resetCounter();
        System.out.println("testing fold 4...");
        for(int i = 0; i < fold1.length; i++) {
            findNearestCentroid(fold4[i], 4);
        }
        printFoldStatistics(4);
        resetCounter();
        System.out.println("testing fold 5...");
        for(int i = 0; i < fold1.length; i++) {
            findNearestCentroid(fold5[i], 5);
        }
        printFoldStatistics(5);
        resetCounter();
    }
    public static void resetCounter(){
        truePositive = 0;
        trueNegative = 0;
        falseNegative = 0;
        falsePostiive = 0;
    }
    public static void printFoldStatistics(int fold) {
        System.out.println("Fold " + fold + " Statistics: ");
        System.out.println(truePositive);
        System.out.println(falsePostiive);
        System.out.println(trueNegative);
        System.out.println(falseNegative);

        double precisionPlus = (double)truePositive / (truePositive + falsePostiive);
        double precisionMinus = (double)trueNegative / (trueNegative + falseNegative);
        double recallPlus = (double)truePositive / (truePositive + falseNegative);
        double recallMinus = (double)trueNegative / (trueNegative + falsePostiive);
        double accuracy = (double)(truePositive + trueNegative)/(truePositive + trueNegative + falsePostiive + falseNegative);
        double precision = (double)(precisionPlus + precisionMinus)/2;
        double recall    = (double)(recallPlus + recallMinus)/2;

        System.out.println("accuracy: " + accuracy);
        System.out.println("precision: " + precision);
        System.out.println("recall: " + recall);

        System.out.println("##########################");
    }
    public static double calculateRatioPositiveToNegative(){
        int totalPositiveWords = 0;
        int totalNegativeWords = 0;
        for(int i = 0; i < positiveMovieReviews.length; i++){
            for (Object key : positiveMovieReviews[i].getWordsVector().keySet()) {
                totalPositiveWords++;
            }
        }
        //System.out.println("total positive words: " + totalPositiveWords);

        for(int i = 0; i < negativeMovieReviews.length; i++){
            for (Object key : negativeMovieReviews[i].getWordsVector().keySet()) {
                totalNegativeWords++;
            }
        }
        //System.out.println("total negative words: " + totalNegativeWords);

        double a = ((double)totalPositiveWords / (double)totalNegativeWords);
        return a;
    }
    public static void findNearestCentroid(InputVector currentReview, int foldNumber){
        totalPositiveDistance = 0;
        totalNegativeDistance = 0;
        //ratio = 1;
        if(foldNumber != 1) {
            for (int i = 0; i < fold1.length; i++) {
                double distance = calculateDistance(currentReview, fold1[i], selectedDistanceMetric);
                if(fold1[i].isPositive == true){
                    totalPositiveDistance += distance;
                }
                else {
                    distance = distance * ratio;
                    totalNegativeDistance += distance;
                }
            }
        }
        if(foldNumber != 2) {
            for (int i = 0; i < fold2.length; i++) {
                double distance = calculateDistance(currentReview, fold2[i], selectedDistanceMetric);
                if(fold1[i].isPositive == true){
                    totalPositiveDistance += distance;
                }
                else {
                    distance = distance * ratio;
                    totalNegativeDistance += distance;
                }
            }
        }
        if(foldNumber != 3) {
            for (int i = 0; i < fold3.length; i++) {
                double distance = calculateDistance(currentReview, fold3[i], selectedDistanceMetric);
                if(fold1[i].isPositive == true){
                    totalPositiveDistance += distance;
                }
                else {
                    distance = distance * ratio;
                    totalNegativeDistance += distance;
                }
            }
        }
        if(foldNumber != 4) {
            for (int i = 0; i < fold4.length; i++) {
                double distance = calculateDistance(currentReview, fold4[i], selectedDistanceMetric);
                if(fold1[i].isPositive == true){
                    totalPositiveDistance += distance;
                }
                else {
                    distance = distance * ratio;
                    totalNegativeDistance += distance;
                }
            }
        }
        if(foldNumber != 5) {
            for (int i = 0; i < fold5.length; i++) {
                double distance = calculateDistance(currentReview, fold5[i], selectedDistanceMetric);
                if(fold1[i].isPositive == true){
                    totalPositiveDistance += distance;
                }
                else {
                    distance = distance * ratio;
                    totalNegativeDistance += distance;
                }
            }
        }

        //System.out.println("Total Positive Distances Away: " + totalPositiveDistance);
        //System.out.println("Total Negative Distances Away: " + totalNegativeDistance + currentReview.isPositive);
        if((totalPositiveDistance < totalNegativeDistance) && currentReview.isPositive == true){
            truePositive++;
        }
        else if((totalPositiveDistance < totalNegativeDistance) && currentReview.isPositive == false){
            falsePostiive++;
        }
        else if((totalPositiveDistance > totalNegativeDistance) && currentReview.isPositive == true){
            falseNegative++;
        }
        else if((totalPositiveDistance > totalNegativeDistance) && currentReview.isPositive == false) {
            trueNegative++;
        }

    }
    public static void initializeFolds(){
        // sort folds by positive reviews
        int movieNumberCounter = 0;
        for(int i = 0; i < 400; i = i + 2){
            fold1[i] = positiveMovieReviews[movieNumberCounter];
            movieNumberCounter++;
        }
        for(int i = 400; i < 800; i = i + 2){
            fold2[i-400] = positiveMovieReviews[movieNumberCounter];
            movieNumberCounter++;
        }
        for(int i = 800; i < 1200; i = i + 2){
            fold3[i-800] = positiveMovieReviews[movieNumberCounter];
            movieNumberCounter++;
        }
        for(int i = 1200; i < 1600; i = i + 2){
            fold4[i-1200] = positiveMovieReviews[movieNumberCounter];
            movieNumberCounter++;
        }
        for(int i = 1600; i < 2000; i = i + 2){
            fold5[i-1600] = positiveMovieReviews[movieNumberCounter];
            movieNumberCounter++;
        }
        movieNumberCounter = 0;
        // sort folds by negative reviews
        for(int i = 1; i < 400; i = i + 2){
            fold1[i] = negativeMovieReviews[movieNumberCounter];
            movieNumberCounter++;
        }
        for(int i = 401; i < 800; i = i + 2){
            fold2[i-400] = negativeMovieReviews[movieNumberCounter];
            movieNumberCounter++;
        }
        for(int i = 801; i < 1200; i = i + 2){
            fold3[i-800] = negativeMovieReviews[movieNumberCounter];
            movieNumberCounter++;
        }
        for(int i = 1201; i < 1600; i = i + 2){
            fold4[i-1200] = negativeMovieReviews[movieNumberCounter];
            movieNumberCounter++;
        }
        for(int i = 1601; i < 2000; i = i + 2){
            fold5[i-1600] = negativeMovieReviews[movieNumberCounter];
            movieNumberCounter++;
        }
    }
    public static double calculateDistance(InputVector a, InputVector b, String method){
        if(method.equalsIgnoreCase("euclidean")) {
            double distance = 0;
            HashMap<String, Double> frequencyDistances = new HashMap<String, Double>();
            // Find the distances between the words in a and b
            for (Object key : a.getWordsVector().keySet()) {
                String distancedWord = a.getWordsVector().get(key).getWord();
                int frequencyA = a.getWordsVector().get(distancedWord).getFrequency();
                int frequencyB = 0;
                if (b.getWordsVector().containsKey(distancedWord)) {
                    frequencyB = b.getWordsVector().get(distancedWord).getFrequency();
                }
                int difference = frequencyA - frequencyB;
                double squaredDistance = Math.pow(difference, 2);
                frequencyDistances.put(distancedWord, squaredDistance); // adds this distance to the hashmap that will be summed and then squared rooted
            }
            // iterate through b to find the distances that were not found by comparing a to b
            for (Object key : b.getWordsVector().keySet()) {
                // check to see if the word is already in the frequencyDistance hashmap
                String distancedWord = b.getWordsVector().get(key).getWord();
                if (frequencyDistances.containsKey(distancedWord)) {

                } else {
                    int frequencyA = 0;
                    if (a.getWordsVector().containsKey(distancedWord)) {
                        frequencyA = a.getWordsVector().get(distancedWord).getFrequency();
                    }
                    int frequencyB = b.getWordsVector().get(distancedWord).getFrequency();
                    int difference = frequencyA - frequencyB;
                    double squaredDistance = Math.pow(difference, 2);
                    frequencyDistances.put(distancedWord, squaredDistance);
                }
            }
            // Sum all the distances together
            for (Object key : frequencyDistances.keySet()) {
                distance = distance + frequencyDistances.get(key);
            }
            double squareRootDistance = Math.sqrt(distance);
            // now we have the distance between these two movie reviews
            return squareRootDistance;
        }
        else if(method.equalsIgnoreCase("manhattan")){
            double distance = 0;
            HashMap<String, Double> frequencyDistances = new HashMap<String, Double>();
            // Find the distances between the words in a and b
            for (Object key : a.getWordsVector().keySet()) {
                String distancedWord = a.getWordsVector().get(key).getWord();
                int frequencyA = a.getWordsVector().get(distancedWord).getFrequency();
                int frequencyB = 0;
                if (b.getWordsVector().containsKey(distancedWord)) {
                    frequencyB = b.getWordsVector().get(distancedWord).getFrequency();
                }
                int difference = frequencyA - frequencyB;
                double squaredDistance = Math.pow(difference, 2);
                frequencyDistances.put(distancedWord, squaredDistance); // adds this distance to the hashmap that will be summed and then squared rooted
            }
            // iterate through b to find the distances that were not found by comparing a to b
            for (Object key : b.getWordsVector().keySet()) {
                // check to see if the word is already in the frequencyDistance hashmap
                String distancedWord = b.getWordsVector().get(key).getWord();
                if (frequencyDistances.containsKey(distancedWord)) {

                } else {
                    int frequencyA = 0;
                    if (a.getWordsVector().containsKey(distancedWord)) {
                        frequencyA = a.getWordsVector().get(distancedWord).getFrequency();
                    }
                    int frequencyB = b.getWordsVector().get(distancedWord).getFrequency();
                    int difference = frequencyA - frequencyB;
                    double squaredDistance = difference;                        // there is no squaring not taking the square root for the manhattan distance
                    frequencyDistances.put(distancedWord, squaredDistance);
                }
            }
            // Sum all the distances together
            for (Object key : frequencyDistances.keySet()) {
                distance = distance + frequencyDistances.get(key);
            }
            double squareRootDistance = Math.abs(distance);
            // now we have the distance between these two movie reviews
            //System.out.println(squareRootDistance);
            return squareRootDistance;
        }
        else{
            System.out.println("no distance metric selected");
            return 0;
        }
    }
    public static void initializeMovieReviewVectors(String path, InputVector[] vector, boolean isPositive) {
        Path dir = FileSystems.getDefault().getPath(path);
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir)) {
            for (Path file : stream) {
                InputVector movieReview = createInputVector(file.toFile(), isPositive);
                vector[movieReviewCounter] = movieReview;
                movieReviewCounter++;
            }
        } catch (IOException | DirectoryIteratorException x) {
            System.err.println(x);
        }
        movieReviewCounter = 0;
    }

    public static InputVector createInputVector(File selectedFile, boolean isPositive) {
        String words[] = new String[20];
        String[] xinputVector = new String[10000];
        InputVector movieReview = new InputVector(isPositive);
        try {
            BufferedReader in = new BufferedReader(new FileReader(selectedFile));
            String text;
            while ((text = in.readLine()) != null) {
                words = text.split(" ");
                for (int i = 0; i < words.length; i++) {
                    movieReview.addWordToInputVector(words[i]);
                }
            }
        } catch (FileNotFoundException e) {
            System.out.println("File Not Found for the BufferedReader");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return movieReview;
    }
}