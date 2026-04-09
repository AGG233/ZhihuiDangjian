#!/usr/bin/env node

const BASE_URL = process.env.API_SMOKE_BASE_URL || "http://127.0.0.1:9000";
const CAPTCHA_CODE = process.env.AUTH_TEST_CAPTCHA_CODE || "CI8888";
const LOGIN_PASSWORD = process.env.TEST_LOGIN_PASSWORD || "Test@1234";
const DOCS_URL = new URL("/v3/api-docs", BASE_URL).toString();

const LOGIN_USERS = {
    student: process.env.TEST_LOGIN_PASSPORT || "ci_smoke_user",
    school: process.env.TEST_SCHOOL_LOGIN_PASSPORT || "ci_smoke_school",
    manager: process.env.TEST_MANAGER_LOGIN_PASSPORT || "ci_smoke_manager",
};

const PATH_PARAM_DEFAULTS = {
    id: "1",
    userId: "10001",
    sessionId: "ci-session",
    courseId: "1",
    chapterId: "1",
    categoryId: "1",
    quizId: "1",
    optionId: "1",
    resourceId: "1",
    hash: "ci-hash",
    order: "1",
};

const QUERY_DEFAULTS = {
    pageNum: "1",
    pageSize: "10",
    keyword: "ci",
    categoryId: "1",
    difficulty: "初级",
    uuid: "ci-smoke",
    code: CAPTCHA_CODE,
    token: "ci-smoke-token",
    uploaderId: "10001",
    originalName: "ci-file.txt",
    hash: "ci-hash",
    resourceType: "0",
    status: "1",
};

function isObject(value) {
    return value && typeof value === "object" && !Array.isArray(value);
}

