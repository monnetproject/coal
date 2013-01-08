package eu.monnetproject.coal.svmrank;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import eu.monnetproject.util.Logger;

import eu.monnetproject.ontology.Entity;
import eu.monnetproject.util.Logging;

public class SVMRankDataFile {

	private File file;
    private Logger log = Logging.getLogger(this);
	
	private FileWriter writer = null;
	
    public SVMRankDataFile(List<SVMRankMatchVector> vectors) {
		try {
	        file = File.createTempFile("vec", ".dat");
	        writer = new FileWriter(file);
	        addVectors(vectors, 1);
		} catch(IOException ex) {
			log.severe(ex.getMessage());
		}
		
    }

    public SVMRankDataFile(Map<Entity,List<SVMRankMatchVector>> vectors) {
		try {
	        file = File.createTempFile("vec", ".dat");
	        writer = new FileWriter(file);
	        int qid = 1;
	        for (Entity entity : vectors.keySet()) {
	        	addVectors(vectors.get(entity),qid);
	        	qid++;
	        }
		} catch(IOException ex) {
			log.severe(ex.getMessage());
		}
		
    }

    private void addVectors(List<SVMRankMatchVector> vectors, int qid) {

    	for (SVMRankMatchVector vector : vectors) {
    		String line = vector.getTarget()+" qid:"+qid+" ";
    		double[] scores = vector.getScores();
    		for (int i = 0; i < scores.length; i++) {
    			line += (i+1)+":"+scores[i]+" ";
    		}

    		line += "# "+vector.getSourceEntity().getURI().toString()+" "+vector.getTargetEntity().getURI().toString()+"\n";
    		
    		try {
				writer.write(line);
				writer.flush();
			} catch (IOException e) {
				log.severe(e.getMessage());
			}
    	}
		
	}

	public String getName() {
    	return file.getAbsolutePath();
    }
    
}
