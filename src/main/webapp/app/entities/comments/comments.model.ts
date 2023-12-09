import dayjs from 'dayjs/esm';
import { IAuthors } from 'app/entities/authors/authors.model';
import { IPosts } from 'app/entities/posts/posts.model';
import { IPages } from 'app/entities/pages/pages.model';
import { Status } from 'app/entities/enumerations/status.model';

export interface IComments {
  id: number;
  content?: string | null;
  createdAt?: dayjs.Dayjs | null;
  updatedAt?: dayjs.Dayjs | null;
  status?: keyof typeof Status | null;
  author?: Pick<IAuthors, 'id'> | null;
  posts?: Pick<IPosts, 'id'>[] | null;
  pages?: Pick<IPages, 'id'>[] | null;
  parents?: Pick<IComments, 'id'>[] | null;
  children?: Pick<IComments, 'id'>[] | null;
}

export type NewComments = Omit<IComments, 'id'> & { id: null };
