import { Injectable } from '@angular/core';
import { FormGroup, FormControl, Validators } from '@angular/forms';

import { IAuthors, NewAuthors } from '../authors.model';

/**
 * A partial Type with required key is used as form input.
 */
type PartialWithRequiredKeyOf<T extends { id: unknown }> = Partial<Omit<T, 'id'>> & { id: T['id'] };

/**
 * Type for createFormGroup and resetForm argument.
 * It accepts IAuthors for edit and NewAuthorsFormGroupInput for create.
 */
type AuthorsFormGroupInput = IAuthors | PartialWithRequiredKeyOf<NewAuthors>;

type AuthorsFormDefaults = Pick<NewAuthors, 'id'>;

type AuthorsFormGroupContent = {
  id: FormControl<IAuthors['id'] | NewAuthors['id']>;
  name: FormControl<IAuthors['name']>;
  email: FormControl<IAuthors['email']>;
  url: FormControl<IAuthors['url']>;
};

export type AuthorsFormGroup = FormGroup<AuthorsFormGroupContent>;

@Injectable({ providedIn: 'root' })
export class AuthorsFormService {
  createAuthorsFormGroup(authors: AuthorsFormGroupInput = { id: null }): AuthorsFormGroup {
    const authorsRawValue = {
      ...this.getFormDefaults(),
      ...authors,
    };
    return new FormGroup<AuthorsFormGroupContent>({
      id: new FormControl(
        { value: authorsRawValue.id, disabled: true },
        {
          nonNullable: true,
          validators: [Validators.required],
        },
      ),
      name: new FormControl(authorsRawValue.name, {
        validators: [Validators.required],
      }),
      email: new FormControl(authorsRawValue.email),
      url: new FormControl(authorsRawValue.url),
    });
  }

  getAuthors(form: AuthorsFormGroup): IAuthors | NewAuthors {
    return form.getRawValue() as IAuthors | NewAuthors;
  }

  resetForm(form: AuthorsFormGroup, authors: AuthorsFormGroupInput): void {
    const authorsRawValue = { ...this.getFormDefaults(), ...authors };
    form.reset(
      {
        ...authorsRawValue,
        id: { value: authorsRawValue.id, disabled: true },
      } as any /* cast to workaround https://github.com/angular/angular/issues/46458 */,
    );
  }

  private getFormDefaults(): AuthorsFormDefaults {
    return {
      id: null,
    };
  }
}
