package eu.monnetproject.coal;

import eu.monnetproject.align.Aligner;
import eu.monnetproject.align.Alignment;
import eu.monnetproject.align.AlignmentSerializer;
import eu.monnetproject.coal.svmrank.SVMRankMatcher;
import eu.monnetproject.framework.services.Services;
import eu.monnetproject.ontology.Ontology;
import eu.monnetproject.ontology.OntologySerializer;
import eu.monnetproject.sim.EntitySimilarityMeasure;
import java.io.File;
import java.io.FileReader;
import java.util.Collections;
import java.util.LinkedList;

/**
 *
 * @author jmccrae
 */
public class CoalTrainer {

    public static void main(String[] args) throws Exception {
        if(args.length != 5) {
            System.err.println("Usage:\n\tmvn exec:java -Dexec.mainClass=" + CoalTrainer.class + " -Dexec.args=\"sourceOntology targetOntology alignment initConfig modelOutFile\"");
            return;
        }
        System.setProperty("matcher", args[3]);
        final OntologySerializer serializer = Services.get(OntologySerializer.class);
        final AlignmentSerializer alignSerializer = Services.get(AlignmentSerializer.class);
        final SVMRankMatcher matcher = new SVMRankMatcher(Services.getAll(EntitySimilarityMeasure.class));
        final Ontology ontology1 = serializer.read(new FileReader(args[0]));
        final Ontology ontology2 = serializer.read(new FileReader(args[1]));
        final Alignment alignment = alignSerializer.readAlignment(new File(args[2]));
        alignment.setSourceOntology(ontology1);
        alignment.setTargetOntology(ontology2);
        if(alignment.isEmpty()) {
            System.err.println("No alignments!");
            return;
        }
        matcher.train(Collections.singleton(alignment), new File(args[4]));
    }
}
