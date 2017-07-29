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
public class KNearestNeighbors {

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

    public static int truePositive = 0;
    public static int trueNegative = 0;
    public static int falsePositive = 0;
    public static int falseNegative = 0;

    public static double ratio = 0;
    public static String selectedDistanceMetric = "manhattan";
    public static String punctuationMetric = "NoPunctuation";   // NoPunctuation controls whether punctuation is counted as points int the vector
    public static String frequencyMetric = "frequency";     // controls whether variables are counted as frequencies or just binary
    public static int kValue = 100;
    public static String[] punctuation = {";",".",",",":","(",")","`"," ","|","\\"};

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
        // true/false is for the boolean isPositive
        initializeMovieReviewVectors("src\\txt_sentoken\\pos", positiveMovieReviews, true, punctuationMetric, frequencyMetric);
        initializeMovieReviewVectors("src\\txt_sentoken\\neg", negativeMovieReviews, false, punctuationMetric, frequencyMetric);


        ratio = calculateRatioPositiveToNegative();
        //calculateEuclideanDistance(positiveMovieReviews[0],negativeMovieReviews[0]);

        // Split the movie review points into folds, with those folds alternating positive and negative reviews
        initializeFolds();
        // find the distance between each of the points in a fold to every point in every other fold


        System.out.println("Testing Fold 1...");
        testFold(kValue,1);
        System.out.println("Testing Fold 2...");
        testFold(kValue,2);
        System.out.println("Testing Fold 3...");
        testFold(kValue,3);
        System.out.println("Testing Fold 4...");
        testFold(kValue,4);
        System.out.println("Testing Fold 5...");
        testFold(kValue,5);
    }
    public static void testFold(int kValue, int fold){
        int k  = kValue;
        if(fold == 1){
            for(int i = 0; i < fold1.length; i++) {
                findKNearestNodes(fold1[i], k, 1);
            }
        }
        else if(fold == 2){
            for(int i = 0; i < fold1.length; i++) {
                findKNearestNodes(fold2[i], k, 2);
            }
        }
        else if(fold == 3){
            for(int i = 0; i < fold1.length; i++) {
                findKNearestNodes(fold3[i], k, 3);
            }
        }
        else if(fold == 4){
            for(int i = 0; i < fold1.length; i++) {
                findKNearestNodes(fold4[i], k, 4);
            }
        }
        else if(fold == 5){
            for(int i = 0; i < fold1.length; i++) {
                findKNearestNodes(fold5[i], k, 5);
            }
        }

        //findKNearestNodes(fold3[0], k, 3);

        //System.out.println("total positive reviews: " + totalPositive);
        //System.out.println("total negative reviews: " + totalNegative);
        System.out.println("Fold " + fold + " results:");
        System.out.println("true  positives: " + truePositive);
        System.out.println("true  negatives: " + trueNegative);
        System.out.println("false positives: " + falsePositive);
        System.out.println("false negatives: " + falseNegative);

        double precisionPlus = (double)truePositive / (truePositive + falsePositive);
        double precisionMinus = (double)trueNegative / (trueNegative + falseNegative);
        double recallPlus = (double)truePositive / (truePositive + falseNegative);
        double recallMinus = (double)trueNegative / (trueNegative + falsePositive);
        double accuracy = (double)(truePositive + trueNegative)/(truePositive + trueNegative + falsePositive + falseNegative);
        double precision = (double)(precisionPlus + precisionMinus)/2;
        double recall    = (double)(recallPlus + recallMinus)/2;

        System.out.println("accuracy: " + accuracy);
        System.out.println("precision: " + precision);
        System.out.println("recall: " + recall);
        System.out.println("#####################################");

        truePositive = 0;
        trueNegative = 0;
        falsePositive = 0;
        falseNegative = 0;


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
    public static void findKNearestNodes(InputVector currentReview, int k, int foldNumber){
        Map<Double, Boolean> map = new TreeMap<Double, Boolean>();
        // find the distance and whether those distances are positive or negative
        /**
         * Depending on what fold i'm evaluating, certain folds have to be fed into the system.
         */
        //System.out.println("ratio: " + ratio);
        //System.out.println(currentReview.isPositive);
        //ratio = 1; // if we want the ratio to be igonred
        if(foldNumber != 1) {
            for (int i = 0; i < fold1.length; i++) {
                double distance = calculateDistance(currentReview, fold1[i], selectedDistanceMetric);
                if(fold1[i].isPositive == false){
                    distance = distance * ratio;
                }
                if (map.containsKey(distance)) {
                    //System.out.println("There was an overlap");
                }
                map.put(distance, fold1[i].isPositive);
            }
        }
        if(foldNumber != 2) {
            for (int i = 0; i < fold2.length; i++) {
                double distance = calculateDistance(currentReview, fold2[i], selectedDistanceMetric);
                if(fold2[i].isPositive == false){
                    distance = distance * ratio;
                }
                if (map.containsKey(distance)) {
                    //System.out.println("There was an overlap");
                }
                map.put(distance, fold2[i].isPositive);
            }
        }
        if(foldNumber != 3) {
            for (int i = 0; i < fold3.length; i++) {
                double distance = calculateDistance(currentReview, fold3[i], selectedDistanceMetric);
                if(fold3[i].isPositive == false){
                    distance = distance * ratio;
                }
                if (map.containsKey(distance)) {
                    //System.out.println("There was an overlap");
                }
                map.put(distance, fold3[i].isPositive);
            }
        }
        if(foldNumber != 4) {
            for (int i = 0; i < fold4.length; i++) {
                double distance = calculateDistance(currentReview, fold4[i], selectedDistanceMetric);
                if(fold4[i].isPositive == false){
                    distance = distance * ratio;
                }
                if (map.containsKey(distance)) {
                    //System.out.println("There was an overlap");
                }
                map.put(distance, fold4[i].isPositive);
            }
        }
        if(foldNumber != 5) {
            for (int i = 0; i < fold5.length; i++) {
                double distance = calculateDistance(currentReview, fold5[i],selectedDistanceMetric);
                if(fold5[i].isPositive == false){
                    distance = distance * ratio;
                    //System.out.println("this line excecuted");
                }
                if (map.containsKey(distance)) {
                    //System.out.println("There was an overlap");
                }
                map.put(distance, fold5[i].isPositive);
            }
        }
        // map sorts the key values from least to greatest
        // take the k nearest values

        /**
         * iterates through the TreeMap, which has sorted the positive and negative reviews from lowest to highest, with the key values
         * being whether the review was positive or not
         */
        int iterateCounter = 0;
        int positiveCounter = 0;
        int negativeCounter = 0;

        for(Double key : map.keySet()) {
            //System.out.println(key + ": " + map.get(key));
            if(map.get(key) == true){
                positiveCounter++;
            }
            else if(map.get(key) == false){
                negativeCounter++;
            }
            else{
                System.out.println("Something went wrong");
            }

            iterateCounter++;
            if(iterateCounter == k){
                break;
            }
        }
        if(positiveCounter > negativeCounter && currentReview.isPositive){
            truePositive++;
        }
        else if(positiveCounter > negativeCounter && !currentReview.isPositive){
            falsePositive++;
        }
        else if(positiveCounter < negativeCounter && currentReview.isPositive){
            falseNegative++;
        }
        else if(positiveCounter < negativeCounter && !currentReview.isPositive){
            trueNegative++;
        }
        //System.out.println("Positive: " + positiveCounter); // prints the total amount of positive and negative reviews that were closest to this review
        //System.out.println("Negative: " + negativeCounter);
        //System.out.println("this is actually a " + currentReview.isPositive + "(true == positive, false == negative) review");
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
            double squareRootDistance = distance;
            // now we have the distance between these two movie reviews
            return squareRootDistance;
        }
        else{
            System.out.println("no distance metric selected");
            return 0;
        }
    }
    public static void initializeMovieReviewVectors(String path, InputVector[] vector, boolean isPositive, String punctuationMetric, String frequencyMetric) {
        Path dir = FileSystems.getDefault().getPath(path);
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir)) {
            for (Path file : stream) {
                InputVector movieReview = createInputVector(file.toFile(), isPositive, punctuationMetric, frequencyMetric);
                vector[movieReviewCounter] = movieReview;
                movieReviewCounter++;
            }
        } catch (IOException | DirectoryIteratorException x) {
            System.err.println(x);
        }
        movieReviewCounter = 0;
    }

    public static InputVector createInputVector(File selectedFile, boolean isPositive, String punctuationMetric, String frequencyMetric) {
        String words[] = new String[20];
        String[] xinputVector = new String[10000];
        InputVector movieReview = new InputVector(isPositive);
        boolean hasPunct = false;
        try {
            BufferedReader in = new BufferedReader(new FileReader(selectedFile));
            String text;
            while ((text = in.readLine()) != null) {
                words = text.split(" ");
                for (int i = 0; i < words.length; i++) {
                    if(punctuationMetric.equalsIgnoreCase("NoPunctuation")){
                        for(int j = 0; j < punctuation.length; j++){
                            if(punctuation[j].equalsIgnoreCase(words[i])){
                                hasPunct = true;
                            }
                            if(hasPunct == false){
                                movieReview.addWordToInputVector(words[i]);
                            }
                        }
                    }
                    else {
                        movieReview.addWordToInputVector(words[i]);
                    }
                    hasPunct = false;
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