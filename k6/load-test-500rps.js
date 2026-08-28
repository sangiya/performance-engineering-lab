import http from 'k6/http';
import { check } from 'k6';

/**
 * 500 RPS load test — use after confirming 100 RPS headroom.
 * Run: k6 run --env BASE_URL=http://localhost:8080 k6/load-test-500rps.js
 */
export const options = {
  scenarios: {
    ramp_to_500rps: {
      executor: 'ramping-arrival-rate',
      startRate: 50,
      timeUnit: '1s',
      preAllocatedVUs: 100,
      maxVUs: 600,
      stages: [
        { duration: '2m', target: 500 },
        { duration: '5m', target: 500 },
        { duration: '1m', target: 0 },
      ],
    },
  },
  thresholds: {
    http_req_failed: ['rate<0.01'],
    http_req_duration: ['p(95)<300', 'p(99)<800'],
  },
};

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';

export default function () {
  const payload = JSON.stringify({
    customerId: `cust-${Math.floor(Math.random() * 5000)}`,
    productId: `prod-${Math.floor(Math.random() * 100)}`,
    quantity: Math.floor(Math.random() * 5) + 1,
    unitPrice: (Math.random() * 50 + 5).toFixed(2),
  });

  const res = http.post(`${BASE_URL}/api/orders`, payload, {
    headers: { 'Content-Type': 'application/json' },
  });

  check(res, { 'order placed': (r) => r.status === 201 });
}
