package eu.monnetproject.coal;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;


import eu.monnetproject.align.Alignment;
import java.io.Writer;

/**
 * Class for writing an alignment to a file.
 * 
 * @author dspohr
 *
 */
//@Component(provide=AlignmentSerializer.class)
public interface CoalWriter {

	public int getProgress();
	
	public void write(Alignment alignment, File file) throws IOException;
	
	public void write(Alignment alignment, OutputStream stream) throws IOException;
	
        public void write(Alignment alignment, Writer writer) throws IOException;
}
