package eu.monnetproject.sim.entity;

import java.util.Collection;
import java.util.Properties;
import eu.monnetproject.util.Logger;

import eu.monnetproject.ontology.Entity;
import eu.monnetproject.sim.EntitySimilarityMeasure;
import eu.monnetproject.sim.util.Functions;
import eu.monnetproject.sim.util.SimilarityUtils;
import eu.monnetproject.util.Logging;

/**
 * Similarity measure based on difference between minimum number of direct summation items in the calculations of the entities.
 * 
 * @author Dennis Spohr
 *
 */
public class MinimumDirectSummationItems implements EntitySimilarityMeasure {
	
    private Logger log = Logging.getLogger(this);
	private final String name = this.getClass().getName();
	
	public MinimumDirectSummationItems() {
	}
	
    public void configure(Properties properties) {
    }
    
	@Override
	public double getScore(Entity srcEntity, Entity tgtEntity) {
		
		Collection<Collection<Entity>> srcCalculations = SimilarityUtils.getCalculations(srcEntity,false);
		Collection<Collection<Entity>> tgtCalculations = SimilarityUtils.getCalculations(tgtEntity,false);
		
		if (srcCalculations.size() < 1 && tgtCalculations.size() < 1) {
			return 1.;
		}

		if (srcCalculations.size() < 1 || tgtCalculations.size() < 1)
			return 0.;
		
		double[] srcItems = new double[srcCalculations.size()];
		double[] tgtItems = new double[tgtCalculations.size()];
		
		int index = 0;
		
		for (Collection<Entity> srcCalculation : srcCalculations) {
			srcItems[index++] = srcCalculation.size();
		}

		index = 0;
		
		for (Collection<Entity> tgtCalculation : tgtCalculations) {
			tgtItems[index++] = tgtCalculation.size();
		}
		
		double srcMin = Functions.min(srcItems);
		
		double tgtMin = Functions.min(tgtItems);
		
		return 1 - (Math.abs(srcMin - tgtMin) / Math.max(srcMin,tgtMin));

	}

	@Override
	public String getName() {
		return this.name;
	}

}
