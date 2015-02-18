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
 * @author Owen Derby and Ignacio Arnaldo
 * 
 */

package evofmj.math;

import java.util.List;

/**
 *
 * @author nacho
 */
public class Divide extends TwoArgFunction {

    /**
     *
     * @param a1
     * @param a2
     */
    public Divide(Function a1, Function a2) {
        super(a1, a2);
    }

    @Override
    public Double eval(List<Double> t) {
        Double denom = arg2.eval(t);
        if (Math.abs(denom) < 1e-6) {
            return (double) 1; // cc Silva 2008 thesis
        } else {
            return arg1.eval(t) / denom;
        }
    }

    /**
     *
     * @return
     */
    public static String getInfixFormatString() {
        //return "mydivide(%s, %s)";
        return "(mydivide %s %s)";
    }
}
