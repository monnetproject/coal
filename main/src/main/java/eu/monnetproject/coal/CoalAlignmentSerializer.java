package eu.monnetproject.coal;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import eu.monnetproject.align.Alignment;
import eu.monnetproject.align.AlignmentSerializer;
import eu.monnetproject.coal.io.edoal.EDOALReader;
import eu.monnetproject.coal.io.skos.SKOSReader;
import eu.monnetproject.coal.io.skos.SKOSWriter;
import eu.monnetproject.config.Configurator;
import eu.monnetproject.ontology.OntologySerializer;
import java.io.Reader;
import java.io.Writer;
import java.util.logging.Logger;

public class CoalAlignmentSerializer implements AlignmentSerializer {

    private CoalWriter writer;
    private CoalReader reader;
        private Logger log = Logger.getLogger(CoalAlignmentSerializer.class.getName());
    private final OntologySerializer ontoSerializer;
    private boolean xml = Boolean.parseBoolean(Configurator.getConfig("eu.monnetproject.coal").getProperty("xml", "false"));
    
    public CoalAlignmentSerializer(OntologySerializer serializer) {
        this.ontoSerializer = serializer;
    }

    public void setXml(boolean xml) {
        this.xml = xml;
    }
    
    @Override
    public void writeAlignment(Alignment alignment, File file) throws IOException {
        writer = new SKOSWriter(xml);
        writer.write(alignment, file);
    }

    @Override
    public void writeAlignment(Alignment alignment, OutputStream stream) throws IOException {
        writer = new SKOSWriter(xml);
        writer.write(alignment, stream);
    }
    
    @Override
    public void writeAlignment(Alignment alignment,Writer writer) throws IOException {
        this.writer = new SKOSWriter(xml);
        this.writer.write(alignment, writer);
    }

    public int getProgress() {
        if (writer == null) {
            return 0;
        }
        return writer.getProgress();
    }

    @Override
    public Alignment createAlignment() {
        return new CoalAlignment();
    }

    @Override
    public Alignment createAlignment(int k) {
        return new CoalAlignment(k);
    }

    @Override
    public Alignment readAlignment(File file) {
        try {
            reader = new EDOALReader();
            return reader.readAlignment(file, this, this.ontoSerializer);
        } catch (Exception x) {
            reader = new SKOSReader();
            return reader.readAlignment(file, this, ontoSerializer);
        }

    }

    @Override
    public Alignment readAlignment(InputStream stream) {
        try {
            reader = new EDOALReader();
            return reader.readAlignment(stream, this, this.ontoSerializer);
        } catch (Exception x) {
            reader = new SKOSReader();
            return reader.readAlignment(stream, this, ontoSerializer);
        }

    }

    public Alignment readAlignment(Reader reader) {
        try {
            this.reader = new EDOALReader();
            return this.reader.readAlignment(reader, this, this.ontoSerializer);
        } catch (Exception x) {
            this.reader = new SKOSReader();
            return this.reader.readAlignment(reader, this, ontoSerializer);
        }
    }
    
    
}
