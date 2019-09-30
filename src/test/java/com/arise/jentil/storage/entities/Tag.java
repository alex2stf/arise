package com.arise.jentil.storage.entities;

import java.util.Set;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import org.hibernate.annotations.NaturalId;

@Entity
@Table(name = "store_tags")
public class Tag {

    @OneToMany(
        mappedBy = "tag",
        cascade = CascadeType.ALL,
        orphanRemoval = true
    )
    private Set<FileTag> fileTags;

    @OneToMany(
        mappedBy = "tag",
        cascade = CascadeType.ALL,
        orphanRemoval = true
    )
    private Set<FolderTag> folderTags;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator="stag_seq_gen")
    @SequenceGenerator(name = "stag_seq_gen", sequenceName="storage_tag_sequence", allocationSize=1)
    @Column(name = "id")
    private Long id;

    @NaturalId
    @Column(name = "value")
    private String value;


    public Set<FileTag> getFileTags() {
        return this.fileTags;
    }

    public Tag setFileTags(Set<FileTag> fileTags) {
        this.fileTags = fileTags;
        return this;
    }

    public Set<FolderTag> getFolderTags() {
        return this.folderTags;
    }

    public Tag setFolderTags(Set<FolderTag> folderTags) {
        this.folderTags = folderTags;
        return this;
    }

    public Long getId() {
        return this.id;
    }

    public Tag setId(Long id) {
        this.id = id;
        return this;
    }

    public String getValue() {
        return this.value;
    }

    public Tag setValue(String value) {
        this.value = value;
        return this;
    }


}
