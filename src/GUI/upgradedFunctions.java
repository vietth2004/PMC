package GUI;

import tree.object.INode;

public class upgradedFunctions
{
    private INode functionv1;
    private INode functionv2;

    public upgradedFunctions(INode funcv1, INode funcv2)
    {
        functionv1 = funcv1;
        functionv2 = funcv2;
    }

    public INode getFunctionv1()
    {
        return functionv1;
    }

    public void setFunctionv1(INode functionv1)
    {
        this.functionv1 = functionv1;
    }

    public INode getFunctionv2()
    {
        return functionv2;
    }

    public void setFunctionv2(INode functionv2)
    {
        this.functionv2 = functionv2;
    }
}
