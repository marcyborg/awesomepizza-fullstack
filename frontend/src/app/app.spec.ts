import { TestBed } from '@angular/core/testing';
import { App } from './app';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';

describe('App', () => {
  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [App],
      providers: [provideHttpClient(), provideHttpClientTesting()],
    }).compileComponents();
  });

  it('should create the app', () => {
    const fixture = TestBed.createComponent(App);
    const app = fixture.componentInstance;
    expect(app).toBeTruthy();
  });

  it('should render title', async () => {
    const fixture = TestBed.createComponent(App);
    await fixture.whenStable();
    const compiled = fixture.nativeElement as HTMLElement;
    expect(compiled.querySelector('h1')?.textContent).toContain('Awesome Pizza');
  });

  it('clears stale order details when a lookup fails', () => {
    const fixture = TestBed.createComponent(App);
    const http = TestBed.inject(HttpTestingController);
    const app = fixture.componentInstance;
    app.statusOrderCode.set('ORD-FOUND');
    app.loadStatus();
    http.expectOne('/api/orders/ORD-FOUND').flush({
      orderCode: 'ORD-FOUND',
      pizzaType: 'Margherita',
      status: 'PENDING',
    });
    expect(app.currentOrder()?.orderCode).toBe('ORD-FOUND');
    app.statusOrderCode.set('ORD-MISSING');
    app.loadStatus();
    expect(app.currentOrder()).toBeUndefined();
    http.expectOne('/api/orders/ORD-MISSING').flush({}, { status: 404, statusText: 'Not Found' });
    expect(app.errorMessage()).toBe('Order not found');
    expect(app.currentOrder()).toBeUndefined();
    http.verify();
  });

  it('prevents an action on a different order and ignores stale lookups', () => {
    const fixture = TestBed.createComponent(App);
    const http = TestBed.inject(HttpTestingController);
    const app = fixture.componentInstance;
    app.setRole('chef');
    app.selectedOrderCode.set('ORD-ONE');
    app.statusOrderCode.set('ORD-ONE');
    app.loadStatus();
    const oldRequest = http.expectOne('/api/orders/ORD-ONE');
    app.statusOrderCode.set('ORD-TWO');
    app.onSelectedCodeChange('ORD-TWO');
    app.loadStatus();
    http.expectOne('/api/orders/ORD-TWO').flush({
      orderCode: 'ORD-TWO',
      pizzaType: 'Funghi',
      status: 'COMPLETED',
    });
    oldRequest.flush({ orderCode: 'ORD-ONE', pizzaType: 'Margherita', status: 'PENDING' });
    expect(app.currentOrder()?.orderCode).toBe('ORD-TWO');
    expect(app.canAssign()).toBe(false);
    app.assignSelected();
    http.expectNone('/api/orders/ORD-TWO/assign');
    http.verify();
  });

  it('invalidates the loaded selection when the chef edits its code', () => {
    const app = TestBed.createComponent(App).componentInstance;
    const http = TestBed.inject(HttpTestingController);
    app.setRole('chef');
    app.selectedOrderCode.set('ORD-ONE');
    app.currentOrder.set({ orderCode: 'ORD-ONE', pizzaType: 'Margherita', status: 'PENDING' });
    expect(app.canAssign()).toBe(true);
    app.onSelectedCodeChange('ORD-TWO');
    expect(app.currentOrder()).toBeUndefined();
    expect(app.canAssign()).toBe(false);
    app.loadSelected();
    http.expectOne('/api/orders/ORD-TWO').flush({
      orderCode: 'ORD-TWO',
      pizzaType: 'Funghi',
      status: 'PENDING',
    });
    expect(app.canAssign()).toBe(true);
    http.verify();
  });

  it('keeps a conflict message after refreshing the queue', () => {
    const app = TestBed.createComponent(App).componentInstance;
    const http = TestBed.inject(HttpTestingController);
    app.setRole('chef');
    app.selectedOrderCode.set('ORD-ONE');
    app.currentOrder.set({ orderCode: 'ORD-ONE', pizzaType: 'Margherita', status: 'PENDING' });
    app.assignSelected();
    expect(app.busy()).toBe(true);
    app.assignSelected();
    http
      .expectOne('/api/orders/ORD-ONE/assign')
      .flush(
        { detail: 'There is already an active order' },
        { status: 409, statusText: 'Conflict' },
      );
    http.expectOne('/api/orders/queue').flush([]);
    expect(app.errorMessage()).toContain('another order is active');
    expect(app.currentOrder()).toBeUndefined();
    expect(app.busy()).toBe(false);
    http.verify();
  });

  it('rejects a blank pizza and prevents duplicate create requests', () => {
    const app = TestBed.createComponent(App).componentInstance;
    const http = TestBed.inject(HttpTestingController);
    app.pizzaType.set('   ');
    app.createOrder();
    http.expectNone('/api/orders');
    expect(app.errorMessage()).toContain('Pizza type is required');
    app.pizzaType.set(' Margherita ');
    app.createOrder();
    app.createOrder();
    const request = http.expectOne('/api/orders');
    expect(request.request.body).toEqual({ pizzaType: 'Margherita' });
    request.flush({ orderCode: 'ORD-NEW' });
    http.expectOne('/api/orders/ORD-NEW').flush({
      orderCode: 'ORD-NEW',
      pizzaType: 'Margherita',
      status: 'PENDING',
    });
    expect(app.busy()).toBe(false);
    http.verify();
  });
});
