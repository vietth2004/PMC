package GUI;

import Common.DSEConstants;
import Common.TestConfig;
import cfg.CFG;
import cfg.CFGGenerationforSubConditionCoverage;
import cfg.ICFG;
import cfg.object.AbstractConditionLoopCfgNode;
import cfg.object.ConditionCfgNode;
import cfg.object.ICfgNode;
import config.AbstractSetting;
import config.FunctionConfig;
import config.ISettingv2;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import normalizer.FunctionNormalizer;
import pairwise.*;
import parser.projectparser.ProjectParser;
import tree.object.*;
import utils.Utils;
import utils.search.FunctionNodeCondition;
import utils.search.Search;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

public class PairwiseUI extends Component
{

    private Stage parentStage;
    public ComboBox cboSelectedFunction;
    public Button btnGetFunctionList;
    public Button btnBVTG;
    public Button btnSTCFG;
    public Button btnWCFT;
    /**
     * Represent control flow graph
     */
    public IProjectNode projectNode;

    @FXML
    public Button btnBrowseInput;
//    @FXML
//    public TextField txtMaxLoop;
    @FXML
    public Button btnGenerateTestData;
    @FXML
    public TextField txtSourceFolder;

    @FXML
    public CheckBox chkSolvePathWhenGenBoundaryTestData;
    public Button btnRunTest;

    ICFG cfg;
    int maxIterationsforEachLoop;
    //List<TestData> testCases;
    List<IVariableNode> variables;

    IFunctionNode function;

    @FXML
    protected void btnGetFunctionList_Clicked(ActionEvent event) throws Exception
    {
        ProjectParser parser = new ProjectParser(new File(txtSourceFolder.getText()));

        projectNode = parser.getRootTree();

        List<INode> functionList = Search.getAllNodes(projectNode, new FunctionNodeCondition());

        if (functionList.size() > 0)
        {
            for (INode function : functionList)
            {
                cboSelectedFunction.getItems().add(new FunctionComboItem(function.getName(), function.getName()));
            }

            cboSelectedFunction.getSelectionModel().select(0);
        }
    }

    @FXML
    protected void btnViewReport_Clicked(ActionEvent event) throws Exception
    {
        System.out.println("btnViewReport_Clicked started");
        Path currentRelativePath = Paths.get("");
        String path = currentRelativePath.toAbsolutePath().toString() + "\\TEST_REPORT.html";

        File htmlFile = new File(path);
        Desktop.getDesktop().browse(htmlFile.toURI());
    }

    @FXML
    protected void btnBrowseInput_Clicked(ActionEvent event) throws Exception
    {
        System.out.println("btnBrowseInput_Clicked started");
        JFileChooser _fileChooser = new JFileChooser();
        _fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

        Path currentRelativePath = Paths.get("");
        String path = currentRelativePath.toAbsolutePath().toString() + "\\data-test\\pairwise";

        _fileChooser.setSelectedFile(new File(path));
        if (_fileChooser.showDialog(this, "Choose folder") == JFileChooser.APPROVE_OPTION)
        {
            String selectedPath = _fileChooser.getSelectedFile().getAbsolutePath();

            txtSourceFolder.setText(selectedPath);
        }
    }

    @FXML
    protected void btnCombination_Clicked(ActionEvent event) throws Exception
    {
        try
        {
            TestConfig.SetProjectPath(txtSourceFolder.getText());

            String value = "";

            if (cboSelectedFunction.getValue() == null)
            {
                JOptionPane.showMessageDialog(null, "Please click on [Get function list] button, then choose a function to generate test data", DSEConstants.PRODUCT_NAME, JOptionPane.ERROR_MESSAGE);
                return;
            }
            value = cboSelectedFunction.getValue().toString();

            System.gc();
            System.runFinalization();

            PairwiseApp.getStage().setTitle("Start generating combination test data...");

            LocalDateTime startDateTime = LocalDateTime.now();

            Runtime rt = Runtime.getRuntime();
            long prevTotal = 0;
            long prevFree = rt.freeMemory();

            List<Param> paramList = GetParamAndValueList(value);

            PairWiser pairWiser = new PairWiser();
            pairWiser.setParams(paramList);
            pairWiser.generateCombinationTestData();

            LocalDateTime afterGenForC = LocalDateTime.now();

            long total = rt.totalMemory();
            long free = rt.freeMemory();
            if (total != prevTotal || free != prevFree) {
                System.out.println(
                        String.format("Total: %s, Free: %s, Diff: %s",
                                total,
                                free,
                                prevFree - free));
                usedMem = (prevFree - free)/MEGABYTE_FACTOR;
            }

            Duration duration = Duration.between(startDateTime, afterGenForC);

            long nano = duration.toNanos();

            long mili = duration.toMillis();

            System.out.println("duration.toNanos() = " + nano);

            System.out.println("duration.toMillis() = " + mili);

            testDataGenerationTime =  ((float)mili)/((float)1000);

            System.out.println("testDataGenerationTime = " + testDataGenerationTime);

            System.out.println("test case set: " + pairWiser.getTestSetT().size());

            ExportCombinationReport(pairWiser.getTestSetT(), value);

            PairwiseApp.getStage().setTitle(TestConfig.TOOL_TITLE);

            JOptionPane.showMessageDialog(null, "Finish generating data. Click on [View report] " +
                    "for the result.", DSEConstants.PRODUCT_NAME, JOptionPane.INFORMATION_MESSAGE);
        }
        catch (Exception e1)
        {
            e1.printStackTrace();
        }
    }

