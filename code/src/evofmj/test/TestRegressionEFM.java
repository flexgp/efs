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
package evofmj.test;

import evofmj.evaluation.java.EFMScaledData;
import evofmj.genotype.Tree;
import evofmj.genotype.TreeGenerator;
import evofmj.math.Function;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Implements fitness evaluation for symbolic regression.
 * 
 * @author Ignacio Arnaldo
 */
public class TestRegressionEFM {
    
    private String pathToTestData;
    private EFMScaledData testData;    
    private String pathToModel;
    ArrayList<Tree> alFeatures; 
    ArrayList<Double> alWeights;
    double intercept, minTarget,maxTarget;
    private boolean round;
    
    /**
     * Test models obtained with the EFM method
     * Complex features are mapped to expression trees, evaluated via the 
     * standard inorder parsing used in tree-based Genetic Programming
     * @param aPathToTestData
     * @param aPathToModel
     * @param aRound
     * @throws java.io.IOException
     * @throws java.lang.ClassNotFoundException
     */
    public TestRegressionEFM( String aPathToTestData, String aPathToModel, boolean aRound) throws IOException, ClassNotFoundException {
        pathToTestData = aPathToTestData;
        pathToModel = aPathToModel;
        round = aRound;
        testData = new EFMScaledData(pathToTestData);
        alFeatures = new ArrayList<Tree>(); 
        alWeights = new ArrayList<Double>();
        intercept = 0;
        readModel();
    }

    /*
    * auxiliary class to read a model from file
    */
    private void readModel() throws IOException, ClassNotFoundException{
        
        Scanner sc = new Scanner(new FileReader(pathToModel));
        String lineMinMax = sc.nextLine();
        String[] minMax = lineMinMax.split(",");
        minTarget = Double.valueOf(minMax[0]);
        maxTarget = Double.valueOf(minMax[1]);
        intercept = Double.valueOf(sc.nextLine());
        while(sc.hasNextLine()){
            String sAux = sc.nextLine();
            sAux = sAux.trim();
            String[] tokens = sAux.split(" ");
            double wAux = Double.valueOf(tokens[1]);
            alWeights.add(wAux);
            
            String featureStringAux = "";
            for(int i=3;i<tokens.length;i++){
                featureStringAux += tokens[i] + " ";
            }
            featureStringAux = featureStringAux.trim();
            Tree g = TreeGenerator.generateTree(featureStringAux);
            alFeatures.add(g);
        }
    }
    
   
    /**
     * @see eval an EFM model
     */
    public void evalModel() {
        double sqDiff = 0;
        double absDiff = 0;
        double[][] inputValuesAux = testData.getInputValues();
        double[] targets = testData.getTargetValues();
        
        for (int i = 0; i < testData.getNumberOfFitnessCases(); i++) {
            List<Double> d = new ArrayList<Double>();
            for (int j = 0; j < testData.getNumberOfOriginalFeatures(); j++) {
                d.add(j, (double)inputValuesAux[i][j]);
            }
            double prediction = intercept;
            for (int j = 0; j < alFeatures.size(); j++) {
                Tree genotype = (Tree) alFeatures.get(j);
                Function func = genotype.generate();
                double funcOutput = func.eval(d);
                if(Double.isNaN(funcOutput) || Double.isInfinite(funcOutput)){
                    funcOutput=0;
                }
                if(alWeights.get(j)!=0){
                    prediction += alWeights.get(j) * funcOutput;
                }
                func = null;
            }
            if(prediction<minTarget) prediction = minTarget;
            if(prediction>maxTarget) prediction = maxTarget;
            if (round) prediction = Math.round(prediction);
            d.clear();
            sqDiff += Math.pow(targets[i] - prediction, 2);
            absDiff += Math.abs(targets[i] - prediction);
        }
        sqDiff = sqDiff / testData.getNumberOfFitnessCases();
        absDiff= absDiff / testData.getNumberOfFitnessCases();
        System.out.println("MSE: " + sqDiff);
        System.out.println("MAE: " + absDiff);
    }

}