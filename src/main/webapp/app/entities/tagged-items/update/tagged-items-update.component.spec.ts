import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpResponse } from '@angular/common/http';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { FormBuilder } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { RouterTestingModule } from '@angular/router/testing';
import { of, Subject, from } from 'rxjs';

import { ITags } from 'app/entities/tags/tags.model';
import { TagsService } from 'app/entities/tags/service/tags.service';
import { IItemTypes } from 'app/entities/item-types/item-types.model';
import { ItemTypesService } from 'app/entities/item-types/service/item-types.service';
import { IPages } from 'app/entities/pages/pages.model';
import { PagesService } from 'app/entities/pages/service/pages.service';
import { IPosts } from 'app/entities/posts/posts.model';
import { PostsService } from 'app/entities/posts/service/posts.service';
import { ITaggedItems } from '../tagged-items.model';
import { TaggedItemsService } from '../service/tagged-items.service';
import { TaggedItemsFormService } from './tagged-items-form.service';

import { TaggedItemsUpdateComponent } from './tagged-items-update.component';

describe('TaggedItems Management Update Component', () => {
  let comp: TaggedItemsUpdateComponent;
  let fixture: ComponentFixture<TaggedItemsUpdateComponent>;
  let activatedRoute: ActivatedRoute;
  let taggedItemsFormService: TaggedItemsFormService;
  let taggedItemsService: TaggedItemsService;
  let tagsService: TagsService;
  let itemTypesService: ItemTypesService;
  let pagesService: PagesService;
  let postsService: PostsService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule, RouterTestingModule.withRoutes([]), TaggedItemsUpdateComponent],
      providers: [
        FormBuilder,
        {
          provide: ActivatedRoute,
          useValue: {
            params: from([{}]),
          },
        },
      ],
    })
      .overrideTemplate(TaggedItemsUpdateComponent, '')
      .compileComponents();

    fixture = TestBed.createComponent(TaggedItemsUpdateComponent);
    activatedRoute = TestBed.inject(ActivatedRoute);
    taggedItemsFormService = TestBed.inject(TaggedItemsFormService);
    taggedItemsService = TestBed.inject(TaggedItemsService);
    tagsService = TestBed.inject(TagsService);
    itemTypesService = TestBed.inject(ItemTypesService);
    pagesService = TestBed.inject(PagesService);
    postsService = TestBed.inject(PostsService);

    comp = fixture.componentInstance;
  });

  describe('ngOnInit', () => {
    it('Should call Tags query and add missing value', () => {
      const taggedItems: ITaggedItems = { id: 456 };
      const tag: ITags = { id: 31357 };
      taggedItems.tag = tag;

      const tagsCollection: ITags[] = [{ id: 15668 }];
      jest.spyOn(tagsService, 'query').mockReturnValue(of(new HttpResponse({ body: tagsCollection })));
      const additionalTags = [tag];
      const expectedCollection: ITags[] = [...additionalTags, ...tagsCollection];
      jest.spyOn(tagsService, 'addTagsToCollectionIfMissing').mockReturnValue(expectedCollection);

      activatedRoute.data = of({ taggedItems });
      comp.ngOnInit();

      expect(tagsService.query).toHaveBeenCalled();
      expect(tagsService.addTagsToCollectionIfMissing).toHaveBeenCalledWith(tagsCollection, ...additionalTags.map(expect.objectContaining));
      expect(comp.tagsSharedCollection).toEqual(expectedCollection);
    });

    it('Should call ItemTypes query and add missing value', () => {
      const taggedItems: ITaggedItems = { id: 456 };
      const item: IItemTypes = { id: 29797 };
      taggedItems.item = item;

      const itemTypesCollection: IItemTypes[] = [{ id: 7259 }];
      jest.spyOn(itemTypesService, 'query').mockReturnValue(of(new HttpResponse({ body: itemTypesCollection })));
      const additionalItemTypes = [item];
      const expectedCollection: IItemTypes[] = [...additionalItemTypes, ...itemTypesCollection];
      jest.spyOn(itemTypesService, 'addItemTypesToCollectionIfMissing').mockReturnValue(expectedCollection);

      activatedRoute.data = of({ taggedItems });
      comp.ngOnInit();

      expect(itemTypesService.query).toHaveBeenCalled();
      expect(itemTypesService.addItemTypesToCollectionIfMissing).toHaveBeenCalledWith(
        itemTypesCollection,
        ...additionalItemTypes.map(expect.objectContaining),
      );
      expect(comp.itemTypesSharedCollection).toEqual(expectedCollection);
    });

    it('Should call Pages query and add missing value', () => {
      const taggedItems: ITaggedItems = { id: 456 };
      const pages: IPages[] = [{ id: 31374 }];
      taggedItems.pages = pages;

      const pagesCollection: IPages[] = [{ id: 5134 }];
      jest.spyOn(pagesService, 'query').mockReturnValue(of(new HttpResponse({ body: pagesCollection })));
      const additionalPages = [...pages];
      const expectedCollection: IPages[] = [...additionalPages, ...pagesCollection];
      jest.spyOn(pagesService, 'addPagesToCollectionIfMissing').mockReturnValue(expectedCollection);

      activatedRoute.data = of({ taggedItems });
      comp.ngOnInit();

      expect(pagesService.query).toHaveBeenCalled();
      expect(pagesService.addPagesToCollectionIfMissing).toHaveBeenCalledWith(
        pagesCollection,
        ...additionalPages.map(expect.objectContaining),
      );
      expect(comp.pagesSharedCollection).toEqual(expectedCollection);
    });

    it('Should call Posts query and add missing value', () => {
      const taggedItems: ITaggedItems = { id: 456 };
      const posts: IPosts[] = [{ id: 25031 }];
      taggedItems.posts = posts;

      const postsCollection: IPosts[] = [{ id: 634 }];
      jest.spyOn(postsService, 'query').mockReturnValue(of(new HttpResponse({ body: postsCollection })));
      const additionalPosts = [...posts];
      const expectedCollection: IPosts[] = [...additionalPosts, ...postsCollection];
      jest.spyOn(postsService, 'addPostsToCollectionIfMissing').mockReturnValue(expectedCollection);

      activatedRoute.data = of({ taggedItems });
      comp.ngOnInit();

      expect(postsService.query).toHaveBeenCalled();
      expect(postsService.addPostsToCollectionIfMissing).toHaveBeenCalledWith(
        postsCollection,
        ...additionalPosts.map(expect.objectContaining),
      );
      expect(comp.postsSharedCollection).toEqual(expectedCollection);
    });

    it('Should update editForm', () => {
      const taggedItems: ITaggedItems = { id: 456 };
      const tag: ITags = { id: 23337 };
      taggedItems.tag = tag;
      const item: IItemTypes = { id: 14436 };
      taggedItems.item = item;
      const page: IPages = { id: 26109 };
      taggedItems.pages = [page];
      const post: IPosts = { id: 25617 };
      taggedItems.posts = [post];

      activatedRoute.data = of({ taggedItems });
      comp.ngOnInit();

      expect(comp.tagsSharedCollection).toContain(tag);
      expect(comp.itemTypesSharedCollection).toContain(item);
      expect(comp.pagesSharedCollection).toContain(page);
      expect(comp.postsSharedCollection).toContain(post);
      expect(comp.taggedItems).toEqual(taggedItems);
    });
  });

  describe('save', () => {
    it('Should call update service on save for existing entity', () => {
      // GIVEN
      const saveSubject = new Subject<HttpResponse<ITaggedItems>>();
      const taggedItems = { id: 123 };
      jest.spyOn(taggedItemsFormService, 'getTaggedItems').mockReturnValue(taggedItems);
      jest.spyOn(taggedItemsService, 'update').mockReturnValue(saveSubject);
      jest.spyOn(comp, 'previousState');
      activatedRoute.data = of({ taggedItems });
      comp.ngOnInit();

      // WHEN
      comp.save();
      expect(comp.isSaving).toEqual(true);
      saveSubject.next(new HttpResponse({ body: taggedItems }));
      saveSubject.complete();

      // THEN
      expect(taggedItemsFormService.getTaggedItems).toHaveBeenCalled();
      expect(comp.previousState).toHaveBeenCalled();
      expect(taggedItemsService.update).toHaveBeenCalledWith(expect.objectContaining(taggedItems));
      expect(comp.isSaving).toEqual(false);
    });

    it('Should call create service on save for new entity', () => {
      // GIVEN
      const saveSubject = new Subject<HttpResponse<ITaggedItems>>();
      const taggedItems = { id: 123 };
      jest.spyOn(taggedItemsFormService, 'getTaggedItems').mockReturnValue({ id: null });
      jest.spyOn(taggedItemsService, 'create').mockReturnValue(saveSubject);
      jest.spyOn(comp, 'previousState');
      activatedRoute.data = of({ taggedItems: null });
      comp.ngOnInit();

      // WHEN
      comp.save();
      expect(comp.isSaving).toEqual(true);
      saveSubject.next(new HttpResponse({ body: taggedItems }));
      saveSubject.complete();

      // THEN
      expect(taggedItemsFormService.getTaggedItems).toHaveBeenCalled();
      expect(taggedItemsService.create).toHaveBeenCalled();
      expect(comp.isSaving).toEqual(false);
      expect(comp.previousState).toHaveBeenCalled();
    });

    it('Should set isSaving to false on error', () => {
      // GIVEN
      const saveSubject = new Subject<HttpResponse<ITaggedItems>>();
      const taggedItems = { id: 123 };
      jest.spyOn(taggedItemsService, 'update').mockReturnValue(saveSubject);
      jest.spyOn(comp, 'previousState');
      activatedRoute.data = of({ taggedItems });
      comp.ngOnInit();

      // WHEN
      comp.save();
      expect(comp.isSaving).toEqual(true);
      saveSubject.error('This is an error!');

      // THEN
      expect(taggedItemsService.update).toHaveBeenCalled();
      expect(comp.isSaving).toEqual(false);
      expect(comp.previousState).not.toHaveBeenCalled();
    });
  });

  describe('Compare relationships', () => {
    describe('compareTags', () => {
      it('Should forward to tagsService', () => {
        const entity = { id: 123 };
        const entity2 = { id: 456 };
        jest.spyOn(tagsService, 'compareTags');
        comp.compareTags(entity, entity2);
        expect(tagsService.compareTags).toHaveBeenCalledWith(entity, entity2);
      });
    });

    describe('compareItemTypes', () => {
      it('Should forward to itemTypesService', () => {
        const entity = { id: 123 };
        const entity2 = { id: 456 };
        jest.spyOn(itemTypesService, 'compareItemTypes');
        comp.compareItemTypes(entity, entity2);
        expect(itemTypesService.compareItemTypes).toHaveBeenCalledWith(entity, entity2);
      });
    });

    describe('comparePages', () => {
      it('Should forward to pagesService', () => {
        const entity = { id: 123 };
        const entity2 = { id: 456 };
        jest.spyOn(pagesService, 'comparePages');
        comp.comparePages(entity, entity2);
        expect(pagesService.comparePages).toHaveBeenCalledWith(entity, entity2);
      });
    });

    describe('comparePosts', () => {
      it('Should forward to postsService', () => {
        const entity = { id: 123 };
        const entity2 = { id: 456 };
        jest.spyOn(postsService, 'comparePosts');
        comp.comparePosts(entity, entity2);
        expect(postsService.comparePosts).toHaveBeenCalledWith(entity, entity2);
      });
    });
  });
});
