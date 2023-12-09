import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpResponse } from '@angular/common/http';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { FormBuilder } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { RouterTestingModule } from '@angular/router/testing';
import { of, Subject, from } from 'rxjs';

import { IUsers } from 'app/entities/users/users.model';
import { UsersService } from 'app/entities/users/service/users.service';
import { PostsService } from '../service/posts.service';
import { IPosts } from '../posts.model';
import { PostsFormService } from './posts-form.service';

import { PostsUpdateComponent } from './posts-update.component';

describe('Posts Management Update Component', () => {
  let comp: PostsUpdateComponent;
  let fixture: ComponentFixture<PostsUpdateComponent>;
  let activatedRoute: ActivatedRoute;
  let postsFormService: PostsFormService;
  let postsService: PostsService;
  let usersService: UsersService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule, RouterTestingModule.withRoutes([]), PostsUpdateComponent],
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
      .overrideTemplate(PostsUpdateComponent, '')
      .compileComponents();

    fixture = TestBed.createComponent(PostsUpdateComponent);
    activatedRoute = TestBed.inject(ActivatedRoute);
    postsFormService = TestBed.inject(PostsFormService);
    postsService = TestBed.inject(PostsService);
    usersService = TestBed.inject(UsersService);

    comp = fixture.componentInstance;
  });

  describe('ngOnInit', () => {
    it('Should call Users query and add missing value', () => {
      const posts: IPosts = { id: 456 };
      const authors: IUsers[] = [{ id: 1218 }];
      posts.authors = authors;

      const usersCollection: IUsers[] = [{ id: 24718 }];
      jest.spyOn(usersService, 'query').mockReturnValue(of(new HttpResponse({ body: usersCollection })));
      const additionalUsers = [...authors];
      const expectedCollection: IUsers[] = [...additionalUsers, ...usersCollection];
      jest.spyOn(usersService, 'addUsersToCollectionIfMissing').mockReturnValue(expectedCollection);

      activatedRoute.data = of({ posts });
      comp.ngOnInit();

      expect(usersService.query).toHaveBeenCalled();
      expect(usersService.addUsersToCollectionIfMissing).toHaveBeenCalledWith(
        usersCollection,
        ...additionalUsers.map(expect.objectContaining),
      );
      expect(comp.usersSharedCollection).toEqual(expectedCollection);
    });

    it('Should update editForm', () => {
      const posts: IPosts = { id: 456 };
      const author: IUsers = { id: 20421 };
      posts.authors = [author];

      activatedRoute.data = of({ posts });
      comp.ngOnInit();

      expect(comp.usersSharedCollection).toContain(author);
      expect(comp.posts).toEqual(posts);
    });
  });

  describe('save', () => {
    it('Should call update service on save for existing entity', () => {
      // GIVEN
      const saveSubject = new Subject<HttpResponse<IPosts>>();
      const posts = { id: 123 };
      jest.spyOn(postsFormService, 'getPosts').mockReturnValue(posts);
      jest.spyOn(postsService, 'update').mockReturnValue(saveSubject);
      jest.spyOn(comp, 'previousState');
      activatedRoute.data = of({ posts });
      comp.ngOnInit();

      // WHEN
      comp.save();
      expect(comp.isSaving).toEqual(true);
      saveSubject.next(new HttpResponse({ body: posts }));
      saveSubject.complete();

      // THEN
      expect(postsFormService.getPosts).toHaveBeenCalled();
      expect(comp.previousState).toHaveBeenCalled();
      expect(postsService.update).toHaveBeenCalledWith(expect.objectContaining(posts));
      expect(comp.isSaving).toEqual(false);
    });

    it('Should call create service on save for new entity', () => {
      // GIVEN
      const saveSubject = new Subject<HttpResponse<IPosts>>();
      const posts = { id: 123 };
      jest.spyOn(postsFormService, 'getPosts').mockReturnValue({ id: null });
      jest.spyOn(postsService, 'create').mockReturnValue(saveSubject);
      jest.spyOn(comp, 'previousState');
      activatedRoute.data = of({ posts: null });
      comp.ngOnInit();

      // WHEN
      comp.save();
      expect(comp.isSaving).toEqual(true);
      saveSubject.next(new HttpResponse({ body: posts }));
      saveSubject.complete();

      // THEN
      expect(postsFormService.getPosts).toHaveBeenCalled();
      expect(postsService.create).toHaveBeenCalled();
      expect(comp.isSaving).toEqual(false);
      expect(comp.previousState).toHaveBeenCalled();
    });

    it('Should set isSaving to false on error', () => {
      // GIVEN
      const saveSubject = new Subject<HttpResponse<IPosts>>();
      const posts = { id: 123 };
      jest.spyOn(postsService, 'update').mockReturnValue(saveSubject);
      jest.spyOn(comp, 'previousState');
      activatedRoute.data = of({ posts });
      comp.ngOnInit();

      // WHEN
      comp.save();
      expect(comp.isSaving).toEqual(true);
      saveSubject.error('This is an error!');

      // THEN
      expect(postsService.update).toHaveBeenCalled();
      expect(comp.isSaving).toEqual(false);
      expect(comp.previousState).not.toHaveBeenCalled();
    });
  });

  describe('Compare relationships', () => {
    describe('compareUsers', () => {
      it('Should forward to usersService', () => {
        const entity = { id: 123 };
        const entity2 = { id: 456 };
        jest.spyOn(usersService, 'compareUsers');
        comp.compareUsers(entity, entity2);
        expect(usersService.compareUsers).toHaveBeenCalledWith(entity, entity2);
      });
    });
  });
});
