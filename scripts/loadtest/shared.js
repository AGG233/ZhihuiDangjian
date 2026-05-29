import http from 'k6/http';
import { check, sleep } from 'k6';

export const BASE_URL = 'http://localhost:9000';

export const TEST_USERS = [
  { passport: 'loadtest01', password: 'LoadTest123!' },
  { passport: 'loadtest02', password: 'LoadTest123!' },
  { passport: 'loadtest03', password: 'LoadTest123!' },
  { passport: 'loadtest04', password: 'LoadTest123!' },
  { passport: 'loadtest05', password: 'LoadTest123!' },
  { passport: 'loadtest06', password: 'LoadTest123!' },
  { passport: 'loadtest07', password: 'LoadTest123!' },
  { passport: 'loadtest08', password: 'LoadTest123!' },
  { passport: 'loadtest09', password: 'LoadTest123!' },
  { passport: 'loadtest10', password: 'LoadTest123!' },
];

export const TEST_CAPTCHA_CODE = 'TEST8888';
export const TEST_CAPTCHA_UUID = 'loadtest-captcha-uuid';

export function getAuthParams(token) {
  return {
    headers: {
      'Authorization': 'Bearer ' + token,
      'Content-Type': 'application/json',
    },
  };
}

export function randomUser() {
  return TEST_USERS[Math.floor(Math.random() * TEST_USERS.length)];
}

export function login(user) {
  const payload = JSON.stringify({
    passport: user.passport,
    password: user.password,
    captchaUUID: TEST_CAPTCHA_UUID,
    captchaCode: TEST_CAPTCHA_CODE,
    platform: 'web',
  });
  const res = http.post(BASE_URL + '/api/auth/login', payload, {
    headers: { 'Content-Type': 'application/json' },
  });
  check(res, { 'login success': (r) => r.status === 200 });
  if (res.status !== 200) return null;
  try {
    return JSON.parse(res.body).data.accessToken || null;
  } catch (_e) {
    return null;
  }
}

export function checkOk(res, name) {
  return check(res, {
    [name + ' status 200']: (r) => r.status === 200,
    [name + ' body ok']: (r) => {
      try { return JSON.parse(r.body).code === '200'; } catch (_e) { return false; }
    },
  });
}
