package eu.monnetproject.coal.demo;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;

import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.RichTextArea;
import com.vaadin.ui.TextArea;

public class ResultPanel extends CustomComponent {
	
    Panel root;
    
    ResultPanel(String caption, File file) {
        root = new Panel(caption);
        setCompositionRoot(root);

        BufferedReader reader;
        
        final TextArea tArea = new TextArea();
        
        tArea.setRows(50);
        tArea.setColumns(120);
        
        String content = "";
        int lineCount = 0;
        
		try {
			reader = new BufferedReader(new FileReader(file));
			while (reader.ready() && lineCount <= 500) {
				content += reader.readLine()+"\n";
				lineCount++;
	        }
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		tArea.setValue(content);
		
        tArea.setReadOnly(true);

        root.addComponent(tArea);

    }
    
}
