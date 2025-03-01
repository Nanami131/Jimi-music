package com.example.duolcmusic.service;

import com.example.duolcmusic.model.domain.Order;
public interface OrderManager {
    void sendPassword(Order order,String reciveAddress);
    void sendCode(String code,String reciveAddress);
}