import { Injectable } from '@angular/core';
import { FormGroup, FormControl, Validators } from '@angular/forms';

import { IItemTypes, NewItemTypes } from '../item-types.model';

/**
 * A partial Type with required key is used as form input.
 */
type PartialWithRequiredKeyOf<T extends { id: unknown }> = Partial<Omit<T, 'id'>> & { id: T['id'] };

/**
 * Type for createFormGroup and resetForm argument.
 * It accepts IItemTypes for edit and NewItemTypesFormGroupInput for create.
 */
type ItemTypesFormGroupInput = IItemTypes | PartialWithRequiredKeyOf<NewItemTypes>;

type ItemTypesFormDefaults = Pick<NewItemTypes, 'id'>;

type ItemTypesFormGroupContent = {
  id: FormControl<IItemTypes['id'] | NewItemTypes['id']>;
  name: FormControl<IItemTypes['name']>;
};

export type ItemTypesFormGroup = FormGroup<ItemTypesFormGroupContent>;

@Injectable({ providedIn: 'root' })
export class ItemTypesFormService {
  createItemTypesFormGroup(itemTypes: ItemTypesFormGroupInput = { id: null }): ItemTypesFormGroup {
    const itemTypesRawValue = {
      ...this.getFormDefaults(),
      ...itemTypes,
    };
    return new FormGroup<ItemTypesFormGroupContent>({
      id: new FormControl(
        { value: itemTypesRawValue.id, disabled: true },
        {
          nonNullable: true,
          validators: [Validators.required],
        },
      ),
      name: new FormControl(itemTypesRawValue.name, {
        validators: [Validators.required],
      }),
    });
  }

  getItemTypes(form: ItemTypesFormGroup): IItemTypes | NewItemTypes {
    return form.getRawValue() as IItemTypes | NewItemTypes;
  }

  resetForm(form: ItemTypesFormGroup, itemTypes: ItemTypesFormGroupInput): void {
    const itemTypesRawValue = { ...this.getFormDefaults(), ...itemTypes };
    form.reset(
      {
        ...itemTypesRawValue,
        id: { value: itemTypesRawValue.id, disabled: true },
      } as any /* cast to workaround https://github.com/angular/angular/issues/46458 */,
    );
  }

  private getFormDefaults(): ItemTypesFormDefaults {
    return {
      id: null,
    };
  }
}
