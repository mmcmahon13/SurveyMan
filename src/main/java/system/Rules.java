package system;

import csv.CSVParser;
import org.apache.log4j.Logger;
import survey.Block;
import survey.Question;
import survey.Survey;
import survey.SurveyException;
import java.util.List;
import java.util.Map;


public class Rules {


    final private static Logger LOGGER = Logger.getLogger(Rules.class);

    private static void ensureBranchForward(int[] toBlock, Question q, CSVParser parser) throws SurveyException {
        int[] fromBlock = q.block.id;
        String toBlockStr = String.valueOf(toBlock[0]);
        for (int i=1; i<toBlock.length; i++)
            toBlockStr = toBlockStr + "." + toBlock[i];
        if (fromBlock[0]>=toBlock[0]) {
            SurveyException e = new CSVParser.BranchException(q.block.strId, toBlockStr, parser, parser.getClass().getEnclosingMethod());
            LOGGER.warn(e);
            throw e;
        }
    }

    public static void ensureBranchForward(Survey survey, CSVParser parser) throws SurveyException {
        for (Question q : survey.questions) {
            if (q.branchMap.isEmpty())
                continue;
            for (Block b : q.branchMap.values()) {
                ensureBranchForward(b.id, q, parser);
            }
        }
    }


    public static void ensureCompactness(CSVParser parser) throws SurveyException {
        //first check the top level
        List<Block> topLevelBlocks = parser.getTopLevelBlocks();
        Map<String, Block> allBlockLookUp = parser.getAllBlockLookUp();
        Block[] temp = new Block[topLevelBlocks.size()];
        for (Block b : topLevelBlocks) {
            if (temp[b.id[0]-1]==null)
                temp[b.id[0]-1]=b;
            else {
                SurveyException e = new CSVParser.SyntaxException(String.format("Block %s is noncontiguous.", b.strId)
                        , parser
                        , parser.getClass().getEnclosingMethod());
                LOGGER.warn(e);
                throw e;
            }
        }
        for (Block b : allBlockLookUp.values())
            if (b.subBlocks!=null)
                for (Block bb : b.subBlocks)
                    if (bb==null) {
                        SurveyException e = new CSVParser.SyntaxException(String.format("Detected noncontiguous subblock in parent block %s", b.strId)
                                , parser
                                , parser.getClass().getEnclosingMethod());
                        LOGGER.warn(e);
                        throw e;
                    }
    }
}
