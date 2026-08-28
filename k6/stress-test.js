import http from 'k6/http';
import { check, sleep } from 'k6';

/**
 * Spike stress test — sudden traffic burst to find the breaking point.
 * Run: k6 run --env BASE_URL=http://localhost:8080 k6/stress-test.js
 */
export const options = {
  scenarios: {
    spike: {
      executor: 'ramping-vus',
      startVUs: 10,
      stages: [
        { duration: '1m', target: 50 },    // warm up
        { duration: '30s', target: 1000 }, // spike
        { duration: '2m', target: 1000 },  // sustain spike
        { duration: '1m', target: 50 },    // recover
        { duration: '1m', target: 10 },    // ramp down
      ],
    },
  },
  thresholds: {
    http_req_failed: ['rate<0.05'],        // 5% error budget under stress
    http_req_duration: ['p(95)<2000'],
  },
};

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';

export default function () {
  const payload = JSON.stringify({
    customerId: `stress-cust-${__VU}`,
    productId: 'stress-prod',
    quantity: 1,
    unitPrice: 1.0,
  });

  const res = http.post(`${BASE_URL}/api/orders`, payload, {
    headers: { 'Content-Type': 'application/json' },
    timeout: '5s',
  });

  check(res, { 'accepted or created': (r) => r.status === 201 || r.status === 503 });
  sleep(0.1);
}
