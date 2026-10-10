package org.utn.ba.order.services.imp;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.utn.ba.events.ClearCartEvent;
import org.utn.ba.events.OrderConfirmationEvent;
import org.utn.ba.order.client.ProductClient;
import org.utn.ba.order.client.ShoppingCartClient;
import org.utn.ba.order.client.dto.ProductOutputDTO;
import org.utn.ba.order.client.dto.ShoppingCartOutputDTO;
import org.utn.ba.order.configuration.KafkaTopicProperties;
import org.utn.ba.order.dto.OrderOutputDTO;
import org.utn.ba.order.dto.UserDetailsDTO;
import org.utn.ba.order.entities.models.Order;
import org.utn.ba.order.entities.models.OrderItem;
import org.utn.ba.order.mappers.OrderConfirmationEventMapper;
import org.utn.ba.order.mappers.OrderMapper;
import org.utn.ba.order.mappers.UserDetailsMapper;
import org.utn.ba.order.services.EventPublisher;
import org.utn.ba.order.services.IOrderService;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;
import org.utn.ba.order.entities.repositories.OrderRepository;

@Service
public class OrderService implements IOrderService {

  @Autowired
  private OrderRepository orderRepository;

  @Autowired
  private EventPublisher eventPublisher;

  @Autowired
  private KafkaTopicProperties topicProperties;

  @Autowired
  private ProductClient productClient;

  @Autowired
  private ShoppingCartClient cartClient;

  @Override
  public List<OrderOutputDTO> findAll() {

    return orderRepository.findAll()
        .stream()
        .map(OrderMapper::createFrom).
        collect(Collectors.toList());
  }


  @Override
  public OrderOutputDTO findById(Long id) {

    return orderRepository.findById(id)
        .map(OrderMapper::createFrom)
        .orElse(null);
  }

  @Override
  @CircuitBreaker(name = "product", fallbackMethod = "fallbackCreateOrderWithProduct")
  @Transactional
  public OrderOutputDTO createOrder(UserDetailsDTO userDetailsDTO) {
    ShoppingCartOutputDTO cart = cartClient.getMyCart();
    if (cart == null || cart.getItems().isEmpty()) {
      return OrderOutputDTO.builder()
          .description("Cannot create an order from an empty cart.")
          .build();
    }

    Order newOrder = new Order();
    newOrder.setDate(LocalDate.now());
    newOrder.setUserDetails(UserDetailsMapper.createFrom(userDetailsDTO));
    List<OrderItem> itemList = cart.getItems()
        .stream().map(i -> {
          ProductOutputDTO product = productClient.getProductById(i.getProductId()).getBody();
          return OrderItem.builder()
              .productId(product.getId())
              .price(product.getPrice())
              .amount(i.getAmount())
              .imageUrl(product.getImageUrl())
              .order(newOrder)
              .productName(product.getName())
              .build();
        })
        .toList();

    itemList.forEach(newOrder::addOrderItem);

    newOrder.calculateFinalPrice();
    this.orderRepository.save(newOrder);

    OrderConfirmationEvent confirmationEvent = OrderConfirmationEventMapper.fromOrder(newOrder);
    eventPublisher.publish(
        topicProperties.getOrderConfirmations(),
        String.valueOf(newOrder.getId()),
        confirmationEvent
    );

    ClearCartEvent clearCartEvent = new ClearCartEvent(userDetailsDTO.userId());
    eventPublisher.publish(
        topicProperties.getClearCart(),
        userDetailsDTO.userId(),
        clearCartEvent
    );

    return OrderMapper.createFrom(newOrder);
  }

  public OrderOutputDTO fallbackCreateOrderWithProduct(Throwable t) {

    return OrderOutputDTO.builder()
        .description("Failed to create Order as " +
            "Product Service or Cart service may be down, error -> " + t.getMessage())
        .build();
  }
}
