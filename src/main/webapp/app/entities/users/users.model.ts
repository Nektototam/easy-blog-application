import { IPages } from 'app/entities/pages/pages.model';
import { IPosts } from 'app/entities/posts/posts.model';

export interface IUsers {
  id: number;
  name?: string | null;
  email?: string | null;
  pages?: Pick<IPages, 'id'>[] | null;
  posts?: Pick<IPosts, 'id'>[] | null;
}

export type NewUsers = Omit<IUsers, 'id'> & { id: null };
