import { Component, OnInit } from '@angular/core';
import { HttpResponse } from '@angular/common/http';
import { ActivatedRoute } from '@angular/router';
import { Observable } from 'rxjs';
import { finalize, map } from 'rxjs/operators';

import SharedModule from 'app/shared/shared.module';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';

import { ITags } from 'app/entities/tags/tags.model';
import { TagsService } from 'app/entities/tags/service/tags.service';
import { IItemTypes } from 'app/entities/item-types/item-types.model';
import { ItemTypesService } from 'app/entities/item-types/service/item-types.service';
import { IPages } from 'app/entities/pages/pages.model';
import { PagesService } from 'app/entities/pages/service/pages.service';
import { IPosts } from 'app/entities/posts/posts.model';
import { PostsService } from 'app/entities/posts/service/posts.service';
import { TaggedItemsService } from '../service/tagged-items.service';
import { ITaggedItems } from '../tagged-items.model';
import { TaggedItemsFormService, TaggedItemsFormGroup } from './tagged-items-form.service';

@Component({
  standalone: true,
  selector: 'jhi-tagged-items-update',
  templateUrl: './tagged-items-update.component.html',
  imports: [SharedModule, FormsModule, ReactiveFormsModule],
})
export class TaggedItemsUpdateComponent implements OnInit {
  isSaving = false;
  taggedItems: ITaggedItems | null = null;

  tagsSharedCollection: ITags[] = [];
  itemTypesSharedCollection: IItemTypes[] = [];
  pagesSharedCollection: IPages[] = [];
  postsSharedCollection: IPosts[] = [];

  editForm: TaggedItemsFormGroup = this.taggedItemsFormService.createTaggedItemsFormGroup();

  constructor(
    protected taggedItemsService: TaggedItemsService,
    protected taggedItemsFormService: TaggedItemsFormService,
    protected tagsService: TagsService,
    protected itemTypesService: ItemTypesService,
    protected pagesService: PagesService,
    protected postsService: PostsService,
    protected activatedRoute: ActivatedRoute,
  ) {}

  compareTags = (o1: ITags | null, o2: ITags | null): boolean => this.tagsService.compareTags(o1, o2);

  compareItemTypes = (o1: IItemTypes | null, o2: IItemTypes | null): boolean => this.itemTypesService.compareItemTypes(o1, o2);

  comparePages = (o1: IPages | null, o2: IPages | null): boolean => this.pagesService.comparePages(o1, o2);

  comparePosts = (o1: IPosts | null, o2: IPosts | null): boolean => this.postsService.comparePosts(o1, o2);

  ngOnInit(): void {
    this.activatedRoute.data.subscribe(({ taggedItems }) => {
      this.taggedItems = taggedItems;
      if (taggedItems) {
        this.updateForm(taggedItems);
      }

      this.loadRelationshipsOptions();
    });
  }

  previousState(): void {
    window.history.back();
  }

  save(): void {
    this.isSaving = true;
    const taggedItems = this.taggedItemsFormService.getTaggedItems(this.editForm);
    if (taggedItems.id !== null) {
      this.subscribeToSaveResponse(this.taggedItemsService.update(taggedItems));
    } else {
      this.subscribeToSaveResponse(this.taggedItemsService.create(taggedItems));
    }
  }

  protected subscribeToSaveResponse(result: Observable<HttpResponse<ITaggedItems>>): void {
    result.pipe(finalize(() => this.onSaveFinalize())).subscribe({
      next: () => this.onSaveSuccess(),
      error: () => this.onSaveError(),
    });
  }

  protected onSaveSuccess(): void {
    this.previousState();
  }

  protected onSaveError(): void {
    // Api for inheritance.
  }

  protected onSaveFinalize(): void {
    this.isSaving = false;
  }

  protected updateForm(taggedItems: ITaggedItems): void {
    this.taggedItems = taggedItems;
    this.taggedItemsFormService.resetForm(this.editForm, taggedItems);

    this.tagsSharedCollection = this.tagsService.addTagsToCollectionIfMissing<ITags>(this.tagsSharedCollection, taggedItems.tag);
    this.itemTypesSharedCollection = this.itemTypesService.addItemTypesToCollectionIfMissing<IItemTypes>(
      this.itemTypesSharedCollection,
      taggedItems.item,
    );
    this.pagesSharedCollection = this.pagesService.addPagesToCollectionIfMissing<IPages>(
      this.pagesSharedCollection,
      ...(taggedItems.pages ?? []),
    );
    this.postsSharedCollection = this.postsService.addPostsToCollectionIfMissing<IPosts>(
      this.postsSharedCollection,
      ...(taggedItems.posts ?? []),
    );
  }

  protected loadRelationshipsOptions(): void {
    this.tagsService
      .query()
      .pipe(map((res: HttpResponse<ITags[]>) => res.body ?? []))
      .pipe(map((tags: ITags[]) => this.tagsService.addTagsToCollectionIfMissing<ITags>(tags, this.taggedItems?.tag)))
      .subscribe((tags: ITags[]) => (this.tagsSharedCollection = tags));

    this.itemTypesService
      .query()
      .pipe(map((res: HttpResponse<IItemTypes[]>) => res.body ?? []))
      .pipe(
        map((itemTypes: IItemTypes[]) =>
          this.itemTypesService.addItemTypesToCollectionIfMissing<IItemTypes>(itemTypes, this.taggedItems?.item),
        ),
      )
      .subscribe((itemTypes: IItemTypes[]) => (this.itemTypesSharedCollection = itemTypes));

    this.pagesService
      .query()
      .pipe(map((res: HttpResponse<IPages[]>) => res.body ?? []))
      .pipe(map((pages: IPages[]) => this.pagesService.addPagesToCollectionIfMissing<IPages>(pages, ...(this.taggedItems?.pages ?? []))))
      .subscribe((pages: IPages[]) => (this.pagesSharedCollection = pages));

    this.postsService
      .query()
      .pipe(map((res: HttpResponse<IPosts[]>) => res.body ?? []))
      .pipe(map((posts: IPosts[]) => this.postsService.addPostsToCollectionIfMissing<IPosts>(posts, ...(this.taggedItems?.posts ?? []))))
      .subscribe((posts: IPosts[]) => (this.postsSharedCollection = posts));
  }
}
