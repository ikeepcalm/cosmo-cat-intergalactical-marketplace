package net.cosmocat.marketplace.database.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

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

  @Column(name = "created_at")
  private LocalDateTime createdAt;

  @PrePersist
  protected void onCreate() {
    createdAt = LocalDateTime.now();
  }
}
