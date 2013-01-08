package eu.monnetproject.align;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;

/**
 * Interface for creating new Alignment objects, reading existing alignments from a file (e.g. for training), and for writing Alignment obejcts to files.
 * 
 * @author Dennis Spohr
 *
 */
public interface AlignmentSerializer {
	
	public void writeAlignment(Alignment alignment, File file) throws IOException;
	
	public void writeAlignment(Alignment alignment, OutputStream stream) throws IOException;
        
	public void writeAlignment(Alignment alignment, Writer write) throws IOException;
	
	public Alignment createAlignment();

	public Alignment createAlignment(int k);
	
	public Alignment readAlignment(File file);

	public Alignment readAlignment(InputStream stream);
        
	public Alignment readAlignment(Reader reader);
        
        int getProgress();

}
