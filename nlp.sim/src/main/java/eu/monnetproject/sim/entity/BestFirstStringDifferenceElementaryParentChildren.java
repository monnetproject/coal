package eu.monnetproject.sim.entity;

import java.util.Collection;
import java.util.Map;
import java.util.Properties;
import eu.monnetproject.util.Logger;

import eu.monnetproject.label.LabelExtractorFactory;
import eu.monnetproject.ontology.Entity;
import eu.monnetproject.sim.EntitySimilarityMeasure;
import eu.monnetproject.sim.StringSimilarityMeasure;
import eu.monnetproject.sim.string.Levenshtein;
import eu.monnetproject.sim.token.TokenBagOfWordsCosine;
import eu.monnetproject.sim.util.Functions;
import eu.monnetproject.sim.util.SimilarityUtils;
import eu.monnetproject.tokenizer.FairlyGoodTokenizer;
import eu.monnetproject.translatorimpl.Translator;
import eu.monnetproject.util.Logging;

/**
 * Similarity measure based on best first string difference between elementary parent-children in the presentations of the entities.
 * 
 * @author Dennis Spohr
 *
 */
public class BestFirstStringDifferenceElementaryParentChildren implements EntitySimilarityMeasure {
	
    private Logger log = Logging.getLogger(this);
	private final String name = this.getClass().getName();
	private EntitySimilarityMeasure subMeasure = null;
	private LabelExtractorFactory lef;
	private StringSimilarityMeasure stringMeasure;
	private Translator translator;

	
	public BestFirstStringDifferenceElementaryParentChildren(LabelExtractorFactory lef) {
            this.lef = lef;
            this.stringMeasure = new Levenshtein();
            this.translator = new Translator();
            this.subMeasure = new AverageAverageLevenshtein(lef,translator,stringMeasure);

	}
	
	public void configure(Properties properties) {
		subMeasure.configure(properties);
    }
    
	@Override
	public double getScore(Entity srcEntity, Entity tgtEntity) {
		
		if (subMeasure == null) {
			log.severe("Couldn't bind sub-measure for calculating String similarity. Returning score 0.");
			return 0.;
		}
		
		Collection<Collection<Entity>> srcPresentations = SimilarityUtils.getPresentations(srcEntity,true);
		Collection<Collection<Entity>> tgtPresentations = SimilarityUtils.getPresentations(tgtEntity,true);
		
		if (srcPresentations.size() < 1 && tgtPresentations.size() < 1) {
			return 1.;
		}

		if (srcPresentations.size() < 1 || tgtPresentations.size() < 1)
			return 0.;
		
		int index = 0;
		
		double[] presentationSims = new double[srcPresentations.size()+tgtPresentations.size()];
		
		for (Collection<Entity> srcPresentation : srcPresentations) {

			double presentationSim = 0;
            Collection<Entity> bestPresentation = null;
            
			for (Collection<Entity> tgtPresentation : tgtPresentations) {
				
				double[][] similarityMatrix = new double[srcPresentation.size()][tgtPresentation.size()];
			
				int i = 0;
				
				for (Entity srcItem : srcPresentation) {
					
					int j = 0;

					for (Entity tgtItem : tgtPresentation) {

						similarityMatrix[i][j] = subMeasure.getScore(srcItem, tgtItem);

                        j++;

					}
					
					i++;
					
				}
				
				double score;

                score = Functions.bestFirst(similarityMatrix);

                if (score > presentationSim) {
                        presentationSim = score;
                        bestPresentation = tgtPresentation;
                }
			}
			
			if (bestPresentation != null) {
                presentationSims[index] = presentationSim;
                index++;
			}
		}
		
		return Functions.max(presentationSims);

	}

	@Override
	public String getName() {
		return this.name;
	}

}
