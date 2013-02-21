package eu.monnetproject.coal.svmrank;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import eu.monnetproject.align.Alignment;
import eu.monnetproject.align.Matcher;
import eu.monnetproject.align.Match;
import eu.monnetproject.coal.CoalMatch;
import eu.monnetproject.ontology.Entity;
import eu.monnetproject.ontology.Ontology;
import eu.monnetproject.config.Configurator;
import eu.monnetproject.sim.EntitySimilarityMeasure;
import java.io.InputStreamReader;
import java.util.logging.Logger;

/**
 * Implementation of an SVMrank model.
 *
 * @author Dennis Spohr
 *
 */
public class SVMRankMatcher implements Matcher {

        private Logger log = Logger.getLogger(SVMRankMatcher.class.getName());
    /**
     * We assume the following static variables as target values (see
     * http://www.cs.cornell.edu/People/tj/svm_light/svm_rank.html).
     */
    private static final String UNKNOWN_MATCH_TARGET = "?";
    private static final String NO_MATCH_TARGET = "1";
    private static final String CLOSE_MATCH_TARGET = "2";
    private static final String EXACT_MATCH_TARGET = "3";
    private static final String EXACT_MATCH_RELATION = "skos:exactMatch";
    private static final String CLOSE_MATCH_RELATION = "skos:closeMatch";
    private static final double DEFAULT_REGULARISATION_PARAMETER = 0.01;
    private static final File DEFAULT_DIR = new File("load/");
    /**
     * Path to the svm_rank_classify executable.
     */
    private File svmRankClassify;
    /**
     * Path to the svm_rank_learn executable.
     */
    private File svmRankLearn;
    /**
     * Map containing the similarity vectors for each source entity.
     */
    private Map<Entity, List<SVMRankMatchVector>> vectors = new HashMap<Entity, List<SVMRankMatchVector>>();
    /**
     * Boolean indicating whether the model is trained or not. Only trained
     * models can be used for ranking potential matches.
     */
    private boolean trained = false;
    /**
     * Configuration indicating the location of the actual model file and the
     * mapping from measures to indices in the vector.
     */
    private SVMRankMatcherConfiguration matcherConfig;
    /**
     * List of measures
     */
    private List<EntitySimilarityMeasure> measures = null;
    private List<EntitySimilarityMeasure> tmpMeasures = new LinkedList<EntitySimilarityMeasure>();
    private List<String> tmpMeasureNames = new LinkedList<String>();
    private boolean active = false;
    private boolean ignoreInstances = true;

    /**
     * SVMRankFile should be a cfg file specifying the location of the actual
     * SVMrank model file, as well as the location of the svm_rank_learn and
     * svm_rank_classify executables.
     *
     * @param config a cfg file
     */
//	public SVMRankMatcher(SVMRankMatcherConfiguration config) {
//		this.matcherConfig = config;
//		this.trained = true;
//	}
    public SVMRankMatcher(Collection<EntitySimilarityMeasure> entitySimilarityMeasures) {
        if(entitySimilarityMeasures.isEmpty()) {
            throw new IllegalArgumentException("No similarity measures");
        }
        for (EntitySimilarityMeasure measure : entitySimilarityMeasures) {
            this.tmpMeasures.add(measure);
            this.tmpMeasureNames.add(measure.getClass().getName());
            System.err.println("Using metric: " + measure.getClass().getName());
        }
        start();
    }

    public void reconfigure(File matcherCfg, File svmRankClassify, File svmRankLearn, File modelFile) {
        this.svmRankClassify = svmRankClassify;
        this.svmRankLearn = svmRankLearn;
        this.matcherConfig = new SVMRankMatcherConfiguration(matcherCfg,modelFile);
        this.trained = this.matcherConfig.hasModel();
        this.ignoreInstances = this.matcherConfig.ignoreInstances();
        initMeasures();
    }
    
    private void start() {
        log.info("Activating SVMRankModel");
        this.active = true;
        Properties properties = Configurator.getConfig("eu.monnetproject.coal");

        File f = new File(properties.getProperty("matcher", "load/matcher.cfg").toString());
        this.svmRankClassify = new File(properties.getProperty("svm_rank_classify", "load/svm_rank_classify").toString() + (System.getProperty("os.name").startsWith("Windows") ? ".exe" : ""));
        this.svmRankLearn = new File(properties.getProperty("svm_rank_learn", "load/svm_rank_learn").toString() + (System.getProperty("os.name").startsWith("Windows") ? ".exe" : ""));
        this.matcherConfig = new SVMRankMatcherConfiguration(f);
        this.trained = this.matcherConfig.hasModel();
        this.ignoreInstances = this.matcherConfig.ignoreInstances();
        initMeasures();
    }

    //@Deactivate
    public void stop() {
        this.active = false;
    }

