import http from 'k6/http';
import { sleep, group } from 'k6';
import { BASE_URL, TEST_USERS, login, getAuthParams, checkOk } from './shared.js';

export const options = {
  stages: [
    { duration: '30s', target: 10 },
    { duration: '60s', target: 30 },
    { duration: '60s', target: 50 },
    { duration: '30s', target: 0 },
  ],
  thresholds: {
    http_req_duration: ['p(95)<2000', 'p(99)<5000'],
    http_req_failed: ['rate<0.02'],
  },
};

export function setup() {
  return TEST_USERS.map(function (u) { return login(u); }).filter(Boolean);
}

export default function (tokens) {
  let token = tokens[Math.floor(Math.random() * tokens.length)];
  if (!token) return;

  group('full-user-flow', function () {
    let catRes = http.get(BASE_URL + '/api/content/categories', getAuthParams(token));
    checkOk(catRes, 'categories');

    let courseRes = http.get(BASE_URL + '/api/content/courses', getAuthParams(token));
    checkOk(courseRes, 'courses');

    let searchRes = http.get(BASE_URL + '/api/search/courses?keyword=test', getAuthParams(token));
    checkOk(searchRes, 'search');

    let progressRes = http.post(
      BASE_URL + '/api/learning/progress',
      JSON.stringify({ chapterId: '1', progress: 50 }),
      getAuthParams(token),
    );
    checkOk(progressRes, 'progress');
  });

  sleep(2);
}
