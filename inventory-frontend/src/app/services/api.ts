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
    return new HttpHeaders({
      'Content-Type': 'application/json',
      'Authorization': token ? `Bearer ${token}` : ''
    });
  }

  private getMultipartHeaders(): HttpHeaders {
    const token = localStorage.getItem('token');
    let headers = new HttpHeaders();
    if (token) {
      headers = headers.set('Authorization', `Bearer ${token}`);
    }
    return headers;
  }

  
  getAdminProducts(): Observable<Product[]> {
    console.log('Getting admin products from:', `${this.baseUrl}/admin/products`);
    return this.http.get<Product[]>(`${this.baseUrl}/admin/products`, {
      headers: this.getHeaders()
    });
  }

  addProductWithImage(formData: FormData): Observable<any> {
    const categoryId = formData.get('categoryId');
    console.log('POST to:', `${this.baseUrl}/admin/products/${categoryId}/with-image`);
    console.log('Category ID:', categoryId);
    
    formData.delete('categoryId');
    
    return this.http.post(`${this.baseUrl}/admin/products/${categoryId}/with-image`, formData, {
      headers: this.getMultipartHeaders()
    });
  }


updateProductWithImage(id: number, formData: FormData): Observable<any> {
  console.log('PUT to:', `${this.baseUrl}/admin/products/${id}/with-image`);
  
  if (formData.has('categoryId')) {
    formData.delete('categoryId');
  }
  
  return this.http.put(`${this.baseUrl}/admin/products/${id}/with-image`, formData, {
    headers: this.getMultipartHeaders()
  });
}
updateProduct(id: number, product: Product): Observable<Product> {
  console.log('PUT to:', `${this.baseUrl}/admin/products/${id}`);
  return this.http.put<Product>(`${this.baseUrl}/admin/products/${id}`, product, { 
    headers: this.getHeaders() 
  });
}

  deleteProduct(id: number): Observable<void> {
    console.log('DELETE from:', `${this.baseUrl}/admin/products/${id}`);
    return this.http.delete<void>(`${this.baseUrl}/admin/products/${id}`, { 
      headers: this.getHeaders() 
    });
  }


  getProducts(search?: string, page: number = 0, size: number = 10): Observable<{ content: Product[]; totalElements: number }> {
    let params = new HttpParams().set('page', page.toString()).set('size', size.toString());
    if (search) params = params.set('search', search);
    return this.http.get<{ content: Product[]; totalElements: number }>(`${this.baseUrl}/products`, { 
      params, 
      headers: this.getHeaders() 
    });
  }

  getProduct(id: number): Observable<Product> {
    return this.http.get<Product>(`${this.baseUrl}/products/${id}`, { 
      headers: this.getHeaders() 
    });
  }


  getCategories(): Observable<Category[]> {
    return this.http.get<Category[]>(`${this.baseUrl}/categories`, { 
      headers: this.getHeaders() 
    });
  }

  getOrderHistory(): Observable<Order[]> {
    return this.http.get<Order[]>(`${this.baseUrl}/orders/history`, { 
      headers: this.getHeaders() 
    });
  }

  checkout(order: { items: any[] }): Observable<Order> {
    return this.http.post<Order>(`${this.baseUrl}/orders/checkout`, order, { 
      headers: this.getHeaders() 
    });
  }
}