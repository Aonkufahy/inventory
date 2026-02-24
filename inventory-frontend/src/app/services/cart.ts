import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';
import { Cart, CartItem } from '../models/cart';
import { Product } from '../models/product';
import { AuthService } from './auth';

@Injectable({
  providedIn: 'root'
})
export class CartService {
  private cartSubject = new BehaviorSubject<Cart>({ items: [] });
  public cart$ = this.cartSubject.asObservable();
  private currentUserId: string | null = null;

  constructor(private authService: AuthService) {
    this.authService.currentUser$.subscribe(user => {
      this.currentUserId = user ? user.username : null;
      this.loadCartForUser();
    });
  }

  private loadCartForUser(): void {
    if (this.currentUserId) {
      const cartKey = `cart_${this.currentUserId}`;
      const savedCart = localStorage.getItem(cartKey);
      if (savedCart) {
        this.cartSubject.next(JSON.parse(savedCart));
      } else {
        this.cartSubject.next({ items: [] });
      }
    } else {
      this.cartSubject.next({ items: [] });
    }
  }

  addToCart(product: Product, quantity: number = 1): void {
    if (!this.currentUserId) {
      alert('Please log in to add items to cart.');
      return;
    }
    const cart = this.cartSubject.value;
    const existingItem = cart.items.find(item => item.product.id === product.id);
    if (existingItem) {
      existingItem.quantity += quantity;
    } else {
      cart.items.push({ product, quantity });
    }
    this.saveCart(cart);
  }

  removeFromCart(productId: number): void {
    if (!this.currentUserId) return;
    const cart = this.cartSubject.value;
    cart.items = cart.items.filter(item => item.product.id !== productId);
    this.saveCart(cart);
  }
  

  clearCart(): void {
    if (this.currentUserId) {
      const cartKey = `cart_${this.currentUserId}`;
      localStorage.removeItem(cartKey);
    }
    this.cartSubject.next({ items: [] });
  }

  private saveCart(cart: Cart): void {
    if (this.currentUserId) {
      const cartKey = `cart_${this.currentUserId}`;
      localStorage.setItem(cartKey, JSON.stringify(cart));
    }
    this.cartSubject.next(cart);
  }
}