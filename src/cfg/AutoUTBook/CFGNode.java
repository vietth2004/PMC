package cfg.AutoUTBook;

import cfg.object.ICfgNode;
import org.eclipse.cdt.core.dom.ast.IASTFileLocation;
import org.eclipse.cdt.internal.core.dom.parser.ASTNode;

import java.util.ArrayList;
import java.util.List;

public class CFGNode
{
    private String content;
    private ICfgNode trueNode;

    private ICfgNode falseNode;

    private ICfgNode parentNode;

    private boolean isVisited;

    private int id;

    private ASTNode ast;
}
