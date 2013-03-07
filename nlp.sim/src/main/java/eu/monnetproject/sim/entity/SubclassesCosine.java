package eu.monnetproject.sim.entity;

import java.util.Collection;
import java.util.List;
import java.util.Properties;
import java.util.Vector;
import eu.monnetproject.util.Logger;

import eu.monnetproject.ontology.Entity;
import eu.monnetproject.sim.EntitySimilarityMeasure;
import eu.monnetproject.sim.util.Functions;
import eu.monnetproject.util.Logging;

/**
 * Cosine similarity of subclass vectors.
 *
 * @author Dennis Spohr, John McCrae
 *
 */
public class SubclassesCosine implements EntitySimilarityMeasure {

    private Logger log = Logging.getLogger(this);
    private final String name = this.getClass().getName();

    public SubclassesCosine() {
    }

    public void configure(Properties properties) {
    }

    @Override
    public double getScore(Entity srcEntity, Entity tgtEntity) {

        if (!(srcEntity instanceof eu.monnetproject.ontology.Class && tgtEntity instanceof eu.monnetproject.ontology.Class)) {
            return 0.;
        }

        Collection<eu.monnetproject.ontology.Class> srcSubClasses = ((eu.monnetproject.ontology.Class) srcEntity).getSubClassOf();
        Collection<eu.monnetproject.ontology.Class> tgtSubClasses = ((eu.monnetproject.ontology.Class) tgtEntity).getSubClassOf();

        List<String> bag = new Vector<String>();

        int[] srcVector = new int[srcSubClasses.size() + tgtSubClasses.size()];
        int[] tgtVector = new int[srcVector.length];

        for (eu.monnetproject.ontology.Class srcCls : srcSubClasses) {
            if (bag.contains(srcCls.getURI().toString())) {
                srcVector[bag.indexOf(srcCls.getURI().toString())]++;
            } else {
                bag.add(srcCls.getURI().toString());
                srcVector[bag.indexOf(srcCls.getURI().toString())] = 1;
            }
        }

        for (eu.monnetproject.ontology.Class tgtCls : tgtSubClasses) {
            if (bag.contains(tgtCls.getURI().toString())) {
                tgtVector[bag.indexOf(tgtCls.getURI().toString())]++;
            } else {
                bag.add(tgtCls.getURI().toString());
                tgtVector[bag.indexOf(tgtCls.getURI().toString())] = 1;
            }
        }

        return Functions.cosine(srcVector, tgtVector);

    }

    @Override
    public String getName() {
        return this.name;
    }
}
