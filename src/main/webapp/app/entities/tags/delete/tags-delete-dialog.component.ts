import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';

import SharedModule from 'app/shared/shared.module';
import { ITEM_DELETED_EVENT } from 'app/config/navigation.constants';
import { ITags } from '../tags.model';
import { TagsService } from '../service/tags.service';

@Component({
  standalone: true,
  templateUrl: './tags-delete-dialog.component.html',
  imports: [SharedModule, FormsModule],
})
export class TagsDeleteDialogComponent {
  tags?: ITags;

  constructor(
    protected tagsService: TagsService,
    protected activeModal: NgbActiveModal,
  ) {}

  cancel(): void {
    this.activeModal.dismiss();
  }

  confirmDelete(id: number): void {
    this.tagsService.delete(id).subscribe(() => {
      this.activeModal.close(ITEM_DELETED_EVENT);
    });
  }
}
