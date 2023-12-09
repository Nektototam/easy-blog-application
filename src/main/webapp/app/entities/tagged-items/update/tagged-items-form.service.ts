import { Injectable } from '@angular/core';
import { FormGroup, FormControl, Validators } from '@angular/forms';

import { ITaggedItems, NewTaggedItems } from '../tagged-items.model';

/**
 * A partial Type with required key is used as form input.
 */
type PartialWithRequiredKeyOf<T extends { id: unknown }> = Partial<Omit<T, 'id'>> & { id: T['id'] };

/**
 * Type for createFormGroup and resetForm argument.
 * It accepts ITaggedItems for edit and NewTaggedItemsFormGroupInput for create.
 */
type TaggedItemsFormGroupInput = ITaggedItems | PartialWithRequiredKeyOf<NewTaggedItems>;

type TaggedItemsFormDefaults = Pick<NewTaggedItems, 'id' | 'pages' | 'posts'>;

type TaggedItemsFormGroupContent = {
  id: FormControl<ITaggedItems['id'] | NewTaggedItems['id']>;
  itemType: FormControl<ITaggedItems['itemType']>;
  tag: FormControl<ITaggedItems['tag']>;
  item: FormControl<ITaggedItems['item']>;
  pages: FormControl<ITaggedItems['pages']>;
  posts: FormControl<ITaggedItems['posts']>;
};

export type TaggedItemsFormGroup = FormGroup<TaggedItemsFormGroupContent>;

@Injectable({ providedIn: 'root' })
export class TaggedItemsFormService {
  createTaggedItemsFormGroup(taggedItems: TaggedItemsFormGroupInput = { id: null }): TaggedItemsFormGroup {
    const taggedItemsRawValue = {
      ...this.getFormDefaults(),
      ...taggedItems,
    };
    return new FormGroup<TaggedItemsFormGroupContent>({
      id: new FormControl(
        { value: taggedItemsRawValue.id, disabled: true },
        {
          nonNullable: true,
          validators: [Validators.required],
        },
      ),
      itemType: new FormControl(taggedItemsRawValue.itemType, {
        validators: [Validators.required],
      }),
      tag: new FormControl(taggedItemsRawValue.tag),
      item: new FormControl(taggedItemsRawValue.item),
      pages: new FormControl(taggedItemsRawValue.pages ?? []),
      posts: new FormControl(taggedItemsRawValue.posts ?? []),
    });
  }

  getTaggedItems(form: TaggedItemsFormGroup): ITaggedItems | NewTaggedItems {
    return form.getRawValue() as ITaggedItems | NewTaggedItems;
  }

  resetForm(form: TaggedItemsFormGroup, taggedItems: TaggedItemsFormGroupInput): void {
    const taggedItemsRawValue = { ...this.getFormDefaults(), ...taggedItems };
    form.reset(
      {
        ...taggedItemsRawValue,
        id: { value: taggedItemsRawValue.id, disabled: true },
      } as any /* cast to workaround https://github.com/angular/angular/issues/46458 */,
    );
  }

  private getFormDefaults(): TaggedItemsFormDefaults {
    return {
      id: null,
      pages: [],
      posts: [],
    };
  }
}
