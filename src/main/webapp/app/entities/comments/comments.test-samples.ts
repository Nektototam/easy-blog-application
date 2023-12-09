import dayjs from 'dayjs/esm';

import { IComments, NewComments } from './comments.model';

export const sampleWithRequiredData: IComments = {
  id: 30807,
  content: '../fake-data/blob/hipster.txt',
  createdAt: dayjs('2023-12-09T01:59'),
  updatedAt: dayjs('2023-12-08T11:28'),
};

export const sampleWithPartialData: IComments = {
  id: 2714,
  content: '../fake-data/blob/hipster.txt',
  createdAt: dayjs('2023-12-09T03:30'),
  updatedAt: dayjs('2023-12-08T12:10'),
  status: 'APPROVED',
};

export const sampleWithFullData: IComments = {
  id: 4052,
  content: '../fake-data/blob/hipster.txt',
  createdAt: dayjs('2023-12-08T10:03'),
  updatedAt: dayjs('2023-12-09T03:48'),
  status: 'APPROVED',
};

export const sampleWithNewData: NewComments = {
  content: '../fake-data/blob/hipster.txt',
  createdAt: dayjs('2023-12-08T15:23'),
  updatedAt: dayjs('2023-12-08T20:21'),
  id: null,
};

Object.freeze(sampleWithNewData);
Object.freeze(sampleWithRequiredData);
Object.freeze(sampleWithPartialData);
Object.freeze(sampleWithFullData);
