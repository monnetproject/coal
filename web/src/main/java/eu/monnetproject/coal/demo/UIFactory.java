package eu.monnetproject.coal.demo;

import java.io.File;

import com.vaadin.terminal.FileResource;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.Embedded;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;

import eu.monnetproject.align.Alignment;

public class UIFactory {

	public static UploadComponent getUploadComponent() {
		
		UploadComponent uploadComponent = new UploadComponent();
		return uploadComponent;
	}
	
	public static CssLayout getDefaultLayout() {
		CssLayout layout = new CssLayout() {
		    @Override
		    protected String getCss(Component c) {
		        if (c instanceof Label) {
		            return ".v-label-mystyle {\n"+
		            	   "font-size: 24px\n"+
                           "line-height: normal;\n"+
                           	"}\n";
		        }
		        return null;
		    }
		};
		return layout;
	}
	
	public static Component getHSpacing() {
		return getHSpacing(10);
	}
	
	public static Component getHSpacing(int n) {
		Label spacing = new Label();
		spacing.setWidth(n+"px");
		return spacing;
	}
	
	public static Component getVSpacing() {
		return getVSpacing(10);
	}
	
	public static Component getVSpacing(int n) {
		Label spacing = new Label();
		spacing.setHeight(n+"px");
		return spacing;
	}
	
	public static Label getBoldLabel(String text) {
		return new Label("<b>"+text+"</b>",Label.CONTENT_XHTML);
	}
	
	public static Label getLargeBlueLabel(String text) {
		return new Label("<font size=\"3\" color=\"blue\"><b>"+text+"</b></font>",Label.CONTENT_XHTML);
	}

	public static Component getResultPanel(File file) {
		return new ResultPanel("Alignment results",file);		
	}

}
