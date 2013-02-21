package eu.monnetproject.coal;

import java.util.List;

import eu.monnetproject.align.Aligner;
import eu.monnetproject.align.Alignment;
import eu.monnetproject.align.AlignmentSerializer;
import eu.monnetproject.align.Matcher;
import eu.monnetproject.align.Match;
import eu.monnetproject.coal.svmrank.SVMRankMatcher;
import eu.monnetproject.framework.services.Services;
import eu.monnetproject.label.LabelExtractorFactory;
import eu.monnetproject.ontology.Entity;
import eu.monnetproject.ontology.Ontology;
import eu.monnetproject.ontology.OntologySerializer;
import eu.monnetproject.sim.EntitySimilarityMeasure;
import java.io.File;
import java.io.FileReader;
import java.util.Collections;
import java.util.logging.Logger;

/**
 * Coal implementation of an Aligner.
 *
 * @author Dennis Spohr
 *
 */
public class CoalAligner implements Aligner {

    private Logger log = Logger.getLogger(CoalAligner.class.getName());
    private final Matcher coalMatcher;
    public volatile int progress = 0;

    public CoalAligner(Matcher coalMatcher) {
        this.coalMatcher = coalMatcher;
    }

    public void reconfigure(File matcherCfg, File svmRankClassify, File svmRankLearn, File modelFile) {
        if (coalMatcher instanceof SVMRankMatcher) {
            ((SVMRankMatcher) coalMatcher).reconfigure(matcherCfg, svmRankClassify, svmRankLearn, modelFile);
        }
    }

    public Alignment align(Ontology ontlg, Ontology ontlg1) {
        Alignment alignment = new CoalAlignment();
        align(ontlg1, ontlg1, alignment);
        return alignment;
    }
    
    public Alignment align(Ontology ontlg, Ontology ontlg1, int k) {
        Alignment alignment = new CoalAlignment(k);
        align(ontlg1, ontlg1, alignment);
        return alignment;
    }

    public void align(Ontology srcOntology, Ontology tgtOntology,
            Alignment alignment, boolean ignoreInstances) {

        this.progress = 0;

        if (srcOntology == null) {
            log.severe("Source ontology is null!");
        }
        if (tgtOntology == null) {
            log.severe("Target ontology is null!");
        }
        if (srcOntology == null || tgtOntology == null) {
            System.exit(-1);
        }

        log.info("Aligning " + srcOntology + " and " + tgtOntology);

        alignment.setSourceOntology(srcOntology);
        alignment.setTargetOntology(tgtOntology);

        for (Entity ent1 : srcOntology.getEntities()) {

            if (ignoreInstances) {
                if (ent1 instanceof eu.monnetproject.ontology.LiteralOrIndividual) {
                    log.info("Skipping individual " + ent1.getURI().toString());
                    this.progress++;
                    continue;
                }
            }

            log.info("Processing " + ent1);

            List<Match> matches = this.coalMatcher.getMatches(ent1, tgtOntology, alignment.getMaximumNumberOfMatches());

            try {
                alignment.addAllMatches(matches);
            } catch (IllegalArgumentException e) {
                log.warning("No matches for " + ent1.getURI());
            }

            this.progress++;

        }

    }

    /**
     * Align two ontologies and store the result in
     * <code>alignment</code>.
     *
     * @param srcOntology source ontology
     * @param tgtOntology target ontology
     * @param alignment alignment
     */
    @Override
    public void align(Ontology srcOntology, Ontology tgtOntology,
            Alignment alignment) {

        align(srcOntology, tgtOntology, alignment, true);

    }

    public int getProgress() {
        return this.progress;
    }

    public static void main(String[] args) throws Exception {
        //args = "load/SourceOntology.owl load/TargetOntology.owl out 5".split(" ");
        if (args.length != 3 && args.length != 4) {
            System.err.println("Usage:\tmvn exec:java -Dexec.mainClass=\"" + CoalAligner.class.getCanonicalName() + "\" -Dexec.args=\"ontology1 ontology2 output [noOfMatches]\"");
            System.exit(-1);
        }
        Services.get(LabelExtractorFactory.class);
        final OntologySerializer serializer = Services.get(OntologySerializer.class);
        final AlignmentSerializer alignSerializer = Services.get(AlignmentSerializer.class);
        final Aligner aligner = new CoalAligner(new SVMRankMatcher(Services.getAll(EntitySimilarityMeasure.class)));
        final Ontology ontology1 = serializer.read(new FileReader(args[0]));
        final Ontology ontology2 = serializer.read(new FileReader(args[1]));
        final int noOfMatches;
        if(args.length == 4) {
            noOfMatches = Integer.parseInt(args[3]);
        } else {
            noOfMatches = 1;
        }
        final Alignment alignment = aligner.align(ontology1, ontology2, noOfMatches);
        alignSerializer.writeAlignment(alignment, new File(args[2]));
        
        // Print scores (for Xichuan)
        for(Entity entity : alignment.getSourceEntities()) {
            for(Match match : alignment.getMatches(entity)) {
                System.out.println(match.getSourceEntity() + " -> " + match.getTargetEntity() + " (" + match.getScore() + ")");
            }
        }
    }
}
