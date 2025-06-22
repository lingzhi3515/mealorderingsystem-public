package com.restaurant.server.models;

import com.restaurant.server.models.OrderDao;
import com.restaurant.server.models.Order;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

public class AdminPanel extends JFrame {
    private final OrderDao orderDao = new OrderDao();
    private JTable orderTable;

    public AdminPanel() {
        setTitle("餐厅订单管理系统");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        initUI();
        loadOrders();
    }

    private void initUI() {
        // 主面板
        JPanel mainPanel = new JPanel(new BorderLayout());

        // 订单表格
        orderTable = new JTable();
        orderTable.setAutoCreateRowSorter(true);
        JScrollPane scrollPane = new JScrollPane(orderTable);

        // 操作按钮
        JButton refreshBtn = new JButton("刷新订单");
        JButton updateBtn = new JButton("标记为已完成");

        refreshBtn.addActionListener(e -> loadOrders());
        updateBtn.addActionListener(e -> updateSelectedOrder());

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(refreshBtn);
        buttonPanel.add(updateBtn);

        // 组装界面
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        add(mainPanel);
    }

    public void loadOrders() {
        try {
            List<Order> orders = orderDao.getAllOrders();
            DefaultTableModel model = new DefaultTableModel(
                    new Object[]{"订单ID", "客户名", "电话", "状态", "下单时间"}, 0);

            for (Order order : orders) {
                model.addRow(new Object[]{
                        order.getOrderId(),
                        order.getCustomerName(),
                        order.getCustomerPhone(),
                        order.getStatus(),
                });
            }

            orderTable.setModel(model);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "加载订单失败: " + e.getMessage(),
                    "错误", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateSelectedOrder() {
        int selectedRow = orderTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "请先选择订单");
            return;
        }

        int orderId = (int) orderTable.getValueAt(selectedRow, 0);
        try {
            if (orderDao.updateOrderStatus(orderId, Order.Status.done)) {
                JOptionPane.showMessageDialog(this, "订单状态已更新");
                loadOrders(); // 刷新列表
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "更新失败: " + e.getMessage(),
                    "错误", JOptionPane.ERROR_MESSAGE);
        }
    }
}