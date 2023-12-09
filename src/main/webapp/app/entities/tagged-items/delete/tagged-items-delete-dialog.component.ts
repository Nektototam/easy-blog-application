import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';

import SharedModule from 'app/shared/shared.module';
import { ITEM_DELETED_EVENT } from 'app/config/navigation.constants';
import { ITaggedItems } from '../tagged-items.model';
import { TaggedItemsService } from '../service/tagged-items.service';

@Component({
  standalone: true,
  templateUrl: './tagged-items-delete-dialog.component.html',
  imports: [SharedModule, FormsModule],
})
export class TaggedItemsDeleteDialogComponent {
  taggedItems?: ITaggedItems;

  constructor(
    protected taggedItemsService: TaggedItemsService,
    protected activeModal: NgbActiveModal,
  ) {}

  cancel(): void {
    this.activeModal.dismiss();
  }

  confirmDelete(id: number): void {
    this.taggedItemsService.delete(id).subscribe(() => {
      this.activeModal.close(ITEM_DELETED_EVENT);
    });
  }
}
