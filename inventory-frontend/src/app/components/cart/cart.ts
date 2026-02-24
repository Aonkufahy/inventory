import { Component } from '@angular/core';
import { CartService } from '../../services/cart';
import { Router } from '@angular/router';
import { Cart } from '../../models/cart';

@Component({
  selector: 'app-cart',
  templateUrl: './cart.html',
  styleUrls: ['./cart.css'],
  standalone: false
})
export class CartComponent {
  cart$;

  constructor(private cartService: CartService, private router: Router) {
    this.cart$ = this.cartService.cart$;
  }

  removeFromCart(productId: number | undefined): void {
    if (productId !== undefined) {
      this.cartService.removeFromCart(productId);
    }
  }

  // Add this method to handle quantity updates
  updateQuantity(productId: number, newQuantity: number, currentQuantity: number): void {
    if (productId !== undefined && newQuantity > 0) {
      // Calculate the difference
      const difference = newQuantity - currentQuantity;
      if (difference > 0) {
        // Add more quantity
        for (let i = 0; i < difference; i++) {
          this.addOne(productId);
        }
      } else if (difference < 0) {
        // Remove quantity (but keep at least 1)
        const removeCount = Math.abs(difference);
        for (let i = 0; i < removeCount; i++) {
          this.removeOne(productId);
        }
      }
    }
  }

  // Add helper methods
  addOne(productId: number): void {
    // Find the product from current cart and add one
    let currentCart: Cart = { items: [] };
    this.cart$.subscribe(cart => currentCart = cart).unsubscribe();
    
    const item = currentCart.items.find(i => i.product.id === productId);
    if (item) {
      this.cartService.addToCart(item.product, 1);
    }
  }

  removeOne(productId: number): void {
    // For removing one, we'll use removeFromCart and then add back if needed
    // This is a workaround since your service doesn't have direct update
    let currentCart: Cart = { items: [] };
    this.cart$.subscribe(cart => currentCart = cart).unsubscribe();
    
    const item = currentCart.items.find(i => i.product.id === productId);
    if (item && item.quantity > 1) {
      // Remove the item and add back with quantity-1
      this.cartService.removeFromCart(productId);
      this.cartService.addToCart(item.product, item.quantity - 1);
    } else if (item && item.quantity === 1) {
      this.cartService.removeFromCart(productId);
    }
  }

  checkout(): void {
    this.router.navigate(['/checkout']);
  }

  getTotal(cart: Cart): number {
    return cart.items.reduce((sum, item) => sum + (item.product.price * item.quantity), 0);
  }
}