import { Component, OnInit } from '@angular/core';
import { CartService } from '../../services/cart';
import { ApiService } from '../../services/api';
import { Router } from '@angular/router';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { timeout } from 'rxjs/operators';

@Component({
  selector: 'app-checkout',
  templateUrl: './checkout.html',
  styleUrls: ['./checkout.css'],
  standalone: false
})
export class CheckoutComponent implements OnInit {
  cart$;
  total = 0;
  isPlacingOrder = false;
  cartSnapshot: any = null;

  constructor(private cartService: CartService, private apiService: ApiService, private router: Router, private http: HttpClient) {
    this.cart$ = this.cartService.cart$;
  }

  ngOnInit(): void {
    this.cart$.subscribe(cart => {
      this.cartSnapshot = { ...cart };
      this.total = cart.items.reduce((sum, item) => sum + (item.product.price * item.quantity), 0);
    });
  }

  placeOrder(): void {
    if (this.isPlacingOrder || !this.cartSnapshot || this.cartSnapshot.items.length === 0) return;
    this.isPlacingOrder = true;
    console.log('Starting placeOrder');  
    const order = { items: this.cartSnapshot.items.map((item: any) => ({ productId: item.product.id, quantity: item.quantity })) };
    console.log('Sending order payload:', order);  

    
    this.apiService.checkout(order).pipe(timeout(5000)).subscribe(
      response => {
        console.log('Checkout success via proxy:', response);  
        this.handleSuccess();
      },
      error => {
        console.log('Proxy failed, trying direct URL');  
        this.tryDirectCheckout(order);
      }
    );
  }

  private tryDirectCheckout(order: any): void {
    const token = localStorage.getItem('token');
    const headers = new HttpHeaders({ 'Content-Type': 'application/json', 'Authorization': `Bearer ${token}` });
    this.http.post('http://localhost:8080/api/orders/checkout', order, { headers }).pipe(timeout(5000)).subscribe(
      response => {
        console.log('Checkout success via direct URL:', response);  
        this.handleSuccess();
      },
      error => {
        console.error('Direct URL also failed:', error);  
        alert('Checkout failed. Please try again.');
        this.isPlacingOrder = false;
      }
    );
  }
    clearCart():void{
    this.cartService.clearCart();
    console.log('Cart cleared'); 
  }
  private handleSuccess(): void {
    this.cartService.clearCart();
    console.log('Cart cleared, navigating to /orders'); 
    this.router.navigate(['/orders']);
  }

  goBack(): void {
    this.router.navigate(['/products']);
  }
}