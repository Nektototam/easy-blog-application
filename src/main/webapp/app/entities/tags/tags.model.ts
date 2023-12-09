export interface ITags {
  id: number;
  name?: string | null;
  description?: string | null;
}

export type NewTags = Omit<ITags, 'id'> & { id: null };
