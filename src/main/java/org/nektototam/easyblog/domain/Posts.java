package org.nektototam.easyblog.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.Set;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.nektototam.easyblog.domain.enumeration.Status;
import org.nektototam.easyblog.domain.enumeration.Visibility;

/**
 * A Posts.
 */
@Entity
@Table(name = "posts")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@org.springframework.data.elasticsearch.annotations.Document(indexName = "posts")
@SuppressWarnings("common-java:DuplicatedBlocks")
public class Posts implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    @Column(name = "id")
    private Long id;

    @Column(name = "title")
    @org.springframework.data.elasticsearch.annotations.Field(type = org.springframework.data.elasticsearch.annotations.FieldType.Text)
    private String title;

    @Column(name = "slug")
    @org.springframework.data.elasticsearch.annotations.Field(type = org.springframework.data.elasticsearch.annotations.FieldType.Text)
    private String slug;

    @Lob
    @Column(name = "html")
    @org.springframework.data.elasticsearch.annotations.Field(type = org.springframework.data.elasticsearch.annotations.FieldType.Text)
    private String html;

    @Column(name = "created_at")
    private ZonedDateTime createdAt;

    @Column(name = "updated_at")
    private ZonedDateTime updatedAt;

    @Column(name = "published_at")
    private ZonedDateTime publishedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    @org.springframework.data.elasticsearch.annotations.Field(type = org.springframework.data.elasticsearch.annotations.FieldType.Keyword)
    private Status status;

    @Enumerated(EnumType.STRING)
    @Column(name = "visibility")
    @org.springframework.data.elasticsearch.annotations.Field(type = org.springframework.data.elasticsearch.annotations.FieldType.Keyword)
    private Visibility visibility;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "rel_posts__author",
        joinColumns = @JoinColumn(name = "posts_id"),
        inverseJoinColumns = @JoinColumn(name = "author_id")
    )
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    @JsonIgnoreProperties(value = { "pages", "posts" }, allowSetters = true)
    private Set<Users> authors = new HashSet<>();

    @ManyToMany(fetch = FetchType.LAZY, mappedBy = "posts")
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    @org.springframework.data.annotation.Transient
    @JsonIgnoreProperties(value = { "tag", "item", "pages", "posts" }, allowSetters = true)
    private Set<TaggedItems> tags = new HashSet<>();

    @ManyToMany(fetch = FetchType.LAZY, mappedBy = "posts")
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    @org.springframework.data.annotation.Transient
    @JsonIgnoreProperties(value = { "author", "posts", "pages", "parents", "children" }, allowSetters = true)
    private Set<Comments> comments = new HashSet<>();

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public Long getId() {
        return this.id;
    }

    public Posts id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return this.title;
    }

    public Posts title(String title) {
        this.setTitle(title);
        return this;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSlug() {
        return this.slug;
    }

    public Posts slug(String slug) {
        this.setSlug(slug);
        return this;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public String getHtml() {
        return this.html;
    }

    public Posts html(String html) {
        this.setHtml(html);
        return this;
    }

    public void setHtml(String html) {
        this.html = html;
    }

    public ZonedDateTime getCreatedAt() {
        return this.createdAt;
    }

    public Posts createdAt(ZonedDateTime createdAt) {
        this.setCreatedAt(createdAt);
        return this;
    }

    public void setCreatedAt(ZonedDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public ZonedDateTime getUpdatedAt() {
        return this.updatedAt;
    }

    public Posts updatedAt(ZonedDateTime updatedAt) {
        this.setUpdatedAt(updatedAt);
        return this;
    }

    public void setUpdatedAt(ZonedDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public ZonedDateTime getPublishedAt() {
        return this.publishedAt;
    }

    public Posts publishedAt(ZonedDateTime publishedAt) {
        this.setPublishedAt(publishedAt);
        return this;
    }

    public void setPublishedAt(ZonedDateTime publishedAt) {
        this.publishedAt = publishedAt;
    }

    public Status getStatus() {
        return this.status;
    }

    public Posts status(Status status) {
        this.setStatus(status);
        return this;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public Visibility getVisibility() {
        return this.visibility;
    }

    public Posts visibility(Visibility visibility) {
        this.setVisibility(visibility);
        return this;
    }

    public void setVisibility(Visibility visibility) {
        this.visibility = visibility;
    }

    public Set<Users> getAuthors() {
        return this.authors;
    }

    public void setAuthors(Set<Users> users) {
        this.authors = users;
    }

    public Posts authors(Set<Users> users) {
        this.setAuthors(users);
        return this;
    }

    public Posts addAuthor(Users users) {
        this.authors.add(users);
        return this;
    }

    public Posts removeAuthor(Users users) {
        this.authors.remove(users);
        return this;
    }

    public Set<TaggedItems> getTags() {
        return this.tags;
    }

    public void setTags(Set<TaggedItems> taggedItems) {
        if (this.tags != null) {
            this.tags.forEach(i -> i.removePost(this));
        }
        if (taggedItems != null) {
            taggedItems.forEach(i -> i.addPost(this));
        }
        this.tags = taggedItems;
    }

    public Posts tags(Set<TaggedItems> taggedItems) {
        this.setTags(taggedItems);
        return this;
    }

    public Posts addTag(TaggedItems taggedItems) {
        this.tags.add(taggedItems);
        taggedItems.getPosts().add(this);
        return this;
    }

    public Posts removeTag(TaggedItems taggedItems) {
        this.tags.remove(taggedItems);
        taggedItems.getPosts().remove(this);
        return this;
    }

    public Set<Comments> getComments() {
        return this.comments;
    }

    public void setComments(Set<Comments> comments) {
        if (this.comments != null) {
            this.comments.forEach(i -> i.removePost(this));
        }
        if (comments != null) {
            comments.forEach(i -> i.addPost(this));
        }
        this.comments = comments;
    }

    public Posts comments(Set<Comments> comments) {
        this.setComments(comments);
        return this;
    }

    public Posts addComment(Comments comments) {
        this.comments.add(comments);
        comments.getPosts().add(this);
        return this;
    }

    public Posts removeComment(Comments comments) {
        this.comments.remove(comments);
        comments.getPosts().remove(this);
        return this;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Posts)) {
            return false;
        }
        return getId() != null && getId().equals(((Posts) o).getId());
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "Posts{" +
            "id=" + getId() +
            ", title='" + getTitle() + "'" +
            ", slug='" + getSlug() + "'" +
            ", html='" + getHtml() + "'" +
            ", createdAt='" + getCreatedAt() + "'" +
            ", updatedAt='" + getUpdatedAt() + "'" +
            ", publishedAt='" + getPublishedAt() + "'" +
            ", status='" + getStatus() + "'" +
            ", visibility='" + getVisibility() + "'" +
            "}";
    }
}
