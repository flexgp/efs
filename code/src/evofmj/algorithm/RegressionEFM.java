/**
 * Copyright (c) 2014 ALFA Group
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

package evofmj.algorithm;

import edu.uci.lasso.LassoFit;
import edu.uci.lasso.LassoFitGenerator;
import evofmj.evaluation.java.EFMScaledData;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

/**
 * Main class of the Evolutionary Feature Search Method
 * @author Ignacio Arnaldo
 */
public class RegressionEFM {
    
    boolean VERBOSE = false;
    EFMScaledData dataMatrix;
    long startTime;
    double TIMEOUT;
    int numberOfOriginalFeatures, numberOfArchiveFeatures, numberOfNewFeatures, maxFinalFeatures;
    int tournamentSize;
    double binary_recomb_rate;
    double[] featureScores;
    int[] indicesArchive;
    ArrayList<String> alWeights;
    Random r;
    double lassoIntercept;
    int maxFeatureSize;
    String[] unaryOps = {"mylog","exp","mysqrt","square","cube","cos","sin"};
    String[] binaryOps = {"*","mydiv","+","-"};
    double BEST_MSE, CURRENT_MSE;
    ArrayList<String> bestFeatures;
    ArrayList<Double> bestWeights;
    double bestIntercerpt;
    int MAX_STALL_ITERATIONS = 200;
    int STALL_ITERATIONS;
    int indexIteration;
    int FITNESS_BIAS = 1; // 0:vc --- 1:r2 --- 2:mse
    int MODEL_SELECTION_BIAS = 1;
    
    /**
     * constructor
     * @param csvPath
     * @param aNumberOfArchiveFeatures
     * @param aNumberOfNewFeatures
     * @param aMaxFeatureSize
     * @param aMaxFinalFeatures
     * @throws IOException
     */
    public RegressionEFM(String csvPath, int aNumberOfArchiveFeatures, int aNumberOfNewFeatures, int aMaxFeatureSize, int aMaxFinalFeatures) throws IOException{
        numberOfArchiveFeatures = aNumberOfArchiveFeatures;
        numberOfNewFeatures = aNumberOfNewFeatures;
        maxFinalFeatures = aMaxFinalFeatures;
        maxFeatureSize = aMaxFeatureSize;
        dataMatrix = new EFMScaledData(numberOfArchiveFeatures, numberOfNewFeatures, csvPath);
        numberOfOriginalFeatures = dataMatrix.getNumberOfOriginalFeatures();
        featureScores = new double[numberOfOriginalFeatures + numberOfArchiveFeatures + numberOfNewFeatures];
        indicesArchive = new int[numberOfArchiveFeatures];
        for(int i=0;i<numberOfArchiveFeatures;i++) indicesArchive[i] = numberOfOriginalFeatures + i;
        lassoIntercept = 0;
        tournamentSize = 2;
        binary_recomb_rate = 0.5;
        long seed = System.currentTimeMillis();
        System.out.println(seed);
        r = new Random(seed);
        BEST_MSE = Double.MAX_VALUE;
        bestFeatures = new ArrayList<String>();
        STALL_ITERATIONS = 0;
        startTime = System.currentTimeMillis();
    }
    
    /**
     * main loop of the EFM method
     * @param timeout
     * @throws Exception
     */
    public void runEFM(double timeout) throws Exception{
        if (timeout > 0) TIMEOUT = startTime + (timeout * 1000);
        dataMatrix.fillInitialArchiveandNewFeatures(r);
        boolean finished = false;
        indexIteration = 0;
        evalAllFeatures();
        while (!finished) {
            generateNewFeatures();
            evalAllFeatures();
            // FOR LOGGING PURPOSES
            getCurrentModelWeights();
            if(VERBOSE){
                saveCurrentFeatureSet();
                saveCurrentModel();
            }
            finished = stopCriteria();
            indexIteration++;
        }
        saveBestFeatureSet(true);
        saveBestModel(true);
    }
    
