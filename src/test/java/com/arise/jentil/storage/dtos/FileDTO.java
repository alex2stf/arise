package com.arise.jentil.storage.dtos;

import com.arise.jentil.storage.entities.FileEntity;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.Map;
import java.util.Set;
import javax.persistence.Transient;

public class FileDTO {

    private String serviceId;

    private Long id;

    private String name;

    private Long parentId;

    private String path;

    private String mimeType;

    private transient Map<String, String> props;

    private String notes;

    private Long size;

    private transient Set<String> tags;

    private boolean deleted;

    private String status;


    //static mappers
    @SuppressWarnings("Duplicates")
    public static void mapDto(FileDTO fileDTO, FileEntity fileEntity) {
        fileDTO.setDeleted(fileEntity.getDeleted());
        fileDTO.setId(fileEntity.getId());
        fileDTO.setMimeType(fileEntity.getMimeType());
        fileDTO.setName(fileEntity.getName());
        fileDTO.setNotes(fileEntity.getNotes());
        fileDTO.setParentId(fileEntity.getParentId());
        fileDTO.setPath(fileEntity.getPath());
        fileDTO.setProps(fileEntity.getProps());
        fileDTO.setServiceId(fileEntity.getServiceId());
        fileDTO.setSize(fileEntity.getSize());
        fileDTO.setStatus(fileEntity.getStatus());
        fileDTO.setTags(fileEntity.getTags());
    }

    @SuppressWarnings("Duplicates")
    public static void mapSource(FileEntity fileEntity, FileDTO fileDTO) {
        fileEntity.setDeleted(fileDTO.getDeleted());
        fileEntity.setId(fileDTO.getId());
        fileEntity.setMimeType(fileDTO.getMimeType());
        fileEntity.setName(fileDTO.getName());
        fileEntity.setNotes(fileDTO.getNotes());
        fileEntity.setParentId(fileDTO.getParentId());
        fileEntity.setPath(fileDTO.getPath());
        fileEntity.setProps(fileDTO.getProps());
        fileEntity.setServiceId(fileDTO.getServiceId());
        fileEntity.setSize(fileDTO.getSize());
        fileEntity.setStatus(fileDTO.getStatus());
        fileEntity.setTags(fileDTO.getTags());
    }

    public static FileDTO fromFileEntity(FileEntity fileEntity) {
        FileDTO fileDTO = new FileDTO();
        FileDTO.mapDto(fileDTO, fileEntity);
        return fileDTO;
    }


    public String getName() {
        return this.name;
    }

    public FileDTO setName(String name) {
        this.name = name;
        return this;
    }

    public String getMimeType() {
        return this.mimeType;
    }

    public FileDTO setMimeType(String mimeType) {
        this.mimeType = mimeType;
        return this;
    }

    public String getPath() {
        return this.path;
    }

    public FileDTO setPath(String path) {
        this.path = path;
        return this;
    }

    public String getStatus() {
        return this.status;
    }

    public FileDTO setStatus(String status) {
        this.status = status;
        return this;
    }

    public Long getParentId() {
        return this.parentId;
    }

    public FileDTO setParentId(Long parentId) {
        this.parentId = parentId;
        return this;
    }

    @Transient
    public Set<String> getTags() {
        return this.tags;
    }

    @Transient
    public FileDTO setTags(Set<String> tags) {
        this.tags = tags;
        return this;
    }

    public boolean getDeleted() {
        return this.deleted;
    }

    public FileDTO setDeleted(boolean deleted) {
        this.deleted = deleted;
        return this;
    }

    public String getNotes() {
        return this.notes;
    }

    public FileDTO setNotes(String notes) {
        this.notes = notes;
        return this;
    }

    public Long getSize() {
        return this.size;
    }

    public FileDTO setSize(Long size) {
        this.size = size;
        return this;
    }

    public String getServiceId() {
        return this.serviceId;
    }

    public FileDTO setServiceId(String serviceId) {
        this.serviceId = serviceId;
        return this;
    }

    public Long getId() {
        return this.id;
    }

    public FileDTO setId(Long id) {
        this.id = id;
        return this;
    }

    @Transient
    public Map<String, String> getProps() {
        return this.props;
    }

    @Transient
    public FileDTO setProps(Map<String, String> props) {
        this.props = props;
        return this;
    }


    //composition getters
    @JsonIgnore
    public final FileEntity toFileEntity() {
        FileEntity fileEntity = new FileEntity();
        this.mapSource(fileEntity, this);
        return fileEntity;
    }

}
