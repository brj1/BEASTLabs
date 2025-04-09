package hack.app;

import beast.base.evolution.tree.Tree;
import beast.base.parser.NexusParser;
import beastlabs.evolution.tree.RNNIMetric;
import java.io.File;
import java.io.FileWriter;
import java.util.List;

/**
 *
 * @author bjones
 */
public class TreeComparerNexus {
    
    public static void main(String[] args) {
        String treeFileName = args[0];
        String outputFile = args[1];
        
        try {
       NexusParser parser = new NexusParser();
       parser.parseFile(new File(treeFileName));
       List<Tree> trees = parser.trees;
       
       RNNIMetric metric = new RNNIMetric();
       metric.taxonsetInput.setValue(trees.get(0).getTaxonset(), metric);
       metric.initAndValidate();
       
       FileWriter output = new FileWriter(outputFile);
       output.write("tree1,tree2,rNNI\n");
       
       for (int i = 0; i < trees.size() - 1; i++) {
           for (int j = i + 1; j < trees.size(); j++) {
               double distance = metric.distance(trees.get(i), trees.get(j));
               output.write(String.format("%d,%d,%f\n", i + 1, j + 1, distance));
           }
       
       }
       output.close();
        
        } catch (Exception e) {
            e.printStackTrace();
        } 
    }
}
