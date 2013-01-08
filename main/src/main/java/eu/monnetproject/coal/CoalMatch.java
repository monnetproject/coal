package eu.monnetproject.coal;

import java.util.Collection;

import eu.monnetproject.align.Matcher;
import eu.monnetproject.align.Match;
import eu.monnetproject.ontology.Entity;
import eu.monnetproject.sim.EntitySimilarityMeasure;

public class CoalMatch implements Match {

	private Entity srcEntity;
	private Entity tgtEntity;
	private double score;
	private String relation;
	private Matcher matcher;

	public CoalMatch(Entity srcEntity, Entity tgtEntity, double score, String relation) {
		this.srcEntity = srcEntity;
		this.tgtEntity = tgtEntity;
		this.score = score;
		this.relation = relation;
	}
	
	@Override
	public String getRelation() {
		return this.relation;
	}

	@Override
	public double getScore() {
		return this.score;
	}

	@Override
	public Entity getSourceEntity() {
		return this.srcEntity;
	}

	@Override
	public Entity getTargetEntity() {
		return this.tgtEntity;
	}

	@Override
	public Collection<EntitySimilarityMeasure> getMeasures() {
		return this.matcher.getMeasures();
	}

}
