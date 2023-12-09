import { ITags } from 'app/entities/tags/tags.model';
import { IItemTypes } from 'app/entities/item-types/item-types.model';
import { IPages } from 'app/entities/pages/pages.model';
import { IPosts } from 'app/entities/posts/posts.model';

export interface ITaggedItems {
  id: number;
  itemType?: string | null;
  tag?: Pick<ITags, 'id'> | null;
  item?: Pick<IItemTypes, 'id'> | null;
  pages?: Pick<IPages, 'id'>[] | null;
  posts?: Pick<IPosts, 'id'>[] | null;
}

export type NewTaggedItems = Omit<ITaggedItems, 'id'> & { id: null };
