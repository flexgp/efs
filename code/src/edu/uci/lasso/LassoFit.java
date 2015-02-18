/**
 * This implemenation is based on: Friedman, J., Hastie, T. and Tibshirani, R.
 * (2008) Regularization Paths for Generalized Linear Models via Coordinate
 * Descent. http://www-stat.stanford.edu/~hastie/Papers/glmnet.pdf
 * 
 * @author Yasser Ganjisaffar
 */

package edu.uci.lasso;

/**
 *
 * @author Yasser Ganjisaffar
 */
public class LassoFit {
    
    /**
    * Number of lambda values
    */
    public int numberOfLambdas;

    /**
    * Intercepts
    */
    public double[] intercepts;

    /**
    * Compressed weights for each solution
    */
    public double[][] compressedWeights;

    /**
    * Pointers to compressed weights
    */
    public int[] indices;

    /**
    * Number of weights for each solution
    */
    public int[] numberOfWeights;

    /**
    * Number of non-zero weights for each solution
    */
    public int[] nonZeroWeights;

    /**
    * The value of lambdas for each solution
    */
    public double[] lambdas;

    /**
    * R^2 value for each solution
    */
    public double[] rsquared;

    /**
     * Total number of passes over data
     */
    public int numberOfPasses;

    private int numFeatures;

    /**
     *
     * @param numberOfLambdas
     * @param maxAllowedFeaturesAlongPath
     * @param numFeatures
     */
    public LassoFit(int numberOfLambdas, int maxAllowedFeaturesAlongPath, int numFeatures) {
        intercepts = new double[numberOfLambdas];
        compressedWeights = MathUtil.allocateDoubleMatrix(numberOfLambdas, maxAllowedFeaturesAlongPath);
        indices = new int[maxAllowedFeaturesAlongPath];
        numberOfWeights = new int[numberOfLambdas];
        lambdas = new double[numberOfLambdas];
        rsquared = new double[numberOfLambdas];
        nonZeroWeights = new int[numberOfLambdas];
        this.numFeatures = numFeatures;
    }

    /**
     *
     * @param lambdaIdx
     * @return
     */
    public double[] getWeights(int lambdaIdx) {
        double[] weights = new double[numFeatures];
        for (int i = 0; i < numberOfWeights[lambdaIdx]; i++) {
            weights[indices[i]] = compressedWeights[lambdaIdx][i];
        }
        return weights;
    }

}