    /**
     * stop criteria: convergence or timeout is reached
     * @return whether the run must be stopped
     */
    public boolean stopCriteria(){
        boolean stop = false;
        if( System.currentTimeMillis() >= TIMEOUT){
            System.out.println("Timout exceeded, exiting. BEST MSE IS: " + BEST_MSE);
            return true;
        }else if(STALL_ITERATIONS>MAX_STALL_ITERATIONS){
            System.out.println("Progress Stalled, exiting. BEST MSE IS: " + BEST_MSE);
            return true;
        }
        return stop;
    }
    
    /*
    * Estimate feature importance according to
    * the number of appearances of a feature in the regularized models obtained
    * via pathwise coordinate descent
    */
    private void computeFeatureImportanceVariableCount(LassoFit fit,ArrayList<FeatureScore> alFS){
        int indexWeights = 0;
        double rcoeff = 0;
        for(int i=0;i<fit.lambdas.length;i++){
            if(fit.rsquared[i]> rcoeff){
                indexWeights = i;
                rcoeff = fit.rsquared[i];
            }
        }
        double[] lassoWeights = fit.getWeights(indexWeights);
        for(int j=0;j<lassoWeights.length;j++){
            if(lassoWeights[j]!=0){
                featureScores[j] = 0;
                for(int z=0;z<fit.nonZeroWeights.length;z++){
                    double[] lassoWeightsAux = fit.getWeights(z);
                    if(lassoWeightsAux[j]!=0) featureScores[j]++;
                }
            }else{
                featureScores[j] = 0;
            }
            if(j>=numberOfOriginalFeatures){
                FeatureScore fsAux = new FeatureScore(j,featureScores[j]);
                alFS.add(fsAux);    
            }
        }
    }

    /*
    * Estimate feature importance according to the coefficient of multiple 
    * correlation of the models in which the feature appears
    */
    private void computeFeatureImportanceBiasR2(LassoFit fit,ArrayList<FeatureScore> alFS){
        int indexWeights = 0;
        double rcoeff = 0;
        for(int i=0;i<fit.lambdas.length;i++){
            if(fit.rsquared[i]> rcoeff){
                indexWeights = i;
                rcoeff = fit.rsquared[i];
            }
        }
        
        double[] lassoWeights = fit.getWeights(indexWeights);
               
        for(int j=0;j<lassoWeights.length;j++){
            featureScores[j] = 0;
            if(lassoWeights[j]!=0){
                for(int z=0;z<fit.nonZeroWeights.length;z++){
                    double[] lassoWeightsAux = fit.getWeights(z);
                    if(lassoWeightsAux[j]!=0) {
                        //featureScores[j] += fit.rsquared[z]/(dataMatrix.getFeatureSize(j)*0.5);
                        featureScores[j] += fit.rsquared[z];
                    }
                }
            }
            if(j>=numberOfOriginalFeatures){
                FeatureScore fsAux = new FeatureScore(j,featureScores[j]);
                alFS.add(fsAux);    
            }
        }
    }    
    
    /*
    * Estimate feature importance according to the mean squared error of the
    * models in which the feature appears
    */
    private void computeFeatureImportanceBiasMSE(LassoFit fit,ArrayList<FeatureScore> alFS){
        double[][] dataMatrixAux = dataMatrix.getInputValues();
        double[] targetsAux = dataMatrix.getTargetValues();
        double minSqError = Double.MAX_VALUE;
        int indexLambdaMinError = 0;
        double[] mseLambdas = new double[fit.lambdas.length];
        for(int l=0;l<fit.lambdas.length;l++){    
            double interceptAux = fit.intercepts[l];
            double[] lassoWeightsAux = fit.getWeights(l);
            double sqError = 0;
            for (int i = 0; i < dataMatrix.getNumberOfFitnessCases(); i++) {
                double prediction = interceptAux;
                int indexFeature=0;
                for(int j=0;j<numberOfOriginalFeatures+numberOfArchiveFeatures+numberOfNewFeatures;j++){
                    prediction += dataMatrixAux[i][j]*lassoWeightsAux[indexFeature];
                    indexFeature++;
                }
                sqError += Math.pow(targetsAux[i] - prediction,2);
            }
            sqError = sqError / dataMatrix.getNumberOfFitnessCases();
            mseLambdas[l] = sqError;
            if((sqError<minSqError)){
                minSqError = sqError;
                indexLambdaMinError = l;
            }
        }
        
        double[] lassoWeights = fit.getWeights(indexLambdaMinError);

        for(int j=0;j<lassoWeights.length;j++){
            featureScores[j] = 0;
            if(lassoWeights[j]!=0){
                for(int l=0;l<fit.numberOfLambdas;l++){
                    double[] lassoWeightsAux = fit.getWeights(l);
                    if(lassoWeightsAux[j]!=0) {
                        featureScores[j] += 1/mseLambdas[l]; //check the bias, not sure it makes sense to add 1/mse;;;;;;
                    }
                }
            }
            if(j>=numberOfOriginalFeatures){
                FeatureScore fsAux = new FeatureScore(j,featureScores[j]);
                alFS.add(fsAux);    
            }
        }
    }

