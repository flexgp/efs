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

package evofmj.genotype;
import java.util.StringTokenizer;

/**
 * Factory class for creating Trees.
 * 
 * @author Owen Derby and Ignacio Arnaldo
 * @see Tree
 */
public class TreeGenerator {

    /**
     * Create the factory
     */
    public TreeGenerator() {

    }

    /**
     * Generate a tree from a string. The string is assumed to be a LISP-like
     * S-expression, using prefix notation for operators.
     * 
     * @param input The S-expression string.
     * @return new tree representing the input string.
     */
    public static Tree generateTree(String input) {
        // Make sure the string is tokenizable
        // FIXME allow other delimiters?
        input = input.replace("(", " ( ");
        input = input.replace("[", " [ ");
        input = input.replace(")", " ) ");
        input = input.replace("]", " ] ");

        StringTokenizer st = new StringTokenizer(input);
        TreeNode holder = new TreeNode(null, "holder");
        parseString(holder, st);
        return new Tree(holder);
    }

    /**
     * parse a String and add the parsed functions and variables to the tree
     */    
    private static void parseString(TreeNode parent, StringTokenizer st) {
        while (st.hasMoreTokens()) {
            String currTok = st.nextToken().trim();
            switch (currTok) {
                case "":
                    break;
                case "(":
                case "[":
                    {
                        // The next token is the parent of a new subtree
                        currTok = st.nextToken().trim();
                        TreeNode newNode = new TreeNode(parent, currTok);
                        parent.addChild(newNode);
                        parseString(newNode, st);
                        break;
                    }
                case ")":
                case "]":
                    // Finish this subtree
                    return;
                default:
                    {
                        // An ordinary child node: add it to parent and continue.
                        TreeNode newNode = new TreeNode(parent, currTok);
                        parent.addChild(newNode);
                        break;
                    }
            }
        }
    }

}
