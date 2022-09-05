package GUI;

import Common.DSEConstants;
import HybridAutoTestGen.FullBoundedTestGen;
import HybridAutoTestGen.TestData;
import HybridAutoTestGen.TestPath;
import cfg.CFG;
import cfg.CFGGenerationforBranchvsStatementCoverage;
import cfg.CFGGenerationforSubConditionCoverage;
import cfg.ICFG;
import cfg.object.*;
import cfg.testpath.*;
import config.*;
import constraints.checker.RelatedConstraintsChecker;
import coverage.FunctionCoverageComputation;
import coverage.StaticFunctionCoverageComputation;
import normalizer.FunctionNormalizer;
import org.apache.log4j.Logger;
import parser.projectparser.ProjectParser;
import testdatagen.fastcompilation.randomgeneration.BasicTypeRandom;
import testdatagen.se.ISymbolicExecution;
import testdatagen.se.Parameter;
import testdatagen.se.PathConstraint;
import testdatagen.se.SymbolicExecution;
import testdatagen.se.solver.ISmtLibGeneration;
import testdatagen.se.solver.RunZ3OnCMD;
import testdatagen.se.solver.SmtLibGeneration;
import testdatagen.se.solver.Z3SolutionParser;
import testdatagen.testdatainit.VariableTypes;
import tree.object.*;
import utils.SpecialCharacter;
import utils.Utils;
import utils.search.FunctionNodeCondition;
import utils.search.Search;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

/*
Tiêu chí cho việc sinh dữ liệu kiểm thử cho hàm đệ quy
1. Phủ hết các câu lệnh return (theo nhánh hoặc theo sub-condition)
2. Phủ hết các câu lệnh gọi đệ quy
 */


public class RecursiveTestGenAlgorithm
{

    public static final String CONSTRAINTS_FILE = Settingv2.getValue(ISettingv2.SMT_LIB_FILE_PATH);
    public static final String Z3 = Settingv2.getValue(ISettingv2.SOLVER_Z3_PATH);
    final static Logger logger = Logger.getLogger(FullBoundedTestGen.class);
    /**
     * Represent control flow graph
     */
    private ICFG cfg;
    private IFunctionNode function;
    private int maxIterationsforEachLoop;
    private FullTestpaths possibleTestpaths = new FullTestpaths();
    private List<IVariableNode> variables;
    private String sourceFolder;
    private StaticFunctionCoverageComputation staticFunctionCoverageComputation;
    private List<TestData> testCases;
    public IProjectNode projectNode;
    private float boundStep = 1;
    private int maxloop = 0;
    private String functionName;
    LocalDateTime beforeTestDataGenerationTime;
    LocalDateTime afterTestDataGenerationTime;
    private String _coverageType = DSEConstants.COVERAGE_STATEMENT_BRANCH;

    public RecursiveTestGenAlgorithm()
    {
        // TODO Auto-generated constructor stub
        this.cfg = cfg;
    }

    public RecursiveTestGenAlgorithm(int _maxloop, String _functionName, String _sourceFolder, String coverageType) throws Exception
    {
        maxloop = _maxloop;
        sourceFolder = _sourceFolder;
        functionName = _functionName;
        _coverageType = coverageType;
    }


    public static void main(String[] args) throws Exception
    {
        RecursiveTestGenAlgorithm tpGen = new RecursiveTestGenAlgorithm(1, "PDF(int,int,int)", Paths.TSDV_R1_2,
                DSEConstants.COVERAGE_STATEMENT_BRANCH);

        tpGen.recursiveTestGen();

    }

