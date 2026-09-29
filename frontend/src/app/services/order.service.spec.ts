import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { OrderService } from './order.service';

describe('OrderService API integration contract', () => {
  let service: OrderService;
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(OrderService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('creates an order using the same-origin API', () => {
    service.createOrder('Margherita').subscribe(result => {
      expect(result.orderCode).toBe('ORD-TEST');
    });
    const request = http.expectOne('/api/orders');
    expect(request.request.method).toBe('POST');
    expect(request.request.body).toEqual({ pizzaType: 'Margherita' });
    request.flush({ orderCode: 'ORD-TEST' });
  });

  it('loads the order status', () => {
    service.getOrderStatus('ORD-TEST').subscribe();
    const request = http.expectOne('/api/orders/ORD-TEST');
    expect(request.request.method).toBe('GET');
    request.flush({});
  });

  it('loads the chef queue', () => {
    service.getQueue().subscribe(result => expect(result).toEqual([]));
    const request = http.expectOne('/api/orders/queue');
    expect(request.request.method).toBe('GET');
    request.flush([]);
  });

  for (const action of ['assign', 'ready', 'complete'] as const) {
    it(`updates an order through the ${action} endpoint`, () => {
      const call = {
        assign: () => service.assignOrder('ORD-TEST'),
        ready: () => service.markReady('ORD-TEST'),
        complete: () => service.completeOrder('ORD-TEST'),
      };
      call[action]().subscribe();
      const request = http.expectOne(`/api/orders/ORD-TEST/${action}`);
      expect(request.request.method).toBe('PUT');
      request.flush({});
    });
  }
});
