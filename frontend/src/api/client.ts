type RequestBody = BodyInit | object | null | undefined;

export class ApiClientError extends Error {
  readonly status: number;
  readonly payload: unknown;

  constructor(status: number, payload: unknown) {
    super(`API request failed with status ${status}`);
    this.name = "ApiClientError";
    this.status = status;
    this.payload = payload;
  }
}

export interface ApiRequestOptions extends Omit<RequestInit, "body"> {
  body?: RequestBody;
}

const apiPathPattern = /^\/api(?:\/|$)/;
const absoluteUrlPattern = /^(?:[a-z][a-z\d+.-]*:)?\/\//i;

export function assertRelativeApiPath(path: string): void {
  if (!apiPathPattern.test(path) || absoluteUrlPattern.test(path)) {
    throw new Error("API path must be a browser-relative /api path.");
  }
}

function serializeBody(body: RequestBody): BodyInit | undefined {
  if (body == null) {
    return undefined;
  }
  if (
    typeof body === "string" ||
    body instanceof FormData ||
    body instanceof Blob ||
    body instanceof URLSearchParams
  ) {
    return body;
  }
  return JSON.stringify(body);
}

function hasJsonBody(body: RequestBody): boolean {
  return (
    body != null &&
    !(body instanceof FormData) &&
    !(body instanceof Blob) &&
    !(body instanceof URLSearchParams)
  );
}

export async function apiFetch<TResponse>(
  path: string,
  options: ApiRequestOptions = {},
): Promise<TResponse> {
  assertRelativeApiPath(path);

  const headers = new Headers(options.headers);
  if (hasJsonBody(options.body) && !headers.has("Content-Type")) {
    headers.set("Content-Type", "application/json");
  }
  if (!headers.has("Accept")) {
    headers.set("Accept", "application/json");
  }

  const response = await fetch(path, {
    ...options,
    credentials: options.credentials ?? "include",
    headers,
    body: serializeBody(options.body),
  });

  const contentType = response.headers.get("content-type") ?? "";
  const payload = contentType.includes("application/json")
    ? await response.json()
    : await response.text();

  if (!response.ok) {
    throw new ApiClientError(response.status, payload);
  }

  return payload as TResponse;
}
