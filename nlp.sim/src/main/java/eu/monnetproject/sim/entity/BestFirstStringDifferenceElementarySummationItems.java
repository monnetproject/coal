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
 * Similarity measure based on best first string difference between elementary summation items in the calculations of the entities.
 * 
 * @author Dennis Spohr
 *
 */
public class BestFirstStringDifferenceElementarySummationItems implements EntitySimilarityMeasure {
	
    private Logger log = Logging.getLogger(this);
	private final String name = this.getClass().getName();
	private EntitySimilarityMeasure subMeasure = null;
	private LabelExtractorFactory lef;
	private StringSimilarityMeasure stringMeasure;
	private Translator translator;

	
	public BestFirstStringDifferenceElementarySummationItems(LabelExtractorFactory lef) {
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
		
		Collection<Collection<Entity>> srcCalculations = SimilarityUtils.getCalculations(srcEntity,true);
		Collection<Collection<Entity>> tgtCalculations = SimilarityUtils.getCalculations(tgtEntity,true);
		
		if (srcCalculations.size() < 1 && tgtCalculations.size() < 1) {
			return 1.;
		}

		if (srcCalculations.size() < 1 || tgtCalculations.size() < 1)
			return 0.;
		
		int index = 0;
		
		double[] calculationSims = new double[srcCalculations.size()+tgtCalculations.size()];
		
		for (Collection<Entity> srcCalculation : srcCalculations) {

			double calculationSim = 0;
            Collection<Entity> bestCalculation = null;
            
			for (Collection<Entity> tgtCalculation : tgtCalculations) {
				
				double[][] similarityMatrix = new double[srcCalculation.size()][tgtCalculation.size()];
			
				int i = 0;
				
				for (Entity srcItem : srcCalculation) {
					
					int j = 0;

					for (Entity tgtItem : tgtCalculation) {

						similarityMatrix[i][j] = subMeasure.getScore(srcItem, tgtItem);

                        j++;

					}
					
					i++;
					
				}
				
				double score;

                score = Functions.bestFirst(similarityMatrix);

                if (score > calculationSim) {
                        calculationSim = score;
                        bestCalculation = tgtCalculation;
                }
			}
			
			if (bestCalculation != null) {
                calculationSims[index] = calculationSim;
                index++;
			}
		}
		
		return Functions.max(calculationSims);

	}

	@Override
	public String getName() {
		return this.name;
	}

}
