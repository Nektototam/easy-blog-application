import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpResponse } from '@angular/common/http';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { FormBuilder } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { RouterTestingModule } from '@angular/router/testing';
import { of, Subject, from } from 'rxjs';

import { ItemTypesService } from '../service/item-types.service';
import { IItemTypes } from '../item-types.model';
import { ItemTypesFormService } from './item-types-form.service';

import { ItemTypesUpdateComponent } from './item-types-update.component';

describe('ItemTypes Management Update Component', () => {
  let comp: ItemTypesUpdateComponent;
  let fixture: ComponentFixture<ItemTypesUpdateComponent>;
  let activatedRoute: ActivatedRoute;
  let itemTypesFormService: ItemTypesFormService;
  let itemTypesService: ItemTypesService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule, RouterTestingModule.withRoutes([]), ItemTypesUpdateComponent],
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
      .overrideTemplate(ItemTypesUpdateComponent, '')
      .compileComponents();

    fixture = TestBed.createComponent(ItemTypesUpdateComponent);
    activatedRoute = TestBed.inject(ActivatedRoute);
    itemTypesFormService = TestBed.inject(ItemTypesFormService);
    itemTypesService = TestBed.inject(ItemTypesService);

    comp = fixture.componentInstance;
  });

  describe('ngOnInit', () => {
    it('Should update editForm', () => {
      const itemTypes: IItemTypes = { id: 456 };

      activatedRoute.data = of({ itemTypes });
      comp.ngOnInit();

      expect(comp.itemTypes).toEqual(itemTypes);
    });
  });

  describe('save', () => {
    it('Should call update service on save for existing entity', () => {
      // GIVEN
      const saveSubject = new Subject<HttpResponse<IItemTypes>>();
      const itemTypes = { id: 123 };
      jest.spyOn(itemTypesFormService, 'getItemTypes').mockReturnValue(itemTypes);
      jest.spyOn(itemTypesService, 'update').mockReturnValue(saveSubject);
      jest.spyOn(comp, 'previousState');
      activatedRoute.data = of({ itemTypes });
      comp.ngOnInit();

      // WHEN
      comp.save();
      expect(comp.isSaving).toEqual(true);
      saveSubject.next(new HttpResponse({ body: itemTypes }));
      saveSubject.complete();

      // THEN
      expect(itemTypesFormService.getItemTypes).toHaveBeenCalled();
      expect(comp.previousState).toHaveBeenCalled();
      expect(itemTypesService.update).toHaveBeenCalledWith(expect.objectContaining(itemTypes));
      expect(comp.isSaving).toEqual(false);
    });

    it('Should call create service on save for new entity', () => {
      // GIVEN
      const saveSubject = new Subject<HttpResponse<IItemTypes>>();
      const itemTypes = { id: 123 };
      jest.spyOn(itemTypesFormService, 'getItemTypes').mockReturnValue({ id: null });
      jest.spyOn(itemTypesService, 'create').mockReturnValue(saveSubject);
      jest.spyOn(comp, 'previousState');
      activatedRoute.data = of({ itemTypes: null });
      comp.ngOnInit();

      // WHEN
      comp.save();
      expect(comp.isSaving).toEqual(true);
      saveSubject.next(new HttpResponse({ body: itemTypes }));
      saveSubject.complete();

      // THEN
      expect(itemTypesFormService.getItemTypes).toHaveBeenCalled();
      expect(itemTypesService.create).toHaveBeenCalled();
      expect(comp.isSaving).toEqual(false);
      expect(comp.previousState).toHaveBeenCalled();
    });

    it('Should set isSaving to false on error', () => {
      // GIVEN
      const saveSubject = new Subject<HttpResponse<IItemTypes>>();
      const itemTypes = { id: 123 };
      jest.spyOn(itemTypesService, 'update').mockReturnValue(saveSubject);
      jest.spyOn(comp, 'previousState');
      activatedRoute.data = of({ itemTypes });
      comp.ngOnInit();

      // WHEN
      comp.save();
      expect(comp.isSaving).toEqual(true);
      saveSubject.error('This is an error!');

      // THEN
      expect(itemTypesService.update).toHaveBeenCalled();
      expect(comp.isSaving).toEqual(false);
      expect(comp.previousState).not.toHaveBeenCalled();
    });
  });
});
