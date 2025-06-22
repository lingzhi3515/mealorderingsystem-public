package com.restaurant.server.models;

import com.google.gson.JsonObject;
import com.restaurant.server.models.OrderDao;
import com.restaurant.server.models.Order;
import com.google.gson.Gson;

import java.io.*;
import java.net.Socket;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public class OrderController {
    private static final Gson gson = new Gson();
    private final OrderDao orderDao = new OrderDao();

    public void handleGetOrders(Socket clientSocket) throws IOException {
        try {
            List<Order> orders = orderDao.getAllOrders();
            sendJsonResponse(clientSocket, 200, orders);
        } catch (SQLException e) {
            sendErrorResponse(clientSocket, 500, "Database error");
        }
    }

    public void handleUpdateStatus(Socket clientSocket, String requestBody) throws IOException {
        try {
            JsonObject json = gson.fromJson(requestBody, JsonObject.class);
            int orderId = json.get("orderId").getAsInt();
            Order.Status newStatus = Order.Status.valueOf(json.get("newStatus").getAsString());

            if (orderDao.updateOrderStatus(orderId, newStatus)) {
                sendJsonResponse(clientSocket, 200, Map.of("success", true));
            } else {
                sendErrorResponse(clientSocket, 404, "Order not found");
            }
        } catch (SQLException e) {
            sendErrorResponse(clientSocket, 500, "Update failed");
        }
    }

    private void sendJsonResponse(Socket socket, int statusCode, Object data) throws IOException {
        PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
        out.println("HTTP/1.1 " + statusCode);
        out.println("Content-Type: application/json");
        out.println();
        out.println(gson.toJson(data));
    }

    private void sendErrorResponse(Socket socket, int statusCode, String message) throws IOException {
        sendJsonResponse(socket, statusCode, Map.of("error", message));
    }
}