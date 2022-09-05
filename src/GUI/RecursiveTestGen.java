package GUI;

import Common.DSEConstants;
import Common.TestConfig;
import config.AbstractSetting;
import config.ISettingv2;
import config.Settingv2;
import console.Console;
import coverage.FunctionCoverageComputation;
import javafx.application.Platform;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import parser.projectparser.ProjectParser;
import tree.object.IFunctionNode;
import tree.object.INode;
import tree.object.IProjectNode;
import utils.Utils;
import utils.search.FunctionNodeCondition;
import utils.search.Search;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import config.Paths.*;

public class RecursiveTestGen  extends Component
{
    private Stage parentStage;
    public IProjectNode projectNode;
    public IProjectNode projectNodev2;

    @FXML
    public Button btnBrowse;
    @FXML
    public TextField txtSourceFolder;
    @FXML
    public TextField txtSourceFolderv2;
    @FXML
    public TextField txtMaxLoop;
    @FXML
    public ComboBox cbFunctionList;
    @FXML
    public ComboBox cbCoverageType;
    @FXML
    public Button btnGetFunctionList;
    @FXML
    public Button btnClose;
    @FXML
    public Button btnGenerateTestData;
    @FXML
    public Button btnViewReport;
    @FXML
    public ProgressIndicator progressIndicator;

    public void initialize()
    {
        // initialization code here...
        Path currentRelativePath = Paths.get("");
        String path = currentRelativePath.toAbsolutePath().toString() + "\\data-test\\redis-6.0.15\\src";
        txtSourceFolder.setText(path);

        String pathv2 = currentRelativePath.toAbsolutePath().toString() + "\\data-test\\redis-6.2.5\\src";

        //F:\VietData\GitLab\bai10_new\data-test\libuv-v1.7.0\src

        txtSourceFolderv2.setText(pathv2);

        txtMaxLoop.setText("1");

        cbCoverageType.getItems().add(new FunctionComboItem(DSEConstants.COVERAGE_STATEMENT_BRANCH, DSEConstants.COVERAGE_STATEMENT_BRANCH));
        cbCoverageType.getItems().add(new FunctionComboItem(DSEConstants.COVERAGE_MCDC, DSEConstants.COVERAGE_MCDC));

        cbCoverageType.getSelectionModel().select(0);

        CURRENT_PROJECT.EXE_PATH = TestConfig.EXE_PATH;
        CURRENT_PROJECT.CLONE_PROJECT_PATH = TestConfig.INSTRUMENTED_CODE;
        CURRENT_PROJECT.CURRENT_TESTDRIVER_EXECUTION_PATH = TestConfig.TEST_DRIVER_PATH;
        CURRENT_PROJECT.CURRENT_TESTDRIVER_EXECUTION_NAME = "testdriver.cpp";
        CURRENT_PROJECT.MAKEFILE_PATH = TestConfig.MAKEFILE_PATH;
        CURRENT_PROJECT.TYPE_OF_PROJECT = ISettingv2.PROJECT_CUSTOMMAKEFILE;
    }

    @FXML
    protected void btnBrowseInput_Clicked(ActionEvent event) throws Exception
    {
        System.out.println("btnBrowseInput_Clicked started");
        JFileChooser _fileChooser = new JFileChooser();
        _fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

        Path currentRelativePath = Paths.get("");
        String path = currentRelativePath.toAbsolutePath().toString() + "\\data-test";

        _fileChooser.setSelectedFile(new File(path));
        if (_fileChooser.showDialog(this, "Choose folder") == JFileChooser.APPROVE_OPTION)
        {
            String selectedPath = _fileChooser.getSelectedFile().getAbsolutePath();

            txtSourceFolder.setText(selectedPath);
        }
    }

    @FXML
    protected void btnBrowseInputv2_Clicked(ActionEvent event) throws Exception
    {
        System.out.println("btnBrowseInput_Clicked started");
        JFileChooser _fileChooser = new JFileChooser();
        _fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

        Path currentRelativePath = Paths.get("");
        String path = currentRelativePath.toAbsolutePath().toString() + "\\data-test";

        _fileChooser.setSelectedFile(new File(path));
        if (_fileChooser.showDialog(this, "Choose folder") == JFileChooser.APPROVE_OPTION)
        {
            String selectedPath = _fileChooser.getSelectedFile().getAbsolutePath();

            txtSourceFolderv2.setText(selectedPath);
        }
    }

    @FXML
    protected void btnGetFunctionList_Clicked(ActionEvent event) throws Exception
    {
        cbFunctionList.getItems().clear();

        getFunctionListThread getFuncListThread = new getFunctionListThread(txtSourceFolder.getText(),
                txtSourceFolderv2.getText());

        progressIndicator.progressProperty().unbind();

        progressIndicator.setProgress(0);

        // Kết nối thuộc tính progress.
        progressIndicator.progressProperty().bind(getFuncListThread.progressProperty());

        getFuncListThread.addEventHandler(WorkerStateEvent.WORKER_STATE_SUCCEEDED,
                (EventHandler<WorkerStateEvent>) t ->
                {
                    if (getFuncListThread.getCommonFunctionList().size() > 0)
                    {
                        for (upgradedFunctions function : getFuncListThread.getCommonFunctionList())
                        {
                            cbFunctionList.getItems().add(new upgradedFunctionsComboItem(function.getFunctionv1().getName(),
                                    function));
                        }

                        cbFunctionList.getSelectionModel().select(0);
                    }
                });

        Thread newThread = new Thread(getFuncListThread);

        newThread.start();

        //newThread.join();


    }

