import dayjs from 'dayjs/esm';

import { IPages, NewPages } from './pages.model';

export const sampleWithRequiredData: IPages = {
  id: 20704,
};

export const sampleWithPartialData: IPages = {
  id: 16902,
  html: '../fake-data/blob/hipster.txt',
  createdAt: dayjs('2023-12-09T02:07'),
  updatedAt: dayjs('2023-12-08T10:20'),
  publishedAt: dayjs('2023-12-09T02:50'),
  status: 'DRAFT',
};

export const sampleWithFullData: IPages = {
  id: 23759,
  title: 'offbeat',
  slug: 'among',
  html: '../fake-data/blob/hipster.txt',
  createdAt: dayjs('2023-12-09T00:13'),
  updatedAt: dayjs('2023-12-08T23:15'),
  publishedAt: dayjs('2023-12-08T17:02'),
  status: 'APPROVED',
  visibility: 'PRIVATE',
};

export const sampleWithNewData: NewPages = {
  id: null,
};

Object.freeze(sampleWithNewData);
Object.freeze(sampleWithRequiredData);
Object.freeze(sampleWithPartialData);
Object.freeze(sampleWithFullData);
