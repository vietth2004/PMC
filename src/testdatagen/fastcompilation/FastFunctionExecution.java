package testdatagen.fastcompilation;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import Common.TestConfig;
import compiler.Terminal;
import org.apache.log4j.Logger;

import config.AbstractSetting;
import config.ISettingv2;
import config.Paths;
import config.Settingv2;
import exception.GUINotifyException;
import parser.projectparser.ProjectParser;
import testcase_manager.TestCase;
import testdatagen.Backup;
import testdatagen.FunctionExecution;
import testdatagen.testdataexec.ConsoleExecution;
import testdatagen.testdataexec.ITestdriverGeneration;
import testdatagen.testdataexec.TestdriverGenerationforC;
import tree.object.CFileNode;
import tree.object.CppFileNode;
import tree.object.FunctionNode;
import tree.object.IFunctionNode;
import utils.CompilerUtils;
import utils.Utils;
import utils.search.FunctionNodeCondition;
import utils.search.Search;

/**
 * Enhance the old function execution by compiling the testing project once
 * time.
 *
 * @author DucAnh
 */
public class FastFunctionExecution extends FunctionExecution
{
    final static Logger logger = Logger.getLogger(FastFunctionExecution.class);

    public static void main(String[] args) throws Exception
    {
        Settingv2.create();
        AbstractSetting.setValue(ISettingv2.SOLVER_Z3_PATH, "C:/z3/bin/z3.exe");
        AbstractSetting.setValue(ISettingv2.GNU_MAKE_PATH, "C:/Dev-Cpp/MinGW64/bin/mingw32-make.exe");
        AbstractSetting.setValue(ISettingv2.GNU_GCC_PATH, "C:/Dev-Cpp/MinGW64/bin/gcc.exe");
        AbstractSetting.setValue(ISettingv2.GNU_GPlusPlus_PATH, "C:/Dev-Cpp/MinGW64/bin/g++.exe");

        ProjectParser parser = new ProjectParser(new File(Paths.TSDV_R1));
        FunctionNode testedFunction = (FunctionNode) Search
                .searchNodes(parser.getRootTree(), new FunctionNodeCondition(), "IntTest(int)").get(0);

        String preparedInput = "";
        new FastFunctionExecution(testedFunction, preparedInput);
    }

    public FastFunctionExecution(IFunctionNode fn, String staticSolution) throws Exception
    {
        clonedProject = fn.getAbsolutePath().substring(0, fn.getAbsolutePath().indexOf("\\", 40));

        String executableFile = TestConfig.EXE_PATH + "\\" + "testCase" + TestConfig.EXE_EXTENTION;

//        if (isInitializedCompilerEnvironment())
//        {
            Backup backup = saveCurrentState(fn);
            try
            {
                Paths.CURRENT_PROJECT.CURRENT_TESTDRIVER_EXECUTION_PATH = generateExecutionFile(fn);
                Paths.CURRENT_PROJECT.TESTDATA_INPUT_FILE_PATH = (new File(
                        Utils.getSourcecodeFile(fn).getAbsolutePath()).getParent() + File.separator
                        + Paths.CURRENT_PROJECT.TESTDATA_INPUT_FILE_NAME).replace("\\", "/");

                // Normalize function before executing it
                // logger.debug("Normalize function before executing it");
                IFunctionNode clone = (IFunctionNode) fn.clone();
                clone.setAST(clone.getGeneralNormalizationFunction().getNormalizedAST());
                changedTokens = clone.getGeneralNormalizationFunction().getTokens();

                ITestdriverGeneration testdriverGen = generateTestdriver(clone, staticSolution, backup);
                if (testdriverGen != null)
                {
                    if (Utils.getSourcecodeFile(fn) instanceof CFileNode)
                    {
                        // Up to now, I have not had any idea for generating better test driver than the
                        // older version.
                        if (new File(Paths.CURRENT_PROJECT.EXE_PATH).exists())
                        {
                            killExeProcess(Paths.CURRENT_PROJECT.EXE_PATH);
                        }
                        logger.debug(Paths.CURRENT_PROJECT.EXE_PATH + " does not exist");
                        ConsoleExecution.compileMakefile(new File(Paths.CURRENT_PROJECT.MAKEFILE_PATH));

                        executeExecutableFile(Utils.findRootProject(fn),
                                Paths.CURRENT_PROJECT.CURRENT_TESTDRIVER_EXECUTION_PATH);

                    }
                    else if (Utils.getSourcecodeFile(fn) instanceof CppFileNode)
                    {
                        if (new File(executableFile).exists())
                        {
                            killExeProcess(executableFile);
                        }

                        String compileAndLinkMessage = compileAndLink(backup);

                        logger.debug("compileAndLinkMessage: " + compileAndLinkMessage);

                        // Run the executable file
                        if (new File(executableFile).exists())
                        {
                            String message = runExecutableFile(executableFile);

                            logger.debug("execution message: " + message);

                            String executionFilePath =
                                    TestConfig.TESTPATH_FILE + File.separator + TestConfig.TESTCASE_NAME + TestConfig.TESTPATH_EXTENTION;

                            encodedTestpath.setEncodedTestpath(normalizeTestpathFromFile(Utils.readFileContent(executionFilePath)));
                        }
                    }
                }
            }
            catch (GUINotifyException e)
            {
                e.printStackTrace();
                throw new GUINotifyException(e.getMessage());

            }
            catch (Exception e)
            {
                e.printStackTrace();
                initialization = "";

            }
            finally
            {
                backup.restore();

                // Delete exe program if it exists before
                if (Utils.getSourcecodeFile(fn) instanceof CFileNode
                        && new File(executableFile).exists())
                {
                    ConsoleExecution.killProcess(new File(executableFile).getName());
                }

                if (encodedTestpath.getEncodedTestpath().length() == 0)
                {
                    initialization = "";
                }
            }
//        }
    }

