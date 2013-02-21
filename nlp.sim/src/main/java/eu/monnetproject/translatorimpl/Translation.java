package eu.monnetproject.translatorimpl;

import eu.monnetproject.lang.Language;

import java.util.Collection;

/**
 * Represents the result of a translation service
 *
 * @author John McCrae
 */
public class Translation {
    private final String srcLabel, trgLabel;
    private final Language srcLang, trgLang;

    public Translation(String srcLabel, String trgLabel, Language srcLang, Language trgLang) {
        this.srcLabel = srcLabel;
        this.trgLabel = trgLabel;
        this.srcLang = srcLang;
        this.trgLang = trgLang;
    }
    
    /**
     * Get the language that this result is in.
     * @return The language
     */
    public Language getLanguage() {
        return trgLang;
    }

    /**
     * Get the source label used to produce this translation
     * @return The source label, or null if no source label was used
     */
    public String getSourceLabel() {
        return srcLabel;
    }
    /**
     * Get the language of the source label language
     * @return The source label langauge, or null if no source label exists.
     */
    public Language getSourceLanguage() {
        return srcLang;
    }

	/**
 	 * Get the translated label
 	 */
    public String getLabel() {
        return trgLabel;
    }
}
