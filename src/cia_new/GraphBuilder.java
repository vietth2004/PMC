package cia_new;

import org.eclipse.cdt.core.dom.ast.*;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTParameterDeclaration;
import org.eclipse.cdt.core.dom.ast.gnu.cpp.GPPLanguage;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.model.ILanguage;
import org.eclipse.cdt.core.parser.*;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTFunctionDeclarator;
import tree.object.INode;
import utils.Utils;
import utils.search.ExpressionCondition;
import utils.search.ISearchCondition;
import utils.search.Search;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GraphBuilder
{
    public GraphBuilder()
    {

    }

    private static IASTFunctionDefinition functionNode = null;

    public static void main(String[] args) throws Exception
    {
        String folderPath = "F:\\VietData\\TestData\\Test";
        File folder = new File(folderPath);

        GraphBuilder graphBuilder = new GraphBuilder();

        for (
                File file:folder.listFiles()
        )
        {
            graphBuilder.GetParameterList(file, "testBoundaryValue");

            //List<IASTExpression> list = graphBuilder.GetSimpleConditionList(functionNode);
            graphBuilder.build(file);

//            System.out.println("list.count = " + list.size());
        }


    }
    List<graphNode> graphNodeList = new ArrayList<>();

    public void build(File filePath) throws Exception
    {
        IASTTranslationUnit translationUnit;

        translationUnit = getIASTTranslationUnit(Utils.readFileContent(filePath.getAbsolutePath()).toCharArray());

        //String source = Utils.readFolderSourceCodeContent(folderPath);

//        System.out.println("source = " + source);

        //translationUnit = getIASTTranslationUnit(source.toCharArray());

        ASTVisitor visitor = new ASTVisitor() {

            @Override
            public int visit(IASTDeclaration declaration) {

                if (declaration instanceof IASTFunctionDefinition)
                {
                    //System.out.println("\n------\nIn function declaration: " + declaration.getRawSignature());

                    System.out.println("Function.getDeclarator(): " + ((IASTFunctionDefinition) declaration).getDeclarator().getRawSignature());
                    System.out.println("Function.getDeclSpecifier(): " + ((IASTFunctionDefinition) declaration).getDeclSpecifier().getRawSignature());

                    graphNode newNode = new graphNode();
                    newNode.setAstFunctionDefinition((IASTFunctionDefinition) declaration);
                    newNode.setFunctionSignature(((IASTFunctionDefinition) declaration).getDeclarator().getRawSignature());
                    newNode.setReturnType(((IASTFunctionDefinition) declaration).getDeclSpecifier().getRawSignature());

                    newNode.setParentFile(filePath);

                    if (declaration.getParent().getNodeLocations().length == 3)
                    {
                        //System.out.println("source = " + declaration.getParent().getNodeLocations()[1].toString());
                    }

                    System.out.println("declaration.getParent().getNodeLocations().length = " + declaration.getParent().getNodeLocations().length);

                    graphNodeList.add(newNode);
                }
                return ASTVisitor.PROCESS_CONTINUE;
            }

            @Override
            public int visit(IASTStatement statement)
            {
                System.out.println("statement = " + statement.getRawSignature());

                if (statement instanceof IASTFunctionCallExpression)
                {
                    System.out.println("Function call expression: " + statement.getRawSignature());
                }

                return super.visit(statement);
            }

//            @Override
//            public int visit(IASTExpression declaration) {
//                if (declaration instanceof IASTExpression)
//                    if (declaration.getRawSignature().startsWith(ThrowNormalizer.THROW_SIGNAL)) {
//
//                        return ASTVisitor.PROCESS_SKIP;
//                    }
//                return ASTVisitor.PROCESS_CONTINUE;
//            }


            @Override
            public int visit(IASTExpression expression)
            {
                System.out.println("expression = " + expression.getRawSignature());

                if (expression instanceof IASTFunctionCallExpression)
                {
                    System.out.println("Function call expression: " + expression.getRawSignature());
                }




                return super.visit(expression);
            }
        };

        visitor.shouldVisitDeclarations = true;
        visitor.shouldVisitStatements = true;
        visitor.shouldVisitExpressions = true;

        translationUnit.accept(visitor);

        System.out.println("graphNodeList.size = " + graphNodeList.size());
    }
    private IASTTranslationUnit getIASTTranslationUnit(char[] code) throws Exception {
        FileContent fc = FileContent.create("", code);
        Map<String, String> macroDefinitions = new HashMap<>();
        String[] includeSearchPaths = new String[0];
        IScannerInfo si = new ScannerInfo(macroDefinitions, includeSearchPaths);
        IncludeFileContentProvider ifcp = IncludeFileContentProvider.getEmptyFilesProvider();
        IIndex idx = null;
        int options = ILanguage.OPTION_IS_SOURCE_UNIT;
        IParserLogService log = new DefaultLogService();
        return GPPLanguage.getDefault().getASTTranslationUnit(fc, si, ifcp, idx, options, log);
    }

    public void GetParameterList(File filePath, String functionName) throws Exception
    {
        IASTTranslationUnit transUnit;

        transUnit = getIASTTranslationUnit(Utils.readFileContent(filePath.getAbsolutePath()).toCharArray());

        ASTVisitor visitor = new ASTVisitor() {

            @Override
            public int visit(IASTDeclaration declaration) {

                if (declaration instanceof IASTFunctionDefinition)
                {
                    String name =
                            ((IASTFunctionDefinition) declaration).getDeclarator().getName().toString();

                    if ( functionName.equals(name))
                    {

                        functionNode = (IASTFunctionDefinition)declaration;
                        ICPPASTParameterDeclaration[] paramDeclList =
                                ((CPPASTFunctionDeclarator)((IASTFunctionDefinition) declaration).getDeclarator()).getParameters();

                        List<functionParameter> paramList = new ArrayList<>();
                        for (
                                ICPPASTParameterDeclaration param: paramDeclList
                        )
                        {
                            functionParameter newParam = new functionParameter(param.getDeclarator().getRawSignature(),
                                    param.getDeclSpecifier().getRawSignature());

                            paramList.add(newParam);
                        }
                    }
                }
                return ASTVisitor.PROCESS_CONTINUE;
            }
        };
        visitor.shouldVisitDeclarations = true;
        transUnit.accept(visitor);
    }

    public List<IASTExpression> GetSimpleConditionList(IASTNode functionNode) throws Exception
    {
        List<IASTExpression> output = new ArrayList<>();
        try {
            for (IASTNode child : functionNode.getChildren()) {
                if (child instanceof IASTIfStatement)
                {
                    output.add((IASTExpression) ((IASTIfStatement)child).getConditionExpression());
                }
                if (child instanceof IASTForStatement)
                {
                    output.add((IASTConditionalExpression) ((IASTForStatement)child).getConditionExpression());
                }
                if (child instanceof IASTWhileStatement)
                {
                    output.add((IASTConditionalExpression) ((IASTWhileStatement)child).getCondition());
                }

                output.addAll(GetSimpleConditionList(child));
            }
            return output;
        } catch (Exception e) {
            return output;
        }
    }
}
