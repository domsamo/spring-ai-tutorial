package com.fbc.ai.domain.dto;

public class Recipe {
    //   음식재료       요리유형      식단제한정보
    private String ingredients;
    private String cuisine;
    private String dietaryRestrictions;

    // 기본 생성자
    public Recipe() {
    }

    // 모든 필드를 포함하는 생성자
    public Recipe(String ingredients, String cuisine, String dietaryRestrictions) {
        this.ingredients = ingredients;
        this.cuisine = cuisine;
        this.dietaryRestrictions = dietaryRestrictions;
    }

    // Getter와 Setter 메서드
    public String getIngredients() {
        return ingredients;
    }

    public void setIngredients(String ingredients) {
        this.ingredients = ingredients;
    }

    public String getCuisine() {
        return cuisine;
    }

    public void setCuisine(String cuisine) {
        this.cuisine = cuisine;
    }

    public String getDietaryRestrictions() {
        return dietaryRestrictions;
    }

    public void setDietaryRestrictions(String dietaryRestrictions) {
        this.dietaryRestrictions = dietaryRestrictions;
    }

    // toString 메서드 (객체 정보 출력에 유용)
    @Override
    public String toString() {
        return "Recipe{" +
                "ingredients='" + ingredients + '\'' +
                ", cuisine='" + cuisine + '\'' +
                ", dietaryRestrictions='" + dietaryRestrictions + '\'' +
                '}';
    }
}
