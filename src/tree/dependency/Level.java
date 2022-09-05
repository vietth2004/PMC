package tree.dependency;

import java.util.ArrayList;
import java.util.List;

import tree.object.INode;
import tree.object.Node;

/**
 * A level represents a list of equivalent
 *
 * @author DucAnh
 */
public class Level extends ArrayList<INode> {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    public static final String STRUCTUTRE_AND_NAMESPACE_SCOPE = "STRUCTUTRE AND NAMESPACE SCOPE";
    public static final String FILE_SCOPE = "FILE SCOPE";
    public static final String INCLUDED_SCOPE = "INCLUDED SCOPE";
    public static final String EXTENDED_INCLUDED_SCOPE = "EXTENDED INCLUDED SCOPE";

    private String name;

    public Level() {

    }

    public Level(List<INode> node) {
        this.addAll(node);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        StringBuilder output = new StringBuilder();
        for (INode n : this)
            output.append(n.getAbsolutePath()).append(", ");
        return output.toString();
    }
}
