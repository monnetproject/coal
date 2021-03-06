package eu.monnetproject.sim.entity;

import java.net.URI;
import java.util.Collection;
import java.util.Properties;
import eu.monnetproject.util.Logger;

import eu.monnetproject.ontology.Entity;
import eu.monnetproject.sim.EntitySimilarityMeasure;
import eu.monnetproject.sim.util.Functions;
import eu.monnetproject.sim.util.SimilarityUtils;
import eu.monnetproject.util.Logging;

/**
 * Similarity measure based on difference between average ratio of credit/debit items in direct summation items in the calculations of the entities.
 * 
 * @author Dennis Spohr
 *
 */
public class AverageRatioCreditDebitDirectSummationItems implements EntitySimilarityMeasure {
	
    private Logger log = Logging.getLogger(this);
	private final String name = this.getClass().getName();
	private eu.monnetproject.ontology.Class credit = null;
	private URI creditURI = URI.create("http://www.xbrl.org/2003/instance/credit");
	private eu.monnetproject.ontology.Class debit = null;
	private URI debitURI = URI.create("http://www.xbrl.org/2003/instance/debit");
	
	public AverageRatioCreditDebitDirectSummationItems() {
	}
	
    public void configure(Properties properties) {
    }
    
	@Override
	public double getScore(Entity srcEntity, Entity tgtEntity) {
		
		if (debit == null) {
			for (Entity cls : srcEntity.getOntology().getEntities(debitURI)) {
				if (cls instanceof eu.monnetproject.ontology.Class) {
					debit = (eu.monnetproject.ontology.Class)cls;
					break;
				}
			}
		}
		
		if (credit == null) {
			for (Entity cls : srcEntity.getOntology().getEntities(creditURI)) {
				if (cls instanceof eu.monnetproject.ontology.Class) {
					credit = (eu.monnetproject.ontology.Class)cls;
					break;
				}
			}
		}
		
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
			srcItems[index++] = SimilarityUtils.getRatio(srcCalculation,credit,debit);
		}

		index = 0;
		
		for (Collection<Entity> tgtCalculation : tgtCalculations) {
			tgtItems[index++] = SimilarityUtils.getRatio(tgtCalculation,credit,debit);
		}
		
		double srcAvg = Functions.mean(srcItems);
		
		double tgtAvg = Functions.mean(tgtItems);
		
		if (srcAvg == 0 && tgtAvg == 0)
			return 1.;
		
		return 1 - (Math.abs(srcAvg - tgtAvg) / Math.max(srcAvg,tgtAvg));

	}
	

	@Override
	public String getName() {
		return this.name;
	}

}
