package coverage;

import coverage.AbstractCoverageComputation;
import parser.projectparser.ProjectParser;
import tree.object.INode;
import tree.object.SourcecodeFileNode;

import java.io.File;
import java.util.List;
import java.util.Map;

/**
 * This class is used to compute coverage of source code file.
 *
 * The type of coverage is only STATEMENT, BRANCH, and MCDC.
 *
 * For STATEMENT+BRANCH, STATEMENT+MCDC, these kinds of coverage include two coverage types.
 */
public class SourcecodeCoverageComputation extends AbstractCoverageComputation
{

    public static void main(String[] args) {
        // parse project
//        ProjectParser projectParser = new ProjectParser(new File("/Users/ducanhnguyen/Documents/akautauto/datatest/official_tested_projects/c-algorithms/src"));
//
//
//        ProjectNode projectRoot = projectParser.getRootTree();
//        Environment.getInstance().setProjectNode(projectRoot);
//
//        INode root = projectParser.getRootTree();
//        ISourcecodeFileNode consideredSourcecodeNode = (ISourcecodeFileNode) Search
//                .searchNodes(root, new SourcecodeFileNodeCondition(), "list.c").get(0);
//        System.out.println(consideredSourcecodeNode.getAbsolutePath());
//
//        // compute the number of statements/branches/mdcdc
//        InstructionComputator instructionComputator = new InstructionComputator();
//        List<INode> srcNodes = Search.searchNodes(root, new SourcecodeFileNodeCondition());
//        for (INode sourcecodeNode: srcNodes) {
//            int[] instructionComputation = instructionComputator.getNumberOfBranchesAndStatements(sourcecodeNode);
//
//            int nStatement = instructionComputation[InstructionComputator.STATEMENT_COV_INDEX];
//            Environment.getInstance().getStatementsMapping().put(PathUtils.toRelative(sourcecodeNode.getAbsolutePath()), nStatement);
//
//            int nBranch = instructionComputation[InstructionComputator.BRANCH_COV_INDEX];
//            Environment.getInstance().getBranchesMapping().put(PathUtils.toRelative(sourcecodeNode.getAbsolutePath()), nBranch);
//
//            int nMcdc = instructionComputation[InstructionComputator.MCDC_COV_INDEX];
//            Environment.getInstance().getMcdcMapping().put(PathUtils.toRelative(sourcecodeNode.getAbsolutePath()), nMcdc);
//        }
//        //
//        String tpFile = "/Users/ducanhnguyen/Documents/akautauto/datatest/official_tested_projects/c-algorithms/aka-working-space/calgovv/test-paths/list_data.2.tp";
//        SourcecodeCoverageComputation computator = new SourcecodeCoverageComputation();
//        computator.setTestpathContent(Utils.readFileContent(tpFile));
//        computator.setConsideredSourcecodeNode(consideredSourcecodeNode);
//        computator.setCoverage(EnviroCoverageTypeNode.STATEMENT);
//        computator.compute();
//        System.out.println("number of instructions = " + computator.getNumberOfInstructions());
//        System.out.println("number of visited instructions = " + computator.getNumberOfVisitedInstructions());
//
//        // highlighter
//        SourcecodeHighlighterForCoverage highlighter = new SourcecodeHighlighterForCoverage();
//        highlighter.setSourcecode(consideredSourcecodeNode.getAST().getRawSignature());
//        highlighter.setTestpathContent(Utils.readFileContent(tpFile));
//        highlighter.setSourcecodePath("/Users/ducanhnguyen/Documents/akautauto/datatest/official_tested_projects/c-algorithms/src/list.c");
//        highlighter.setAllCFG(computator.getAllCFG());
//        highlighter.setTypeOfCoverage(computator.getCoverage());
//        highlighter.highlight();
//        Utils.writeContentToFile(highlighter.getFullHighlightedSourcecode(), "/Users/ducanhnguyen/Desktop/x.html");
    }

    public void compute() {
        if (consideredSourcecodeNode == null || !(new File(consideredSourcecodeNode.getAbsolutePath())).exists()
                || !(consideredSourcecodeNode instanceof SourcecodeFileNode)
                || testpathContent == null || testpathContent.length() == 0) {
            this.numberOfInstructions = getNumberOfInstructions(consideredSourcecodeNode, coverage);
            return;
        }
        if (coverage.equals(EnviroCoverageTypeNode.STATEMENT_AND_BRANCH) ||
                coverage.equals(EnviroCoverageTypeNode.STATEMENT_AND_MCDC))
            return;

        Map<String, TestpathsOfAFunction> affectedFunctions = categoryTestpathByFunctionPath(testpathContent.split("\n"), coverage);
        affectedFunctions = removeRedundantTestpath(affectedFunctions);

        int nInstructions = getNumberOfInstructions(consideredSourcecodeNode, coverage);

        int nVisitedInstructions;
//        if (visitedCache.containsKey(testpathContent)) {
//            logger.debug("[" + Thread.currentThread().getName() + "] " + "Coverage Computer Cache Hit");
//            nVisitedInstructions = visitedCache.get(testpathContent);
//            logger.debug("[" + Thread.currentThread().getName() + "] " + "Visited instructions: " + nVisitedInstructions);
//        } else {
//            logger.debug("[" + Thread.currentThread().getName() + "] " + "Coverage Computer Cache Miss");
//            logger.debug("[" + Thread.currentThread().getName() + "] " + "Calculate visited instructions");
        nVisitedInstructions = getNumberOfVisitedInstructions(affectedFunctions, coverage, consideredSourcecodeNode, allCFG);
//            visitedCache.put(testpathContent, nVisitedInstructions);
//        }

        this.numberOfInstructions = nInstructions;
        this.numberOfVisitedInstructions = nVisitedInstructions;
    }

//    protected Map<String, TestpathsOfAFunction>  removeRedundantTestpath(Map<String, TestpathsOfAFunction> affectedFunctions){
//        return affectedFunctions;
//    }

    @Override
    protected Map<String, TestpathsOfAFunction> removeRedundantTestpath(Map<String, TestpathsOfAFunction> affectedFunctions)
    {
        return affectedFunctions;
    }

    @Override
    protected int getNumberofBranches(INode consideredSourcecodeNode) {
        return 0;// Environment.getInstance().getBranchesMapping().get(PathUtils.toRelative(consideredSourcecodeNode.getAbsolutePath()));
    }

    @Override
    protected int getNumberofStatements(INode consideredSourcecodeNode) {
        return 0;// Environment.getInstance().getStatementsMapping().get(PathUtils.toRelative(consideredSourcecodeNode.getAbsolutePath()));
    }

}
