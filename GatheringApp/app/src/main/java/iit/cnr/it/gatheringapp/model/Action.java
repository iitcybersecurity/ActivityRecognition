package iit.cnr.it.gatheringapp.model;


public class Action {

    private String label;
    private String description;
    private String previewResourceName;
    private String instructionsResourceName;

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getPreviewResourceName() {
        return previewResourceName;
    }

    public void setPreviewResourceName(String previewResourceName) {
        this.previewResourceName = previewResourceName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getInstructionsResourceName() {
        return instructionsResourceName;
    }

    public void setInstructionsResourceName(String instructionsResourceName) {
        this.instructionsResourceName = instructionsResourceName;
    }
}
