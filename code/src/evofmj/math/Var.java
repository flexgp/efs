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
 * @author Owen Derby
 * 
 */
package evofmj.math;


import evofmj.efm.GPException;
import java.util.List;

/**
 *
 * @author nacho
 */
public class Var extends ZeroArgFunction {

	private final int ind;

    /**
     *
     * @param label
     * @throws GPException
     */
    public Var(String label) throws GPException {
            super(label);
            if (label.startsWith("X")) {
                String numPart = label.substring(1);
                ind = Integer.parseInt(numPart) - 1; // zero-index
            } else if (label.equals("x")) {
                ind = 0;
            } else if (label.equals("y")) {
                ind = 1;
            } else {
                throw new GPException("Unknonwn variable: " + label);
            }
	}

	@Override
	public Double eval(List<Double> t) {
            return t.get(ind);
	}

    /**
     *
     * @return
     */
    public static String getInfixFormatString() {
            return "%s";
	}
}
