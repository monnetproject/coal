package eu.monnetproject.coal.io.skos;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.List;

import eu.monnetproject.align.Alignment;
import eu.monnetproject.align.Match;
import eu.monnetproject.coal.CoalWriter;
import eu.monnetproject.ontology.Entity;
import java.io.Writer;
import java.util.logging.Logger;

/**
 * 
 * @author Dennis Spohr
 *
 */
public class SKOSWriter implements CoalWriter {

    private final Logger log = Logger.getLogger(SKOSWriter.class.getName());
    private Writer fileWriter;
    private final boolean xml;
    private volatile int progress = 0;

    public SKOSWriter(File file) throws IOException {
        this.fileWriter = new FileWriter(file);
        this.xml = false;
    }

    public SKOSWriter() {
        this.xml = false;
    }

    public SKOSWriter(boolean xml) {
        this.xml = xml;
    }

    @Override
    public void write(Alignment alignment, OutputStream stream)
            throws IOException {

        this.fileWriter = new OutputStreamWriter(stream);

        writeHeader(alignment);

        for (Entity sourceEntity : alignment.getSourceEntities()) {

            writeMatches(sourceEntity, alignment.getMatches(sourceEntity));

            progress++;

        }

        closeRoot();
    }

    public void write(Alignment alignment, File file) throws IOException {

        log.info("Writing alignment to " + file.getAbsolutePath());

        write(alignment, new FileOutputStream(file));

    }

    public void write(Alignment alignment, Writer writer) throws IOException {
        this.fileWriter = writer;

        writeHeader(alignment);

        for (Entity sourceEntity : alignment.getSourceEntities()) {

            writeMatches(sourceEntity, alignment.getMatches(sourceEntity));

            progress++;

        }

        closeRoot();

    }

    private void closeRoot() throws IOException {
        if (xml) {
            fileWriter.write("</rdf:RDF>");
        }
        fileWriter.flush();
    }

    private void writeMatches(Entity srcEntity, List<Match> matches) throws IOException {

        if (matches.size() == 0) {
            return;
        }

        if (xml) {
            fileWriter.write("\t<rdf:Description rdf:about=\"" + srcEntity.getURI().toString() + "\">\n");
        }

        for (int i = 0; i < matches.size(); i++) {

            Match match = matches.get(i);
            if (xml) {
                if (match.getRelation().contains("exact") || match.getRelation().equals("=")) {
                    fileWriter.write("\t\t<skos:exactMatch rdf:resource=\"" + match.getTargetEntity().getURI() + "\"/>\n");
                } else {
                    fileWriter.write("\t\t<skos:closeMatch rdf:resource=\"" + match.getTargetEntity().getURI() + "\"/>\n");
                }
            } else {
                if (match.getRelation().contains("exact") || match.getRelation().equals("=")) {
                    fileWriter.write("<" + srcEntity.getURI().toString() + "> skos:exactMatch <" + match.getTargetEntity().getURI() + "> .\n\n");
                } else {
                    fileWriter.write("<" + srcEntity.getURI().toString() + "> skos:closeMatch <" + match.getTargetEntity().getURI() + "> .\n\n");
                }
            }


        }

        if (xml) {
            fileWriter.write("\t</rdf:Description>");
        }
    }

    private void writeHeader(Alignment alignment) throws IOException {
        if (xml) {
            fileWriter.write("<rdf:RDF xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\" xmlns:skos=\"http://www.w3.org/2004/02/skos/core#\">\n");
        } else {
            fileWriter.write("@prefix skos:<http://www.w3.org/2004/02/skos/core#> .\n\n");
        }
    }

    public int getProgress() {
        return this.progress;
    }
}