    public String compileAndLink(Backup backup) throws IOException,
            InterruptedException
    {
        StringBuilder output = new StringBuilder();

        String testCaseName = "testCase";
        String sourceFile = backup.getFnParent().getAbsolutePath();//.getSourceCodeFile();
        String sourceFileName = (new File(sourceFile)).getName();
        String instrumentedFile = TestConfig.INSTRUMENTED_CODE + "\\" +
                sourceFileName.substring(0, sourceFileName.lastIndexOf(".")) +
                TestConfig.UET_IGNORE_FILE + TestConfig.CPP_EXTENTION;

        String outFile = TestConfig.COMPILE_OUTPUT + "\\" + testCaseName + ".out";

//		String testDriverFile = TestConfig.TEST_DRIVER_PATH + "\\" +
//				testCaseName + TestConfig.CPP_EXTENTION;

        String testDriverFile =
                TestConfig.TEST_DRIVER_PATH + File.separator + TestConfig.SDART_TESTCASE_NAME + TestConfig.CPP_EXTENTION;// backup.getFnParent().getAbsolutePath();

        String exeFile = TestConfig.EXE_PATH + "\\" + testCaseName + TestConfig.EXE_EXTENTION;

        String compilationCommand = String.format(TestConfig.COMPILE_COMMAND_TEMPLATE, testDriverFile, outFile);

        String linkCommand = String.format(TestConfig.LINK_COMMAND_TEMPLATE, outFile, exeFile);

        String[] script = CompilerUtils.prepareForTerminal(Utils.getCompiler(), compilationCommand);

        String response = new Terminal(script, TestConfig.COMPILE_OUTPUT).get();

        output.append(response).append("\n");

        String[] linkScript = CompilerUtils
                .prepareForTerminal(Utils.getCompiler(), linkCommand);
        String linkResponse = new Terminal(linkScript, TestConfig.LINK_OUTPUT).get();
        output.append(linkResponse);

        return output.toString().trim();
    }

    public String runExecutableFile(String executableFile) throws IOException, InterruptedException
    {

        String directory = TestConfig.PROJECT_PATH;

        Terminal terminal;

        terminal = new Terminal(executableFile, directory);

        Process p = terminal.getProcess();
        p.waitFor(10, TimeUnit.SECONDS); // give it a chance to stop

        if (p.isAlive())
        {
            p.destroy(); // tell the process to stop
            p.waitFor(10, TimeUnit.SECONDS); // give it a chance to stop
            p.destroyForcibly(); // tell the OS to kill the process
            p.waitFor();
        }

        //testCase.setExecutedTime(terminal.getTime());

        return terminal.get();
    }