    private float boundStep = 1.0f;
    private float usedMem = 0;
    private final float MEGABYTE_FACTOR = 1024L * 1024L;

    @FXML
    protected void btnPairwise_Clicked(ActionEvent event) throws Exception
    {
        try
        {

            TestConfig.SetProjectPath(txtSourceFolder.getText());

            String value = "";

            if (cboSelectedFunction.getValue() == null)
            {
                JOptionPane.showMessageDialog(null, "Please click on [Get function list] button, then choose a function to generate test data", DSEConstants.PRODUCT_NAME, JOptionPane.ERROR_MESSAGE);
                return;
            }
            value = cboSelectedFunction.getValue().toString();

            System.gc();
            System.runFinalization();

            PairwiseApp.getStage().setTitle("Start generating pairwise test data...");

            LocalDateTime startDateTime = LocalDateTime.now();

            Runtime rt = Runtime.getRuntime();
            long prevTotal = 0;
            long prevFree = rt.freeMemory();

            List<Param> paramList = GetParamAndValueList(value);

            PairWiser pairWiser = new PairWiser();
            pairWiser.setParams(paramList);
            pairWiser.generatePairwiseTestData();

            LocalDateTime afterGenForC = LocalDateTime.now();


            long total = rt.totalMemory();
            long free = rt.freeMemory();
            if (total != prevTotal || free != prevFree) {
                System.out.println(
                        String.format("Total: %s, Free: %s, Diff: %s",
                                total,
                                free,
                                prevFree - free));
                usedMem = (prevFree - free)/MEGABYTE_FACTOR;
            }



            Duration duration = Duration.between(startDateTime, afterGenForC);

            long nano = duration.toNanos();

            long mili = duration.toMillis();

            System.out.println("duration.toNanos() = " + nano);

            System.out.println("duration.toMillis() = " + mili);

            testDataGenerationTime =  ((float)mili)/((float)1000);

            System.out.println("testDataGenerationTime = " + testDataGenerationTime);

            System.out.println("test case set: " + pairWiser.getTestSetT().size());

            ExportPairwiseReport(pairWiser.getTestSetT(), value);

            PairwiseApp.getStage().setTitle(TestConfig.TOOL_TITLE);

            JOptionPane.showMessageDialog(null, "Finish generating data. Click on [View report] " +
                    "for the result.", DSEConstants.PRODUCT_NAME, JOptionPane.INFORMATION_MESSAGE);
        }
        catch (Exception e1)
        {
            e1.printStackTrace();
        }
    }
    float testDataGenerationTime = 0.0f;