function deref(schema, spec) {
    if (!schema || typeof schema !== "object" || !schema.$ref) {
        return schema;
    }
    const refPath = schema.$ref.replace(/^#\//, "").split("/");
    let current = spec;
    for (const part of refPath) {
        current = current?.[part];
    }
    return current || schema;
}

function mergeSchemas(base, extra) {
    if (!isObject(base)) return extra;
    if (!isObject(extra)) return base;
    return {
        ...base,
        ...extra,
        properties: {...(base.properties || {}), ...(extra.properties || {})},
        required: [...new Set([...(base.required || []), ...(extra.required || [])])],
    };
}

function resolveSchema(schema, spec, seen = new Set()) {
    let resolved = deref(schema, spec);
    if (!resolved || typeof resolved !== "object") {
        return resolved;
    }

    if (resolved.$ref) {
        if (seen.has(resolved.$ref)) {
            return {};
        }
        seen.add(resolved.$ref);
        resolved = deref(resolved, spec);
    }

    if (Array.isArray(resolved.allOf) && resolved.allOf.length > 0) {
        return resolved.allOf.reduce(
            (acc, part) => mergeSchemas(acc, resolveSchema(part, spec, new Set(seen))),
            {}
        );
    }

    if (Array.isArray(resolved.oneOf) && resolved.oneOf.length > 0) {
        return resolveSchema(resolved.oneOf[0], spec, new Set(seen));
    }

    if (Array.isArray(resolved.anyOf) && resolved.anyOf.length > 0) {
        return resolveSchema(resolved.anyOf[0], spec, new Set(seen));
    }

    return resolved;
}

function generateScalar(schema, name = "") {
    if (schema.example !== undefined) return schema.example;
    if (schema.default !== undefined) return schema.default;
    if (Array.isArray(schema.enum) && schema.enum.length > 0) return schema.enum[0];

    const type = schema.type || "string";
    const format = schema.format;

    if (type === "boolean") return true;
    if (type === "integer" || type === "number") return 1;
    if (type === "array") return [];

    if (format === "date-time") return "2024-01-01T00:00:00";
    if (format === "date") return "2024-01-01";
    if (format === "email") return "ci@example.com";
    if (format === "uri" || format === "url") return "https://example.com";
    if (format === "uuid") return "00000000-0000-0000-0000-000000000001";

    const normalizedName = name.toLowerCase();
    if (normalizedName.includes("password")) return LOGIN_PASSWORD;
    if (normalizedName.includes("phone")) return "13800138000";
    if (normalizedName.includes("email")) return "ci@example.com";
    if (normalizedName.includes("name")) return "ci";
    if (normalizedName.includes("title")) return "ci-title";
    if (normalizedName.includes("content")) return "ci-content";
    if (normalizedName.includes("description")) return "ci-description";
    if (normalizedName.includes("passport")) return LOGIN_USERS.manager;
    if (normalizedName.includes("captchauuid")) return "ci-smoke";
    if (normalizedName.includes("captchacode")) return CAPTCHA_CODE;
    if (normalizedName.endsWith("id")) return "1";
    if (normalizedName.includes("hash")) return "ci-hash";
    if (normalizedName.includes("url")) return "https://example.com/resource";

    return "ci";
}

function generateExample(schema, spec, depth = 0, name = "") {
    if (!schema || depth > 6) return {};

    const resolved = resolveSchema(schema, spec);
    if (!resolved || typeof resolved !== "object") return resolved;

    if (resolved.example !== undefined) return resolved.example;
    if (resolved.default !== undefined) return resolved.default;

    if (resolved.type === "array") {
        return [generateExample(resolved.items || {}, spec, depth + 1, name)];
    }

    if (resolved.type === "object" || resolved.properties || resolved.additionalProperties) {
        const properties = resolved.properties || {};
        const required = new Set(resolved.required || []);
        const propertyNames = Object.keys(properties);
        const targetNames = required.size > 0 ? propertyNames.filter((key) => required.has(key)) : propertyNames;
        const result = {};

        for (const key of targetNames) {
            result[key] = generateExample(properties[key], spec, depth + 1, key);
        }

        if (Object.keys(result).length > 0) {
            return result;
        }

        if (isObject(resolved.additionalProperties)) {
            return {ci: generateExample(resolved.additionalProperties, spec, depth + 1, name)};
        }

        return {};
    }

    return generateScalar(resolved, name);
}

function getBodyContent(operation) {
    const content = operation.requestBody?.content || {};
    if (content["application/json"]) {
        return {type: "application/json", schema: content["application/json"].schema};
    }
    if (content["application/x-www-form-urlencoded"]) {
        return {type: "application/x-www-form-urlencoded", schema: content["application/x-www-form-urlencoded"].schema};
    }
    if (content["multipart/form-data"]) {
        return {type: "multipart/form-data", schema: content["multipart/form-data"].schema};
    }
    const [contentType, value] = Object.entries(content)[0] || [];
    return contentType ? {type: contentType, schema: value?.schema} : null;
}

function shouldSkip(pathName, method, operation) {
    const producesEventStream = Object.values(operation.responses || {}).some((response) =>
        Boolean(response?.content?.["text/event-stream"])
    );
    if (producesEventStream) {
        return "SSE interface requires streaming/external AI dependency";
    }

    const bodyContent = getBodyContent(operation);
    if (bodyContent?.type === "multipart/form-data") {
        return "Multipart upload interface requires binary payload";
    }

    if (pathName.startsWith("/api/ai/chat") && method === "post") {
        return "AI chat interface depends on external model service";
    }

    return null;
}

function authScopeForPath(pathName) {
    if (
        pathName.startsWith("/auth/captcha") ||
        pathName === "/auth/login" ||
        pathName.startsWith("/api/school/") ||
        pathName.startsWith("/api/resource/") ||
        pathName.startsWith("/api/ai/")
    ) {
        return "public";
    }
    if (pathName.startsWith("/api/admin/") || pathName.startsWith("/api/search/")) {
        return "manager";
    }
    return "manager";
}

function replacePathParams(pathName, parameters = []) {
    return pathName.replace(/\{([^}]+)\}/g, (_, name) => {
        const param = parameters.find((item) => item.in === "path" && item.name === name) || {};
        const schema = param.schema || {};
        const fallback = PATH_PARAM_DEFAULTS[name] || generateScalar(schema, name);
        return encodeURIComponent(String(fallback));
    });
}

function buildQuery(pathName, parameters = [], spec) {
    const url = new URL(pathName, BASE_URL);
    for (const param of parameters.filter((item) => item.in === "query")) {
        const schema = resolveSchema(param.schema || {}, spec);
        let value = QUERY_DEFAULTS[param.name];
        if (value === undefined) {
            value = generateExample(schema, spec, 0, param.name);
        }
        if (value === undefined || value === null || value === "") {
            continue;
        }
        if (Array.isArray(value)) {
            for (const item of value) {
                url.searchParams.append(param.name, String(item));
            }
            continue;
        }
        if (typeof value === "object") {
            url.searchParams.set(param.name, JSON.stringify(value));
            continue;
        }
        url.searchParams.set(param.name, String(value));
    }
    return url;
}

async function requestJson(url, options = {}) {
    const response = await fetch(url, options);
    const contentType = response.headers.get("content-type") || "";
    const text = await response.text();
    let body = null;
    if (text && contentType.includes("application/json")) {
        try {
            body = JSON.parse(text);
        } catch {
            body = text;
        }
    } else {
        body = text;
    }
    return {response, body};
}

