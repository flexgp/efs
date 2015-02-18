/**
 * Copyright (c) 2011-2013 Evolutionary Design and Optimization Group
 * 
 * Licensed under the MIT License.
 * 
 * See the "LICENSE" file for a copy of the license.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS
 * BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN
 * ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.  
 * 
 * @author Ignacio Arnaldo
 *
 */
package evofmj.evaluation.java;

import evofmj.evaluation.DataSizeRetreiver;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.Random;

/**
 * This class stores the training data and the values of the composed features
 * @author Ignacio Arnaldo
 */
public class EFMScaledData {
	
    // the number of fitness cases
    private int numberOfFitnessCases;

    // the number of features
    private int numberOfOriginalFeatures;
    
    // the number of evolved features added to the model
    private int numberOfArchiveFeatures;
    
    // the number of new features composed at each generation
    private int numberOfNewFeatures;

    // the matrix composed of n number of exemplars * (numberOfOriginalFeatures+numberOfArchiveFeatures+numberOfNewFeatures)
    private final double[][] dataMatrix;
    
    // the string representation of features
    String[] featureStrings;
    
    // the size of the features: number of operators + number of variables included in the complex feature
    int[] featureSizes;
    
    /**
     * the true/target values
     */
    private final double[] target;

    // minimum target value
    private Double target_min;
    
    // maximum target value
    private Double target_max;
       
    // Maximum correlation allowed between new features and their parents
    private double CORR_THRESHOLD = 0.95;

    /**
     * 
     * @param aNumberOfArchiveFeatures
     * @param aNumberOfNewFeatures
     * @param csvPath
     * @throws IOException
     */
    public EFMScaledData(int aNumberOfArchiveFeatures, int aNumberOfNewFeatures,String csvPath) throws IOException {
        numberOfFitnessCases = DataSizeRetreiver.num_fitness_cases(csvPath);
        numberOfOriginalFeatures = DataSizeRetreiver.num_terminals(csvPath);
        numberOfArchiveFeatures = aNumberOfArchiveFeatures;
        numberOfNewFeatures = aNumberOfNewFeatures;
        int totalNumberOfFeatures = numberOfOriginalFeatures + numberOfArchiveFeatures + numberOfNewFeatures;
        dataMatrix = new double[numberOfFitnessCases][totalNumberOfFeatures];
        featureStrings = new String[totalNumberOfFeatures];
        for(int j=0;j<numberOfOriginalFeatures;j++){
            featureStrings[j] = "X" + (j+1);
        }
        featureSizes = new int[totalNumberOfFeatures];
        this.target = new double[numberOfFitnessCases];
        target_min = null;
        target_max = null;
        readCSV(csvPath);
    }
    
    /**
     * Constructor for the data matrix
     * @param csvPath
     * @throws IOException
     */
    public EFMScaledData(String csvPath) throws IOException {
        numberOfFitnessCases = DataSizeRetreiver.num_fitness_cases(csvPath);
        numberOfOriginalFeatures = DataSizeRetreiver.num_terminals(csvPath);
        dataMatrix = new double[numberOfFitnessCases][numberOfOriginalFeatures];
        this.target = new double[numberOfFitnessCases];
        target_min = null;
        target_max = null;
        readCSV(csvPath);
    }

    /*
    * read the csv containing the training data
    * the last column contains the target value
    */
    private void readCSV(String csvfile) throws FileNotFoundException, IOException {
        //BufferedReader f = new BufferedReader(new FileReader(csvfile));
        BufferedReader f = new BufferedReader(new InputStreamReader(new FileInputStream(csvfile), Charset.defaultCharset()));
        String[] token;
        int fitnessCaseIndex = 0;
        while (f.ready() && fitnessCaseIndex < numberOfFitnessCases) {
            token = f.readLine().split(",");
            for (int i = 0; i < token.length - 1; i++) {
                this.dataMatrix[fitnessCaseIndex][i] = Double.valueOf(token[i]);
            }
            double val = Double.valueOf(token[token.length - 1]);
            addTargetValue(val, fitnessCaseIndex);
            fitnessCaseIndex++;
        }
        f.close();
    }
        