    /*
    * A coordinate descent method for the Lasso is run and the resulting
    * models are mined to estimate the importance of the features.
    */
    private void evalAllFeatures() throws Exception{
        LassoFitGenerator fitGenerator = new LassoFitGenerator();
        int numObservations = dataMatrix.getNumberOfFitnessCases();
        fitGenerator.init((numberOfOriginalFeatures + numberOfArchiveFeatures + numberOfNewFeatures), numObservations);
        for (int i = 0; i < numObservations; i++) {
            double[] row = dataMatrix.getRow(i);
            float[] rowFloats = new float[numberOfOriginalFeatures + numberOfArchiveFeatures + numberOfNewFeatures];
            for(int j=0;j<numberOfOriginalFeatures+numberOfArchiveFeatures+numberOfNewFeatures;j++){
                rowFloats[j]=(float)row[j];
            }
            fitGenerator.setObservationValues(i,rowFloats);
            fitGenerator.setTarget(i, dataMatrix.getTargetValues()[i]);
        }

        /*
         * Generate the Lasso fit. The -1 arguments means that there would be 
         * no limit on the maximum number of features per model.
         * We set the value to numberOfOriginalFeatures+numberOfArchiveFeatures to force selective pressure
         */
        LassoFit fit = fitGenerator.fit(numberOfOriginalFeatures+numberOfArchiveFeatures + numberOfNewFeatures,100);
        
        alWeights = null;
        alWeights = new ArrayList<String>();
        
        ArrayList<FeatureScore> alFS = new ArrayList<FeatureScore>();
        
        /*
        * These are variant to estimate feature importance
        */
        if(FITNESS_BIAS==0){//bias feature importance of feature with # of different Lambdas
            computeFeatureImportanceVariableCount(fit,alFS);
        }else if(FITNESS_BIAS==1){// bias feature importance of feature with r2 of different Lambdas
            computeFeatureImportanceBiasR2(fit,alFS);
        }else if(FITNESS_BIAS==2){// bias feature importance with MSE of different Lambdas
            computeFeatureImportanceBiasMSE(fit, alFS);
        }

        Collections.sort(alFS);
        for(int i=0;i<numberOfArchiveFeatures;i++){
            indicesArchive[i] = alFS.get(i).getIndex();
        }
        alFS.clear();
    }
    
    /*
    * Select the model that maximizes the coefficient of multiple correlation
    */
    private int getIndexLambdaModelSelectionR2(LassoFit fit){
        int indexLambda = 0;
        double rsquared=fit.rsquared[indexLambda];
        for(int i=1;i<fit.lambdas.length;i++){
            if(fit.rsquared[i]>rsquared){
                indexLambda = i;
            }
        }
        return indexLambda;
    }
        
