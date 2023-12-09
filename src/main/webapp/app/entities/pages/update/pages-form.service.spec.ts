import { TestBed } from '@angular/core/testing';

import { sampleWithRequiredData, sampleWithNewData } from '../pages.test-samples';

import { PagesFormService } from './pages-form.service';

describe('Pages Form Service', () => {
  let service: PagesFormService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(PagesFormService);
  });

  describe('Service methods', () => {
    describe('createPagesFormGroup', () => {
      it('should create a new form with FormControl', () => {
        const formGroup = service.createPagesFormGroup();

        expect(formGroup.controls).toEqual(
          expect.objectContaining({
            id: expect.any(Object),
            title: expect.any(Object),
            slug: expect.any(Object),
            html: expect.any(Object),
            createdAt: expect.any(Object),
            updatedAt: expect.any(Object),
            publishedAt: expect.any(Object),
            status: expect.any(Object),
            visibility: expect.any(Object),
            authors: expect.any(Object),
            tags: expect.any(Object),
            comments: expect.any(Object),
          }),
        );
      });

      it('passing IPages should create a new form with FormGroup', () => {
        const formGroup = service.createPagesFormGroup(sampleWithRequiredData);

        expect(formGroup.controls).toEqual(
          expect.objectContaining({
            id: expect.any(Object),
            title: expect.any(Object),
            slug: expect.any(Object),
            html: expect.any(Object),
            createdAt: expect.any(Object),
            updatedAt: expect.any(Object),
            publishedAt: expect.any(Object),
            status: expect.any(Object),
            visibility: expect.any(Object),
            authors: expect.any(Object),
            tags: expect.any(Object),
            comments: expect.any(Object),
          }),
        );
      });
    });

    describe('getPages', () => {
      it('should return NewPages for default Pages initial value', () => {
        const formGroup = service.createPagesFormGroup(sampleWithNewData);

        const pages = service.getPages(formGroup) as any;

        expect(pages).toMatchObject(sampleWithNewData);
      });

      it('should return NewPages for empty Pages initial value', () => {
        const formGroup = service.createPagesFormGroup();

        const pages = service.getPages(formGroup) as any;

        expect(pages).toMatchObject({});
      });

      it('should return IPages', () => {
        const formGroup = service.createPagesFormGroup(sampleWithRequiredData);

        const pages = service.getPages(formGroup) as any;

        expect(pages).toMatchObject(sampleWithRequiredData);
      });
    });

    describe('resetForm', () => {
      it('passing IPages should not enable id FormControl', () => {
        const formGroup = service.createPagesFormGroup();
        expect(formGroup.controls.id.disabled).toBe(true);

        service.resetForm(formGroup, sampleWithRequiredData);

        expect(formGroup.controls.id.disabled).toBe(true);
      });

      it('passing NewPages should disable id FormControl', () => {
        const formGroup = service.createPagesFormGroup(sampleWithRequiredData);
        expect(formGroup.controls.id.disabled).toBe(true);

        service.resetForm(formGroup, { id: null });

        expect(formGroup.controls.id.disabled).toBe(true);
      });
    });
  });
});
