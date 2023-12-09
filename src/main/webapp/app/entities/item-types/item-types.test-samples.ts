import { IItemTypes, NewItemTypes } from './item-types.model';

export const sampleWithRequiredData: IItemTypes = {
  id: 21998,
  name: 'splendid evil',
};

export const sampleWithPartialData: IItemTypes = {
  id: 20352,
  name: 'sympathetically ooze amidst',
};

export const sampleWithFullData: IItemTypes = {
  id: 15206,
  name: 'seemingly dial save',
};

export const sampleWithNewData: NewItemTypes = {
  name: 'pish where',
  id: null,
};

Object.freeze(sampleWithNewData);
Object.freeze(sampleWithRequiredData);
Object.freeze(sampleWithPartialData);
Object.freeze(sampleWithFullData);
