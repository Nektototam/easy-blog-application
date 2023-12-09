import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpResponse } from '@angular/common/http';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { FormBuilder } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { RouterTestingModule } from '@angular/router/testing';
import { of, Subject, from } from 'rxjs';

import { UsersService } from '../service/users.service';
import { IUsers } from '../users.model';
import { UsersFormService } from './users-form.service';

import { UsersUpdateComponent } from './users-update.component';

describe('Users Management Update Component', () => {
  let comp: UsersUpdateComponent;
  let fixture: ComponentFixture<UsersUpdateComponent>;
  let activatedRoute: ActivatedRoute;
  let usersFormService: UsersFormService;
  let usersService: UsersService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule, RouterTestingModule.withRoutes([]), UsersUpdateComponent],
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
      .overrideTemplate(UsersUpdateComponent, '')
      .compileComponents();

    fixture = TestBed.createComponent(UsersUpdateComponent);
    activatedRoute = TestBed.inject(ActivatedRoute);
    usersFormService = TestBed.inject(UsersFormService);
    usersService = TestBed.inject(UsersService);

    comp = fixture.componentInstance;
  });

  describe('ngOnInit', () => {
    it('Should update editForm', () => {
      const users: IUsers = { id: 456 };

      activatedRoute.data = of({ users });
      comp.ngOnInit();

      expect(comp.users).toEqual(users);
    });
  });

  describe('save', () => {
    it('Should call update service on save for existing entity', () => {
      // GIVEN
      const saveSubject = new Subject<HttpResponse<IUsers>>();
      const users = { id: 123 };
      jest.spyOn(usersFormService, 'getUsers').mockReturnValue(users);
      jest.spyOn(usersService, 'update').mockReturnValue(saveSubject);
      jest.spyOn(comp, 'previousState');
      activatedRoute.data = of({ users });
      comp.ngOnInit();

      // WHEN
      comp.save();
      expect(comp.isSaving).toEqual(true);
      saveSubject.next(new HttpResponse({ body: users }));
      saveSubject.complete();

      // THEN
      expect(usersFormService.getUsers).toHaveBeenCalled();
      expect(comp.previousState).toHaveBeenCalled();
      expect(usersService.update).toHaveBeenCalledWith(expect.objectContaining(users));
      expect(comp.isSaving).toEqual(false);
    });

    it('Should call create service on save for new entity', () => {
      // GIVEN
      const saveSubject = new Subject<HttpResponse<IUsers>>();
      const users = { id: 123 };
      jest.spyOn(usersFormService, 'getUsers').mockReturnValue({ id: null });
      jest.spyOn(usersService, 'create').mockReturnValue(saveSubject);
      jest.spyOn(comp, 'previousState');
      activatedRoute.data = of({ users: null });
      comp.ngOnInit();

      // WHEN
      comp.save();
      expect(comp.isSaving).toEqual(true);
      saveSubject.next(new HttpResponse({ body: users }));
      saveSubject.complete();

      // THEN
      expect(usersFormService.getUsers).toHaveBeenCalled();
      expect(usersService.create).toHaveBeenCalled();
      expect(comp.isSaving).toEqual(false);
      expect(comp.previousState).toHaveBeenCalled();
    });

    it('Should set isSaving to false on error', () => {
      // GIVEN
      const saveSubject = new Subject<HttpResponse<IUsers>>();
      const users = { id: 123 };
      jest.spyOn(usersService, 'update').mockReturnValue(saveSubject);
      jest.spyOn(comp, 'previousState');
      activatedRoute.data = of({ users });
      comp.ngOnInit();

      // WHEN
      comp.save();
      expect(comp.isSaving).toEqual(true);
      saveSubject.error('This is an error!');

      // THEN
      expect(usersService.update).toHaveBeenCalled();
      expect(comp.isSaving).toEqual(false);
      expect(comp.previousState).not.toHaveBeenCalled();
    });
  });
});
