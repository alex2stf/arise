package com.arise.jentil.storage.entities;

import java.util.Date;
import java.util.Set;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import org.hibernate.annotations.Type;

@Entity
@Table(name = "ent_folders")
public class FolderEntity {

    @Column(name = "creation_date")
    private Date creationDate;

    @OneToOne
    private FileEntity currentFile;

    @Column(name = "deleted")
    @Type(type = "org.hibernate.type.NumericBooleanType")
    private boolean deleted;

    @Column(name = "description")
    private String description;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator="dfolder_sequence_generator")
    @SequenceGenerator(name = "dfolder_sequence_generator", sequenceName="dfolder_sequence", allocationSize=1)
    @Column(name = "id")
    private Long id;

    @OneToOne(
        cascade = CascadeType.ALL,
        fetch = FetchType.EAGER
    )
    @JoinColumn(name = "parent_id")
    private FolderEntity parent;

    @Column(name = "phase")
    private String phase;

    @Column(name = "product_code")
    private String productCode;

    @Column(name = "product_id")
    private Long productId;

    @Column(name = "product_name")
    private String productName;

    @Column(name = "source")
    private String source;

    @Column(name = "status")
    private String status;

    @OneToMany(
        mappedBy = "folder",
        fetch = FetchType.LAZY,
        orphanRemoval = true
    )
    private Set<FolderTag> tags;

    @Column(name = "type")
    private String type;


    public Date getCreationDate() {
        return this.creationDate;
    }

    public FolderEntity setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
        return this;
    }

    public FileEntity getCurrentFile() {
        return this.currentFile;
    }

    public FolderEntity setCurrentFile(FileEntity currentFile) {
        this.currentFile = currentFile;
        return this;
    }

    public boolean getDeleted() {
        return this.deleted;
    }

    public FolderEntity setDeleted(boolean deleted) {
        this.deleted = deleted;
        return this;
    }

    public String getDescription() {
        return this.description;
    }

    public FolderEntity setDescription(String description) {
        this.description = description;
        return this;
    }

    public Long getId() {
        return this.id;
    }

    public FolderEntity setId(Long id) {
        this.id = id;
        return this;
    }

    public FolderEntity getParent() {
        return this.parent;
    }

    public FolderEntity setParent(FolderEntity parent) {
        this.parent = parent;
        return this;
    }

    public String getPhase() {
        return this.phase;
    }

    public FolderEntity setPhase(String phase) {
        this.phase = phase;
        return this;
    }

    public String getProductCode() {
        return this.productCode;
    }

    public FolderEntity setProductCode(String productCode) {
        this.productCode = productCode;
        return this;
    }

    public Long getProductId() {
        return this.productId;
    }

    public FolderEntity setProductId(Long productId) {
        this.productId = productId;
        return this;
    }

    public String getProductName() {
        return this.productName;
    }

    public FolderEntity setProductName(String productName) {
        this.productName = productName;
        return this;
    }

    public String getSource() {
        return this.source;
    }

    public FolderEntity setSource(String source) {
        this.source = source;
        return this;
    }

    public String getStatus() {
        return this.status;
    }

    public FolderEntity setStatus(String status) {
        this.status = status;
        return this;
    }

    public Set<FolderTag> getTags() {
        return this.tags;
    }

    public FolderEntity setTags(Set<FolderTag> tags) {
        this.tags = tags;
        return this;
    }

    public String getType() {
        return this.type;
    }

    public FolderEntity setType(String type) {
        this.type = type;
        return this;
    }


}
