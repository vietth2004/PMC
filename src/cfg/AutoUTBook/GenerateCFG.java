package cfg.AutoUTBook;

import cfg.ICFGGeneration;
import cfg.object.*;
import org.eclipse.cdt.core.dom.ast.*;
import org.eclipse.cdt.internal.core.dom.parser.ASTNode;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTFunctionCallExpression;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTSimpleDeclaration;
import tree.object.IFunctionNode;
import utils.ASTUtils;

import java.util.ArrayList;
import java.util.List;

public class GenerateCFG
{
    public CFGNode DuyetASTConditionStatement(IFunctionNode functionNode)
    {

        List<CPPASTFunctionCallExpression> functionCalls = new ArrayList<>();

        IASTFunctionDefinition stm = functionNode.getAST();

        ASTVisitor visitor = new ASTVisitor()
        {
            @Override
            public int visit(IASTStatement statement)
            {
                if (statement instanceof IASTIfStatement)
                {
                    IASTIfStatement stmIf = (IASTIfStatement) statement;
                    IASTExpression astCond = null;
                    astCond = stmIf.getConditionExpression();

                    IASTStatement astThen = stmIf.getThenClause();
                    IASTStatement astElse = stmIf.getElseClause();

                    ICfgNode afterTrue = new ForwardCfgNode();
                    ICfgNode afterFalse = new ForwardCfgNode();

                    ICfgNode condNode = null;
                    condNode = new ConditionIfCfgNode(astCond);

//                    begin.setBranch(condNode);
                    condNode.setTrue(afterTrue);
                    condNode.setFalse(afterFalse);

//                    visitCondition(astCond, begin, afterTrue, afterFalse, parent, ICFGGeneration.IF_FLAG);

//                visitStatement(astThen, afterTrue, end, _break, _continue, _throw, begin);

//                visitStatement(astElse, afterFalse, end, _break, _continue, _throw, begin);
                }
                return 0;
            };
        };
        visitor.shouldVisitStatements = true;
        visitor.shouldVisitExpressions = true;
		stm.accept(visitor);

		return new CFGNode();
    }


}
