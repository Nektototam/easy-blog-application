import { IAuthors, NewAuthors } from './authors.model';

export const sampleWithRequiredData: IAuthors = {
  id: 18412,
  name: 'verifiable whopping',
};

export const sampleWithPartialData: IAuthors = {
  id: 23382,
  name: 'alpenhorn',
  url: 'https://illiterate-celsius.name/',
};

export const sampleWithFullData: IAuthors = {
  id: 19257,
  name: 'however',
  email: 'Alaina52@hotmail.com',
  url: 'https://amusing-waffle.org/',
};

export const sampleWithNewData: NewAuthors = {
  name: 'furthermore an',
  id: null,
};

Object.freeze(sampleWithNewData);
Object.freeze(sampleWithRequiredData);
Object.freeze(sampleWithPartialData);
Object.freeze(sampleWithFullData);
