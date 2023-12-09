export interface IItemTypes {
  id: number;
  name?: string | null;
}

export type NewItemTypes = Omit<IItemTypes, 'id'> & { id: null };
