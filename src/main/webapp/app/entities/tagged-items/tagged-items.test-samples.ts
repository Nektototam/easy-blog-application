import { ITaggedItems, NewTaggedItems } from './tagged-items.model';

export const sampleWithRequiredData: ITaggedItems = {
  id: 29304,
  itemType: 'provided ottoman uncomfortable',
};

export const sampleWithPartialData: ITaggedItems = {
  id: 16602,
  itemType: 'behind',
};

export const sampleWithFullData: ITaggedItems = {
  id: 20288,
  itemType: 'lest',
};

export const sampleWithNewData: NewTaggedItems = {
  itemType: 'requisition eek',
  id: null,
};

Object.freeze(sampleWithNewData);
Object.freeze(sampleWithRequiredData);
Object.freeze(sampleWithPartialData);
Object.freeze(sampleWithFullData);
