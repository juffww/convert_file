package model.bean;

import java.sql.Timestamp;

public class conversion {
    private int id;
    private int userId;
    private String inputUrl;
    private String inputPublicId;
    private String inputFilename;
    private String outputUrl;
    private String outputPublicId;
    private String status; // UPLOADED, PENDING, PROCESSING, COMPLETED, FAILED
    private String errorMessage;
    private Timestamp createdAt;
    private Timestamp updatedAt;

    public conversion() {}

    public conversion(int userId, String inputUrl, String inputPublicId, String inputFilename) {
        this.userId = userId;
        this.inputUrl = inputUrl;
        this.inputPublicId = inputPublicId;
        this.inputFilename = inputFilename;
        this.status = "UPLOADED";
    }

    // Full Getters and Setters (Tự generate bằng IDE nhé)
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
    public String getInputUrl() { return inputUrl; }
    public void setInputUrl(String inputUrl) { this.inputUrl = inputUrl; }
    public String getInputPublicId() { return inputPublicId; }
    public void setInputPublicId(String inputPublicId) { this.inputPublicId = inputPublicId; }
    public String getInputFilename() { return inputFilename; }
    public void setInputFilename(String inputFilename) { this.inputFilename = inputFilename; }
    public String getOutputUrl() { return outputUrl; }
    public void setOutputUrl(String outputUrl) { this.outputUrl = outputUrl; }
    public String getOutputPublicId() { return outputPublicId; }
    public void setOutputPublicId(String outputPublicId) { this.outputPublicId = outputPublicId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
    public Timestamp getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Timestamp updatedAt) { this.updatedAt = updatedAt; }
}
