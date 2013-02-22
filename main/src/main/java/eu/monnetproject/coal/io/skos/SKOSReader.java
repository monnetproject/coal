/****************************************************************************
 * Copyright (c) 2011, Monnet Project
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the Monnet Project nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE MONNET PROJECT BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 ********************************************************************************/
package eu.monnetproject.coal.io.skos;

import eu.monnetproject.align.Alignment;
import eu.monnetproject.align.AlignmentSerializer;
import eu.monnetproject.align.Match;
import eu.monnetproject.coal.CoalAlignment;
import eu.monnetproject.coal.CoalMatch;
import eu.monnetproject.coal.CoalReader;
import eu.monnetproject.ontology.Entity;
import eu.monnetproject.ontology.OntologyFactory;
import eu.monnetproject.ontology.OntologySerializer;
import java.io.File;
import java.io.InputStream;
import java.io.Reader;
import java.net.URI;
import org.openrdf.model.Statement;
import org.openrdf.model.ValueFactory;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.RepositoryResult;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFParseException;
import org.openrdf.sail.memory.MemoryStore;

/**
 *
 * @author John McCrae
 */
public class SKOSReader implements CoalReader {

    public Alignment readAlignment(File file, AlignmentSerializer alignmentSerializer, OntologySerializer ontoSerializer) {
        Repository repo = new SailRepository(new MemoryStore());
        RepositoryConnection conn = null;
        try {
            repo.initialize();
            conn = repo.getConnection();
            try {
                conn.add(file, "file:test", RDFFormat.RDFXML);
            } catch (RDFParseException x) {
                conn.add(file, "file:test", RDFFormat.TURTLE);
            }
            return doRead(conn,ontoSerializer);
        } catch(Exception x) {
            throw new RuntimeException(x);
        } finally {
            try {
                if (conn != null) {
                    conn.close();
                }
                repo.shutDown();
            } catch (Throwable t) {
            }
        }
    }
    
    public Alignment readAlignment(InputStream stream, AlignmentSerializer alignmentSerializer, OntologySerializer ontoSerializer) {
        Repository repo = new SailRepository(new MemoryStore());
        RepositoryConnection conn = null;
        try {
            repo.initialize();
            conn = repo.getConnection();
            try {
                conn.add(stream, "file:test", RDFFormat.RDFXML);
            } catch (RepositoryException x) {
                conn.add(stream, "file:test", RDFFormat.TURTLE);
            }
            return doRead(conn,ontoSerializer);
        } catch(Exception x) {
            throw new RuntimeException(x);
        } finally {
            try {
                if (conn != null) {
                    conn.close();
                }
                repo.shutDown();
            } catch (Throwable t) {
            }
        }
    }
    
    public Alignment readAlignment(Reader reader, AlignmentSerializer alignmentSerializer, OntologySerializer ontoSerializer) {
        Repository repo = new SailRepository(new MemoryStore());
        RepositoryConnection conn = null;
        try {
            repo.initialize();
            conn = repo.getConnection();
            try {
                conn.add(reader, "file:test", RDFFormat.RDFXML);
            } catch (RepositoryException x) {
                conn.add(reader, "file:test", RDFFormat.TURTLE);
            }
            return doRead(conn,ontoSerializer);
        } catch(Exception x) {
            throw new RuntimeException(x);
        } finally {
            try {
                if (conn != null) {
                    conn.close();
                }
                repo.shutDown();
            } catch (Throwable t) {
            }
        }
    }

    private Alignment doRead(RepositoryConnection conn, OntologySerializer ontoSerializer) throws RepositoryException {
        Alignment alignment = new CoalAlignment(null,null);
        final ValueFactory valueFactory = conn.getValueFactory();
        final OntologyFactory ontoFactory = ontoSerializer.create(URI.create("file:test#irrelevant")).getFactory();
        RepositoryResult<Statement> statements = conn.getStatements(null, valueFactory.createURI("http://www.w3.org/2004/02/skos/core#exactMatch"), null, false);
        while(statements.hasNext()) {
            final Statement next = statements.next();
            final Entity srcEntity = ontoFactory.makeIndividual(URI.create(next.getSubject().stringValue()));
            final Entity trgEntity = ontoFactory.makeIndividual(URI.create(next.getObject().stringValue()));
            final Match match = new CoalMatch(srcEntity, trgEntity, 1.0, "skos:exactMatch");
            alignment.addMatch(match);
        }
        statements = conn.getStatements(null, valueFactory.createURI("http://www.w3.org/2004/02/skos/core#closeMatch"), null, false);
        while(statements.hasNext()) {
            final Statement next = statements.next();
            final Entity srcEntity = ontoFactory.makeIndividual(URI.create(next.getSubject().stringValue()));
            final Entity trgEntity = ontoFactory.makeIndividual(URI.create(next.getObject().stringValue()));
            final Match match = new CoalMatch(srcEntity, trgEntity, 1.0, "skos:closeMatch");
            alignment.addMatch(match);
        }
        return alignment;
    }
    
    
}
