import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpResponse } from '@angular/common/http';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { FormBuilder } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { RouterTestingModule } from '@angular/router/testing';
import { of, Subject, from } from 'rxjs';

import { IAuthors } from 'app/entities/authors/authors.model';
import { AuthorsService } from 'app/entities/authors/service/authors.service';
import { IPosts } from 'app/entities/posts/posts.model';
import { PostsService } from 'app/entities/posts/service/posts.service';
import { IPages } from 'app/entities/pages/pages.model';
import { PagesService } from 'app/entities/pages/service/pages.service';
import { IComments } from '../comments.model';
import { CommentsService } from '../service/comments.service';
import { CommentsFormService } from './comments-form.service';

import { CommentsUpdateComponent } from './comments-update.component';

describe('Comments Management Update Component', () => {
  let comp: CommentsUpdateComponent;
  let fixture: ComponentFixture<CommentsUpdateComponent>;
  let activatedRoute: ActivatedRoute;
  let commentsFormService: CommentsFormService;
  let commentsService: CommentsService;
  let authorsService: AuthorsService;
  let postsService: PostsService;
  let pagesService: PagesService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule, RouterTestingModule.withRoutes([]), CommentsUpdateComponent],
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
      .overrideTemplate(CommentsUpdateComponent, '')
      .compileComponents();

    fixture = TestBed.createComponent(CommentsUpdateComponent);
    activatedRoute = TestBed.inject(ActivatedRoute);
    commentsFormService = TestBed.inject(CommentsFormService);
    commentsService = TestBed.inject(CommentsService);
    authorsService = TestBed.inject(AuthorsService);
    postsService = TestBed.inject(PostsService);
    pagesService = TestBed.inject(PagesService);

    comp = fixture.componentInstance;
  });

  describe('ngOnInit', () => {
    it('Should call Authors query and add missing value', () => {
      const comments: IComments = { id: 456 };
      const author: IAuthors = { id: 10397 };
      comments.author = author;

      const authorsCollection: IAuthors[] = [{ id: 406 }];
      jest.spyOn(authorsService, 'query').mockReturnValue(of(new HttpResponse({ body: authorsCollection })));
      const additionalAuthors = [author];
      const expectedCollection: IAuthors[] = [...additionalAuthors, ...authorsCollection];
      jest.spyOn(authorsService, 'addAuthorsToCollectionIfMissing').mockReturnValue(expectedCollection);

      activatedRoute.data = of({ comments });
      comp.ngOnInit();

      expect(authorsService.query).toHaveBeenCalled();
      expect(authorsService.addAuthorsToCollectionIfMissing).toHaveBeenCalledWith(
        authorsCollection,
        ...additionalAuthors.map(expect.objectContaining),
      );
      expect(comp.authorsSharedCollection).toEqual(expectedCollection);
    });

    it('Should call Posts query and add missing value', () => {
      const comments: IComments = { id: 456 };
      const posts: IPosts[] = [{ id: 31760 }];
      comments.posts = posts;

      const postsCollection: IPosts[] = [{ id: 27159 }];
      jest.spyOn(postsService, 'query').mockReturnValue(of(new HttpResponse({ body: postsCollection })));
      const additionalPosts = [...posts];
      const expectedCollection: IPosts[] = [...additionalPosts, ...postsCollection];
      jest.spyOn(postsService, 'addPostsToCollectionIfMissing').mockReturnValue(expectedCollection);

      activatedRoute.data = of({ comments });
      comp.ngOnInit();

      expect(postsService.query).toHaveBeenCalled();
      expect(postsService.addPostsToCollectionIfMissing).toHaveBeenCalledWith(
        postsCollection,
        ...additionalPosts.map(expect.objectContaining),
      );
      expect(comp.postsSharedCollection).toEqual(expectedCollection);
    });

    it('Should call Pages query and add missing value', () => {
      const comments: IComments = { id: 456 };
      const pages: IPages[] = [{ id: 22545 }];
      comments.pages = pages;

      const pagesCollection: IPages[] = [{ id: 31865 }];
      jest.spyOn(pagesService, 'query').mockReturnValue(of(new HttpResponse({ body: pagesCollection })));
      const additionalPages = [...pages];
      const expectedCollection: IPages[] = [...additionalPages, ...pagesCollection];
      jest.spyOn(pagesService, 'addPagesToCollectionIfMissing').mockReturnValue(expectedCollection);

      activatedRoute.data = of({ comments });
      comp.ngOnInit();

      expect(pagesService.query).toHaveBeenCalled();
      expect(pagesService.addPagesToCollectionIfMissing).toHaveBeenCalledWith(
        pagesCollection,
        ...additionalPages.map(expect.objectContaining),
      );
      expect(comp.pagesSharedCollection).toEqual(expectedCollection);
    });

    it('Should call Comments query and add missing value', () => {
      const comments: IComments = { id: 456 };
      const parents: IComments[] = [{ id: 7152 }];
      comments.parents = parents;

      const commentsCollection: IComments[] = [{ id: 1184 }];
      jest.spyOn(commentsService, 'query').mockReturnValue(of(new HttpResponse({ body: commentsCollection })));
      const additionalComments = [...parents];
      const expectedCollection: IComments[] = [...additionalComments, ...commentsCollection];
      jest.spyOn(commentsService, 'addCommentsToCollectionIfMissing').mockReturnValue(expectedCollection);

      activatedRoute.data = of({ comments });
      comp.ngOnInit();

      expect(commentsService.query).toHaveBeenCalled();
      expect(commentsService.addCommentsToCollectionIfMissing).toHaveBeenCalledWith(
        commentsCollection,
        ...additionalComments.map(expect.objectContaining),
      );
      expect(comp.commentsSharedCollection).toEqual(expectedCollection);
    });

    it('Should update editForm', () => {
      const comments: IComments = { id: 456 };
      const author: IAuthors = { id: 29779 };
      comments.author = author;
      const post: IPosts = { id: 19935 };
      comments.posts = [post];
      const page: IPages = { id: 8574 };
      comments.pages = [page];
      const parent: IComments = { id: 6319 };
      comments.parents = [parent];

      activatedRoute.data = of({ comments });
      comp.ngOnInit();

      expect(comp.authorsSharedCollection).toContain(author);
      expect(comp.postsSharedCollection).toContain(post);
      expect(comp.pagesSharedCollection).toContain(page);
      expect(comp.commentsSharedCollection).toContain(parent);
      expect(comp.comments).toEqual(comments);
    });
  });

  describe('save', () => {
    it('Should call update service on save for existing entity', () => {
      // GIVEN
      const saveSubject = new Subject<HttpResponse<IComments>>();
      const comments = { id: 123 };
      jest.spyOn(commentsFormService, 'getComments').mockReturnValue(comments);
      jest.spyOn(commentsService, 'update').mockReturnValue(saveSubject);
      jest.spyOn(comp, 'previousState');
      activatedRoute.data = of({ comments });
      comp.ngOnInit();

      // WHEN
      comp.save();
      expect(comp.isSaving).toEqual(true);
      saveSubject.next(new HttpResponse({ body: comments }));
      saveSubject.complete();

      // THEN
      expect(commentsFormService.getComments).toHaveBeenCalled();
      expect(comp.previousState).toHaveBeenCalled();
      expect(commentsService.update).toHaveBeenCalledWith(expect.objectContaining(comments));
      expect(comp.isSaving).toEqual(false);
    });

    it('Should call create service on save for new entity', () => {
      // GIVEN
      const saveSubject = new Subject<HttpResponse<IComments>>();
      const comments = { id: 123 };
      jest.spyOn(commentsFormService, 'getComments').mockReturnValue({ id: null });
      jest.spyOn(commentsService, 'create').mockReturnValue(saveSubject);
      jest.spyOn(comp, 'previousState');
      activatedRoute.data = of({ comments: null });
      comp.ngOnInit();

      // WHEN
      comp.save();
      expect(comp.isSaving).toEqual(true);
      saveSubject.next(new HttpResponse({ body: comments }));
      saveSubject.complete();

      // THEN
      expect(commentsFormService.getComments).toHaveBeenCalled();
      expect(commentsService.create).toHaveBeenCalled();
      expect(comp.isSaving).toEqual(false);
      expect(comp.previousState).toHaveBeenCalled();
    });

    it('Should set isSaving to false on error', () => {
      // GIVEN
      const saveSubject = new Subject<HttpResponse<IComments>>();
      const comments = { id: 123 };
      jest.spyOn(commentsService, 'update').mockReturnValue(saveSubject);
      jest.spyOn(comp, 'previousState');
      activatedRoute.data = of({ comments });
      comp.ngOnInit();

      // WHEN
      comp.save();
      expect(comp.isSaving).toEqual(true);
      saveSubject.error('This is an error!');

      // THEN
      expect(commentsService.update).toHaveBeenCalled();
      expect(comp.isSaving).toEqual(false);
      expect(comp.previousState).not.toHaveBeenCalled();
    });
  });

  describe('Compare relationships', () => {
    describe('compareAuthors', () => {
      it('Should forward to authorsService', () => {
        const entity = { id: 123 };
        const entity2 = { id: 456 };
        jest.spyOn(authorsService, 'compareAuthors');
        comp.compareAuthors(entity, entity2);
        expect(authorsService.compareAuthors).toHaveBeenCalledWith(entity, entity2);
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

    describe('comparePages', () => {
      it('Should forward to pagesService', () => {
        const entity = { id: 123 };
        const entity2 = { id: 456 };
        jest.spyOn(pagesService, 'comparePages');
        comp.comparePages(entity, entity2);
        expect(pagesService.comparePages).toHaveBeenCalledWith(entity, entity2);
      });
    });

    describe('compareComments', () => {
      it('Should forward to commentsService', () => {
        const entity = { id: 123 };
        const entity2 = { id: 456 };
        jest.spyOn(commentsService, 'compareComments');
        comp.compareComments(entity, entity2);
        expect(commentsService.compareComments).toHaveBeenCalledWith(entity, entity2);
      });
    });
  });
});