    @FXML
    protected void btnGenerateTestData_Clicked(ActionEvent event) throws Exception
    {

        try
        {

            int maxloop = 1;
            try
            {
                maxloop = Integer.parseInt(txtMaxLoop.getText());
            }
            catch (Exception ex)
            {
                JOptionPane.showMessageDialog(null, "Maxloop is invalid",
                        DSEConstants.PRODUCT_NAME, JOptionPane.ERROR_MESSAGE);
            }
//            String value = "";
//
//            if (cbFunctionList.getValue() == null)
//            {
//                JOptionPane.showMessageDialog(null, "Please click on [Get function list] button, then choose a function to generate test data", DSEConstants.PRODUCT_NAME, JOptionPane.ERROR_MESSAGE);
//                return;
//            }
//            String functionv1AbsPath =
//                    ((upgradedFunctionsComboItem)cbFunctionList.getValue()).getValue().getFunctionv1().getAbsolutePath();
//
//            String functionv1Name =
//                    ((upgradedFunctionsComboItem)cbFunctionList.getValue()).getValue().getFunctionv1().getName();
//
//            String functionv1Signature =
//                    ((IFunctionNode)((upgradedFunctionsComboItem)cbFunctionList.getValue()).getValue().getFunctionv1()).getAST().getRawSignature();
//
//            String functionv2AbsPath =
//                    ((upgradedFunctionsComboItem)cbFunctionList.getValue()).getValue().getFunctionv2().getAbsolutePath();
//
//            String functionv2Name =
//                    ((upgradedFunctionsComboItem)cbFunctionList.getValue()).getValue().getFunctionv2().getName();
//            String functionv2Signature =
//                    ((IFunctionNode)((upgradedFunctionsComboItem)cbFunctionList.getValue()).getValue().getFunctionv2()).getAST().getRawSignature();


//            System.out.println("functionv1AbsPath = " + functionv1AbsPath);
//            System.out.println("functionv1Name = " + functionv1Name);
//            System.out.println("functionv1Signature = " + functionv1Signature);
//
//            System.out.println("functionv2AbsPath = " + functionv2AbsPath);
//            System.out.println("functionv2Name = " + functionv2Name);
//            System.out.println("functionv2Signature = " + functionv2Signature);

            Recursive.getStage().setTitle("Start generating test data...");

//            value =
//                    ((upgradedFunctionsComboItem)cbFunctionList.getValue()).getValue().getFunctionv2().getName();

//            RecursiveTestGenAlgorithm bGen = new RecursiveTestGenAlgorithm(maxloop, value, txtSourceFolderv2.getText(),
//                    cbCoverageType.getValue().toString());


            RecursiveTestGenAlgorithm bGen = new RecursiveTestGenAlgorithm(maxloop,
                    "ACLCheckPasswordHash(unsigned char*,int)",
                    "F:\\VietData\\GitLab\\bai10_new\\data-test\\redis-6.2.5\\src",
                    cbCoverageType.getValue().toString());

            //ACLCheckPasswordHash(unsigned char*,int)

            bGen.recursiveTestGen();

            Recursive.getStage().setTitle("Running test cases and calculating coverage...");

//            FunctionCoverageComputation functionCoverageComputation = Utils.ExecuteTestCase(txtSourceFolder.getText(), value, bGen.getTestCases());
//
//            bGen.setFunctionCoverageComputation(functionCoverageComputation);
//
//            Recursive.getStage().setTitle("Exporting test report...");
//
//            bGen.ExportReport();

            Recursive.getStage().setTitle(TestConfig.TOOL_TITLE);

            JOptionPane.showMessageDialog(null, "Finish generating data. Click on [View report] " +
                    "for the result.", DSEConstants.PRODUCT_NAME, JOptionPane.INFORMATION_MESSAGE);
        }
        catch (Exception e1)
        {
            e1.printStackTrace();
        }
    }


    @FXML
    protected void btnSDART_Clicked(ActionEvent event) throws Exception
    {
        try
        {
            //TestConfig.PROJECT_PATH = txtSourceFolder.getText();

            TestConfig.SetProjectPath(txtSourceFolder.getText());

            CURRENT_PROJECT.ORIGINAL_PROJECT_PATH = TestConfig.PROJECT_PATH;

            TestConfig.PATH_SELECTION_STRATEGY = "SDART";

            int maxloop = 1;
            try
            {
                maxloop = Integer.parseInt(txtMaxLoop.getText());
            }
            catch (Exception ex)
            {
                JOptionPane.showMessageDialog(null, "Maxloop is invalid",
                        DSEConstants.PRODUCT_NAME, JOptionPane.ERROR_MESSAGE);
            }
            String value = "";

            if (cbFunctionList.getValue() == null)
            {
                JOptionPane.showMessageDialog(null, "Please click on [Get function list] button, then choose a function to generate test data", DSEConstants.PRODUCT_NAME, JOptionPane.ERROR_MESSAGE);
                return;
            }
            value = cbFunctionList.getValue().toString();

            Console console = new Console(value, TestConfig.PROJECT_PATH);

            console.exportToHtml(new File(AbstractSetting.getValue(Settingv2.TEST_REPORT) + ".html"), value);

            JOptionPane.showMessageDialog(null, "Finish generating data using SDART. Click on [View report] " +
                    "for the result.", DSEConstants.PRODUCT_NAME, JOptionPane.INFORMATION_MESSAGE);
        }
        catch (Exception e1)
        {
            // TODO Auto-generated catch block
            e1.printStackTrace();
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
    protected void btnClose_Clicked(ActionEvent event) throws Exception
    {
        Platform.exit();
        System.exit(0);
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
