package com.arise.jentil.storage.entities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

@Entity
@Table(name = "store_props")
public class Property {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator="fldr_seq_gen")
    @SequenceGenerator(name = "fldr_seq_gen", sequenceName="folder_sequence", allocationSize=1)
    @Column(name = "id")
    private Long id;

    @Column(name = "p_key")
    private String key;

    @Column(name = "p_val")
    private String values;


    public Long getId() {
        return this.id;
    }

    public Property setId(Long id) {
        this.id = id;
        return this;
    }

    public String getKey() {
        return this.key;
    }

    public Property setKey(String key) {
        this.key = key;
        return this;
    }

    public String getValues() {
        return this.values;
    }

    public Property setValues(String values) {
        this.values = values;
        return this;
    }


}
