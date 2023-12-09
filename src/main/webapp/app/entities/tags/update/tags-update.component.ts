import { Component, OnInit } from '@angular/core';
import { HttpResponse } from '@angular/common/http';
import { ActivatedRoute } from '@angular/router';
import { Observable } from 'rxjs';
import { finalize } from 'rxjs/operators';

import SharedModule from 'app/shared/shared.module';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';

import { ITags } from '../tags.model';
import { TagsService } from '../service/tags.service';
import { TagsFormService, TagsFormGroup } from './tags-form.service';

@Component({
  standalone: true,
  selector: 'jhi-tags-update',
  templateUrl: './tags-update.component.html',
  imports: [SharedModule, FormsModule, ReactiveFormsModule],
})
export class TagsUpdateComponent implements OnInit {
  isSaving = false;
  tags: ITags | null = null;

  editForm: TagsFormGroup = this.tagsFormService.createTagsFormGroup();

  constructor(
    protected tagsService: TagsService,
    protected tagsFormService: TagsFormService,
    protected activatedRoute: ActivatedRoute,
  ) {}

  ngOnInit(): void {
    this.activatedRoute.data.subscribe(({ tags }) => {
      this.tags = tags;
      if (tags) {
        this.updateForm(tags);
      }
    });
  }

  previousState(): void {
    window.history.back();
  }

  save(): void {
    this.isSaving = true;
    const tags = this.tagsFormService.getTags(this.editForm);
    if (tags.id !== null) {
      this.subscribeToSaveResponse(this.tagsService.update(tags));
    } else {
      this.subscribeToSaveResponse(this.tagsService.create(tags));
    }
  }

  protected subscribeToSaveResponse(result: Observable<HttpResponse<ITags>>): void {
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

  protected updateForm(tags: ITags): void {
    this.tags = tags;
    this.tagsFormService.resetForm(this.editForm, tags);
  }
}
