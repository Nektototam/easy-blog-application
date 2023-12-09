import { IUsers, NewUsers } from './users.model';

export const sampleWithRequiredData: IUsers = {
  id: 20157,
};

export const sampleWithPartialData: IUsers = {
  id: 760,
};

export const sampleWithFullData: IUsers = {
  id: 15374,
  name: 'trifling even',
  email: 'Marcia.Nienow42@hotmail.com',
};

export const sampleWithNewData: NewUsers = {
  id: null,
};

Object.freeze(sampleWithNewData);
Object.freeze(sampleWithRequiredData);
Object.freeze(sampleWithPartialData);
Object.freeze(sampleWithFullData);
