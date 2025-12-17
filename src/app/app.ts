import { Component, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { OrderService } from './services/order.service';
import { Order } from './models/order';
@Component({
  selector: 'app-root',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './app.html',
  styleUrls: ['./app.css']
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
    this.errorMessage.set('');
    this.orderService.createOrder(this.pizzaType()).subscribe({
      next: res => {
        this.createdOrderCode.set(res.orderCode);
        this.statusOrderCode.set(res.orderCode);
        this.loadStatus();
      },
      error: () => this.errorMessage.set('Error creating order')
    });
  }

/**
 * Loads the status of the order given by statusOrderCode.
 * If no order code is provided, sets the error to 'Please enter an order code'.
 * If the order is not found, sets the error to 'Order not found'.
 * Otherwise, sets the currentOrder to the order status.
 */
loadStatus(): void {
  if (!this.statusOrderCode()) {
    this.errorMessage.set('Please enter an order code');
    this.currentOrder.set(undefined); 
    return;
  }
  this.errorMessage.set('');
  this.orderService.getOrderStatus(this.statusOrderCode()).subscribe({
    next: order => this.currentOrder.set(order),
    error: () => this.errorMessage.set('Order not found')
  });
}

/**
 * Loads the pizza queue.
 * Resets the error message.
 * If successful, sets the queue to the retrieved orders.
 * If an error occurs, sets the error message to 'Error loading pizza queue'.
 */
  loadQueue(): void {
    this.errorMessage.set('');
    this.orderService.getQueue().subscribe({
      next: orders => this.queue.set(orders),
      error: () => this.errorMessage.set('Error loading pizza queue')
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
  if (!code) return;
  this.errorMessage.set('');
  this.orderService.assignOrder(code).subscribe({
    next: order => {
      this.currentOrder.set(order);
      this.loadQueue();
    },
    error: () => this.errorMessage.set('You can only have one order in progress at a time')
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
  if (!code) return;
  this.errorMessage.set('');
  this.orderService.markReady(code).subscribe({
    next: order => {
      this.currentOrder.set(order);
      this.loadQueue();
    },
    error: () => this.errorMessage.set('Error passing to READY status')
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
    if (!code) return;
    this.errorMessage.set('');
    this.orderService.completeOrder(code).subscribe({
      next: order => {
        this.currentOrder.set(order);
        this.loadQueue();
      },
      error: () => this.errorMessage.set('Error completing order')
    });
  }

/**
 * Selects an order from the queue.
 * Sets the selectedOrderCode and statusOrderCode to the selected order code.
 * Loads the status of the selected order.
 * @param code The order code to select.
 */
  selectFromQueue(code: string): void {
    this.selectedOrderCode.set(code);
    this.statusOrderCode.set(code);
    this.loadStatus();
  }

/**
 * Returns true if the current order is in PENDING status and can be assigned to a pizza chef.
 * Returns false if there is no current order or if the order status is not PENDING.
 * Used to enable the 'Take Charge' button in the Pizza Chef Queue view.
 */
 canAssign(): boolean {
  const order = this.currentOrder();
  return !!order && order.status === 'PENDING';
}

/**
 * Returns true if the current order is in IN_PROGRESS status and can be marked as READY.
 * Returns false if there is no current order or if the order status is not IN_PROGRESS.
 * Used to enable the 'Mark Ready' button in the Pizza Chef Queue view.
 */
canMarkReady(): boolean {
  const order = this.currentOrder();
  return !!order && order.status === 'IN_PROGRESS';
}

/**
 * Returns true if the current order is in READY status and can be completed.
 * Returns false if there is no current order or if the order status is not READY.
 * Used to enable the 'Complete Order' button in the Pizza Chef Queue view.
 */
canComplete(): boolean {
  const order = this.currentOrder();
  return !!order && order.status === 'READY';
}
}

