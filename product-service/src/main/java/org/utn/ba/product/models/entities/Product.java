package org.utn.ba.product.models.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Table
@Entity
@Getter
@Setter
@NoArgsConstructor
public class Product {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        @Column
        private String name;

        @Column
        private Float price;

        @Column
        private String imageUrl;

        @Column(name = "idempotency_key", nullable = false, unique = true)
        private String idempotencyKey;

}