    public void recursiveTestGen() throws Exception
    {
        //region Generate CFG
        ProjectParser parser = new ProjectParser(new File(sourceFolder));

        projectNode = parser.getRootTree();

        List<INode> nodeList = Search.searchNodes(projectNode, new FunctionNodeCondition(), functionName);

        if (nodeList.size() <= 0)
        {
            logger.debug("Cannot find function with name: " + functionName);
            return;
        }

        function = (IFunctionNode) Search.searchNodes(projectNode, new FunctionNodeCondition(), functionName).get(0);

        //Sinh dữ liệu test theo biên, cần phải sinh CFG theo sub condition thì mới có được các điều kiện
        FunctionConfig functionConfig = new FunctionConfig();
        functionConfig.setSolvingStrategy(ISettingv2.SUPPORT_SOLVING_STRATEGIES[0]);
        functionConfig.setCharacterBound(new ParameterBound(30, 120));
        functionConfig.setIntegerBound(new ParameterBound(-100, 200));
        functionConfig.setSizeOfArray(5);
        functionConfig.setMaximumInterationsForEachLoop(3);
        functionConfig.setSolvingStrategy(ISettingv2.SUPPORT_SOLVING_STRATEGIES[0]);
        //functionConfig.setIntegerBound(new IntegerBou);


        ((IFunctionNode) function).setFunctionConfig(functionConfig);

        FunctionNormalizer fnNorm = ((IFunctionNode) function).normalizedAST();
        String normalizedCoverage = fnNorm.getNormalizedSourcecode();
        ((IFunctionNode) function).setAST(fnNorm.getNormalizedAST());
        IFunctionNode clone = (IFunctionNode) function.clone();
        clone.setAST(Utils.getFunctionsinAST(normalizedCoverage.toCharArray()).get(0));
        System.out.println(clone.getAST().getRawSignature());

        if (_coverageType.equals(DSEConstants.COVERAGE_MCDC))
        {
            CFGGenerationforSubConditionCoverage cfgGen = new CFGGenerationforSubConditionCoverage(function);

            cfg = (CFG) cfgGen.generateCFG();
        }
        else if (_coverageType.equals(DSEConstants.COVERAGE_STATEMENT_BRANCH))
        {
            CFGGenerationforBranchvsStatementCoverage cfgGen = new CFGGenerationforBranchvsStatementCoverage(function);

            cfg = (CFG) cfgGen.generateCFG();

        }
        cfg.setFunctionNode(function);
        this.cfg.resetVisitedStateOfNodes();
        this.cfg.setIdforAllNodes();

        staticFunctionCoverageComputation = new StaticFunctionCoverageComputation(cfg);

        //endregion

        this.setTestCases(new ArrayList<TestData>());
        this.maxIterationsforEachLoop = maxloop;
        this.variables = function.getArguments();

        beforeTestDataGenerationTime = LocalDateTime.now();

        generateTestpaths(function);

        calculateCoverage();

        List<ICfgNode> list = cfg.getAllNodes();
        List<ICfgNode> listConditionNode = new ArrayList<>();
//        for (ICfgNode node : list)
//        {
//            if (node instanceof ConditionCfgNode && !(node instanceof AbstractConditionLoopCfgNode))
//            {
//                listConditionNode.add(node);
//            }
//        }
//
//        List<TestData> recursiveTestDataList = Utils.generateTestpathsForBoundaryTestGen(listConditionNode,
//                function.getPassingVariables(), 1);
//
//        getTestCases().addAll(recursiveTestDataList);

        afterTestDataGenerationTime = LocalDateTime.now();

    }

    private void calculateCoverage()
    {
        for (int i= 0; i < possibleTestpaths.size(); i++)
        {
            FullTestpath testPath = (FullTestpath)possibleTestpaths.get(i);

            System.out.println("Test data [" + i + "] = " + testPath.getTestCase());

            if (testPath.getTestCase() != null && testPath.getTestCase() != "")
            {
                for (int nodeIdx = 0; nodeIdx < testPath.size(); nodeIdx++)
                {
                    ICfgNode node = (ICfgNode) testPath.get(nodeIdx);
                    node.setVisit(true);
                }
            }
        }

        int numberOfStatements = cfg.computeNumofStatements();
        int numberOfBranches = cfg.computeNumOfBranches();
        int numberOfVisitedBranches = cfg.computeNumofVisitedBranches();
        int numberOfVisitedStatements = cfg.computeNumofVisitedStatements();

        System.out.println("numberOfStatements = " + numberOfStatements);
        System.out.println("numberOfBranches = " + numberOfBranches);
        System.out.println("numberOfVisitedBranches = " + numberOfVisitedBranches);
        System.out.println("numberOfVisitedStatements = " + numberOfVisitedStatements);
    }

