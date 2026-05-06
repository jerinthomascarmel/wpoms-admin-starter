package com.wpoms.admin.services.impl;

import com.wpoms.admin.models.entities.*;
import com.wpoms.admin.models.response.*;
import com.wpoms.admin.models.response.PlaceOrderResponse.OrderSummary;
import com.wpoms.admin.models.response.VendorOrderDetailResponse.OrderItemDetail;
import com.wpoms.admin.models.response.VendorOrderDetailResponse.ProductInfo;
import com.wpoms.admin.models.response.VendorOrderListResponse.VendorOrderSummary;
import com.wpoms.admin.repositories.*;
import com.wpoms.admin.services.IVendorOrderService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class VendorOrderService implements IVendorOrderService {

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ManufacturerMasterRepository manufacturerRepository;

    // ========================= PLACE ORDER =========================
    @Override
    @Transactional
    public PlaceOrderResponse placeOrder(int vendorId) {

        // 1. Find vendor's cart
        Cart cart = cartRepository.findByVendorId(vendorId)
                .orElseThrow(() -> new RuntimeException("Cart not found for vendor"));

        // 2. Get cart items
        List<CartItem> cartItems = cartItemRepository.findByCartId(cart.getCartId());
        if (cartItems.isEmpty()) {
            throw new RuntimeException("Cart is empty");
        }

        // 3. Group cart items by manufacturer_id (from product)
        Map<Integer, List<CartItem>> groupedByManufacturer = new HashMap<>();
        Map<Integer, Product> productMap = new HashMap<>();

        for (CartItem cartItem : cartItems) {
            Product product = productRepository.findById(cartItem.getProductId())
                    .orElseThrow(() -> new RuntimeException("Product not found: " + cartItem.getProductId()));
            productMap.put(product.getProductId(), product);

            groupedByManufacturer
                    .computeIfAbsent(product.getManufacturerId(), k -> new ArrayList<>())
                    .add(cartItem);
        }

        // 4. Create one order per manufacturer
        List<OrderSummary> orderSummaries = new ArrayList<>();

        for (Map.Entry<Integer, List<CartItem>> entry : groupedByManufacturer.entrySet()) {
            int manufacturerId = entry.getKey();
            List<CartItem> items = entry.getValue();

            // Calculate total amount
            double totalAmount = 0;
            for (CartItem item : items) {
                Product product = productMap.get(item.getProductId());
                totalAmount += product.getPrice() * item.getQuantity();
            }

            // Create order
            Order order = new Order();
            order.setVendorId(vendorId);
            order.setManufacturerId(manufacturerId);
            order.setOrderDate(LocalDate.now());
            order.setStatus("PENDING");
            order.setTotalAmount(totalAmount);

            Order savedOrder = orderRepository.save(order);

            // Generate PO number
            savedOrder.setPoNumber("PO-" + String.format("%03d", savedOrder.getOrderId()));
            orderRepository.save(savedOrder);

            // Create order items
            for (CartItem cartItem : items) {
                Product product = productMap.get(cartItem.getProductId());

                OrderItem orderItem = new OrderItem();
                orderItem.setOrderId(savedOrder.getOrderId());
                orderItem.setProductId(cartItem.getProductId());
                orderItem.setQuantity(cartItem.getQuantity());
                orderItem.setPrice(product.getPrice());
                orderItem.setSubtotal(product.getPrice() * cartItem.getQuantity());

                orderItemRepository.save(orderItem);
            }

            // Build order summary for response
            ManufacturerMaster manufacturer = manufacturerRepository.findById(manufacturerId)
                    .orElse(null);
            String manufacturerName = manufacturer != null ? manufacturer.getCompanyName() : "Unknown";

            OrderSummary summary = new OrderSummary();
            summary.setPoNumber(savedOrder.getPoNumber());
            summary.setManufacturer(manufacturerName);
            summary.setStatus("PENDING");
            summary.setTotalAmount(totalAmount);

            orderSummaries.add(summary);
        }

        // 5. Clear the cart
        cartItemRepository.deleteByCartId(cart.getCartId());

        // 6. Build response
        PlaceOrderResponse response = new PlaceOrderResponse();
        response.setMessage("Order placed successfully");
        response.setOrders(orderSummaries);

        return response;
    }

    // ========================= GET ALL ORDERS =========================
    @Override
    public VendorOrderListResponse getAllOrders(int vendorId) {

        List<Order> orders = orderRepository.findByVendorId(vendorId);

        List<VendorOrderSummary> orderSummaries = orders.stream().map(order -> {
            VendorOrderSummary summary = new VendorOrderSummary();
            summary.setPoNumber(order.getPoNumber());
            summary.setOrderDate(order.getOrderDate() != null ? order.getOrderDate().toString() : null);
            summary.setStatus(order.getStatus());
            summary.setDeliveryDate(order.getDeliveryDate() != null ? order.getDeliveryDate().toString() : null);

            // Get manufacturer name
            ManufacturerMaster manufacturer = manufacturerRepository.findById(order.getManufacturerId())
                    .orElse(null);
            summary.setManufacturer(manufacturer != null ? manufacturer.getCompanyName() : "Unknown");

            return summary;
        }).collect(Collectors.toList());

        VendorOrderListResponse response = new VendorOrderListResponse();
        response.setOrders(orderSummaries);
        return response;
    }

    // ========================= GET ORDER DETAILS =========================
    @Override
    public VendorOrderDetailResponse getOrderDetails(int orderId) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        // Get manufacturer name
        ManufacturerMaster manufacturer = manufacturerRepository.findById(order.getManufacturerId())
                .orElse(null);

        // Get order items
        List<OrderItem> orderItems = orderItemRepository.findByOrderId(orderId);

        List<OrderItemDetail> itemDetails = orderItems.stream().map(item -> {
            OrderItemDetail detail = new OrderItemDetail();
            detail.setOrderItemId(item.getOrderItemId());
            detail.setQuantity(item.getQuantity());
            detail.setPrice(item.getPrice());
            detail.setSubtotal(item.getSubtotal());

            // Get product info
            Product product = productRepository.findById(item.getProductId()).orElse(null);
            if (product != null) {
                ProductInfo productInfo = new ProductInfo();
                productInfo.setProductId(product.getProductId());
                productInfo.setProductName(product.getProductName());
                productInfo.setCategory(product.getCategory());
                productInfo.setDescription(product.getDescription());
                productInfo.setWarrantyType(product.getWarrantyType());
                productInfo.setManufacturerId(product.getManufacturerId());
                detail.setProduct(productInfo);
            }

            return detail;
        }).collect(Collectors.toList());

        VendorOrderDetailResponse response = new VendorOrderDetailResponse();
        response.setPoNumber(order.getPoNumber());
        response.setManufacturer(manufacturer != null ? manufacturer.getCompanyName() : "Unknown");
        response.setStatus(order.getStatus());
        response.setOrderDate(order.getOrderDate() != null ? order.getOrderDate().toString() : null);
        response.setDeliveryDate(order.getDeliveryDate() != null ? order.getDeliveryDate().toString() : null);
        response.setItems(itemDetails);
        response.setTotalAmount(order.getTotalAmount());

        return response;
    }

    // ========================= CANCEL ORDER =========================
    @Override
    public CancelOrderResponse cancelOrder(int orderId) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if (!"PENDING".equals(order.getStatus())) {
            throw new RuntimeException("Only PENDING orders can be cancelled");
        }

        order.setStatus("CANCELLED");
        orderRepository.save(order);

        CancelOrderResponse response = new CancelOrderResponse();
        response.setMessage("Order cancelled successfully");
        response.setStatus("CANCELLED");

        return response;
    }
}