    /*
    * Select the model that maximizes the mean squared error
    */    
    private int getIndexLambdaModelSelectionMSE(LassoFit fit){
        double[][] dataMatrixAux = dataMatrix.getInputValues();
        double[] targetsAux = dataMatrix.getTargetValues();
        double minSqError = Double.MAX_VALUE;
        int indexLambdaMinError = 0;
        for(int l=0;l<fit.lambdas.length;l++){
            double interceptAux = fit.intercepts[l];
            double[] lassoWeightsAux = fit.getWeights(l);
            double sqError = 0;
            for (int i = 0; i < dataMatrix.getNumberOfFitnessCases(); i++) {
                double prediction = interceptAux;
                int indexFeature =0;
                for(int j=0;j<numberOfOriginalFeatures+numberOfArchiveFeatures;j++){
                    prediction += dataMatrixAux[i][j]*lassoWeightsAux[indexFeature];
                    indexFeature++;
                }
                sqError += Math.pow(targetsAux[i] - prediction,2);
            }
            sqError = sqError / dataMatrix.getNumberOfFitnessCases();
            if(sqError<minSqError){
                minSqError = sqError;
                indexLambdaMinError = l;
            }
        }
        return indexLambdaMinError;
    }
    
    /*
    * Obtain a linear model with the best/selected features
    */
    private void getCurrentModelWeights() throws Exception{
        /*
         * LassoFitGenerator is initialized
         */
        LassoFitGenerator fitGenerator = new LassoFitGenerator();
        int numObservations = dataMatrix.getNumberOfFitnessCases();
        fitGenerator.init(numberOfOriginalFeatures+numberOfArchiveFeatures, numObservations);
        for (int i = 0; i < numObservations; i++) {
            double[] row = dataMatrix.getRow(i);
            float[] reducedRow = new float[numberOfOriginalFeatures+numberOfArchiveFeatures];
            int indexAddedFeature=0;
            for(int j=0;j<numberOfOriginalFeatures+numberOfArchiveFeatures+numberOfNewFeatures;j++){
                if(j<numberOfOriginalFeatures || archiveContains(j) ){
                    reducedRow[indexAddedFeature]=(float)row[j];
                    indexAddedFeature++;
                }
            }
            //fitGenerator.setObservationValues(i,dataMatrix.getRow(i));
            fitGenerator.setObservationValues(i,reducedRow);
            fitGenerator.setTarget(i, dataMatrix.getTargetValues()[i]);
            reducedRow = null;
        }

        /*
         * Generate the Lasso fit. The -1 arguments means that there would be 
         * no limit on the maximum number of features per model
         * We set the value to numberOfOriginalFeatures+numberOfArchiveFeatures to force selective pressure
         */
        LassoFit fit = fitGenerator.fit(maxFinalFeatures,100);
        
        int indexLambda = 0;
        /*
        * model selection criterion: 
        * we need to choose a value for the regularization coefficient Lambda
        */
        if(MODEL_SELECTION_BIAS==1){
            indexLambda = getIndexLambdaModelSelectionR2(fit);
        }else if(MODEL_SELECTION_BIAS==2){
            indexLambda = getIndexLambdaModelSelectionMSE(fit);
        }
       

        alWeights = null;
        alWeights = new ArrayList<String>();
        double[] lassoWeights = fit.getWeights(indexLambda);
        
        for(int j=0;j<lassoWeights.length;j++){
            alWeights.add(Double.toString(lassoWeights[j]));
        }
        lassoIntercept = fit.intercepts[indexLambda];
        
        // We compute the mean squared error of the selected model
        double[][] dataMatrixAux = dataMatrix.getInputValues();
        double[] targetsAux = dataMatrix.getTargetValues();
        double sqError = 0;
        double absError = 0;
        for (int i = 0; i < dataMatrix.getNumberOfFitnessCases(); i++) {
            double prediction = lassoIntercept;
            int indexFeature =0;
            for(int j=0;j<numberOfOriginalFeatures+numberOfArchiveFeatures+numberOfNewFeatures;j++){
                if(j<numberOfOriginalFeatures || archiveContains(j) ){
                    prediction += dataMatrixAux[i][j]*lassoWeights[indexFeature];
                    indexFeature++;
                }
            }
            sqError += Math.pow(targetsAux[i] - prediction,2);
            absError += Math.abs(targetsAux[i] - prediction);
        }
        sqError = sqError / dataMatrix.getNumberOfFitnessCases();
        absError = absError / dataMatrix.getNumberOfFitnessCases();
        
        double currentTime = System.currentTimeMillis() - startTime;
        currentTime = currentTime/1000;
        System.out.println("TIME IS: " + currentTime + " ; CURRENT MSE IS: " + sqError + " ; MAE IS: " + absError + " ; BEST MSE IS: " + BEST_MSE);
        
        // check if it is the best model so far
        CURRENT_MSE = sqError;
        if(CURRENT_MSE < BEST_MSE){
            BEST_MSE = CURRENT_MSE;
            STALL_ITERATIONS = 0;
            bestFeatures = null;
            bestFeatures = new ArrayList<String>();
            bestWeights = null;
            bestWeights = new ArrayList<Double>();
            bestIntercerpt = lassoIntercept;
            int indexFeature = 0;
            for(int j=0;j<numberOfOriginalFeatures+numberOfArchiveFeatures+numberOfNewFeatures;j++){
                if(j<numberOfOriginalFeatures || archiveContains(j) ){
                    String featureAux = dataMatrix.getFeatureString(j);
                    double weightAux = lassoWeights[indexFeature];
                    bestFeatures.add(featureAux);
                    bestWeights.add(weightAux);
                    indexFeature++;
                }
            }
        }else{
            STALL_ITERATIONS++;
        }
    }
    
    
    /*
    * compose new features from the features of the population + the original 
    * variables of the problem
    */
    private void generateNewFeatures(){    
        int indexStart = numberOfOriginalFeatures ;
        int indexEnd = numberOfOriginalFeatures + numberOfArchiveFeatures + numberOfNewFeatures;
        for(int j=indexStart;j<indexEnd;j++){
            if(!archiveContains(j)){
                int indexParent1 = tournamentSelection();
                if(r.nextFloat()< binary_recomb_rate){
                    int indexParent2 = tournamentSelection();
                    if((dataMatrix.getFeatureSize(indexParent1) + dataMatrix.getFeatureSize(indexParent2)) <maxFeatureSize){
                        binaryRecombination(j,indexParent1,indexParent2);
                    }else{
                        dataMatrix.setFeatureToZero(j);
                    }
                }else{
                    if(dataMatrix.getFeatureSize(indexParent1) < maxFeatureSize){
                        unaryRecombination(j,indexParent1);
                    }else{
                        dataMatrix.setFeatureToZero(j);
                    }
                }
            }
        }
    }
    
