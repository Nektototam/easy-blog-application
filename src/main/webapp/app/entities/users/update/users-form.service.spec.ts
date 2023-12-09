import { TestBed } from '@angular/core/testing';

import { sampleWithRequiredData, sampleWithNewData } from '../users.test-samples';

import { UsersFormService } from './users-form.service';

describe('Users Form Service', () => {
  let service: UsersFormService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(UsersFormService);
  });

  describe('Service methods', () => {
    describe('createUsersFormGroup', () => {
      it('should create a new form with FormControl', () => {
        const formGroup = service.createUsersFormGroup();

        expect(formGroup.controls).toEqual(
          expect.objectContaining({
            id: expect.any(Object),
            name: expect.any(Object),
            email: expect.any(Object),
            pages: expect.any(Object),
            posts: expect.any(Object),
          }),
        );
      });

      it('passing IUsers should create a new form with FormGroup', () => {
        const formGroup = service.createUsersFormGroup(sampleWithRequiredData);

        expect(formGroup.controls).toEqual(
          expect.objectContaining({
            id: expect.any(Object),
            name: expect.any(Object),
            email: expect.any(Object),
            pages: expect.any(Object),
            posts: expect.any(Object),
          }),
        );
      });
    });

    describe('getUsers', () => {
      it('should return NewUsers for default Users initial value', () => {
        const formGroup = service.createUsersFormGroup(sampleWithNewData);

        const users = service.getUsers(formGroup) as any;

        expect(users).toMatchObject(sampleWithNewData);
      });

      it('should return NewUsers for empty Users initial value', () => {
        const formGroup = service.createUsersFormGroup();

        const users = service.getUsers(formGroup) as any;

        expect(users).toMatchObject({});
      });

      it('should return IUsers', () => {
        const formGroup = service.createUsersFormGroup(sampleWithRequiredData);

        const users = service.getUsers(formGroup) as any;

        expect(users).toMatchObject(sampleWithRequiredData);
      });
    });

    describe('resetForm', () => {
      it('passing IUsers should not enable id FormControl', () => {
        const formGroup = service.createUsersFormGroup();
        expect(formGroup.controls.id.disabled).toBe(true);

        service.resetForm(formGroup, sampleWithRequiredData);

        expect(formGroup.controls.id.disabled).toBe(true);
      });

      it('passing NewUsers should disable id FormControl', () => {
        const formGroup = service.createUsersFormGroup(sampleWithRequiredData);
        expect(formGroup.controls.id.disabled).toBe(true);

        service.resetForm(formGroup, { id: null });

        expect(formGroup.controls.id.disabled).toBe(true);
      });
    });
  });
});
