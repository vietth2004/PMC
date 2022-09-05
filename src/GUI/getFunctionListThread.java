package GUI;

import javafx.concurrent.Task;
import javafx.scene.control.ProgressIndicator;
import parser.projectparser.ProjectParser;
import tree.object.IFunctionNode;
import tree.object.INode;
import tree.object.IProjectNode;
import utils.search.FunctionNodeCondition;
import utils.search.Search;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class getFunctionListThread extends Task
{
    private IProjectNode projectNodev1;
    private IProjectNode projectNodev2;
    private String sourceFolderv1;
    private String sourceFolderv2;

    private List<upgradedFunctions> commonFunctionList = new ArrayList<>();

    public getFunctionListThread(String version1, String version2)
    {
        sourceFolderv1 = version1;
        sourceFolderv2 = version2;
    }

    public void run1()
    {
        this.updateProgress(5, 100);
        ProjectParser parser = new ProjectParser(new File(sourceFolderv1));

        projectNodev1 = parser.getRootTree();
        this.updateProgress(25, 100);

        ProjectParser parser2 = new ProjectParser(new File(sourceFolderv2));

        projectNodev2 = parser2.getRootTree();

        this.updateProgress(50, 100);

        List<INode> functionListv1 = null;
        try
        {
            functionListv1 = Search.getAllUnitNodesWithBranches(projectNodev1, new FunctionNodeCondition());
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        this.updateProgress(75, 100);
        List<INode> functionListv2 = null;
        try
        {
            functionListv2 = Search.getAllUnitNodesWithBranches(projectNodev2, new FunctionNodeCondition());
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        //getProgressIndicator().setProgress(45);
        this.updateProgress(80, 100);

        for (
                INode funcv1 :
                functionListv1
        )
        {
            String funcv1Name = funcv1.getName();

            for (
                    INode funcv2 :
                    functionListv2
            )
            {
                String funcv2Name = funcv2.getName();
                if (funcv1Name.equals(funcv2Name))
                {
                    String signaturev1 = ((IFunctionNode)funcv1).getAST().getRawSignature();
                    String signaturev2 = ((IFunctionNode)funcv2).getAST().getRawSignature();

                    if (!signaturev1.equals(signaturev2))
                    {
                        getCommonFunctionList().add(new upgradedFunctions(funcv1, funcv2));
                    }
                    break;
                }
            }
        }
        this.updateProgress(100, 100);

    }

    public List<upgradedFunctions> getCommonFunctionList()
    {
        return commonFunctionList;
    }

    public void setCommonFunctionList(List<upgradedFunctions> commonFunctionList)
    {
        this.commonFunctionList = commonFunctionList;
    }

    //Nếu có hàm run thì hàm call này ko được gọi và cũng ko bắn sự kiện
    //        getFuncListThread.addEventHandler(WorkerStateEvent.WORKER_STATE_SUCCEEDED,
    //                (EventHandler<WorkerStateEvent>) t ->
    @Override
    protected Object call() throws Exception
    {
        run1();
        return "";
    }
}
