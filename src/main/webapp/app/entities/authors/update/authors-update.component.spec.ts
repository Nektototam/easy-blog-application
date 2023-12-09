import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpResponse } from '@angular/common/http';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { FormBuilder } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { RouterTestingModule } from '@angular/router/testing';
import { of, Subject, from } from 'rxjs';

import { AuthorsService } from '../service/authors.service';
import { IAuthors } from '../authors.model';
import { AuthorsFormService } from './authors-form.service';

import { AuthorsUpdateComponent } from './authors-update.component';

describe('Authors Management Update Component', () => {
  let comp: AuthorsUpdateComponent;
  let fixture: ComponentFixture<AuthorsUpdateComponent>;
  let activatedRoute: ActivatedRoute;
  let authorsFormService: AuthorsFormService;
  let authorsService: AuthorsService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule, RouterTestingModule.withRoutes([]), AuthorsUpdateComponent],
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
      .overrideTemplate(AuthorsUpdateComponent, '')
      .compileComponents();

    fixture = TestBed.createComponent(AuthorsUpdateComponent);
    activatedRoute = TestBed.inject(ActivatedRoute);
    authorsFormService = TestBed.inject(AuthorsFormService);
    authorsService = TestBed.inject(AuthorsService);

    comp = fixture.componentInstance;
  });

  describe('ngOnInit', () => {
    it('Should update editForm', () => {
      const authors: IAuthors = { id: 456 };

      activatedRoute.data = of({ authors });
      comp.ngOnInit();

      expect(comp.authors).toEqual(authors);
    });
  });

  describe('save', () => {
    it('Should call update service on save for existing entity', () => {
      // GIVEN
      const saveSubject = new Subject<HttpResponse<IAuthors>>();
      const authors = { id: 123 };
      jest.spyOn(authorsFormService, 'getAuthors').mockReturnValue(authors);
      jest.spyOn(authorsService, 'update').mockReturnValue(saveSubject);
      jest.spyOn(comp, 'previousState');
      activatedRoute.data = of({ authors });
      comp.ngOnInit();

      // WHEN
      comp.save();
      expect(comp.isSaving).toEqual(true);
      saveSubject.next(new HttpResponse({ body: authors }));
      saveSubject.complete();

      // THEN
      expect(authorsFormService.getAuthors).toHaveBeenCalled();
      expect(comp.previousState).toHaveBeenCalled();
      expect(authorsService.update).toHaveBeenCalledWith(expect.objectContaining(authors));
      expect(comp.isSaving).toEqual(false);
    });

    it('Should call create service on save for new entity', () => {
      // GIVEN
      const saveSubject = new Subject<HttpResponse<IAuthors>>();
      const authors = { id: 123 };
      jest.spyOn(authorsFormService, 'getAuthors').mockReturnValue({ id: null });
      jest.spyOn(authorsService, 'create').mockReturnValue(saveSubject);
      jest.spyOn(comp, 'previousState');
      activatedRoute.data = of({ authors: null });
      comp.ngOnInit();

      // WHEN
      comp.save();
      expect(comp.isSaving).toEqual(true);
      saveSubject.next(new HttpResponse({ body: authors }));
      saveSubject.complete();

      // THEN
      expect(authorsFormService.getAuthors).toHaveBeenCalled();
      expect(authorsService.create).toHaveBeenCalled();
      expect(comp.isSaving).toEqual(false);
      expect(comp.previousState).toHaveBeenCalled();
    });

    it('Should set isSaving to false on error', () => {
      // GIVEN
      const saveSubject = new Subject<HttpResponse<IAuthors>>();
      const authors = { id: 123 };
      jest.spyOn(authorsService, 'update').mockReturnValue(saveSubject);
      jest.spyOn(comp, 'previousState');
      activatedRoute.data = of({ authors });
      comp.ngOnInit();

      // WHEN
      comp.save();
      expect(comp.isSaving).toEqual(true);
      saveSubject.error('This is an error!');

      // THEN
      expect(authorsService.update).toHaveBeenCalled();
      expect(comp.isSaving).toEqual(false);
      expect(comp.previousState).not.toHaveBeenCalled();
    });
  });
});
