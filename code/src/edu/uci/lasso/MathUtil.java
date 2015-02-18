/**
 * This implemenation is based on: Friedman, J., Hastie, T. and Tibshirani, R.
 * (2008) Regularization Paths for Generalized Linear Models via Coordinate
 * Descent. http://www-stat.stanford.edu/~hastie/Papers/glmnet.pdf
 * 
 * @author Yasser Ganjisaffar
 */

package edu.uci.lasso;

import java.util.List;

/**
 *
 * @author Yasser Ganjisaffar
 */
public class MathUtil {

    /**
     *
     * @param arr
     * @return
     */
    public static double getAvg(double[] arr) {
                double sum = 0;
                for (double item : arr) {
                        sum += item;
                }
                return sum / arr.length;
        }

    /**
     *
     * @param arr
     * @return
     */
    public static double getAvg(float[] arr) {
                double sum = 0;
                for (double item : arr) {
                        sum += item;
                }
                return sum / arr.length;
        }

    /**
     *
     * @param arr
     * @return
     */
    public static double getAvg(List<Double> arr) {
                double sum = 0;
                for (double item : arr) {
                        sum += item;
                }
                return sum / arr.size();
        }

    /**
     *
     * @param arr
     * @return
     */
    public static double getStg(double[] arr) {
                return getStd(arr, getAvg(arr));
        }

    /**
     *
     * @param arr
     * @return
     */
    public static double getStg(List<Double> arr) {
                return getStd(arr, getAvg(arr));
        }

    /**
     *
     * @param arr
     * @param avg
     * @return
     */
    public static double getStd(double[] arr, double avg) {
                double sum = 0;
                for (double item : arr) {
                        sum += Math.pow(item - avg, 2);
                }
                return Math.sqrt(sum / arr.length);
        }

    /**
     *
     * @param arr
     * @param avg
     * @return
     */
    public static double getStd(List<Double> arr, double avg) {
                double sum = 0;
                for (double item : arr) {
                        sum += Math.pow(item - avg, 2);
                }
                return Math.sqrt(sum / arr.size());
        }

    /**
     *
     * @param vector1
     * @param vector2
     * @param length
     * @return
     */
    public static double getDotProduct(float[] vector1, float[] vector2, int length) {
                double product = 0;
                for (int i = 0; i < length; i++) {
                        product += vector1[i] * vector2[i];
                }
                return product;
        }

    /**
     *
     * @param vector1
     * @param vector2
     * @param length
     * @return
     */
    public static double getDotProduct(double[] vector1, double[] vector2, int length) {
                double product = 0;
                for (int i = 0; i < length; i++) {
                        product += vector1[i] * vector2[i];
                }
                return product;
        }

    /**
     *
     * @param vector1
     * @param vector2
     * @return
     */
    public static double getDotProduct(float[] vector1, float[] vector2)
    {
        return getDotProduct(vector1, vector2, vector1.length);
    }
        
        // Divides the second vector from the first one (vector1[i] /= val)

    /**
     *
     * @param vector
     * @param val
     */
        public static void divideInPlace(float[] vector, float val)
    {
        int length = vector.length;
        for (int i = 0; i < length; i++)
        {
                vector[i] /= val;
        }
    }
    
    /**
     *
     * @param m
     * @param n
     * @return
     */
    public static double[][] allocateDoubleMatrix(int m, int n) {
        double[][] mat = new double[m][];
        for (int i = 0; i < m; i++) {
            mat[i] = new double[n];
        }
        return mat;
    }
}

