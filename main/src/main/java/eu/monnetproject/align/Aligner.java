package eu.monnetproject.align;

import eu.monnetproject.ontology.Ontology;

/**
 * Interface for aligning two ontologies
 * 
 * @author Dennis Spohr
 *
 */
public interface Aligner {

        /**
         * Aligns two ontologies and stores the results in <code>alignment</code>.
         * 
         * @param srcOntology source ontology
         * @param tgtOntology target ontology
         * @param alignment the suggested alignment
         */
        public void align(Alignment alignment);

        /**
         * Aligns two ontologies and stores the results in <code>alignment</code>.
         * 
         * @param srcOntology source ontology
         * @param tgtOntology target ontology
         * @return  the suggested alignment
         */
        public Alignment align(Ontology srcOntology, Ontology tgtOntology);
        
        /**
         * Aligns two ontologies and stores the results in <code>alignment</code>.
         * 
         * @param srcOntology source ontology
         * @param tgtOntology target ontology
         * @param k number of matches to return
         * @return  the suggested alignment
         */
        public Alignment align(Ontology srcOntology, Ontology tgtOntology, int k);
        
        int getProgress();

}
