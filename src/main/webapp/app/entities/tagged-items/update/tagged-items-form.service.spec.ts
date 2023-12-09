import { TestBed } from '@angular/core/testing';

import { sampleWithRequiredData, sampleWithNewData } from '../tagged-items.test-samples';

import { TaggedItemsFormService } from './tagged-items-form.service';

describe('TaggedItems Form Service', () => {
  let service: TaggedItemsFormService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(TaggedItemsFormService);
  });

  describe('Service methods', () => {
    describe('createTaggedItemsFormGroup', () => {
      it('should create a new form with FormControl', () => {
        const formGroup = service.createTaggedItemsFormGroup();

        expect(formGroup.controls).toEqual(
          expect.objectContaining({
            id: expect.any(Object),
            itemType: expect.any(Object),
            tag: expect.any(Object),
            item: expect.any(Object),
            pages: expect.any(Object),
            posts: expect.any(Object),
          }),
        );
      });

      it('passing ITaggedItems should create a new form with FormGroup', () => {
        const formGroup = service.createTaggedItemsFormGroup(sampleWithRequiredData);

        expect(formGroup.controls).toEqual(
          expect.objectContaining({
            id: expect.any(Object),
            itemType: expect.any(Object),
            tag: expect.any(Object),
            item: expect.any(Object),
            pages: expect.any(Object),
            posts: expect.any(Object),
          }),
        );
      });
    });

    describe('getTaggedItems', () => {
      it('should return NewTaggedItems for default TaggedItems initial value', () => {
        const formGroup = service.createTaggedItemsFormGroup(sampleWithNewData);

        const taggedItems = service.getTaggedItems(formGroup) as any;

        expect(taggedItems).toMatchObject(sampleWithNewData);
      });

      it('should return NewTaggedItems for empty TaggedItems initial value', () => {
        const formGroup = service.createTaggedItemsFormGroup();

        const taggedItems = service.getTaggedItems(formGroup) as any;

        expect(taggedItems).toMatchObject({});
      });

      it('should return ITaggedItems', () => {
        const formGroup = service.createTaggedItemsFormGroup(sampleWithRequiredData);

        const taggedItems = service.getTaggedItems(formGroup) as any;

        expect(taggedItems).toMatchObject(sampleWithRequiredData);
      });
    });

    describe('resetForm', () => {
      it('passing ITaggedItems should not enable id FormControl', () => {
        const formGroup = service.createTaggedItemsFormGroup();
        expect(formGroup.controls.id.disabled).toBe(true);

        service.resetForm(formGroup, sampleWithRequiredData);

        expect(formGroup.controls.id.disabled).toBe(true);
      });

      it('passing NewTaggedItems should disable id FormControl', () => {
        const formGroup = service.createTaggedItemsFormGroup(sampleWithRequiredData);
        expect(formGroup.controls.id.disabled).toBe(true);

        service.resetForm(formGroup, { id: null });

        expect(formGroup.controls.id.disabled).toBe(true);
      });
    });
  });
});
