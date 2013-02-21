/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.monnetproject.translator;

import eu.monnetproject.translatorimpl.Translator;
import eu.monnetproject.lang.Language;
import java.util.Collection;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author jmccrae
 */
public class TranslatorTest {
    
    public TranslatorTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }

    /**
     * Test of translate method, of class Translator.
     */
    @Test
    public void testTranslate() {
        System.out.println("translate");
        String label = "cat";
        Language srcLang = Language.ENGLISH;
        Language trgLang = Language.SPANISH;
        Translator instance = new Translator();
        Collection result = instance.translate(label, srcLang, trgLang);
    }
}
