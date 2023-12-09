package org.nektototam.easyblog.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.Set;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.nektototam.easyblog.domain.enumeration.Status;

/**
 * A Comments.
 */
@Entity
@Table(name = "comments")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@org.springframework.data.elasticsearch.annotations.Document(indexName = "comments")
@SuppressWarnings("common-java:DuplicatedBlocks")
public class Comments implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    @Column(name = "id")
    private Long id;

    @Lob
    @Column(name = "content", nullable = false)
    @org.springframework.data.elasticsearch.annotations.Field(type = org.springframework.data.elasticsearch.annotations.FieldType.Text)
    private String content;

    @NotNull
    @Column(name = "created_at", nullable = false)
    private ZonedDateTime createdAt;

    @NotNull
    @Column(name = "updated_at", nullable = false)
    private ZonedDateTime updatedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    @org.springframework.data.elasticsearch.annotations.Field(type = org.springframework.data.elasticsearch.annotations.FieldType.Keyword)
    private Status status;

    @ManyToOne(fetch = FetchType.LAZY)
    private Authors author;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "rel_comments__post",
        joinColumns = @JoinColumn(name = "comments_id"),
        inverseJoinColumns = @JoinColumn(name = "post_id")
    )
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    @JsonIgnoreProperties(value = { "authors", "tags", "comments" }, allowSetters = true)
    private Set<Posts> posts = new HashSet<>();

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "rel_comments__page",
        joinColumns = @JoinColumn(name = "comments_id"),
        inverseJoinColumns = @JoinColumn(name = "page_id")
    )
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    @JsonIgnoreProperties(value = { "authors", "tags", "comments" }, allowSetters = true)
    private Set<Pages> pages = new HashSet<>();

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "rel_comments__parent",
        joinColumns = @JoinColumn(name = "comments_id"),
        inverseJoinColumns = @JoinColumn(name = "parent_id")
    )
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    @JsonIgnoreProperties(value = { "author", "posts", "pages", "parents", "children" }, allowSetters = true)
    private Set<Comments> parents = new HashSet<>();

    @ManyToMany(fetch = FetchType.LAZY, mappedBy = "parents")
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    @org.springframework.data.annotation.Transient
    @JsonIgnoreProperties(value = { "author", "posts", "pages", "parents", "children" }, allowSetters = true)
    private Set<Comments> children = new HashSet<>();

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public Long getId() {
        return this.id;
    }

    public Comments id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getContent() {
        return this.content;
    }

    public Comments content(String content) {
        this.setContent(content);
        return this;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public ZonedDateTime getCreatedAt() {
        return this.createdAt;
    }

    public Comments createdAt(ZonedDateTime createdAt) {
        this.setCreatedAt(createdAt);
        return this;
    }

    public void setCreatedAt(ZonedDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public ZonedDateTime getUpdatedAt() {
        return this.updatedAt;
    }

    public Comments updatedAt(ZonedDateTime updatedAt) {
        this.setUpdatedAt(updatedAt);
        return this;
    }

    public void setUpdatedAt(ZonedDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Status getStatus() {
        return this.status;
    }

    public Comments status(Status status) {
        this.setStatus(status);
        return this;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public Authors getAuthor() {
        return this.author;
    }

    public void setAuthor(Authors authors) {
        this.author = authors;
    }

    public Comments author(Authors authors) {
        this.setAuthor(authors);
        return this;
    }

    public Set<Posts> getPosts() {
        return this.posts;
    }

    public void setPosts(Set<Posts> posts) {
        this.posts = posts;
    }

    public Comments posts(Set<Posts> posts) {
        this.setPosts(posts);
        return this;
    }

    public Comments addPost(Posts posts) {
        this.posts.add(posts);
        return this;
    }

    public Comments removePost(Posts posts) {
        this.posts.remove(posts);
        return this;
    }

    public Set<Pages> getPages() {
        return this.pages;
    }

    public void setPages(Set<Pages> pages) {
        this.pages = pages;
    }

    public Comments pages(Set<Pages> pages) {
        this.setPages(pages);
        return this;
    }

    public Comments addPage(Pages pages) {
        this.pages.add(pages);
        return this;
    }

    public Comments removePage(Pages pages) {
        this.pages.remove(pages);
        return this;
    }

    public Set<Comments> getParents() {
        return this.parents;
    }

    public void setParents(Set<Comments> comments) {
        this.parents = comments;
    }

    public Comments parents(Set<Comments> comments) {
        this.setParents(comments);
        return this;
    }

    public Comments addParent(Comments comments) {
        this.parents.add(comments);
        return this;
    }

    public Comments removeParent(Comments comments) {
        this.parents.remove(comments);
        return this;
    }

    public Set<Comments> getChildren() {
        return this.children;
    }

    public void setChildren(Set<Comments> comments) {
        if (this.children != null) {
            this.children.forEach(i -> i.removeParent(this));
        }
        if (comments != null) {
            comments.forEach(i -> i.addParent(this));
        }
        this.children = comments;
    }

    public Comments children(Set<Comments> comments) {
        this.setChildren(comments);
        return this;
    }

    public Comments addChild(Comments comments) {
        this.children.add(comments);
        comments.getParents().add(this);
        return this;
    }

    public Comments removeChild(Comments comments) {
        this.children.remove(comments);
        comments.getParents().remove(this);
        return this;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Comments)) {
            return false;
        }
        return getId() != null && getId().equals(((Comments) o).getId());
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "Comments{" +
            "id=" + getId() +
            ", content='" + getContent() + "'" +
            ", createdAt='" + getCreatedAt() + "'" +
            ", updatedAt='" + getUpdatedAt() + "'" +
            ", status='" + getStatus() + "'" +
            "}";
    }
}