    /**
     * Initialize the population of features
     * @param r random number generator
     */
    public void fillInitialArchiveandNewFeatures(Random r){
        for(int j=numberOfOriginalFeatures;j<numberOfOriginalFeatures+numberOfArchiveFeatures+numberOfNewFeatures;j++){
            int indexOriginal = r.nextInt(numberOfOriginalFeatures);
            featureStrings[j] = featureStrings[indexOriginal];
            for(int i=0;i<numberOfFitnessCases;i++){
                dataMatrix[i][j] = dataMatrix[i][indexOriginal];
            }
        }
        for(int j=0;j<numberOfOriginalFeatures+numberOfArchiveFeatures+numberOfNewFeatures;j++){
            featureSizes[j] = 1;
        }
    }
    
    /**
     * add a target value
     * @param val
     * @param index
     */
    protected void addTargetValue(double val,int index) {
        this.target[index] = val;
        if (target_min == null || val < target_min) {
            target_min = val;
        }
        if (target_max == null || val > target_max) {
            target_max = val;
        }
    }

        
    /**
     * composition of a new feature by computing the
     * multiplication of two existing features
     * @param indexNewFeature
     * @param indexParent1
     * @param indexParent2
     */
    public void multiplication(int indexNewFeature,int indexParent1,int indexParent2){
        featureStrings[indexNewFeature] = "(* " + featureStrings[indexParent1] + " " + featureStrings[indexParent2] + ")";
        if(isNewFeature(indexNewFeature)){
            for(int i=0;i<numberOfFitnessCases;i++){
                dataMatrix[i][indexNewFeature] = dataMatrix[i][indexParent1] * dataMatrix[i][indexParent2];
            }
            featureSizes[indexNewFeature] = featureSizes[indexParent1] + featureSizes[indexParent2] + 1;
            boolean valid = checkValidity(indexNewFeature,indexParent1,indexParent2);
            if(!valid){
                setFeatureToZero(indexNewFeature);
            }
        }else{
            setFeatureToZero(indexNewFeature);
        }
    }
    
    /**
     * composition of a new feature by computing the
     * division of two existing features
     * @param indexNewFeature
     * @param indexParent1
     * @param indexParent2
     */
    public void division(int indexNewFeature,int indexParent1,int indexParent2){
        featureStrings[indexNewFeature] = "(mydivide " + featureStrings[indexParent1] + " " + featureStrings[indexParent2] + ")";
        if(isNewFeature(indexNewFeature)){
            for(int i=0;i<numberOfFitnessCases;i++){
                dataMatrix[i][indexNewFeature] = dataMatrix[i][indexParent1] / dataMatrix[i][indexParent2];
            }
            featureSizes[indexNewFeature] = featureSizes[indexParent1] + featureSizes[indexParent2] + 1;
            boolean valid = checkValidity(indexNewFeature,indexParent1,indexParent2);
            if(!valid){
                setFeatureToZero(indexNewFeature);
            }
        }else{
            setFeatureToZero(indexNewFeature);
        }
    }
    
    /**
     * composition of a new feature by computing the
     * sum of two existing features
     * @param indexNewFeature
     * @param indexParent1
     * @param indexParent2
     */
    public void sum(int indexNewFeature,int indexParent1,int indexParent2){
        featureStrings[indexNewFeature] = "(+ " + featureStrings[indexParent1] + " " + featureStrings[indexParent2] + ")";
        if(isNewFeature(indexNewFeature)){
            for(int i=0;i<numberOfFitnessCases;i++){
                dataMatrix[i][indexNewFeature] = dataMatrix[i][indexParent1] + dataMatrix[i][indexParent2];
            }
            featureSizes[indexNewFeature] = featureSizes[indexParent1] + featureSizes[indexParent2] + 1;
            boolean valid = checkValidity(indexNewFeature,indexParent1,indexParent2);
            if(!valid){
                setFeatureToZero(indexNewFeature);
            }
        }else{
            setFeatureToZero(indexNewFeature);
        }            
    }
    