    /*
    * auxiliar method to check a given feature is part of the population
    */
    private boolean archiveContains(int index){
        boolean contains = false;
        for(int i=0;i<numberOfArchiveFeatures;i++){
            if(indicesArchive[i]==index){
                return true;
            }
        }
        return contains;
    }
    
    /*
    * tournament selection method
    */
    private int tournamentSelection(){
        int indexParent = r.nextInt(numberOfOriginalFeatures + numberOfArchiveFeatures);
        if(indexParent>=numberOfOriginalFeatures){
            indexParent = indicesArchive[indexParent-numberOfOriginalFeatures];
        }
        for(int i=0;i<tournamentSize-1;i++){
            int indexAux = r.nextInt(numberOfOriginalFeatures + numberOfArchiveFeatures);
            if(indexAux>=numberOfOriginalFeatures){
                indexAux = indicesArchive[indexAux-numberOfOriginalFeatures];
            }
            if(featureScores[indexAux] > featureScores[indexParent]) indexParent = indexAux;
        }
        return indexParent;
    }
    
    /*
    * composition of new features via binary functions
    */
    private void binaryRecombination(int indexNewFeature,int indexParent1,int indexParent2){
        int indexOp  = r.nextInt(binaryOps.length);
        switch (binaryOps[indexOp]) {
            case "*":
                dataMatrix.multiplication(indexNewFeature,indexParent1,indexParent2);
                break;
            case "mydiv":
                dataMatrix.division(indexNewFeature,indexParent1,indexParent2);
                break;
            case "+":
                dataMatrix.sum(indexNewFeature,indexParent1,indexParent2);
                break;
            case "-":
                dataMatrix.minus(indexNewFeature,indexParent1,indexParent2);
                break;
            default:
                break;
        }
    }

