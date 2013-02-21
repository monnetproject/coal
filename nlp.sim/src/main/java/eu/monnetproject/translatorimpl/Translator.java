package eu.monnetproject.translatorimpl;

import eu.monnetproject.lang.Language;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Scanner;

/**
 * A process capable of translating ontology labels
 *
 * @author John McCrae
 */
public class Translator implements AbstractTranslator<Translation> {

    public Translator() {
    }

    @Override
    public Collection<Translation> translate(String label, Language srcLang, Language trgLang) {
        try {
            if ((srcLang.equals(Language.ENGLISH) || srcLang.equals(Language.GERMAN) || srcLang.equals(Language.DUTCH) || srcLang.equals(Language.SPANISH))
                    && (trgLang.equals(Language.ENGLISH) || trgLang.equals(Language.GERMAN) || trgLang.equals(Language.DUTCH) || trgLang.equals(Language.SPANISH))) {
                final URL url = new URL("http://monnet01.sindice.net:8080/quicktranslate?src=" + srcLang + "&trg=" + trgLang + "&label=" + URLEncoder.encode(label, "UTF-8"));
                final InputStream urlIn = url.openConnection().getInputStream();
                final Scanner in = new Scanner(urlIn);
                final LinkedList<Translation> translations = new LinkedList<Translation>();
                while (in.hasNextLine()) {
                    translations.add(new Translation(label, in.nextLine().trim(), srcLang, trgLang));
                }
                urlIn.close();
                return translations;
            } else {
                return Collections.EMPTY_LIST;
            }
        } catch (IOException x) {
            System.err.println("Translation failed as " + x.getMessage());
            return Collections.EMPTY_LIST;
        }
    }
}
