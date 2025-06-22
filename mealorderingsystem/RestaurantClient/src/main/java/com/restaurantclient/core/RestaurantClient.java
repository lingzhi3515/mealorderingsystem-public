// RestaurantClient.java
package com.restaurantclient.core;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.restaurantclient.models.MenuItem;
import com.restaurantclient.models.Order;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RestaurantClient {
    private static final String SERVER_HOST = "localhost";
    private static final int SERVER_PORT = 8888;
    private static final Gson gson = new Gson();

    private JFrame frame;
    private JPanel menuPanel;
    private JPanel orderPanel;
    private JTextArea orderSummary;
    private Map<Integer, MenuItem> menuItems = new HashMap<>();
    private Map<Integer, Integer> cart = new HashMap<>();

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                new RestaurantClient().initialize();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public void initialize() {
        // 创建主窗口
        frame = new JFrame("餐厅订餐系统");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 600);

        // 创建选项卡面板
        JTabbedPane tabbedPane = new JTabbedPane();

        // 菜单面板
        menuPanel = new JPanel(new BorderLayout());
        loadMenuItems();
        tabbedPane.addTab("菜单", menuPanel);

        // 订单面板
        orderPanel = new JPanel(new BorderLayout());
        setupOrderPanel();
        tabbedPane.addTab("我的订单", orderPanel);

        frame.add(tabbedPane);
        frame.setVisible(true);
    }

    private void loadMenuItems() {
        try (Socket socket = new Socket(SERVER_HOST, SERVER_PORT);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            // 发送获取菜单请求
            String request = "{\"action\":\"getMenu\"}";
            out.println(request);

            // 接收响应
            String response = in.readLine();
            List<MenuItem> items = gson.fromJson(response, new TypeToken<List<MenuItem>>(){}.getType());

            // 显示菜单
            displayMenuItems(items);
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(frame, "无法连接到服务器: " + e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void displayMenuItems(List<MenuItem> items) {
        JPanel itemsPanel = new JPanel(new GridLayout(0, 1, 5, 5));
        JScrollPane scrollPane = new JScrollPane(itemsPanel);

        for (MenuItem item : items) {
            menuItems.put(item.getId(), item);

            JPanel itemPanel = new JPanel(new BorderLayout());
            itemPanel.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));

            JLabel nameLabel = new JLabel(item.getName() + " - ¥" + item.getPrice());
            JTextArea descArea = new JTextArea(item.getDescription());
            descArea.setEditable(false);
            descArea.setLineWrap(true);
            descArea.setBackground(itemPanel.getBackground());

            JPanel controlPanel = new JPanel();
            JSpinner quantitySpinner = new JSpinner(new SpinnerNumberModel(0, 0, 10, 1));
            JButton addButton = new JButton("加入订单");

            addButton.addActionListener(e -> {
                int quantity = (Integer) quantitySpinner.getValue();
                if (quantity > 0) {
                    cart.put(item.getId(), cart.getOrDefault(item.getId(), 0) + quantity);
                    quantitySpinner.setValue(0);
                    updateOrderSummary();
                    JOptionPane.showMessageDialog(frame, "已添加 " + quantity + " 份 " + item.getName(), "提示", JOptionPane.INFORMATION_MESSAGE);
                }
            });

            controlPanel.add(new JLabel("数量:"));
            controlPanel.add(quantitySpinner);
            controlPanel.add(addButton);

            itemPanel.add(nameLabel, BorderLayout.NORTH);
            itemPanel.add(descArea, BorderLayout.CENTER);
            itemPanel.add(controlPanel, BorderLayout.SOUTH);

            itemsPanel.add(itemPanel);
        }

        menuPanel.add(scrollPane, BorderLayout.CENTER);
    }

    private void setupOrderPanel() {
        orderSummary = new JTextArea("您的订单:\n\n");
        orderSummary.setEditable(false);

        JScrollPane scrollPane = new JScrollPane(orderSummary);

        JPanel buttonPanel = new JPanel();
        JButton submitButton = new JButton("提交订单");
        JButton clearButton = new JButton("清空订单");

        submitButton.addActionListener(e -> submitOrder());
        clearButton.addActionListener(e -> {
            cart.clear();
            updateOrderSummary();
        });

        buttonPanel.add(submitButton);
        buttonPanel.add(clearButton);

        orderPanel.add(scrollPane, BorderLayout.CENTER);
        orderPanel.add(buttonPanel, BorderLayout.SOUTH);
    }

    private void updateOrderSummary() {
        StringBuilder summary = new StringBuilder("您的订单:\n\n");
        double total = 0;

        for (Map.Entry<Integer, Integer> entry : cart.entrySet()) {
            MenuItem item = menuItems.get(entry.getKey());
            int quantity = entry.getValue();
            double itemTotal = item.getPrice() * quantity;

            summary.append(item.getName())
                    .append(" x ").append(quantity)
                    .append(" = ¥").append(String.format("%.2f", itemTotal))
                    .append("\n");

            total += itemTotal;
        }

        summary.append("\n总计: ¥").append(String.format("%.2f", total));
        orderSummary.setText(summary.toString());
    }

    private void submitOrder() {
        if (cart.isEmpty()) {
            JOptionPane.showMessageDialog(frame, "订单为空，请先添加菜品", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String customerName = JOptionPane.showInputDialog(frame, "请输入您的姓名:", "客户信息", JOptionPane.QUESTION_MESSAGE);
        if (customerName == null || customerName.trim().isEmpty()) {
            return;
        }

        String customerPhone = JOptionPane.showInputDialog(frame, "请输入您的电话:", "客户信息", JOptionPane.QUESTION_MESSAGE);
        if (customerPhone == null || customerPhone.trim().isEmpty()) {
            return;
        }

        // 创建订单对象
        Order order = new Order();
        order.setCustomerName(customerName);
        order.setCustomerPhone(customerPhone);

        List<Order.OrderItem> orderItems = new ArrayList<>();
        for (Map.Entry<Integer, Integer> entry : cart.entrySet()) {
            Order.OrderItem item = new Order.OrderItem();
            item.setItemId(entry.getKey());
            item.setQuantity(entry.getValue());
            orderItems.add(item);
        }
        order.setItems(orderItems);

        // 发送订单到服务器
        try (Socket socket = new Socket(SERVER_HOST, SERVER_PORT);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            JsonObject request = new JsonObject();
            request.addProperty("action", "placeOrder");
            request.add("order", gson.toJsonTree(order));

            out.println(gson.toJson(request));

            String response = in.readLine();
            if (response.contains("successfully")) {
                JOptionPane.showMessageDialog(frame, "订单提交成功!", "成功", JOptionPane.INFORMATION_MESSAGE);
                cart.clear();
                updateOrderSummary();
            } else {
                JOptionPane.showMessageDialog(frame, "订单提交失败: " + response, "错误", JOptionPane.ERROR_MESSAGE);
            }
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(frame, "无法连接到服务器: " + e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
        }
    }
}