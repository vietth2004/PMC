package tree.dependency;


import tree.object.INode;

public class GlobalVariableDependency extends Dependency{
    public GlobalVariableDependency(INode startArrow, INode endArrow) {
        super(startArrow, endArrow);
    }

    public boolean fromNode(INode func) {
        return this.startArrow == func;
    }

    public boolean toNode(INode func) {
        return this.endArrow == func;
    }
}
