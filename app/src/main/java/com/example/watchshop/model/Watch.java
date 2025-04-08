package com.example.watchshop.model;

import java.io.Serializable;
import java.util.List;

public class Watch implements Serializable {
    private String _id;
    private String brand;
    private String model;
    private double price_range;
    private List<String> images;
    private String description;
    private String movement;
    private String case_material;
    private String bracelet_material;
    private String water_resistance_m;
    private String diameter;

    // Getters and setters
    public String get_Id() {
        return _id;
    }

    public void set_Id(String id) {
        this._id = id;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public double getPrice_Range() {
        return price_range;
    }

    public void setPrice_Range(double price) {
        this.price_range = price;
    }

    public List<String> getImages() {
        return images;
    }

    public void setImages(List<String> images) {
        this.images = images;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getMovement() {
        return movement;
    }

    public void setMovement(String movement) {
        this.movement = movement;
    }

    public String getCase_Material() {
        return case_material;
    }

    public void setCase_Material(String caseMaterial) {
        this.case_material = caseMaterial;
    }

    public String getBracelet_Material() {
        return bracelet_material;
    }

    public void setBracelet_Material(String braceletMaterial) {
        this.bracelet_material = braceletMaterial;
    }

    public String getWater_Resistance_M() {
        return water_resistance_m;
    }

    public void setWater_Resistance_M(String waterResistance) {
        this.water_resistance_m = waterResistance;
    }

    public String getDiameter() {
        return diameter;
    }

    public void setDiameter(String diameter) {
        this.diameter = diameter;
    }
}