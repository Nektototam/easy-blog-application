import dayjs from 'dayjs/esm';
import { IUsers } from 'app/entities/users/users.model';
import { ITaggedItems } from 'app/entities/tagged-items/tagged-items.model';
import { IComments } from 'app/entities/comments/comments.model';
import { Status } from 'app/entities/enumerations/status.model';
import { Visibility } from 'app/entities/enumerations/visibility.model';

export interface IPages {
  id: number;
  title?: string | null;
  slug?: string | null;
  html?: string | null;
  createdAt?: dayjs.Dayjs | null;
  updatedAt?: dayjs.Dayjs | null;
  publishedAt?: dayjs.Dayjs | null;
  status?: keyof typeof Status | null;
  visibility?: keyof typeof Visibility | null;
  authors?: Pick<IUsers, 'id'>[] | null;
  tags?: Pick<ITaggedItems, 'id'>[] | null;
  comments?: Pick<IComments, 'id'>[] | null;
}

export type NewPages = Omit<IPages, 'id'> & { id: null };
