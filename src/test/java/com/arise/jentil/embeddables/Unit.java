package com.arise.jentil.embeddables;

import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Embeddable;

@Embeddable
public class Unit implements Serializable {

    @Column(name = "code")
    private String code;

    @Column(name = "num_val")
    private Long value;


    public String getCode() {
        return this.code;
    }

    public Unit setCode(String code) {
        this.code = code;
        return this;
    }

    public Long getValue() {
        return this.value;
    }

    public Unit setValue(Long value) {
        this.value = value;
        return this;
    }


}