    /**
     * composition of a new feature by computing the
     * subtraction of two existing features
     * @param indexNewFeature
     * @param indexParent1
     * @param indexParent2
     */
    public void minus(int indexNewFeature,int indexParent1,int indexParent2){
        featureStrings[indexNewFeature] = "(- " + featureStrings[indexParent1] + " " + featureStrings[indexParent2] + ")";
        if(isNewFeature(indexNewFeature)){
            for(int i=0;i<numberOfFitnessCases;i++){
                dataMatrix[i][indexNewFeature] = dataMatrix[i][indexParent1] - dataMatrix[i][indexParent2];
            }
            featureSizes[indexNewFeature] = featureSizes[indexParent1] + featureSizes[indexParent2] + 1;
            boolean valid = checkValidity(indexNewFeature,indexParent1,indexParent2);
            if(!valid){
                setFeatureToZero(indexNewFeature);
            }
        }else{
            setFeatureToZero(indexNewFeature);
        }
    }
    
    /**
     * composition of a new feature by computing the
     * log of an existing feature
     * @param indexNewFeature
     * @param indexParent1
     */
    public void log(int indexNewFeature,int indexParent1){
        featureStrings[indexNewFeature] = "(mylog " + featureStrings[indexParent1] + ")";
        if(isNewFeature(indexNewFeature)){
            for(int i=0;i<numberOfFitnessCases;i++){
                dataMatrix[i][indexNewFeature] = Math.log(dataMatrix[i][indexParent1]);
            }
            featureSizes[indexNewFeature] = featureSizes[indexParent1] + 1;
            boolean valid = checkValidity(indexNewFeature,indexParent1,-1);
            if(!valid){
                setFeatureToZero(indexNewFeature);
            }
        }else{
            setFeatureToZero(indexNewFeature);
        }
    }
        
    /**
     * composition of a new feature by computing the
     * exp of an existing feature
     * @param indexNewFeature
     * @param indexParent1
     */
    public void exp(int indexNewFeature,int indexParent1){
        featureStrings[indexNewFeature] = "(exp " + featureStrings[indexParent1] + ")";
        if(isNewFeature(indexNewFeature)){
            for(int i=0;i<numberOfFitnessCases;i++){
                dataMatrix[i][indexNewFeature] = Math.exp(dataMatrix[i][indexParent1]);
            }
            featureSizes[indexNewFeature] = featureSizes[indexParent1] + 1;
            boolean valid = checkValidity(indexNewFeature,indexParent1,-1);
            if(!valid){
                setFeatureToZero(indexNewFeature);
            }
        }else{
            setFeatureToZero(indexNewFeature);
        }
    }
    
    /**
     * composition of a new feature by computing the
     * sin of an existing feature
     * @param indexNewFeature
     * @param indexParent1
     */
    public void sin(int indexNewFeature,int indexParent1){
        featureStrings[indexNewFeature] = "(sin " + featureStrings[indexParent1] + ")";
        if(isNewFeature(indexNewFeature)){
            for(int i=0;i<numberOfFitnessCases;i++){
                dataMatrix[i][indexNewFeature] = Math.sin(dataMatrix[i][indexParent1]);
            }
            featureSizes[indexNewFeature] = featureSizes[indexParent1] + 1;
            boolean valid = checkValidity(indexNewFeature,indexParent1,-1);
            if(!valid){
                setFeatureToZero(indexNewFeature);
            }
        }else{
            setFeatureToZero(indexNewFeature);
        }
    }
    
