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
import { PagesService } from '../service/pages.service';
import { IPages } from '../pages.model';
import { PagesFormService, PagesFormGroup } from './pages-form.service';

@Component({
  standalone: true,
  selector: 'jhi-pages-update',
  templateUrl: './pages-update.component.html',
  imports: [SharedModule, FormsModule, ReactiveFormsModule],
})
export class PagesUpdateComponent implements OnInit {
  isSaving = false;
  pages: IPages | null = null;
  statusValues = Object.keys(Status);
  visibilityValues = Object.keys(Visibility);

  usersSharedCollection: IUsers[] = [];

  editForm: PagesFormGroup = this.pagesFormService.createPagesFormGroup();

  constructor(
    protected dataUtils: DataUtils,
    protected eventManager: EventManager,
    protected pagesService: PagesService,
    protected pagesFormService: PagesFormService,
    protected usersService: UsersService,
    protected activatedRoute: ActivatedRoute,
  ) {}

  compareUsers = (o1: IUsers | null, o2: IUsers | null): boolean => this.usersService.compareUsers(o1, o2);

  ngOnInit(): void {
    this.activatedRoute.data.subscribe(({ pages }) => {
      this.pages = pages;
      if (pages) {
        this.updateForm(pages);
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
    const pages = this.pagesFormService.getPages(this.editForm);
    if (pages.id !== null) {
      this.subscribeToSaveResponse(this.pagesService.update(pages));
    } else {
      this.subscribeToSaveResponse(this.pagesService.create(pages));
    }
  }

  protected subscribeToSaveResponse(result: Observable<HttpResponse<IPages>>): void {
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

  protected updateForm(pages: IPages): void {
    this.pages = pages;
    this.pagesFormService.resetForm(this.editForm, pages);

    this.usersSharedCollection = this.usersService.addUsersToCollectionIfMissing<IUsers>(
      this.usersSharedCollection,
      ...(pages.authors ?? []),
    );
  }

  protected loadRelationshipsOptions(): void {
    this.usersService
      .query()
      .pipe(map((res: HttpResponse<IUsers[]>) => res.body ?? []))
      .pipe(map((users: IUsers[]) => this.usersService.addUsersToCollectionIfMissing<IUsers>(users, ...(this.pages?.authors ?? []))))
      .subscribe((users: IUsers[]) => (this.usersSharedCollection = users));
  }
}
