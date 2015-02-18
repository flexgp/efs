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

import java.io.Serializable;

/**
 * In GP, phenotypes represent the observational status of individuals. Since
 * such observables are highly problem/domain specific, this class is currently
 * an empty abstract class for organizational purposes. It might someday hold
 * functions relevant to all phenotypes.
 * 
 * @author Owen Derby
 */
public abstract class Phenotype implements Serializable {
	private static final long serialVersionUID = -1110082525231499386L;

	/**
	 * Perform a deep-copy of the phenotype
	 * 
	 * @return new phenotype
	 */
	public abstract Phenotype copy();

}
