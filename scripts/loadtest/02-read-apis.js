import http from 'k6/http';
import { sleep, group } from 'k6';
import { BASE_URL, loginWithPreset, registerAndLogin, getAuthParams, checkOk, handleSummary } from './shared.js';

export { handleSummary };

export const options = {
  stages: [
    { duration: '30s', target: 500 },
    { duration: '1m', target: 1000 },
    { duration: '2m', target: 2000 },
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
  var token = loginWithPreset();
  if (!token) {
    token = registerAndLogin();
  }
  return { token: token };
}

export default function (data) {
  if (!data.token) return;

  var endpoint = ENDPOINTS[Math.floor(Math.random() * ENDPOINTS.length)];
  group(endpoint.name, function () {
    var res = http.get(endpoint.url, getAuthParams(data.token));
    checkOk(res, endpoint.name);
  });
  sleep(Math.random() * 0.5 + 0.1);
}