    public List<Param> GetParamAndValueList(String functionName) throws Exception
    {
        ProjectParser parser = new ProjectParser(new File(txtSourceFolder.getText()));

        projectNode = parser.getRootTree();

        function = (IFunctionNode) Search.searchNodes(projectNode, new FunctionNodeCondition(),
                functionName).get(0);

        List<IVariableNode> list = function.getArguments();

        //Sinh dữ liệu test theo biên, cần phải sinh CFG theo sub condition thì mới có được các điều kiện
        FunctionConfig functionConfig = new FunctionConfig();
        functionConfig.setSolvingStrategy(ISettingv2.SUPPORT_SOLVING_STRATEGIES[0]);
        ((IFunctionNode) function).setFunctionConfig(functionConfig);
        FunctionNormalizer fnNorm = ((IFunctionNode) function).normalizedAST();
        String normalizedCoverage = fnNorm.getNormalizedSourcecode();
        ((IFunctionNode) function).setAST(fnNorm.getNormalizedAST());
        IFunctionNode clone = (IFunctionNode) function.clone();
        clone.setAST(Utils.getFunctionsinAST(normalizedCoverage.toCharArray()).get(0));
        CFGGenerationforSubConditionCoverage cfgGen = new CFGGenerationforSubConditionCoverage(clone);

        cfg = (CFG) cfgGen.generateCFG();
        cfg.setFunctionNode(clone);

        this.cfg.resetVisitedStateOfNodes();
        this.cfg.setIdforAllNodes();
        //this.setTestCases(new ArrayList<TestData>());
        this.maxIterationsforEachLoop = 1;
        this.variables = function.getArguments();

        //beforeTestDataGenerationTime = LocalDateTime.now();

        List<ICfgNode> list1 = cfg.getAllNodes();
        List<ICfgNode> listConditionNode = new ArrayList<>();

        for (ICfgNode node : list1)
        {
            if (node instanceof ConditionCfgNode && !(node instanceof AbstractConditionLoopCfgNode))
            {
                listConditionNode.add(node);
            }
        }

        List<Param> paramList = new ArrayList<>();
        Map<String, TypeValue> paramValueList = new HashMap<>();

        for (IVariableNode variableNode : variables)
        {
            String VariableName = variableNode.getName();
            TypeValue typeValue = new TypeValue();

            String max = "", min = "";
            switch (variableNode.getRealType())
            {
                case "int":
                    max = Integer.toString(Integer.MAX_VALUE);
                    min = Integer.toString(Integer.MIN_VALUE);
                    typeValue.setTypeName("int");
                    break;
                case "long":
                    max = Long.toString(Long.MAX_VALUE);
                    min = Long.toString(Long.MIN_VALUE);
                    typeValue.setTypeName("long");
                    break;
                case "float":
                    max = Float.toString(Float.MAX_VALUE);
                    min = Float.toString(Float.MIN_VALUE);
                    typeValue.setTypeName("float");
                    break;
                case "double":
                    max = Double.toString(Double.MAX_VALUE);
                    min = Double.toString(Double.MIN_VALUE);
                    typeValue.setTypeName("double");
                    break;
                case "short":
                    max = Short.toString(Short.MAX_VALUE);
                    min = Short.toString(Short.MIN_VALUE);
                    typeValue.setTypeName("short");
                    break;
            }

            typeValue.getValues().add(max);
            typeValue.getValues().add(min);

            for (
                    ICfgNode node : listConditionNode
            )
            {
                ConditionCfgNode stm1 = (ConditionCfgNode) node.clone();

                String Content = stm1.getContent();
                Content = Content.replaceAll("<=|>=|<|>|!=|==", "=");

                String[] varOrVal = Content.split("=");


                if (varOrVal.length != 2)
                {
                    continue;
                }

                varOrVal[0] = varOrVal[0].trim();
                varOrVal[1] = varOrVal[1].trim();

                String paramName = "";
                String value1 = "";

                if (Utils.contains(variables, varOrVal[0].trim()))
                {
                    paramName = varOrVal[0].trim();
                    value1 = varOrVal[1].trim();
                }
                else if (Utils.contains(variables, varOrVal[1].trim()))
                {
                    paramName = varOrVal[1].trim();
                    value1 = varOrVal[0].trim();
                }

                if (paramName.equals(VariableName))
                {
                    try
                    {
                        switch (variableNode.getRealType())
                        {
                            case "int":
                                int valInt = 0;
                                valInt = Integer.parseInt(value1);

                                if (!(typeValue.getValues().contains(Integer.toString(valInt))))
                                {
                                    typeValue.getValues().add(Integer.toString(valInt));
                                }
                                break;
                            case "long":
                                long valLong = 0;
                                valLong = Long.parseLong(value1);

                                if (!(typeValue.getValues().contains(Long.toString(valLong))))
                                {
                                    typeValue.getValues().add(Long.toString(valLong));
                                }
                                break;
                            case "float":
                                float valFloat = 0;
                                valFloat = Float.parseFloat(value1);

                                if (!(typeValue.getValues().contains(Float.toString(valFloat))))
                                {
                                    typeValue.getValues().add(Float.toString(valFloat));
                                }
                                break;
                            case "double":
                                double val = 0;
                                val = Double.parseDouble(value1);

                                if (!(typeValue.getValues().contains(Double.toString(val))))
                                {
                                    typeValue.getValues().add(Double.toString(val));
                                }
                                break;
                            case "short":
                                short valShort = 0;
                                valShort = Short.parseShort(value1);

                                if (!(typeValue.getValues().contains(Short.toString(valShort))))
                                {
                                    typeValue.getValues().add(Short.toString(valShort));
                                }
                                break;
                        }
                    }
                    catch (Exception ex)
                    {
                        continue;
                    }

                }
            }
            Utils.sortValueList(typeValue.getValues());

            List<Value> valueList = new ArrayList<>();

            for (int i = 0; i < typeValue.getValues().size() - 1; i++)
            {
                Value value1 = new Value();

                switch (typeValue.getTypeName())
                {
                    case "int":
                        int valInt = 0;
                        valInt = ThreadLocalRandom.current().nextInt(Integer.parseInt(typeValue.getValues().get(i)), Integer.parseInt(typeValue.getValues().get(i + 1)));
                        value1.setVal(valInt);
                        break;
                    case "long":
                        long valLong = 0;
                        valLong = (int) ((Long.parseLong(typeValue.getValues().get(i)) +
                                Long.parseLong(typeValue.getValues().get(i + 1))) / 2);
                        value1.setVal(valLong);
                        break;
                    case "float":
                        float valFloat = 0;
                        valFloat = (int) ((Float.parseFloat(typeValue.getValues().get(i)) +
                                Float.parseFloat(typeValue.getValues().get(i + 1))) / 2);
                        value1.setVal(valFloat);
                        break;
                    case "double":
                        double val = 0;
                        val = (int) ((Double.parseDouble(typeValue.getValues().get(i)) +
                                Double.parseDouble(typeValue.getValues().get(i + 1))) / 2);
                        value1.setVal(val);
                        break;
                    case "short":
                        short valShort = 0;
                        valShort = (short) ((Short.parseShort(typeValue.getValues().get(i)) +
                                Short.parseShort(typeValue.getValues().get(i + 1))) / 2);
                        value1.setVal(valShort);
                        break;
                }

                valueList.add(value1);
            }

            Param newParam = new Param(VariableName, valueList);

            if (!Utils.contains(paramList, newParam))
            {
                paramList.add(newParam);
            }
            else
            {
                Param param3 = Utils.getParam(paramList, newParam);
                param3.getValues().addAll(valueList);
            }
        }

        return paramList;
    }

