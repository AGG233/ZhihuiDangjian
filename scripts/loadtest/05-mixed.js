import http from 'k6/http';
import { sleep } from 'k6';
import { BASE_URL, loginWithPreset, registerAndLogin, getAuthParams, checkOk, handleSummary } from './shared.js';

export { handleSummary };

export const options = {
  stages: [
    { duration: '30s', target: 200 },
    { duration: '2m', target: 500 },
    { duration: '2m', target: 800 },
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
  var token = loginWithPreset();
  if (!token) {
    token = registerAndLogin();
  }
  return { token: token };
}

export default function (data) {
  if (!data.token) return;

  var rand = Math.random();

  if (rand < 0.6) {
    var ep = READ_ENDPOINTS[Math.floor(Math.random() * READ_ENDPOINTS.length)];
    var res = http.get(ep.url, getAuthParams(data.token));
    checkOk(res, ep.name);
  } else if (rand < 0.85) {
    var res2 = http.post(
      BASE_URL + '/api/user/users/search',
      JSON.stringify({}),
      getAuthParams(data.token),
    );
    checkOk(res2, 'user-search');
  } else {
    var res3 = http.get(BASE_URL + '/api/graph/knowledge-graphs/users/1', getAuthParams(data.token));
    checkOk(res3, 'knowledge-graph');
  }

  sleep(Math.random() * 1 + 0.5);
}
