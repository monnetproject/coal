package eu.monnetproject.align;

import java.util.Collection;
import java.util.List;

import eu.monnetproject.ontology.Entity;
import eu.monnetproject.ontology.Ontology;

/**
 * Interface representing an alignment between two ontologies. 
 * 
 * @author Dennis Spohr
 *
 */
public interface Alignment {

    /**
     * Adds a new match to the Alignment.
     * 
     * @param match the match to be added
     */
    public void addMatch(Match match);
    
    /**
     * Get the source ontology.
     * 
     * @return source ontology
     */
    public Ontology getSourceOntology();
    
    /**
     * Get the target ontology.
     * 
     * @return target ontology
     */
    public Ontology getTargetOntology();
    
    /**
     * Set the source ontology.
     * 
     * @param ontology source ontology
     */
    public void setSourceOntology(Ontology ontology);
    
    /**
     * Set the target ontology.
     * 
     * @param ontology target ontology
     */
    public void setTargetOntology(Ontology ontology);
    
    /**
     * Returns all matches of a source entity. 
     * 
     * @return list of matches
     */
    public List<Match> getMatches(Entity sourceEntity);
    
    /**
     * Returns all source entities in the alignment.
     * 
     * @return collection of entities
     */
    public Collection<Entity> getSourceEntities();
    
    /**
     * Adds a list of new matches to the Alignment.
     * 
     * @param matches the matches to be added
     */
    public void addAllMatches(List<Match> matches);
        
    /**
     * Returns the maximum number of matches that is allowed per source entity.
     * 
     * @return maximum number of matches per source entity
     */
    public int getMaximumNumberOfMatches();
                
}
