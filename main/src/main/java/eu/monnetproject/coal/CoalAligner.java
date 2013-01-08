package eu.monnetproject.coal;

import java.util.List;
import eu.monnetproject.util.Logger;

import eu.monnetproject.align.Aligner;
import eu.monnetproject.align.Alignment;
import eu.monnetproject.align.Matcher;
import eu.monnetproject.align.Match;
import eu.monnetproject.coal.svmrank.SVMRankMatcher;
import eu.monnetproject.ontology.Entity;
import eu.monnetproject.ontology.Ontology;
import eu.monnetproject.util.Logging;
import java.io.File;

/**
 * Coal implementation of an Aligner.
 * 
 * @author Dennis Spohr
 *
 */
public class CoalAligner implements Aligner {

        private Logger log = Logging.getLogger(this);
        private final Matcher coalMatcher;
        public volatile int progress = 0;

    public CoalAligner(Matcher coalMatcher) {
        this.coalMatcher = coalMatcher;
    }

    public void reconfigure(File matcherCfg, File svmRankClassify, File svmRankLearn, File modelFile) {
        if(coalMatcher instanceof SVMRankMatcher) {
            ((SVMRankMatcher)coalMatcher).reconfigure(matcherCfg, svmRankClassify, svmRankLearn,modelFile);
        }
    }
    
    public Alignment align(Ontology ontlg, Ontology ontlg1) {
        Alignment alignment = new CoalAlignment();
        align(ontlg1, ontlg1, alignment);
        return alignment;
    }
        
        
        
        public void align(Ontology srcOntology, Ontology tgtOntology,
                Alignment alignment, boolean ignoreInstances) {
        	
        	this.progress = 0;
        	
        	if (srcOntology == null)
        		log.severe("Source ontology is null!");
        	if (tgtOntology == null)
        		log.severe("Target ontology is null!");
        	if (srcOntology == null || tgtOntology == null)
        		System.exit(-1);
        		
        	log.info("Aligning "+srcOntology+ " and "+tgtOntology);

        	alignment.setSourceOntology(srcOntology);
        	alignment.setTargetOntology(tgtOntology);
        	
        	for(Entity ent1 : srcOntology.getEntities()) {

    			if (ignoreInstances) {
    				if (ent1 instanceof eu.monnetproject.ontology.LiteralOrIndividual) {
        				log.info("Skipping individual "+ent1.getURI().toString());
    					this.progress++;
    					continue;
    				}
    			}
    			
    			log.info("Processing "+ent1);
    			
    			List<Match> matches = this.coalMatcher.getMatches(ent1,tgtOntology,alignment.getMaximumNumberOfMatches());

    			try{
        			alignment.addAllMatches(matches);
        		} catch (IllegalArgumentException e) {
        			log.warning("No matches for "+ent1.getURI());
        		}
        		
        		this.progress++;
        		
        	}
        	
        }
        
        /**
         * Align two ontologies and store the result in <code>alignment</code>.
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

        
}
