package com.awesome.awesomepizza.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * One persistent row represents the shared chef station.
 * Locking this row serializes assignments even across application instances.
 */
@Entity
@Table(name = "chef_station")
public class ChefStation {
    @Id
    private Long id;

    protected ChefStation() {}

    public ChefStation(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }
}
