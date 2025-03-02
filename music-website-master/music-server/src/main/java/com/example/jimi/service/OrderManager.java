package com.example.jimi.service;

import com.example.jimi.model.domain.Order;
public interface OrderManager {
    void sendPassword(Order order,String reciveAddress);
    void sendCode(String code,String reciveAddress);
}