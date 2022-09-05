package parser.projectparser.externalvariable;

import interfaces.ISearch;
import tree.object.IFunctionNode;
import tree.object.IVariableNode;

import java.util.List;

/**
 * Find all external variables of a function
 *
 * @author ducanhnguyen
 */
public interface IVariableDetecter extends ISearch
{
    /**
     * Find external variables of a function
     *
     * @return
     */
    List<IVariableNode> findVariables();

    IFunctionNode getFunction();

    void setFunction(IFunctionNode function);
}