package utils.search;

import tree.object.ExternalVariableNode;
//import com.dse.parser.object.INode;
//import com.dse.search.SearchCondition;
import tree.object.INode;

/**
 * Represent global or extern variable, e.g., "int MY_MAX_VALUE"
 *
 * @author TungLam
 */
public class GlobalVariableNodeCondition extends SearchCondition {

    @Override
    public boolean isSatisfiable(INode n) {
        return n instanceof ExternalVariableNode;
    }
}
