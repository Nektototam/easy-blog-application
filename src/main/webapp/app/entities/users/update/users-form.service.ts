import { Injectable } from '@angular/core';
import { FormGroup, FormControl, Validators } from '@angular/forms';

import { IUsers, NewUsers } from '../users.model';

/**
 * A partial Type with required key is used as form input.
 */
type PartialWithRequiredKeyOf<T extends { id: unknown }> = Partial<Omit<T, 'id'>> & { id: T['id'] };

/**
 * Type for createFormGroup and resetForm argument.
 * It accepts IUsers for edit and NewUsersFormGroupInput for create.
 */
type UsersFormGroupInput = IUsers | PartialWithRequiredKeyOf<NewUsers>;

type UsersFormDefaults = Pick<NewUsers, 'id' | 'pages' | 'posts'>;

type UsersFormGroupContent = {
  id: FormControl<IUsers['id'] | NewUsers['id']>;
  name: FormControl<IUsers['name']>;
  email: FormControl<IUsers['email']>;
  pages: FormControl<IUsers['pages']>;
  posts: FormControl<IUsers['posts']>;
};

export type UsersFormGroup = FormGroup<UsersFormGroupContent>;

@Injectable({ providedIn: 'root' })
export class UsersFormService {
  createUsersFormGroup(users: UsersFormGroupInput = { id: null }): UsersFormGroup {
    const usersRawValue = {
      ...this.getFormDefaults(),
      ...users,
    };
    return new FormGroup<UsersFormGroupContent>({
      id: new FormControl(
        { value: usersRawValue.id, disabled: true },
        {
          nonNullable: true,
          validators: [Validators.required],
        },
      ),
      name: new FormControl(usersRawValue.name),
      email: new FormControl(usersRawValue.email),
      pages: new FormControl(usersRawValue.pages ?? []),
      posts: new FormControl(usersRawValue.posts ?? []),
    });
  }

  getUsers(form: UsersFormGroup): IUsers | NewUsers {
    return form.getRawValue() as IUsers | NewUsers;
  }

  resetForm(form: UsersFormGroup, users: UsersFormGroupInput): void {
    const usersRawValue = { ...this.getFormDefaults(), ...users };
    form.reset(
      {
        ...usersRawValue,
        id: { value: usersRawValue.id, disabled: true },
      } as any /* cast to workaround https://github.com/angular/angular/issues/46458 */,
    );
  }

  private getFormDefaults(): UsersFormDefaults {
    return {
      id: null,
      pages: [],
      posts: [],
    };
  }
}
