import { Injectable } from '@angular/core';
import { FormGroup, FormControl, Validators } from '@angular/forms';

import { ITags, NewTags } from '../tags.model';

/**
 * A partial Type with required key is used as form input.
 */
type PartialWithRequiredKeyOf<T extends { id: unknown }> = Partial<Omit<T, 'id'>> & { id: T['id'] };

/**
 * Type for createFormGroup and resetForm argument.
 * It accepts ITags for edit and NewTagsFormGroupInput for create.
 */
type TagsFormGroupInput = ITags | PartialWithRequiredKeyOf<NewTags>;

type TagsFormDefaults = Pick<NewTags, 'id'>;

type TagsFormGroupContent = {
  id: FormControl<ITags['id'] | NewTags['id']>;
  name: FormControl<ITags['name']>;
  description: FormControl<ITags['description']>;
};

export type TagsFormGroup = FormGroup<TagsFormGroupContent>;

@Injectable({ providedIn: 'root' })
export class TagsFormService {
  createTagsFormGroup(tags: TagsFormGroupInput = { id: null }): TagsFormGroup {
    const tagsRawValue = {
      ...this.getFormDefaults(),
      ...tags,
    };
    return new FormGroup<TagsFormGroupContent>({
      id: new FormControl(
        { value: tagsRawValue.id, disabled: true },
        {
          nonNullable: true,
          validators: [Validators.required],
        },
      ),
      name: new FormControl(tagsRawValue.name, {
        validators: [Validators.required],
      }),
      description: new FormControl(tagsRawValue.description),
    });
  }

  getTags(form: TagsFormGroup): ITags | NewTags {
    return form.getRawValue() as ITags | NewTags;
  }

  resetForm(form: TagsFormGroup, tags: TagsFormGroupInput): void {
    const tagsRawValue = { ...this.getFormDefaults(), ...tags };
    form.reset(
      {
        ...tagsRawValue,
        id: { value: tagsRawValue.id, disabled: true },
      } as any /* cast to workaround https://github.com/angular/angular/issues/46458 */,
    );
  }

  private getFormDefaults(): TagsFormDefaults {
    return {
      id: null,
    };
  }
}