    /**
     * composition of a new feature by computing the
     * cos of an existing feature
     * @param indexNewFeature
     * @param indexParent1
     */
    public void cos(int indexNewFeature,int indexParent1){
        featureStrings[indexNewFeature] = "(cos " + featureStrings[indexParent1] + ")";
        if(isNewFeature(indexNewFeature)){
            for(int i=0;i<numberOfFitnessCases;i++){
                dataMatrix[i][indexNewFeature] = Math.cos(dataMatrix[i][indexParent1]);
            }
            featureSizes[indexNewFeature] = featureSizes[indexParent1] + 1;
            boolean valid = checkValidity(indexNewFeature,indexParent1,-1);
            if(!valid){
                setFeatureToZero(indexNewFeature);
            }
        }else{
            setFeatureToZero(indexNewFeature);
        }
    }
        
    /**
     * composition of a new feature by computing the
     * square root of an existing feature
     * @param indexNewFeature
     * @param indexParent1
     */
    public void sqrt(int indexNewFeature,int indexParent1){
        featureStrings[indexNewFeature] = "(sqrt " + featureStrings[indexParent1] + ")";
        if(isNewFeature(indexNewFeature)){
            for(int i=0;i<numberOfFitnessCases;i++){
                dataMatrix[i][indexNewFeature] = Math.sqrt(dataMatrix[i][indexParent1]);
            }
            featureSizes[indexNewFeature] = featureSizes[indexParent1] + 1;
            boolean valid = checkValidity(indexNewFeature,indexParent1,-1);
            if(!valid){
                setFeatureToZero(indexNewFeature);
            }
        }else{
            setFeatureToZero(indexNewFeature);
        }
    }
    
    /**
     * composition of a new feature by computing the
     * square of an existing feature
     * @param indexNewFeature
     * @param indexParent1
     */
    public void square(int indexNewFeature,int indexParent1){
        featureStrings[indexNewFeature] = "(square " + featureStrings[indexParent1] + ")";
        if(isNewFeature(indexNewFeature)){
            for(int i=0;i<numberOfFitnessCases;i++){
                dataMatrix[i][indexNewFeature] = Math.pow(dataMatrix[i][indexParent1],2);
            }
            featureSizes[indexNewFeature] = featureSizes[indexParent1] + 1;
            boolean valid = checkValidity(indexNewFeature,indexParent1,-1);
            if(!valid){
                setFeatureToZero(indexNewFeature);
            }
        }else{
            setFeatureToZero(indexNewFeature);
        }
    }
    
    /**
     * composition of a new feature by computing the
     * cube of an existing feature
     * @param indexNewFeature
     * @param indexParent1
     */
    public void cube(int indexNewFeature,int indexParent1){
        featureStrings[indexNewFeature] = "(cube " + featureStrings[indexParent1] + ")";
        if(isNewFeature(indexNewFeature)){
            for(int i=0;i<numberOfFitnessCases;i++){
                dataMatrix[i][indexNewFeature] = Math.pow(dataMatrix[i][indexParent1],3);
            }
            featureSizes[indexNewFeature] = featureSizes[indexParent1] + 1;
            boolean valid = checkValidity(indexNewFeature,indexParent1,-1);
            if(!valid){
                setFeatureToZero(indexNewFeature);
            }
        }else{
            setFeatureToZero(indexNewFeature);
        }
    }

    /**
     * check whether the index corresponds to a new feature
     * @param indexNewFeature
     * @return
     */
    public boolean isNewFeature(int indexNewFeature){
        boolean isNew = true;
        String newFeatureString = featureStrings[indexNewFeature];
        for(int i = numberOfOriginalFeatures; i<indexNewFeature;i++){
            if(featureStrings[i].equals(newFeatureString)){
                return false;
            }
        }
        return isNew;
    }
    
    /**
     * copy a feature
     * @param indexParent
     * @param indexNewFeature
     */
    public void copyFeature(int indexParent, int indexNewFeature){
        for(int i=0;i<numberOfFitnessCases;i++){
            dataMatrix[i][indexNewFeature]=dataMatrix[i][indexParent];
        }
        featureStrings[indexNewFeature] = featureStrings[indexParent];
        featureSizes[indexNewFeature] = featureSizes[indexParent];
    }
    
