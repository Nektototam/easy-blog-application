import { ITags, NewTags } from './tags.model';

export const sampleWithRequiredData: ITags = {
  id: 30793,
  name: 'engineer',
};

export const sampleWithPartialData: ITags = {
  id: 28618,
  name: 'on some poorly',
  description: 'past',
};

export const sampleWithFullData: ITags = {
  id: 1300,
  name: 'vaguely economise oh',
  description: 'yawningly gun',
};

export const sampleWithNewData: NewTags = {
  name: 'governor subscribe',
  id: null,
};

Object.freeze(sampleWithNewData);
Object.freeze(sampleWithRequiredData);
Object.freeze(sampleWithPartialData);
Object.freeze(sampleWithFullData);
