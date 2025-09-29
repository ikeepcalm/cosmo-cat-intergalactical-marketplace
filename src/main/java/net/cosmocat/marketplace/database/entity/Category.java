package net.cosmocat.marketplace.database.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
@Entity( name = "categories")
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name")
    private String name;

    @Column(name = "description")
    private String description;

    @ElementCollection
    @Column(name = "tag")
    @CollectionTable(name = "category_tags", joinColumns = @JoinColumn(name = "category_id"))
    private List<String> tags;

    @OneToMany(mappedBy = "category")
    private List<Product> products;

}
