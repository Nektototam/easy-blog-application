package org.nektototam.easyblog.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

/**
 * A Users.
 */
@Entity
@Table(name = "users")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@org.springframework.data.elasticsearch.annotations.Document(indexName = "users")
@SuppressWarnings("common-java:DuplicatedBlocks")
public class Users implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    @Column(name = "id")
    private Long id;

    @Column(name = "name")
    @org.springframework.data.elasticsearch.annotations.Field(type = org.springframework.data.elasticsearch.annotations.FieldType.Text)
    private String name;

    @Column(name = "email")
    @org.springframework.data.elasticsearch.annotations.Field(type = org.springframework.data.elasticsearch.annotations.FieldType.Text)
    private String email;

    @ManyToMany(fetch = FetchType.LAZY, mappedBy = "authors")
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    @org.springframework.data.annotation.Transient
    @JsonIgnoreProperties(value = { "authors", "tags", "comments" }, allowSetters = true)
    private Set<Pages> pages = new HashSet<>();

    @ManyToMany(fetch = FetchType.LAZY, mappedBy = "authors")
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    @org.springframework.data.annotation.Transient
    @JsonIgnoreProperties(value = { "authors", "tags", "comments" }, allowSetters = true)
    private Set<Posts> posts = new HashSet<>();

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public Long getId() {
        return this.id;
    }

    public Users id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return this.name;
    }

    public Users name(String name) {
        this.setName(name);
        return this;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return this.email;
    }

    public Users email(String email) {
        this.setEmail(email);
        return this;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Set<Pages> getPages() {
        return this.pages;
    }

    public void setPages(Set<Pages> pages) {
        if (this.pages != null) {
            this.pages.forEach(i -> i.removeAuthor(this));
        }
        if (pages != null) {
            pages.forEach(i -> i.addAuthor(this));
        }
        this.pages = pages;
    }

    public Users pages(Set<Pages> pages) {
        this.setPages(pages);
        return this;
    }

    public Users addPage(Pages pages) {
        this.pages.add(pages);
        pages.getAuthors().add(this);
        return this;
    }

    public Users removePage(Pages pages) {
        this.pages.remove(pages);
        pages.getAuthors().remove(this);
        return this;
    }

    public Set<Posts> getPosts() {
        return this.posts;
    }

    public void setPosts(Set<Posts> posts) {
        if (this.posts != null) {
            this.posts.forEach(i -> i.removeAuthor(this));
        }
        if (posts != null) {
            posts.forEach(i -> i.addAuthor(this));
        }
        this.posts = posts;
    }

    public Users posts(Set<Posts> posts) {
        this.setPosts(posts);
        return this;
    }

    public Users addPost(Posts posts) {
        this.posts.add(posts);
        posts.getAuthors().add(this);
        return this;
    }

    public Users removePost(Posts posts) {
        this.posts.remove(posts);
        posts.getAuthors().remove(this);
        return this;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Users)) {
            return false;
        }
        return getId() != null && getId().equals(((Users) o).getId());
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "Users{" +
            "id=" + getId() +
            ", name='" + getName() + "'" +
            ", email='" + getEmail() + "'" +
            "}";
    }
}
