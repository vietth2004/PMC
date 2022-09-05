package parser.projectparser.externalvariable;

import org.eclipse.cdt.core.dom.ast.*;
import parser.projectparser.ProjectParser;
import tree.object.IFunctionNode;
import tree.object.INode;
import tree.object.IVariableNode;
import utils.Utils;
import utils.search.FunctionNodeCondition;
import utils.search.GlobalVariableNodeCondition;
import utils.search.Search;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Find all external variables of a function
 * <p>
 * Remain: not detected variable through setter and getter yet
 */
public class RelatedExternalVariableDetecter extends ASTVisitor implements IVariableDetecter {

    /**
     * Represent function
     */
    private IFunctionNode function;

    private final List<IASTName> variableNames = new ArrayList<>();

    private final List<IASTSimpleDeclaration> declarations = new ArrayList<>();

    public static void main(String[] args) {
        ProjectParser parser = new ProjectParser(new File("datatest/duc-anh/Algorithm"));

        IFunctionNode function = (IFunctionNode) Search
                .searchNodes(parser.getRootTree(), new FunctionNodeCondition(), "Tritype(int,int,int)").get(0);


        RelatedExternalVariableDetecter detecter = new RelatedExternalVariableDetecter(function);
        detecter.findVariables();
    }


    public RelatedExternalVariableDetecter(IFunctionNode function) {
        this.function = function;
        this.shouldVisitExpressions = true;
        this.shouldVisitDeclarations = true;
        function.getAST().accept(this);
    }

    @Override
    public List<IVariableNode> findVariables() {
        for (IASTSimpleDeclaration declaration : declarations) {
            for (IASTDeclarator declarator : declaration.getDeclarators()) {
                String name = declarator.getName().getRawSignature();
                variableNames
                        .removeIf(varName ->
                                varName.getRawSignature().equals(name));
            }
        }

        List<IVariableNode> globalVars = getAllGlobalVariables();
        if (globalVars.isEmpty()) {
        } else {
        }

        List<IVariableNode> relatedVars = globalVars.stream()
                .filter(this::isUsedInFunction)
                .collect(Collectors.toList());

        if (relatedVars.isEmpty()) {
        } else {
        }

        return relatedVars;
    }

    private boolean isUsedInFunction(IVariableNode v) {
        final String varName = v.getName();
        return variableNames.stream()
                .anyMatch(name -> name.getRawSignature().equals(varName));
    }

    private List<IVariableNode> getAllGlobalVariables() {
        INode unit = Utils.getSourcecodeFile(function);
        return Search.searchNodes(unit, new GlobalVariableNodeCondition());
    }

    @Override
    public int visit(IASTExpression expression) {
        if (expression instanceof IASTIdExpression) {
            variableNames.add(((IASTIdExpression) expression).getName());
        }

        return PROCESS_CONTINUE;
    }

    @Override
    public int visit(IASTDeclaration declaration) {
        if (declaration instanceof IASTSimpleDeclaration)
            declarations.add((IASTSimpleDeclaration) declaration);
        return PROCESS_CONTINUE;
    }

    @Override
    public IFunctionNode getFunction() {
        return function;
    }

    @Override
    public void setFunction(IFunctionNode function) {
        this.function = function;
    }

}
