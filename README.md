Cross-lingual ontology alignment (COAL)
=======================================

This tool is useful for aligning ontologies in two different languages

The structure of the code is as follows

* main: The aligner
* nlp.core: Basic NLP methods (tokenization, etc.)
* nlp.sim: Cross-lingual similarity metrics
* test: Integration tests
* translation: Interface to the Monnet Translate system
* web: Web interface for COAL

Installing and Running
----------------------

COAL can be installed using Maven simply as follows

  mvn install

Running the aligner can be done with `./main/align` script as follows

  ./align Ontology1.owl Ontology2.owl Alignment.rdf

Example ontologies can be found at [main/load/SourceOntology.owl](main/load/SourceOntology.owl) and 
at [main/load/TargetOntology.owl](main/load/TargetOntology.owl). 

In addition a you can require COAL to provide more matches as follows:

  ./align Ontology1.owl Ontology2.owl Alignment.rdf 5

COAL can be trained as follows

  ./train Ontology1.owl Ontology2.owl Alignments.rdf modelOutFile

COAL is built on [SVM-Rank](http://www.cs.cornell.edu/people/tj/svm_light/svm_rank.html) and so will
only work on Linux and Windows


Web server
----------

The web server can be installed by compiling and uploading the WAR file found under `web/target` to a
Java EE container.