    /*@Reference(type='+')
     public void addSimilarityMeasure(EntitySimilarityMeasure measure) {
     if (!isActive()) {
     } else
     log.warning("Not adding new similarity measure "+measure.getName()+" since "+this+" is already active.");
     }*/
    private boolean isActive() {
        return this.active;
    }

    public void removeSimilarityMeasure(EntitySimilarityMeasure measure) {
        this.tmpMeasures.remove(measure);
        this.tmpMeasureNames.remove(measure.getName());
    }

    private void initMeasures() {

        this.measures = new LinkedList<EntitySimilarityMeasure>();

        Properties measureConfiguration = matcherConfig.getMeasureConfiguration();

        List<String> measureNames = matcherConfig.getMeasureNames();

        for (String measureName : measureNames) {

            if (tmpMeasureNames.contains(measureName)) {
                log.info("Adding " + measureName);
                EntitySimilarityMeasure thisMeasure = this.tmpMeasures.get(this.tmpMeasureNames.indexOf(measureName));
                thisMeasure.configure(measureConfiguration);
                this.measures.add(thisMeasure);
            }

        }

        if (measureNames.size() > this.measures.size()) {
            log.severe("Not all required measures active.");
            stop();
        }

    }

    @Override
    public List<Match> getMatches(Entity srcEntity, Ontology tgtOntology, int ranks) throws UnsupportedOperationException {

        if (!this.trained) {
            throw new UnsupportedOperationException("Model is not trained.");
        }

        for (Entity ent2 : tgtOntology.getEntities()) {

            if (ignoreInstances) {
                if (ent2 instanceof eu.monnetproject.ontology.Individual) {
                    continue;
                }
            }

            match(srcEntity, ent2);

        }

        List<SVMRankMatchVector> vectors = this.vectors.get(srcEntity);

        List<Match> matches = null;

        try {
            log.info("Executing SVMrank classify");
            matches = executeSVMRankClassify(vectors);
        } catch (Exception ex) {
            log.severe(ex.getMessage());
        }

        return getTopKRanks(matches, ranks);

    }

    /**
     * Returns the top k matches.
     *
     * @param matches
     * @param ranks
     * @return
     */
    private List<Match> getTopKRanks(List<Match> matches, int ranks) {

        if (matches == null || matches.size() < 1) {
            log.severe("No matches. Something went wrong.");
            return Collections.emptyList();
        }

        double highest = 0.0;
        int indexOfHighest = 0;
        List<Match> resultMatches = new LinkedList<Match>();
        for (int k = 0; k < ranks; k++) {
            for (Match match : matches) {
                if (match.getScore() > highest) {
                    indexOfHighest = matches.indexOf(match);
                    highest = match.getScore();
                }
            }
            resultMatches.add(matches.get(indexOfHighest));
            matches.remove(indexOfHighest);
            highest = 0.0;
            indexOfHighest = 0;
        }

        return resultMatches;
    }

    /**
     * Executes the svm_rank_classify executable by constructing a temporary
     * .dat file containing the similarity vectors.
     *
     * @param vectors
     * @return
     * @throws Exception
     */
    private List<Match> executeSVMRankClassify(List<SVMRankMatchVector> vectors) throws Exception {

        SVMRankDataFile datFile = new SVMRankDataFile(vectors);
        File outFile;

        try {
            outFile = File.createTempFile("pred", ".tmp");
        } catch (IOException ex) {
            throw ex;
        }

        String cmd = svmRankClassify.getAbsolutePath() + " " + datFile.getName() + " " + matcherConfig.getModelFile().getAbsolutePath() + " " + outFile.getAbsolutePath();
        log.info(cmd);

        try {
            Process process = Runtime.getRuntime().exec(cmd);
            final BufferedReader stdout = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String s;
            while((s = stdout.readLine()) != null) {
                System.out.println(s);
            }
            process.waitFor();
            process.destroy();
        } catch (InterruptedException ex) {
            throw ex;
        } catch (IOException ex) {
            throw ex;
        }

        List<Match> resultMatches = new LinkedList<Match>();

        BufferedReader reader = new BufferedReader(new FileReader(outFile));
        Iterator<SVMRankMatchVector> iterator = vectors.iterator();

        while (reader.ready()) {

            Double score = new Double(reader.readLine().trim());

            if (!iterator.hasNext()) {
                throw new Exception("More predictions than vectors.");
            }

            SVMRankMatchVector vector = iterator.next();

            resultMatches.add(new CoalMatch(vector.getSourceEntity(), vector.getTargetEntity(), score.doubleValue(), SVMRankMatcher.EXACT_MATCH_RELATION));

        }

        return resultMatches;

    }

