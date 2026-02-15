import { Component } from '@angular/core';
import { CartService } from '../../services/cart';
import { Router } from '@angular/router';
import { Cart } from '../../models/cart';

@Component({
  selector: 'app-cart',
  templateUrl: './cart.html',
  styleUrls: ['./cart.css'],
  standalone:false
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

  checkout(): void {
    this.router.navigate(['/checkout']);
  }

  getTotal(cart: Cart): number {
    return cart.items.reduce((sum, item) => sum + (item.product.price * item.quantity), 0);
  }
  
}