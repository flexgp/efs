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

import java.util.ArrayList;
import java.util.List;

/**
 * Class to store the boolean predictions of a classifier
 * @author Ignacio Arnaldo
 */
public class BooleanPhenotype extends Phenotype {
    
    private static final long serialVersionUID = 4665885502138691362L;

    
    private List<Boolean> dataValues;

    /**
     * Constructor - the predictions are stored as an array list
     */
    public BooleanPhenotype() {
        dataValues = new ArrayList<Boolean>();
    }

    /**
     *
     * @param i
     * @return
     */
    public Boolean getDataValue(int i) {
		return dataValues.get(i);
	}

    /**
     *
     * @param dataValue
     */
    public void setDataValue(Boolean dataValue) {
		this.dataValues.add(dataValue);
	}

    /**
     *
     * @return
     */
    public int size() {
		return dataValues.size();
	}

	@Override
	public String toString() {
		return dataValues.toString();
	}

	@Override
	public Phenotype copy() {
		BooleanPhenotype b = new BooleanPhenotype();
		b.dataValues = new ArrayList<Boolean>();
		b.dataValues.addAll(this.dataValues);
		return b;
	}
}
