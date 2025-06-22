package com.restaurant.server.models;

import com.restaurant.server.database.DatabaseConnection;
import com.restaurant.server.models.Order;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class OrderDao {
    // 查询所有订单
    public List<Order> getAllOrders() throws SQLException {
        String sql = "SELECT * FROM orders";
        List<Order> orders = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Order order = new Order();
                order.setOrderId(rs.getInt("id"));
                order.setStatus(Order.Status.valueOf(rs.getString("status")));
                // 设置其他字段...
                orders.add(order);
            }
        }
        return orders;
    }

    // 更新订单状态
    public boolean updateOrderStatus(int orderId, Order.Status newStatus) throws SQLException {
        String sql = "UPDATE orders SET status = ? WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, newStatus.name());
            stmt.setInt(2, orderId);
            return stmt.executeUpdate() > 0;
        }
    }
}