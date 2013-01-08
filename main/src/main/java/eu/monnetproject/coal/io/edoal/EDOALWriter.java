package eu.monnetproject.coal.io.edoal;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.List;

import eu.monnetproject.align.Alignment;
import eu.monnetproject.align.Match;
import eu.monnetproject.coal.CoalWriter;
import eu.monnetproject.ontology.Entity;

/**
 * 
 * @author Dennis Spohr
 *
 */
public class EDOALWriter implements CoalWriter {

        private java.util.logging.Logger log = java.util.logging.Logger.getLogger(EDOALWriter.class.getName());
    private Writer fileWriter;
    private volatile int progress = 0;

    public EDOALWriter(File file) throws IOException {
        this.fileWriter = new FileWriter(file);
    }

    public EDOALWriter() {
    }

    public void write(Alignment alignment, File file) throws IOException {
        log.info("Writing alignment to " + file.getAbsolutePath());

        write(alignment, new FileOutputStream(file));
    }

    public void write(Alignment alignment, OutputStream stream) throws IOException {

        this.fileWriter = new OutputStreamWriter(stream);

        writeHeader(alignment);

        for (Entity sourceEntity : alignment.getSourceEntities()) {

            //log.info("Processing "+sourceEntity.getURI().toString());

            writeMatches(sourceEntity, alignment.getMatches(sourceEntity));

            progress++;

        }

        closeRoot();

    }

    public void write(Alignment alignment, Writer writer) throws IOException {
        this.fileWriter = writer;

        writeHeader(alignment);

        for (Entity sourceEntity : alignment.getSourceEntities()) {

            //log.info("Processing "+sourceEntity.getURI().toString());

            writeMatches(sourceEntity, alignment.getMatches(sourceEntity));

            progress++;

        }

        closeRoot();
    }

    private void closeRoot() throws IOException {
        fileWriter.write(" </Alignment>\n");
        fileWriter.write("</rdf:RDF>\n");
        fileWriter.flush();
    }

    private void writeMatches(Entity srcEntity, List<Match> matches) throws IOException {
        fileWriter.write("  <map>\n");
        for (Match match : matches) {
            fileWriter.write("   <Cell>\n");
            fileWriter.write("    <entity1 rdf:resource='" + srcEntity.getURI().toString() + "' />\n");
            fileWriter.write("    <entity2 rdf:resource='" + match.getTargetEntity().getURI().toString() + "' />\n");
            fileWriter.write("    <relation>" + match.getRelation() + "</relation>\n");
            fileWriter.write("    <measure rdf:datatype='http://www.w3.org/2001/XMLSchema#float'>" + match.getScore() + "</measure>\n");
            fileWriter.write("   </Cell>\n");
            fileWriter.flush();
        }
        fileWriter.write("  </map>\n");
    }

    private void writeHeader(Alignment alignment) throws IOException {
        // TODO Auto-generated method stub
        fileWriter.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        fileWriter.write("<rdf:RDF xmlns='http://knowledgeweb.semanticweb.org/heterogeneity/alignment#'\n"
                + "         xmlns:rdf='http://www.w3.org/1999/02/22-rdf-syntax-ns#'\n"
                + "         xmlns:xsd='http://www.w3.org/2001/XMLSchema#'\n"
                + "         xmlns:skos='http://www.w3.org/2004/02/skos/core#'\n"
                + "         xmlns:align='http://knowledgeweb.semanticweb.org/heterogeneity/alignment#'>\n");
        fileWriter.write(" <Alignment>\n");
        fileWriter.write("  <onto1>\n");
        fileWriter.write("   <Ontology rdf:about=\"" + alignment.getSourceOntology().getURI() + "\" >\n");
        fileWriter.write("    <location>" + alignment.getSourceOntology().getURI() + "</location>\n");
        fileWriter.write("   </Ontology>\n");
        fileWriter.write("  </onto1>\n");
        fileWriter.write("  <onto2>\n");
        fileWriter.write("   <Ontology rdf:about=\"" + alignment.getTargetOntology().getURI() + "\" >\n");
        fileWriter.write("    <location>" + alignment.getTargetOntology().getURI() + "</location>\n");
        fileWriter.write("   </Ontology>\n");
        fileWriter.write("  </onto2>\n");
        fileWriter.flush();
    }

    public int getProgress() {
        return this.progress;
    }
}
