package eu.monnetproject.align;

import java.util.Collection;
import java.util.List;

import eu.monnetproject.ontology.Entity;
import eu.monnetproject.ontology.Ontology;
import eu.monnetproject.sim.EntitySimilarityMeasure;
import java.io.File;

/**
 * A trainable matcher.
 * 
 * @author Dennis Spohr
 *
 */
public interface Matcher {

        /**
         * Tells whether the model has already been trained or not.
         * @return
         */
        public boolean isTrained();
        
        /**
         * Trains an untrained model on an existing alignment.
         * 
         * @param alignment
         */
        public void train(Collection<Alignment> alignments, File modeFile);
        
        public Collection<EntitySimilarityMeasure> getMeasures();
        
        /**
         * Returns a list of k matches for a source entity in a target ontology.
         * 
         * @param srcEntity source entity
         * @param tgtOntology target ontology
         * @param ranks number of matches to return
         * @return list of ranked matches
         */
        public List<Match> getMatches(Entity srcEntity, Ontology tgtOntology, int ranks);

        
}
