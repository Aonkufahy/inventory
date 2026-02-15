import { Component } from '@angular/core';
import { AuthService } from './services/auth';  
import { CartService } from './services/cart'; 

@Component({
  selector: 'app-root',
  templateUrl: './app.html',  
  styleUrls: ['./app.css'],  
  standalone: false
})
export class AppComponent {
  title = 'Inventory & Shopping Simulation';
  public currentUser$; 
  public cart$;  

  constructor(public authService: AuthService, private cartService: CartService) {  
    this.currentUser$ = this.authService.currentUser$;
    this.cart$ = this.cartService.cart$;
  }

  logout(): void {
    this.authService.logout();
  }
}