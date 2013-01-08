package eu.monnetproject.coal;

import java.io.File;
import java.io.InputStream;

import eu.monnetproject.align.Alignment;
import eu.monnetproject.align.AlignmentSerializer;
import eu.monnetproject.ontology.OntologySerializer;
import java.io.Reader;

public interface CoalReader {

	public Alignment readAlignment(File file, AlignmentSerializer alignmentSerializer, OntologySerializer ontoSerializer);
	
	public Alignment readAlignment(InputStream stream, AlignmentSerializer alignmentSerializer, OntologySerializer ontoSerializer);
	
        public Alignment readAlignment(Reader reader, AlignmentSerializer alignmentSerializer, OntologySerializer ontoSerializer);
	
}
