package cfg.testpath;

import cfg.CFGGenerationforSubConditionCoverage;
import cfg.ICFG;
import cfg.object.AbstractConditionLoopCfgNode;
import cfg.object.ConditionCfgNode;
import cfg.object.EndFlagCfgNode;
import cfg.object.ICfgNode;
import config.IFunctionConfig;
import config.ISettingv2;
import config.Paths;
import config.Settingv2;
import constraints.checker.RelatedConstraintsChecker;
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
import tree.object.IFunctionNode;
import tree.object.INode;
import tree.object.IVariableNode;
import tree.object.StructureNode;
import utils.SpecialCharacter;
import utils.Utils;
import utils.search.FunctionNodeCondition;
import utils.search.Search;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;

/**
 * Generate all possible test paths
 *
 * @author DucAnh
 */
public class PossibleTestpathGeneration implements ITestpathGeneration
{
    public static final String CONSTRAINTS_FILE = Settingv2.getValue(ISettingv2.SMT_LIB_FILE_PATH);
    public static final String Z3 = Settingv2.getValue(ISettingv2.SOLVER_Z3_PATH);
    final static Logger logger = Logger.getLogger(PossibleTestpathGeneration.class);
    /**
     * Represent control flow graph
     */
    private ICFG cfg;
    private int maxIterationsforEachLoop = ITestpathGeneration.DEFAULT_MAX_ITERATIONS_FOR_EACH_LOOP;
    private FullTestpaths possibleTestpaths = new FullTestpaths();

    public PossibleTestpathGeneration(ICFG cfg)
    {
        this.cfg = cfg;
    }

    public PossibleTestpathGeneration(ICFG cfg, int maxloop)
    {
        maxIterationsforEachLoop = maxloop;
        this.cfg = cfg;

        this.cfg.resetVisitedStateOfNodes();
        this.cfg.setIdforAllNodes();
    }

    /**
     * @param cfg
     * @param maxloop
     * @param isResetVisitedState true if the visit stated is marked unvisited
     */
    public PossibleTestpathGeneration(ICFG cfg, int maxloop, boolean isResetVisitedState)
    {
        maxIterationsforEachLoop = maxloop;
        this.cfg = cfg;

        if (isResetVisitedState)
        {
            this.cfg.resetVisitedStateOfNodes();
            this.cfg.setIdforAllNodes();
        }
    }

    public static void main(String[] args) throws Exception
    {
        ProjectParser parser = new ProjectParser(new File(Paths.JOURNAL_TEST));

        IFunctionNode function = (IFunctionNode) Search
                .searchNodes(parser.getRootTree(), new FunctionNodeCondition(), "find_maximum(int[],int)").get(0);


        CFGGenerationforSubConditionCoverage cfgGen = new CFGGenerationforSubConditionCoverage(function);
        ICFG cfg = cfgGen.generateCFG();
        cfg.setFunctionNode(function);
        cfg.setIdforAllNodes();
        cfg.resetVisitedStateOfNodes();

        int maxIterations = 1;
        PossibleTestpathGeneration tpGen = new PossibleTestpathGeneration(cfg, maxIterations);
        tpGen.generateTestpaths();

        System.out.println("Num of test paths: " + tpGen.getPossibleTestpaths().size());
        System.out.println(tpGen.getPossibleTestpaths().get(0));
    }

    @Override
    public void generateTestpaths()
    {
        // Date startTime = Calendar.getInstance().getTime();
        FullTestpaths testpaths_ = new FullTestpaths();

        ICfgNode beginNode = cfg.getBeginNode();
        FullTestpath initialTestpath = new FullTestpath();
        initialTestpath.setFunctionNode(cfg.getFunctionNode());
        try
        {
            traverseCFG(beginNode, initialTestpath, testpaths_);
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

        // Calculate the running time
        // Date end = Calendar.getInstance().getTime();
        // totalRunningTime = end.getTime() - startTime.getTime();
        // logger.debug("Total running time: " + totalRunningTime + " ms");
        // logger.debug("Solving time: " + solvingTime + " ms");
        // logger.debug("Number of solving calls: " + numberOfSolvingCalls + "
        // ms");
        // logger.debug(
        // "Number of solving calls that does not have solution: " +
        // numberOfSolvingCallsThatNoSolution + " ms");
    }

    private void traverseCFG(ICfgNode stm, FullTestpath tp, FullTestpaths testpaths) throws Exception
    {

        tp.add(stm);
        if (stm instanceof EndFlagCfgNode)
        {
            testpaths.add((FullTestpath) tp.clone());
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

                        traverseCFG(falseNode, tp, testpaths);
                        traverseCFG(trueNode, tp, testpaths);
                    }
                    else
                    {
                        traverseCFG(falseNode, tp, testpaths);
                    }
                }
                else
                {
                    traverseCFG(falseNode, tp, testpaths);
                    traverseCFG(trueNode, tp, testpaths);
                }
            }
            else
            {
                traverseCFG(trueNode, tp, testpaths);
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
                String staticSolution = new Z3SolutionParser().getSolution(z3.getSolution());

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
            return initializeTestdataAtRandom(function);
        }
    }

    /**
     * Initialize data at random
     *
     * @return
     */
    protected String initializeTestdataAtRandom(IFunctionNode function) {
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
    @Override
    public ICFG getCfg()
    {
        return cfg;
    }

    @Override
    public void setCfg(ICFG cfg)
    {
        this.cfg = cfg;
    }

    @Override
    public int getMaxIterationsforEachLoop()
    {
        return maxIterationsforEachLoop;
    }

    @Override
    public void setMaxIterationsforEachLoop(int maxIterationsforEachLoop)
    {
        this.maxIterationsforEachLoop = maxIterationsforEachLoop;
    }

    @Override
    public FullTestpaths getPossibleTestpaths()
    {
        return possibleTestpaths;
    }
}
