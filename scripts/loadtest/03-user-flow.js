import http from 'k6/http';
import { sleep, group } from 'k6';
import { BASE_URL, loginWithPreset, registerAndLogin, getAuthParams, checkOk, handleSummary } from './shared.js';

export { handleSummary };

export const options = {
  stages: [
    { duration: '30s', target: 100 },
    { duration: '2m', target: 300 },
    { duration: '2m', target: 500 },
    { duration: '30s', target: 0 },
  ],
  thresholds: {
    http_req_duration: ['p(95)<2000', 'p(99)<5000'],
    http_req_failed: ['rate<0.02'],
  },
};

export function setup() {
  var token = loginWithPreset();
  if (!token) {
    token = registerAndLogin();
  }
  return { token: token };
}

export default function (data) {
  if (!data.token) return;

  group('full-user-flow', function () {
    var catRes = http.get(BASE_URL + '/api/content/categories', getAuthParams(data.token));
    checkOk(catRes, 'categories');

    var courseRes = http.get(BASE_URL + '/api/content/courses', getAuthParams(data.token));
    checkOk(courseRes, 'courses');

    var searchRes = http.get(BASE_URL + '/api/search/courses?keyword=test', getAuthParams(data.token));
    checkOk(searchRes, 'search');

    var progressRes = http.post(
      BASE_URL + '/api/learning/progress',
      JSON.stringify({ chapterId: '1', progress: 50 }),
      getAuthParams(data.token),
    );
    checkOk(progressRes, 'progress');
  });

  sleep(2);
}
