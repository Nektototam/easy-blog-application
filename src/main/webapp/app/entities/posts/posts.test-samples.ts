import dayjs from 'dayjs/esm';

import { IPosts, NewPosts } from './posts.model';

export const sampleWithRequiredData: IPosts = {
  id: 30814,
};

export const sampleWithPartialData: IPosts = {
  id: 9721,
  createdAt: dayjs('2023-12-08T20:33'),
  updatedAt: dayjs('2023-12-08T06:38'),
  visibility: 'PRIVATE',
};

export const sampleWithFullData: IPosts = {
  id: 28613,
  title: 'apud nor fair',
  slug: 'ack if insist',
  html: '../fake-data/blob/hipster.txt',
  createdAt: dayjs('2023-12-08T13:58'),
  updatedAt: dayjs('2023-12-08T09:09'),
  publishedAt: dayjs('2023-12-08T07:50'),
  status: 'UNAPPROVED',
  visibility: 'PRIVATE',
};

export const sampleWithNewData: NewPosts = {
  id: null,
};

Object.freeze(sampleWithNewData);
Object.freeze(sampleWithRequiredData);
Object.freeze(sampleWithPartialData);
Object.freeze(sampleWithFullData);