    public void ExportPairwiseReport(List<Testcase> testCaseList, String testGenMethodName) throws IOException
    {
        FileWriter csvWriter = new FileWriter(AbstractSetting.getValue("TEST_REPORT") + ".html", false);
        String valueString = "<!DOCTYPE html>\r\n" +
                "<html>\r\n" +
                "\r\n" +
                "<head> <link rel=\"stylesheet\" type=\"text/css\" href=\"hmm_report.css\">\r\n" +
                "\r\n" +
                "</head>\r\n" +
                "\r\n" +
                "<body>\r\n";

        valueString +=
                "    <h2>PAIRWISE TEST REPORT</h2>" +
                "    <h2> Function name: " + function.getName() +  "</h2><br />";

        valueString += "    <div class=\"table-wrapper\">\r\n" +
                "        <table class=\"fl-table\">\r\n" +
                "            <thead>\r\n" +
                "                <tr>\r\n" +
                "                    <th>No.</th>\r\n" +
//                "                    <th style=\"width: 800px\">Test path</th>\r\n" +
                "                    <th>Test Data</th>\r\n" +
                "                </tr>\r\n" +
                "            </thead>\r\n" +
                "            <tbody>";
        int i = 1;
        for (Testcase testCase : testCaseList)
        {
                valueString += "<tr><td>" + i + "</td><td>" + testCase.toString() + "</td></tr>";
                i += 1;
        }
        valueString += "</tbody></table></div>";

        String loopString = "";

        valueString += "    <div class=\"table-wrapper\">\r\n" +
                "        <table class=\"fl-table\">\r\n" +
                "            <thead>\r\n" +
                "                <tr>\r\n" +
                "                    <th>Coverage information</th>\r\n" +
                "                </tr>\r\n" +
                "            </thead>\r\n" +
                "            <tbody>";


        String coverInfo = "";
        try
        {
            coverInfo =
                            "        <tr><td>Time: " + String.format("%.2f",testDataGenerationTime)  + " ms</td></tr>\r\n";
            coverInfo +=
                    "        <tr><td>Memory: " + String.format("%.2f",usedMem) + " MB</td></tr>\r\n";
        }
        catch (Exception e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        valueString += coverInfo;
        valueString += "   </tbody>\r\n" +
                "        </table></div>\r\n";


        valueString += "    <div class=\"table-wrapper\">\r\n" +
                "        <table class=\"fl-table\">\r\n" +
                "            <thead>\r\n" +
                "                <tr>\r\n" +
                "                    <th>Function raw signature</th>\r\n" +
                "                </tr>\r\n" +
                "            </thead>\r\n" +
                "            <tbody>" +
                "<tr><td><pre>" + this.function.getAST().getRawSignature().toString() +

                "</pre></td></tr></tbody></table></div>" +

                "</body></html>";
        csvWriter.append(valueString);
        csvWriter.close();
    }

    public void ExportCombinationReport(List<Testcase> testCaseList, String testGenMethodName) throws IOException
    {
        FileWriter csvWriter = new FileWriter(AbstractSetting.getValue("TEST_REPORT") + ".html", false);
        String valueString = "<!DOCTYPE html>\r\n" +
                "<html>\r\n" +
                "\r\n" +
                "<head> <link rel=\"stylesheet\" type=\"text/css\" href=\"hmm_report.css\">\r\n" +
                "\r\n" +
                "</head>\r\n" +
                "\r\n" +
                "<body>\r\n";

        valueString +=
                "    <h2>COMBINATION TEST REPORT</h2>" +
                        "    <h2> Function name: " + function.getName() +  "</h2><br />";

        valueString += "    <div class=\"table-wrapper\">\r\n" +
                "        <table class=\"fl-table\">\r\n" +
                "            <thead>\r\n" +
                "                <tr>\r\n" +
                "                    <th>No.</th>\r\n" +
//                "                    <th style=\"width: 800px\">Test path</th>\r\n" +
                "                    <th>Test Data</th>\r\n" +
                "                </tr>\r\n" +
                "            </thead>\r\n" +
                "            <tbody>";
        int i = 1;
        for (Testcase testCase : testCaseList)
        {
            valueString += "<tr><td>" + i + "</td><td>" + testCase.toString() + "</td></tr>";
            i += 1;
        }
        valueString += "</tbody></table></div>";

        String loopString = "";

        valueString += "    <div class=\"table-wrapper\">\r\n" +
                "        <table class=\"fl-table\">\r\n" +
                "            <thead>\r\n" +
                "                <tr>\r\n" +
                "                    <th>Coverage information</th>\r\n" +
                "                </tr>\r\n" +
                "            </thead>\r\n" +
                "            <tbody>";


        String coverInfo = "";
        try
        {
            coverInfo =
                    "        <tr><td>Time: " + String.format("%.2f",testDataGenerationTime)  + " ms</td></tr>\r\n";
            coverInfo +=
                    "        <tr><td>Memory: " + String.format("%.2f",usedMem) + " MB</td></tr>\r\n";
        }
        catch (Exception e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        valueString += coverInfo;
        valueString += "   </tbody>\r\n" +
                "        </table></div>\r\n";


        valueString += "    <div class=\"table-wrapper\">\r\n" +
                "        <table class=\"fl-table\">\r\n" +
                "            <thead>\r\n" +
                "                <tr>\r\n" +
                "                    <th>Function raw signature</th>\r\n" +
                "                </tr>\r\n" +
                "            </thead>\r\n" +
                "            <tbody>" +
                "<tr><td><pre>" + this.function.getAST().getRawSignature().toString() +

                "</pre></td></tr></tbody></table></div>" +

                "</body></html>";
        csvWriter.append(valueString);
        csvWriter.close();
    }

    public void initialize()
    {
        // initialization code here...
        Path currentRelativePath = Paths.get("");
        String path = currentRelativePath.toAbsolutePath().toString() + "\\data-test\\pairwise";

        txtSourceFolder.setText(path);
    }

    public Stage getParentStage()
    {
        return parentStage;
    }

    public void setParentStage(Stage parentStage)
    {
        this.parentStage = parentStage;
    }
}
