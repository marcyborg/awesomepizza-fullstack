import { Component, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { OrderService } from './services/order.service';
import { Order } from './models/order';
import { HttpErrorResponse } from '@angular/common/http';
@Component({
  selector: 'app-root',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './app.html',
  styleUrls: ['./app.css'],
})
export class App {
  title = signal('Awesome Pizza');
  pizzaType = signal('Margherita');
  createdOrderCode = signal('');
  statusOrderCode = signal('');
  currentOrder = signal<Order | undefined>(undefined);
  errorMessage = signal('');
  queue = signal<Order[]>([]);
  selectedOrderCode = signal('');
  busy = signal(false);
  lookupLoading = signal(false);
  private requestId = 0;

  view = signal<'new' | 'status' | 'queue'>('new');

  /**
   * Sets the current view to 'new', 'status', or 'queue'.
   * @param v - The view to set.
   */
  setView(v: 'new' | 'status' | 'queue'): void {
    this.view.set(v);
  }

  role = signal<'user' | 'chef'>('user');

  /**
   * Sets the current role to 'user' or 'chef'.
   * @param r - The role to set.
   * If 'user', sets the view to 'new'. If 'chef', sets the view to 'queue'.
   */
  setRole(r: 'user' | 'chef'): void {
    this.role.set(r);
    this.currentOrder.set(undefined);
    this.errorMessage.set('');
    this.requestId++;
    this.lookupLoading.set(false);
    if (r === 'user') {
      this.view.set('new');
    } else {
      this.view.set('queue');
    }
  }

  constructor(private orderService: OrderService) {}

  /**
   * Creates a new order with the current pizza type.
   * Sets the createdOrderCode and statusOrderCode to the new order code.
   * Loads the status of the new order.
   */
  createOrder(): void {
    if (this.busy()) return;
    const pizzaType = this.pizzaType().trim();
    if (!pizzaType || pizzaType.length > 255) {
      this.errorMessage.set('Pizza type is required and must be at most 255 characters');
      return;
    }
    this.errorMessage.set('');
    this.createdOrderCode.set('');
    this.busy.set(true);
    this.orderService.createOrder(pizzaType).subscribe({
      next: (res) => {
        this.busy.set(false);
        this.createdOrderCode.set(res.orderCode);
        this.statusOrderCode.set(res.orderCode);
        this.loadStatus();
      },
      error: (error) => {
        this.busy.set(false);
        this.errorMessage.set(this.describeError(error, 'Error creating order'));
      },
    });
  }

  /**
   * Loads the status of the order given by statusOrderCode.
   * If no order code is provided, sets the error to 'Please enter an order code'.
   * If the order is not found, sets the error to 'Order not found'.
   * Otherwise, sets the currentOrder to the order status.
   */
  loadStatus(): void {
    const code = this.statusOrderCode().trim();
    const request = ++this.requestId;
    this.currentOrder.set(undefined);
    this.lookupLoading.set(false);
    if (!code) {
      this.errorMessage.set('Please enter an order code');
      return;
    }
    this.errorMessage.set('');
    this.lookupLoading.set(true);
    this.orderService.getOrderStatus(code).subscribe({
      next: (order) => {
        if (request === this.requestId) {
          this.lookupLoading.set(false);
          this.currentOrder.set(order);
        }
      },
      error: (error) => {
        if (request === this.requestId) {
          this.lookupLoading.set(false);
          this.currentOrder.set(undefined);
          this.errorMessage.set(error.status === 404 ? 'Order not found' : 'Unable to load order');
        }
      },
    });
  }

  /**
   * Loads the pizza queue.
   * Resets the error message.
   * If successful, sets the queue to the retrieved orders.
   * If an error occurs, sets the error message to 'Error loading pizza queue'.
   */
  loadQueue(preserveError = false): void {
    if (!preserveError) this.errorMessage.set('');
    this.orderService.getQueue().subscribe({
      next: (orders) => this.queue.set(orders),
      error: () => this.errorMessage.set('Error loading pizza queue'),
    });
  }

  /**
   * Assigns an order to a pizza chef.
   * If no order code is provided, returns.
   * Resets the error message.
   * If successful, sets the currentOrder to the updated order status and loads the queue.
   * If an error occurs, sets the error message to 'You can only have one order in progress at a time'.
   */

  assignSelected(): void {
    const code = this.selectedOrderCode();
    if (!this.canAssign() || this.busy()) return;
    this.errorMessage.set('');
    this.busy.set(true);
    this.orderService.assignOrder(code).subscribe({
      next: (order) => {
        this.busy.set(false);
        if (this.role() === 'chef' && this.selectedOrderCode() === code)
          this.currentOrder.set(order);
        this.loadQueue(true);
      },
      error: (error) => {
        this.busy.set(false);
        this.errorMessage.set(
          error.status === 409
            ? 'Order state changed or another order is active. Reload the queue.'
            : this.describeError(error, 'Unable to take charge of the order'),
        );
        this.currentOrder.set(undefined);
        this.loadQueue(true);
      },
    });
  }

  /**
   * Marks the selected order as READY.
   * If no order code is provided, returns.
   * Resets the error message.
   * If successful, sets the currentOrder to the updated order status and loads the queue.
   * If an error occurs, sets the error message to 'Error passing to READY status'.
   */
  markReadySelected(): void {
    const code = this.selectedOrderCode();
    if (!this.canMarkReady() || this.busy()) return;
    this.errorMessage.set('');
    this.busy.set(true);
    this.orderService.markReady(code).subscribe({
      next: (order) => {
        this.busy.set(false);
        if (this.role() === 'chef' && this.selectedOrderCode() === code)
          this.currentOrder.set(order);
        this.loadQueue(true);
      },
      error: (error) => {
        this.busy.set(false);
        this.currentOrder.set(undefined);
        this.errorMessage.set(this.describeError(error, 'Error passing to READY status'));
        this.loadQueue(true);
      },
    });
  }

  /**
   * Completes the selected order.
   * If no order code is provided, returns.
   * Resets the error message.
   * If successful, sets the currentOrder to the updated order status and loads the queue.
   * If an error occurs, sets the error message to 'Error completing order'.
   */
  completeSelected(): void {
    const code = this.selectedOrderCode();
    if (!this.canComplete() || this.busy()) return;
    this.errorMessage.set('');
    this.busy.set(true);
    this.orderService.completeOrder(code).subscribe({
      next: (order) => {
        this.busy.set(false);
        if (this.role() === 'chef' && this.selectedOrderCode() === code)
          this.currentOrder.set(order);
        this.loadQueue(true);
      },
      error: (error) => {
        this.busy.set(false);
        this.currentOrder.set(undefined);
        this.errorMessage.set(this.describeError(error, 'Error completing order'));
        this.loadQueue(true);
      },
    });
  }

  /**
   * Selects an order from the queue.
   * Sets the selectedOrderCode and statusOrderCode to the selected order code.
   * Loads the status of the selected order.
   * @param code The order code to select.
   */
  selectFromQueue(code: string): void {
    if (this.busy()) return;
    this.selectedOrderCode.set(code);
    this.statusOrderCode.set(code);
    this.loadStatus();
  }
  onSelectedCodeChange(code: string): void {
    this.selectedOrderCode.set(code);
    this.currentOrder.set(undefined);
    this.errorMessage.set('');
    this.requestId++;
    this.lookupLoading.set(false);
  }
  loadSelected(): void {
    this.selectedOrderCode.set(this.selectedOrderCode().trim());
    this.statusOrderCode.set(this.selectedOrderCode());
    this.loadStatus();
  }
  onStatusCodeChange(code: string): void {
    this.statusOrderCode.set(code);
    this.currentOrder.set(undefined);
    this.requestId++;
    this.lookupLoading.set(false);
    this.errorMessage.set('');
  }
  private describeError(error: HttpErrorResponse, fallback: string): string {
    if (error.status === 404) return 'Order not found';
    if (error.status === 409) return 'Order state changed. Reload the queue.';
    if (error.status === 400) return 'Invalid order data';
    if (error.status === 0) return 'Unable to reach the backend';
    return fallback;
  }

  /**
   * Returns true if the current order is in PENDING status and can be assigned to a pizza chef.
   * Returns false if there is no current order or if the order status is not PENDING.
   * Used to enable the 'Take Charge' button in the Pizza Chef Queue view.
   */
  canAssign(): boolean {
    const order = this.currentOrder();
    return (
      this.role() === 'chef' &&
      !this.lookupLoading() &&
      !this.busy() &&
      !!order &&
      order.orderCode === this.selectedOrderCode() &&
      order.status === 'PENDING'
    );
  }

  /**
   * Returns true if the current order is in IN_PROGRESS status and can be marked as READY.
   * Returns false if there is no current order or if the order status is not IN_PROGRESS.
   * Used to enable the 'Mark Ready' button in the Pizza Chef Queue view.
   */
  canMarkReady(): boolean {
    const order = this.currentOrder();
    return (
      this.role() === 'chef' &&
      !this.lookupLoading() &&
      !this.busy() &&
      !!order &&
      order.orderCode === this.selectedOrderCode() &&
      order.status === 'IN_PROGRESS'
    );
  }

  /**
   * Returns true if the current order is in READY status and can be completed.
   * Returns false if there is no current order or if the order status is not READY.
   * Used to enable the 'Complete Order' button in the Pizza Chef Queue view.
   */
  canComplete(): boolean {
    const order = this.currentOrder();
    return (
      this.role() === 'chef' &&
      !this.lookupLoading() &&
      !this.busy() &&
      !!order &&
      order.orderCode === this.selectedOrderCode() &&
      order.status === 'READY'
    );
  }
}
