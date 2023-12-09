import { Component, OnInit } from '@angular/core';
import { HttpResponse } from '@angular/common/http';
import { ActivatedRoute } from '@angular/router';
import { Observable } from 'rxjs';
import { finalize, map } from 'rxjs/operators';

import SharedModule from 'app/shared/shared.module';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';

import { AlertError } from 'app/shared/alert/alert-error.model';
import { EventManager, EventWithContent } from 'app/core/util/event-manager.service';
import { DataUtils, FileLoadError } from 'app/core/util/data-util.service';
import { IUsers } from 'app/entities/users/users.model';
import { UsersService } from 'app/entities/users/service/users.service';
import { Status } from 'app/entities/enumerations/status.model';
import { Visibility } from 'app/entities/enumerations/visibility.model';
import { PostsService } from '../service/posts.service';
import { IPosts } from '../posts.model';
import { PostsFormService, PostsFormGroup } from './posts-form.service';

@Component({
  standalone: true,
  selector: 'jhi-posts-update',
  templateUrl: './posts-update.component.html',
  imports: [SharedModule, FormsModule, ReactiveFormsModule],
})
export class PostsUpdateComponent implements OnInit {
  isSaving = false;
  posts: IPosts | null = null;
  statusValues = Object.keys(Status);
  visibilityValues = Object.keys(Visibility);

  usersSharedCollection: IUsers[] = [];

  editForm: PostsFormGroup = this.postsFormService.createPostsFormGroup();

  constructor(
    protected dataUtils: DataUtils,
    protected eventManager: EventManager,
    protected postsService: PostsService,
    protected postsFormService: PostsFormService,
    protected usersService: UsersService,
    protected activatedRoute: ActivatedRoute,
  ) {}

  compareUsers = (o1: IUsers | null, o2: IUsers | null): boolean => this.usersService.compareUsers(o1, o2);

  ngOnInit(): void {
    this.activatedRoute.data.subscribe(({ posts }) => {
      this.posts = posts;
      if (posts) {
        this.updateForm(posts);
      }

      this.loadRelationshipsOptions();
    });
  }

  byteSize(base64String: string): string {
    return this.dataUtils.byteSize(base64String);
  }

  openFile(base64String: string, contentType: string | null | undefined): void {
    this.dataUtils.openFile(base64String, contentType);
  }

  setFileData(event: Event, field: string, isImage: boolean): void {
    this.dataUtils.loadFileToForm(event, this.editForm, field, isImage).subscribe({
      error: (err: FileLoadError) =>
        this.eventManager.broadcast(new EventWithContent<AlertError>('easyBlogApp.error', { ...err, key: 'error.file.' + err.key })),
    });
  }

  previousState(): void {
    window.history.back();
  }

  save(): void {
    this.isSaving = true;
    const posts = this.postsFormService.getPosts(this.editForm);
    if (posts.id !== null) {
      this.subscribeToSaveResponse(this.postsService.update(posts));
    } else {
      this.subscribeToSaveResponse(this.postsService.create(posts));
    }
  }

  protected subscribeToSaveResponse(result: Observable<HttpResponse<IPosts>>): void {
    result.pipe(finalize(() => this.onSaveFinalize())).subscribe({
      next: () => this.onSaveSuccess(),
      error: () => this.onSaveError(),
    });
  }

  protected onSaveSuccess(): void {
    this.previousState();
  }

  protected onSaveError(): void {
    // Api for inheritance.
  }

  protected onSaveFinalize(): void {
    this.isSaving = false;
  }

  protected updateForm(posts: IPosts): void {
    this.posts = posts;
    this.postsFormService.resetForm(this.editForm, posts);

    this.usersSharedCollection = this.usersService.addUsersToCollectionIfMissing<IUsers>(
      this.usersSharedCollection,
      ...(posts.authors ?? []),
    );
  }

  protected loadRelationshipsOptions(): void {
    this.usersService
      .query()
      .pipe(map((res: HttpResponse<IUsers[]>) => res.body ?? []))
      .pipe(map((users: IUsers[]) => this.usersService.addUsersToCollectionIfMissing<IUsers>(users, ...(this.posts?.authors ?? []))))
      .subscribe((users: IUsers[]) => (this.usersSharedCollection = users));
  }
}
