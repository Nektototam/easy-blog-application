import { Injectable } from '@angular/core';
import { FormGroup, FormControl, Validators } from '@angular/forms';

import dayjs from 'dayjs/esm';
import { DATE_TIME_FORMAT } from 'app/config/input.constants';
import { IPosts, NewPosts } from '../posts.model';

/**
 * A partial Type with required key is used as form input.
 */
type PartialWithRequiredKeyOf<T extends { id: unknown }> = Partial<Omit<T, 'id'>> & { id: T['id'] };

/**
 * Type for createFormGroup and resetForm argument.
 * It accepts IPosts for edit and NewPostsFormGroupInput for create.
 */
type PostsFormGroupInput = IPosts | PartialWithRequiredKeyOf<NewPosts>;

/**
 * Type that converts some properties for forms.
 */
type FormValueOf<T extends IPosts | NewPosts> = Omit<T, 'createdAt' | 'updatedAt' | 'publishedAt'> & {
  createdAt?: string | null;
  updatedAt?: string | null;
  publishedAt?: string | null;
};

type PostsFormRawValue = FormValueOf<IPosts>;

type NewPostsFormRawValue = FormValueOf<NewPosts>;

type PostsFormDefaults = Pick<NewPosts, 'id' | 'createdAt' | 'updatedAt' | 'publishedAt' | 'authors' | 'tags' | 'comments'>;

type PostsFormGroupContent = {
  id: FormControl<PostsFormRawValue['id'] | NewPosts['id']>;
  title: FormControl<PostsFormRawValue['title']>;
  slug: FormControl<PostsFormRawValue['slug']>;
  html: FormControl<PostsFormRawValue['html']>;
  createdAt: FormControl<PostsFormRawValue['createdAt']>;
  updatedAt: FormControl<PostsFormRawValue['updatedAt']>;
  publishedAt: FormControl<PostsFormRawValue['publishedAt']>;
  status: FormControl<PostsFormRawValue['status']>;
  visibility: FormControl<PostsFormRawValue['visibility']>;
  authors: FormControl<PostsFormRawValue['authors']>;
  tags: FormControl<PostsFormRawValue['tags']>;
  comments: FormControl<PostsFormRawValue['comments']>;
};

export type PostsFormGroup = FormGroup<PostsFormGroupContent>;

@Injectable({ providedIn: 'root' })
export class PostsFormService {
  createPostsFormGroup(posts: PostsFormGroupInput = { id: null }): PostsFormGroup {
    const postsRawValue = this.convertPostsToPostsRawValue({
      ...this.getFormDefaults(),
      ...posts,
    });
    return new FormGroup<PostsFormGroupContent>({
      id: new FormControl(
        { value: postsRawValue.id, disabled: true },
        {
          nonNullable: true,
          validators: [Validators.required],
        },
      ),
      title: new FormControl(postsRawValue.title),
      slug: new FormControl(postsRawValue.slug),
      html: new FormControl(postsRawValue.html),
      createdAt: new FormControl(postsRawValue.createdAt),
      updatedAt: new FormControl(postsRawValue.updatedAt),
      publishedAt: new FormControl(postsRawValue.publishedAt),
      status: new FormControl(postsRawValue.status),
      visibility: new FormControl(postsRawValue.visibility),
      authors: new FormControl(postsRawValue.authors ?? []),
      tags: new FormControl(postsRawValue.tags ?? []),
      comments: new FormControl(postsRawValue.comments ?? []),
    });
  }

  getPosts(form: PostsFormGroup): IPosts | NewPosts {
    return this.convertPostsRawValueToPosts(form.getRawValue() as PostsFormRawValue | NewPostsFormRawValue);
  }

  resetForm(form: PostsFormGroup, posts: PostsFormGroupInput): void {
    const postsRawValue = this.convertPostsToPostsRawValue({ ...this.getFormDefaults(), ...posts });
    form.reset(
      {
        ...postsRawValue,
        id: { value: postsRawValue.id, disabled: true },
      } as any /* cast to workaround https://github.com/angular/angular/issues/46458 */,
    );
  }

  private getFormDefaults(): PostsFormDefaults {
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

  private convertPostsRawValueToPosts(rawPosts: PostsFormRawValue | NewPostsFormRawValue): IPosts | NewPosts {
    return {
      ...rawPosts,
      createdAt: dayjs(rawPosts.createdAt, DATE_TIME_FORMAT),
      updatedAt: dayjs(rawPosts.updatedAt, DATE_TIME_FORMAT),
      publishedAt: dayjs(rawPosts.publishedAt, DATE_TIME_FORMAT),
    };
  }

  private convertPostsToPostsRawValue(
    posts: IPosts | (Partial<NewPosts> & PostsFormDefaults),
  ): PostsFormRawValue | PartialWithRequiredKeyOf<NewPostsFormRawValue> {
    return {
      ...posts,
      createdAt: posts.createdAt ? posts.createdAt.format(DATE_TIME_FORMAT) : undefined,
      updatedAt: posts.updatedAt ? posts.updatedAt.format(DATE_TIME_FORMAT) : undefined,
      publishedAt: posts.publishedAt ? posts.publishedAt.format(DATE_TIME_FORMAT) : undefined,
      authors: posts.authors ?? [],
      tags: posts.tags ?? [],
      comments: posts.comments ?? [],
    };
  }
}
