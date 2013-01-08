package eu.monnetproject.coal.io.edoal;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import java.io.File;
import java.net.URI;
import eu.monnetproject.util.Logger;

import eu.monnetproject.align.Alignment;
import eu.monnetproject.align.AlignmentSerializer;
import eu.monnetproject.coal.CoalMatch;
import eu.monnetproject.coal.CoalReader;
import eu.monnetproject.data.FileDataSource;
import eu.monnetproject.ontology.Entity;
import eu.monnetproject.ontology.Individual;
import eu.monnetproject.ontology.Ontology;
import eu.monnetproject.ontology.OntologySerializer;
import eu.monnetproject.util.Logging;
import java.io.Reader;

public class EDOALReader implements CoalReader {

    private Logger log = Logging.getLogger(this);
	private BufferedReader reader;

	public EDOALReader() {
	}

	@Override
	public Alignment readAlignment(File file, AlignmentSerializer alignmentSerializer,OntologySerializer ontoSerializer) {
		InputStream stream = null;
		try {
			stream = new FileInputStream(file);
		} catch (FileNotFoundException e) {
			log.severe("Can't read file. "+e.getMessage());
		}
		
		return readAlignment(stream, alignmentSerializer, ontoSerializer);
	}
	
	@Override
	public Alignment readAlignment(InputStream stream, AlignmentSerializer alignmentSerializer,OntologySerializer ontoSerializer) {
		
		
		try {
			reader = new BufferedReader(new InputStreamReader(stream));
		} catch (Exception e) {
			log.severe("Can't read input stream. "+e.getMessage());
		}
                return readAlignment(reader, alignmentSerializer, ontoSerializer);
        }
        
        public Alignment readAlignment(Reader reader2, AlignmentSerializer alignmentSerializer, OntologySerializer ontoSerializer) {
		Alignment alignment = alignmentSerializer.createAlignment();
		this.reader = new BufferedReader(reader2);
		boolean onto1 = false;
		boolean onto2 = false;
		boolean entity1 = false;
		boolean entity2 = false;
		
		Entity srcEntity = null;
		Entity tgtEntity = null;
		double score = 0.0;
		String relation = "";
		boolean skip = false;
		
		String srcLocation = "";
		String tgtLocation = "";
		
		log.info("Starting");
		
		try {
			while (reader.ready()) {
				String line = reader.readLine().trim();
				String tmpString;
				
				if (line.startsWith("</Cell")) {
					if (!skip)
						alignment.addMatch(new CoalMatch(srcEntity, tgtEntity, score, relation));
					srcEntity = null;
					score = 0.0;
					relation = "";
					skip = false;
				}
				
				if (skip)
					continue;

				if (line.startsWith("<onto1"))
					onto1 = true;

				if (line.startsWith("<onto2"))
					onto2 = true;
				
				if (line.startsWith("<entity1"))
					entity1 = true;
				else entity1 = false;
				
				if (line.startsWith("<measure")) {
					tmpString = line.replaceAll("^.*?>", "");
					score = new Double(tmpString.replaceAll("<.*$", ""));
				}
				
				if (line.startsWith("<relation")) {
					tmpString = line.replaceAll("^.*?>", "");
					relation = tmpString.replaceAll("<.*$", "");
				}
				
				if (line.startsWith("<entity2"))
					entity2 = true;
				else entity2 = false;
					
				if (line.startsWith("</Ontology")) {
					if (onto1) {
						alignment.setSourceOntology(ontoSerializer.read(new FileDataSource(srcLocation)));
						log.info("Source ontology is "+alignment.getSourceOntology().getURI());
						onto1 = false;
					} else 	if (onto2) {
						alignment.setTargetOntology(ontoSerializer.read(new FileDataSource(tgtLocation)));
						log.info("Target ontology is "+alignment.getTargetOntology().getURI());
						onto2 = false;
					}

				}
				
				if (line.startsWith("<Ontology")) {
					
					tmpString = line.replaceAll("^.*?('|\")", "");
					String onto = tmpString.replaceAll("('|\").*$", "");
					
					if (onto1)
						srcLocation = onto;
					else if (onto2)
						tgtLocation = onto;
					
				}
				
				if (line.startsWith("<location>")) {
					
					tmpString = line.replaceAll("^.*?>", "");
					String onto = URI.create(tmpString.replaceAll("<.*$", "")).toURL().getFile();
					
					if (onto1)
						srcLocation = onto;
					else if (onto2)
						tgtLocation = onto;
						
				}

				if (entity1 || entity2) {
				
					tmpString = line.replaceAll("^.*?('|\")", "");
					String ent = tmpString.replaceAll("('|\").*$", "");
					
					Entity tmpEntity = null;
					Ontology tmpOnto;
					
					if (entity1)
						tmpOnto = alignment.getSourceOntology();
					else
						tmpOnto = alignment.getTargetOntology();
					
					for (Entity entity : tmpOnto.getEntities(URI.create(ent))) {
						if (!(entity instanceof Individual)) {
							tmpEntity = entity;
							break;
						} else if (tmpEntity == null) {
							tmpEntity = entity;
						}
					}
					
					if (tmpEntity == null) {
						log.warning("Entity "+ent+" does not exist in source nor target ontology. Skipping this match.");
						skip = true;
						continue;
					}

					if (entity1) {
						
						srcEntity = tmpEntity;
						entity1 = false;
						
					} else if (entity2) {

						tgtEntity = tmpEntity;
						entity2 = false;
					}
				}
				
			}
		} catch (IOException e) {
			log.severe(e.getMessage());
		}
		
		return alignment;
		
	}

}
