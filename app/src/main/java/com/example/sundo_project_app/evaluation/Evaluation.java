// Evaluation.java
package com.example.sundo_project_app.evaluation;

import java.io.Serializable;

public class Evaluation implements Serializable {
    private String title;
    private String registrantName;
    private String arImage;
    private int windVolume;
    private int noiseLevel;
    private int waterDepth;
    private int scenery;
    private int averageRating;
    private long evaluationId;
    private String priRegistrationDate;

    // Getters and Setters
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getRegistrantName() {
        return registrantName;
    }

    public void setRegistrantName(String registrantName) {
        this.registrantName = registrantName;
    }

    public String getArImage() {
        return arImage;
    }

    public void setArImage(String arImage) {
        this.arImage = arImage;
    }

    public int getAverageRating (){return averageRating;}
    public void setAverageRating (int averageRating){this.averageRating = averageRating;}

    public Long getEvaluationId (){return evaluationId;}
    public void setEvaluationId (long evaluationId){this.evaluationId = evaluationId;}

    public int getWindVolume (){return windVolume;}
    public void setWindVolume (int windVolume){this.windVolume = windVolume;}

    public int getNoiseLevel (){return noiseLevel;}
    public void setNoiseLevel (int noiseLevel){this.noiseLevel = noiseLevel;}

    public int getWaterDepth (){return waterDepth;}
    public void setWaterDepth (int waterDepth){this.waterDepth = waterDepth;}

    public int getScenery (){return scenery;}
    public void setScenery(int scenery){this.scenery = scenery;}

    public String getPriRegistrationDate (){return priRegistrationDate;}
    public void setPriRegistrationDate(String priRegistrationDate){this.priRegistrationDate = priRegistrationDate;}
}