    private ITestdriverGeneration generateTestdriver(IFunctionNode clone, String staticSolution, Backup backup)
            throws Exception
    {
        // Generate test driver
        logger.info("Generate a test driver");
        dataGen = clone.generateDataTree(FunctionExecution.staticSolutionsGen(staticSolution));
        dataGen.setFunctionNode(clone);
        dataGen.generateTree();

        String functionCall = dataGen.getFunctionCall();

        ITestdriverGeneration testdriverGen = null;
        if (Utils.getSourcecodeFile(clone) instanceof CFileNode)
        {
            initialization = dataGen.getInputforGoogleTest();
            testdriverGen = new TestdriverGenerationforC();

        }
        else if (Utils.getSourcecodeFile(clone) instanceof CppFileNode)
        {
            // Improvement here
            initialization = dataGen.getInputformFile();
            logger.debug(new File(Paths.CURRENT_PROJECT.TESTDATA_INPUT_FILE_PATH).getName() + "="
                    + dataGen.getInputSavedInFile().replace("\n", "; "));
            //Utils.writeContentToFile(dataGen.getInputSavedInFile(), Paths.CURRENT_PROJECT.TESTDATA_INPUT_FILE_PATH);

            Utils.writeContentToFile(dataGen.getInputSavedInFile(), Paths.CURRENT_PROJECT.TESTDATA_INPUT_FILE_PATH);

            testdriverGen = new FastTestdriverGenerationforCpp();
        }
        else
        {
            throw new Exception("Dont support this type of file source code");
        }

        logger.debug("driver=" + initialization.replace("\n", "").replace("\t", "") + functionCall.replace("\n", "")
                + "...");

        if (testdriverGen != null)
        {
            testdriverGen.setTestedFunction(clone);
            testdriverGen.setInitialization(initialization);
            testdriverGen.setFunctionCall(functionCall);
            testdriverGen.generate();

            //Utils.writeContentToFile(testdriverGen.getCompleteSourceFile(), backup.getFnParent());
            Utils.writeContentToFile(testdriverGen.getCompleteSourceFile(),
                    TestConfig.TEST_DRIVER_PATH + File.separator + TestConfig.SDART_TESTCASE_NAME + TestConfig.CPP_EXTENTION);
        }
        return testdriverGen;
    }

    /**
     * Create for storing execution test path
     *
     * @param fn
     * @return
     * @throws IOException
     */
    private String generateExecutionFile(IFunctionNode fn) throws IOException
    {
        String executionFilePath = "";
        Paths.CURRENT_PROJECT.CURRENT_TESTDRIVER_EXECUTION_NAME = TestConfig.TESTCASE_NAME // FunctionExecution.id
                + Paths.CURRENT_PROJECT.TESTDRIVER_EXECUTION_NAME_POSTFIX;

        switch (Paths.CURRENT_PROJECT.TYPE_OF_PROJECT)
        {

            case ISettingv2.PROJECT_ECLIPSE:
            {
                executionFilePath = new File(Utils.getSourcecodeFile(fn).getAbsolutePath()).getParent() + File.separator
                        + Paths.CURRENT_PROJECT.CURRENT_TESTDRIVER_EXECUTION_NAME;
                break;
            }
            case ISettingv2.PROJECT_DEV_CPP:
            case ISettingv2.PROJECT_CODEBLOCK:
            case ISettingv2.PROJECT_VISUALSTUDIO:
                executionFilePath = new File(Utils.getSourcecodeFile(fn).getAbsolutePath()).getParent() + File.separator
                        + Paths.CURRENT_PROJECT.CURRENT_TESTDRIVER_EXECUTION_NAME;
                break;
            case ISettingv2.PROJECT_CUSTOMMAKEFILE:
                executionFilePath = TestConfig.EXE_PATH + File.separator
                        + Paths.CURRENT_PROJECT.CURRENT_TESTDRIVER_EXECUTION_NAME;
                break;
        }
        //executionFilePath = executionFilePath.replace("\\", "/");
        //Utils.writeContentToFile("", executionFilePath);
        return executionFilePath;
    }
}
