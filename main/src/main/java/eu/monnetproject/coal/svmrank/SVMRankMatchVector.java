package eu.monnetproject.coal.svmrank;

import eu.monnetproject.ontology.Entity;

/**
 * Class representing a vector of similarity scores between two entities.
 * 
 * @author dspohr
 *
 */
public class SVMRankMatchVector {
        
        private Entity srcEntity;
        private Entity tgtEntity;
        
        /**
         * Target value as required by the SVMrank tool.
         */
        private String target;
        
        /**
         * The individual scores in the vector. 
         */
        private double[] scores;

        public SVMRankMatchVector(Entity srcEntity, Entity tgtEntity, String target, int numOfFeatures) {
                this.srcEntity = srcEntity;
                this.tgtEntity = tgtEntity;
                this.target = target;
                this.scores = new double[numOfFeatures];
        }
        
        public String getTarget() {
                return this.target;
        }
        
        public double[] getScores() {
                return this.scores;
        }
        
        public Entity getTargetEntity() {
                return this.tgtEntity;
        }

        public Entity getSourceEntity() {
                return this.srcEntity;
        }

        public void addScore(int index, double similarity) throws IllegalArgumentException {
                if (index >= this.scores.length)
                        throw new IllegalArgumentException("Feature index too high for vector.");
                this.scores[index] = similarity;
        }

}
