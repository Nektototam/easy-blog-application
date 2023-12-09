import { TestBed } from '@angular/core/testing';

import { sampleWithRequiredData, sampleWithNewData } from '../authors.test-samples';

import { AuthorsFormService } from './authors-form.service';

describe('Authors Form Service', () => {
  let service: AuthorsFormService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(AuthorsFormService);
  });

  describe('Service methods', () => {
    describe('createAuthorsFormGroup', () => {
      it('should create a new form with FormControl', () => {
        const formGroup = service.createAuthorsFormGroup();

        expect(formGroup.controls).toEqual(
          expect.objectContaining({
            id: expect.any(Object),
            name: expect.any(Object),
            email: expect.any(Object),
            url: expect.any(Object),
          }),
        );
      });

      it('passing IAuthors should create a new form with FormGroup', () => {
        const formGroup = service.createAuthorsFormGroup(sampleWithRequiredData);

        expect(formGroup.controls).toEqual(
          expect.objectContaining({
            id: expect.any(Object),
            name: expect.any(Object),
            email: expect.any(Object),
            url: expect.any(Object),
          }),
        );
      });
    });

    describe('getAuthors', () => {
      it('should return NewAuthors for default Authors initial value', () => {
        const formGroup = service.createAuthorsFormGroup(sampleWithNewData);

        const authors = service.getAuthors(formGroup) as any;

        expect(authors).toMatchObject(sampleWithNewData);
      });

      it('should return NewAuthors for empty Authors initial value', () => {
        const formGroup = service.createAuthorsFormGroup();

        const authors = service.getAuthors(formGroup) as any;

        expect(authors).toMatchObject({});
      });

      it('should return IAuthors', () => {
        const formGroup = service.createAuthorsFormGroup(sampleWithRequiredData);

        const authors = service.getAuthors(formGroup) as any;

        expect(authors).toMatchObject(sampleWithRequiredData);
      });
    });

    describe('resetForm', () => {
      it('passing IAuthors should not enable id FormControl', () => {
        const formGroup = service.createAuthorsFormGroup();
        expect(formGroup.controls.id.disabled).toBe(true);

        service.resetForm(formGroup, sampleWithRequiredData);

        expect(formGroup.controls.id.disabled).toBe(true);
      });

      it('passing NewAuthors should disable id FormControl', () => {
        const formGroup = service.createAuthorsFormGroup(sampleWithRequiredData);
        expect(formGroup.controls.id.disabled).toBe(true);

        service.resetForm(formGroup, { id: null });

        expect(formGroup.controls.id.disabled).toBe(true);
      });
    });
  });
});
