import { Injectable } from '@angular/core';
import { FormGroup, FormControl, Validators } from '@angular/forms';

import dayjs from 'dayjs/esm';
import { DATE_TIME_FORMAT } from 'app/config/input.constants';
import { IPages, NewPages } from '../pages.model';

/**
 * A partial Type with required key is used as form input.
 */
type PartialWithRequiredKeyOf<T extends { id: unknown }> = Partial<Omit<T, 'id'>> & { id: T['id'] };

/**
 * Type for createFormGroup and resetForm argument.
 * It accepts IPages for edit and NewPagesFormGroupInput for create.
 */
type PagesFormGroupInput = IPages | PartialWithRequiredKeyOf<NewPages>;

/**
 * Type that converts some properties for forms.
 */
type FormValueOf<T extends IPages | NewPages> = Omit<T, 'createdAt' | 'updatedAt' | 'publishedAt'> & {
  createdAt?: string | null;
  updatedAt?: string | null;
  publishedAt?: string | null;
};

type PagesFormRawValue = FormValueOf<IPages>;

type NewPagesFormRawValue = FormValueOf<NewPages>;

type PagesFormDefaults = Pick<NewPages, 'id' | 'createdAt' | 'updatedAt' | 'publishedAt' | 'authors' | 'tags' | 'comments'>;

type PagesFormGroupContent = {
  id: FormControl<PagesFormRawValue['id'] | NewPages['id']>;
  title: FormControl<PagesFormRawValue['title']>;
  slug: FormControl<PagesFormRawValue['slug']>;
  html: FormControl<PagesFormRawValue['html']>;
  createdAt: FormControl<PagesFormRawValue['createdAt']>;
  updatedAt: FormControl<PagesFormRawValue['updatedAt']>;
  publishedAt: FormControl<PagesFormRawValue['publishedAt']>;
  status: FormControl<PagesFormRawValue['status']>;
  visibility: FormControl<PagesFormRawValue['visibility']>;
  authors: FormControl<PagesFormRawValue['authors']>;
  tags: FormControl<PagesFormRawValue['tags']>;
  comments: FormControl<PagesFormRawValue['comments']>;
};

export type PagesFormGroup = FormGroup<PagesFormGroupContent>;

@Injectable({ providedIn: 'root' })
export class PagesFormService {
  createPagesFormGroup(pages: PagesFormGroupInput = { id: null }): PagesFormGroup {
    const pagesRawValue = this.convertPagesToPagesRawValue({
      ...this.getFormDefaults(),
      ...pages,
    });
    return new FormGroup<PagesFormGroupContent>({
      id: new FormControl(
        { value: pagesRawValue.id, disabled: true },
        {
          nonNullable: true,
          validators: [Validators.required],
        },
      ),
      title: new FormControl(pagesRawValue.title),
      slug: new FormControl(pagesRawValue.slug),
      html: new FormControl(pagesRawValue.html),
      createdAt: new FormControl(pagesRawValue.createdAt),
      updatedAt: new FormControl(pagesRawValue.updatedAt),
      publishedAt: new FormControl(pagesRawValue.publishedAt),
      status: new FormControl(pagesRawValue.status),
      visibility: new FormControl(pagesRawValue.visibility),
      authors: new FormControl(pagesRawValue.authors ?? []),
      tags: new FormControl(pagesRawValue.tags ?? []),
      comments: new FormControl(pagesRawValue.comments ?? []),
    });
  }

  getPages(form: PagesFormGroup): IPages | NewPages {
    return this.convertPagesRawValueToPages(form.getRawValue() as PagesFormRawValue | NewPagesFormRawValue);
  }

  resetForm(form: PagesFormGroup, pages: PagesFormGroupInput): void {
    const pagesRawValue = this.convertPagesToPagesRawValue({ ...this.getFormDefaults(), ...pages });
    form.reset(
      {
        ...pagesRawValue,
        id: { value: pagesRawValue.id, disabled: true },
      } as any /* cast to workaround https://github.com/angular/angular/issues/46458 */,
    );
  }

  private getFormDefaults(): PagesFormDefaults {
    const currentTime = dayjs();

    return {
      id: null,
      createdAt: currentTime,
      updatedAt: currentTime,
      publishedAt: currentTime,
      authors: [],
      tags: [],
      comments: [],
    };
  }

  private convertPagesRawValueToPages(rawPages: PagesFormRawValue | NewPagesFormRawValue): IPages | NewPages {
    return {
      ...rawPages,
      createdAt: dayjs(rawPages.createdAt, DATE_TIME_FORMAT),
      updatedAt: dayjs(rawPages.updatedAt, DATE_TIME_FORMAT),
      publishedAt: dayjs(rawPages.publishedAt, DATE_TIME_FORMAT),
    };
  }

  private convertPagesToPagesRawValue(
    pages: IPages | (Partial<NewPages> & PagesFormDefaults),
  ): PagesFormRawValue | PartialWithRequiredKeyOf<NewPagesFormRawValue> {
    return {
      ...pages,
      createdAt: pages.createdAt ? pages.createdAt.format(DATE_TIME_FORMAT) : undefined,
      updatedAt: pages.updatedAt ? pages.updatedAt.format(DATE_TIME_FORMAT) : undefined,
      publishedAt: pages.publishedAt ? pages.publishedAt.format(DATE_TIME_FORMAT) : undefined,
      authors: pages.authors ?? [],
      tags: pages.tags ?? [],
      comments: pages.comments ?? [],
    };
  }
}
