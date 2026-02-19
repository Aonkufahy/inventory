import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Product } from '../models/product';
import { Category } from '../models/category';
import { Order } from '../models/order';

@Injectable({
  providedIn: 'root'
})
export class ApiService {
  private baseUrl = 'http://localhost:8080/api';

  constructor(private http: HttpClient) {}

 private getHeaders(): HttpHeaders {
  const token = localStorage.getItem('token');
  console.log('Token from localStorage:', token);  
  let headers = new HttpHeaders({ 'Content-Type': 'application/json' });
  if (token) {
    headers = headers.set('Authorization', `Bearer ${token}`);
    console.log('Authorization header set:', `Bearer ${token}`);  
  } else {
    console.log('No token found');  
  }
  return headers;
}

  
  getProducts(search?: string, page: number = 0, size: number = 10): Observable<{ content: Product[]; totalElements: number }> {
    let params = new HttpParams().set('page', page.toString()).set('size', size.toString());
    if (search) params = params.set('search', search);
    return this.http.get<{ content: Product[]; totalElements: number }>(`${this.baseUrl}/products`, { params, headers: this.getHeaders() });
  }

  getProduct(id: number): Observable<Product> {
    return this.http.get<Product>(`${this.baseUrl}/products/${id}`, { headers: this.getHeaders() });
  }

  
  getAdminProducts(): Observable<Product[]> {
    return this.http.get<Product[]>(`${this.baseUrl}/admin/products`, { headers: this.getHeaders() });
  }

  addProduct(categoryId: number, product: Product): Observable<Product> {
    return this.http.post<Product>(`${this.baseUrl}/admin/products/${categoryId}`, product, { headers: this.getHeaders() });
  }

  updateProduct(id: number, product: Product): Observable<Product> {
    return this.http.put<Product>(`${this.baseUrl}/admin/products/${id}`, product, { headers: this.getHeaders() });
  }

  deleteProduct(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/admin/products/${id}`, { headers: this.getHeaders() });
  }

  
  getCategories(): Observable<Category[]> {
    return this.http.get<Category[]>(`${this.baseUrl}/categories`, { headers: this.getHeaders() });
  }

  getOrderHistory(): Observable<Order[]> {
    return this.http.get<Order[]>(`${this.baseUrl}/orders/history`, { headers: this.getHeaders() });
  }

  checkout(order: { items: any[] }): Observable<Order> {
  const headers = this.getHeaders();
  console.log('Checkout headers:', headers);  
  console.log('Checkout URL:', `${this.baseUrl}/orders/checkout`);  
  return this.http.post<Order>(`${this.baseUrl}/orders/checkout`, order, { headers });
}
}