    /**
     *
     * @param indexNewFeature
     */
    public void setFeatureToZero(int indexNewFeature){
        for(int i=0;i<numberOfFitnessCases;i++){
            dataMatrix[i][indexNewFeature]=0;
        }
        featureStrings[indexNewFeature] = "(- X0 X0)";
        featureSizes[indexNewFeature] = 3;
    }
    
    /**
     *
     * @param indexF1
     * @param indexP1
     * @param indexP2
     * @return
     */
    public boolean checkValidity(int indexF1,int indexP1,int indexP2){
        boolean valid =true;
        for(int i=0;i<numberOfFitnessCases;i++){
            float fAux = (float)dataMatrix[i][indexF1];
            if(Float.isInfinite(fAux) || Float.isNaN(fAux)){
                return false;
            }
        }
        double pcoeffP1 = computeCorrelation(indexF1,indexP1);
        if(pcoeffP1<Math.abs(CORR_THRESHOLD)){
            if(indexP2!=-1) {
                double pcoeffP2 = computeCorrelation(indexF1,indexP2);
                if(pcoeffP2>=Math.abs(CORR_THRESHOLD)) {
                    valid = false;
                }
            }
        }else{
            valid = false;
        }
        return valid;
    }
    
    private double computeCorrelation(int index1, int index2){
        double sumX = 0;
        double sumY = 0;
        double sumX2 = 0;
        double sumY2 = 0;
        double sumXY = 0;
        for (int i=0;i<numberOfFitnessCases;i++){
            sumX += dataMatrix[i][index1];
            sumY += dataMatrix[i][index2];
            sumX2 += Math.pow(dataMatrix[i][index1],2);
            sumY2 += Math.pow(dataMatrix[i][index2],2);
            sumXY += (dataMatrix[i][index1] * dataMatrix[i][index2]);
        }
        double numerator = (numberOfFitnessCases * sumXY) - (sumX*sumY);
        double denominatorLeft = Math.sqrt((numberOfFitnessCases*sumX2) - Math.pow(sumX, 2));
        double denominatorRight = Math.sqrt((numberOfFitnessCases*sumY2) - Math.pow(sumY, 2));
        double denominator = denominatorLeft * denominatorRight;
        double coeff = numerator/denominator;
        return coeff;
    }
    
    /**
     * @return the data matrix
     */
    public double[][] getInputValues(){
        return this.dataMatrix;
    }
    
    /**
     * return a row of the data matrix
     * @param index
     * @return
     */
    public double[] getRow(int index){
        return dataMatrix[index];
    }

    /**
     * @return the vector of target values
     */
    public double[] getTargetValues(){
        return this.target;
    }
        
    /**
     * @return maximum target value
     */
    public double getTargetMax() {
        return target_max;
    }

    /**
     * @return minimum target value
     */
    public double getTargetMin() {
        return target_min;
    }

    /**
     * @return the number of exemplars
     */
    public int getNumberOfFitnessCases() {
        return numberOfFitnessCases;
    }

    /**
     * @return the total number Of Features
     */
    public int getNumberOfTotalFeatures() {
        return (numberOfOriginalFeatures + numberOfArchiveFeatures + numberOfNewFeatures);
    }
     
    /**
     * @return the numberOfOriginalFeatures
     */
    public int getNumberOfOriginalFeatures() {
        return numberOfOriginalFeatures;
    }
    
    /**
     * @return the numberOfArchiveFeatures
     */
    public int getNumberOfArchiveFeatures() {
        return numberOfArchiveFeatures;
    }
    
    /**
     * @return the numberOfNewFeatures
     */
    public int getNumberOfNewFeatures() {
        return numberOfNewFeatures;
    }

    /**
     * return the string representation of a given feature
     * @param index
     * @return
     */
    public String getFeatureString(int index){
        return featureStrings[index];
    }

    /**
     * return the size of a given feature
     * @param index
     * @return
     */
    public int getFeatureSize(int index){
        return featureSizes[index];
    }
}