import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';

import SharedModule from 'app/shared/shared.module';
import { ITEM_DELETED_EVENT } from 'app/config/navigation.constants';
import { IPosts } from '../posts.model';
import { PostsService } from '../service/posts.service';

@Component({
  standalone: true,
  templateUrl: './posts-delete-dialog.component.html',
  imports: [SharedModule, FormsModule],
})
export class PostsDeleteDialogComponent {
  posts?: IPosts;

  constructor(
    protected postsService: PostsService,
    protected activeModal: NgbActiveModal,
  ) {}

  cancel(): void {
    this.activeModal.dismiss();
  }

  confirmDelete(id: number): void {
    this.postsService.delete(id).subscribe(() => {
      this.activeModal.close(ITEM_DELETED_EVENT);
    });
  }
}
