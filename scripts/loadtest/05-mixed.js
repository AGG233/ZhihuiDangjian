import http from 'k6/http';
import { sleep } from 'k6';
import { BASE_URL, TEST_USERS, login, getAuthParams, checkOk } from './shared.js';

export const options = {
  stages: [
    { duration: '30s', target: 20 },
    { duration: '2m', target: 50 },
    { duration: '2m', target: 80 },
    { duration: '1m', target: 0 },
  ],
  thresholds: {
    http_req_duration: ['p(95)<2000', 'p(99)<5000'],
    http_req_failed: ['rate<0.01'],
  },
};

const READ_ENDPOINTS = [
  { url: BASE_URL + '/api/school/all', name: 'school-all' },
  { url: BASE_URL + '/api/content/categories', name: 'categories' },
  { url: BASE_URL + '/api/content/courses', name: 'courses' },
  { url: BASE_URL + '/api/search/courses?keyword=test', name: 'search' },
  { url: BASE_URL + '/api/resource/banners', name: 'banners' },
];

export function setup() {
  return TEST_USERS.map(function (u) { return login(u); }).filter(Boolean);
}

export default function (tokens) {
  const rand = Math.random();

  if (rand < 0.6) {
    const token = tokens[Math.floor(Math.random() * tokens.length)];
    if (!token) return;
    const ep = READ_ENDPOINTS[Math.floor(Math.random() * READ_ENDPOINTS.length)];
    const res = http.get(ep.url, getAuthParams(token));
    checkOk(res, ep.name);
  } else if (rand < 0.85) {
    const token3 = tokens[Math.floor(Math.random() * tokens.length)];
    if (!token3) return;
    const res3 = http.post(
      BASE_URL + '/api/user/users/search',
      JSON.stringify({}),
      getAuthParams(token3),
    );
    checkOk(res3, 'user-search');
  } else {
    const token4 = tokens[Math.floor(Math.random() * tokens.length)];
    if (!token4) return;
    const res4 = http.get(BASE_URL + '/api/graph/knowledge-graphs/users/1', getAuthParams(token4));
    checkOk(res4, 'knowledge-graph');
  }

  sleep(Math.random() * 1 + 0.5);
}
