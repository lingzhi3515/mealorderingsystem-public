package com.restaurant.server.core;

import com.restaurant.server.database.DatabaseConnection;
import com.restaurant.server.models.OrderDao;
import com.restaurant.server.models.AdminPanel;
import com.restaurant.server.models.MenuItem;
import com.restaurant.server.models.Order;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.restaurant.server.models.OrderDao;

import javax.swing.*;

public class RestaurantServer {
    private static final int PORT = 8888;
    private static final Gson gson = new Gson();
    private static AdminPanel adminPanel;

    public static void main(String[] args) {
        // 启动管理面板
        SwingUtilities.invokeLater(() -> {
            adminPanel = new AdminPanel();
            adminPanel.setVisible(true);
        });

        // 启动网络服务
        startServer();
    }

    private static void startServer() {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server started on port " + PORT);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                new ClientHandler(clientSocket).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 新增方法：通知面板刷新数据
    public static void refreshAdminPanel() {
        if (adminPanel != null) {
            adminPanel.loadOrders();
        }
    }

    private static class ClientHandler extends Thread {
        private final Socket clientSocket;
        private final OrderDao orderDao = new OrderDao();

        public ClientHandler(Socket socket) {
            this.clientSocket = socket;
        }

        public void run() {
            try (
                    BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                    PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)
            ) {
                String request = in.readLine();
                System.out.println("Received request: " + request);

                JsonObject jsonRequest = JsonParser.parseString(request).getAsJsonObject();
                String action = jsonRequest.get("action").getAsString();

                switch (action) {
                    case "getMenu":
                        List<MenuItem> menuItems = getMenuItems();
                        out.println(gson.toJson(menuItems));
                        break;
                    case "placeOrder":
                        Order order = gson.fromJson(jsonRequest.get("order"), Order.class);
                        boolean success = placeOrder(order);
                        if (success) {
                            refreshAdminPanel(); // 新增：订单提交后刷新面板
                        }
                        out.println(success ? "Order placed successfully" : "Failed to place order");
                        break;
                    case "updateOrderStatus": // 新增状态更新处理
                        int orderId = jsonRequest.get("orderId").getAsInt();
                        Order.Status status = Order.Status.valueOf(jsonRequest.get("status").getAsString());
                        boolean updated = orderDao.updateOrderStatus(orderId, status);
                        if (updated) {
                            refreshAdminPanel();
                        }
                        out.println(updated ? "Status updated" : "Update failed");
                        break;
                    default:
                        out.println("Invalid action");
                }
            } catch (IOException | SQLException e) {
                e.printStackTrace();
            } finally {
                try {
                    clientSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        private List<MenuItem> getMenuItems() {
            List<MenuItem> menuItems = new ArrayList<>();
            String query = "SELECT * FROM menu_items";

            try (Connection conn = DatabaseConnection.getConnection();
                 Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(query)) {

                while (rs.next()) {
                    MenuItem item = new MenuItem();
                    item.setId(rs.getInt("id"));
                    item.setName(rs.getString("name"));
                    item.setDescription(rs.getString("description"));
                    item.setPrice(rs.getDouble("price"));
                    item.setCategory(rs.getString("category"));
                    menuItems.add(item);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }

            return menuItems;
        }

        private boolean placeOrder(Order order) {
            String orderQuery = "INSERT INTO orders (customer_name, customer_phone) VALUES (?, ?)";
            String detailQuery = "INSERT INTO order_details (order_id, item_id, quantity) VALUES (?, ?, ?)";

            try (Connection conn = DatabaseConnection.getConnection()) {
                conn.setAutoCommit(false);

                try (PreparedStatement orderStmt = conn.prepareStatement(orderQuery, Statement.RETURN_GENERATED_KEYS)) {
                    orderStmt.setString(1, order.getCustomerName());
                    orderStmt.setString(2, order.getCustomerPhone());
                    orderStmt.executeUpdate();

                    try (ResultSet generatedKeys = orderStmt.getGeneratedKeys()) {
                        if (generatedKeys.next()) {
                            int orderId = generatedKeys.getInt(1);

                            try (PreparedStatement detailStmt = conn.prepareStatement(detailQuery)) {
                                for (Order.OrderItem item : order.getItems()) {
                                    detailStmt.setInt(1, orderId);
                                    detailStmt.setInt(2, item.getItemId());
                                    detailStmt.setInt(3, item.getQuantity());
                                    detailStmt.addBatch();
                                }
                                detailStmt.executeBatch();
                            }
                        }
                    }
                }

                conn.commit();
                return true;
            } catch (SQLException e) {
                e.printStackTrace();
                return false;
            }
        }
    }
}