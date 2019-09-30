package com.arise.jentil.embeddables;

import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Embeddable;

@Embeddable
public class InfoID implements Serializable {

    @Column(name = "entity_id")
    private Long entityId;

    @Column(name = "info_id")
    private Long infoId;


    public Long getEntityId() {
        return this.entityId;
    }

    public InfoID setEntityId(Long entityId) {
        this.entityId = entityId;
        return this;
    }

    public Long getInfoId() {
        return this.infoId;
    }

    public InfoID setInfoId(Long infoId) {
        this.infoId = infoId;
        return this;
    }


}
