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
import { IAuthors } from 'app/entities/authors/authors.model';
import { AuthorsService } from 'app/entities/authors/service/authors.service';
import { IPosts } from 'app/entities/posts/posts.model';
import { PostsService } from 'app/entities/posts/service/posts.service';
import { IPages } from 'app/entities/pages/pages.model';
import { PagesService } from 'app/entities/pages/service/pages.service';
import { Status } from 'app/entities/enumerations/status.model';
import { CommentsService } from '../service/comments.service';
import { IComments } from '../comments.model';
import { CommentsFormService, CommentsFormGroup } from './comments-form.service';

@Component({
  standalone: true,
  selector: 'jhi-comments-update',
  templateUrl: './comments-update.component.html',
  imports: [SharedModule, FormsModule, ReactiveFormsModule],
})
export class CommentsUpdateComponent implements OnInit {
  isSaving = false;
  comments: IComments | null = null;
  statusValues = Object.keys(Status);

  authorsSharedCollection: IAuthors[] = [];
  postsSharedCollection: IPosts[] = [];
  pagesSharedCollection: IPages[] = [];
  commentsSharedCollection: IComments[] = [];

  editForm: CommentsFormGroup = this.commentsFormService.createCommentsFormGroup();

  constructor(
    protected dataUtils: DataUtils,
    protected eventManager: EventManager,
    protected commentsService: CommentsService,
    protected commentsFormService: CommentsFormService,
    protected authorsService: AuthorsService,
    protected postsService: PostsService,
    protected pagesService: PagesService,
    protected activatedRoute: ActivatedRoute,
  ) {}

  compareAuthors = (o1: IAuthors | null, o2: IAuthors | null): boolean => this.authorsService.compareAuthors(o1, o2);

  comparePosts = (o1: IPosts | null, o2: IPosts | null): boolean => this.postsService.comparePosts(o1, o2);

  comparePages = (o1: IPages | null, o2: IPages | null): boolean => this.pagesService.comparePages(o1, o2);

  compareComments = (o1: IComments | null, o2: IComments | null): boolean => this.commentsService.compareComments(o1, o2);

  ngOnInit(): void {
    this.activatedRoute.data.subscribe(({ comments }) => {
      this.comments = comments;
      if (comments) {
        this.updateForm(comments);
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
    const comments = this.commentsFormService.getComments(this.editForm);
    if (comments.id !== null) {
      this.subscribeToSaveResponse(this.commentsService.update(comments));
    } else {
      this.subscribeToSaveResponse(this.commentsService.create(comments));
    }
  }

  protected subscribeToSaveResponse(result: Observable<HttpResponse<IComments>>): void {
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

  protected updateForm(comments: IComments): void {
    this.comments = comments;
    this.commentsFormService.resetForm(this.editForm, comments);

    this.authorsSharedCollection = this.authorsService.addAuthorsToCollectionIfMissing<IAuthors>(
      this.authorsSharedCollection,
      comments.author,
    );
    this.postsSharedCollection = this.postsService.addPostsToCollectionIfMissing<IPosts>(
      this.postsSharedCollection,
      ...(comments.posts ?? []),
    );
    this.pagesSharedCollection = this.pagesService.addPagesToCollectionIfMissing<IPages>(
      this.pagesSharedCollection,
      ...(comments.pages ?? []),
    );
    this.commentsSharedCollection = this.commentsService.addCommentsToCollectionIfMissing<IComments>(
      this.commentsSharedCollection,
      ...(comments.parents ?? []),
    );
  }

  protected loadRelationshipsOptions(): void {
    this.authorsService
      .query()
      .pipe(map((res: HttpResponse<IAuthors[]>) => res.body ?? []))
      .pipe(map((authors: IAuthors[]) => this.authorsService.addAuthorsToCollectionIfMissing<IAuthors>(authors, this.comments?.author)))
      .subscribe((authors: IAuthors[]) => (this.authorsSharedCollection = authors));

    this.postsService
      .query()
      .pipe(map((res: HttpResponse<IPosts[]>) => res.body ?? []))
      .pipe(map((posts: IPosts[]) => this.postsService.addPostsToCollectionIfMissing<IPosts>(posts, ...(this.comments?.posts ?? []))))
      .subscribe((posts: IPosts[]) => (this.postsSharedCollection = posts));

    this.pagesService
      .query()
      .pipe(map((res: HttpResponse<IPages[]>) => res.body ?? []))
      .pipe(map((pages: IPages[]) => this.pagesService.addPagesToCollectionIfMissing<IPages>(pages, ...(this.comments?.pages ?? []))))
      .subscribe((pages: IPages[]) => (this.pagesSharedCollection = pages));

    this.commentsService
      .query()
      .pipe(map((res: HttpResponse<IComments[]>) => res.body ?? []))
      .pipe(
        map((comments: IComments[]) =>
          this.commentsService.addCommentsToCollectionIfMissing<IComments>(comments, ...(this.comments?.parents ?? [])),
        ),
      )
      .subscribe((comments: IComments[]) => (this.commentsSharedCollection = comments));
  }
}
