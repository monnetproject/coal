package eu.monnetproject.coal.svmrank;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import eu.monnetproject.lang.Language;
import java.util.logging.Logger;

public class SVMRankMatcherConfiguration {

        private Logger log = Logger.getLogger(SVMRankMatcherConfiguration.class.getName());
    private File configFile;
    private List<Language> languages;
    private Properties properties;
    private static final String DEFAULT_MEASURES = "eu.monnetproject.sim.entity.AverageAverageLevenshtein";

    public Properties getConfig() {
        Properties props = new Properties();
        if (configFile.exists()) {
            try {
                props.load(new FileInputStream(configFile));
            } catch (IOException x) {
                throw new RuntimeException(x);
            }
        } else {
            log.warning("Config file was not found");
        }
        return props;
    }

    public List<Language> getLanguages() {
        return languages;
    }

    public void setLanguages(List<Language> languages) {
        this.languages = languages;
    }

    public void writeConfiguration(File modelFile) {

        FileWriter writer = null;
        configFile = new File(modelFile.getAbsolutePath() + ".cfg");

        try {
            writer = new FileWriter(configFile);
            writer.write("model=" + modelFile.getPath() + "\n");
            writer.write("languages=" + properties.getProperty("languages", "") + "\n");
            writer.write("include_puns=" + properties.getProperty("include_puns", "true") + "\n");
            writer.write("ignore_instances=" + properties.getProperty("ignore_instances", "true") + "\n");

            List<String> tmpMeasures = getMeasureNames();

            writer.write("measures=" + tmpMeasures.get(0));

            for (int i = 1; i < tmpMeasures.size(); i++) {
                writer.write(",\\\n" + tmpMeasures.get(i));
            }

            writer.flush();
        } catch (IOException e) {
            log.severe(e.getMessage());
        }

        this.properties = getConfig();

    }
    
    private File modelFile;

    public SVMRankMatcherConfiguration(File file) {
        this.configFile = file;
        this.properties = getConfig();
        this.modelFile = null;
    }
    
    public SVMRankMatcherConfiguration(File file, File modelFile) {
        this.configFile = file;
        this.properties = getConfig();
        this.modelFile = modelFile;
    }

    public SVMRankMatcherConfiguration() {
    }

    public Properties getMeasureConfiguration() {
        Properties properties = new Properties();

        properties.put("languages", this.properties.getProperty("languages", ""));
        properties.put("ignore_instances", this.properties.getProperty("ignore_instances", "true"));
        properties.put("include_puns", this.properties.getProperty("include_puns", "true"));

        return properties;
    }

    public boolean hasModel() {
        return modelFile != null || this.properties.containsKey("model");
    }

    public boolean ignoreInstances() {
        return new Boolean(this.properties.getProperty("ignore_instances", "true"));
    }

    public File getModelFile() {
        return modelFile != null ? modelFile : new File(this.properties.get("model").toString());
    }

    public String getRegularisationParameter() {
        return this.properties.getProperty("regularisation_parameter", "0.01");
    }

    public List<String> getMeasureNames() {
        List<String> rv = new LinkedList<String>();
        if (properties.getProperty("measures") != null) {
            for (String feature : properties.getProperty("measures").split(",")) {

                rv.add(feature);

            }
        }

        if (rv.size() < 1) {
            log.warning("No features indicated in " + configFile.getAbsolutePath() + ". Will use default measures: " + SVMRankMatcherConfiguration.DEFAULT_MEASURES);
        }

        return rv;
    }
}
