package hack.app;

import beast.base.evolution.tree.Tree;
import beast.base.evolution.tree.TreeParser;
import beastlabs.evolution.tree.RNNIMetric;
import java.io.File;
import java.io.FileWriter;
import java.util.Scanner;

/**
 *
 * @author bjones
 */
public class TreeComparer {
    
    public static void main(String[] args) {
        String treeFileName1 = args[0];
        String treeFileName2 = args[1];
        String outputFile = args[2];
        
        
        try {
       Scanner stream1 = new Scanner(new File(treeFileName1));
       Tree tree1 = new TreeParser(stream1.nextLine());
       stream1.close();
       
        Scanner stream2 = new Scanner(new File(treeFileName2));
       Tree tree2 = new TreeParser(stream2.nextLine());
       stream2.close();
       
       RNNIMetric metric = new RNNIMetric();
       metric.taxonsetInput.setValue(tree1.getTaxonset(), metric);
       metric.initAndValidate();
       double distance = metric.distance(tree1, tree2);
       
       FileWriter output = new FileWriter(outputFile);
       output.write(String.valueOf(distance));
       output.close();
        
        } catch (Exception e) {
            e.printStackTrace();
        } 
    }
}
