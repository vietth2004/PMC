package cia_new;

import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTParameterDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTPointerOperator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTParameterDeclaration;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTFunctionDeclarator;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTFunctionDefinition;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTParameterDeclaration;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class graphNode
{
    private String id = "";
    private IASTFunctionDefinition astFunctionDefinition = null;
    private List<graphNode> children = new ArrayList<>();
    private String rawSignature= "";
    private String functionSignature = "";
    private String returnType = "";
    private String returnPointer = "";

    private File parentFile;

    private String functionName = "";
    private List<IASTParameterDeclaration> parameterDeclarationList;

    public graphNode()
    {

    }

    public String getId()
    {
        return id;
    }

    public void setId(String id)
    {
        this.id = id;
    }

    public IASTFunctionDefinition getAstFunctionDefinition()
    {
        return astFunctionDefinition;
    }

    public void setAstFunctionDefinition(IASTFunctionDefinition astFunctionDefinition)
    {
        this.astFunctionDefinition = astFunctionDefinition;

        this.id = astFunctionDefinition.toString();

        this.functionName = astFunctionDefinition.getDeclarator().getName().getRawSignature();
        this.returnType = astFunctionDefinition.getDeclSpecifier().getRawSignature();
        ICPPASTParameterDeclaration[] paramList =
                ((CPPASTFunctionDeclarator)astFunctionDefinition.getDeclarator()).getParameters();

        List<functionParameter> parameterList = new ArrayList<>();
        for (
                ICPPASTParameterDeclaration param: paramList
        )
        {
            functionParameter newParam = new functionParameter(param.getDeclarator().getRawSignature(),
                    param.getDeclSpecifier().getRawSignature());

            parameterList.add(newParam);
        }

        IASTPointerOperator[] pointerOperators =
                ((CPPASTFunctionDeclarator)astFunctionDefinition.getDeclarator()).getPointerOperators();


        this.rawSignature = astFunctionDefinition.getRawSignature();
    }

    public List<graphNode> getChildren()
    {
        return children;
    }

    public void setChildren(List<graphNode> children)
    {
        this.children = children;
    }

    public String getRawSignature()
    {
        return rawSignature;
    }

    public void setRawSignature(String rawSignature)
    {
        this.rawSignature = rawSignature;
    }

    public String getFunctionSignature()
    {
        return functionSignature;
    }

    public void setFunctionSignature(String functionSignature)
    {
        this.functionSignature = functionSignature;
    }

    public File getParentFile()
    {
        return parentFile;
    }

    public void setParentFile(File parentFile)
    {
        this.parentFile = parentFile;
    }

    public String getReturnType()
    {
        return returnType;
    }

    public void setReturnType(String returnType)
    {
        this.returnType = returnType;
    }

    public String getFunctionName()
    {
        return functionName;
    }

    public void setFunctionName(String functionName)
    {
        this.functionName = functionName;
    }

    public String getReturnPointer()
    {
        return returnPointer;
    }

    public void setReturnPointer(String returnPointer)
    {
        this.returnPointer = returnPointer;
    }
}


