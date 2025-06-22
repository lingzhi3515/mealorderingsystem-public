package com.restaurant.server.models;

/**
 * 用户数据模型（如需身份验证）
 */
public class User {
    private int userId;
    private String username;
    private String passwordHash;
    private String role; // admin/customer

    // 构造器和getter/setter
}