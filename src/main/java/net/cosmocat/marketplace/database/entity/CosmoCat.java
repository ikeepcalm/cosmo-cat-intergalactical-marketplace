package net.cosmocat.marketplace.database.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity(name = "cosmo_cats")
public class CosmoCat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "breed")
    private String breed;

    @Column(name = "color")
    private String color;

    @Column(name = "age")
    private Integer age;

    @Column(name = "image_url")
    private String image;

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;
}
