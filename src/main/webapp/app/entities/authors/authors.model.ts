export interface IAuthors {
  id: number;
  name?: string | null;
  email?: string | null;
  url?: string | null;
}

export type NewAuthors = Omit<IAuthors, 'id'> & { id: null };
