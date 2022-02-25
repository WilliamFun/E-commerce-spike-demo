package org.example.service;

import org.example.error.BussinessException;
import org.example.service.model.OrderModel;

public interface OrderService {
    OrderModel createOrder(Integer userId,Integer itemId,Integer amount) throws BussinessException;
}
