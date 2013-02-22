package eu.monnetproject.coal;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import eu.monnetproject.align.Match;
import eu.monnetproject.align.Alignment;
import eu.monnetproject.ontology.AnnotationProperty;
import eu.monnetproject.ontology.AnnotationValue;
import eu.monnetproject.ontology.Entity;
import eu.monnetproject.ontology.Ontology;
import java.util.Collections;

/**
 * Default implementation of Alignment.
 *
 * @author Dennis Spohr
 *
 */
public class CoalAlignment implements Alignment {

    private java.util.logging.Logger log = java.util.logging.Logger.getLogger(CoalAlignment.class.getName());
    /**
     * Maximum number of matches per source entity.
     */
    private int numberOfMatches;
    /**
     * Map storing the matches for each source entity URI. Strictly speaking,
     * this map is not necessary, since the source entity is also contained in
     * each Match instance. However, this way it is easier to access the matches
     * for a source entity.
     */
    private Map<URI, List<Match>> matches = new HashMap<URI, List<Match>>();
    private Ontology srcOntology;
    private Ontology tgtOntology;

    /**
     * Creates a new DefaultAlignment instance with at most 1 match per source
     * entity.
     */
    public CoalAlignment(Ontology srcOntology, Ontology trgOntology) {
        this(1, srcOntology, trgOntology);
        System.err.println("CoalAlignment constructor");
    }

    /**
     * Creates a new DefaultAlignment instance with at most k matches per source
     * entity.
     *
     * @param k maximum number of matches per source entity
     */
    public CoalAlignment(int k, Ontology srcOntology, Ontology trgOntology) {
        this.numberOfMatches = k;
        this.srcOntology = srcOntology;
        this.tgtOntology = trgOntology;
    }

    /**
     * Adds a new match to the Alignment
     *
     * @param match the match to be added
     */
    public void addMatch(Match match) {

        List<Match> thisMatches = this.matches.get(match.getSourceEntity().getURI());

        this.matches.put(match.getSourceEntity().getURI(), addSorted(thisMatches, match));

    }

    /**
     * Makes sure that the new match is added at the correct position (i.e.
     * sorted by score). If the score for match is smaller than the existing
     * matches for the source entity and there are already k matches for the
     * source entity, match is not added.
     *
     * @param matches list of existing matches for a source entity, sorted by
     * score
     * @param match match to be added
     * @return the new list of matches
     */
    private List<Match> addSorted(List<Match> matches, Match match) {

        if (matches == null) {
            matches = new ArrayList<Match>();
        }

        if (matches.size() < 1) {
            matches.add(match);
            return matches;
        }

        for (Match currentMatch : matches) {
            if (currentMatch.getScore() < match.getScore()) {
                int index = matches.indexOf(currentMatch);
                matches.add(index, match);
                try {
                    return matches.subList(0, this.numberOfMatches);
                } catch (IndexOutOfBoundsException e) {
                    return matches;
                }
            }
        }

        if (matches.size() < this.numberOfMatches) {
            matches.add(match);
        }

        return matches;
    }

    /**
     * Makes sure that the new matches are added at the correct position (i.e.
     * sorted by score). If the score for a match is smaller than the scores of
     * the existing matches for the source entity and there are already k
     * matches for the source entity, the match is not added.
     *
     * @param matches list of existing matches for a source entity, sorted by
     * score
     * @param newMatches list of matches to be added
     * @return the new list of matches
     */
    private List<Match> addAllSorted(List<Match> matches, List<Match> newMatches) {

        if (newMatches.size() < 1) {
            log.warning("Attempted to add 0 matches!");
            return matches;
        }

        for (Match newMatch : newMatches) {
            addSorted(matches, newMatch);
        }

        return matches;
    }

    @Override
    public void addAllMatches(List<Match> matches) throws IllegalArgumentException {

        if (matches.size() > getMaximumNumberOfMatches()) {
            throw new IllegalArgumentException("Number of matches exceeds maximum number of matches for this alignment. Will only add the first " + getMaximumNumberOfMatches() + " matches.");
        }
        if (matches == null || matches.size() == 0) {
            throw new IllegalArgumentException("Empty list of matches.");
        }

        Iterator<Match> iterator = matches.iterator();
        Entity entity = iterator.next().getSourceEntity();

        while (iterator.hasNext()) {
            if (iterator.next().getSourceEntity() != entity) {
                throw new IllegalArgumentException("List of matches contains different source entities.");
            }
        }

        List<Match> matchesOfEntity = getMatches(entity);

        this.matches.put(entity.getURI(), addAllSorted(matchesOfEntity, matches));

    }

    public int getMaximumNumberOfMatches() {
        return this.numberOfMatches;
    }

    @Override
    public List<Match> getMatches(Entity sourceEntity) {

        if (matches.get(sourceEntity.getURI()) == null) {
            return new ArrayList<Match>();
        }

        return matches.get(sourceEntity.getURI());

    }

    @Override
    public Collection<Entity> getSourceEntities() {

        Collection<Entity> rv = new HashSet<Entity>();
        Collection<URI> added = new HashSet<URI>();


        for (URI uri : matches.keySet()) {
            if (srcOntology != null) {
                for (Entity entity : srcOntology.getEntities(uri)) {
                    if (!added.contains(entity.getURI())) {
                        rv.add(entity);
                        added.add(entity.getURI());
                    }
                }
            } else {
                rv.add(new EntityImpl(uri));
            }
        }

        return rv;

    }

    @Override
    public Ontology getSourceOntology() {
        return this.srcOntology;
    }

    @Override
    public Ontology getTargetOntology() {
        return this.tgtOntology;
    }

    @Override
    public void setSourceOntology(Ontology ontology) {
        this.srcOntology = ontology;
    }

    @Override
    public void setTargetOntology(Ontology ontology) {
        this.tgtOntology = ontology;
    }

    @Override
    public boolean isEmpty() {
        return matches.isEmpty();
    }
    
    private static class EntityImpl implements Entity {
        private final URI uri;

        public EntityImpl(URI uri) {
            this.uri = uri;
        }
        
        
        
        @Override
        public URI getURI() {
            return uri;
        }

        @Override
        public String getID() {
            return null;
        }

        @Override
        public Map<AnnotationProperty, Collection<AnnotationValue>> getAnnotations() {
            return Collections.EMPTY_MAP;
        }

        @Override
        public Collection<AnnotationValue> getAnnotationValues(AnnotationProperty property) {
            return Collections.EMPTY_LIST;
        }

        @Override
        public boolean addAnnotation(AnnotationProperty property, AnnotationValue annotation) {
            throw new UnsupportedOperationException("Not mutable");
        }

        @Override
        public boolean removeAnnotation(AnnotationProperty property, AnnotationValue annotation) {
            throw new UnsupportedOperationException("Not mutable");
        }

        @Override
        public Ontology getOntology() {
            throw new UnsupportedOperationException("No ontology.");
        }

        @Override
        public Collection<Entity> getPuns() {
            return Collections.EMPTY_LIST;
        }
        
    }
}
