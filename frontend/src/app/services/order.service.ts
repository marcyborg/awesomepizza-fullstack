import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { Order } from '../models/order';

@Injectable({
  providedIn: 'root'
})
export class OrderService {

  private readonly baseUrl = environment.apiBaseUrl + '/api/orders';

  constructor(private http: HttpClient) {}

  /**
   * Create a new order.
   * @param pizzaType The type of pizza to order.
   * @returns An observable with the order code.
   */
  createOrder(pizzaType: string): Observable<{ orderCode: string }> {
    return this.http.post<{ orderCode: string }>(this.baseUrl, { pizzaType });
  }

  /**
   * Retrieve the status of an order.
   * @param code The order code to retrieve.
   * @returns An observable with the order status.
   */
  getOrderStatus(code: string): Observable<Order> {
    return this.http.get<Order>(`${this.baseUrl}/${code}`);
  }

  /**
   * Retrieves the list of orders in the queue.
   * @returns An observable with an array of Order objects.
   */
  getQueue(): Observable<Order[]> {
    return this.http.get<Order[]>(`${this.baseUrl}/queue`);
  }

  /**
   * Assign an order to a pizza chef.
   * @param code The order code to assign.
   * @returns An observable with the updated order status.
   */
  assignOrder(code: string): Observable<Order> {
    return this.http.put<Order>(`${this.baseUrl}/${code}/assign`, {});
  }

  /**
   * Mark an order as READY.
   * @param code The order code to mark as READY.
   * @returns An observable with the updated order status.
   */
  markReady(code: string): Observable<Order> {
  return this.http.put<Order>(`${this.baseUrl}/${code}/ready`, {});
}

  /**
   * Completes an order.
   * @param code The order code to complete.
   * @returns An observable with the updated order status.
   */
  completeOrder(code: string): Observable<Order> {
    return this.http.put<Order>(`${this.baseUrl}/${code}/complete`, {});
  }
}