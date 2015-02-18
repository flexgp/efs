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

package main;

import evofmj.algorithm.RegressionEFM;
import evofmj.evaluation.java.EFMScaledData;
import evofmj.test.TestRegressionEFM;
import java.io.File;
import java.io.IOException;


/**
 * wrapper class to manage the train and test functionality of the EFM method
 * @author Ignacio Arnaldo
 */
public class RegEFMMenuManager {
    
    /**
     * void constructor
     */
    public RegEFMMenuManager(){
        
    }
    
    /**
     * print usage of the EFM method
     */
    public void printUsage(){
        System.err.println();
        System.err.println("USAGE:");
        System.err.println();
        System.err.println("TRAIN:");
        System.err.println("java -jar efm.jar -train path_to_data -minutes min");
        System.err.println();
        System.err.println("TEST:");
        System.err.println("java -jar efm.jar -test path_to_test_data path_to_model");
        System.err.println();
    }
    
    /**
     * parse arguments to train a EFM model
     * @param args
     * @throws IOException
     * @throws Exception
     */
    public void parseRegEFMTrain(String args[]) throws IOException, Exception{
        String dataPath;
        double numMinutes;
        if(args.length==4){
            dataPath = args[1];
            if (args[2].equals("-minutes")) {
                numMinutes = Double.valueOf(args[3]);
                if(numMinutes==0){
                    EFMScaledData data = new EFMScaledData(0, 0, dataPath);
                    int numberOfOriginalFeatures = data.getNumberOfOriginalFeatures();
                    int numArchiveFeatures = 0;
                    int numNewFeatures = 0;
                    int maxFeatureSize = 5;
                    RegressionEFM rEFM = new RegressionEFM(dataPath, numArchiveFeatures, numNewFeatures,maxFeatureSize,numberOfOriginalFeatures);
                    rEFM.runEFM(numMinutes*60);
                }else{
                    EFMScaledData data = new EFMScaledData(0, 0, dataPath);
                    int numberOfOriginalFeatures = data.getNumberOfOriginalFeatures();
                    int numArchiveFeatures = 3*numberOfOriginalFeatures;
                    int numNewFeatures = numberOfOriginalFeatures;
                    int maxFeatureSize = 5;
                    int numberOfFinalFeatures = numberOfOriginalFeatures + numArchiveFeatures;
                    //int numberOfFinalFeatures = numberOfOriginalFeatures ;
                    RegressionEFM rEFM = new RegressionEFM(dataPath, numArchiveFeatures, numNewFeatures,maxFeatureSize,numberOfFinalFeatures);
                    rEFM.runEFM(numMinutes*60);
                }
            }else{
                System.err.println("Error: must specify the optimization time in minutes");
                printUsage();
            } 

        }else{
            System.err.println("Error: wrong number of arguments");
            printUsage();
        }
    }
    
    /**
     * parse arguments to test a EFM model
     * @param args
     * @throws IOException
     * @throws ClassNotFoundException
     */
    public void parseRegEFMTest(String args[]) throws IOException, ClassNotFoundException{
        String testDataPath;
        String popPath;
        if (args.length==3){
            testDataPath = args[1];
            // check if knee model exists
            popPath = args[2];
            System.out.println();
            if(new File(popPath).isFile()){
                System.out.println("TESTING MODEL:");
                TestRegressionEFM trefm = new TestRegressionEFM(testDataPath,popPath,true);
                trefm.evalModel();
                System.out.println();
            }
        }else{
            System.err.println("Error: wrong number of arguments");
            printUsage();
        }
        
    }

    /**
     * parse arguments of a call to EFM.jar from the command line
     * @param args
     * @throws IOException
     * @throws ClassNotFoundException
     * @throws Exception
     */
    public static void main(String args[]) throws IOException, ClassNotFoundException, Exception{
        RegEFMMenuManager m = new RegEFMMenuManager();
        if (args.length == 0) {
            System.err.println("Error: too few arguments");
            m.printUsage();
            System.exit(-1);
        }else{
            switch (args[0]) {
                case "-train":
                    m.parseRegEFMTrain(args);
                    break;
                case "-test":
                    m.parseRegEFMTest(args);
                    break;
                default:
                    System.err.println("Error: unknown argument");
                    m.printUsage();
                    System.exit(-1);
                    break;
            }
        }
    }
}
