package coverage;

import cfg.CFGGenerationforBranchvsStatementCoverage;
import cfg.CFGGenerationforSubConditionCoverage;
import cfg.ICFG;
import cfg.ICFGGeneration;
import tree.object.AbstractFunctionNode;
import tree.object.IFunctionNode;
import tree.object.INode;

import java.util.HashMap;
import java.util.Map;

public class StaticFunctionCoverageComputation extends AbstractCoverageComputation
{
    ICFG cfg;

    public StaticFunctionCoverageComputation(ICFG cfg)
    {
        this.cfg = cfg;
    }

    protected IFunctionNode functionNode;

    @Override
    protected Map<String, TestpathsOfAFunction> removeRedundantTestpath(Map<String, TestpathsOfAFunction> affectedFunctions)
    {
        Map<String, TestpathsOfAFunction> output = new HashMap<>();
        String path = functionNode.getAbsolutePath();
        output.put(path, affectedFunctions.get(path));
        return output;
    }

    @Override
    protected int getNumberofBranches(INode consideredSourcecodeNode)
    {
        int nBranches = 0;
        if (functionNode instanceof AbstractFunctionNode)
        {
            try
            {
                if (cfg != null)
                {
                    nBranches += cfg.computeNumOfBranches();
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }

        return nBranches;
    }

    @Override
    protected int getNumberofStatements(INode consideredSourcecodeNode)
    {
        int nStatements = 0;
        if (functionNode instanceof AbstractFunctionNode)
        {
            try
            {
                nStatements += cfg.computeNumofStatements();
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }

        return nStatements;
    }

    public void setFunctionNode(IFunctionNode functionNode)
    {
        this.functionNode = functionNode;
    }

    public IFunctionNode getFunctionNode()
    {
        return functionNode;
    }
}
