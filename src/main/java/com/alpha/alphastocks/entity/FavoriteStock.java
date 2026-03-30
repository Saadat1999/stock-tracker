package com.alpha.alphastocks.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "favorite_stocks")
@Data
@NoArgsConstructor(force = true)
@AllArgsConstructor
@Builder
public class FavoriteStock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private final Long id;

    @Column(nullable = false, unique = true)
    private final String symbol;
}