    //region Generate test paths

    public void generateTestpaths(IFunctionNode function)
    {
        // Date startTime = Calendar.getInstance().getTime();
        FullTestpaths testpaths_ = new FullTestpaths();

        ICfgNode beginNode = cfg.getBeginNode();
        FullTestpath initialTestpath = new FullTestpath();
        initialTestpath.setFunctionNode(cfg.getFunctionNode());
        try
        {
            traverseCFG(beginNode, initialTestpath, testpaths_, function);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        for (ITestpathInCFG tp : testpaths_)
        {
            tp.setFunctionNode(cfg.getFunctionNode());
        }

        possibleTestpaths = testpaths_;
    }

    private void traverseCFG(ICfgNode stm, FullTestpath tp, FullTestpaths testpaths, IFunctionNode function) throws Exception
    {
        tp.add(stm);
        FullTestpath tp1 = (FullTestpath) tp.clone();
        FullTestpath tp2 = (FullTestpath) tp.clone();
        if (stm instanceof EndFlagCfgNode)
        {
            FullTestpath tpclone = (FullTestpath) tp.clone();

//            LocalDateTime beforeTime = LocalDateTime.now();

            String solution = this.solveTestpath(function, tp);

            if (!solution.equals((IStaticSolutionGeneration.NO_SOLUTION)))
            {
                if (solution.equals(IStaticSolutionGeneration.EVERY_SOLUTION))
                {
                    // Just pick a random test data
                    solution = initializeTestdataAtRandom();
                }

//                LocalDateTime afterTime = LocalDateTime.now();
//
//                Duration duration = Duration.between(beforeTime, afterTime);

//                float diff = Math.abs((float) duration.toMillis());
//
//                logger.debug(tp.getFullPath());
//
//                logger.debug("Solve full path time = " + diff);
//
//                logger.debug("Solution = " + solution);

                tpclone.setTestCase(solution);
                testpaths.add(tpclone);

                TestData testData = new TestData();

                testCases.add(TestData.parseString(tp.getTestCase()));
            }

            tp.remove(tp.size() - 1);
        }
        else
        {
            ICfgNode trueNode = stm.getTrueNode();
            ICfgNode falseNode = stm.getFalseNode();

            if (stm instanceof ConditionCfgNode)
            {
                if (stm instanceof AbstractConditionLoopCfgNode)
                {
                    int currentIterations = tp.count(trueNode);
                    if (currentIterations < maxIterationsforEachLoop)
                    {
                        tp1.add(falseNode);
                        traverseCFG(falseNode, tp, testpaths, function);
                        tp2.add(trueNode);
                        traverseCFG(trueNode, tp, testpaths, function);

                    }
                    else
                    {
                        tp1.add(falseNode);

                        traverseCFG(falseNode, tp, testpaths, function);
                    }
                }
                else
                {
                    tp1.add(falseNode);

                    traverseCFG(falseNode, tp, testpaths, function);

                    tp2.add(trueNode);

                    traverseCFG(trueNode, tp, testpaths, function);
                }
            }
            else
            {
                traverseCFG(trueNode, tp, testpaths, function);
            }

            tp.remove(tp.size() - 1);
        }
    }

    protected boolean haveSolution(FullTestpath tp, boolean finalConditionType) throws Exception
    {
        IPartialTestpath tp1 = createPartialTestpath(tp, finalConditionType);

        String solution = solveTestpath(cfg.getFunctionNode(), tp1);

        if (!solution.equals(IStaticSolutionGeneration.NO_SOLUTION))
        {
            return true;
        }
        else
        {
            return false;
        }
    }

    protected IPartialTestpath createPartialTestpath(FullTestpath fullTp, boolean finalConditionType)
    {
        IPartialTestpath partialTp = new PartialTestpath();
        for (ICfgNode node : fullTp.getAllCfgNodes())
        {
            partialTp.getAllCfgNodes().add(node);
        }

        partialTp.setFinalConditionType(finalConditionType);
        return partialTp;
    }

    protected String solveTestpath(IFunctionNode function, ITestpathInCFG testpath) throws Exception
    {
        /*
         * Get the passing variables of the given function
         */
        Parameter paramaters = new Parameter();
        for (IVariableNode n : function.getArguments())
        {
            paramaters.add(n);
        }
        for (IVariableNode n : function.getReducedExternalVariables())
        {
            paramaters.add(n);
        }

        /*
         * Get the corresponding path constraints of the test path
         */
        ISymbolicExecution se = new SymbolicExecution(testpath, paramaters, function);

        // fast checking
        RelatedConstraintsChecker relatedConstraintsChecker = new RelatedConstraintsChecker(
                se.getConstraints().getNormalConstraints(), function);
        boolean isRelated = relatedConstraintsChecker.check();
        //
        if (isRelated)
        {
            if (se.getConstraints().getNormalConstraints().size()
                    + se.getConstraints().getNullorNotNullConstraints().size() > 0)
            {
                /*
                 * Solve the path constraints
                 */
                ISmtLibGeneration smtLibGen = new SmtLibGeneration(function.getPassingVariables(),
                        se.getConstraints().getNormalConstraints());
                smtLibGen.generate();

                Utils.writeContentToFile(smtLibGen.getSmtLibContent(), CONSTRAINTS_FILE);

                RunZ3OnCMD z3 = new RunZ3OnCMD(Z3, CONSTRAINTS_FILE);
                z3.execute();
                logger.debug("solving done");
                String solution = z3.getSolution();
                String staticSolution = new Z3SolutionParser().getSolution(solution);

                if (staticSolution.equals(IStaticSolutionGeneration.NO_SOLUTION))
                {
                    return IStaticSolutionGeneration.NO_SOLUTION;
                }
                else
                {
                    if (se.getConstraints().getNullorNotNullConstraints().size() > 0)
                    {
                        for (PathConstraint nullConstraint : se.getConstraints().getNullorNotNullConstraints())
                        {
                            staticSolution += nullConstraint + SpecialCharacter.END_OF_STATEMENT;
                        }
                    }

                    if (se.getConstraints().getNullorNotNullConstraints().size() > 0)
                    {
                        return staticSolution + "; " + se.getConstraints().getNullorNotNullConstraints();
                    }
                    else
                    {
                        return staticSolution + ";";
                    }
                }
            }
            else
            {
                return IStaticSolutionGeneration.NO_SOLUTION;
            }
        }
        else
        {
            //return IStaticSolutionGeneration.EVERY_SOLUTION;
            return initializeTestdataAtRandom();
        }
    }

    /**
     * Initialize data at random
     *
     * @return
     */
    protected String initializeTestdataAtRandom() {
        String testdata = ""; // Ex: a=1;b=2
        Map<String, String> initialization = constructRandomInput(function.getArguments(), function.getFunctionConfig(), "");
        for (String key : initialization.keySet())
            testdata += key + "=" + initialization.get(key) + ";";
        return testdata;
    }

    /**
     * Ex: Consider this function:
     *
     * <pre>
     *  int struct_test1(SinhVien sv){
     *		char* s = sv.other[0].eeee;
     *		if (sv.age > 0){
     *			if (s[0] == 'a')
     *				return 0;
     *			else
     *				return 1;
     *		}else{
     *			return 2;
     *		}
     *	}
     * </pre>
     *
     * The above function has only one argument and it has been configured. <br/>
     * Example of output: sv.age=306;sv.name=NULL;sv.other[0].eeee=NULL;
     *
     * @param arguments
     * @param functionConfig
     * @param prefixName
     * @return
     */
    protected Map<String, String> constructRandomInput(List<IVariableNode> arguments, IFunctionConfig functionConfig,
                                                       String prefixName) {
        Map<String, String> input = new TreeMap<>();
        for (IVariableNode argument : arguments) {
            String type = argument.getRawType();

            // Number
            if (VariableTypes.isBool(type)) {
                // 0 - false; 1 - true
                input.put(prefixName + argument.getName(), BasicTypeRandom.generateInt(0, 1) + "");
            } else if (VariableTypes.isNumBasic(type)) {
                if (VariableTypes.isNumBasicFloat(type)) {
                    input.put(prefixName + argument.getName(),
                            BasicTypeRandom.generateFloat(functionConfig.getIntegerBound().getLower(),
                                    functionConfig.getIntegerBound().getUpper()) + "");
                } else {
                    input.put(prefixName + argument.getName(),
                            BasicTypeRandom.generateInt(functionConfig.getIntegerBound().getLower(),
                                    functionConfig.getIntegerBound().getUpper()) + "");
                }

            } else if (VariableTypes.isNumOneDimension(type)) {
                for (int i = 0; i < functionConfig.getSizeOfArray(); i++)
                    if (VariableTypes.isNumOneDimensionFloat(type)) {
                        input.put(prefixName + argument.getName() + "[" + i + "]",
                                BasicTypeRandom.generateFloat(functionConfig.getIntegerBound().getLower(),
                                        functionConfig.getIntegerBound().getUpper()) + "");
                    } else {
                        input.put(prefixName + argument.getName() + "[" + i + "]",
                                BasicTypeRandom.generateInt(functionConfig.getIntegerBound().getLower(),
                                        functionConfig.getIntegerBound().getUpper()) + "");
                    }

            } else if (VariableTypes.isNumOneLevel(type)) {
                if (assignPointerToNull()) {
                    input.put(prefixName + argument.getName(), "NULL");
                } else {
                    for (int i = 0; i < functionConfig.getSizeOfArray(); i++)
                        input.put(prefixName + argument.getName() + "[" + i + "]",
                                BasicTypeRandom.generateInt(functionConfig.getIntegerBound().getLower(),
                                        functionConfig.getIntegerBound().getUpper()) + "");
                }
            }
            // Character
            else if (VariableTypes.isChBasic(type)) {
                input.put(prefixName + argument.getName(),
                        BasicTypeRandom.generateInt(functionConfig.getCharacterBound().getLower(),
                                functionConfig.getCharacterBound().getUpper()) + "");

            } else if (VariableTypes.isChOneDimension(type)) {
                for (int i = 0; i < functionConfig.getSizeOfArray(); i++)
                    input.put(prefixName + argument.getName() + "[" + i + "]",
                            BasicTypeRandom.generateInt(functionConfig.getCharacterBound().getLower(),
                                    functionConfig.getCharacterBound().getUpper()) + "" + "");

            } else if (VariableTypes.isChOneLevel(type)) {
                if (assignPointerToNull()) {
                    input.put(prefixName + argument.getName(), "NULL");
                } else {
                    for (int i = 0; i < functionConfig.getSizeOfArray(); i++)
                        input.put(prefixName + argument.getName() + "[" + i + "]",
                                BasicTypeRandom.generateInt(functionConfig.getCharacterBound().getLower(),
                                        functionConfig.getCharacterBound().getUpper()) + "" + "");
                }
            }
            // Structure
            else if (VariableTypes.isStructureSimple(type)) {
                INode correspondingNode = argument.resolveCoreType();
                if (correspondingNode != null && correspondingNode instanceof StructureNode) {
                    input.putAll(constructRandomInput(((StructureNode) correspondingNode).getAttributes(),
                            functionConfig, prefixName + argument.getName() + "."));
                }

            } else if (VariableTypes.isStructureOneDimension(type)) {
                INode correspondingNode = argument.resolveCoreType();

                if (correspondingNode != null && correspondingNode instanceof StructureNode)
                    for (int i = 0; i < functionConfig.getSizeOfArray(); i++) {
                        input.putAll(constructRandomInput(((StructureNode) correspondingNode).getAttributes(),
                                functionConfig, prefixName + argument.getName() + "[" + i + "]" + "."));
                    }

            } else if (VariableTypes.isStructureOneLevel(type)) {
                if (assignPointerToNull()) {
                    input.put(prefixName + argument.getName(), "NULL");
                } else {
                    INode correspondingNode = argument.resolveCoreType();

                    if (correspondingNode != null && correspondingNode instanceof StructureNode) {
                        List<IVariableNode> attributes = ((StructureNode) correspondingNode).getAttributes();

                        // Consider the linked list case (e.g., "class A{A*
                        // next}"), we assign value of
                        // "next" to NULL. Besides, we assume the size of the
                        // structure pointer is
                        // equivalent to 0.
                        for (int i = attributes.size() - 1; i >= 0; i--)
                            if (attributes.get(i).getReducedRawType().equals(argument.getReducedRawType())) {
                                input.put(prefixName + argument.getName() + "[0]." + attributes.get(i).getName(),
                                        "NULL");
                                attributes.remove(i);
                            }

                        //
                        input.putAll(constructRandomInput(attributes, functionConfig,
                                prefixName + argument.getName() + "[0]."));
                    }
                }
            }
        }
        return input;
    }

    protected boolean assignPointerToNull() {
        return new Random().nextInt(2/* default */) == 1;
    }
    //endregion Generate test paths

    public void ExportReport() throws IOException
    {
        Duration duration = Duration.between(beforeTestDataGenerationTime, afterTestDataGenerationTime);

        float diff = Math.abs((float) duration.toMillis() / 1000);

        FileWriter csvWriter = new FileWriter(AbstractSetting.getValue("TEST_REPORT") + ".html", false);

        String valueString = "<!DOCTYPE html>\r\n" +
                "<html>\r\n" +
                "\r\n" +
                "<head> <link rel=\"stylesheet\" type=\"text/css\" href=\"hmm_report.css\">\r\n" +
                "\r\n" +
                "</head>\r\n" +
                "\r\n" +
                "<body>\r\n" +
                "    <h2>BVTG: TEST REPORT</h2>\r\n" +

                "    <div class=\"table-wrapper\">\r\n" +
                "        <table class=\"fl-table\">\r\n" +
                "            <thead>\r\n" +
                "                <tr>\r\n" +
                "                    <th>Boundary generated test data</th>\r\n" +
                "                </tr>\r\n" +
                "            </thead>\r\n" +
                "            <tbody>";
        for (TestData testcase : getTestCases())
        {
            valueString += "<tr><td>" + testcase.toString() + "</td></tr>";
        }

        valueString += "        <tr><td>Number of test data: " + getTestCases().size() + "; Elapsed time: " + diff + " s</td></tr>";

        valueString += "            </tbody></table></div>";

        valueString += "   </tbody>\r\n" +
                "        </table></div>\r\n" +
                "<div  class=\"table-wrapper\">" +
                "        <table class=\"fl-table\">\r\n" +
                "            <thead>\r\n" +
                "                <tr>\r\n" +
                "                    <th>Function raw signature</th>\r\n" +
                "                </tr>\r\n" +
                "            </thead>\r\n" +
                "            <tbody>" +
                "<tr><td><pre>" + this.function.getAST().getRawSignature().toString() +

                "</pre></tr></td>" +
                "            </tbody></table></div>"+

                "</body></html>";
        csvWriter.append(valueString);
        csvWriter.close();

    }


    //region Get properties

    public ICFG getCfg()
    {
        return cfg;
    }

    public void setCfg(ICFG cfg)
    {
        this.cfg = cfg;
    }

    public int getMaxIterationsforEachLoop()
    {
        return maxIterationsforEachLoop;
    }

    public void setMaxIterationsforEachLoop(int maxIterationsforEachLoop)
    {
        this.maxIterationsforEachLoop = maxIterationsforEachLoop;
    }

    public FullTestpaths getPossibleTestpaths()
    {
        return possibleTestpaths;
    }

    public void setFunctionNode(IFunctionNode functionNode)
    {
        this.function = functionNode;
    }

    public IFunctionNode getFunctionNode()
    {
        return this.function;
    }

    public StaticFunctionCoverageComputation getFunctionCoverageComputation()
    {
        return staticFunctionCoverageComputation;
    }

    public void setFunctionCoverageComputation(StaticFunctionCoverageComputation staticFunctionCoverageComputation)
    {
        this.staticFunctionCoverageComputation = staticFunctionCoverageComputation;
    }

    public List<TestData> getTestCases()
    {
        return testCases;
    }

    public void setTestCases(List<TestData> testCases)
    {
        this.testCases = testCases;
    }

    //endregion Get properties
}
