package utils.search;

import cfg.CFG;
import cfg.CFGGenerationforBranchvsStatementCoverage;
import cfg.object.*;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTInitializer;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPVariable;
import org.eclipse.cdt.internal.core.dom.parser.cpp.*;
import parser.projectparser.ICommonFunctionNode;
import tree.dependency.Level;
import tree.object.*;
import utils.Utils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Search implements ISearch
{
    private static final int MAX_ITERATIONS = 20;

    public synchronized static INode searchFirstNodeByName(INode parent, String name)
    {
        for (INode child : parent.getChildren())
        {
            String nameChild = "";
            if (child instanceof IFunctionNode)
            {
                nameChild = ((FunctionNode) child).getSimpleName();
            }
            else
            {
                nameChild = child.getNewType();
            }

            if (nameChild.equals(name))
            {
                return child;
            }
        }
        return null;
    }

    public synchronized static <T extends INode> List<T> searchNodes(INode root,
                                                                     List<SearchCondition> conditions) {
        List<T> output = new ArrayList<>();

        for (INode child : root.getChildren()) {
            boolean isSatisfiable = false;

            for (ISearchCondition con : conditions)
                if (con.isSatisfiable(child)) {
                    isSatisfiable = true;
                    break;
                }

            if (isSatisfiable) {
                try {
                    output.add((T) child);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
            output.addAll(Search.searchNodes(child, conditions));
        }
        return output;
    }

    public synchronized static <T extends INode> List<T> searchNodes(INode root,
                                                                     ISearchCondition condition) {
        List<T> output = new ArrayList<>();
        try {
            for (INode child : root.getChildren()) {
                if (condition.isSatisfiable(child)) {
                    try {
                        output.add((T) child);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }

                output.addAll(Search.searchNodes(child, condition));
            }
            return output;
        } catch (Exception e) {
            return output;
        }
    }

    public synchronized static <T extends INode> List<T> searchNodes(INode root,
                                                                     ISearchCondition condition, String relativePath) {
        List<T> output = Search.searchNodes(root, condition);
        relativePath = Utils.normalizePath(relativePath);
        if (Utils.isUnix() || Utils.isMac())
            if (!relativePath.startsWith(File.separator))
                relativePath = File.separator + relativePath;

        List<T> returnOuput = new ArrayList<>();
        for (INode node : output) {
            if (node.getAbsolutePath().endsWith(relativePath)) {
                try {
                    returnOuput.add((T) node);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }
        return returnOuput;
    }
    public static <T extends INode> List<T> searchInSpace(List<Level> spaces, ISearchCondition c) {
        List<INode> children = new ArrayList<>();

        for (Level l : spaces) {
            for (INode n : l) {
                if (n != null) {
                    children.addAll(Search.searchNodes(n, c));
                }
            }
        }

        return children.stream()
                .distinct()
                .map(n -> (T) n)
                .collect(Collectors.toList());
    }
    public static List<INode> searchInSpace(List<Level> spaces, ISearchCondition c, String searchedPath) {
        List<INode> potentialCorrespondingNodes = new ArrayList<>();

        List<INode> children = new ArrayList<>();

        for (Level l : spaces) {
            for (INode n : l) {
                if (n != null) {
                    children.addAll(n.getChildren());
                }
            }
        }

        int iteration = 0;

        while (iteration <= MAX_ITERATIONS) {
            iteration++;

            List<INode> tempList = new ArrayList<>();

            for (INode child : children) {
                if (c.isSatisfiable(child)) {
                    if (child.getAbsolutePath().endsWith(searchedPath)) {
                        String[] targetItems, sourceItems;
                        if (Utils.isWindows()) {
                            targetItems = searchedPath.split("\\\\");
                            sourceItems = child.getAbsolutePath().split("\\\\");
                        } else {
                            targetItems = searchedPath.split(File.separator);
                            sourceItems = child.getAbsolutePath().split(File.separator);
                        }
                        if (targetItems[targetItems.length - 1].equals(sourceItems[sourceItems.length - 1])) {
                            if (!potentialCorrespondingNodes.contains(child))
                                potentialCorrespondingNodes.add(child);
                        }
                    }
                }

                tempList.add(child);
            }

            /*
             * Case NamespaceTest.cpp/ns1/ns2/Level2MultipleNsTest(::X,::ns1::X,X)
             * ::X -> lowest level
             */
            if (searchedPath.startsWith(File.separator)
                    && searchedPath.indexOf(File.separator) == searchedPath.lastIndexOf(File.separator)) {
                potentialCorrespondingNodes.removeIf(node -> node.getParent() instanceof StructureNode
                        || node.getParent() instanceof NamespaceNode);
            }

            if (potentialCorrespondingNodes.size() > 0)
                break;
            else {
                children.clear();

                for (INode node : tempList)
                    if (node instanceof ISourcecodeFileNode
                            || node instanceof StructureNode
                            || node instanceof TypedefDeclaration
                            || node instanceof NamespaceNode)
                        children.addAll(node.getChildren());
            }
        }

        potentialCorrespondingNodes.removeIf(n -> {
            if (n instanceof ClassNode) {
                return ((ClassNode) n).isTemplate() && n.getParent() instanceof ClassNode
                        && ((ClassNode) n).getAST().equals(((ClassNode) n.getParent()).getAST());
            } else if (n instanceof ICommonFunctionNode) {
                return n.getParent() instanceof ICommonFunctionNode;
            }

            return false;
        });

        return potentialCorrespondingNodes;
    }


    public synchronized static List<INode> getAllNodes(INode root, ISearchCondition condition)
    {
        List<INode> output = Search.searchNodes(root, condition);
        return output;
    }

    //chỉ lấy những hàm không gọi những hàm khác
    public synchronized static List<INode> getAllUnitNodes(INode root, ISearchCondition condition) throws Exception
    {
        List<INode> output = Search.searchNodes(root, condition);

        List<INode> ret = new ArrayList<>();

        for (int i = 0; i < output.size(); i++)
        {
            Boolean found = false;
            INode node = output.get(i);

            CFGGenerationforBranchvsStatementCoverage cfgGen =
                    new CFGGenerationforBranchvsStatementCoverage((IFunctionNode) node);

            CFG cfg = (CFG) cfgGen.generateCFG();

            for (
                    ICfgNode child :
                    cfg.getAllNodes()
            )
            {
                if (child instanceof SimpleCfgNode)
                {
                    if (((SimpleCfgNode) child).getAst() instanceof CPPASTExpressionStatement)
                    {
                        if (((CPPASTExpressionStatement) ((SimpleCfgNode) child).getAst()).getExpression()
                                instanceof CPPASTFunctionCallExpression)
                        {
                            found = true;
                            break;
                        }
                    }
                }
            }
            if (!found)
            {
                ret.add(node);
            }
        }

        return ret;
    }

    //chỉ lấy những hàm không gọi những hàm khác
    //Hàm này cần chứa tối thiểu 1 lệnh rẽ nhánh, loop, switch
    public synchronized static List<INode> getAllUnitNodesWithBranches(INode root, ISearchCondition condition) throws Exception
    {
        List<INode> output = Search.searchNodes(root, condition);

        List<INode> ret = new ArrayList<>();

        for (int i = 0; i < output.size(); i++)
        {
            Boolean functionCallFound = false;
            Boolean ifLoopSwitchFound = false;
            INode node = output.get(i);

            List<IVariableNode> variables = ((IFunctionNode) node).getArguments();
            if (variables.size() == 0 || (variables.size() == 1 && variables.get(0).getRawType().equals("void")))
            {
                continue;
            }

            CFGGenerationforBranchvsStatementCoverage cfgGen =
                    new CFGGenerationforBranchvsStatementCoverage((IFunctionNode) node);

            CFG cfg = (CFG) cfgGen.generateCFG();

            if (node.getName().contains("uv_insert_pending_req"))
            {
                int t = 0;
                System.out.println("t = " + t);
            }

            for (
                    ICfgNode child :
                    cfg.getAllNodes()
            )
            {

                if (child instanceof SimpleCfgNode)
                {
                    if (((SimpleCfgNode) child).getAst() instanceof CPPASTExpressionStatement)
                    {
                        if (((CPPASTExpressionStatement) ((SimpleCfgNode) child).getAst()).getExpression()
                                instanceof CPPASTFunctionCallExpression)
                        {
                            functionCallFound = true;
                            break;
                        }

                        IASTNode[] node1 =
                                ((CPPASTExpressionStatement) ((SimpleCfgNode) child).getAst()).getExpression().getChildren();

                        for (
                                IASTNode temp :
                                node1
                        )
                        {
                            if (temp instanceof CPPASTFunctionCallExpression)
                            {
                                functionCallFound = true;
                                break;
                            }

                        }
                    }

                    if (functionCallFound == true)
                    {
                        break;
                    }

                    if (((SimpleCfgNode) child).getAst() instanceof CPPASTDeclarationStatement)
                    {
                        CPPASTDeclarationStatement declarationStatement =
                                (CPPASTDeclarationStatement) ((SimpleCfgNode) child).getAst();

                        IASTDeclaration declaration = declarationStatement.getDeclaration();

                        if (declaration instanceof CPPASTSimpleDeclaration)
                        {
                            IASTDeclarator[] declarators = ((CPPASTSimpleDeclaration) declaration).getDeclarators();

                            for (
                                    IASTDeclarator temp :
                                    declarators
                            )
                            {
                                IASTInitializer initializer = temp.getInitializer();

                                if (initializer != null)
                                {
                                    IASTNode[] funcCall = initializer.getChildren();

                                    for (
                                            IASTNode temp2:
                                            funcCall
                                    )
                                    {
                                        if (temp2 instanceof CPPASTFunctionCallExpression)
                                        {
                                            functionCallFound = true;
                                            break;
                                        }
                                    }
                                }

                                if (functionCallFound == true)
                                {
                                    break;
                                }
                            }
                        }

                    }
                    //((CPPASTEqualsInitializer)((CPPASTDeclarator)((org.eclipse.cdt.core.dom.ast.IASTDeclarator[])(
                    // (CPPASTSimpleDeclaration).declaration).declarators)[0]).initializer).fArgument

                    if (child instanceof ReturnNode)
                    {
                        if (((ReturnNode) child).getAst() instanceof CPPASTReturnStatement)
                        {
                            if (((CPPASTReturnStatement) ((ReturnNode) child).getAst()).getReturnValue() instanceof CPPASTFunctionCallExpression)
                            {
                                functionCallFound = true;
                            }

                        }
                    }

                    if (functionCallFound == true)
                    {
                        break;
                    }
                }

                if (child instanceof ConditionCfgNode)
                {
                    if (child instanceof ConditionIfCfgNode)
                    {
                        if (((ConditionIfCfgNode) child).getAst() instanceof CPPASTFunctionCallExpression)
                        {
                            functionCallFound = true;
                        }
                    }

                    IASTNode[] node1 = ((ConditionCfgNode) child).getAst().getChildren();
                    for (
                            IASTNode temp:
                            node1
                    )
                    {
                        if (temp instanceof CPPASTFunctionCallExpression)
                        {
                            functionCallFound = true;
                            break;
                        }
                    }

                    if (functionCallFound)
                    {
                        break;
                    }

                }

            }

            for (
                    ICfgNode child :
                    cfg.getAllNodes()
            )
            {
                if (child instanceof ConditionCfgNode)
                {
                    ifLoopSwitchFound = true;
                    break;

                }

            }


            if (!functionCallFound && ifLoopSwitchFound)
            {
                ret.add(node);
            }
        }

        return ret;
    }
}
