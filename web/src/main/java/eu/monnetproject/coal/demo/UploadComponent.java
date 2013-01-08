package eu.monnetproject.coal.demo;

import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

import com.vaadin.terminal.FileResource;
import com.vaadin.ui.*;
import java.util.logging.Logger;

public class UploadComponent extends CustomComponent
        implements Upload.SucceededListener,
        Upload.FailedListener,
        Upload.Receiver,
        Button.ClickListener {

    private final Logger log = Logger.getLogger(UploadComponent.class.getName());
    Panel root;         // Root element for contained components.
    Panel srcPanel;   // Panel that contains the uploaded image.
    Panel tgtPanel;   // Panel that contains the uploaded image.
    File file;         // File to write to.
    File srcOntology = null;
    File tgtOntology = null;
    Upload uploadSrc;
    Upload uploadTgt;
    Button alignButton = null;
    private Button resetButton;

    UploadComponent() {
        root = new Panel();
        setCompositionRoot(root);

        init();

    }

    public void init() {

        root.removeAllComponents();
        // Create the Upload component.
        uploadSrc =
                new Upload("Source ontology", this);
        uploadTgt =
                new Upload("Target ontology", this);

        // Use a custom button caption instead of plain "Upload".
        uploadSrc.setButtonCaption("Upload ontology");
        uploadTgt.setButtonCaption("Upload ontology");

        // Listen for events regarding the success of upload.
        uploadSrc.addListener((Upload.SucceededListener) this);
        uploadSrc.addListener((Upload.FailedListener) this);

        uploadTgt.addListener((Upload.SucceededListener) this);
        uploadTgt.addListener((Upload.FailedListener) this);

        root.addComponent(uploadSrc);
        root.addComponent(uploadTgt);
        //root.addComponent(new Label("Click 'Browse' to "+
        //        "select a file and then click 'Upload'."));

        Label vSpacing = new Label();
        vSpacing.setHeight("10px");
        root.addComponent(vSpacing);

        Label hSpacing = new Label();
        hSpacing.setWidth("10px");

        srcPanel = new Panel("Source ontology");
        srcPanel.addComponent(
                new Label("No source ontology uploaded yet"));
        root.addComponent(srcPanel);

        root.addComponent(vSpacing);

        tgtPanel = new Panel("Target ontology");
        tgtPanel.addComponent(
                new Label("No target ontology uploaded yet"));
        root.addComponent(tgtPanel);

        root.addComponent(vSpacing);

        HorizontalLayout horiz = new HorizontalLayout();

        alignButton =
                new Button("Align ontologies", (Button.ClickListener) this);
        alignButton.setEnabled(false);

        horiz.addComponent(alignButton);

        horiz.addComponent(hSpacing);

        resetButton =
                new Button("Reset", (Button.ClickListener) this);
        resetButton.setEnabled(true);

        horiz.addComponent(resetButton);

        root.addComponent(horiz);

    }

    // Callback method to begin receiving the upload.
    public OutputStream receiveUpload(String filename,
            String MIMEType) {
        FileOutputStream fos = null; // Output stream to write to


        try {
            file = File.createTempFile(filename, ".rdf");
            file.deleteOnExit();


            log.info("Receiving upload");
            // Open the file for writing.
            fos = new FileOutputStream(file);
        } catch (final java.io.IOException e) {
            // Error while opening the file. Not reported here.
            e.printStackTrace();
            return null;
        }

        return fos; // Return the output stream to write to
    }

    // This is called if the upload is finished.
    public void uploadSucceeded(Upload.SucceededEvent event) {
        // Log the upload on screen.
        //root.addComponent(new Label("File " + event.getFilename()
        //        + " of type '" + event.getMIMEType()
        //        + "' uploaded."));

        // Display the uploaded file in the image panel.
        final FileResource ontResource =
                new FileResource(file, getApplication());

        if (event.getUpload().equals(uploadSrc)) {
            srcPanel.removeAllComponents();
            srcOntology = ontResource.getSourceFile();
            srcPanel.addComponent(new Label(srcOntology.getAbsolutePath()));
        } else if (event.getUpload().equals(uploadTgt)) {
            tgtPanel.removeAllComponents();
            tgtOntology = ontResource.getSourceFile();
            tgtPanel.addComponent(new Label(tgtOntology.getAbsolutePath()));
        }

        event.getUpload().setEnabled(false);

        if (srcOntology != null && tgtOntology != null) {
            alignButton.setEnabled(true);
        }

    }

    // This is called if the upload fails.
    public void uploadFailed(Upload.FailedEvent event) {
        // Log the failure on screen.
        root.addComponent(new Label("Uploading "
                + event.getFilename() + " of type '"
                + event.getMIMEType() + "' failed."));
    }

    @Override
    public void buttonClick(ClickEvent event) {
        event.getButton().setEnabled(false);
        if (event.getComponent().equals(alignButton) && getApplication() instanceof COALDemo) {
            ((COALDemo) getApplication()).startAlignment(srcOntology, tgtOntology, event.getButton());
        } else if (event.getComponent().equals(resetButton)) {
            ((COALDemo) getApplication()).reset();
        }
    }
}