    /*
    * composition of new features via unary functions
    */
    private void unaryRecombination(int indexNewFeature,int indexParent1){
        int indexOp  = r.nextInt(unaryOps.length);
        switch (unaryOps[indexOp]) {
            case "mylog":
                dataMatrix.log(indexNewFeature,indexParent1);
                break;
            case "exp":
                dataMatrix.exp(indexNewFeature,indexParent1);
                break;
            case "mysqrt":
                dataMatrix.sqrt(indexNewFeature,indexParent1);
                break;
            case "square":
                dataMatrix.square(indexNewFeature,indexParent1);
                break;
            case "cube":
                dataMatrix.cube(indexNewFeature,indexParent1);
                break;
            case "cos":
                dataMatrix.cos(indexNewFeature,indexParent1);
                break;
            case "sin":
                dataMatrix.sin(indexNewFeature,indexParent1);
                break;
            default:
                break;
        }
    }
    
    /*
    * auxiliar method to save a string in a file
    */
    private void saveText(String filepath, String text, Boolean append) throws IOException {
        BufferedWriter bw = new BufferedWriter(new FileWriter(filepath,append));
        PrintWriter printWriter = new PrintWriter(bw);
        printWriter.write(text);
        printWriter.flush();
        printWriter.close();
    }
        
    /*
    * save the population of features during the run
    * Logging method for research purposes
    */
    private void saveCurrentFeatureSet() throws IOException{
        String featuresPath = "features_" + indexIteration + ".txt";
        this.saveText(featuresPath,"", false);
        for(int j=0;j<numberOfOriginalFeatures+numberOfArchiveFeatures+numberOfNewFeatures;j++){
            if(j<numberOfOriginalFeatures || archiveContains(j) ){
                this.saveText(featuresPath, dataMatrix.getFeatureString(j) + ",", true);
            }
        }
    }
    
    /*
    * save the model at the end of the iteration/generation
    * Logging method for research purposes
    */
    private void saveCurrentModel() throws IOException{
        System.out.println(indexIteration);
        String modelPath = "model_" + indexIteration + ".txt";
        this.saveText(modelPath, lassoIntercept + "\n", false);
        int indexFeature = 0;
        for(int j=0;j<numberOfOriginalFeatures+numberOfArchiveFeatures+numberOfNewFeatures;j++){
            if(j<numberOfOriginalFeatures || archiveContains(j) ){
                this.saveText(modelPath, " + " + alWeights.get(indexFeature) + " * " + dataMatrix.getFeatureString(j) + "\n", true);
                indexFeature++;
            }
        }
    }
    
    /*
    * save the population of features at the end of the run
    */
    private void saveBestFeatureSet(boolean finished) throws IOException{
        String featuresPath = "features_" + indexIteration + ".txt";
        if (finished) featuresPath = "features.txt";
        this.saveText(featuresPath,"", false);
        for(int j=0; j<bestFeatures.size();j++){
            this.saveText(featuresPath, bestFeatures.get(j) + ",", true);
        }
    }
  
    /*
    * save the model at the end of the run
    */
    private void saveBestModel(boolean finished) throws IOException{
        String modelPath = "model_" + indexIteration + ".txt";
        if (finished) modelPath = "model.txt";
        this.saveText(modelPath, dataMatrix.getTargetMin() + "," + dataMatrix.getTargetMax() + "\n", false);
        this.saveText(modelPath, bestIntercerpt + "\n", true);
        for(int j=0; j<bestFeatures.size();j++){
            if(bestWeights.get(j) != 0){
                this.saveText(modelPath, " + " + bestWeights.get(j) + " * " + bestFeatures.get(j) + "\n", true);
            }
        }
    }
    
    /*
    * auxiliary class to sort features according to their estimated score/importance
    */
    private class FeatureScore implements Comparable<FeatureScore>{
        private int index;
        private double score;
        
        public FeatureScore(int anIndex, double aScore){
            index = anIndex;
            score = aScore;
        }
        
        public int getIndex(){
            return index;
        }
        
        public double getScore(){
            return score;
        }
        
        @Override
        public int compareTo(FeatureScore other){
            int comp = 0;
            if(this.score>other.getScore()){
                comp=-1;
            }else if (this.score<other.getScore()){
                comp = 1;
            }
            return comp;
        }
    }
}

