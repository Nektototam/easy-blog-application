package org.nektototam.easyblog.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

/**
 * A TaggedItems.
 */
@Entity
@Table(name = "tagged_items")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@org.springframework.data.elasticsearch.annotations.Document(indexName = "taggeditems")
@SuppressWarnings("common-java:DuplicatedBlocks")
public class TaggedItems implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    @Column(name = "id")
    private Long id;

    @NotNull
    @Column(name = "item_type", nullable = false)
    @org.springframework.data.elasticsearch.annotations.Field(type = org.springframework.data.elasticsearch.annotations.FieldType.Text)
    private String itemType;

    @ManyToOne(fetch = FetchType.LAZY)
    private Tags tag;

    @ManyToOne(fetch = FetchType.LAZY)
    private ItemTypes item;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "rel_tagged_items__page",
        joinColumns = @JoinColumn(name = "tagged_items_id"),
        inverseJoinColumns = @JoinColumn(name = "page_id")
    )
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    @JsonIgnoreProperties(value = { "authors", "tags", "comments" }, allowSetters = true)
    private Set<Pages> pages = new HashSet<>();

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "rel_tagged_items__post",
        joinColumns = @JoinColumn(name = "tagged_items_id"),
        inverseJoinColumns = @JoinColumn(name = "post_id")
    )
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    @JsonIgnoreProperties(value = { "authors", "tags", "comments" }, allowSetters = true)
    private Set<Posts> posts = new HashSet<>();

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public Long getId() {
        return this.id;
    }

    public TaggedItems id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getItemType() {
        return this.itemType;
    }

    public TaggedItems itemType(String itemType) {
        this.setItemType(itemType);
        return this;
    }

    public void setItemType(String itemType) {
        this.itemType = itemType;
    }

    public Tags getTag() {
        return this.tag;
    }

    public void setTag(Tags tags) {
        this.tag = tags;
    }

    public TaggedItems tag(Tags tags) {
        this.setTag(tags);
        return this;
    }

    public ItemTypes getItem() {
        return this.item;
    }

    public void setItem(ItemTypes itemTypes) {
        this.item = itemTypes;
    }

    public TaggedItems item(ItemTypes itemTypes) {
        this.setItem(itemTypes);
        return this;
    }

    public Set<Pages> getPages() {
        return this.pages;
    }

    public void setPages(Set<Pages> pages) {
        this.pages = pages;
    }

    public TaggedItems pages(Set<Pages> pages) {
        this.setPages(pages);
        return this;
    }

    public TaggedItems addPage(Pages pages) {
        this.pages.add(pages);
        return this;
    }

    public TaggedItems removePage(Pages pages) {
        this.pages.remove(pages);
        return this;
    }

    public Set<Posts> getPosts() {
        return this.posts;
    }

    public void setPosts(Set<Posts> posts) {
        this.posts = posts;
    }

    public TaggedItems posts(Set<Posts> posts) {
        this.setPosts(posts);
        return this;
    }

    public TaggedItems addPost(Posts posts) {
        this.posts.add(posts);
        return this;
    }

    public TaggedItems removePost(Posts posts) {
        this.posts.remove(posts);
        return this;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof TaggedItems)) {
            return false;
        }
        return getId() != null && getId().equals(((TaggedItems) o).getId());
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "TaggedItems{" +
            "id=" + getId() +
            ", itemType='" + getItemType() + "'" +
            "}";
    }
}