async function login(passport) {
    const {response, body} = await requestJson(new URL("/auth/login", BASE_URL), {
        method: "POST",
        headers: {
            Accept: "application/json",
            "Content-Type": "application/json",
        },
        body: JSON.stringify({
            passport,
            password: LOGIN_PASSWORD,
            platform: "web",
            captchaUUID: "ci-smoke",
            captchaCode: CAPTCHA_CODE,
        }),
    });

    if (!response.ok || body?.code !== "200" || !body?.data?.accessToken) {
        throw new Error(`Login failed for ${passport}: HTTP ${response.status} ${JSON.stringify(body)}`);
    }

    return body.data.accessToken;
}

async function main() {
    const tokens = {
        manager: await login(LOGIN_USERS.manager),
    };

    const {response: docsResponse, body: spec} = await requestJson(DOCS_URL, {
        headers: {
            Accept: "application/json",
            Authorization: `Bearer ${tokens.manager}`,
        },
    });

    if (!docsResponse.ok || !spec?.paths) {
        throw new Error(`Unable to load OpenAPI docs from ${DOCS_URL}: HTTP ${docsResponse.status}`);
    }

    const operations = [];
    for (const [pathName, pathItem] of Object.entries(spec.paths)) {
        for (const method of Object.keys(pathItem)) {
            const normalizedMethod = method.toLowerCase();
            if (!["get", "post", "put", "delete", "patch"].includes(normalizedMethod)) {
                continue;
            }
            operations.push({pathName, method: normalizedMethod, operation: pathItem[method], pathItem});
        }
    }

    operations.sort((a, b) => `${a.pathName}:${a.method}`.localeCompare(`${b.pathName}:${b.method}`));

    let passed = 0;
    let skipped = 0;
    const failures = [];

    for (const item of operations) {
        const {pathName, method, operation, pathItem} = item;
        const skipReason = shouldSkip(pathName, method, operation);
        const label = `${method.toUpperCase()} ${pathName}`;

        if (skipReason) {
            skipped += 1;
            console.log(`SKIP ${label} -> ${skipReason}`);
            continue;
        }

        const parameters = [
            ...((pathItem.parameters || []).map((param) => resolveSchema(param, spec))),
            ...((operation.parameters || []).map((param) => resolveSchema(param, spec))),
        ];

        const resolvedPath = replacePathParams(pathName, parameters);
        const url = buildQuery(resolvedPath, parameters, spec);
        const headers = {Accept: "application/json"};
        const authScope = authScopeForPath(pathName);
        if (authScope !== "public") {
            headers.Authorization = `Bearer ${tokens[authScope] || tokens.manager}`;
        }

        const bodyContent = getBodyContent(operation);
        let requestBody;
        if (bodyContent?.type === "application/json") {
            headers["Content-Type"] = "application/json";
            requestBody = JSON.stringify(generateExample(bodyContent.schema, spec));
        } else if (bodyContent?.type === "application/x-www-form-urlencoded") {
            headers["Content-Type"] = "application/x-www-form-urlencoded";
            const formData = new URLSearchParams();
            const bodyObject = generateExample(bodyContent.schema, spec);
            for (const [key, value] of Object.entries(bodyObject || {})) {
                formData.append(key, String(value));
            }
            requestBody = formData.toString();
        }

        try {
            const {response, body} = await requestJson(url, {
                method: method.toUpperCase(),
                headers,
                body: ["GET", "HEAD"].includes(method.toUpperCase()) ? undefined : requestBody,
            });

            if (response.status >= 500) {
                failures.push({
                    label,
                    status: response.status,
                    body,
                });
                console.log(`FAIL ${label} -> HTTP ${response.status}`);
                continue;
            }

            passed += 1;
            console.log(`PASS ${label} -> HTTP ${response.status}`);
        } catch (error) {
            failures.push({
                label,
                status: "NETWORK_ERROR",
                body: error instanceof Error ? error.message : String(error),
            });
            console.log(`FAIL ${label} -> ${error instanceof Error ? error.message : String(error)}`);
        }
    }

    console.log(`\nSummary: passed=${passed}, skipped=${skipped}, failed=${failures.length}`);

    if (failures.length > 0) {
        for (const failure of failures) {
            const details =
                typeof failure.body === "string" ? failure.body : JSON.stringify(failure.body);
            console.error(`FAILED ${failure.label} -> ${failure.status} ${details}`);
        }
        process.exit(1);
    }
}

main().catch((error) => {
    console.error(error instanceof Error ? error.stack : String(error));
    process.exit(1);
});
