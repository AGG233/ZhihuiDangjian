import http from 'k6/http';
import { check } from 'k6';

export const BASE_URL = __ENV.TARGET_HOST || 'http://localhost:9000';
export const TEST_CAPTCHA_CODE = __ENV.CAPTCHA_CODE || 'TEST8888';

// 读取预存用户（从 credentials.json）
var PRESET_USERS = [];
try {
  var creds = JSON.parse(open('./credentials.json'));
  PRESET_USERS = creds.users || [];
} catch (_e) {
  PRESET_USERS = [];
}

export function getAuthParams(token) {
  return {
    headers: {
      'Authorization': 'Bearer ' + token,
      'Content-Type': 'application/json',
    },
  };
}

// 获取验证码 UUID
export function fetchCaptchaUUID() {
  var res = http.get(BASE_URL + '/api/auth/captcha');
  if (res.status !== 200) return 'loadtest-captcha-uuid';
  try {
    var body = JSON.parse(res.body);
    return body.data && body.data.uuid ? body.data.uuid : 'loadtest-captcha-uuid';
  } catch (_e) {
    return 'loadtest-captcha-uuid';
  }
}

// 生成唯一随机字符串（避免多 worker 冲突）
function uniqueRand() {
  var ts = Date.now().toString(36);
  var r = '';
  for (var i = 0; i < 4; i++) {
    r += Math.random().toString(36).substring(2, 6);
  }
  return ts + '-' + r;
}

// 使用预存用户登录（优先）
export function loginWithPreset() {
  if (PRESET_USERS.length === 0) return null;
  var user = PRESET_USERS[Math.floor(Math.random() * PRESET_USERS.length)];

  var captchaUUID = fetchCaptchaUUID();
  var loginPayload = JSON.stringify({
    passport: user.passport,
    password: user.password,
    captchaUUID: captchaUUID,
    captchaCode: TEST_CAPTCHA_CODE,
    platform: 'web',
  });

  var loginRes = http.post(BASE_URL + '/api/auth/login', loginPayload, {
    headers: { 'Content-Type': 'application/json' },
  });

  if (loginRes.status === 200) {
    try {
      return JSON.parse(loginRes.body).data.accessToken || null;
    } catch (_e) {
      return null;
    }
  }
  return null;
}

// 注册用户并登录，返回 token（带重试）
export function registerAndLogin() {
  for (var attempt = 0; attempt < 5; attempt++) {
    var captchaUUID = fetchCaptchaUUID();
    var rand = uniqueRand() + attempt;
    var username = ('load' + rand).substring(0, 16);
    var email = rand + '@example.com';
    var phone = '138' + (Math.floor(Math.random() * 90000000) + 10000000);
    var idcard = '11010119900101' + (Math.floor(Math.random() * 9000) + 1000);
    var pmid = 'PM' + Date.now() + Math.floor(Math.random() * 100000);

    var regPayload = JSON.stringify({
      type: '学生',
      username: username,
      password: 'Test@12345',
      realName: '压测用户',
      idCard: idcard,
      partyMemberId: pmid,
      partyStatus: '正式党员',
      branchName: '测试党支部',
      email: email,
      phone: phone,
      universityId: '001',
      captchaUUID: captchaUUID,
      captchaCode: TEST_CAPTCHA_CODE,
    });

    var regRes = http.post(BASE_URL + '/api/auth/register', regPayload, {
      headers: { 'Content-Type': 'application/json' },
    });

    if (regRes.status !== 200) {
      continue;
    }

    // 注册成功后立即登录
    var captchaUUID2 = fetchCaptchaUUID();
    var loginPayload = JSON.stringify({
      passport: username,
      password: 'Test@12345',
      captchaUUID: captchaUUID2,
      captchaCode: TEST_CAPTCHA_CODE,
      platform: 'web',
    });

    var loginRes = http.post(BASE_URL + '/api/auth/login', loginPayload, {
      headers: { 'Content-Type': 'application/json' },
    });

    if (loginRes.status === 200) {
      try {
        return JSON.parse(loginRes.body).data.accessToken || null;
      } catch (_e) {
        return null;
      }
    }
  }

  return null;
}

export function checkOk(res, name) {
  return check(res, {
    [name + ' 状态码 200']: function (r) { return r.status === 200; },
    [name + ' 响应成功']: function (r) {
      try { return JSON.parse(r.body).code === '200'; } catch (_e) { return false; }
    },
  });
}

// 中文汇总输出
export function handleSummary(data) {
  var httpReqs = data.metrics.http_reqs || {};
  var httpReqsValues = httpReqs.values || {};
  var httpFailed = data.metrics.http_req_failed || {};
  var httpFailedValues = httpFailed.values || {};
  var httpDuration = data.metrics.http_req_duration || {};
  var httpDurationValues = httpDuration.values || {};
  var checks = data.metrics.checks || {};
  var checksValues = checks.values || {};

  var totalReqs = httpReqsValues.count || 0;
  var httpFailRate = httpFailedValues.rate ? (httpFailedValues.rate * 100).toFixed(2) : '0.00';

  var avg = httpDurationValues.avg ? httpDurationValues.avg.toFixed(2) : 'N/A';
  var min = httpDurationValues.min ? httpDurationValues.min.toFixed(2) : 'N/A';
  var med = httpDurationValues.med ? httpDurationValues.med.toFixed(2) : 'N/A';
  var max = httpDurationValues.max ? httpDurationValues.max.toFixed(2) : 'N/A';
  var p90 = httpDurationValues['p(90)'] ? httpDurationValues['p(90)'].toFixed(2) : 'N/A';
  var p95 = httpDurationValues['p(95)'] ? httpDurationValues['p(95)'].toFixed(2) : 'N/A';
  var p99 = httpDurationValues['p(99)'] ? httpDurationValues['p(99)'].toFixed(2) : 'N/A';

  var checkPass = checksValues.passes || 0;
  var checkFail = checksValues.fails || 0;
  var checkTotal = checkPass + checkFail;
  var checkFailRate = checkTotal > 0 ? ((checkFail / checkTotal) * 100).toFixed(2) : '0.00';

  var duration = data.state ? data.state.testRunDurationMs : 1;
  var qps = (duration > 0) ? (totalReqs / (duration / 1000)).toFixed(2) : '0.00';

  var summary = '\n';
  summary += '========================================\n';
  summary += '           压测结果汇总\n';
  summary += '========================================\n';
  summary += '\n';
  summary += '【请求统计】\n';
  summary += '  总请求数: ' + totalReqs + '\n';
  summary += '  HTTP失败率: ' + httpFailRate + '%\n';
  summary += '  检查点失败率: ' + checkFailRate + '%\n';
  summary += '  QPS:      ' + qps + ' req/s\n';
  summary += '\n';
  summary += '【响应延迟】\n';
  summary += '  平均: ' + avg + 'ms\n';
  summary += '  最小: ' + min + 'ms\n';
  summary += '  中位数: ' + med + 'ms\n';
  summary += '  最大: ' + max + 'ms\n';
  summary += '  P90:  ' + p90 + 'ms\n';
  summary += '  P95:  ' + p95 + 'ms\n';
  summary += '  P99:  ' + p99 + 'ms\n';
  summary += '\n';
  summary += '【检查点】\n';
  summary += '  通过: ' + checkPass + '\n';
  summary += '  失败: ' + checkFail + '\n';
  summary += '  通过率: ' + (checkTotal > 0 ? ((checkPass / checkTotal) * 100).toFixed(2) : '0.00') + '%\n';
  summary += '\n';
  summary += '========================================\n';

  return {
    stdout: summary,
  };
}
