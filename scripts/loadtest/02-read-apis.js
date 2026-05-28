import http from 'k6/http';
import { check, sleep, group } from 'k6';
import { BASE_URL, TEST_USERS, login, getAuthParams, checkOk } from './shared.js';

export const options = {
  stages: [
    { duration: '30s', target: 50 },
    { duration: '30s', target: 100 },
    { duration: '30s', target: 200 },
    { duration: '30s', target: 0 },
  ],
  thresholds: {
    http_req_duration: ['p(95)<500', 'p(99)<2000'],
    http_req_failed: ['rate<0.01'],
  },
};

const ENDPOINTS = [
  { url: BASE_URL + '/api/school/all', name: 'school-list' },
  { url: BASE_URL + '/api/content/categories', name: 'categories' },
  { url: BASE_URL + '/api/content/courses', name: 'courses' },
  { url: BASE_URL + '/api/resource/banners', name: 'banners' },
  { url: BASE_URL + '/api/content/content-blocks/carousel', name: 'carousel' },
];

export function setup() {
  return TEST_USERS.map(function (u) { return login(u); }).filter(Boolean);
}

export default function (tokens) {
  var token = tokens[Math.floor(Math.random() * tokens.length)];
  if (!token) return;

  var endpoint = ENDPOINTS[Math.floor(Math.random() * ENDPOINTS.length)];
  group(endpoint.name, function () {
    var res = http.get(endpoint.url, getAuthParams(token));
    checkOk(res, endpoint.name);
  });
  sleep(Math.random() * 0.5 + 0.1);
}
