package org.utn.ba.order.services;

import org.utn.ba.order.entities.models.Order;

public interface IOrderCreationTxService {
  Order createAndSaveOrder(Order order);
}
