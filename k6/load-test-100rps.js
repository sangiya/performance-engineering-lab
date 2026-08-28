import http from 'k6/http';
import { check } from 'k6';

/**
 * 100 RPS load test — ramping up over 2 min, sustained for 5 min, ramping down.
 * Run: k6 run --env BASE_URL=http://localhost:8080 k6/load-test-100rps.js
 */
export const options = {
  scenarios: {
    constant_request_rate: {
      executor: 'constant-arrival-rate',
      rate: 100,
      timeUnit: '1s',
      duration: '5m',
      preAllocatedVUs: 50,
      maxVUs: 200,
    },
  },
  thresholds: {
    http_req_failed: ['rate<0.005'],
    http_req_duration: ['p(95)<200', 'p(99)<500'],
  },
};

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';

export default function () {
  const payload = JSON.stringify({
    customerId: `cust-${Math.floor(Math.random() * 1000)}`,
    productId: `prod-${Math.floor(Math.random() * 50)}`,
    quantity: Math.floor(Math.random() * 10) + 1,
    unitPrice: (Math.random() * 100 + 1).toFixed(2),
  });

  const res = http.post(`${BASE_URL}/api/orders`, payload, {
    headers: { 'Content-Type': 'application/json' },
  });

  check(res, { 'order placed': (r) => r.status === 201 });
}