    /**
     * Executes the svm_rank_learn executable by constructing a temporary .dat
     * file containing the similarity vectors.
     *
     * @param vectors
     * @return
     * @throws Exception
     */
    private File executeSVMRankLearn(Map<Entity, List<SVMRankMatchVector>> vectors) throws Exception {

        SVMRankDataFile datFile = new SVMRankDataFile(vectors);
        File modelOutFile = null;

        try {
            modelOutFile = File.createTempFile("model", ".dat", SVMRankMatcher.DEFAULT_DIR);
        } catch (IOException ex) {
            throw ex;
        }

        double thisRegularisation = SVMRankMatcher.DEFAULT_REGULARISATION_PARAMETER * new Double(vectors.keySet().size());

        try {
            thisRegularisation = new Double(this.matcherConfig.getRegularisationParameter()) * new Double(vectors.keySet().size());
        } catch (Exception e) {
            log.warning("Invalid value \"" + this.matcherConfig.getRegularisationParameter() + "\" for regularisation parameter. Using default value " + (SVMRankMatcher.DEFAULT_REGULARISATION_PARAMETER * new Double(vectors.keySet().size())) + ".");
        }

        String cmd = svmRankLearn.getAbsolutePath() + " -c " + thisRegularisation + " " + datFile.getName() + " " + modelOutFile.getAbsolutePath();

        log.info(cmd);

        try {
            Process process = Runtime.getRuntime().exec(cmd);
            process.waitFor();
            process.destroy();
        } catch (InterruptedException ex) {
            throw ex;
        } catch (IOException ex) {
            throw ex;
        }

        return modelOutFile;

    }
    private SVMRankMatchVector vector;

    /**
     * Match two entities.
     *
     * @param srcEntity
     * @param tgtEntity
     * @param target indicates e.g. the desired rank of the match (the higher
     * the target the better the rank) or "?" if unknown (for classification).
     */
    private void match(Entity srcEntity, Entity tgtEntity, String target) {
        vector = new SVMRankMatchVector(srcEntity, tgtEntity, target, measures.size());

        for (EntitySimilarityMeasure measure : measures) {
            vector.addScore(measures.indexOf(measure), measure.getScore(srcEntity, tgtEntity));
        }
        if (!this.vectors.containsKey(srcEntity)) {
            this.vectors.put(srcEntity, new LinkedList<SVMRankMatchVector>());
        }
        this.vectors.get(srcEntity).add(vector);
    }

    private void match(Entity srcEntity, Entity tgtEntity) {
        match(srcEntity, tgtEntity, SVMRankMatcher.UNKNOWN_MATCH_TARGET);
    }

    @Override
    public boolean isTrained() {
        return this.trained;
    }

    @Override
    public void train(Collection<Alignment> alignments) {
        train(alignments, 50);
    }

    private void train(Collection<Alignment> alignments, int negativeSamples) {

        for (Alignment alignment : alignments) {

            initMeasures();

            Collection<Entity> srcEntities = alignment.getSourceEntities();

            log.info("Training alignment contains matches for " + srcEntities.size() + " source entities.");

            int processedMatches = 0;

            for (Entity srcEntity : srcEntities) {

                processedMatches++;

                log.info("Processing matches of  " + srcEntity.getURI().toString() + " (" + processedMatches + " of " + srcEntities.size() + ").");

                Set<Entity> processed = new HashSet<Entity>();

                for (Match match : alignment.getMatches(srcEntity)) {

                    processed.add(match.getTargetEntity());

                    if (match.getRelation().equals(SVMRankMatcher.EXACT_MATCH_RELATION)) {
                        match(srcEntity, match.getTargetEntity(), SVMRankMatcher.EXACT_MATCH_TARGET);
                    } else if (match.getRelation().equals(SVMRankMatcher.CLOSE_MATCH_RELATION)) {
                        match(srcEntity, match.getTargetEntity(), SVMRankMatcher.CLOSE_MATCH_TARGET);
                    } else {
                        log.severe("Unknown matching relation " + match.getRelation() + ". Skipping match " + srcEntity.getURI() + " " + match.getTargetEntity().getURI());
                        continue;
                    }

                }

                int negatives = 0;

                for (Entity tgtEntity : alignment.getTargetOntology().getEntities()) {
                    if (processed.contains(tgtEntity)) {
                        continue;
                    }
                    if (ignoreInstances) {
                        if (tgtEntity instanceof eu.monnetproject.ontology.Individual) {
                            continue;
                        }
                    }
                    if (negatives < negativeSamples) {
                        match(srcEntity, tgtEntity, SVMRankMatcher.NO_MATCH_TARGET);
                        negatives++;
                    } else {
                        break;
                    }
                }

            }
        }

        File modelFile = null;

        try {
            log.info("Executing SVMrank learn");
            modelFile = executeSVMRankLearn(vectors);
        } catch (Exception ex) {
            log.severe(ex.getMessage());
        }

        matcherConfig.writeConfiguration(modelFile);

        this.trained = true;
        initMeasures();

    }

    @Override
    public List<EntitySimilarityMeasure> getMeasures() {
        return this.measures;
    }
}
