import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpResponse } from '@angular/common/http';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { FormBuilder } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { RouterTestingModule } from '@angular/router/testing';
import { of, Subject, from } from 'rxjs';

import { IUsers } from 'app/entities/users/users.model';
import { UsersService } from 'app/entities/users/service/users.service';
import { PagesService } from '../service/pages.service';
import { IPages } from '../pages.model';
import { PagesFormService } from './pages-form.service';

import { PagesUpdateComponent } from './pages-update.component';

describe('Pages Management Update Component', () => {
  let comp: PagesUpdateComponent;
  let fixture: ComponentFixture<PagesUpdateComponent>;
  let activatedRoute: ActivatedRoute;
  let pagesFormService: PagesFormService;
  let pagesService: PagesService;
  let usersService: UsersService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule, RouterTestingModule.withRoutes([]), PagesUpdateComponent],
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
      .overrideTemplate(PagesUpdateComponent, '')
      .compileComponents();

    fixture = TestBed.createComponent(PagesUpdateComponent);
    activatedRoute = TestBed.inject(ActivatedRoute);
    pagesFormService = TestBed.inject(PagesFormService);
    pagesService = TestBed.inject(PagesService);
    usersService = TestBed.inject(UsersService);

    comp = fixture.componentInstance;
  });

  describe('ngOnInit', () => {
    it('Should call Users query and add missing value', () => {
      const pages: IPages = { id: 456 };
      const authors: IUsers[] = [{ id: 31118 }];
      pages.authors = authors;

      const usersCollection: IUsers[] = [{ id: 23135 }];
      jest.spyOn(usersService, 'query').mockReturnValue(of(new HttpResponse({ body: usersCollection })));
      const additionalUsers = [...authors];
      const expectedCollection: IUsers[] = [...additionalUsers, ...usersCollection];
      jest.spyOn(usersService, 'addUsersToCollectionIfMissing').mockReturnValue(expectedCollection);

      activatedRoute.data = of({ pages });
      comp.ngOnInit();

      expect(usersService.query).toHaveBeenCalled();
      expect(usersService.addUsersToCollectionIfMissing).toHaveBeenCalledWith(
        usersCollection,
        ...additionalUsers.map(expect.objectContaining),
      );
      expect(comp.usersSharedCollection).toEqual(expectedCollection);
    });

    it('Should update editForm', () => {
      const pages: IPages = { id: 456 };
      const author: IUsers = { id: 7726 };
      pages.authors = [author];

      activatedRoute.data = of({ pages });
      comp.ngOnInit();

      expect(comp.usersSharedCollection).toContain(author);
      expect(comp.pages).toEqual(pages);
    });
  });

  describe('save', () => {
    it('Should call update service on save for existing entity', () => {
      // GIVEN
      const saveSubject = new Subject<HttpResponse<IPages>>();
      const pages = { id: 123 };
      jest.spyOn(pagesFormService, 'getPages').mockReturnValue(pages);
      jest.spyOn(pagesService, 'update').mockReturnValue(saveSubject);
      jest.spyOn(comp, 'previousState');
      activatedRoute.data = of({ pages });
      comp.ngOnInit();

      // WHEN
      comp.save();
      expect(comp.isSaving).toEqual(true);
      saveSubject.next(new HttpResponse({ body: pages }));
      saveSubject.complete();

      // THEN
      expect(pagesFormService.getPages).toHaveBeenCalled();
      expect(comp.previousState).toHaveBeenCalled();
      expect(pagesService.update).toHaveBeenCalledWith(expect.objectContaining(pages));
      expect(comp.isSaving).toEqual(false);
    });

    it('Should call create service on save for new entity', () => {
      // GIVEN
      const saveSubject = new Subject<HttpResponse<IPages>>();
      const pages = { id: 123 };
      jest.spyOn(pagesFormService, 'getPages').mockReturnValue({ id: null });
      jest.spyOn(pagesService, 'create').mockReturnValue(saveSubject);
      jest.spyOn(comp, 'previousState');
      activatedRoute.data = of({ pages: null });
      comp.ngOnInit();

      // WHEN
      comp.save();
      expect(comp.isSaving).toEqual(true);
      saveSubject.next(new HttpResponse({ body: pages }));
      saveSubject.complete();

      // THEN
      expect(pagesFormService.getPages).toHaveBeenCalled();
      expect(pagesService.create).toHaveBeenCalled();
      expect(comp.isSaving).toEqual(false);
      expect(comp.previousState).toHaveBeenCalled();
    });

    it('Should set isSaving to false on error', () => {
      // GIVEN
      const saveSubject = new Subject<HttpResponse<IPages>>();
      const pages = { id: 123 };
      jest.spyOn(pagesService, 'update').mockReturnValue(saveSubject);
      jest.spyOn(comp, 'previousState');
      activatedRoute.data = of({ pages });
      comp.ngOnInit();

      // WHEN
      comp.save();
      expect(comp.isSaving).toEqual(true);
      saveSubject.error('This is an error!');

      // THEN
      expect(pagesService.update).toHaveBeenCalled();
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
