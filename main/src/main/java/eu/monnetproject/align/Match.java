package eu.monnetproject.align;

import java.util.Collection;

import eu.monnetproject.ontology.Entity;
import eu.monnetproject.sim.EntitySimilarityMeasure;

/**
 * Interface representing a match between two entities.
 * 
 * @author Dennis Spohr
 *
 */
public interface Match {

    /** Get the source entity of the match */
    public Entity getSourceEntity();

    /** Get the target entity of the match */
    public Entity getTargetEntity();

    /**
     * Returns the type of relation (e.g. "=", "<" or ">") between source and target entity as a String value.
     * 
     * @return the type of relation
     */
    public String getRelation();

    /**
     * The score for the match.
     * 
     * @return
     */
    public double getScore();

    /**
     * Returns the measures used to calculate the score of the match. Might be deprecated and changed for a method getAligner()...
     * 
     * @return
     */
    public Collection<EntitySimilarityMeasure> getMeasures();
}
