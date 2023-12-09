import { TestBed } from '@angular/core/testing';

import { sampleWithRequiredData, sampleWithNewData } from '../item-types.test-samples';

import { ItemTypesFormService } from './item-types-form.service';

describe('ItemTypes Form Service', () => {
  let service: ItemTypesFormService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(ItemTypesFormService);
  });

  describe('Service methods', () => {
    describe('createItemTypesFormGroup', () => {
      it('should create a new form with FormControl', () => {
        const formGroup = service.createItemTypesFormGroup();

        expect(formGroup.controls).toEqual(
          expect.objectContaining({
            id: expect.any(Object),
            name: expect.any(Object),
          }),
        );
      });

      it('passing IItemTypes should create a new form with FormGroup', () => {
        const formGroup = service.createItemTypesFormGroup(sampleWithRequiredData);

        expect(formGroup.controls).toEqual(
          expect.objectContaining({
            id: expect.any(Object),
            name: expect.any(Object),
          }),
        );
      });
    });

    describe('getItemTypes', () => {
      it('should return NewItemTypes for default ItemTypes initial value', () => {
        const formGroup = service.createItemTypesFormGroup(sampleWithNewData);

        const itemTypes = service.getItemTypes(formGroup) as any;

        expect(itemTypes).toMatchObject(sampleWithNewData);
      });

      it('should return NewItemTypes for empty ItemTypes initial value', () => {
        const formGroup = service.createItemTypesFormGroup();

        const itemTypes = service.getItemTypes(formGroup) as any;

        expect(itemTypes).toMatchObject({});
      });

      it('should return IItemTypes', () => {
        const formGroup = service.createItemTypesFormGroup(sampleWithRequiredData);

        const itemTypes = service.getItemTypes(formGroup) as any;

        expect(itemTypes).toMatchObject(sampleWithRequiredData);
      });
    });

    describe('resetForm', () => {
      it('passing IItemTypes should not enable id FormControl', () => {
        const formGroup = service.createItemTypesFormGroup();
        expect(formGroup.controls.id.disabled).toBe(true);

        service.resetForm(formGroup, sampleWithRequiredData);

        expect(formGroup.controls.id.disabled).toBe(true);
      });

      it('passing NewItemTypes should disable id FormControl', () => {
        const formGroup = service.createItemTypesFormGroup(sampleWithRequiredData);
        expect(formGroup.controls.id.disabled).toBe(true);

        service.resetForm(formGroup, { id: null });

        expect(formGroup.controls.id.disabled).toBe(true);
      });
    });
  });
});
