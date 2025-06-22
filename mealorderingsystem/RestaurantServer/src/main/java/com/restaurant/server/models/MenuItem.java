package com.restaurant.server.models;

/**
 * 菜品数据模型
 */
public class MenuItem {
    private int id;
    private String name;
    private String description;
    private double price;
    private String category;
    private String imageUrl; // 菜品图片URL

    // 全参构造器
    public MenuItem(int id, String name, String description,
                    double price, String category) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;
        this.category = category;
    }

    public MenuItem() {

    }


    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
}